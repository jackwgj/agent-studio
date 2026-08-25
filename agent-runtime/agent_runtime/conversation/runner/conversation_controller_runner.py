"""Conversation-specific Controller runner."""

from __future__ import annotations

import uuid
from collections.abc import AsyncGenerator
from typing import Any

from agent_runtime.runner.controller_runner import ControllerRunner
from agent_runtime.runner.controller_stream_data_adapter import (
    ControllerStreamDataAdapter,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionRequest


_UNAVAILABLE_MESSAGE = (
    "Controller and PlanExecute modes are unavailable in the conversation "
    "workspace until remote sandbox support is implemented."
)


class ConversationControllerRunner(ControllerRunner):
    """Reusable conversation wrapper around the official Controller runner."""

    async def run_streaming(
        self,
        req: ExecutionRequest,
        execution_id: str | None = None,
    ) -> AsyncGenerator[Any]:
        """Explicitly disable conversation Controller paths pending remote sandbox support."""
        exec_id = execution_id or req.conversation_id or str(uuid.uuid4())
        adapter = ControllerStreamDataAdapter(execution_id=exec_id)
        yield adapter.adapt_error(_UNAVAILABLE_MESSAGE, exec_id)
