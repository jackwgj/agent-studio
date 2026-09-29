import asyncio
from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest

from agent_runtime.conversation.config.supervisor_config import SupervisorConfig
from agent_runtime.conversation.rail.graceful_tool_budget_rail import (
    GracefulToolBudgetRail,
)
from agent_runtime.conversation.runner.conversation_react_runner import (
    ConversationReActRunner,
)
from agent_runtime.supervisor.conversation_supervisor_builder import (
    build_conversation_supervisor_config,
)
from agent_runtime.common.config import settings


def test_supervisor_config_converts_to_react_ir_shape():
    config = SupervisorConfig(
        agent_id="conversation_team_supervisor",
        agent_name="Team Supervisor",
        description="Dispatches work to configured agents",
        system_prompt="Delegate the task.",
        model_deployment_id="deployment-a",
        max_iterations=30,
        allowed_sub_agent_ids=["agent-a", "agent-b"],
    )

    ir = config.to_ir()

    assert ir["agentId"] == "conversation_team_supervisor"
    assert ir["agentName"] == "Team Supervisor"
    assert ir["description"] == "Dispatches work to configured agents"
    assert ir["configs"]["mode"] == "ReAct"
    assert ir["configs"]["sysPromptTemplate"] == "Delegate the task."
    assert ir["configs"]["maxIteration"] == 30
    assert ir["configs"]["modelConfig"]["modelName"] == "deployment-a"
    assert ir["configs"]["plugins"] == []
    assert ir["configs"]["workflows"] == []
    assert ir["configs"]["conversationTeam"]["subAgentIds"] == ["agent-a", "agent-b"]


def test_conversation_react_runner_exposes_supervisor_ir_conversion():
    config = SupervisorConfig(
        agent_id="supervisor",
        agent_name="Supervisor",
        description="desc",
        system_prompt="prompt",
        model_deployment_id="deployment-a",
        max_iterations=30,
        allowed_sub_agent_ids=[],
    )

    ir = ConversationReActRunner()._convert_supervisor_to_ir(config)

    assert ir["agentId"] == "supervisor"
    assert ir["configs"]["mode"] == "ReAct"
    assert ir["configs"]["maxIteration"] == 30


def test_conversation_supervisor_builder_uses_runtime_iteration_setting(monkeypatch):
    monkeypatch.setattr(settings.agent, "max_iteration", 42)

    config = asyncio.run(
        build_conversation_supervisor_config(
            sub_agent_ids=["agent-a"],
            model_deployment_id="deployment-a",
        )
    )

    assert config.max_iterations == 42
    assert config.to_ir()["configs"]["maxIteration"] == 42


def test_default_supervisor_gets_one_extra_tool_free_summary_iteration():
    runner = ConversationReActRunner()
    ir = {
        "agentId": "conversation_team_supervisor",
        "configs": {"mode": "ReAct", "maxIteration": 2},
    }

    prepared_ir, tool_budget = runner._prepare_graceful_tool_budget_ir(
        ir, {"type": "SUPERVISOR"}
    )

    assert tool_budget == 2
    assert prepared_ir["configs"]["maxIteration"] == 3
    assert ir["configs"]["maxIteration"] == 2


def test_default_supervisor_budget_of_one_hundred_reserves_iteration_101_for_summary():
    ir = {
        "agentId": "conversation_team_supervisor",
        "configs": {"mode": "ReAct", "maxIteration": 100},
    }

    prepared_ir, tool_budget = ConversationReActRunner()._prepare_graceful_tool_budget_ir(
        ir, {"type": "SUPERVISOR"}
    )

    assert tool_budget == 100
    assert prepared_ir["configs"]["maxIteration"] == 101


@pytest.mark.parametrize(
    ("ir", "team_config"),
    [
        ({"agentId": "published-react", "configs": {"maxIteration": 2}}, {"type": "APP"}),
        ({"agentId": "published-plan", "configs": {"maxIteration": 2}}, {"type": "APP"}),
        ({"agentId": "controller", "configs": {"maxIteration": 2}}, {"type": "CONTROLLER"}),
    ],
)
def test_non_default_agents_do_not_get_graceful_budget(ir, team_config):
    prepared_ir, tool_budget = ConversationReActRunner()._prepare_graceful_tool_budget_ir(
        ir, team_config
    )

    assert tool_budget is None
    assert prepared_ir == ir


@pytest.mark.asyncio
async def test_budget_rail_registration_is_explicit_and_configured():
    agent = SimpleNamespace(register_rail=AsyncMock())

    await ConversationReActRunner()._register_graceful_tool_budget_rail(agent, 7)

    rail = agent.register_rail.await_args.args[0]
    assert isinstance(rail, GracefulToolBudgetRail)
    assert rail.max_tool_call_rounds == 7
