"""Collect trusted formal outputs from the active remote sandbox execution."""

from __future__ import annotations

import hashlib
import json
import mimetypes
import posixpath
from dataclasses import dataclass
from pathlib import PurePosixPath
from typing import Mapping, Protocol, Sequence

from agent_runtime.conversation.execution_context import (
    get_conversation_execution_context,
)
from agent_runtime.conversation.operation_result import operation_error_detail, operation_succeeded
from agent_runtime.conversation.sandbox.remote_directories import remote_directory_command


DEFAULT_MAX_OUTPUT_FILES = 20
DEFAULT_MAX_OUTPUT_FILE_SIZE = 60 * 1024 * 1024
DEFAULT_MAX_OUTPUT_TOTAL_SIZE = 200 * 1024 * 1024

_ALLOWED_EXTENSIONS = frozenset({
    # Documents and tabular data.
    ".csv", ".doc", ".docx", ".md", ".pdf", ".ppt", ".pptx", ".txt",
    ".xls", ".xlsx",
    # Images.
    ".bmp", ".gif", ".jpeg", ".jpg", ".png", ".svg", ".webp",
    # Archives.
    ".7z", ".gz", ".tar", ".tgz", ".zip",
    # Code and structured text.
    ".c", ".cpp", ".cs", ".css", ".go", ".h", ".hpp", ".html",
    ".java", ".js", ".json", ".py", ".rs", ".sh", ".sql", ".ts",
    ".xml", ".yaml", ".yml",
})


class OutputArtifactCollectionError(RuntimeError):
    """A sandbox output could not be collected within the trusted boundary."""


@dataclass(frozen=True, slots=True)
class SandboxOutputEntry:
    """Remote lstat metadata for one candidate output entry."""

    path: str
    size: int
    is_symlink: bool
    is_regular_file: bool = True


@dataclass(frozen=True, slots=True)
class CollectedOutputArtifact:
    """Verified bytes and durable metadata ready for object-storage upload."""

    sandbox_path: str
    file_name: str
    size: int
    media_type: str
    checksum: str
    content: bytes


class SandboxOutputSource(Protocol):
    """Program-internal boundary for inspecting and reading a remote sandbox."""

    async def scan(self, root: str) -> Sequence[SandboxOutputEntry]: ...

    async def read_bytes(self, path: str) -> bytes: ...


class ConversationOutputCollector:
    """Validate every candidate before reading formal output bytes."""

    def __init__(
        self,
        source: SandboxOutputSource,
        *,
        max_files: int = DEFAULT_MAX_OUTPUT_FILES,
        max_file_size: int = DEFAULT_MAX_OUTPUT_FILE_SIZE,
        max_total_size: int = DEFAULT_MAX_OUTPUT_TOTAL_SIZE,
        allowed_extensions: frozenset[str] = _ALLOWED_EXTENSIONS,
    ) -> None:
        if max_files <= 0 or max_file_size <= 0 or max_total_size <= 0:
            raise ValueError("output collection limits must be positive")
        self._source = source
        self._max_files = max_files
        self._max_file_size = max_file_size
        self._max_total_size = max_total_size
        self._allowed_extensions = frozenset(
            extension.lower() for extension in allowed_extensions
        )

    async def collect(
        self, *, baseline: Mapping[str, str] | None = None
    ) -> list[CollectedOutputArtifact]:
        artifacts = await self._collect_all(enforce_batch_limits=baseline is None)
        if baseline is None:
            return artifacts
        output_root = get_conversation_execution_context().output_dir
        changed = [
            artifact for artifact in artifacts
            if baseline.get(
                PurePosixPath(artifact.sandbox_path).relative_to(output_root).as_posix()
            ) != artifact.checksum
        ]
        self._validate_batch_limits(changed)
        return changed

    async def snapshot(self) -> dict[str, str]:
        output_root = get_conversation_execution_context().output_dir
        return {
            PurePosixPath(artifact.sandbox_path).relative_to(output_root).as_posix(): artifact.checksum
            for artifact in await self._collect_all(enforce_batch_limits=False)
        }

    async def _collect_all(
        self, *, enforce_batch_limits: bool
    ) -> list[CollectedOutputArtifact]:
        context = get_conversation_execution_context()
        output_root = context.output_dir
        try:
            entries = list(await self._source.scan(str(output_root)))
        except OutputArtifactCollectionError:
            raise
        except Exception as error:
            raise OutputArtifactCollectionError(
                "remote sandbox output scan failed"
            ) from error

        self._validate_entries(
            output_root, entries, enforce_batch_limits=enforce_batch_limits
        )

        artifacts: list[CollectedOutputArtifact] = []
        actual_total = 0
        for entry in entries:
            try:
                content = await self._source.read_bytes(entry.path)
            except OutputArtifactCollectionError:
                raise
            except Exception as error:
                raise OutputArtifactCollectionError(
                    "remote sandbox output read failed"
                ) from error
            if not isinstance(content, bytes):
                raise OutputArtifactCollectionError(
                    "remote sandbox output read returned invalid bytes"
                )
            if len(content) != entry.size:
                raise OutputArtifactCollectionError(
                    "output file size changed during collection"
                )
            if len(content) > self._max_file_size:
                raise OutputArtifactCollectionError(
                    "output file exceeds the configured file size limit"
                )
            actual_total += len(content)
            if enforce_batch_limits and actual_total > self._max_total_size:
                raise OutputArtifactCollectionError(
                    "outputs exceed the configured total size limit"
                )
            file_name = PurePosixPath(entry.path).name
            artifacts.append(CollectedOutputArtifact(
                sandbox_path=entry.path,
                file_name=file_name,
                size=len(content),
                media_type=_media_type(file_name),
                checksum=hashlib.sha256(content).hexdigest(),
                content=content,
            ))
        return artifacts

    def _validate_entries(
        self,
        output_root: PurePosixPath,
        entries: Sequence[SandboxOutputEntry],
        *,
        enforce_batch_limits: bool,
    ) -> None:
        if enforce_batch_limits and len(entries) > self._max_files:
            raise OutputArtifactCollectionError(
                "outputs exceed the configured file count limit"
            )

        declared_total = 0
        seen_paths: set[PurePosixPath] = set()
        for entry in entries:
            raw_path = PurePosixPath(entry.path)
            normalized_path = PurePosixPath(posixpath.normpath(entry.path))
            if (
                not raw_path.is_absolute()
                or ".." in raw_path.parts
                or normalized_path == output_root
                or not normalized_path.is_relative_to(output_root)
            ):
                raise OutputArtifactCollectionError(
                    "output candidate escaped the current output directory"
                )
            if normalized_path in seen_paths:
                raise OutputArtifactCollectionError("duplicate output path")
            seen_paths.add(normalized_path)
            if entry.is_symlink:
                raise OutputArtifactCollectionError(
                    "output candidate is a symbolic link"
                )
            if not entry.is_regular_file:
                raise OutputArtifactCollectionError(
                    "output candidate is not a regular file"
                )
            if not isinstance(entry.size, int) or isinstance(entry.size, bool) or entry.size < 0:
                raise OutputArtifactCollectionError("output file size is invalid")
            if entry.size > self._max_file_size:
                raise OutputArtifactCollectionError(
                    "output file exceeds the configured file size limit"
                )
            if PurePosixPath(entry.path).suffix.lower() not in self._allowed_extensions:
                raise OutputArtifactCollectionError(
                    "output file type is not allowed"
                )
            declared_total += entry.size
            if enforce_batch_limits and declared_total > self._max_total_size:
                raise OutputArtifactCollectionError(
                    "outputs exceed the configured total size limit"
                )

    def _validate_batch_limits(
        self, artifacts: Sequence[CollectedOutputArtifact]
    ) -> None:
        if len(artifacts) > self._max_files:
            raise OutputArtifactCollectionError(
                "outputs exceed the configured file count limit"
            )
        if sum(artifact.size for artifact in artifacts) > self._max_total_size:
            raise OutputArtifactCollectionError(
                "outputs exceed the configured total size limit"
            )


