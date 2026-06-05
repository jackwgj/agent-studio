#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""BaseAgentGroup基类"""

from abc import ABC, abstractmethod
from typing import Any, Optional, Union, AsyncGenerator

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.multi_agent.agent_group.config import AgentGroupConfig
from jiuwen.multi_agent.agent_group.group_state import AgentGroupState
from jiuwen.multi_agent.core.runner.standalone_runner import StandaloneRunner


class BaseAgentGroup(ABC):
    """智能体组基类"""

    def __init__(self, config: AgentGroupConfig):
        self.config = config
        self.control_agent: Optional[Any] = None
        self.agents: set[str] = set()
        self.runner: Optional[StandaloneRunner] = None
        self._group_state: Optional[AgentGroupState] = None
        self._running = False

    async def start(self, state: AgentGroupState) -> None:
        """启动智能体组"""
        pass

    async def stop(self) -> None:
        """停止智能体组"""
        pass

    async def astream(self, inputs: Union[dict, str], **kwargs) -> AsyncGenerator:
        """
        执行AgentGroup的流式处理 - 核心任务循环

        Args:
            inputs: 用户输入指令
            **kwargs: 额外参数

        Yields:
            处理结果的流式输出
        """
        if not self._running or not self.control_agent:
            raise JiuWenBaseException(
                error_code=StatusCode.MULTI_AGENT_GROUP_NOT_INITIALIZED.code,
                message=StatusCode.MULTI_AGENT_GROUP_NOT_INITIALIZED.errmsg,
            )

        try:
            async for result in self.control_agent.execute(inputs, **kwargs):
                yield result

        except Exception as e:
            logger.error(
                f"Error in AgentGroup execution: {e}",
                simple_log="Error in AgentGroup execution",
            )
            raise JiuWenBaseException(
                error_code=StatusCode.MULTI_AGENT_GROUP_EXECUTE_ERROR.code,
                message=StatusCode.MULTI_AGENT_GROUP_EXECUTE_ERROR.errmsg.format(
                    reason=type(e)
                ),
            ) from e

    @abstractmethod
    async def get_state(self) -> AgentGroupState:
        """获取Agent Group状态"""
        pass

    @abstractmethod
    async def load_state(self, state: AgentGroupState) -> None:
        """加载Agent Group状态"""
        pass

    @abstractmethod
    def clear_state(self) -> None:
        """清除实例状态"""
        pass

    @abstractmethod
    def create_control_agent(self):
        """创建控制Agent - 子类必须实现"""
        pass

    async def _register_agents(self):
        """初始化所有Agent"""
        try:
            logger.info(f"Registered {len(self.agents)} agents")
        except Exception as e:
            logger.error(
                f"Failed to initialize agents: {e}",
                simple_log="Failed to initialize agents",
            )
            raise
