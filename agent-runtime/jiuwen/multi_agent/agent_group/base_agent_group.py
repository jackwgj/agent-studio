#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""BaseAgentGroup基类"""

from abc import ABC, abstractmethod
from typing import Any, Optional, Union, AsyncGenerator

from openjiuwen.core.multi_agent import TeamConfig, TeamCard, BaseTeam

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.multi_agent.agent_group.config import AgentGroupConfig, GroupSettings
from jiuwen.multi_agent.agent_group.group_state import AgentGroupState
from jiuwen.multi_agent.core.runner.standalone_runner import StandaloneRunner


class BaseAgentGroup(BaseTeam, ABC):
    """智能体组基类"""

    def __init__(
        self, config: AgentGroupConfig, *, team_config: Optional[TeamConfig] = None
    ):
        self.group_config = config
        resolved_team_config = team_config or self._build_team_config(config)
        BaseTeam.__init__(
            self,
            card=self._build_team_card(config),
            config=resolved_team_config,
        )
        self.team_config = resolved_team_config
        self.config = config
        self.control_agent: Optional[Any] = None
        self.agents: set[str] = set()
        self.runner: Optional[StandaloneRunner] = None
        self._group_state: Optional[AgentGroupState] = None
        self._running = False
        self.runtime = None

    @staticmethod
    def _build_team_card(config: AgentGroupConfig) -> TeamCard:
        """构建 OpenJiuWen TeamCard，仅用于暴露 team 继承关系。"""
        return TeamCard(
            id=config.group_id,
            name=config.group_name,
            description=config.description or "",
        )

    @staticmethod
    def _build_team_config(config: AgentGroupConfig) -> TeamConfig:
        """构建 OpenJiuWen TeamConfig，不改变既有 AgentGroup 运行时语义。"""
        group_settings = config.group_settings or GroupSettings()
        max_agents = 1 + len(config.agents or [])
        return TeamConfig(
            max_agents=max_agents,
            max_concurrent_messages=group_settings.max_concurrent_agents,
            message_timeout=float(group_settings.timeout),
        )

    def configure(self, config):
        """兼容 BaseTeam 配置接口，同时保留 AgentGroupConfig 语义。"""
        if isinstance(config, TeamConfig):
            self.team_config = config
            return self
        self.group_config = config
        self.config = config
        return self

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

    async def invoke(self, message, session=None) -> Any:
        """对齐 OpenJiuWen BaseTeam 接口；保持当前 AgentGroup blocking 语义不变。"""
        raise JiuWenBaseException(
            error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
            message="Blocking invoke is not supported yet.",
        )

    async def stream(self, message, session=None) -> AsyncGenerator:
        """对齐 OpenJiuWen BaseTeam 接口，透传到既有 astream。"""
        stream_kwargs = {}
        if session is not None:
            stream_kwargs["session"] = session
        async for item in self.astream(message, **stream_kwargs):
            yield item

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
