# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

import json
from typing import List, Dict, Any, Iterator, AsyncIterator, TypedDict

from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import (
    BaseChatModel as JiuwenBaseChatModel,
)
from jiuwen.common.llm_service.messages import (
    AIMessage as JiuwenAIMessage,
    ToolCall as JiuwenToolCall,
    Tool as JiuwenTool,
)
from jiuwen.common.llm_service.model_util import ModelUtil
from pydantic import BaseModel

_ai_message_mapping = {}


class AIMessageExtendFields(TypedDict, total=False):
    reason_content: str


def register_ai_message_mapping(**kwargs: AIMessageExtendFields):
    """支持扩展消息映射"""
    _ai_message_mapping.update(kwargs)


def get_open_jiuwen_proxy_instance(
    model_type: str,
    model_name: str,
    temperature: float,
    top_p: float,
    api_base: str = "",
    max_retries: int = 3,
    timeout: int = 60,
):
    """获取 jiuwenmind 模型代理实例"""
    from jiuwenmind.core.utils.llm.base import BaseChatModel
    from jiuwenmind.core.utils.llm.messages import (
        AIMessage,
        UsageMetadata,
        FunctionInfo,
        ToolCall,
    )
    from jiuwenmind.core.utils.llm.messages_chunk import AIMessageChunk

    class OpenJiuwenLLMProxy(BaseModel, BaseChatModel):
        """
        适配 jiuwenmind 规范的模型代理
        将 jiuwenmind 的模型实现替换为 jiuwen 模型实现，并将返回数据进行类型转换，符合 jiuwenmind 数据结构
        """

        _model_type: str = ""
        _model: JiuwenBaseChatModel = None

        def __init__(
            self,
            model_type: str,
            model_name: str,
            temperature: float,
            top_p: float,
            api_base: str = "",
            max_retries: int = 3,
            timeout: int = 60,
        ):
            super().__init__(
                api_key="", api_base=api_base, max_retrie=max_retries, timeout=timeout
            )
            self._model_type = model_type
            # 获取 jiuwen 模型网关
            self._model = ModelFactory().get_model(
                model_type, model_name, temperature, top_p
            )

        def model_provider(self):
            """模型提供商"""
            return self._model_type

        def to_jiuwen_tool(self, tools: List[Dict]):
            """转为 jiuwen 标准 Tool"""
            if not tools:
                return []

            jiuwen_tools = []
            for tool in tools:
                function = tool.get("function")
                jiuwen_tools.append(
                    JiuwenTool(
                        name=function.get("name"),
                        description=function.get("description"),
                        parameters=function.get("parameters"),
                    )
                )
            return jiuwen_tools

        def to_jiuwenmind_message_chunk(self, chunk: JiuwenAIMessage) -> AIMessageChunk:
            """转为 jiuwenmind 标准 AIMessageChunk"""
            if not chunk:
                return AIMessageChunk(raw_content="LLM response is empty.")

            # ToolCall
            tool_calls = None
            if isinstance(chunk.tool_calls, JiuwenToolCall):
                # 单个 ToolCall 转列表
                tool_calls = [chunk.tool_calls]

            if isinstance(chunk.tool_calls, list):
                tool_calls = []
                for tool_call in chunk.tool_calls:
                    if not isinstance(tool_call, JiuwenToolCall):
                        continue
                    arguments = json.dumps(tool_call.args, ensure_ascii=False)

                    # 创建 jiuwenmind ToolCall, FunctionInfo 内容为 jiuwen ToolCall 携带的 name 和 args 信息
                    tool_calls.append(
                        ToolCall(
                            type="function",
                            function=FunctionInfo(
                                name=tool_call.name, arguments=arguments
                            ),
                            **tool_call.model_dump(),
                        )
                    )

            # 转换 UsageMetadata
            usage_metadata = None
            if chunk.usage_metadata:
                usage_metadata = UsageMetadata(
                    request_start_time=chunk.usage_metadata.requests_start_time,
                    **chunk.usage_metadata.model_dump(),
                )
                if usage_metadata.finish_reason in ["stop", "function_call"]:
                    # 最后一条消息为汇总消息，影响 jiuwen_deepsearch 的 json 解析，清空
                    chunk.content = ""
                    chunk.reasoning_content = ""

            # 补充其他消息字段
            other_message = {}
            if _ai_message_mapping:
                for target, source in _ai_message_mapping.items():
                    if source and hasattr(chunk, source):
                        other_message[target] = getattr(chunk, source)

            return AIMessageChunk(
                role=chunk.type,
                content=chunk.content,
                name=chunk.name,
                tool_calls=tool_calls,
                usage_metadata=usage_metadata,
                raw_content=chunk.raw_content,
                **other_message,
            )

        def _invoke(
            self,
            model_name: str,
            messages: List[Dict],
            tools: List[Dict] = None,
            temperature: float = 0.1,
            top_p: float = 0.1,
            **kwargs: Any,
        ) -> AIMessage:
            # 1. message 转换 Dict -> jiuwen BaseMessage
            messages = ModelUtil.switch_message(messages=messages)

            # 2. tool 转换 Dict -> jiuwen Tool
            tools = self.to_jiuwen_tool(tools=tools)

            # 3. AIMessage 转换 jiuwen AIMessage -> jiuwenmind AIMessage
            jiuwenmind_ai_messages = self.to_jiuwenmind_message_chunk(
                self._model._chat(messages=messages, tools=tools, **kwargs)
            )
            return jiuwenmind_ai_messages

        async def _ainvoke(
            self,
            model_name: str,
            messages: List[Dict],
            tools: List[Dict] = None,
            temperature: float = 0.1,
            top_p: float = 0.1,
            **kwargs: Any,
        ) -> AIMessage:
            messages = ModelUtil.switch_message(messages=messages)
            tools = self.to_jiuwen_tool(tools=tools)
            jiuwenmind_ai_messages = self.to_jiuwenmind_message_chunk(
                await self._model._achat(messages=messages, tools=tools, **kwargs)
            )
            return jiuwenmind_ai_messages

        def _stream(
            self,
            model_name: str,
            messages: List[Dict],
            tools: List[Dict] = None,
            temperature: float = 0.1,
            top_p: float = 0.1,
            **kwargs: Any,
        ) -> Iterator[AIMessageChunk]:
            messages = ModelUtil.switch_message(messages=messages)
            tools = self.to_jiuwen_tool(tools=tools)
            for chunk in self._model._stream(messages=messages, tools=tools, **kwargs):
                yield self.to_jiuwenmind_message_chunk(chunk)

        async def _astream(
            self,
            model_name: str,
            messages: List[Dict],
            tools: List[Dict] = None,
            temperature: float = 0.1,
            top_p: float = 0.1,
            **kwargs: Any,
        ) -> AsyncIterator[AIMessageChunk]:
            messages = ModelUtil.switch_message(messages=messages)
            tools = self.to_jiuwen_tool(tools=tools)
            async for chunk in self._model._astream(
                messages=messages, tools=tools, **kwargs
            ):
                yield self.to_jiuwenmind_message_chunk(chunk)

    model = OpenJiuwenLLMProxy(
        model_name=model_name,
        model_type=model_type,
        temperature=temperature,
        top_p=top_p,
        api_base=api_base,
        max_retries=max_retries,
        timeout=timeout,
    )
    return model


