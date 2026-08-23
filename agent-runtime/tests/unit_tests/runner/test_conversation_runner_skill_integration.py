import pytest

from agent_runtime.conversation.runner.conversation_react_runner import (
    ConversationReActRunner,
)


class _AbilityManager:
    def add(self, card):
        return type("AddResult", (), {"added": True})()


class _Agent:
    def __init__(self):
        self.ability_manager = _AbilityManager()
        self.prompt_sections = []

    def add_prompt_builder_section(self, name, content, priority):
        self.prompt_sections.append((name, content, priority))


@pytest.mark.asyncio
async def test_react_runner_attaches_request_supervisor_skill_context(monkeypatch):
    captured = {}

    async def attach(agent, catalog, recommended):
        captured["agent"] = agent
        captured["catalog"] = catalog
        captured["recommended"] = recommended

    monkeypatch.setattr(
        "agent_runtime.conversation.runner.conversation_react_runner.attach_skill_context",
        attach,
    )

    runner = ConversationReActRunner()
    agent = _Agent()
    team_config = {
        "type": "SUPERVISOR",
        "skillCatalog": [
            {
                "skillId": "meeting-minutes",
                "versionId": "v1",
                "name": "Meeting Minutes",
                "description": "Structure meeting notes",
                "objectKey": "user/skills/meeting-minutes/v1/skill.zip",
            }
        ],
        "recommendedSkillIds": ["meeting-minutes"],
    }

    await runner._attach_supervisor_skill_context(agent, team_config)

    assert captured["agent"] is agent
    assert captured["catalog"][0].skill_id == "meeting-minutes"
    assert captured["recommended"] == ["meeting-minutes"]
