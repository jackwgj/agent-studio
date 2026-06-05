# -*- coding: UTF-8 -*-
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2025. All rights reserved.
"""
Meta optimizer
"""

import re
from typing import List

from cacheout import Cache
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.prompt.common.config import LLMModelInfo
from jiuwen.prompt.tune.meta.meta_prompt_lib import MetaPromptLib

Context = Cache(maxsize=65536)


class Assembler:
    def __init__(self):
        self.template_fills = {
            "inference": ["PROMPT_TEMPLATE", "INSTRUCTION", "TOOLS"],
            "opt": [
                "HISTORICAL_META_PROMPTS",
                "REFERENCE_ELEMENTS",
                "BASELINE_META_PROMPT",
            ],
            "evaluation": ["USER_QUESTION", "ANSWER_A", "ANSWER_B"],
        }
        self.replacements = {}
        self.meta_templates = MetaPromptLib()

    def replacer(self, match):
        """
        replace function
        """
        return self.replacements.get(match.group(1), match.group(0))

    def filled_prompt(self, template_type: str, fills: List[str]):
        """
        fill inference, operation, evaluation template
        """
        if template_type in self.template_fills:
            self.replacements = {
                self.template_fills.get(template_type, [])[0]: fills[0],
                self.template_fills.get(template_type, [])[1]: fills[1],
                self.template_fills.get(template_type, [])[2]: fills[2],
            }
            return re.sub(
                r"\{\{(\w+)\}\}",
                self.replacer,
                self.meta_templates.function(template_type.upper()),
            )
        return ""


class LLMModel:
    def __init__(self, model_info: LLMModelInfo):
        """init"""
        if model_info is None:
            raise JiuWenBaseException(
                error_code=StatusCode.LLM_MODEL_SOURCE_ERROR.code,
                message=StatusCode.LLM_MODEL_SOURCE_ERROR.errmsg.format(
                    error_msg="the model info input is missing."
                ),
            )
        self.chat_llm = ModelFactory().get_model(
            model_type=model_info.model_source,
            model_name=model_info.model,
            temperature=model_info.temperature,
            top_p=model_info.top_p,
        )

    def chat(self, user_prompt, system_prompt=None, tools=None):
        """chat"""
        message_list = []
        if system_prompt is not None:
            message_list.append({"role": "system", "content": system_prompt})
        message_list.append({"role": "user", "content": user_prompt})
        response = self.chat_llm.invoke(
            LanguageModelInput(
                messages=ModelUtil.switch_message(messages=message_list), tools=tools
            )
        )
        return response["content"]
