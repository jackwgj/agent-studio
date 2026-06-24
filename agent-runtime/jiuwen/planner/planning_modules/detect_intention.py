# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""Detect intention planning module"""

import ast
import re
from abc import ABC
from dataclasses import dataclass, field

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.messages import AIMessage
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.planner.common import OpType
from jiuwen.planner.common.stream_post_utils import convert_ai_message_to_llm_output
from jiuwen.planner.planning_modules.base import LlmPlanningModule
from jiuwen.prompt import TemplateManager, Prompt, Template


@dataclass
class IntentDetectionConfig:
    user_prompt: str
    intent_detection_template: Template
    category_info: str = field(default="")
    category_list: list[str] = field(default_factory=list)
    category_name_list: list[str] = field(default_factory=list)
    default_class: str = "分类0"
    enable_history: bool = False
    enable_input: bool = True
    chat_history_max_turn: int = 3
    example_content: list[str] = field(default_factory=list)
    overridable: bool = False
    enableKnowledge: bool = False
    enalbe_q2fewshot: bool = True
    enable_validate: bool = True
    recallThreshold: float = 0.9
    levenshtein_ration: float = 0.8
    q2label_few_shot_score: float = 0.7


USER_PROMPT = "user_prompt"
CATEGORY_INFO = "category_info"
CATEGORY_LIST = "category_list"
DEFAULT_CLASS = "default_class"
CHAT_HISTORY = "chat_history"
ENABLE_HISTORY = "enable_history"
ENABLE_INPUT = "enable_input"
EXAMPLE_CONTENT = "example_content"
CHAT_HISTORY_MAX_TURN = "chat_history_max_turn"
INTENT_DETECTION_TEMPLATE = "intent_detection_template"
ROLE = "role"
CONTENT = "content"
ROLE_MAP = {"user": "用户", "assistant": "助手", "system": "系统", "tool": "工具"}
JSON_PARSE_FAIL_REASON = "当前意图识别的输出:'{result}'格式不符合有效的JSON规范，导致解析失败，因此返回默认分类。"
CLASS_KEY_MISSING_REASON = (
    "当前意图识别的输出 '{result}' 缺少必要的输出'class'分类信息，因此返回默认分类。"
)
VALIDATION_FAIL_REASON = "当前意图识别的输出类别 '{intent_class}' 不在预定义的分类列表: '{category_list}'中，因此系统返回默认分类。"
CLASS = "class"
REASON = "reason"


def _get_config_info():
    formatted_config = dict()
    formatted_config[USER_PROMPT] = ""

    # default class is '分类0' if '分类0' in category_list, otherwise '分类1'
    formatted_config[DEFAULT_CLASS] = "分类1"

    # intent_detection_template
    model_name = "gy-agentBuilder-38b-v31-jiuwen"
    # get intent_detection_template from prompt resource
    intent_detection_template = TemplateManager().get(
        name="intent_detection", filters={"tag": model_name}
    )
    formatted_config[INTENT_DETECTION_TEMPLATE] = intent_detection_template

    # enable_history switch
    formatted_config[ENABLE_HISTORY] = True

    # enable_input switch
    formatted_config[ENABLE_INPUT] = False

    # chatHistoryMaxTurn
    formatted_config[CHAT_HISTORY_MAX_TURN] = 10

    # exampleContent
    return formatted_config


