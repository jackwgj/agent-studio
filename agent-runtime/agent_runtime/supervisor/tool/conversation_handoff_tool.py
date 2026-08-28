"""Conversation-only handoff through the canonical ReAct runner path."""

from __future__ import annotations

import copy
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
from agent_runtime.serve.apis.conversation_team_app import _event_payload
from agent_runtime.supervisor.event.canonical import (
    build_run_end,
    build_tool_call,
    build_tool_result,
)
from agent_runtime.supervisor.event.channel import (
    get_channel,
    reset_channel,
    set_channel,
)
from agent_runtime.supervisor.event.conversation_adapter import adapt_runner_event
from agent_runtime.supervisor.tool.handoff_tool import HandoffTool
from jiuwen.serve.controllers.execution.open_utils import async_ir_load


_runner_factory = ConversationRunnerFactory()


class ConversationHandoffTool(HandoffTool):
    """Delegate to one ReAct child while preserving trusted conversation state."""

    def __init__(
        self,
        agent_id: str,
        description: str,
        prepared_file_references: list[dict] | None = None,
        model_deployment_id: str | None = None,
    ) -> None:
        super().__init__(agent_id=agent_id, description=description)
        self._prepared_file_references = tuple(
            MappingProxyType(dict(item))
            for item in (prepared_file_references or [])
            if isinstance(item, dict) and item.get("path")
        )
        self._model_deployment_id = model_deployment_id

    async def build_child_request(
        self,
        query: str,
        conversation_id: str | None = None,
        sub_execution_id: str | None = None,
    ) -> ExecutionRequest:
        """Build a request from trusted ContextVar identity, never caller identity."""
        context = get_conversation_execution_context().for_child_call()
        identity = context.identity
        child_run_id = sub_execution_id or str(uuid.uuid4())
        ir_data = copy.deepcopy(await async_ir_load(self._ir_path()))
        if self._model_deployment_id:
            model_config = ir_data.setdefault("configs", {}).setdefault(
                "modelConfig", {}
            )
            model_config["modelName"] = self._model_deployment_id
        trusted_identity = {
            "projectId": identity.project_id,
            "workspaceId": identity.workspace_id,
            "userId": identity.user_id,
            "conversationId": identity.conversation_id,
            "executionId": identity.execution_id,
        }
        params = ExecutionParams(
            conversationHistory=[],
            globalVariables={
                **trusted_identity,
                "subExecutionId": child_run_id,
                "trustedConversationIdentity": trusted_identity,
                "conversationInputFiles": [
                    dict(item) for item in self._prepared_file_references
                ],
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
        """Adapt a child stream with an independent run and parent relation."""
        parent_run_id = execution_id or get_conversation_execution_context().identity.execution_id
        child_run_id = sub_execution_id or str(uuid.uuid4())
        terminal_seen = False
        async for raw in runner.run_streaming(request, child_run_id):
            if _event_payload(raw) is None:
                continue
            event = adapt_runner_event(
                raw,
                conversation_id=request.conversation_id,
                run_id=child_run_id,
                parent_run_id=parent_run_id,
                execution_type="sub_agent",
            )
            if event is None:
                continue
            event["data"]["agentId"] = self.agent_id
            if event["event"] in {"run_end", "error"}:
                if terminal_seen:
                    continue
                terminal_seen = True
            yield event
        if not terminal_seen:
            event = build_run_end(
                request.conversation_id,
                child_run_id,
                parent_run_id=parent_run_id,
                execution_type="sub_agent",
            )
            event["data"]["agentId"] = self.agent_id
            yield event

    async def invoke(self, inputs, **kwargs):
        """Execute one child and forward its canonical events to the root channel."""
        query = self._extract_query(inputs)
        context = get_conversation_execution_context().for_child_call()
        identity = context.identity
        root_channel = get_channel()
        if root_channel is None:
            raise RuntimeError("conversation event channel is required for handoff")
        parent_run_id = kwargs.get("execution_id") or identity.execution_id
        child_run_id = kwargs.get("sub_execution_id") or str(uuid.uuid4())
        tool_call_id = kwargs.get("tool_call_id") or str(uuid.uuid4())
        tool_name = self.card.name
        request = await self.build_child_request(
            query,
            identity.conversation_id,
            child_run_id,
        )
        runner = _runner_factory.get("ReAct")

        await root_channel.emit(
            build_tool_call(
                identity.conversation_id,
                parent_run_id,
                tool_call_id,
                tool_name,
                arguments={"query": query},
            )
        )
        events: list[dict] = []
        child_channel = root_channel.child(child_run_id, agent_id=self.agent_id)
        child_channel_token = set_channel(child_channel)
        try:
            async for event in self.stream_child_request(
                request,
                runner,
                execution_id=parent_run_id,
                sub_execution_id=child_run_id,
            ):
                events.append(event)
                await root_channel.emit(event)
        except Exception as error:
            await root_channel.emit(
                build_tool_result(
                    identity.conversation_id,
                    parent_run_id,
                    tool_call_id,
                    tool_name,
                    result=str(error),
                )
            )
            raise
        finally:
            reset_channel(child_channel_token)

        result = "".join(
            str(event.get("data", {}).get("delta") or "")
            for event in events
            if event.get("event") == "message"
        )
        await root_channel.emit(
            build_tool_result(
                identity.conversation_id,
                parent_run_id,
                tool_call_id,
                tool_name,
                result=f"[子Agent {self.agent_id}] {result}",
            )
        )
        return {"result": result}
