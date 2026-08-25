"""Collect trusted formal outputs from the active remote sandbox execution."""

from __future__ import annotations

import base64
import hashlib
import json
import mimetypes
import posixpath
from dataclasses import dataclass
from pathlib import PurePosixPath
from typing import Protocol, Sequence

from agent_runtime.conversation.execution_context import (
    get_conversation_execution_context,
)


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

    async def collect(self) -> list[CollectedOutputArtifact]:
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

        self._validate_entries(output_root, entries)

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
            if actual_total > self._max_total_size:
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
    ) -> None:
        if len(entries) > self._max_files:
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
            if declared_total > self._max_total_size:
                raise OutputArtifactCollectionError(
                    "outputs exceed the configured total size limit"
                )


class RemoteSandboxOutputSource:
    """Inspect remote outputs with lstat and read them through Sandbox FS."""

    def __init__(self, operation) -> None:
        self._operation = operation

    async def scan(self, root: str) -> list[SandboxOutputEntry]:
        result = await self._operation.shell().execute_cmd(
            _remote_scan_command(),
            cwd=root,
            environment={"OJW_OUTPUT_ROOT": root},
        )
        if not _result_succeeded(result):
            raise OutputArtifactCollectionError("remote sandbox output scan failed")
        data = getattr(result, "data", None)
        if data is None or getattr(data, "exit_code", None) != 0:
            raise OutputArtifactCollectionError("remote sandbox output scan failed")
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
        if not _result_succeeded(result):
            raise OutputArtifactCollectionError("remote sandbox output read failed")
        data = getattr(result, "data", None)
        content = getattr(data, "content", None)
        if not isinstance(content, bytes):
            raise OutputArtifactCollectionError(
                "remote sandbox output read returned invalid bytes"
            )
        return content


def _result_succeeded(result) -> bool:
    is_ok = getattr(result, "is_ok", None)
    return not callable(is_ok) or bool(is_ok())


def _media_type(file_name: str) -> str:
    guessed, _ = mimetypes.guess_type(file_name)
    return guessed or "application/octet-stream"


def _remote_scan_command() -> str:
    script = r'''import json
import os
import stat

root = os.environ["OJW_OUTPUT_ROOT"]
entries = []
if not os.path.lexists(root):
    print("[]")
    raise SystemExit(0)
root_metadata = os.lstat(root)
if stat.S_ISLNK(root_metadata.st_mode) or not stat.S_ISDIR(root_metadata.st_mode):
    raise RuntimeError("output root must be a real directory")
if os.path.realpath(root) != os.path.abspath(root):
    raise RuntimeError("output root contains a symbolic-link component")
for directory, directory_names, file_names in os.walk(root, followlinks=False):
    for name in list(directory_names):
        path = os.path.join(directory, name)
        metadata = os.lstat(path)
        if stat.S_ISLNK(metadata.st_mode):
            entries.append({
                "path": path,
                "size": metadata.st_size,
                "is_symlink": True,
                "is_regular_file": False,
            })
            directory_names.remove(name)
    for name in file_names:
        path = os.path.join(directory, name)
        metadata = os.lstat(path)
        entries.append({
            "path": path,
            "size": metadata.st_size,
            "is_symlink": stat.S_ISLNK(metadata.st_mode),
            "is_regular_file": stat.S_ISREG(metadata.st_mode),
        })
print(json.dumps(entries, ensure_ascii=False))
'''
    encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
    return (
        "python3 -c \"import base64;"
        f"exec(base64.b64decode('{encoded}'))\""
    )
