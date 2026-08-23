import pytest

from agent_runtime.supervisor.conversation_supervisor_builder import (
    build_conversation_supervisor,
)
from agent_runtime.supervisor.skill_context import (
    bind_agent_skill_context,
    get_skill_context,
    reset_skill_context,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor


class _AbilityManager:
    def __init__(self):
        self.cards = []

    def add(self, card):
        self.cards.append(card)
        return type("AddResult", (), {"added": True})()

    def list(self):
        return self.cards


class _Agent:
    def __init__(self, card):
        self.card = card
        self.ability_manager = _AbilityManager()
        self.prompt_sections = []

    def configure(self, _config):
        pass

    def add_prompt_builder_section(self, name, content, priority):
        self.prompt_sections.append((name, content, priority))


def _skill():
    return SkillDescriptor(
        skill_id="meeting-minutes",
        version_id="v1",
        name="Meeting Minutes",
        description="Structure meeting notes",
        object_key="user/skills/meeting-minutes/v1/skill.zip",
    )


@pytest.mark.asyncio
async def test_new_supervisor_builder_injects_catalog_and_activation_tool(monkeypatch):
    monkeypatch.setattr(
        "agent_runtime.supervisor.conversation_supervisor_builder.ReActAgent",
        _Agent,
    )
    monkeypatch.setattr(
        "agent_runtime.supervisor.conversation_supervisor_builder.build_react_config",
        lambda *_args: object(),
    )

    agent = await build_conversation_supervisor(
        sub_agent_ids=[],
        model_deployment_id="deployment-a",
        skill_catalog=[_skill()],
        recommended_skill_ids=["meeting-minutes"],
    )

    assert agent.prompt_sections
    assert "meeting-minutes" in agent.prompt_sections[0][1]
    assert any(card.id == "conversation_activate_skill" for card in agent.ability_manager.list())

    token = bind_agent_skill_context(agent)
    try:
        context = get_skill_context()
        assert context is not None
        assert "meeting-minutes" in context.catalog_by_id
    finally:
        reset_skill_context(token)

    assert get_skill_context() is None
