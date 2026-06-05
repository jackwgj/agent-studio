# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

"""
RestfulApiWrapper -- RESTful API 包装器（浅封装）。

从 Runner.resource_mgr.get_tool() 获取已注册的 RestfulApiToolNew，
适配旧版 RestFulAPI 的接口签名。

核心特点：
1. 独立类，不继承任何基类
2. 通过 tool_id 从 Runner.resource_mgr.get_tool() 获取 RestfulApiToolNew
3. 仅做接口适配，保持与旧版 RestFulAPI 相同的方法签名
4. 所有属性访问委托给内部的 RestfulApiToolNew
"""

from typing import AsyncIterator, Dict, Iterator, List, Optional

from jiuwen.common.log.base import logger
from jiuwen.orchestration.utils import Input, Output
from jiuwen.plugin.models.param import Param


class RestfulApiWrapper:
    """
    RESTful API 包装器（浅封装）

    从 Runner.resource_mgr.get_tool() 获取已注册的 RestfulApiToolNew，
    适配旧版 RestFulAPI 的接口签名
    """

    IS_FROM_IR_KEY = "is_from_ir"

    def __init__(self, tool_id: str, agent_id: str, session_id: str):
        """
        初始化包装器

        Args:
            tool_id: 已注册到 ResourceMgr 的工具 ID
        """
        self._tool_id = tool_id
        self._agent_id = agent_id
        self._session_id = session_id
        self._tool = None

    def _get_tool(self):
        """延迟获取 Tool 实例"""
        if self._tool is None:
            from openjiuwen.core.runner import Runner
            from openjiuwen.core.session.agent import create_agent_session

            session = create_agent_session(session_id=self._session_id)
            self._tool = Runner.resource_mgr.get_tool(
                self._tool_id, tag=self._agent_id, session=session
            )
            if self._tool is None:
                raise ValueError(f"Tool not found: {self._tool_id}")
        return self._tool

    @property
    def name(self) -> str:
        return self._get_tool().name

    @property
    def description(self) -> str:
        return self._get_tool().description

    @property
    def params(self) -> List[Param]:
        """输入参数列表"""
        return self._get_tool().params

    @property
    def path(self) -> str:
        return self._get_tool().path

    @property
    def headers(self) -> dict:
        return self._get_tool().headers

    @property
    def method(self) -> str:
        return self._get_tool().method

    @property
    def response(self) -> List[Param]:
        """输出参数列表"""
        return self._get_tool().response

    @property
    def plugin_name(self) -> str:
        return self._get_tool().plugin_name

    @property
    def plugin_desc(self) -> str:
        return self._get_tool().plugin_desc

    @property
    def auth(self) -> dict:
        return self._get_tool().auth

    @property
    def label(self) -> str:
        return self._get_tool().label

    @property
    def related_info(self) -> Optional[dict]:
        return self._get_tool().related_info

    @property
    def status(self) -> Optional[int]:
        return self._get_tool().status

    @property
    def principle(self) -> str:
        return self._get_tool().principle

    @property
    def query_params(self) -> dict:
        return self._get_tool().query_params

    @property
    def user_id(self) -> str:
        return self._get_tool().user_id

    @property
    def plugin_dependency(self) -> dict:
        return self._get_tool().plugin_dependency

    @property
    def id(self) -> str:
        return self._get_tool().id

    @property
    def async_conf(self) -> dict:
        return self._get_tool().async_conf

    @property
    def async_switch(self) -> bool:
        return self._get_tool().async_switch

    @property
    def is_from_ir(self) -> bool:
        return self._get_tool().is_from_ir

    @property
    def input_parameters(self) -> Dict[str, str]:
        return self._get_tool().input_parameters

    @property
    def plugin_reade_buffer_size(self) -> int:
        return self._get_tool().plugin_reade_buffer_size

    def invoke(self, inputs: Input, **kwargs) -> Output:
        """同步调用（不支持）"""
        logger.error("restful api not support invoke, please use ainvoke instead")

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        """
        异步调用主入口

        保持与旧版 RestFulAPI.ainvoke() 完全相同的行为
        直接委托给 RestfulApiToolNew.ainvoke()
        """
        return await self._get_tool().ainvoke(inputs, **kwargs)

    def stream(self, inputs: Input, **kwargs) -> Iterator[Output]:
        """流式输出（不支持）"""
        logger.error("restful api not support stream, please use astream instead")

    async def astream(self, inputs: Input, **kwargs) -> AsyncIterator[Output]:
        """
        异步流式输出

        保持与旧版 RestFulAPI.astream() 完全相同的行为
        直接委托给 RestfulApiToolNew.astream()
        """
        async for chunk in self._get_tool().astream(inputs, **kwargs):
            yield chunk
