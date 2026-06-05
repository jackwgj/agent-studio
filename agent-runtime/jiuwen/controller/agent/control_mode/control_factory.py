#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Any

from jiuwen.controller.agent.control_mode.base_mode import BaseMode
from jiuwen.controller.agent.control_mode.control_mode import ControllerMode
from jiuwen.controller.agent.control_mode.plan_execute_mode import PlanExecuteMode
from jiuwen.controller.agent.control_mode.react_mode import ReactMode
from jiuwen.controller.agent.control_mode.tool_use_mode import ToolUseMode
from jiuwen.controller.common.config import AgentConfig, ControllerConfig


class ControlFactory:
    """规划器工厂

    用于创建各种任务规划器的工厂。
    """

    _control_mode_map = {
        "ReAct": ReactMode,
        "Controller": ControllerMode,
        "ToolUse": ToolUseMode,
        "PlanExecute": PlanExecuteMode,
    }

    @classmethod
    def create_control_mode(
        cls, agent_config: AgentConfig = None, context_manager: Any = None
    ) -> BaseMode:
        """创建规划器

        Args:

        Returns:
            创建的规划器对象
        """
        mode = agent_config.plan_config.plan_mode
        if mode not in cls._control_mode_map:
            raise ValueError(f"Unsupported planning mode: {mode}")
        if mode == "Controller":
            plan_config: ControllerConfig = agent_config.plan_config
            plan_config.start_workflow = agent_config.start_workflow
            plan_config.end_workflow = agent_config.end_workflow
            plan_config.global_workflows = agent_config.global_workflows
            plan_config.default_workflow = agent_config.default_workflow
            plan_config.user_prompt = agent_config.user_prompt
            plan_config.intent_identification = agent_config.intent_identification
            plan_config.metadata = agent_config.metadata
            plan_config.parent_agent_metadata = agent_config.parent_agent_metadata
            plan_config.child_agents_metadata = agent_config.child_agents_metadata
            return ControllerMode(
                plan_config=agent_config.plan_config,
                model=agent_config.model,
                plugins=agent_config.plugins,
                workflows=agent_config.workflows,
                context_manager=context_manager,
                task_id=agent_config.task_id,
            )
        if mode == "ToolUse":
            return ToolUseMode(
                plan_config=agent_config.plan_config,
                model=agent_config.model,
                plugins=agent_config.plugins,
                workflows=agent_config.workflows,
                context_manager=context_manager,
                task_id=agent_config.task_id,
            )

        if mode == "PlanExecute":
            return PlanExecuteMode(
                plan_config=agent_config.plan_config,
                model=agent_config.model,
                plugins=agent_config.plugins,
                workflows=agent_config.workflows,
                context_manager=context_manager,
                task_id=agent_config.task_id,
            )

        return ReactMode(
            plan_config=agent_config.plan_config,
            model=agent_config.model,
            plugins=agent_config.plugins,
            workflows=agent_config.workflows,
            context_manager=context_manager,
            task_id=agent_config.task_id,
        )
