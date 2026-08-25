"""Conversation-specific Supervisor handoff tool.

The legacy HandoffTool remains unchanged. This additive tool reuses its card and
IR-path behavior while delegating child execution to the conversation ReAct
runner's standard run_streaming contract.
"""

from __future__ import annotations

import uuid
from collections.abc import AsyncGenerator
from types import MappingProxyType

from agent_runtime.conversation.execution_context import (
    get_conversation_execution_context,
)
from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionParams, ExecutionRequest
from agent_runtime.supervisor.event.adapt import (
    build_message,
    build_reasoning,
    build_sub_done,
    build_sub_start,
    build_tool_call,
    build_tool_result,
)
from agent_runtime.supervisor.event.channel import get_channel
from agent_runtime.serve.apis.conversation_team_app import _event_payload
from agent_runtime.supervisor.tool.handoff_tool import HandoffTool
from jiuwen.serve.controllers.execution.open_utils import async_ir_load


_runner_factory = ConversationRunnerFactory()


class ConversationHandoffTool(HandoffTool):
    """Supervisor-only handoff that delegates child execution to the new runner."""

    def __init__(
        self,
        agent_id: str,
        description: str,
        prepared_file_references: list[dict] | None = None,
    ) -> None:
        super().__init__(agent_id=agent_id, description=description)
        self._prepared_file_references = tuple(
            MappingProxyType(dict(item))
            for item in (prepared_file_references or [])
            if isinstance(item, dict) and item.get("path")
        )

    async def build_child_request(
        self,
        query: str,
        execution_id: str,
        sub_execution_id: str,
    ) -> ExecutionRequest:
        """Load existing child IR and build the standard runner request."""
        execution_context = get_conversation_execution_context().for_child_call()
        identity = execution_context.identity
        ir_data = await async_ir_load(self._ir_path())
        history = []
        params = ExecutionParams(
            conversationHistory=history,
            globalVariables={
                "conversationId": identity.conversation_id,
                "projectId": identity.project_id,
                "workspaceId": identity.workspace_id,
                "userId": identity.user_id,
                "executionId": identity.execution_id,
                "subExecutionId": sub_execution_id,
                "conversationInputFiles": [dict(item) for item in self._prepared_file_references],
            },
            pluginConfigs=[],
            toolSwitchDict={},
            isDebug=False,
            ir_cache=ir_data,
        )
        return ExecutionRequest(
            conversationId=identity.conversation_id,
            userId=identity.user_id,
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
        """Run the child through the standard runner and add child metadata."""
        execution_context = get_conversation_execution_context().for_child_call()
        main_execution_id = execution_context.identity.execution_id
        child_execution_id = sub_execution_id or main_execution_id
        async for raw in runner.run_streaming(request, child_execution_id):
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
                        main_execution_id,
                        str(delta),
                        agent_id=self.agent_id,
                        sub_execution_id=child_execution_id,
                    )
            elif event == "reasoning":
                content = data.get("content") or ""
                if content:
                    yield build_reasoning(
                        main_execution_id,
                        str(content),
                        agent_id=self.agent_id,
                        sub_execution_id=child_execution_id,
                    )
            elif event == "done":
                # The enclosing handoff emits sub_done exactly once after the
                # standard child stream closes; done is not forwarded as a child boundary.
                continue

    async def invoke(self, inputs, **kwargs):
        """Execute one handoff using the standard conversation runner path."""
        query = self._extract_query(inputs)
        execution_context = get_conversation_execution_context().for_child_call()
        channel = get_channel()
        execution_id = execution_context.identity.execution_id
        sub_execution_id = kwargs.get("sub_execution_id") or str(uuid.uuid4())
        tool_call_id = kwargs.get("tool_call_id") or str(uuid.uuid4())
        tool_name = self.card.name
        request = await self.build_child_request(query, execution_id, sub_execution_id)
        runner = _runner_factory.get("ReAct")

        if channel is not None:
            await channel.emit(build_tool_call(
                execution_id,
                tool_call_id,
                tool_name,
                arguments={"query": query},
            ))
            await channel.emit(build_sub_start(execution_id, sub_execution_id, self.agent_id))
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
            await channel.emit(build_sub_done(execution_id, sub_execution_id, self.agent_id, result))
            await channel.emit(build_tool_result(
                execution_id,
                tool_call_id,
                tool_name,
                result=f"[子Agent {self.agent_id}] {result}",
            ))
        return {"result": result}
