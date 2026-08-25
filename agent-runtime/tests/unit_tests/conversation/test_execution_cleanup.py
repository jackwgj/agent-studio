from types import SimpleNamespace
from unittest.mock import AsyncMock, patch

import pytest

from agent_runtime.conversation.execution_cleanup import cleanup_execution_directories
from agent_runtime.conversation.execution_context import ConversationExecutionContext, ConversationIdentity


@pytest.mark.asyncio
async def test_execution_cleanup_only_removes_tmp_and_uploaded_output():
    context = ConversationExecutionContext.create(
        ConversationIdentity("p", "w", "u", "c", "e"), "/workspace"
    )
    shell = SimpleNamespace(execute_cmd=AsyncMock(return_value=SimpleNamespace(is_ok=lambda: True)))
    operation = SimpleNamespace(shell=lambda: shell)

    class OperationContext:
        async def __aenter__(self): return operation
        async def __aexit__(self, *_): return None

    with patch(
        "agent_runtime.conversation.execution_cleanup.conversation_sandbox_operation",
        return_value=OperationContext(),
    ):
        await cleanup_execution_directories(context, remove_output=True)

    command = shell.execute_cmd.call_args.args[0]
    assert str(context.tmp_dir) in command
    assert str(context.output_dir) in command
    assert str(context.workspace.input_dir) not in command
    assert str(context.workspace.skills_dir) not in command
    assert str(context.workspace.work_dir) not in command


@pytest.mark.asyncio
async def test_failed_execution_keeps_output_for_ttl_recovery():
    context = ConversationExecutionContext.create(
        ConversationIdentity("p", "w", "u", "c", "e"), "/workspace"
    )
    shell = SimpleNamespace(execute_cmd=AsyncMock(return_value=SimpleNamespace(is_ok=lambda: True)))
    operation = SimpleNamespace(shell=lambda: shell)

    class OperationContext:
        async def __aenter__(self): return operation
        async def __aexit__(self, *_): return None

    with patch(
        "agent_runtime.conversation.execution_cleanup.conversation_sandbox_operation",
        return_value=OperationContext(),
    ):
        await cleanup_execution_directories(context, remove_output=False)

    command = shell.execute_cmd.call_args.args[0]
    assert str(context.tmp_dir) in command
    assert str(context.output_dir) not in command
