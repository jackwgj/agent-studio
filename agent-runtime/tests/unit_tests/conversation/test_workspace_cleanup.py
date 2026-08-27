from types import SimpleNamespace
from unittest.mock import AsyncMock, patch

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from agent_runtime.serve.apis.conversation_cleanup import conversation_cleanup_router


def app_client() -> TestClient:
    app = FastAPI()
    app.include_router(conversation_cleanup_router)
    return TestClient(app)


def payload(**extra):
    return {
        "projectId": "p1",
        "workspaceId": "w1",
        "userId": "u1",
        "conversationId": "c1",
        **extra,
    }


def test_cleanup_requires_internal_credential():
    with patch("agent_runtime.serve.apis.conversation_cleanup.settings") as settings:
        settings.security_sandbox.cleanup_internal_token = "secret"
        assert app_client().post(
            "/internal/v1/conversation/workspace/cleanup", json=payload()
        ).status_code == 401


def test_cleanup_rejects_missing_identity_and_arbitrary_path():
    with patch("agent_runtime.serve.apis.conversation_cleanup.settings") as settings:
        settings.security_sandbox.cleanup_internal_token = "secret"
        headers = {"X-Conversation-Cleanup-Token": "secret"}
        assert app_client().post(
            "/internal/v1/conversation/workspace/cleanup",
            json=payload(userId=""), headers=headers
        ).status_code == 422
        assert app_client().post(
            "/internal/v1/conversation/workspace/cleanup",
            json=payload(path="/workspace"), headers=headers
        ).status_code == 422


@pytest.mark.asyncio
async def test_cleanup_derives_hashed_target_and_is_idempotent():
    shell = SimpleNamespace(execute_cmd=AsyncMock(return_value=SimpleNamespace(is_ok=lambda: True)))
    operation = SimpleNamespace(shell=lambda: shell)

    class OperationContext:
        async def __aenter__(self):
            return operation

        async def __aexit__(self, *_):
            return None

    with patch("agent_runtime.conversation.workspace_cleanup.settings") as settings, patch(
        "agent_runtime.conversation.workspace_cleanup.conversation_sandbox_operation",
        return_value=OperationContext(),
    ):
        settings.security_sandbox.workspace_root = "/workspace"
        from agent_runtime.conversation.workspace_cleanup import delete_conversation_workspace
        await delete_conversation_workspace("p1", "w1", "u1", "c1")
        await delete_conversation_workspace("p1", "w1", "u1", "c1")

    command = shell.execute_cmd.call_args_list[0].args[0]
    assert command.startswith("rm -rf -- /workspace/")
    assert "p1" not in command and ".." not in command
    assert shell.execute_cmd.await_count == 2


@pytest.mark.asyncio
async def test_cleanup_can_retry_after_remote_sandbox_recovers():
    shell = SimpleNamespace(
        execute_cmd=AsyncMock(return_value=SimpleNamespace(is_ok=lambda: True))
    )
    operation = SimpleNamespace(shell=lambda: shell)
    attempts = 0

    class RecoveringOperationContext:
        async def __aenter__(self):
            nonlocal attempts
            attempts += 1
            if attempts == 1:
                raise RuntimeError("sandbox unavailable")
            return operation

        async def __aexit__(self, *_):
            return None

    with patch("agent_runtime.conversation.workspace_cleanup.settings") as settings, patch(
        "agent_runtime.conversation.workspace_cleanup.conversation_sandbox_operation",
        side_effect=lambda *_: RecoveringOperationContext(),
    ):
        settings.security_sandbox.workspace_root = "/workspace"
        from agent_runtime.conversation.workspace_cleanup import (
            delete_conversation_workspace,
        )

        with pytest.raises(RuntimeError, match="sandbox unavailable"):
            await delete_conversation_workspace("p1", "w1", "u1", "c1")
        await delete_conversation_workspace("p1", "w1", "u1", "c1")

    assert attempts == 2
    shell.execute_cmd.assert_awaited_once()


@pytest.mark.asyncio
async def test_cleanup_rejects_aio_nonzero_result_without_is_ok():
    shell = SimpleNamespace(
        execute_cmd=AsyncMock(
            return_value=SimpleNamespace(code=199004, message="shell failed", data=None)
        )
    )
    operation = SimpleNamespace(shell=lambda: shell)

    class OperationContext:
        async def __aenter__(self):
            return operation

        async def __aexit__(self, *_):
            return None

    with patch("agent_runtime.conversation.workspace_cleanup.settings") as settings, patch(
        "agent_runtime.conversation.workspace_cleanup.conversation_sandbox_operation",
        return_value=OperationContext(),
    ):
        settings.security_sandbox.workspace_root = "/workspace"
        from agent_runtime.conversation.workspace_cleanup import (
            ConversationWorkspaceCleanupError,
            delete_conversation_workspace,
        )

        with pytest.raises(ConversationWorkspaceCleanupError, match="cleanup failed"):
            await delete_conversation_workspace("p1", "w1", "u1", "c1")


@pytest.mark.asyncio
async def test_cleanup_rejects_nonzero_shell_exit_code():
    shell = SimpleNamespace(
        execute_cmd=AsyncMock(return_value=SimpleNamespace(
            code=0, data=SimpleNamespace(exit_code=13)
        ))
    )
    operation = SimpleNamespace(shell=lambda: shell)

    class OperationContext:
        async def __aenter__(self):
            return operation

        async def __aexit__(self, *_):
            return None

    with patch("agent_runtime.conversation.workspace_cleanup.settings") as settings, patch(
        "agent_runtime.conversation.workspace_cleanup.conversation_sandbox_operation",
        return_value=OperationContext(),
    ):
        settings.security_sandbox.workspace_root = "/workspace"
        from agent_runtime.conversation.workspace_cleanup import (
            ConversationWorkspaceCleanupError,
            delete_conversation_workspace,
        )

        with pytest.raises(ConversationWorkspaceCleanupError, match="cleanup failed"):
            await delete_conversation_workspace("p1", "w1", "u1", "c1")
