#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import AsyncIterator, Iterator, Dict, Any, List


@dataclass
class AgentStrategyContext:
    """Agent策略执行上下文 - 为策略提供运行时环境"""

    # 静态配置（从AgentNodeConfig复制而来）
    model_config: Dict[str, Any]
    plugins: List[Dict[str, Any]]
    system_prompt: str
    max_iteration: int
    with_chat_history: bool
    chat_history_max_turn: int

    # 运行时数据
    user_inputs: Dict[str, Any]  # 当前输入
    chat_history: List[Dict[str, Any]] = field(default_factory=list)  # 对话历史
    execution_id: str = ""  # 执行ID


@dataclass
class AgentStrategyResult:
    """Agent策略执行结果"""

    output: str


class AgentStrategy(ABC):
    """Agent策略基础接口"""

    @abstractmethod
    def get_strategy_name(self) -> str:
        """获取策略名称"""
        pass

    @abstractmethod
    def invoke(self, context: AgentStrategyContext) -> AgentStrategyResult:
        """同步执行Agent策略"""
        pass

    @abstractmethod
    async def ainvoke(self, context: AgentStrategyContext) -> AgentStrategyResult:
        """异步执行Agent策略"""
        pass

    @abstractmethod
    def stream(self, context: AgentStrategyContext) -> Iterator[str]:
        """流式执行Agent策略"""
        pass

    @abstractmethod
    async def astream(self, context: AgentStrategyContext) -> AsyncIterator[str]:
        """异步流式执行Agent策略"""
        pass
