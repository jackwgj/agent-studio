import pytest

from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)
from agent_runtime.supervisor.tool.conversation_handoff_tool import (
    ConversationHandoffTool,
)


class _FakeRunner:
    def __init__(self):
        self.calls = []

    async def run_streaming(self, req, execution_id=None):
        self.calls.append((req, execution_id))
        yield {"event": "message", "data": {"delta": "child-result"}}
        yield {"event": "done", "data": {"answer": "child-result"}}


@pytest.mark.asyncio
async def test_conversation_handoff_uses_standard_conversation_runner(monkeypatch):
    runner = _FakeRunner()
    factory = ConversationRunnerFactory()
    monkeypatch.setattr(factory, "get", lambda mode: runner)
    monkeypatch.setattr(
        "agent_runtime.supervisor.tool.conversation_handoff_tool._runner_factory",
        factory,
    )
    monkeypatch.setattr(
        "agent_runtime.supervisor.tool.conversation_handoff_tool.async_ir_load",
        lambda path: _ir_load(path),
    )

    tool = ConversationHandoffTool(agent_id="child-a", description="child")
    request = await tool.build_child_request(
        query="inspect this",
        execution_id="turn-1",
        sub_execution_id="sub-1",
    )

    events = []
    async for event in tool.stream_child_request(
        request,
        runner,
        execution_id="turn-1",
        sub_execution_id="sub-1",
    ):
        events.append(event)

    assert runner.calls[0][0].ir_path == "agent/ir/child-a/child-a.json"
    assert runner.calls[0][0].query == "inspect this"
    assert runner.calls[0][1] == "sub-1"
    assert events[0]["data"]["subExecutionId"] == "sub-1"
    assert events[0]["data"]["agentId"] == "child-a"


async def _ir_load(path):
    return {
        "agentId": "child-a",
        "configs": {"modelConfig": {"modelName": "deployment-a"}},
    }
