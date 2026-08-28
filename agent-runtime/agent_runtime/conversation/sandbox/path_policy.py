"""Best-effort path guard for commands sent to a shared remote sandbox."""

from __future__ import annotations

from dataclasses import dataclass
import posixpath
import re
from pathlib import PurePosixPath
from typing import Mapping


_ABSOLUTE_PATH_PATTERN = re.compile(
    r"(?:^|[\s\"'(=,<>])(/[a-zA-Z0-9_.][^\s\"'|&;()*?]*)"
)
_ROOT_PATH_PATTERN = re.compile(
    r"(?:^|[\s\"'(=,<>])(/)(?=$|[\s\"'|&;()<>])"
)
_RELATIVE_PATH_PATTERN = re.compile(
    r"(?:^|[\s\"'(=,<>])((?:\.\.?/)[^\s\"'|&;()*?]*)"
)
_SAFE_DEVICE_PATHS = frozenset(
    {"/dev/null", "/dev/stdin", "/dev/stdout", "/dev/stderr"}
)


@dataclass(frozen=True, slots=True)
class ConversationPathPolicy:
    """Keep explicit command paths inside one conversation workspace.

    This guard prevents common accidental path escapes before a command is
    submitted to AIO. It is deliberately not presented as an OS sandbox:
    dynamically constructed shell paths still require server-side isolation.
    """

    conversation_root: PurePosixPath
    input_dir: PurePosixPath
    skills_dir: PurePosixPath
    work_dir: PurePosixPath
    output_dir: PurePosixPath
    tmp_dir: PurePosixPath

    def resolve(self, value: str | None) -> str:
        """Resolve a caller path against work/ and reject workspace escapes."""
        candidate = PurePosixPath(value) if value is not None else PurePosixPath()
        path = candidate if candidate.is_absolute() else self.work_dir / candidate
        normalized = PurePosixPath(posixpath.normpath(str(path)))
        self._require_within_workspace(normalized, str(candidate))
        return str(normalized)

    def validate_command(self, command: str, cwd: str) -> None:
        """Reject explicit absolute or traversal paths outside this conversation."""
        resolved_cwd = PurePosixPath(self.resolve(cwd))
        for reference in self._path_references(command):
            if reference in _SAFE_DEVICE_PATHS:
                continue
            candidate = PurePosixPath(reference)
            path = candidate if candidate.is_absolute() else resolved_cwd / candidate
            normalized = PurePosixPath(posixpath.normpath(str(path)))
            self._require_within_workspace(normalized, reference)

    def environment(self, supplied: Mapping[str, str] | None = None) -> dict[str, str]:
        """Return caller variables plus authoritative conversation directory values."""
        environment = dict(supplied or {})
        environment.update(
            {
                "CONVERSATION_ROOT": str(self.conversation_root),
                "CONVERSATION_INPUT_DIR": str(self.input_dir),
                "CONVERSATION_SKILLS_DIR": str(self.skills_dir),
                "CONVERSATION_WORK_DIR": str(self.work_dir),
                "CONVERSATION_OUTPUT_DIR": str(self.output_dir),
                "CONVERSATION_TMP_DIR": str(self.tmp_dir),
            }
        )
        return environment

    @staticmethod
    def _path_references(command: str) -> list[str]:
        references = [match.group(1) for match in _ABSOLUTE_PATH_PATTERN.finditer(command)]
        references.extend(match.group(1) for match in _ROOT_PATH_PATTERN.finditer(command))
        references.extend(
            match.group(1) for match in _RELATIVE_PATH_PATTERN.finditer(command)
        )
        return references

    def _require_within_workspace(self, path: PurePosixPath, reference: str) -> None:
        if not path.is_relative_to(self.conversation_root):
            raise ValueError(
                f"command path {reference!r} is outside the active conversation workspace"
            )
