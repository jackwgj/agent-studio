"""Immutable identity and execution context values for conversations."""

from __future__ import annotations

from dataclasses import dataclass
import hashlib
from pathlib import PurePosixPath


@dataclass(frozen=True, slots=True)
class ConversationIdentity:
    """Audit identity supplied for one conversation execution."""

    project_id: str
    workspace_id: str
    user_id: str
    conversation_id: str
    execution_id: str

    def __post_init__(self) -> None:
        for field_name in (
            "project_id",
            "workspace_id",
            "user_id",
            "conversation_id",
            "execution_id",
        ):
            value = getattr(self, field_name)
            if not isinstance(value, str):
                raise TypeError(f"{field_name} must be a string")
            if not value.strip():
                raise ValueError(f"{field_name} must not be blank")


@dataclass(frozen=True, slots=True)
class ConversationWorkspace:
    """Immutable, conversation-scoped directories in a remote sandbox."""

    identity: ConversationIdentity
    sandbox_root: PurePosixPath
    conversation_root: PurePosixPath
    input_dir: PurePosixPath
    skills_dir: PurePosixPath
    work_dir: PurePosixPath

    def __post_init__(self) -> None:
        root = _sandbox_root(self.sandbox_root)
        object.__setattr__(self, "sandbox_root", root)
        conversation_root = root.joinpath(
            _path_key(self.identity.project_id),
            _path_key(self.identity.workspace_id),
            _path_key(self.identity.user_id),
            _path_key(self.identity.conversation_id),
        )
        _require_paths_under_root(
            root,
            conversation_root,
            self.conversation_root,
            self.input_dir,
            self.skills_dir,
            self.work_dir,
        )
        if (
            self.conversation_root != conversation_root
            or self.input_dir != conversation_root / "input"
            or self.skills_dir != conversation_root / "skills"
            or self.work_dir != conversation_root / "work"
        ):
            raise ValueError("workspace paths must match the deterministic layout")

    @classmethod
    def create(
        cls, identity: ConversationIdentity, sandbox_root: str | PurePosixPath
    ) -> ConversationWorkspace:
        root = _sandbox_root(sandbox_root)
        conversation_root = root.joinpath(
            _path_key(identity.project_id),
            _path_key(identity.workspace_id),
            _path_key(identity.user_id),
            _path_key(identity.conversation_id),
        )
        return cls(
            identity=identity,
            sandbox_root=root,
            conversation_root=conversation_root,
            input_dir=conversation_root / "input",
            skills_dir=conversation_root / "skills",
            work_dir=conversation_root / "work",
        )


@dataclass(frozen=True, slots=True)
class ConversationExecutionContext:
    """Immutable context shared by every call in one conversation execution."""

    identity: ConversationIdentity
    workspace: ConversationWorkspace
    execution_root: PurePosixPath
    output_dir: PurePosixPath
    tmp_dir: PurePosixPath

    def __post_init__(self) -> None:
        if self.workspace.identity != self.identity:
            raise ValueError("workspace identity must match execution identity")
        execution_root = self.workspace.conversation_root / "runs" / _path_key(
            self.identity.execution_id
        )
        _require_paths_under_root(
            self.workspace.sandbox_root,
            execution_root,
            self.execution_root,
            self.output_dir,
            self.tmp_dir,
        )
        if (
            self.execution_root != execution_root
            or self.output_dir != execution_root / "output"
            or self.tmp_dir != execution_root / "tmp"
        ):
            raise ValueError("execution paths must match the deterministic layout")

    @classmethod
    def create(
        cls, identity: ConversationIdentity, sandbox_root: str | PurePosixPath
    ) -> ConversationExecutionContext:
        workspace = ConversationWorkspace.create(identity, sandbox_root)
        execution_root = workspace.conversation_root / "runs" / _path_key(
            identity.execution_id
        )
        return cls(
            identity=identity,
            workspace=workspace,
            execution_root=execution_root,
            output_dir=execution_root / "output",
            tmp_dir=execution_root / "tmp",
        )

    def for_child_call(self) -> ConversationExecutionContext:
        """Reuse this immutable context for a nested conversation call."""
        return self


def _path_key(value: str) -> str:
    """Return a path-safe key without exposing an audit identity on disk."""
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def _sandbox_root(value: str | PurePosixPath) -> PurePosixPath:
    root = PurePosixPath(value)
    if not root.is_absolute() or ".." in root.parts:
        raise ValueError("sandbox_root must be an absolute, non-traversing POSIX path")
    return root


def _require_paths_under_root(root: PurePosixPath, *paths: PurePosixPath) -> None:
    for path in paths:
        if not path.is_relative_to(root):
            raise ValueError("sandbox paths must remain under sandbox_root")
