from dataclasses import FrozenInstanceError
from pathlib import PurePosixPath

import pytest

from agent_runtime.conversation import execution_context as execution_context_module
from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
    ConversationWorkspace,
)


def test_get_execution_context_explicitly_fails_outside_an_active_execution():
    with pytest.raises(LookupError, match="no conversation execution context is active"):
        execution_context_module.get_conversation_execution_context()


def _identity(**overrides):
    values = {
        "project_id": "project-a",
        "workspace_id": "workspace-a",
        "user_id": "user-a",
        "conversation_id": "conversation-a",
        "execution_id": "execution-a",
    }
    values.update(overrides)
    return ConversationIdentity(**values)


@pytest.mark.parametrize(
    "missing_field",
    ["project_id", "workspace_id", "user_id", "conversation_id", "execution_id"],
)
def test_conversation_identity_requires_every_audit_field(missing_field):
    values = {
        "project_id": "project-a",
        "workspace_id": "workspace-a",
        "user_id": "user-a",
        "conversation_id": "conversation-a",
        "execution_id": "execution-a",
    }
    del values[missing_field]

    with pytest.raises(TypeError):
        ConversationIdentity(**values)


@pytest.mark.parametrize("value", ["", " ", "\t\n"])
def test_conversation_identity_rejects_blank_audit_fields(value):
    with pytest.raises(ValueError):
        _identity(user_id=value)


def test_execution_context_is_immutable_and_parent_child_calls_share_identity():
    identity = _identity()
    workspace = ConversationWorkspace.create(identity, "/remote/sandboxes")
    context = ConversationExecutionContext.create(identity, "/remote/sandboxes")

    assert context.identity == identity
    assert workspace.identity == identity
    assert context.for_child_call() is context
    assert context.for_child_call().identity is identity
    assert context.for_child_call().workspace is context.workspace

    with pytest.raises(FrozenInstanceError):
        identity.project_id = "project-b"
    with pytest.raises(FrozenInstanceError):
        workspace.sandbox_root = "/other"
    with pytest.raises(FrozenInstanceError):
        context.workspace = workspace


def test_execution_context_uses_the_exact_deterministic_sandbox_layout():
    context = ConversationExecutionContext.create(_identity(), "/remote/sandboxes")
    conversation_root = PurePosixPath(
        "/remote/sandboxes/"
        "0e3ffbf31db2e5b45f9fe42abf9ae60d024e48dc0dc1f0ab6b366739b961bf89/"
        "0dcf2d98505da17dc4a5eaefb2553d5b86f2ed378a08219fe8c9dae3f55cc288/"
        "fc95297aa4f56781f0decb7d4bf59b1447f09b3611039b80188b1c6beb03ee6a/"
        "30d36aa9aeae09e9c322f17c7d13842a1035dbe88abd25606d199f7279a902b7"
    )

    assert context.workspace.conversation_root == conversation_root
    assert context.workspace.input_dir == conversation_root / "input"
    assert context.workspace.skills_dir == conversation_root / "skills"
    assert context.workspace.work_dir == conversation_root / "work"
    assert context.execution_root == (
        conversation_root
        / "runs"
        / "db47f954a31a24a3070622baacdc07470d3369bf327ffb43df78a4ac4942ad2a"
    )
    assert context.output_dir == context.execution_root / "output"
    assert context.tmp_dir == context.execution_root / "tmp"


def test_execution_ids_share_conversation_directories_but_isolate_run_directories():
    parent = ConversationExecutionContext.create(_identity(execution_id="execution-a"), "/remote/sandboxes")
    child = ConversationExecutionContext.create(_identity(execution_id="execution-b"), "/remote/sandboxes")

    assert child.workspace.conversation_root == parent.workspace.conversation_root
    assert child.workspace.input_dir == parent.workspace.input_dir
    assert child.workspace.skills_dir == parent.workspace.skills_dir
    assert child.workspace.work_dir == parent.workspace.work_dir
    assert child.execution_root != parent.execution_root
    assert child.output_dir != parent.output_dir
    assert child.tmp_dir != parent.tmp_dir


@pytest.mark.parametrize(
    ("field_name", "other_value"),
    [
        ("project_id", "project-b"),
        ("workspace_id", "workspace-b"),
        ("user_id", "user-b"),
        ("conversation_id", "conversation-b"),
    ],
)
def test_each_conversation_identity_field_isolates_the_conversation_root(
    field_name, other_value
):
    original = ConversationExecutionContext.create(_identity(), "/remote/sandboxes")
    isolated = ConversationExecutionContext.create(
        _identity(**{field_name: other_value}), "/remote/sandboxes"
    )

    assert isolated.workspace.conversation_root != original.workspace.conversation_root


def test_unsafe_and_long_identities_are_opaque_and_cannot_escape_sandbox_root():
    identity = _identity(
        project_id="project/../../evil",
        workspace_id="workspace:reserved?",
        user_id="用户😀",
        conversation_id="x" * 4096,
        execution_id="..\\..\\escape",
    )
    context = ConversationExecutionContext.create(identity, "/remote/sandboxes")
    raw_values = (
        identity.project_id,
        identity.workspace_id,
        identity.user_id,
        identity.conversation_id,
        identity.execution_id,
    )
    paths = (
        context.workspace.conversation_root,
        context.workspace.input_dir,
        context.workspace.skills_dir,
        context.workspace.work_dir,
        context.execution_root,
        context.output_dir,
        context.tmp_dir,
    )

    for path in paths:
        assert isinstance(path, PurePosixPath)
        assert path.is_relative_to(PurePosixPath("/remote/sandboxes"))
        assert ".." not in path.parts
        assert all(raw not in path.parts for raw in raw_values)
    assert all(len(part) <= 64 for path in paths for part in path.parts)
