from unittest.mock import AsyncMock

import pytest

from agent_runtime.conversation.execution_context import ConversationIdentity
from agent_runtime.conversation.output_artifact_collector import (
    CollectedOutputArtifact,
)
from agent_runtime.conversation.output_artifact_publisher import (
    ConversationOutputArtifactPublisher,
    OutputArtifactPublishError,
)


def _identity():
    return ConversationIdentity(
        "project", "workspace", "user", "conversation", "execution"
    )


def _artifact(file_name="report.pdf"):
    return CollectedOutputArtifact(
        sandbox_path=f"/workspace/output/{file_name}",
        file_name=file_name,
        size=4,
        media_type="application/pdf",
        checksum="0" * 64,
        content=b"data",
    )


@pytest.mark.asyncio
async def test_returns_artifact_metadata_only_after_upload_succeeds():
    calls = []

    class Storage:
        async def put_object_bytes(self, object_key, content):
            calls.append(("upload", object_key, content))

    publisher = ConversationOutputArtifactPublisher(
        Storage(), object_key_factory=lambda _identity, name: f"trusted/{name}"
    )

    published = await publisher.publish(_identity(), [_artifact()])
    calls.append(("event", published[0].object_key))

    assert calls == [
        ("upload", "trusted/report.pdf", b"data"),
        ("event", "trusted/report.pdf"),
    ]
    assert published[0].execution_id == "execution"
    assert published[0].checksum == "0" * 64


@pytest.mark.asyncio
async def test_upload_failure_returns_no_successful_artifact_metadata():
    storage = AsyncMock()
    storage.put_object_bytes.side_effect = RuntimeError("minio unavailable")
    publisher = ConversationOutputArtifactPublisher(
        storage, object_key_factory=lambda _identity, name: f"trusted/{name}"
    )

    with pytest.raises(OutputArtifactPublishError, match="upload failed"):
        await publisher.publish(_identity(), [_artifact()])
