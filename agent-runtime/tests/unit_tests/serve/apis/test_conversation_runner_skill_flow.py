from types import SimpleNamespace

import pytest

from agent_runtime.conversation import execution_context as execution_context_module
from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
)
from agent_runtime.conversation import supervisor_runner


class _Runner:
    def __init__(self):
        self.request = None

    async def run_streaming(self, request, _execution_id):
        self.request = request
        yield {"event": "done", "data": {}}


@pytest.mark.asyncio
async def test_supervisor_bridge_passes_skill_catalog_to_runner_context(monkeypatch):
    runner = _Runner()
    monkeypatch.setattr(
        supervisor_runner,
        "ConversationRunnerFactory",
        lambda: SimpleNamespace(get=lambda _mode: runner),
    )

    req = SimpleNamespace(
        conversation_id="conversation-1",
        project_id="project-1",
        workspace_id="workspace-1",
        user_id="user-1",
        query="hello",
        sub_agent_ids=[],
        skill_catalog=[
            SimpleNamespace(
                skill_id="meeting-minutes",
                version_id="v1",
                name="Meeting Minutes",
                description="Structure meeting notes",
                object_key="user/skills/meeting-minutes/v1/skill.zip",
            )
        ],
        recommended_skill_ids=["meeting-minutes"],
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
        _ = [event async for event in supervisor_runner.run_conversation_supervisor(
            req, "execution-1", config
        )]
    finally:
        execution_context_module.reset_conversation_execution_context(token)

    team = runner.request.params.global_variables["conversationTeam"]
    assert team["recommendedSkillIds"] == ["meeting-minutes"]
    assert team["skillCatalog"][0]["skillId"] == "meeting-minutes"
