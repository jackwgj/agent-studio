"""Prepare complete validated workspace Skills in the remote conversation sandbox."""

from __future__ import annotations

from pathlib import Path, PurePosixPath

from agent_runtime.conversation.execution_context import get_conversation_execution_context
from agent_runtime.conversation.input_artifact_bridge import (
    InputArtifactPreparationError,
    conversation_sandbox_operation,
)
from agent_runtime.conversation.operation_result import operation_succeeded
from agent_runtime.conversation.sandbox import ConversationSandboxConfig, ConversationSysOperationFactory
from agent_runtime.supervisor.skill_artifact_cache import (
    CachedSkillArtifact,
    MAX_FILE_BYTES,
    MAX_UNCOMPRESSED_BYTES,
    MAX_ZIP_ENTRIES,
    SkillArtifactError,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor


class ConversationSkillArtifactBridge:
    """Copy a trusted local cache tree to a server-derived sandbox Skill directory."""

    def __init__(self, operation_supplier) -> None:
        self._operation_supplier = operation_supplier

    async def prepare(self, skill: SkillDescriptor, artifact: CachedSkillArtifact) -> str:
        operation = self._operation_supplier()
        if operation is None:
            raise SkillArtifactError("remote sandbox is unavailable for Skill preparation")
        files = self._collect_files(artifact.artifact_dir)
        target_root = get_conversation_execution_context().workspace.skills_dir / skill.cache_key
        for relative_path, source_path in files:
            content = source_path.read_bytes()
            target = target_root.joinpath(*relative_path.parts)
            try:
                result = await operation.fs().write_file(
                    str(target),
                    content,
                    mode="bytes",
                    prepend_newline=False,
                    append_newline=False,
                )
            except Exception as error:
                raise SkillArtifactError("Skill sandbox write failed") from error
            if not operation_succeeded(result):
                raise SkillArtifactError("Skill sandbox write failed")
        return str(target_root)

    @staticmethod
    def _collect_files(root: Path) -> list[tuple[PurePosixPath, Path]]:
        if not root.is_dir() or root.is_symlink():
            raise SkillArtifactError("invalid existing cache entry")
        files: list[tuple[PurePosixPath, Path]] = []
        total_size = 0
        for path in sorted(root.rglob("*")):
            if path.is_symlink():
                raise SkillArtifactError("unsafe cached Skill member")
            if path.is_dir():
                continue
            if not path.is_file():
                raise SkillArtifactError("unsafe cached Skill member")
            relative = PurePosixPath(path.relative_to(root).as_posix())
            if not relative.parts or any(part in {"", ".", ".."} for part in relative.parts):
                raise SkillArtifactError("unsafe cached Skill member")
            size = path.stat().st_size
            total_size += size
            if size > MAX_FILE_BYTES or total_size > MAX_UNCOMPRESSED_BYTES:
                raise SkillArtifactError("uncompressed content too large")
            files.append((relative, path))
            if len(files) > MAX_ZIP_ENTRIES:
                raise SkillArtifactError("too many Skill files")
        if not any(relative == PurePosixPath("SKILL.md") for relative, _ in files):
            raise SkillArtifactError("cached SKILL.md cannot be read")
        return files


async def prepare_conversation_skill(
    skill: SkillDescriptor,
    artifact: CachedSkillArtifact,
) -> str:
    """Use a short-lived remote-only operation; never fall back to Runtime LOCAL."""
    try:
        async with conversation_sandbox_operation("conversation_skill_bridge") as operation:
            return await ConversationSkillArtifactBridge(lambda: operation).prepare(skill, artifact)
    except InputArtifactPreparationError as error:
        raise SkillArtifactError("remote sandbox is unavailable for Skill preparation") from error


def conversation_skill_sandbox_enabled() -> bool:
    """Return whether conversation Skill resources must be prepared remotely."""
    config = ConversationSandboxConfig.from_security_sandbox_settings(
        __import__("agent_runtime.common.config", fromlist=["settings"]).settings.security_sandbox
    )
    return ConversationSysOperationFactory(config).create() is not None
