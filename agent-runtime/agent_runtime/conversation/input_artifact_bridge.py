"""Prepare trusted conversation inputs inside the remote sandbox only."""

from __future__ import annotations

import hashlib
import posixpath
import re
import uuid
from contextlib import asynccontextmanager
from pathlib import PurePosixPath
from typing import Callable, Sequence

from pydantic import BaseModel, ConfigDict, Field, field_validator

from agent_runtime.conversation.execution_context import (
    ConversationIdentity,
    get_conversation_execution_context,
)
from agent_runtime.conversation.operation_result import operation_succeeded
from agent_runtime.conversation.sandbox import ConversationSandboxConfig, ConversationSysOperationFactory
from agent_runtime.conversation.sandbox.registration import get_conversation_sandbox_operation


DEFAULT_MAX_INPUT_FILE_SIZE = 60 * 1024 * 1024
_INPUT_PREFIX = "conversation-inputs"
_CHECKSUM = re.compile(r"^[0-9a-f]{64}$")


class InputArtifactPreparationError(RuntimeError):
    """An input was not safely available to the remote sandbox."""


class ConversationInputArtifact(BaseModel):
    """Manager-verified, durable source metadata for one user input."""

    model_config = ConfigDict(populate_by_name=True, extra="forbid", frozen=True)

    object_key: str = Field(alias="objectKey")
    file_name: str = Field(alias="fileName")
    size: int = Field(gt=0)
    checksum: str

    @field_validator("object_key")
    @classmethod
    def require_safe_object_key(cls, value: str) -> str:
        if not isinstance(value, str) or not value.strip() or value != value.strip():
            raise ValueError("input object key must not be blank")
        if "\\" in value or value.startswith("/") or "://" in value:
            raise ValueError("input object key must be a trusted relative object key")
        parts = value.split("/")
        if (
            len(parts) != 6
            or parts[0] != _INPUT_PREFIX
            or any(not part or part in {".", ".."} for part in parts)
            or any(ord(character) < 32 or ord(character) == 127 for part in parts for character in part)
        ):
            raise ValueError("input object key is unsafe")
        return value

    def require_bound_identity(self, identity: ConversationIdentity) -> None:
        """Reject an object key not minted for the current trusted user scope."""
        parts = self.object_key.split("/")
        expected = [
            _INPUT_PREFIX,
            hashlib.sha256(identity.project_id.encode("utf-8")).hexdigest(),
            hashlib.sha256(identity.workspace_id.encode("utf-8")).hexdigest(),
            hashlib.sha256(identity.user_id.encode("utf-8")).hexdigest(),
        ]
        if parts[:4] != expected or parts[5] != self.file_name:
            raise InputArtifactPreparationError("input object key is not bound to the active conversation identity")
        try:
            uuid.UUID(parts[4])
        except ValueError as error:
            raise InputArtifactPreparationError("input object key is not server-generated") from error

    @field_validator("file_name")
    @classmethod
    def keep_original_basename(cls, value: str) -> str:
        if not isinstance(value, str) or not value.strip():
            raise ValueError("input file name must not be blank")
        if any(ord(character) < 32 or ord(character) == 127 for character in value):
            raise ValueError("input file name contains a control character")
        basename = PurePosixPath(value.replace("\\", "/")).name
        if (
            basename in {"", ".", ".."}
            or basename != value
            or len(basename.encode("utf-8")) > 180
        ):
            raise ValueError("input file name is unsafe")
        return basename

    @field_validator("checksum")
    @classmethod
    def require_sha256_checksum(cls, value: str) -> str:
        if not isinstance(value, str) or not _CHECKSUM.fullmatch(value):
            raise ValueError("input checksum must be a lowercase SHA-256 hex digest")
        return value


