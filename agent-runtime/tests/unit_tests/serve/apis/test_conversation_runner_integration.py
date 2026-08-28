import json
from types import SimpleNamespace

import pytest

from agent_runtime.conversation import execution_context as execution_context_module
from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
)
from agent_runtime.conversation import supervisor_runner
from agent_runtime.serve.apis import conversation_team_app
from agent_runtime.serve.apis.conversation_team import ConversationTeamReq, team_sse_stream
from agent_runtime.supervisor.event.channel import get_channel


class _FakeConversationRunner:
    def __init__(self, events):
        self.events = events
        self.calls = []
        self.contexts = []

    async def run_streaming(self, request, execution_id=None):
        self.calls.append((request, execution_id))
        self.contexts.append(
            execution_context_module.get_conversation_execution_context()
        )
        for event in self.events:
            yield event


class _FakeFactory:
    def __init__(self, runner):
        self.runner = runner
        self.modes = []

    def get(self, mode):
        self.modes.append(mode)
        return self.runner


@pytest.mark.asyncio
async def test_conversation_supervisor_bridge_consumes_standard_runner_events(monkeypatch):
    raw_runner = _FakeConversationRunner([
        {"event": "message", "data": {"delta": "主回答"}},
        {"event": "run_end", "data": {"status": "success", "text": "主回答"}},
    ])
    monkeypatch.setattr(
        supervisor_runner,
        "ConversationRunnerFactory",
        lambda: _FakeFactory(raw_runner),
    )
    request = SimpleNamespace(
        conversation_id="untrusted-conversation",
        project_id="untrusted-project",
        workspace_id="untrusted-workspace",
        user_id="untrusted-user",
        query="hello",
        sub_agent_ids=["child-a"],
        model_deployment_id="deployment-a",
        skill_catalog=[],
        recommended_skill_ids=[],
    )
    config = SimpleNamespace(
        to_ir=lambda: {
            "agentId": "conversation_team_supervisor",
            "configs": {"mode": "ReAct"},
        }
    )

    context = ConversationExecutionContext.create(ConversationIdentity(
        project_id="project-1",
        workspace_id="workspace-1",
        user_id="user-1",
        conversation_id="conversation-1",
        execution_id="execution-1",
    ), "/sandbox/root")
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        events = [event async for event in supervisor_runner.run_conversation_supervisor(
            request, "execution-1", config
        )]
    finally:
        execution_context_module.reset_conversation_execution_context(token)

    assert [event["event"] for event in events] == ["message", "run_end"]
    assert events[0]["data"]["delta"] == "主回答"
    assert events[0]["runId"] == "execution-1"
    assert events[-1]["data"]["text"] == "主回答"
    execution_request = raw_runner.calls[0][0]
    assert raw_runner.contexts == [context]
    assert raw_runner.contexts[0].workspace is context.workspace
    assert execution_request.conversation_id == "conversation-1"
    assert execution_request.user_id == "user-1"
    assert execution_request.params.global_variables["conversationId"] == "conversation-1"
    assert execution_request.params.global_variables["projectId"] == "project-1"
    assert execution_request.params.global_variables["workspaceId"] == "workspace-1"
    assert execution_request.params.global_variables["userId"] == "user-1"
    assert execution_request.params.global_variables["executionId"] == "execution-1"
    assert get_channel() is None


@pytest.mark.asyncio
async def test_team_stream_defaults_to_new_supervisor_path(monkeypatch):
    called = {}

    async def build_config(**kwargs):
        called["config"] = kwargs
        return SimpleNamespace()

    async def new_runner(req, execution_id, config, prepared_file_references=None):
        called["run"] = (req, execution_id, config, prepared_file_references)
        yield {"event": "message", "data": {"delta": "new-path"}}

    monkeypatch.setattr(
        "agent_runtime.serve.apis.conversation_team.build_conversation_supervisor_config",
        build_config,
    )
    monkeypatch.setattr(
        "agent_runtime.serve.apis.conversation_team.run_conversation_supervisor",
        new_runner,
    )

    req = ConversationTeamReq.model_validate({
        "conversationId": "conversation-1",
        "projectId": "project-1",
        "workspaceId": "workspace-1",
        "userId": "user-1",
        "query": "hello",
        "subAgentIds": ["child-a"],
        "modelDeploymentId": "deployment-a",
    })
    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(req, "execution-1")]

    assert called["config"]["sub_agent_ids"] == ["child-a"]
    assert called["run"][1] == "execution-1"
    assert [event["event"] for event in events[-2:]] == ["message", "run_end"]
    assert events[-2]["data"]["delta"] == "new-path"

