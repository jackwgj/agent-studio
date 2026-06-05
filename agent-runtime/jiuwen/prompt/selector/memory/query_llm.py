#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from typing import Union, Dict, List

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.messages import BaseMessage, HumanMessage
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.prompt import Template, TemplateManager


def register_agent_builder_template(
    content: List[Dict],
    name: str = "llm_without_tool",
    model_name: str = "gy-agentBuilder-38b-v31-jiuwen",
    force: bool = False,
) -> bool:
    """Register a template in TemplateManager for agent-buildermodel to use"""
    template = Template(name=name, content=content, filters={"tag": model_name})
    return TemplateManager().register(template, force=force)


class LLMClient:
    """An LLM client class that supports agent-builderand siliconflow type LLMs, streaming option not implemented"""

    def __init__(self, model_type: str, model_name: str, **kwargs):
        model_factory = ModelFactory()
        self.model = model_factory.get_model(model_type, model_name, **kwargs)

    @staticmethod
    def prepare_query_args(
        messages: Union[str, List[Union[Dict, BaseMessage]]], **kwargs
    ) -> tuple[list, dict]:
        """Query LLM, support response_format in OpenAI/Siliconflow style"""
        if isinstance(messages, str):
            messages = HumanMessage(content=messages)
        elif isinstance(messages, list):
            if any(isinstance(msg, dict) for msg in messages):
                messages = ModelUtil.switch_message(messages)
        else:
            raise ValueError("Got unknown type for query messages.")
        if "__executor" in kwargs:
            del kwargs["__executor"]
        return messages, kwargs

    def query(
        self, messages: Union[str, List[Union[Dict, BaseMessage]]], *args, **kwargs
    ) -> BaseMessage:
        """Query LLM, support response_format in OpenAI/Siliconflow style"""
        messages, kwargs = self.prepare_query_args(messages, **kwargs)
        try:
            response = self.model.invoke(inputs=messages, **kwargs)
        except JiuWenBaseException as e:
            logger.info(f"LLM call failed: {e.message}")
            raise e
        return response

    async def async_query(
        self, messages: Union[str, List[Union[Dict, BaseMessage]]], *args, **kwargs
    ):
        """Query LLM asynchronously, support response_format in OpenAI/Siliconflow style"""
        messages, kwargs = self.prepare_query_args(messages, **kwargs)
        try:
            # If ainvoke is not implemented, it may return None
            response = await self.model.ainvoke(inputs=messages, **kwargs)
            if not response:
                response = self.model.invoke(inputs=messages, **kwargs)
        except JiuWenBaseException as e:
            logger.info(f"LLM call failed: {e.message}")
            raise e
        return response
