"""Trusted, remote-only cleanup of conversation workspace directories."""

from __future__ import annotations

import shlex

from agent_runtime.common.config import settings
from agent_runtime.conversation.execution_context import (
    ConversationIdentity,
    ConversationWorkspace,
)
from agent_runtime.conversation.input_artifact_bridge import conversation_sandbox_operation
from agent_runtime.conversation.operation_result import operation_succeeded


class ConversationWorkspaceCleanupError(RuntimeError):
    """The remote conversation directory could not be safely removed."""


async def delete_conversation_workspace(
    project_id: str, workspace_id: str, user_id: str, conversation_id: str
) -> None:
    """Derive the only permitted target from identity and delete it idempotently."""
    identity = ConversationIdentity(
        project_id=project_id,
        workspace_id=workspace_id,
        user_id=user_id,
        conversation_id=conversation_id,
        execution_id="cleanup",
    )
    workspace = ConversationWorkspace.create(
        identity, settings.security_sandbox.workspace_root
    )
    target = workspace.conversation_root
    if target == workspace.sandbox_root or not target.is_relative_to(workspace.sandbox_root):
        raise ConversationWorkspaceCleanupError("refusing to delete sandbox root")

    async with conversation_sandbox_operation("conversation_workspace_cleanup") as operation:
        try:
            result = await operation.shell().execute_cmd(
                f"rm -rf -- {shlex.quote(str(target))}", cwd=str(workspace.sandbox_root)
            )
        except Exception as error:
            raise ConversationWorkspaceCleanupError("remote workspace cleanup failed") from error
        if not operation_succeeded(result):
            raise ConversationWorkspaceCleanupError("remote workspace cleanup failed")
