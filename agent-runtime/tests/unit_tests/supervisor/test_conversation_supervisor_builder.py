import pytest

from agent_runtime.supervisor.conversation_supervisor_builder import (
    build_conversation_supervisor,
)
from agent_runtime.supervisor.tool.conversation_handoff_tool import (
    ConversationHandoffTool,
)


class _AbilityManager:
    def __init__(self):
        self.cards = []

    def add(self, card):
        self.cards.append(card)
        return type("AddResult", (), {"added": True})()

    def list(self):
        return self.cards


class _FakeAgent:
    def __init__(self, card):
        self.card = card
        self.ability_manager = _AbilityManager()

    def configure(self, _config):
        pass


@pytest.mark.asyncio
async def test_conversation_supervisor_builder_injects_new_handoff_tool(monkeypatch):
    async def load_description(agent_id):
        return f"description-{agent_id}"

    monkeypatch.setattr(
        "agent_runtime.supervisor.conversation_supervisor_builder._load_sub_agent_description",
        load_description,
    )
    monkeypatch.setattr(
        "agent_runtime.supervisor.conversation_supervisor_builder.ReActAgent",
        _FakeAgent,
    )
    monkeypatch.setattr(
        "agent_runtime.supervisor.conversation_supervisor_builder.build_react_config",
        lambda *_args: object(),
    )

    agent = await build_conversation_supervisor(
        sub_agent_ids=["child-a"],
        model_deployment_id="deployment-a",
    )

    assert any(card.id == "handoff_child-a" for card in agent.ability_manager.list())
