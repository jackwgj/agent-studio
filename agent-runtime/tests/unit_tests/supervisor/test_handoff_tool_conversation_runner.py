import pytest

from agent_runtime.conversation import execution_context as execution_context_module
from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
)
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
        self.calls.append((
            req,
            execution_id,
            execution_context_module.get_conversation_execution_context(),
        ))
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

    prepared_inputs = [{"fileName": "report.pdf", "path": "/sandbox/root/input/a/report.pdf"}]
    tool = ConversationHandoffTool(
        agent_id="child-a", description="child", prepared_file_references=prepared_inputs
    )
    context = ConversationExecutionContext.create(ConversationIdentity(
        project_id="project-1",
        workspace_id="workspace-1",
        user_id="user-1",
        conversation_id="conversation-1",
        execution_id="turn-1",
    ), "/sandbox/root")
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        request = await tool.build_child_request(
            query="inspect this",
            execution_id="untrusted-turn",
            sub_execution_id="sub-1",
        )

        events = []
        async for event in tool.stream_child_request(
            request,
            runner,
            execution_id="untrusted-turn",
            sub_execution_id="sub-1",
        ):
            events.append(event)
    finally:
        execution_context_module.reset_conversation_execution_context(token)

    assert runner.calls[0][0].ir_path == "agent/ir/child-a/child-a.json"
    assert runner.calls[0][0].query == "inspect this"
    assert runner.calls[0][1] == "sub-1"
    assert runner.calls[0][2] is context
    assert runner.calls[0][2].workspace is context.workspace
    assert request.conversation_id == "conversation-1"
    assert request.user_id == "user-1"
    assert request.params.global_variables == {
        "conversationId": "conversation-1",
        "projectId": "project-1",
        "workspaceId": "workspace-1",
        "userId": "user-1",
        "executionId": "turn-1",
        "subExecutionId": "sub-1",
        "conversationInputFiles": prepared_inputs,
    }
    assert events[0]["executionId"] == "turn-1"
    assert events[0]["data"]["subExecutionId"] == "sub-1"
    assert events[0]["data"]["agentId"] == "child-a"


async def _ir_load(path):
    return {
        "agentId": "child-a",
        "configs": {"modelConfig": {"modelName": "deployment-a"}},
    }
