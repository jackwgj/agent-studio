"""Cleanup execution-scoped remote directories without touching conversation state."""

from __future__ import annotations

import asyncio
import logging
import shlex

from agent_runtime.conversation.execution_context import ConversationExecutionContext
from agent_runtime.conversation.input_artifact_bridge import conversation_sandbox_operation
from agent_runtime.conversation.operation_result import operation_succeeded

logger = logging.getLogger(__name__)


async def cleanup_execution_directories(
    context: ConversationExecutionContext, *, remove_output: bool
) -> None:
    # output is conversation-persistent. The compatibility flag is intentionally
    # ignored so old callers cannot delete it after the layout refactor.
    targets = [context.tmp_dir]
    if any(
        target == context.workspace.conversation_root
        or not target.is_relative_to(context.workspace.conversation_root)
        for target in targets
    ):
        raise ValueError("temporary cleanup target escaped its conversation root")
    async with conversation_sandbox_operation("conversation_execution_cleanup") as operation:
        command = "rm -rf -- " + " ".join(shlex.quote(str(target)) for target in targets)
        result = await operation.shell().execute_cmd(
            command, cwd=str(context.workspace.conversation_root)
        )
        if not operation_succeeded(result):
            raise RuntimeError("remote execution cleanup failed")


def schedule_execution_cleanup(
    context: ConversationExecutionContext, *, remove_output: bool, delay_seconds: int
) -> asyncio.Task:
    async def delayed_cleanup() -> None:
        await asyncio.sleep(max(delay_seconds, 0))
        try:
            await cleanup_execution_directories(context, remove_output=remove_output)
        except Exception:
            logger.warning("deferred conversation execution cleanup failed", exc_info=True)

    return asyncio.create_task(delayed_cleanup())
