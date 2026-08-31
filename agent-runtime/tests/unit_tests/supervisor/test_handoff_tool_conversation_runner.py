import pytest

from agent_runtime.conversation import execution_context as execution_context_module
from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
)
from agent_runtime.supervisor.tool.conversation_handoff_tool import ConversationHandoffTool


class _Runner:
    async def run_streaming(self, _request, _run_id):
        yield {"event": "message", "data": {"delta": "child answer"}}


@pytest.mark.asyncio
async def test_stream_child_request_uses_parent_and_child_run_ids():
    tool = ConversationHandoffTool(agent_id="child-agent", description="child agent")
    request = type("Request", (), {"conversation_id": "conversation-1"})()
    events = [
        event
        async for event in tool.stream_child_request(
            request, _Runner(), execution_id="parent-run-1", sub_execution_id="child-run-1"
        )
    ]

    assert [event["event"] for event in events] == ["message", "run_end"]
    for event in events:
        assert event["conversationId"] == "conversation-1"
        assert event["runId"] == "child-run-1"
        assert event["parentRunId"] == "parent-run-1"
        assert event["data"]["agentId"] == "child-agent"


@pytest.mark.asyncio
async def test_stream_child_request_preserves_reasoning_hierarchy():
    tool = ConversationHandoffTool(agent_id="child-agent", description="child agent")
    request = type("Request", (), {"conversation_id": "conversation-2"})()

    class _ReasoningRunner:
        async def run_streaming(self, _request, _run_id):
            yield {"event": "reasoning", "data": {"content": "thinking"}}

    events = [
        event
        async for event in tool.stream_child_request(
            request,
            _ReasoningRunner(),
            execution_id="parent-run-2",
            sub_execution_id="child-run-2",
        )
    ]

    assert events[0]["event"] == "reasoning"
    assert events[0]["conversationId"] == "conversation-2"
    assert events[0]["runId"] == "child-run-2"
    assert events[0]["parentRunId"] == "parent-run-2"


@pytest.mark.asyncio
async def test_build_child_request_uses_trusted_identity_inputs_and_request_model(monkeypatch):
    async def load_ir(_path):
        return {
            "agentId": "child-a",
            "configs": {"modelConfig": {"modelName": "original-model"}},
        }

    monkeypatch.setattr(
        "agent_runtime.supervisor.tool.conversation_handoff_tool.async_ir_load", load_ir
    )
    prepared_inputs = [{"fileName": "报告.pdf", "path": "/sandbox/root/input/a/报告.pdf"}]
    tool = ConversationHandoffTool(
        agent_id="child-a",
        description="child",
        prepared_file_references=prepared_inputs,
        model_deployment_id="request-model",
    )
    context = ConversationExecutionContext.create(
        ConversationIdentity(
            project_id="project-1",
            workspace_id="workspace-1",
            user_id="user-1",
            conversation_id="conversation-1",
            execution_id="turn-1",
        ),
        "/sandbox/root",
    )
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        request = await tool.build_child_request(
            query="inspect this",
            conversation_id="untrusted-conversation",
            sub_execution_id="sub-1",
        )
    finally:
        execution_context_module.reset_conversation_execution_context(token)

    variables = request.params.global_variables
    assert request.conversation_id == "conversation-1"
    assert request.user_id == "user-1"
    assert request.query == "inspect this"
    assert variables["projectId"] == "project-1"
    assert variables["workspaceId"] == "workspace-1"
    assert variables["executionId"] == "turn-1"
    assert variables["subExecutionId"] == "sub-1"
    assert variables["conversationInputFiles"] == prepared_inputs
    assert request.params.ir_cache["configs"]["modelConfig"]["modelName"] == "request-model"
