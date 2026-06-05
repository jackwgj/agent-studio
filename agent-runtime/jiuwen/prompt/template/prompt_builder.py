#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
prompt builder interface
"""

import re
from typing import Union, List

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.prompt.common.config import LLMModelInfo
from jiuwen.prompt.common.exception.prompt_builder import (
    MetaTemplateNotSupportedException,
)
from jiuwen.prompt.index.template_store.template_store import Template
from jiuwen.prompt.template.base import MetaTemplate
from jiuwen.prompt.template.plan_meta import PlanMeta
from jiuwen.prompt.template.rag_meta import RagMeta
from jiuwen.prompt.template.template_editor import TemplateEditor
from pydantic import BaseModel, Field


class PromptBuilder(BaseModel):
    """prompt builder implement"""

    editor: Union[TemplateEditor, None] = Field(default=None)
    model_info: Union[LLMModelInfo, None] = Field(default=None)

    @staticmethod
    def _result_generator(result):
        """result generator handler"""
        idx = 0
        for element in result:
            if element.get("data"):
                idx += 1
                if idx < 5:
                    element["data"] = re.sub(
                        r"\n|```|markdown|mark|down", "", str(element["data"])
                    )
                else:
                    element["data"] = str(element["data"]).replace("```", "")
            yield element

    @classmethod
    def initialize(
        cls, model_info: LLMModelInfo = LLMModelInfo(), editor: TemplateEditor = None
    ):
        """initialize for prompt builder"""
        return cls(editor=editor, model_info=model_info)

    @classmethod
    def base_build(
        cls,
        name: str,
        content: Union[list, str],
        description: str = None,
        model: dict = None,
        user_id: str = "",
    ) -> Template:
        """base build, generating a simple Template.

        Args:
            name (str): string-type template name.
            content (list | str): Union type content.
            description (str, optional): string type description of template. Default ``None``.
            model (dict, optional): model information of template. Default ``None``.

                name (str): model name.
                version (str): model version.

            user_id (str, optional): user id of template. Default ``default_user``.

        Returns:
            Template: template object.
        """
        model = {} if model is None else model
        return Template(
            name=name,
            content=content,
            description=description,
            model=model,
            user_id=user_id,
        )

    def hmi_build(
        self, meta_template: str, instruction: str, tools: List[dict] = None, **kwargs
    ) -> Template:
        """hmi_build"""
        concrete_template = MetaTemplate.create(
            name=meta_template, stream=False, model_info=self.model_info
        ).build_prompt(instruction=instruction, tools=tools, **kwargs)
        if self.editor:
            concrete_template = self.editor.run(concrete_template)
        return self.base_build(name=meta_template, content=concrete_template)

    def hmi_streaming_build(
        self, meta_template: str, instruction: str, tools: List[dict] = None, **kwargs
    ):
        """hmi_streaming_build"""
        stream_char = MetaTemplate.create(
            name=meta_template, stream=True, model_info=self.model_info
        ).build_prompt(instruction=instruction, tools=tools, **kwargs)
        try:
            if self.editor:
                result = self.editor.stream_run(concrete_template=stream_char)
                return self._result_generator(result=result)
            return self._result_generator(result=stream_char)
        except Exception as _:
            code = StatusCode.PROMPT_TEMPLATE_EDITOR_ERROR.code
            msg = StatusCode.PROMPT_TEMPLATE_EDITOR_ERROR.errmsg
            logger.error(f"Hmi streaming build failed! code: {code}")
            return dict(code=code, message=msg, data="")

    def modular_stream_build(
        self, meta_template: str, instruction: str, tools: List[dict] = None, **kwargs
    ):
        """modular_stream_build"""
        modular_mapping = {
            "rag_meta_epo": RagMeta(model_info=self.model_info),
            "plan_meta_epo": PlanMeta(model_info=self.model_info),
        }
        if meta_template not in modular_mapping:
            raise MetaTemplateNotSupportedException(
                f"{meta_template} not in modular support list"
            )
        stream_char = modular_mapping.get(meta_template).build_prompt(
            instruction=instruction, tools=tools
        )
        if self.editor:
            result = self.editor.stream_run(concrete_template=stream_char)
            for char in result:
                yield char
        else:
            for char in stream_char:
                yield char