class RemoteSandboxOutputSource:
    """Inspect remote outputs with lstat and read them through Sandbox FS."""

    def __init__(self, operation) -> None:
        self._operation = operation

    async def scan(self, root: str) -> list[SandboxOutputEntry]:
        context = get_conversation_execution_context()
        if root != str(context.output_dir):
            raise OutputArtifactCollectionError("output root is outside the active execution boundary")
        result = await self._operation.shell().execute_cmd(
            _remote_scan_command(),
            # The target may not exist. Never cd into it before checking it.
            cwd="/",
            environment={"OJW_OUTPUT_ROOT": root},
        )
        if not operation_succeeded(result):
            raise OutputArtifactCollectionError(
                f"remote sandbox output scan failed: {str(operation_error_detail(result))[:1000]}"
            )
        data = getattr(result, "data", None)
        if data is None or getattr(data, "exit_code", None) != 0:
            detail = getattr(data, "stderr", "") or getattr(data, "stdout", "")
            raise OutputArtifactCollectionError(f"remote sandbox output scan failed: {str(detail)[:1000]}")
        try:
            payload = json.loads(data.stdout)
            if not isinstance(payload, list):
                raise TypeError("scan result must be a list")
            return [SandboxOutputEntry(**item) for item in payload]
        except (TypeError, ValueError, json.JSONDecodeError) as error:
            raise OutputArtifactCollectionError(
                "remote sandbox output scan returned invalid metadata"
            ) from error

    async def read_bytes(self, path: str) -> bytes:
        result = await self._operation.fs().read_file(path, mode="bytes")
        if not operation_succeeded(result):
            raise OutputArtifactCollectionError("remote sandbox output read failed")
        data = getattr(result, "data", None)
        content = getattr(data, "content", None)
        if not isinstance(content, bytes):
            raise OutputArtifactCollectionError(
                "remote sandbox output read returned invalid bytes"
            )
        return content


def _media_type(file_name: str) -> str:
    guessed, _ = mimetypes.guess_type(file_name)
    return guessed or "application/octet-stream"


def _remote_scan_command() -> str:
    script = r'''
root = os.environ["OJW_OUTPUT_ROOT"]
entries = []
root_fd = open_absolute(root, missing_ok=True)
if root_fd is None:
    print("[]")
    raise SystemExit(0)

def scan(directory, fd):
    # Unlike os.walk's default, permission and enumeration errors propagate.
    for name in sorted(os.listdir(fd)):
        path = os.path.join(directory, name)
        metadata = os.stat(name, dir_fd=fd, follow_symlinks=False)
        if stat.S_ISDIR(metadata.st_mode):
            child_fd = open_child(fd, name)
            try:
                scan(path, child_fd)
            finally:
                os.close(child_fd)
            continue
        entries.append({
            "path": path,
            "size": metadata.st_size,
            "is_symlink": stat.S_ISLNK(metadata.st_mode),
            "is_regular_file": stat.S_ISREG(metadata.st_mode),
        })
try:
    scan(root, root_fd)
finally:
    os.close(root_fd)
print(json.dumps(entries, ensure_ascii=False))
'''
    return remote_directory_command(script, {})
