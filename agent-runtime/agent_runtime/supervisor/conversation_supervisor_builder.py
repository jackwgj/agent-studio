"""Conversation Supervisor builder using the additive handoff tool."""

from openjiuwen.core.runner import Runner
from openjiuwen.core.single_agent.agents.react_agent import ReActAgent
from openjiuwen.core.single_agent.schema.agent_card import AgentCard

from agent_runtime.supervisor.builder import (
    SUPERVISOR_SYSTEM_PROMPT,
    _load_sub_agent_description,
    _register_file_reader,
    format_file_references,
    normalize_skill_inputs,
)
from agent_runtime.supervisor.config import build_react_config, format_conversation_history
from agent_runtime.supervisor.skill_context import attach as attach_skill_context
from agent_runtime.supervisor.skill_model import SkillDescriptor
from agent_runtime.supervisor.tool.conversation_handoff_tool import ConversationHandoffTool
from agent_runtime.conversation.config.supervisor_config import SupervisorConfig


async def build_conversation_supervisor_config(
    sub_agent_ids: list,
    model_deployment_id: str,
    conversation_history: list | None = None,
    file_references: list[dict] | None = None,
) -> SupervisorConfig:
    """Build code-owned Supervisor configuration for the derived ReAct runner."""
    system_prompt = (
        SUPERVISOR_SYSTEM_PROMPT
        + format_conversation_history(conversation_history)
        + format_file_references(file_references)
    )
    return SupervisorConfig(
        agent_id="conversation_team_supervisor",
        agent_name="Team Supervisor",
        description="团队监督者，负责把任务分派给最合适的子 Agent",
        system_prompt=system_prompt,
        model_deployment_id=model_deployment_id,
        allowed_sub_agent_ids=tuple(sub_agent_ids),
    )


async def build_conversation_supervisor(
    sub_agent_ids: list,
    model_deployment_id: str,
    conversation_history: list | None = None,
    skill_catalog: list[SkillDescriptor] | None = None,
    recommended_skill_ids: list[str] | None = None,
    file_references: list[dict] | None = None,
) -> ReActAgent:
    """Build the additive Supervisor variant with ConversationHandoffTool only."""
    skill_catalog, recommended_skill_ids = normalize_skill_inputs(
        skill_catalog, recommended_skill_ids
    )

    tools = []
    for agent_id in sub_agent_ids:
        description = await _load_sub_agent_description(agent_id)
        tools.append(ConversationHandoffTool(agent_id=agent_id, description=description))

    system_prompt = (
        SUPERVISOR_SYSTEM_PROMPT
        + format_conversation_history(conversation_history)
        + format_file_references(file_references)
    )
    agent = ReActAgent(
        card=AgentCard(
            id="conversation_team_supervisor",
            name="Team Supervisor",
            description="团队监督者，负责把任务分派给最合适的子 Agent",
        )
    )
    agent.configure(build_react_config(system_prompt, model_deployment_id))
    await attach_skill_context(agent, skill_catalog, recommended_skill_ids)
    if file_references:
        _register_file_reader(agent)

    for tool in tools:
        result = agent.ability_manager.add(tool.card)
        if result.added:
            existing = Runner.resource_mgr.get_tool(tool.card.id)
            if existing is None:
                Runner.resource_mgr.add_tool(tool)
    return agent
