# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
agentBuilder chain Template evaluator
"""

import copy
import json
import queue
import random
import re
import threading
from typing import List, Tuple, Dict

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.prompt import Template, PromptAssembler
from jiuwen.prompt.common.config import LLMModelInfo
from jiuwen.prompt.common.exception.base import JiuWenBaseException
from jiuwen.prompt.template.template_manager import InnerTemplate
from jiuwen.prompt.tune.template.utils import (
    Case,
    OptimizeInfo,
    CompareRes,
    LLMModelProcess,
    check_not_none_with_alias,
)

CONTENT_STRING = "content"


class TemplateEvaluatorWithRef:
    """Template evaluator,evaluate the responses of the two assistants
    as good or bad based on the reference answers.
    """

    def __init__(
        self,
        raw_templates: List[str],
        model_info: LLMModelInfo,
        assistant_info: LLMModelInfo,
    ):
        self.raw_templates = raw_templates
        self.model_info = model_info
        self.assistant_info = assistant_info
        self.model = LLMModelProcess(model_info)
        self.assistant_model = LLMModelProcess(assistant_info)

    @staticmethod
    def case_augment(case: Case) -> Case:
        """Perform data augmentation and placeholder extraction"""
        # The value of case.messages is not None when a case is defined.
        check_not_none_with_alias(case, "case")
        case_aug = copy.deepcopy(case)

        # Randomly intercept the message list for data augmentation
        assistant_index = [
            inx
            for inx, msg in enumerate(case_aug.messages)
            if msg["role"] == "assistant"
        ]
        if not assistant_index:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.code,
                message=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.errmsg.format(
                    error_msg="messages in case does not have assistant role"
                ),
            )
        case_aug.messages = case_aug.messages[: random.choice(assistant_index) + 1]
        return case_aug

    def extract_placeholder(self, case: Case) -> Tuple[Dict, int]:
        """Extract placeholder values by compare with raw_templates"""
        # The value of case.messages is not None when a case is defined.
        check_not_none_with_alias(case, "case")
        placeholders = None
        template_index = None
        for raw_template in self.raw_templates:
            raw_template_split = re.split(r"\{\{[^{}]*\}\}", raw_template)
            if len(raw_template_split) > 1:
                raw_template_split = [s for s in raw_template_split if s != ""]
                placeholder_key = re.split(
                    "|".join(map(re.escape, raw_template_split)), raw_template
                )
                for inx, msg in enumerate(case.messages):
                    placeholder_value = re.split(
                        "|".join(map(re.escape, raw_template_split)), msg["content"]
                    )
                    if len(placeholder_value) == len(placeholder_key):
                        placeholders = {
                            re.sub(r"\{|\}", "", key): val
                            for key, val in zip(placeholder_key, placeholder_value)
                            if key != ""
                        }
                        template_index = inx
                        break
            else:
                for inx, msg in enumerate(case.messages):
                    if raw_template in msg["content"]:
                        placeholders = {}
                        template_index = inx
                        break
            if placeholders is not None and template_index is not None:
                break

        if placeholders is None:
            raise JiuWenBaseException(
                StatusCode.PROMPT_TEMPLATE_CASE_PLACEHOLDER_ERROR.code,
                StatusCode.PROMPT_TEMPLATE_CASE_PLACEHOLDER_ERROR.errmsg,
            )
        if template_index is None:
            raise JiuWenBaseException(
                StatusCode.PROMPT_TEMPLATE_CASE_CONTENT_ERROR.code,
                StatusCode.PROMPT_TEMPLATE_CASE_CONTENT_ERROR.errmsg,
            )
        return placeholders, template_index

    def get_answer(self, template: str, case: Case, optimize_info: OptimizeInfo) -> str:
        """Obtain the response of LLM under different cases for each template"""
        # The value of case.messages is not None when a case is defined.
        check_not_none_with_alias(case, "case")
        # Replace the newly generated template into the original messages
        placeholders, template_index = self.extract_placeholder(case)
        assembler = PromptAssembler(Template(name="", content=template))
        template = assembler.assemble(**placeholders)
        messages = copy.deepcopy(case.messages[:-1])
        messages[template_index][CONTENT_STRING] = template

        exc = ""
        function_call_key = "function_call"
        for _ in range(optimize_info.retry_times if optimize_info else 5):
            try:
                response = self.model.chat(messages, case.tools)
                if function_call_key in response:
                    return json.dumps(
                        {
                            "role": "assistant",
                            CONTENT_STRING: "",
                            function_call_key: response.get(function_call_key, ""),
                        },
                        ensure_ascii=False,
                    )
                return json.dumps(
                    {
                        "role": "assistant",
                        CONTENT_STRING: response.get(CONTENT_STRING, ""),
                    },
                    ensure_ascii=False,
                )
            except Exception as e:
                exc = e
        raise JiuWenBaseException(
            error_code=StatusCode.PROMPT_OPTIMIZE_EVALUATE_ERROR.code,
            message=StatusCode.PROMPT_OPTIMIZE_EVALUATE_ERROR.errmsg.format(
                error_msg="LLM get answer failed"
            ),
        ) from exc

    def compare_answer(
        self,
        ground_answer: str,
        generate_answer: str,
        case: Case,
        optimize_info: OptimizeInfo,
    ) -> CompareRes:
        """Compare the quality of the two assistants’ answers"""
        # The value of case.messages is not None when a case is defined.
        check_not_none_with_alias(case, "case")
        evaluate_template = InnerTemplate().get(name="prompt_epo_evaluate_ref")
        reference = ""
        for msg in case.messages:
            if msg["role"] == "assistant":
                reference = json.dumps(msg, ensure_ascii=False)
        messages = PromptAssembler(evaluate_template).assemble(
            reference=reference, answer1=ground_answer, answer2=generate_answer
        )
        exc = ""
        for _ in range(optimize_info.retry_times if optimize_info else 5):
            try:
                response = self.assistant_model.chat(messages)
                text_match = re.findall(
                    r"\[\[([A-C])\]\]", response.get("content"), re.DOTALL
                )
                for cmp in CompareRes:
                    if text_match and text_match[-1] == cmp.value:
                        return cmp
            except Exception as e:
                exc = e
        raise JiuWenBaseException(
            error_code=StatusCode.PROMPT_OPTIMIZE_EVALUATE_ERROR.code,
            message=StatusCode.PROMPT_OPTIMIZE_EVALUATE_ERROR.errmsg.format(
                error_msg="LLM evaluate answer failed"
            ),
        ) from exc

    def evaluate(
        self,
        templates: List[str],
        optimize_info: OptimizeInfo,
        cancel_event: threading.Event,
    ) -> List[float]:
        """Template evaluator interface"""
        check_not_none_with_alias(optimize_info, "optimize_info")
        check_not_none_with_alias(cancel_event, "cancel_event")
        eval_cases = random.sample(optimize_info.cases, optimize_info.eval_size)
        num_thread = min(optimize_info.llm_parallel, len(eval_cases))
        # The value of num_thread is not 0 when optimize_info is defined.
        num_per_thread = (len(eval_cases) + num_thread - 1) // num_thread
        template_scores = [[1, 1] for _ in range(len(templates))]
        mq = queue.Queue()

        def parallel_eval(thread_cases):
            try:
                for case in thread_cases:
                    case = self.case_augment(case)
                    ground_answer = self.get_answer(templates[0], case, optimize_info)
                    for i, template in enumerate(templates[1:], start=1):
                        if cancel_event.is_set():
                            logger.info("evolve finished")
                            return
                        generate_answer = self.get_answer(template, case, optimize_info)
                        cmp_result = self.compare_answer(
                            ground_answer, generate_answer, case, optimize_info
                        )
                        if cmp_result == CompareRes.GOOD:
                            template_scores[i][0] += 1
                        if cmp_result == CompareRes.BAD:
                            template_scores[i][1] += 1
            except Exception as e:
                mq.put(e)

        threads = []
        for i in range(num_thread):
            t = threading.Thread(
                target=parallel_eval,
                args=(eval_cases[i * num_per_thread : (i + 1) * num_per_thread],),
            )
            threads.append(t)
            t.start()

        # Catching child thread exceptions
        try:
            e = mq.get(block=False)
        except queue.Empty:
            pass
        else:
            if e is not None:
                raise JiuWenBaseException(
                    error_code=StatusCode.PROMPT_OPTIMIZE_EVALUATE_ERROR.code,
                    message=StatusCode.PROMPT_OPTIMIZE_EVALUATE_ERROR.errmsg.format(
                        error_msg="parallel_eval failed"
                    ),
                )

        # Waiting for all child threads complete
        for t in threads:
            t.join()
        # i[0] indicates a good score, i[1] indicates a bad score
        template_scores = [i[0] / (i[1] if i[1] != 0 else 1) for i in template_scores]
        return template_scores
