import json
from types import SimpleNamespace

import pytest

from agent_runtime.conversation.runner import conversation_controller_runner as controller_runner_module
from agent_runtime.conversation.runner.conversation_controller_runner import (
    ConversationControllerRunner,
)
from agent_runtime.serve.apis import conversation_team_app
from agent_runtime.serve.apis.conversation_team import ConversationTeamReq, team_sse_stream
from agent_runtime.supervisor.event.channel import get_channel


class _FakeConversationRunner:
    def __init__(self, events):
        self.events = events
        self.calls = []

    async def run_streaming(self, request, execution_id=None):
        self.calls.append((request, execution_id))
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
async def test_team_stream_defaults_to_new_supervisor_path(monkeypatch):
    called = {}

    async def build_config(**kwargs):
        called["config"] = kwargs
        return SimpleNamespace()

    async def new_runner(req, execution_id, config):
        called["run"] = (req, execution_id, config)
        yield {"event": "message", "data": {"delta": "new-path"}}

    async def old_builder(**_kwargs):
        raise AssertionError("legacy Supervisor path must not be default")

    monkeypatch.delenv("CONVERSATION_TEAM_USE_LEGACY_SUPERVISOR", raising=False)
    monkeypatch.setattr(
        "agent_runtime.serve.apis.conversation_team.build_conversation_supervisor_config",
        build_config,
    )
    monkeypatch.setattr(
        "agent_runtime.serve.apis.conversation_team.run_conversation_supervisor",
        new_runner,
    )
    monkeypatch.setattr(
        "agent_runtime.serve.apis.conversation_team.build_supervisor",
        old_builder,
    )

    req = ConversationTeamReq.model_validate({
        "conversationId": "conversation-1",
        "query": "hello",
        "subAgentIds": ["child-a"],
        "modelDeploymentId": "deployment-a",
    })
    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(req, "execution-1")]

    assert called["config"]["sub_agent_ids"] == ["child-a"]
    assert called["run"][1] == "execution-1"
    assert events[-1]["event"] == "message"
    assert events[-1]["data"]["delta"] == "new-path"


def test_controller_runner_builds_request_skill_context_without_rewriting_ir():
    team_config = {
        "skillCatalog": [
            {
                "skillId": "research",
                "versionId": "v2",
                "name": "research",
                "description": "research tasks",
                "objectKey": "skills/research/v2.zip",
            }
        ],
        "recommendedSkillIds": ["research"],
    }

    context = ConversationControllerRunner._build_skill_context(team_config)

    assert context.catalog_by_id["research"].version_id == "v2"
    assert context.recommended_skill_ids == ("research",)

@pytest.mark.asyncio
async def test_app_controller_preserves_request_skills_in_runner_request(monkeypatch):
    raw_runner = _FakeConversationRunner([
        {"event": "message", "data": {"answer": "controller-answer"}},
    ])
    factory = _FakeFactory(raw_runner)

    async def load_ir(_path):
        return {"configs": {"mode": "Controller"}}

    monkeypatch.setattr(conversation_team_app, "async_ir_load", load_ir)
    monkeypatch.setattr(conversation_team_app, "prepare_params", lambda request: request.params)
    monkeypatch.setattr(conversation_team_app, "_conversation_runner_factory", factory)

    req = SimpleNamespace(
        app_id="app-controller",
        conversation_id="conversation-controller",
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

    [event async for event in conversation_team_app.stream_application(req, "execution-controller")]

    assert factory.modes == ["Controller"]
    assert raw_runner.calls[0][0].params.global_variables["conversationTeam"]["type"] == "APP"
    assert raw_runner.calls[0][0].params.global_variables["conversationTeam"]["skillCatalog"][0]["skillId"] == "research"