@pytest.mark.asyncio
async def test_app_react_preserves_trusted_context_inputs_and_request_skills(monkeypatch):
    raw_runner = _FakeConversationRunner([
        {"event": "message", "data": {"answer": "controller-answer"}},
    ])
    factory = _FakeFactory(raw_runner)

    async def load_ir(_path):
        return {"configs": {"mode": "ReAct"}}

    monkeypatch.setattr(conversation_team_app, "async_ir_load", load_ir)
    monkeypatch.setattr(conversation_team_app, "prepare_params", lambda request: request.params)
    monkeypatch.setattr(conversation_team_app, "_conversation_runner_factory", factory)

    req = SimpleNamespace(
        app_id="app-1",
        conversation_id="conversation-1",
        project_id="project-1",
        workspace_id="workspace-1",
        user_id="user-1",
        query="use the skill",
        conversation_history=[],
        skill_catalog=[
            SimpleNamespace(
                skill_id="research",
                version_id="v2",
                name="Research",
                description="Research tasks",
                object_key="skills/research/v2.zip",
            )
        ],
        recommended_skill_ids=["research"],
    )
    context = ConversationExecutionContext.create(ConversationIdentity(
        project_id="trusted-project",
        workspace_id="trusted-workspace",
        user_id="trusted-user",
        conversation_id="trusted-conversation",
        execution_id="execution-1",
    ), "/sandbox/root")
    token = execution_context_module.set_conversation_execution_context(context)
    prepared_inputs = [{"fileName": "report.pdf", "path": "/sandbox/root/input/a/report.pdf"}]
    try:
        events = [event async for event in conversation_team_app.stream_application(
            req, "ignored-execution", prepared_inputs
        )]
    finally:
        execution_context_module.reset_conversation_execution_context(token)

    assert factory.modes == ["ReAct"]
    assert raw_runner.calls[0][0].ir_path == "agent/ir/app-1/app-1.json"
    execution_request = raw_runner.calls[0][0]
    assert raw_runner.contexts == [context]
    assert raw_runner.contexts[0].workspace is context.workspace
    assert execution_request.conversation_id == "trusted-conversation"
    assert execution_request.user_id == "trusted-user"
    assert raw_runner.calls[0][1] == "execution-1"
    global_variables = raw_runner.calls[0][0].params.global_variables
    assert global_variables["conversationId"] == "trusted-conversation"
    assert global_variables["projectId"] == "trusted-project"
    assert global_variables["workspaceId"] == "trusted-workspace"
    assert global_variables["userId"] == "trusted-user"
    assert global_variables["executionId"] == "execution-1"
    assert global_variables["conversationInputFiles"] == prepared_inputs
    assert [event["event"] for event in events] == ["message"]
    assert events[0]["data"]["delta"] == "controller-answer"
    assert global_variables["conversationTeam"]["type"] == "APP"
    assert global_variables["conversationTeam"]["skillCatalog"][0]["skillId"] == "research"


@pytest.mark.asyncio
@pytest.mark.parametrize("mode", ["Controller", "PlanExecute"])
async def test_app_non_react_modes_remain_routable_without_sys_operation_card(
    monkeypatch, mode
):
    raw_runner = _FakeConversationRunner(
        [{"event": "message", "data": {"answer": f"{mode}-answer"}}]
    )
    factory = _FakeFactory(raw_runner)

    async def load_ir(_path):
        return {"configs": {"mode": mode}}

    monkeypatch.setattr(conversation_team_app, "async_ir_load", load_ir)
    monkeypatch.setattr(
        conversation_team_app, "prepare_params", lambda request: request.params
    )
    monkeypatch.setattr(conversation_team_app, "_conversation_runner_factory", factory)

    req = SimpleNamespace(
        app_id="app-non-react",
        conversation_history=[],
        query="hello",
        skill_catalog=[],
        recommended_skill_ids=[],
    )
    context = ConversationExecutionContext.create(
        ConversationIdentity(
            project_id="project-1",
            workspace_id="workspace-1",
            user_id="user-1",
            conversation_id="conversation-1",
            execution_id="execution-1",
        ),
        "/sandbox/root",
    )
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        events = [
            event
            async for event in conversation_team_app.stream_application(
                req, "execution-1"
            )
        ]
    finally:
        execution_context_module.reset_conversation_execution_context(token)

    assert factory.modes == [mode]
    assert raw_runner.calls[0][0].params.sys_operation_card is None
    assert [event["event"] for event in events] == ["message"]
