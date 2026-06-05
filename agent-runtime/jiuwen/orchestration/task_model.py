#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""This module contains Task input types."""

from typing import Any

from jiuwen.context.base import ContextConfig
from jiuwen.context.memory_engine.config.memory_ir_config import MemoryIrConfig
from jiuwen.planner import PlanStrategyType
from pydantic import BaseModel


class TaskConfig(BaseModel):
    sys_prompt_template: str
    mode: PlanStrategyType
    max_iteration: int
    plan_config: Any
    context: ContextConfig
    memory_config: MemoryIrConfig


class TaskModel(BaseModel):
    agent_id: str
    agent_version: str
    agent_name: str
    description: str
    configs: TaskConfig


class TaskInputs(BaseModel):
    user_input: str
    prompt_variables: dict
