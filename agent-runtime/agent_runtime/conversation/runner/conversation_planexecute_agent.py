"""Conversation-only PlanExecute Agent adapter.

This module keeps OpenJiuwen's AgentGroup and PlanExecute implementation intact
while replacing its request-time Skill/SysOperation injection for this entry.
"""

from __future__ import annotations

from jiuwen.controller.agent.agent import Agent
from jiuwen.controller.common.config import SkillInjectionContext
from jiuwen.multi_agent.agent_group.hierarchical_group.agent_group import (
    HierarchicalAgentGroup,
)


_CONVERSATION_SKILL_CONTEXT = "_conversation_planexecute_skill_context"


class ConversationPlanExecuteAgent(Agent):
    """Use the request-owned Skill context and never invoke official LOCAL setup."""

    def __init__(self, config=None):
        super().__init__(config=config)
        self._conversation_skill_context = getattr(
            config, _CONVERSATION_SKILL_CONTEXT, SkillInjectionContext.empty()
        )

    async def _inject_skills_if_needed(self, runtime_context, plugins):
        return list(plugins or []), self._conversation_skill_context

    def _patch_system_prompt(self, skills_prompt_suffix):
        # PlanExecuteMode receives the context directly. Avoid mutating the
        # process-global TemplateManager used by concurrent conversations.
        return None

    @staticmethod
    def _restore_system_prompt(skills_prompt_suffix, original_template):
        return None


class ConversationPlanExecuteAgentGroup(HierarchicalAgentGroup):
    """Register only the top-level PlanExecute member with the conversation adapter."""

    async def _register_agents(self):
        main_id = self.config.main_agent.metadata.id
        self.agents.add(main_id)
        self.runner.register_member(
            main_id, ConversationPlanExecuteAgent, self.config.main_agent
        )

        for config in self.config.agents:
            member_id = config.metadata.id
            if member_id == main_id:
                continue
            self.agents.add(member_id)
            self.runner.register_member(member_id, Agent, config)


def attach_conversation_skill_context(agent_config, context: SkillInjectionContext) -> None:
    setattr(agent_config, _CONVERSATION_SKILL_CONTEXT, context)
