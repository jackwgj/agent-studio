"""Conversation-specific Supervisor handoff tool.

The legacy HandoffTool remains unchanged. This additive tool reuses its card and
IR-path behavior while delegating child execution to the conversation ReAct
runner's standard run_streaming contract.
"""

from __future__ import annotations

import uuid
from collections.abc import AsyncGenerator

from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionParams, ExecutionRequest
from agent_runtime.supervisor.event.canonical import build_message, build_reasoning, build_tool_call, build_tool_result
from agent_runtime.supervisor.event.channel import get_channel
from agent_runtime.serve.apis.conversation_team_app import _event_payload
from agent_runtime.supervisor.tool.handoff_tool import HandoffTool
from jiuwen.serve.controllers.execution.open_utils import async_ir_load


_runner_factory = ConversationRunnerFactory()


class ConversationHandoffTool(HandoffTool):
    """Supervisor-only handoff that delegates child execution to the new runner."""

    async def build_child_request(
        self,
        query: str,
        conversation_id: str,
    ) -> ExecutionRequest:
        """Load existing child IR and build the standard runner request."""
        ir_data = await async_ir_load(self._ir_path())
        history = []
        params = ExecutionParams(
            conversationHistory=history,
            globalVariables={},
            pluginConfigs=[],
            toolSwitchDict={},
            isDebug=False,
            ir_cache=ir_data,
        )
        return ExecutionRequest(
            conversationId=conversation_id,
            userId="anonymous",
            irPath=self._ir_path(),
            query=query,
            params=params,
            headers={},
        )

    async def stream_child_request(
        self,
        request: ExecutionRequest,
        runner,
        execution_id: str | None = None,
        sub_execution_id: str | None = None,
    ) -> AsyncGenerator[dict, None]:
        """Run the child through the standard runner and adapt its events."""
        child_run_id = sub_execution_id or str(uuid.uuid4())
        conversation_id = request.conversation_id
        async for raw in runner.run_streaming(request, child_run_id):
            payload = _event_payload(raw)
            if not payload:
                continue
            event = payload.get("event", "")
            data = payload.get("data") or {}
            if not isinstance(data, dict):
                data = {"content": str(data)}

            if event == "message":
                delta = data.get("delta") or data.get("answer") or data.get("content") or ""
                if delta:
                    yield build_message(
                        conversation_id,
                        child_run_id,
                        str(delta),
                        agent_id=self.agent_id,
                        parent_run_id=execution_id,
                        execution_type="sub_agent",
                    )
            elif event == "reasoning":
                content = data.get("content") or ""
                if content:
                    yield build_reasoning(
                        conversation_id,
                        child_run_id,
                        str(content),
                        agent_id=self.agent_id,
                        parent_run_id=execution_id,
                        execution_type="sub_agent",
                    )
            elif event == "done":
                continue

    async def invoke(self, inputs, **kwargs):
        """Execute one handoff using the standard conversation runner path."""
        query = self._extract_query(inputs)
        channel = get_channel()
        conversation_id = channel.conversation_id if channel is not None else ""
        if not conversation_id:
            raise RuntimeError("conversation_id is required for conversation handoff")
        execution_id = kwargs.get("execution_id") or (channel.execution_id if channel else "")
        sub_execution_id = kwargs.get("sub_execution_id") or str(uuid.uuid4())
        tool_call_id = kwargs.get("tool_call_id") or str(uuid.uuid4())
        tool_name = self.card.name
        request = await self.build_child_request(query, conversation_id)
        runner = _runner_factory.get("ReAct")

        if channel is not None:
            await channel.emit(build_tool_call(
                channel.conversation_id,
                execution_id,
                tool_call_id,
                tool_name,
                arguments={"query": query},
            ))
        events = []
        try:
            async for event in self.stream_child_request(
                request,
                runner,
                execution_id=execution_id,
                sub_execution_id=sub_execution_id,
            ):
                events.append(event)
                if channel is not None:
                    await channel.emit(event)
        except Exception as error:
            if channel is not None:
                await channel.emit(build_tool_result(
                    channel.conversation_id,
                    execution_id,
                    tool_call_id,
                    tool_name,
                    result=str(error),
                ))
            raise

        result = ""
        if events:
            result = "".join(
                str(event.get("data", {}).get("delta") or "")
                for event in events
                if event.get("event") == "message"
            )
        if channel is not None:
            await channel.emit(build_tool_result(
                channel.conversation_id,
                execution_id,
                tool_call_id,
                tool_name,
                result=f"[子Agent {self.agent_id}] {result}",
            ))
        return {"result": result}
