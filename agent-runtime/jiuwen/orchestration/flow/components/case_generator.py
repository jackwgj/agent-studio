# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
case generator
"""

import re
from typing import List

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.prompt import TemplateManager, PromptAssembler
from jiuwen.prompt.common.exception.base import JiuWenBaseException


class CaseGenerator:
    def __init__(self, model_info):
        """init"""
        self.model = ModelFactory().get_model(
            model_type=model_info.modelType,
            model_name=model_info.modelName,
            temperature=model_info.hyperParameters.temperature,
            top_p=model_info.hyperParameters.top_p,
        )
        self.case_generator_template = TemplateManager().get(
            name="case_generator", filters={"tag": model_info.modelName}
        )

    def __call__(self, *args, **kwargs) -> List[str]:
        """call"""
        return self._run(*args, **kwargs)

    @staticmethod
    def parse_tagged_text(text: str, start_tag: str, end_tag: str) -> List[str]:
        """Parse text that is tagged with start and end tags, returning all matches."""
        # 使用 findall 来获取所有匹配的值
        first = re.escape(end_tag[0])
        rest = re.escape(end_tag[1:]) if len(end_tag) > 1 else ""
        pattern = f"{re.escape(start_tag)}([^{first}]*(?:{first}(?!{rest})[^{first}]*)*){re.escape(end_tag)}"
        matches = re.findall(pattern, text, re.DOTALL)
        return [match.strip() for match in matches]

    @staticmethod
    def llm_inference(model, prompt) -> str:
        """llm_inference"""
        messages = [{"role": "user", "content": prompt}]
        language_model_input = LanguageModelInput(
            messages=ModelUtil.switch_message(messages=messages), tools=None
        )

        try:
            result = model.invoke(language_model_input)
        except Exception as e:
            raise JiuWenBaseException(
                error_code=StatusCode.LLM_FALSE_RESULT_ERROR.code,
                message=StatusCode.LLM_FALSE_RESULT_ERROR.errmsg.format(
                    error_msg="LLM not return result"
                ),
            ) from e
        return result.content

    def _sub_run(self, user_prompt, branches, generation_num) -> List[str]:
        """_sub_run"""
        prompt = PromptAssembler(self.case_generator_template).assemble(
            prompt=user_prompt, branches=branches, generation_num=generation_num
        )
        response = self.llm_inference(self.model, prompt)
        result = self.parse_tagged_text(response, "<START>", "<END>")
        deduplicated_result = list(dict.fromkeys(result))
        return deduplicated_result

    def _run(self, user_prompt, branches, generation_num) -> List[str]:
        """_run"""
        # first try
        first_try_result = self._sub_run(user_prompt, branches, generation_num)

        # second try
        if len(first_try_result) < generation_num:
            second_try_result = self._sub_run(user_prompt, branches, generation_num)
            result = list(dict.fromkeys(first_try_result + second_try_result))
            if len(result) <= 0:
                raise JiuWenBaseException(
                    error_code=StatusCode.LLM_FALSE_RESULT_ERROR.code,
                    message=StatusCode.LLM_FALSE_RESULT_ERROR.errmsg.format(
                        error_msg="LLM Cannot generate examples within both two attempts"
                    ),
                )
            return result[:generation_num]
        return first_try_result[:generation_num]
