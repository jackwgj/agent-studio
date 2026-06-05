#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
meta template base
"""

from abc import abstractmethod
from typing import List, Union

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.prompt.assemble.base import PromptAssembler
from jiuwen.prompt.common.config import LLMModelInfo
from jiuwen.prompt.common.exception.prompt_builder import (
    MetaTemplateNotExistException,
    MetaTemplateBuildFailedException,
    MetaTemplateToolParseException,
    MetaTemplateMissingKeysException,
)
from jiuwen.prompt.index.template_store.template_store import Template
from jiuwen.prompt.template.llm_service import LLMServiceManager
from jiuwen.prompt.template.template_manager import InnerTemplate
from pydantic import BaseModel, Field


class MetaTemplate(BaseModel):
    """
    meta template base
    """

    name: str = Field(default="")
    stream: bool = Field(default=False)
    model_info: Union[LLMModelInfo, None] = Field(default=None)

    @classmethod
    def create(cls, name: str, stream: bool = False, model_info: LLMModelInfo = None):
        """create meta template"""
        return StandardMeta(name=name, stream=stream, model_info=model_info)

    def get_template_content(
        self, instruction: str = "", tools: List[dict] = None, **kwargs
    ):
        """get template content"""
        template = InnerTemplate().get(name=self.name)
        if not template:
            if self.stream:
                return dict(
                    code=StatusCode.PROMPT_META_TEMPLATE_NOT_EXIST_ERROR.code,
                    message=StatusCode.PROMPT_META_TEMPLATE_NOT_EXIST_ERROR.errmsg,
                    data="",
                )
            raise MetaTemplateNotExistException(
                f"{self.name} not found in source database"
            )
        # assemble meta template
        try:
            content = self._assemble_input_value(
                template=template, instruction=instruction, tools=tools, **kwargs
            )
        except JiuWenBaseException as error:
            if self.stream:
                return dict(code=error.error_code, message=error.message, data="")
            raise error
        return content

    def build_prompt(self, instruction: str = "", tools: List[dict] = None, **kwargs):
        """build prompt interface"""
        content = self.get_template_content(
            instruction=instruction, tools=tools, **kwargs
        )
        if self.stream:
            return self._streaming_build(content=content)
        concrete_template = self._full_build(content=content)
        if not concrete_template:
            raise MetaTemplateBuildFailedException(
                "Concrete template build failed, content is null!"
            )
        return concrete_template

    @abstractmethod
    def _assemble_input_value(
        self,
        template: Template,
        instruction: str = "",
        tools: List[dict] = None,
        **kwargs,
    ):
        raise NotImplementedError("Not Implemented")

    @abstractmethod
    def _streaming_build(self, content: str) -> str:
        raise NotImplementedError("Not Implemented")

    @abstractmethod
    def _full_build(self, content: str) -> str:
        raise NotImplementedError("Not Implemented")


class ModularMetaTemplate:
    """ModularMetaTemplate"""

    def __init__(self, model_info: LLMModelInfo):
        self.success_msg = "success"
        self.model_info = model_info

    @abstractmethod
    def build_prompt(self):
        """build prompt"""


class StandardMeta(MetaTemplate):
    """standard meta implement"""

    @staticmethod
    def _parse_tool_list(tools: List[dict] = None):
        """_parse_tool_list"""
        tool_description = ""
        for _, tool in enumerate(tools, start=1):
            try:
                tool_description += f"{tool.get('name')}，{tool.get('description')}\n"
            except Exception as e:
                raise MetaTemplateToolParseException(
                    "tools structure type error"
                ) from e
        return tool_description

    def _assemble_input_value(
        self,
        template: Template,
        instruction: str = "",
        tools: List[dict] = None,
        **kwargs,
    ):
        if tools:
            kwargs["tools"] = self._parse_tool_list(tools=tools)
        if instruction:
            kwargs["instruction"] = instruction
        # 获取当前模板的占位符
        variables = PromptAssembler(template).variables
        missing_keys = set(variables.keys()) - set(kwargs.keys())
        if missing_keys:
            raise MetaTemplateMissingKeysException(
                f"Missing keys for updating the meta template: {list(missing_keys)}"
            )
        # 取出对应的值, 外部可以多传，但在组装时不会
        assemble_value = {}
        for variable in variables:
            assemble_value[variable] = kwargs.get(variable)
        content = PromptAssembler(template).assemble(**assemble_value)
        return content

    def _streaming_build(self, content: str) -> str:
        messages = [{"role": "user", "content": content}]
        return LLMServiceManager.get_llm_backend().chat(
            messages, method="stream", model_info=self.model_info
        )

    def _full_build(self, content: str) -> str:
        messages = [{"role": "user", "content": content}]
        result = LLMServiceManager.get_llm_backend().chat(
            messages, model_info=self.model_info
        )
        if "latency" in result:
            result.pop("latency")
        return result.get("content", "content not exit")
