#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Any

from jiuwen.controller.task_planner.planners.controller_planner import ControllerPlanner
from jiuwen.controller.task_planner.planners.plan_execute_planner import (
    PlanExecutePlanner,
)
from jiuwen.controller.task_planner.planners.react_planner import ReactPlanner
from jiuwen.controller.task_planner.planners.tool_use_planner import ToolUsePlanner
from jiuwen.controller.task_planner.task_planner import TaskPlanner


class PlannerFactory:
    """规划器工厂

    用于创建各种任务规划器的工厂。
    """

    _planner_map = {
        "ReAct": ReactPlanner,
        "Controller": ControllerPlanner,
        "ToolUse": ToolUsePlanner,
        "PlanExecute": PlanExecutePlanner,
    }

    @classmethod
    def create_planner(cls, config=None, context_manager: Any = None) -> TaskPlanner:
        """创建规划器

        Args:
            planner_type: 规划器类型，可选值：
                - "react": ReAct规划器
                - "controller": Controller规划器
                - "rule": 规则引擎规划器
            **kwargs: 传递给规划器构造函数的参数

        Returns:
            创建的规划器对象
        """
        mode = config.plan_config.plan_mode
        if mode not in cls._planner_map:
            raise ValueError(f"Unsupported planning mode: {mode}")
        if mode == "Controller":
            return ControllerPlanner(
                plan_config=config.plan_config,
                llm=config.model,
                plugins=config.plugins,
                workflows=config.workflows,
                context_manager=context_manager,
                task_id=config.task_id,
            )
        if mode == "ToolUse":
            return ToolUsePlanner(
                plan_config=config.plan_config,
                llm=config.model,
                plugins=config.plugins,
                workflows=config.workflows,
                context_manager=context_manager,
                task_id=config.task_id,
            )
        if mode == "PlanExecute":
            return PlanExecutePlanner(
                plan_config=config.plan_config,
                llm=config.model,
                plugins=config.plugins,
                workflows=config.workflows,
                context_manager=context_manager,
                task_id=config.task_id,
            )

        return ReactPlanner(
            plan_config=config.plan_config,
            llm=config.model,
            plugins=config.plugins,
            workflows=config.workflows,
            context_manager=context_manager,
            task_id=config.task_id,
        )
