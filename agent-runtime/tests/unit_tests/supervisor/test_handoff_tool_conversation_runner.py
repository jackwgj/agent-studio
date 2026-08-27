import pytest

from agent_runtime.supervisor.tool.conversation_handoff_tool import ConversationHandoffTool


class _Runner:
    async def run_streaming(self, _request, _run_id):
        yield {
            "event": "message",
            "data": {"delta": "child answer"},
        }


@pytest.mark.asyncio
async def test_stream_child_request_uses_request_conversation_id_for_child_event():
    tool = ConversationHandoffTool(
        agent_id="child-agent",
        description="child agent",
    )
    request = type("Request", (), {"conversation_id": "conversation-1"})()

    events = [
        event
        async for event in tool.stream_child_request(
            request,
            _Runner(),
            execution_id="parent-run-1",
            sub_execution_id="child-run-1",
        )
    ]

    assert len(events) == 1
    event = events[0]
    assert event["conversationId"] == "conversation-1"
    assert event["runId"] == "child-run-1"
    assert event["parentRunId"] == "parent-run-1"
    assert event["data"]["runId"] == "child-run-1"
    assert event["data"]["parentRunId"] == "parent-run-1"


@pytest.mark.asyncio
async def test_stream_child_request_uses_same_conversation_id_for_reasoning():
    tool = ConversationHandoffTool(
        agent_id="child-agent",
        description="child agent",
    )
    request = type("Request", (), {"conversation_id": "conversation-2"})()

    class _ReasoningRunner:
        async def run_streaming(self, _request, _run_id):
            yield {
                "event": "reasoning",
                "data": {"content": "thinking"},
            }

    events = [
        event
        async for event in tool.stream_child_request(
            request,
            _ReasoningRunner(),
            execution_id="parent-run-2",
            sub_execution_id="child-run-2",
        )
    ]



@pytest.mark.asyncio
async def test_build_child_request_preserves_parent_conversation_id(monkeypatch):
    async def load_ir(_path):
        return {"agentName": "child agent"}

    monkeypatch.setattr(
        "agent_runtime.supervisor.tool.conversation_handoff_tool.async_ir_load",
        load_ir,
    )
    tool = ConversationHandoffTool(
        agent_id="child-agent",
        description="child agent",
    )

    request = await tool.build_child_request("child task", "conversation-3")

    assert request.conversation_id == "conversation-3"
    assert request.query == "child task"
