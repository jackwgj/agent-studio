#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
prompt unit
"""

import copy

from jiuwen.insight.manager import TraceManager
from jiuwen.insight.utils import get_instance_info
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.utils import Input, Output
from jiuwen.prompt.assemble.base import PromptAssembler
from jiuwen.prompt.assemble.message_handler import messages_to_template
from jiuwen.prompt.index.template_store import Template
from jiuwen.prompt.template.template_manager import TemplateManager


class Prompt(Invokable):
    """prompt engine chain unit interface

    Args:
        template (Template): Template class.

    """

    def __init__(self, template: Template):
        self.template = template

    @staticmethod
    def _concatenate_message(messages_list: list) -> str:
        """concatenate all message to one string context"""
        try:
            return "\n".join([f"{item['content']}" for item in messages_list])
        except KeyError as error:
            raise KeyError("``content`` key not in messages_list") from error

    @staticmethod
    def _merge_inputs(inputs: dict, kwargs: dict):
        """merge inputs

        Args:
            inputs : origin key words pair.
            kwargs: extra key words pair.

        Returns:
            merge kwargs into inputs.
        """
        for key, value in kwargs.items():
            if key not in inputs:
                inputs.update({key: value})

    @classmethod
    def get_prompt(cls, name):
        """get prompt

        Args:
            name (str): template name.

        Returns:
            Prompt (class Prompt): instance of Prompt
        """
        template = TemplateManager().get(name=name)
        if not template:
            raise ValueError("Template not found!")
        return cls(template=template)

    def invoke(self, inputs: Input, **kwargs) -> Output:
        ...

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        """Prompt invoke

        Args:
            inputs (dict): key-value pairs for assemble prompt.
            **kwargs (dict):

        Returns:
            Output (str): formatted prompt

        Raises:
            TypeError: check inputs type, inputs should be dict.
            ValueError: get inputs key error.
        """
        if not isinstance(inputs, dict):
            raise TypeError(f"inputs type error: {type(inputs)}, expected dict")
        # 后续template中有模型支持token数的时候，不需要指定
        self.template.model = dict(max_tokens_len=4096)
        cpy_template = copy.deepcopy(self.template)
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None), get_instance_info(self)
        )
        await trace_manager.on_prompt_start(inputs)
        if isinstance(cpy_template.content, list):
            cpy_template.content = messages_to_template(cpy_template.content)
        assembler = PromptAssembler(template=cpy_template)
        assemble_inputs = {k: v for k, v in inputs.items() if k in assembler.input_keys}
        formatted_prompt = assembler.assemble(**assemble_inputs)
        if isinstance(formatted_prompt, list):
            formatted_prompt = self._concatenate_message(formatted_prompt)
        await trace_manager.on_prompt_end(formatted_prompt)
        return formatted_prompt
