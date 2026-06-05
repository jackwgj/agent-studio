#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
Runnable
"""

import os
from abc import abstractmethod, ABC
from typing import Generic, TypeVar, Iterator, AsyncIterator, List

from jiuwen.common.llm_service.messages import BaseMessage, Tool
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.prompt.assemble.assembler import PromptAssemblerForEI

Input = TypeVar("Input", contravariant=True)
Output = TypeVar("Output", covariant=True)
REQUEST_TIME_OUT = 100
DEFAULT_LLM_CONTEXT_LENGTH = 4096


class Runnable(Generic[Input, Output], ABC):
    """Runnable class"""

    @abstractmethod
    def invoke(self, inputs: Input, **kwargs) -> Output:
        """invoke"""

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        """ainvoke"""

        pass

    def stream(self, inputs: Input, **kwargs) -> Iterator[Output]:
        """stream"""

        pass

    async def astream(self, inputs: Input, **kwargs) -> AsyncIterator[Output]:
        """astream"""

        pass


def refine_with_strategy(
    message_dicts: List[BaseMessage], tools: List[Tool], special_token: List
) -> str:
    """refine_with_strategy"""
    try:
        llm_context_length = int(
            os.getenv("llmContextLength", DEFAULT_LLM_CONTEXT_LENGTH)
        )
    except ValueError:
        llm_context_length = DEFAULT_LLM_CONTEXT_LENGTH

    template = []
    history = []
    for message in message_dicts:
        if message.get("role", "") == "system":
            template.append(message)
        else:
            history.append(message)

    assembler = PromptAssemblerForEI.from_config(
        template=template, refiner_config={"llmContextLength": llm_context_length}
    )
    refined_prompt, refined_tools = assembler.assemble(
        variables={}, history=history, tools=tools
    )
    prompt = ModelUtil.convert_message_to_prompt(
        messages_dict=refined_prompt,
        functions_dict=refined_tools,
        special_token=special_token,
    )

    return prompt