class DetectIntention(LlmPlanningModule, ABC):
    def __init__(self, plan_config, model):
        super(DetectIntention, self).__init__(
            op_type=OpType.DetectIntention, plan_config=plan_config, model=model
        )
        self.intent_config = IntentDetectionConfig(**_get_config_info())
        self.intent_function_map = {}

    async def pre_process(self, inputs: dict, intent_config):
        """
        Pre-process inputs for model
        """
        try:
            final_prompts = []
            for prompt_message in intent_config.intent_detection_template.content:
                rendered_content = await Prompt(
                    template=Template(name="template", content=[prompt_message])
                ).ainvoke(inputs)
                final_prompts.append(
                    {
                        ROLE: prompt_message.get(ROLE, ""),
                        CONTENT: rendered_content,
                    }
                )
        except JiuWenBaseException:
            raise
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_INTENT_DETECTION_PROMPT_INVOKE_ERROR.errmsg.format(
                    error_msg="ERROR OCCURRED WHILE FETCHING THE INTENT DETECTION PROMPT TEMPLATE."
                ),
                error_code=StatusCode.WORKFLOW_INTENT_DETECTION_PROMPT_INVOKE_ERROR.code,
            ) from e

        try:
            llm_inputs = LanguageModelInput(
                messages=ModelUtil.switch_message(final_prompts), tools=None
            )
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_INTENT_DETECTION_MODEL_INPUT_ERROR.errmsg.format(
                    error_msg=str(e)
                ),
                error_code=StatusCode.WORKFLOW_INTENT_DETECTION_MODEL_INPUT_ERROR.code,
            ) from e
        return llm_inputs

    async def create_llm_input(self, **kwargs):
        planning_state = kwargs.get("planning_state")
        steps = planning_state.context.get("steps", [])
        max_turn = self.intent_config.chat_history_max_turn
        chat_history = steps[:1] + steps[1:][-max_turn:]

        functions = kwargs.get("functions")
        formatted_functions = (
            self.format_tools(functions, **kwargs) if functions else []
        )

        category_info = "\n".join(
            f"分类{index}: {function.get('name')}"
            for index, function in enumerate(formatted_functions, start=1)
        )
        self.intent_function_map.update(
            {
                f"分类{index}": function.get("name")
                for index, function in enumerate(formatted_functions, start=1)
            }
        )

        current_inputs = {
            CATEGORY_INFO: category_info,
            DEFAULT_CLASS: self.intent_config.default_class,
            ENABLE_HISTORY: self.intent_config.enable_history,
            ENABLE_INPUT: self.intent_config.enable_input,
            EXAMPLE_CONTENT: "\n\n".join(self.intent_config.example_content),
            CHAT_HISTORY_MAX_TURN: self.intent_config.chat_history_max_turn,
        }

        if self.intent_config.enable_history:
            chat_history_sliced = chat_history[
                -self.intent_config.chat_history_max_turn :
            ]
            current_inputs[CHAT_HISTORY] = "\n".join(
                f"{ROLE_MAP.get(h.get(ROLE, CONTENT), '用户')}：{h.get(CONTENT)}"
                for h in chat_history_sliced
            )

        llm_inputs = await self.pre_process(current_inputs, self.intent_config)
        return llm_inputs

    async def invoke(self, **kwargs):
        llm_input = await self.create_llm_input(**kwargs)

        llm_message: AIMessage = await self.llm.ainvoke(llm_input)
        llm_output = convert_ai_message_to_llm_output(llm_message)
        logger.info("\n<LLM Response>:Received")
        intent_class = self.intent_detection_post_process(llm_output.content)
        updated_state = {
            "intent_function": self.intent_function_map.get(intent_class, None),
            "planning_state": kwargs.get("planning_state"),
        }
        self.update_planning_state(**updated_state)

    def update_planning_state(self, **kwargs):
        intent_function = kwargs.get("intent_function")
        planning_state = kwargs.get("planning_state")
        if intent_function:
            planning_state.intent_function_name = intent_function

    def intent_detection_post_process(self, result):
        """
        Post-process the result
        Args:
            result: The result is a dict string.
        Returns:
            The processed results are 'class' and 'reason'
        """
        try:
            parsed_dict = ast.literal_eval(result)
            if not isinstance(parsed_dict, dict):
                return self.intent_config.default_class, JSON_PARSE_FAIL_REASON.format(
                    result=result
                )
        except Exception:
            return self.intent_config.default_class, JSON_PARSE_FAIL_REASON.format(
                result=result
            )

        # post_process class information
        if not parsed_dict.get(CLASS):
            return self.intent_config.default_class, CLASS_KEY_MISSING_REASON.format(
                result=parsed_dict
            )

        intent_class = (
            parsed_dict.get(CLASS)
            .replace("\n", "")
            .replace(" ", "")
            .replace("'", "")
            .replace('"', "")
        )
        match = re.search(r"分类\d+", intent_class)
        if match:
            parsed_dict.update({CLASS: match.group(0)})
        logger.info(
            f"Finish detect intention, class: {type(parsed_dict.get(CLASS))}, "
            f"reason: {type(parsed_dict.get(REASON, ''))}"
        )
        return parsed_dict.get(CLASS)