def get_llm_proxy():
    """获取 jiuwen 模型"""
    from jiuwen_deepsearch.config.config import AgentConfig, Config

    def create_llm_proxy(agent_config: AgentConfig):
        """创建适配 jiuwenmind 的 llm 模型网关代理"""
        try:
            llm_config = agent_config.llm_config
            if llm_config.model_type == "agentBuilder":
                from jiuwen_deepsearch.framework.jiuwen.llm.llm_model_factory import (
                    LLMModelFactory,
                )

                llm_config = agent_config.llm_config
                auth = bytes(llm_config.api_key).decode("utf-8")
                model = LLMModelFactory().get_model(
                    model_provider=llm_config.model_type,
                    api_key=auth,
                    api_base=llm_config.base_url,
                    timeout=Config().service_config.llm_timeout,
                )

                def build_agent_builder_headers():
                    return {"Content-Type": "application/json", "X-Auth-Token": auth}

                if hasattr(model, "_agentBuilder_model"):
                    agent_builder_model = model._agentBuilder_model  # pylint: disable=protected-access
                    setattr(
                        agent_builder_model,
                        "_build_headers",
                        build_agent_builder_headers,
                    )

            else:
                model = get_open_jiuwen_proxy_instance(
                    model_name=llm_config.model_name,
                    model_type=llm_config.model_type,
                    temperature=llm_config.hyper_parameters.get("temperature", 0.1),
                    top_p=llm_config.hyper_parameters.get("top_p", 0.1),
                    api_base=llm_config.base_url,
                    timeout=Config().service_config.llm_timeout,
                )
            model_name = llm_config.model_name
            return dict(model=model, model_name=model_name)
        finally:
            from jiuwen_deepsearch.framework.jiuwen.utils.common_utils import (
                secure_clear,
            )

            secure_clear(agent_config.llm_config.api_key)

    return create_llm_proxy


def init_adapter():
    """初始化 jiuwen adapter"""
    from jiuwen_deepsearch.llm import llm_wrapper

    llm_wrapper.create_llm_obj = get_llm_proxy()
