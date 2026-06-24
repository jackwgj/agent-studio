#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any, AsyncGenerator, Optional, TYPE_CHECKING, Union

from openjiuwen.core.single_agent import BaseAgent as OpenjiuwenBaseAgent
from openjiuwen.core.single_agent import AgentCard

if TYPE_CHECKING:
    from jiuwen.controller.common.config import AgentConfig


class BaseAgent(OpenjiuwenBaseAgent, ABC):
    """Commercial Agent facade base class aligned to openJiuwen BaseAgent."""

    def __init__(self, config: Optional["AgentConfig"] = None):
        self.agent_config = config
        self._config = config
        super().__init__(card=self._build_agent_card(config))

    @staticmethod
    def _build_agent_card(config: Optional["AgentConfig"]) -> AgentCard:
        agent_id = getattr(config, "agent_id", None) or getattr(
            config, "task_id", None
        ) or "jiuwen_agent"
        return AgentCard(
            id=agent_id,
            name=agent_id,
            description="Jiuwen commercial agent facade",
        )

    def configure(self, config) -> "BaseAgent":
        self.agent_config = config
        self._config = config
        return self

    @property
    def config(self):
        return self.agent_config

    @config.setter
    def config(self, value) -> None:
        self.agent_config = value
        self._config = value

    @abstractmethod
    async def invoke(
        self, inputs: Union[dict, str], session: Optional[Any] = None, **kwargs
    ) -> Any:
        """执行一次任务并返回最终结果。"""
        pass

    @abstractmethod
    async def stream(
        self, inputs: Union[dict, str], session: Optional[Any] = None, **kwargs
    ) -> AsyncGenerator[Any, None]:
        """对齐 openJiuwen BaseAgent 的流式接口。"""
        pass

    async def astream(
        self, inputs: Union[dict, str], **kwargs
    ) -> AsyncGenerator[Any, None]:
        """兼容 Jiuwen 历史异步流式接口。"""
        async for item in self.stream(inputs, **kwargs):
            yield item
