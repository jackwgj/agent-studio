"""Upload verified conversation outputs before exposing Artifact events."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Callable, Sequence

from agent_runtime.conversation.artifact_object_key import (
    create_conversation_artifact_object_key,
)
from agent_runtime.conversation.execution_context import (
    ConversationIdentity,
    get_conversation_execution_context,
)
from agent_runtime.conversation.input_artifact_bridge import (
    conversation_sandbox_operation,
)
from agent_runtime.conversation.output_artifact_collector import (
    CollectedOutputArtifact,
    ConversationOutputCollector,
    RemoteSandboxOutputSource,
)
from agent_runtime.conversation.sandbox import (
    ConversationSandboxConfig,
    ConversationSysOperationFactory,
)


class OutputArtifactPublishError(RuntimeError):
    """A verified output could not be durably uploaded."""


@dataclass(frozen=True, slots=True)
class PublishedConversationArtifact:
    object_key: str
    file_name: str
    size: int
    media_type: str
    checksum: str
    execution_id: str


class ConversationOutputArtifactPublisher:
    def __init__(
        self,
        storage,
        *,
        object_key_factory: Callable[[ConversationIdentity, str], str] = (
            create_conversation_artifact_object_key
        ),
    ) -> None:
        self._storage = storage
        self._object_key_factory = object_key_factory

    async def publish(
        self,
        identity: ConversationIdentity,
        artifacts: Sequence[CollectedOutputArtifact],
    ) -> list[PublishedConversationArtifact]:
        published: list[PublishedConversationArtifact] = []
        for artifact in artifacts:
            object_key = self._object_key_factory(identity, artifact.file_name)
            try:
                await self._storage.put_object_bytes(object_key, artifact.content)
            except Exception as error:
                raise OutputArtifactPublishError(
                    f"conversation artifact upload failed: {artifact.file_name}"
                ) from error
            published.append(PublishedConversationArtifact(
                object_key=object_key,
                file_name=artifact.file_name,
                size=artifact.size,
                media_type=artifact.media_type,
                checksum=artifact.checksum,
                execution_id=identity.execution_id,
            ))
        return published


async def publish_conversation_outputs() -> list[PublishedConversationArtifact]:
    """Collect from remote AIO and upload to configured remote object storage."""
    from agent_runtime.common.config import settings
    from storage.object_storage import LocalStorageProvider, get_storage_provider

    config = ConversationSandboxConfig.from_security_sandbox_settings(
        settings.security_sandbox
    )
    if ConversationSysOperationFactory(config).create() is None:
        return []

    async with conversation_sandbox_operation("conversation_output_bridge") as operation:
        artifacts = await ConversationOutputCollector(
            RemoteSandboxOutputSource(operation)
        ).collect()
    if not artifacts:
        return []

    storage = get_storage_provider()
    if isinstance(storage, LocalStorageProvider) or not hasattr(
        storage, "put_object_bytes"
    ):
        raise OutputArtifactPublishError(
            "remote object storage is unavailable for conversation artifacts"
        )
    context = get_conversation_execution_context()
    return await ConversationOutputArtifactPublisher(storage).publish(
        context.identity, artifacts
    )
