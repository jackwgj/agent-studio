#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""BaseControlAgent控制器智能体"""

from abc import ABC, abstractmethod
from typing import Union, AsyncGenerator, Any, Set

from jiuwen.multi_agent.agent_group.config import AgentGroupConfig


class BaseControlAgent(ABC):
    """控制Agent基类"""

    def __init__(self, agents: Set[str], config: AgentGroupConfig, runner: Any):
        self.agents = agents
        self.config = config
        self.runner = runner
        self.current_agent_calls = 0
        self.task_end = False

    @abstractmethod
    async def execute(self, inputs: Union[dict, str], **kwargs) -> AsyncGenerator:
        """执行任务 - 子类实现具体流程"""
        pass

    @abstractmethod
    async def select_agent(self, inputs: Union[dict, str]) -> str:
        """选择Agent - 子类实现"""
        pass