class InputArtifactBridge:
    """Copy verified Object Storage bytes to a server-derived sandbox input path."""

    def __init__(
        self,
        storage,
        operation_supplier: Callable[[], object],
        *,
        max_file_size: int = DEFAULT_MAX_INPUT_FILE_SIZE,
    ) -> None:
        self._storage = storage
        self._operation_supplier = operation_supplier
        self._max_file_size = max_file_size

    async def prepare(self, artifacts: Sequence[ConversationInputArtifact]) -> list[str]:
        context = get_conversation_execution_context()
        operation = self._operation_supplier()
        if operation is None:
            raise InputArtifactPreparationError("remote sandbox is unavailable for conversation inputs")

        paths: list[str] = []
        for artifact in artifacts:
            artifact.require_bound_identity(context.identity)
            self._check_declared_size(artifact)
            content = await self._download(artifact)
            self._verify_content(artifact, content)
            target_path = self._target_path(context.workspace.input_dir, artifact)
            await self._write(operation, target_path, content)
            paths.append(target_path)
        return paths

    def _check_declared_size(self, artifact: ConversationInputArtifact) -> None:
        if artifact.size > self._max_file_size:
            raise InputArtifactPreparationError("input file exceeds the configured size limit")

    async def _download(self, artifact: ConversationInputArtifact) -> bytes:
        try:
            content = await self._storage.get_object_bytes(artifact.object_key)
        except Exception as error:
            raise InputArtifactPreparationError(
                f"input artifact download failed: {artifact.object_key}"
            ) from error
        if not isinstance(content, bytes):
            raise InputArtifactPreparationError("input artifact download returned invalid bytes")
        return content

    def _verify_content(self, artifact: ConversationInputArtifact, content: bytes) -> None:
        if len(content) != artifact.size:
            raise InputArtifactPreparationError("input artifact size verification failed")
        if len(content) > self._max_file_size:
            raise InputArtifactPreparationError("input file exceeds the configured size limit")
        if hashlib.sha256(content).hexdigest() != artifact.checksum:
            raise InputArtifactPreparationError("input artifact checksum verification failed")

    async def _write(self, operation, target_path: str, content: bytes) -> None:
        try:
            result = await operation.fs().write_file(
                target_path,
                content,
                mode="bytes",
                prepend_newline=False,
                append_newline=False,
            )
        except Exception as error:
            raise InputArtifactPreparationError("input artifact sandbox write failed") from error
        if not operation_succeeded(result):
            raise InputArtifactPreparationError("input artifact sandbox write failed")

    @staticmethod
    def _target_path(input_dir: PurePosixPath, artifact: ConversationInputArtifact) -> str:
        source_key = hashlib.sha256(artifact.object_key.encode("utf-8")).hexdigest()[:16]
        target = PurePosixPath(posixpath.normpath(str(input_dir / source_key / artifact.file_name)))
        if not target.is_relative_to(input_dir):
            raise InputArtifactPreparationError("input artifact target escaped the sandbox input directory")
        return str(target)


async def prepare_conversation_inputs(
    artifacts: Sequence[ConversationInputArtifact],
) -> list[str]:
    """Create a short-lived SANDBOX SysOperation for program-internal input copying."""
    if not artifacts:
        return []

    from storage.object_storage import LocalStorageProvider, get_storage_provider

    storage = get_storage_provider()
    if isinstance(storage, LocalStorageProvider):
        raise InputArtifactPreparationError("remote object storage is unavailable for conversation inputs")

    async with conversation_sandbox_operation("conversation_input_bridge") as operation:
        return await InputArtifactBridge(storage, lambda: operation).prepare(artifacts)


@asynccontextmanager
async def conversation_sandbox_operation(operation_prefix: str):
    """Borrow the process-owned remote operation, without changing its lifetime."""
    from openjiuwen.core.runner import Runner
    from openjiuwen.core.sys_operation import OperationMode

    config = ConversationSandboxConfig.from_security_sandbox_settings(
        __import__("agent_runtime.common.config", fromlist=["settings"]).settings.security_sandbox
    )
    card = ConversationSysOperationFactory(config).create()
    if card is None or card.mode is not OperationMode.SANDBOX:
        raise InputArtifactPreparationError("remote sandbox is unavailable for conversation artifacts")

    try:
        operation = get_conversation_sandbox_operation(Runner.resource_mgr, card)
    except Exception as error:
        raise RuntimeError(f"{operation_prefix}: sandbox registration failed: {error}") from error
    yield operation
