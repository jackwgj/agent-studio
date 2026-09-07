"""Conversation Supervisor builder using the additive handoff tool."""

from agent_runtime.supervisor.builder import (
    SUPERVISOR_SYSTEM_PROMPT,
    format_file_references,
)
from agent_runtime.supervisor.config import format_conversation_history
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
