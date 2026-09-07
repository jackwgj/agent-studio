import hashlib
import uuid

import pytest

from agent_runtime.conversation.artifact_object_key import (
    conversation_artifact_prefix,
    create_conversation_artifact_object_key,
)
from agent_runtime.conversation.execution_context import ConversationIdentity


def _identity(conversation_id="conversation", execution_id="execution"):
    return ConversationIdentity(
        "project", "workspace", "user", conversation_id, execution_id
    )


def _key(value):
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def test_prefix_contains_only_trusted_hashed_identity_segments():
    identity = _identity()

    assert conversation_artifact_prefix(identity) == (
        "conversation-artifacts/"
        f"{_key('project')}/{_key('workspace')}/{_key('user')}/"
        f"{_key('conversation')}/{_key('execution')}/"
    )


def test_same_named_outputs_receive_distinct_server_generated_keys():
    identity = _identity()

    first = create_conversation_artifact_object_key(identity, "result.xlsx")
    second = create_conversation_artifact_object_key(identity, "result.xlsx")

    assert first != second
    assert first.startswith(conversation_artifact_prefix(identity))
    assert second.startswith(conversation_artifact_prefix(identity))
    assert first.endswith("-result.xlsx")
    assert second.endswith("-result.xlsx")


def test_different_conversations_and_executions_never_share_a_prefix():
    current = conversation_artifact_prefix(_identity())
    another_conversation = conversation_artifact_prefix(
        _identity(conversation_id="another")
    )
    another_execution = conversation_artifact_prefix(
        _identity(execution_id="another")
    )

    assert len({current, another_conversation, another_execution}) == 3


@pytest.mark.parametrize("file_name", [
    "../../outside.pdf",
    "nested\\outside.pdf",
    "..",
    ".hidden",
    "报告 2026/最终?.xlsx",
    "a" * 300 + ".txt",
])
def test_dangerous_file_names_cannot_escape_or_create_object_key_segments(file_name):
    artifact_id = uuid.UUID("00000000-0000-0000-0000-000000000001")

    object_key = create_conversation_artifact_object_key(
        _identity(), file_name, artifact_id=artifact_id
    )
    suffix = object_key.removeprefix(conversation_artifact_prefix(_identity()))

    assert suffix.startswith(f"{artifact_id}-")
    assert "/" not in suffix
    assert "\\" not in suffix
    assert ".." not in suffix
    assert len(suffix.encode("utf-8")) <= 180


@pytest.mark.parametrize("artifact_id", ["", "../id", "not-a-uuid"])
def test_rejects_non_uuid_artifact_identifiers(artifact_id):
    with pytest.raises(ValueError, match="artifact id"):
        create_conversation_artifact_object_key(
            _identity(), "result.pdf", artifact_id=artifact_id
        )
