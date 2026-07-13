#!/usr/bin/python3.9
# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved
"""
@Copyright: Copyright (C) 2023-2024 Huawei Inc
@Author: jiuwen
@Description: Questioner
"""

import ast
import json
import os
import re
from copy import deepcopy, copy
from dataclasses import dataclass, field
from enum import Enum
from typing import Any

from jiuwen.common.config import config
from jiuwen.common.configs.env_constants import RAILS_CUSTOM_RAILS_PATH_KEY
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.common.security.util.fileutil import validate_path_traversal
from jiuwen.common.types import Type
from jiuwen.controller.task_planner.planning_modules.prompt_module import ChatContent
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.callbacks.span import Span
from jiuwen.orchestration.flow.components.interactive_base import InteractiveComponent
from jiuwen.orchestration.flow.components.message_base import (
    MessageComponentBase,
    STRUCT_OUTPUT_TEMPLATE,
    ENABLE_STRUCT_MESSAGE,
)
from jiuwen.orchestration.flow.components.util import (
    format_interaction_res,
    process_on_invoke_info,
    filter_enable_history,
)
from jiuwen.orchestration.flow.constant import (
    WORKFLOW_CHAT_MANAGER,
    WORKFLOW_LLM_EXTRA_CONFIGS,
    WORKFLOW_CHAT_HISTORY,
    QuestionerKeyword,
    USER_FIELDS,
)
from jiuwen.orchestration.flow.enum import NodeType, ExecutionStatus
from jiuwen.orchestration.flow.model.workflow_data_class import (
    WorkflowMetadata,
    WorkflowStreamingData,
)
from jiuwen.orchestration.flow.runtime_context import RuntimeContext
from jiuwen.orchestration.flow.state import State
from jiuwen.orchestration.flow.stream.workflow_streaming import WorkflowStreamCallback
from jiuwen.orchestration.utils import Input, Output
from jiuwen.planner.planning_modules.base import PromptPlanningModule
from jiuwen.planner.rails.config import RailsConfig
from jiuwen.planner.rails.rails_factory import RailsFactory
from jiuwen.prompt import TemplateManager


class Role(Enum):
    USER = "user"
    ASSISTANT = "assistant"


@dataclass
class ModelConfig:
    name: str
    source: str
    url: str


class QuestionConstructionMethod(Enum):
    RuleBased = "rule_based"
    LLMBased = "llm_based"


class ResponseType(Enum):
    ReplyDirectly = "reply_directly"
    ReplyBasedOnOptions = "reply"


class ConfigPrevious(Enum):
    TEMPLATE_CONTENT = "templateContent"
    TEMPERATURE = "temperature"
    QUESTION = "question"
    EXTRACONFIG = "extraConfig"
    LLM_MODEL = "llm_model"
    MODEL = "model"
    MODEL_NAME = "model_name"


class ConfigCurrent(Enum):
    EXTRA_PROMPT_FOR_FIELDS_EXTRACTION = "extra_prompt_for_fields_extraction"
    TEMPERATURE = "temperature"
    QUESTION_CONTENT = "question_content"
    FIELD_NAMES = "field_names"
    EXTRACT_FIELDS_FROM_RESPONSE = "extract_fields_from_response"
    WITH_CHAT_HISTORY = "with_chat_history"
    INPUT_COMPLEMENT = "input_complement"
    TYPE_COMPLEMENT = "type_complement"
    MAX_RESPONSE = "max_response"
    PROMPT_TEMPLATE = "prompt_template"
    EXAMPLE_CONTENT = "example_content"
    RAILS_CONFIG = "rails_config"
    MODEL = "model"
    MODEL_NAME = "modelName"
    MODEL_TYPE = "modelType"
    HYPER_PARAMETERS = "hyper_parameters"
    TOP_P = "top_p"


@dataclass
class QuestionerState(State):
    extracted_key_fields: dict = field(default_factory=dict)
    user_response: str = ""
    reflection_map: dict = field(default_factory=dict)
    response_num: int = 0
    status: ExecutionStatus = ExecutionStatus.START
    inputs: dict = field(default_factory=dict)


@dataclass
class QuestionerConfig:
    model_name: str
    model_type: str = "agentBuilder"
    response_type: str = ResponseType.ReplyDirectly.value
    # 前端适配该参数后设置为False
    extract_fields_from_response: bool = True
    with_chat_history: bool = False
    chat_history_max_rounds: int = 0
    extra_prompt_for_fields_extraction: str = None
    question_content: str = None
    question_construction_method: str = QuestionConstructionMethod.RuleBased.value
    cn_fields_name: dict = field(default_factory=dict)
    max_response: int = 3
    option_content: list = field(default_factory=list)
    key_fields: list[dict] = field(default_factory=list)
    input_complement: bool = False
    type_complement: str = ""
    hyper_parameters: dict = None
    extension: dict = None
    prompt_template: list[dict] = None
    example_content: str = QuestionerKeyword.DEFAULT_SHOT
    rails_config: dict = None


@dataclass
class QuestionerOutput:
    """
    define data class of questioner component
    """

    user_response: str = None
    question: str = None
    key_fields: dict = field(default_factory=dict)

    def __getattr__(self, item):
        """
        get attributes
        """
        return self.key_fields.get(item)

    def update_key_field(self, new_key_fields):
        """
        update key fields
        Args:
            new_key_fields: new key fields

        Returns:

        """
        if new_key_fields is not None:
            self.key_fields.update(new_key_fields)

    def as_dict(self):
        """
        transfer QuestionerOutput to dict and output it
        """
        questioner_output = {}
        if self.user_response:
            questioner_output[QuestionerKeyword.USER_RESPONSE] = self.user_response
        if self.question:
            questioner_output[QuestionerKeyword.QUESTION] = self.question
        questioner_output.update(self.key_fields)
        return questioner_output


DEFAULT_RAILS_CONFIG = {
    "rails": {"execution": [{"action": "parse_time"}]},
    "actions_config": [{"action": "parse_time", "action_extra_args": {"format": ""}}],
}

LLM_INPUTS = "llm_inputs"
LLM_OUTPUTS = "llm_outputs"


def format_questioner_configs(configs: dict) -> dict:
    """
    Preprocessing compatibility configuration information
    """
    formatted_questioner_configs = {}

    # llm config
    if isinstance(configs.get(ConfigCurrent.MODEL.value), dict) and configs.get(
        ConfigCurrent.MODEL.value, {}
    ).get(ConfigCurrent.MODEL_NAME.value, ""):
        formatted_questioner_configs[QuestionerKeyword.MODEL_NAME] = configs.get(
            ConfigCurrent.MODEL.value, {}
        ).get(ConfigCurrent.MODEL_NAME.value, "")
    else:
        formatted_questioner_configs[QuestionerKeyword.MODEL_NAME] = configs.get(
            ConfigPrevious.MODEL.value
        )
    if isinstance(configs.get(ConfigCurrent.MODEL.value), dict) and configs.get(
        ConfigCurrent.MODEL.value, {}
    ).get(ConfigCurrent.MODEL_TYPE.value, ""):
        formatted_questioner_configs[QuestionerKeyword.MODEL_TYPE] = configs.get(
            ConfigCurrent.MODEL.value, {}
        ).get(ConfigCurrent.MODEL_TYPE.value, "")
    formatted_questioner_configs[QuestionerKeyword.HYPER_PARAMETERS] = configs.get(
        QuestionerKeyword.MODEL, {}
    ).get(QuestionerKeyword.HYPER_PARAMETERS_CANAL, {})
    top_p = (
        configs.get(QuestionerKeyword.MODEL, {})
        .get(QuestionerKeyword.HYPER_PARAMETERS_CANAL, {})
        .get(ConfigCurrent.TOP_P.value, 0.15)
    )
    temperature = (
        configs.get(QuestionerKeyword.MODEL, {})
        .get(QuestionerKeyword.HYPER_PARAMETERS_CANAL, {})
        .get(ConfigCurrent.TEMPERATURE.value, 0.1)
    )
    formatted_questioner_configs[QuestionerKeyword.HYPER_PARAMETERS][
        QuestionerKeyword.TOP_P_KEYWORD
    ] = top_p
    formatted_questioner_configs[QuestionerKeyword.HYPER_PARAMETERS][
        ConfigCurrent.TEMPERATURE.value
    ] = temperature
    formatted_questioner_configs["extension"] = configs.get(
        QuestionerKeyword.MODEL, {}
    ).get("extension", {})

    # other config
    formatted_questioner_configs[ConfigCurrent.QUESTION_CONTENT.value] = configs.get(
        ConfigCurrent.QUESTION_CONTENT.value, configs.get(ConfigPrevious.QUESTION.value)
    )
    if ConfigCurrent.EXTRA_PROMPT_FOR_FIELDS_EXTRACTION.value in configs:
        formatted_questioner_configs[
            ConfigCurrent.EXTRA_PROMPT_FOR_FIELDS_EXTRACTION.value
        ] = configs.get(ConfigCurrent.EXTRA_PROMPT_FOR_FIELDS_EXTRACTION.value)

    if configs.get(ConfigCurrent.FIELD_NAMES.value):
        formatted_questioner_configs[QuestionerKeyword.CN_FIELDS_NAME] = {
            item.get(QuestionerKeyword.FIELD_NAME): item.get(
                QuestionerKeyword.CN_FIELD_NAME
            )
            for item in configs.get(ConfigCurrent.FIELD_NAMES.value)
        }
        formatted_questioner_configs[QuestionerKeyword.KEY_FIELDS] = [
            {
                QuestionerKeyword.NAME: item.get(QuestionerKeyword.FIELD_NAME, ""),
                QuestionerKeyword.DESC: item.get(QuestionerKeyword.DESCRIPTION, ""),
                "required": item.get("required", True),
                "default_value": item.get("default_value"),
                "reflection": item.get("reflection", False),
            }
            for item in configs.get(ConfigCurrent.FIELD_NAMES.value)
        ]
    elif configs.get(ConfigPrevious.EXTRACONFIG.value):
        formatted_questioner_configs[QuestionerKeyword.CN_FIELDS_NAME] = {
            item.get(QuestionerKeyword.NAME): item.get(
                QuestionerKeyword.QUESTIONKEYWORD
            )
            for item in configs.get(ConfigPrevious.EXTRACONFIG.value)
        }
        formatted_questioner_configs[QuestionerKeyword.KEY_FIELDS] = [
            {
                QuestionerKeyword.NAME: item.get(QuestionerKeyword.NAME, ""),
                QuestionerKeyword.DESC: item.get(QuestionerKeyword.DESCRIPTION, ""),
            }
            for item in configs.get(ConfigPrevious.EXTRACONFIG.value)
        ]

    if ConfigCurrent.EXTRACT_FIELDS_FROM_RESPONSE.value in configs:
        formatted_questioner_configs[
            ConfigCurrent.EXTRACT_FIELDS_FROM_RESPONSE.value
        ] = configs.get(ConfigCurrent.EXTRACT_FIELDS_FROM_RESPONSE.value)
    if ConfigCurrent.WITH_CHAT_HISTORY.value in configs:
        formatted_questioner_configs[ConfigCurrent.WITH_CHAT_HISTORY.value] = (
            configs.get(ConfigCurrent.WITH_CHAT_HISTORY.value)
        )
    if ConfigCurrent.INPUT_COMPLEMENT.value in configs:
        formatted_questioner_configs[ConfigCurrent.INPUT_COMPLEMENT.value] = (
            configs.get(ConfigCurrent.INPUT_COMPLEMENT.value)
        )
    if ConfigCurrent.MAX_RESPONSE.value in configs:
        formatted_questioner_configs[ConfigCurrent.MAX_RESPONSE.value] = configs.get(
            ConfigCurrent.MAX_RESPONSE.value
        )
    if formatted_questioner_configs.get(
        ConfigCurrent.INPUT_COMPLEMENT.value
    ) and configs.get(ConfigCurrent.TYPE_COMPLEMENT.value):
        formatted_questioner_configs[ConfigCurrent.TYPE_COMPLEMENT.value] = configs.get(
            ConfigCurrent.TYPE_COMPLEMENT.value
        )
    if formatted_questioner_configs.get(ConfigCurrent.WITH_CHAT_HISTORY.value):
        formatted_questioner_configs[QuestionerKeyword.CHAT_HISTORY_MAX_ROUNDS] = (
            QuestionerKeyword.CHAT_HISTORY_MAX_ROUNDS_NUMBER
        )

    if ConfigCurrent.PROMPT_TEMPLATE.value in configs:
        formatted_questioner_configs[ConfigCurrent.PROMPT_TEMPLATE.value] = configs.get(
            ConfigCurrent.PROMPT_TEMPLATE.value
        )

    if ConfigCurrent.RAILS_CONFIG.value in configs:
        formatted_questioner_configs[ConfigCurrent.RAILS_CONFIG.value] = configs.get(
            ConfigCurrent.RAILS_CONFIG.value
        )

    if configs.get(ConfigCurrent.EXAMPLE_CONTENT.value):
        formatted_questioner_configs[ConfigCurrent.EXAMPLE_CONTENT.value] = configs.get(
            ConfigCurrent.EXAMPLE_CONTENT.value
        )

    return formatted_questioner_configs


class Questioner(Invokable, MessageComponentBase, InteractiveComponent):
    """
    Questioner is a sub class of invokable, as a type of node in workflow
    """

    def __init__(self):
        """
        Initialization of Questioner
        """
        self.component_config = None
        self.runtime_context = None
        self._workflow_llm_extra_configs = {}
        self.model_map = {}
        self.inputs = dict()
        self.user_response = ""
        self.metadata = None
        self.default_rails_config = deepcopy(DEFAULT_RAILS_CONFIG)
        self.questioner_config = QuestionerConfig("")
        self.chat_manager = None
        self.llm = None
        self.node_state = QuestionerState()
        self.default_rail = None
        self.execute_rail = None
        self.input_rail = None
        self.prompt_reflection = None
        self.prompt = None
        self.prompt_engine = None
        self.conversation_id = None
        self.struct_output_template = None
        self.enable_struct_message = None
        super().__init__()

    @staticmethod
    def check_percent_signs(input_string, valid_chars):
        """
        check available percentage sign
        """
        # Convert the list of valid characters into a character set in regular expressions.
        pattern = r"%([" + "".join(re.escape(char) for char in valid_chars) + "])"

        # Use re.sub to replace all matching patterns.
        # If the resulting string is empty or contains only non-percent characters,
        # it indicates that all percent signs are immediately followed by valid characters.
        replaced_string = re.sub(pattern, "", input_string)

        # Check if the replaced string is empty or contains only non-percent characters.
        if replaced_string == "" or re.fullmatch(r"[^%]+", replaced_string):
            return True
        return False

    @staticmethod
    def camel_to_snake(text):
        """
        convert camel format configuration value into snake format
        """
        result = [text[0].lower()]
        for char in text[1:]:
            if char.isupper():
                result.extend(["_", char.lower()])
            else:
                result.append(char)
        return "".join(result)

    @staticmethod
    def sub_placeholder(text: str, variables: dict) -> str:
        """
        Substitute placeholders in a string with values from a dictionary.
        """

        def replace(match):
            key = match.group(1)
            return str(variables.get(key))

        try:
            result = re.sub(r"\{\{([^{}]+)\}\}", replace, text)
            return result
        except (KeyError, TypeError, AttributeError):
            return ""

    @staticmethod
    def get_latest_k_rounds_chat(chat_history: list, k: int) -> list:
        """
        get the chat history of the last k rounds.
        chat_history: [... prev_question, prev_query] + [cur_question, cur_query ...]
        """
        if k is None:
            return chat_history
        return chat_history[-k * 2 - 1 :]

    @staticmethod
    def prompt_template_check(conf):
        """
        check if prompt template is legal
        """
        # check prompt template. List<Dict>, with role, content as key and value type as string.
        if conf.get("prompt_template"):
            if not isinstance(conf.get("prompt_template"), list):
                return (
                    True,
                    "Prompt Template should be a list in questioner configuration.",
                )
            for unit in conf["prompt_template"]:
                prompt_template_match = (
                    not isinstance(unit, dict)
                    or not isinstance(unit.get("role"), str)
                    or not isinstance(unit.get("content"), str)
                )
                if prompt_template_match:
                    return (
                        True,
                        "Prompt Template should be with role and content in questioner configuration.",
                    )
        return False, ""

    @classmethod
    def from_state(
        cls,
        state: QuestionerState,
        conf: dict,
        runtime_context: RuntimeContext,
        metadata: WorkflowMetadata,
    ):
        """
        Initialize Questioner from input question_state, runtime_context and WorkflowMetadata
        """
        questioner_instance = cls()
        questioner_instance.init(
            conf=conf, runtime_context=runtime_context, metadata=metadata
        )
        questioner_instance.node_state = deepcopy(state)
        return questioner_instance

    def init(self, conf: dict, **kwargs):
        """
        Initialization of Questioner
        """
        if self.node_state.status == ExecutionStatus.USER_INTERACT:
            return
        # initialize questioner state
        self.node_state = QuestionerState()
        runtime_context = kwargs.get("runtime_context")
        metadata = kwargs.get("metadata")
        self.component_config = deepcopy(conf)
        self.enable_struct_message = conf.get(ENABLE_STRUCT_MESSAGE, False)
        conf = self.dict_keys_camel_to_snake(conf)
        self.check_config(conf)
        self.runtime_context = runtime_context
        # get workflow extra_configs
        self._workflow_llm_extra_configs = (
            runtime_context.get(WORKFLOW_LLM_EXTRA_CONFIGS, {}) or {}
        )
        self.model_map = self._workflow_llm_extra_configs.get("model_map", {})
        # workflow must put chat history with lastest query into runtime context
        self.inputs = dict()
        self.user_response = ""
        self.metadata = metadata
        self.default_rails_config = deepcopy(DEFAULT_RAILS_CONFIG)
        self.struct_output_template = conf.get(STRUCT_OUTPUT_TEMPLATE, "")

        # initialize questioner config
        self.questioner_config = QuestionerConfig(**format_questioner_configs(conf))

        # get chat manager for interact with user
        self.chat_manager = runtime_context.get(WORKFLOW_CHAT_MANAGER)
        # initialize llm service for extract key fields
        self.llm = ModelFactory().get_model(
            model_type=self.questioner_config.model_type,
            model_name=self.questioner_config.model_name,
            **self.questioner_config.hyper_parameters,
            **self.questioner_config.extension,
        )

        # initialize questioner state
        self.node_state = QuestionerState()

        # initial rails
        self.initial_rails()

        # initial prompt
        self.initial_prompt()

    def should_interrupt(self) -> bool:
        """
        check if should be interrupted

        Returns:
            bool: The result of the interruption.
        """
        return self.node_state.status == ExecutionStatus.USER_INTERACT

    def error_to_output(self):
        """发生异常时，走默认值或者异常分支时，状态按正常结束算"""
        self.node_state.status = ExecutionStatus.END

    def get_state(self):
        """
        get questioner state
        """
        return self.node_state

    def load_state(self, state: QuestionerState):
        """
        load questioner state from input state
        """
        self.node_state = deepcopy(state)

    def get_node_status(self):
        """
        返回节点状态
        :return:
        """
        return self.node_state.status

    def check_config(self, conf):
        """
        check if the conf is legal
        """
        match_result, error_msg = self.match_error_config(conf)
        if match_result:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_INITIAL_CONFIG_ERROR.errmsg.format(
                    error_msg=error_msg
                ),
                error_code=StatusCode.WORKFLOW_INITIAL_CONFIG_ERROR.code,
            )

    def match_error_config(self, conf):
        """
        check if the conf is illegal
        """
        # check model configuration. modelName and modelType is string. temperature is integer or float and > 0
        if not isinstance(
            conf.get("model", {}).get("modelName"), str
        ) or not isinstance(conf.get("model", {}).get("modelType"), str):
            return True, "Model name or model type is not in questioner configuration."
        temperature = (
            conf.get("model", {}).get("hyperParameters", {}).get("temperature", 0)
        )
        match_temperature = not isinstance(temperature, (int, float))
        if match_temperature:
            return True, "Temperature is illegal in questioner configuration."

        prompt_template_match, prompt_template_str = self.prompt_template_check(conf)
        if prompt_template_match:
            return prompt_template_match, prompt_template_str

        # check extra prompt
        extra_prompt_match = conf.get(
            "extra_prompt_for_fields_extraction"
        ) is not None and (
            not isinstance(conf.get("extra_prompt_for_fields_extraction"), str)
        )
        if extra_prompt_match:
            return True, "Extra prompt is illegal in questioner configuration."

        # check question content. string
        question_content_match = conf.get("question_content") is not None and (
            not isinstance(conf.get("question_content"), str)
        )
        if question_content_match:
            return True, "Question content is illegal in questioner configuration."

        # check bool configuration
        valid_types = (bool, type(None))
        for key in [
            "input_complement",
            "with_chat_history",
            "extract_fields_from_response",
        ]:
            if not isinstance(conf.get(key), valid_types):
                return True, "{key} is illegal in questioner configuration.".format(
                    key=key
                )

        # check max response. integer
        if conf.get("max_response") is not None:
            max_response_match = not isinstance(conf.get("max_response"), int)
            if max_response_match:
                return True, "Max response is illegal in questioner configuration."

        # check field names. List. cn_field_name, description and field_name should be in field_names
        if not isinstance(conf.get("field_names"), (list, type(None))):
            return True, "field_names should be a list in questioner configuration."
        for unit in conf.get("field_names", []):
            if not all(
                key in unit for key in ["field_name", "description", "cn_field_name"]
            ):
                return (
                    True,
                    "field_name, description and cn_field_name should be in field_names "
                    "in questioner configuration.",
                )

        # check default rails string.
        if not isinstance(conf.get("type_complement"), (str, type(None))):
            return (
                True,
                "Type of complement should be string in questioner configuration.",
            )
        return False, ""

    def dict_keys_camel_to_snake(self, input_dict):
        """
        convert camel format configuration dictionary into snake format
        """
        output_dict = {}
        for key, value in input_dict.items():
            snake_key = self.camel_to_snake(key)
            output_dict[snake_key] = value
        return output_dict

    def check_rails(self):
        """
        check if the rails available
        """
        configs_action = []
        rails_action = []
        if not self.questioner_config.rails_config:
            return True
        if self.questioner_config.rails_config.get(
            "actions_config"
        ) and self.questioner_config.rails_config.get("rails"):
            for unit in self.questioner_config.rails_config["actions_config"]:
                if "action" not in unit and "action_extra_args" not in unit:
                    logger.error(
                        "Fail to parse action or action_extra_args in the rails action config"
                    )
                    return False
                configs_action.append(unit["action"])
            for key in self.questioner_config.rails_config["rails"]:
                if key not in ["execution", "input"]:
                    logger.error("Fail to parse the rails for the illegitimate types")
                    return False
                for rail in self.questioner_config.rails_config["rails"][key]:
                    if "action" not in rail:
                        logger.error("Fail to parse action in the rails")
                        return False
                    rails_action.append(rail["action"])
            if set(configs_action) != set(rails_action):
                logger.error("Fail to match rails actions")
                return False
            return True
        return False

    def initial_rails(self):
        """
        initial rails
        """
        # check if the rails is legal
        if not self.check_rails():
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_INITIAL_RAILS_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_INITIAL_RAILS_ERROR.code,
            )

        # initialize rails config dir
        default_rails_config_dir_str = "../../../resource/planner_rails"
        default_rails_config_dir = os.path.normpath(
            os.path.join(
                os.path.dirname(os.path.abspath(__file__)), default_rails_config_dir_str
            )
        )

        # initialize default planner rails
        planner_rails_config_default = RailsConfig.config_file_dir(
            config_path=default_rails_config_dir, config_dict=self.default_rails_config
        )

        # initialize time format based on user configuration
        time_format = QuestionerKeyword.TYPE_COMPLEMENT_MAP.get(
            self.questioner_config.type_complement,
            self.questioner_config.type_complement,
        )
        if time_format and "parse_time" in planner_rails_config_default.actions:
            planner_rails_config_default.actions["parse_time"].action_extra_args = {
                "format": time_format
            }

        rail_factory_default = RailsFactory(config=planner_rails_config_default)
        self.default_rail = rail_factory_default.get_execution_rails()

        if self.questioner_config.rails_config:
            # initialize custom planner rails
            custom_rails_config_dir_str = os.environ.get(
                RAILS_CUSTOM_RAILS_PATH_KEY,
                config.get("rails", {}).get("custom_rails_path"),
            )
            if not custom_rails_config_dir_str:
                logger.error(QuestionerKeyword.FAIL_RAILS_PATH_INITIALIZATION_ERROR)
                self.questioner_config.rails_config = {}
            else:
                validate_path_traversal(custom_rails_config_dir_str)
                custom_rails_config_dir_str = os.path.join(
                    "../../../", custom_rails_config_dir_str
                )
                custom_rails_config_dir = os.path.normpath(
                    os.path.join(
                        os.path.dirname(os.path.abspath(__file__)),
                        custom_rails_config_dir_str,
                    )
                )
                try:
                    planner_rails_config_custom = RailsConfig.config_file_dir(
                        config_path=custom_rails_config_dir,
                        config_dict=self.questioner_config.rails_config,
                    )
                    rail_factory_custom = RailsFactory(
                        config=planner_rails_config_custom
                    )
                    self.input_rail = rail_factory_custom.get_input_rails()
                    self.execute_rail = rail_factory_custom.get_execution_rails()
                except Exception as e:
                    raise JiuWenBaseException(
                        message=StatusCode.WORKFLOW_INITIAL_RAILS_ERROR.errmsg,
                        error_code=StatusCode.WORKFLOW_INITIAL_RAILS_ERROR.code,
                    ) from e

    def initial_prompt(self):
        """
        initial prompt
        """
        # initialize inference prompt
        self.prompt_engine = PromptPlanningModule()
        if self.questioner_config.prompt_template:
            self.prompt = self.questioner_config.prompt_template
        else:
            prompt = TemplateManager().get(
                name="questioner", filters={"tag": self.questioner_config.model_name}
            )
            if prompt is not None and hasattr(prompt, "content"):
                self.prompt = prompt.content
            else:
                raise JiuWenBaseException(
                    message=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR.errmsg,
                    error_code=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR.code,
                )

        # initial reflection prompt
        prompt_reflection = TemplateManager().get(
            name="questioner_reflection",
            filters={"tag": self.questioner_config.model_name},
        )
        if prompt_reflection is not None and hasattr(prompt_reflection, "content"):
            self.prompt_reflection = prompt_reflection.content

    def format_target_data(self, chat_history, user_response):
        """
        convert the chat history into target data
        """
        if len(chat_history) == 1:
            return "用户输入： {user_res}".format(user_res=user_response)
        if len(chat_history) > 1:
            history = "，".join(
                [
                    "{role}：{content}".format(
                        role=unit.get(QuestionerKeyword.ROLE),
                        content=unit.get(QuestionerKeyword.CONTENT),
                    )
                    for unit in chat_history[:-1]
                ]
            )
            return "对话历史： {history}；当前用户输入： {user_res}".format(
                history=history, user_res=user_response
            )
        return json.dumps(chat_history, ensure_ascii=False)

    async def create_prompt_template_input(self, chat_history: list) -> dict:
        """
        create input for prompt template
        """
        required_params = []
        for i, param in enumerate(self.questioner_config.key_fields):
            number = i + 1
            required_params.append(
                "变量{number}名称：{name}，变量{number}的描述：{desc} ".format(
                    number=str(number),
                    name=param.get(QuestionerKeyword.NAME, ""),
                    desc=param.get("desc", ""),
                )
            )

        required_name = []
        for _, item in self.questioner_config.cn_fields_name.items():
            required_name.append(item)
        required_name = (
            "、".join(required_name)
            + f"{len(self.questioner_config.cn_fields_name.items())}个必要信息"
        )
        # input rails for preprocessing
        user_response = (
            self.user_response
            if self.user_response
            else chat_history[-1].get(QuestionerKeyword.CONTENT)
        )
        if (
            self.questioner_config.rails_config
            and self.questioner_config.rails_config.get("rails", {}).get("input")
        ):
            try:
                result = await self.input_rail.ainvoke(
                    {
                        "query": user_response,
                        "dialogue_history": self.get_latest_chat_history(),
                        "inputs": self.inputs,
                        "outputs": self.questioner_config.key_fields,
                        "component_config": self.component_config,
                    }
                )
                user_response = result.get("query", user_response)
            except Exception as e:
                logger.error(QuestionerKeyword.FAIL_CUSTOM_INPUT_RAILS_PARSE_MESSAGE)
                raise JiuWenBaseException(
                    message=StatusCode.WORKFLOW_INPUT_RAILS_ERROR.errmsg,
                    error_code=StatusCode.WORKFLOW_INPUT_RAILS_ERROR.code,
                ) from e

        required_params_list = []
        for _, param in enumerate(self.questioner_config.key_fields):
            required_params_list.append(
                "{name}:{desc} ".format(
                    name=param.get(QuestionerKeyword.NAME, ""),
                    desc=param.get("desc", ""),
                )
            )
        required_params_list = "\n".join(required_params_list)

        chat_history = filter_enable_history(chat_history)
        dig_history = "\n".join(
            [
                "{role}：{content}".format(
                    role=unit.get(QuestionerKeyword.ROLE),
                    content=unit.get(QuestionerKeyword.CONTENT),
                )
                for unit in chat_history[:-1]
                + [{"role": "user", "content": user_response}]
            ]
        )

        extra_info = (
            self.questioner_config.extra_prompt_for_fields_extraction
            if self.questioner_config.extra_prompt_for_fields_extraction
            else ""
        )
        # Placeholder Filling
        required_params = " \n " + " \n ".join(required_params)

        return {
            "target_data": "目标数据：'{target_data}'".format(
                target_data=self.format_target_data(chat_history, user_response)
            ),
            "required_params": required_params,
            "example": self.questioner_config.example_content,
            "dig_history": dig_history,
            "required_name": required_name,
            "extra_info": extra_info,
            "required_params_list": required_params_list,
        }

    async def extract_key_fields(self, chat_history: list, debug_info=None) -> dict:
        """
        extract key fields from chat history
        """
        prompt_template_input = await self.create_prompt_template_input(chat_history)
        prompt_template = [
            ChatContent(
                role=p.get(QuestionerKeyword.ROLE),
                content=p.get(QuestionerKeyword.CONTENT),
            )
            for p in self.prompt
        ]
        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        llm_inputs = LanguageModelInput(
            messages=ModelUtil.switch_message(prompt), tools=[]
        )
        debug_info = {} if debug_info is None else debug_info
        # llm invoke
        try:
            response = await self._invoke_llm_with_insights(llm_inputs, debug_info)
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e

        # convert llm output from string to dictionary
        try:
            cur_extracted_key_fields = {
                k: v
                for k, v in ast.literal_eval(response).items()
                if v is not None and str(v)
            }
        except (SyntaxError, AttributeError, ValueError):
            logger.error(QuestionerKeyword.LLM_PARSE_FAIL_MESSAGE)
            return {}
        except Exception:
            logger.error(QuestionerKeyword.LLM_UNKNOWN_ERROR)
            return {}

        # reflection
        cur_extracted_key_fields = await self.reflection(
            cur_extracted_key_fields, prompt_template_input, debug_info
        )

        # processing of default rails
        if self.questioner_config.input_complement:
            processed_extracted_key_fields = await self.convert_default_rails(
                cur_extracted_key_fields
            )
        else:
            processed_extracted_key_fields = cur_extracted_key_fields

        # processing of custom rails
        if (
            self.questioner_config.rails_config
            and self.questioner_config.rails_config.get("rails", {}).get("execution")
        ):
            processed_extracted_key_fields = await self.convert_custom_exec_rails(
                processed_extracted_key_fields, chat_history
            )
        return processed_extracted_key_fields

    async def reflection(
        self, cur_extracted_key_fields, prompt_template_input, debug_info
    ):
        """
        reflection at parameters level and improve the result
        """
        try:
            for unit in self.questioner_config.key_fields:
                extract_value = cur_extracted_key_fields.get(unit.get("name"))
                if (
                    extract_value
                    and unit.get("reflection")
                    and self.node_state.reflection_map.get(unit.get("name"))
                    != extract_value
                ):
                    prompt_template_input["field_name"] = unit.get("name")
                    prompt_template_input["response"] = {
                        unit.get("name"): extract_value
                    }
                    prompt_template = [
                        ChatContent(
                            role=p.get(QuestionerKeyword.ROLE),
                            content=p.get(QuestionerKeyword.CONTENT),
                        )
                        for p in self.prompt_reflection
                    ]
                    prompt = self.prompt_engine.format(
                        keywords=prompt_template_input, prompt_template=prompt_template
                    )
                    llm_inputs = LanguageModelInput(
                        messages=ModelUtil.switch_message(prompt), tools=[]
                    )
                    new_response = await self._invoke_llm_with_insights(
                        llm_inputs, debug_info
                    )
                    new_response = ast.literal_eval(new_response)
                    new_response = {
                        k: v for k, v in new_response.items() if k == unit.get("name")
                    }
                    self.node_state.reflection_map.update(new_response)
                    cur_extracted_key_fields.update(new_response)
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_QUESTIONER_REFLECTION_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_QUESTIONER_REFLECTION_ERROR.code,
            ) from e
        return cur_extracted_key_fields

    async def convert_default_rails(self, cur_extracted_key_fields):
        """
        value transformation based on default rails
        """
        try:
            output = await self.default_rail.ainvoke(
                {"arguments": cur_extracted_key_fields}
            )
            result = output.get("arguments")
            return result if result else cur_extracted_key_fields
        except UnboundLocalError as e:
            logger.error(QuestionerKeyword.FAIL_TIME_PARSE_MESSAGE)
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_EXECUTION_DEFAULT_RAILS_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_EXECUTION_DEFAULT_RAILS_ERROR.code,
            ) from e
        except Exception as e:
            logger.error(QuestionerKeyword.FAIL_TIME_PARSE_MESSAGE_UNKNOWN)
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_EXECUTION_DEFAULT_RAILS_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_EXECUTION_DEFAULT_RAILS_ERROR.code,
            ) from e

    async def convert_custom_exec_rails(
        self, processed_extracted_key_fields, chat_history
    ):
        """
        value transformation based on custom rails
        """
        try:
            rail_result = await self.execute_rail.ainvoke(
                {
                    "arguments": processed_extracted_key_fields,
                    "inputs": self.inputs,
                    "outputs": self.questioner_config.key_fields,
                    "extracted_args": self.node_state.extracted_key_fields,
                    "dialogue_history": self.get_latest_chat_history(),
                    "component_config": self.component_config,
                }
            )
            result = rail_result.get("arguments")
            return (
                result
                if result and isinstance(result, dict)
                else processed_extracted_key_fields
            )
        except Exception as e:
            logger.error(QuestionerKeyword.FAIL_CUSTOM_EXECUTION_RAILS_PARSE_MESSAGE)
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_EXECUTION_CUSTOM_RAILS_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_EXECUTION_CUSTOM_RAILS_ERROR.code,
            ) from e

    def construct_question_based_on_rule(self) -> str:
        """
        construct question based on rule for extracting key fields
        """
        unextracted_key_fields = []
        for kf in self.questioner_config.key_fields:
            question_keyword_name = kf.get(QuestionerKeyword.NAME, "")
            keyword_name = self.node_state.extracted_key_fields.get(
                question_keyword_name, ""
            )
            if (
                question_keyword_name not in self.node_state.extracted_key_fields
                or not keyword_name
            ):
                unextracted_key_fields.append(kf)
        unextracted_cn_field_names = []
        for kf in unextracted_key_fields:
            keyword = self.questioner_config.cn_fields_name.get(kf.get("name"))
            if keyword:
                unextracted_cn_field_names.append(keyword)
            else:
                unextracted_cn_field_names.append(kf.get("desc"))
        question = "请您提供{unextracted_cn_field_names}相关的信息".format(
            unextracted_cn_field_names=", ".join(unextracted_cn_field_names)
        )
        return question

    def get_latest_user_query_intent(self, k=None):
        """
        获取最后一条用户消息的intent
        """
        chat_history = self.runtime_context.get(WORKFLOW_CHAT_HISTORY, None)
        if chat_history:
            chat_history = chat_history.get_conversation_history()
        else:
            return None

        # 从后往前找最后一条role为"user"的记录
        for chat in reversed(chat_history):
            if chat.get("role") == "user":
                return chat.get("intent")

        return None

    def get_latest_chat_history(self, k=None):
        """
        get the latest chat history from runtime_context
        """
        chat_history = deepcopy(self.runtime_context.get(WORKFLOW_CHAT_HISTORY, None))
        if chat_history:
            chat_history = chat_history.get_conversation_history()
        else:
            chat_history = []
        chat_history = self.get_latest_k_rounds_chat(chat_history, k)
        # get the latest k chat history
        for history in chat_history:
            history.update(
                {QuestionerKeyword.CONTENT: history.get(QuestionerKeyword.CONTENT)}
            )
        return chat_history

    def get_extract_all_key_fields(self):
        """
        check whether extract all key fields
        """
        unextracted_key_fields = []
        default_map = {}
        ask_more_flag = False
        for kf in self.questioner_config.key_fields:
            question_keyword_name = kf.get(QuestionerKeyword.NAME, "")
            value = self.node_state.extracted_key_fields.get(question_keyword_name)
            if kf.get("default_value") and not value:
                default_map[question_keyword_name] = kf.get("default_value")
            if (
                question_keyword_name not in self.node_state.extracted_key_fields
                or value is None
            ):
                unextracted_key_fields.append(kf)
                if kf.get("required") and not kf.get("default_value"):
                    ask_more_flag = True
        if not ask_more_flag:
            self.node_state.extracted_key_fields.update(default_map)
            self.node_state.extracted_key_fields = {
                k: v
                for k, v in self.node_state.extracted_key_fields.items()
                if v is not None
            }
            return []
        return unextracted_key_fields

    async def update_key_fields(self, debug_info):
        """
        updating key fields
        """
        chat_history = self.get_latest_chat_history(
            k=self.questioner_config.chat_history_max_rounds
        )
        cur_extracted_key_fields = await self.extract_key_fields(
            chat_history, debug_info
        )
        cur_extracted_key_fields = {
            k: v
            for k, v in cur_extracted_key_fields.items()
            if k in self.questioner_config.cn_fields_name
        }
        self.node_state.extracted_key_fields.update(cur_extracted_key_fields)

    def invoke(self, inputs: Input, **kwargs) -> Output:
        pass

    async def await_by_stream_callback(self, stream_callback, answer, span: Span):
        """适配流式以及非流式调用"""
        if stream_callback:
            answer = answer.get("answer")
        stream_related_info = WorkflowStreamingData(
            stream_callback=stream_callback, execution_id=span.trace_id
        )
        # 调用父类await_send_format_output发送交互信息
        struct_answer_item = (
            dict(
                struct_output_template=self.struct_output_template,
                variables=self.inputs,
            )
            if self.enable_struct_message and self.struct_output_template
            else {}
        )
        await self.await_send_format_output(
            origin_output=answer,
            struct_answer_item=struct_answer_item,
            metadata=self.metadata,
            stream_data=stream_related_info,
            chat_manager=self.chat_manager,
        )

    async def reply_directly_without_extraction(
        self, inputs, debug_info, stream_callback, check_hash_content=False
    ):
        """
        Processing branch of replying directly without key fields extractions.
        """
        question = self.sub_placeholder(
            copy(self.questioner_config.question_content), inputs
        )

        # 检查是否需要处理'#'号逻辑
        if check_hash_content and self._is_only_hash_content(question):
            question = self._get_latest_non_hash_response()

        question_format = format_interaction_res(
            result=question, node_type=NodeType.QUESTIONER.value
        )

        questioner_output = QuestionerOutput(question=question)
        await self.await_by_stream_callback(
            stream_callback, question_format, debug_info.get("span")
        )
        return questioner_output

    async def reply_directly_with_extraction_question(
        self, inputs, debug_info: dict, stream_callback
    ):
        """
        Accessing the pre-defined question content in processing branch of replying directly with
         key fields extractions.
        """
        if self.questioner_config.question_content:
            question = self.sub_placeholder(
                copy(self.questioner_config.question_content), inputs
            )
            await process_on_invoke_info(
                component_metadata=debug_info.get("component_metadata"),
                runtime_context=debug_info.get("runtime_context"),
                data=dict(assistant=question),
                span=debug_info.get("span"),
            )
            question_format = format_interaction_res(
                result=question, node_type=NodeType.QUESTIONER.value
            )
            self.node_state.response_num += 1
            await self.await_by_stream_callback(
                stream_callback, question_format, debug_info.get("span")
            )
            return question

    async def reply_directly_with_extraction_rule_base_start(
        self, debug_info, stream_callback
    ):
        """
        Construct question in rule-based approach in processing branch of replying directly with
         key fields extractions.
        """
        await self.update_key_fields(debug_info)
        if self.node_state.response_num < self.questioner_config.max_response:
            questioner_output = QuestionerOutput()
            if self.get_extract_all_key_fields():
                question = self.construct_question_based_on_rule()
                await process_on_invoke_info(
                    component_metadata=debug_info.get("component_metadata"),
                    runtime_context=debug_info.get("runtime_context"),
                    data=dict(assistant=question),
                    span=debug_info.get("span"),
                )
                question_format = format_interaction_res(
                    result=question, node_type=NodeType.QUESTIONER.value
                )
                questioner_output.question = question
                self.node_state.response_num += 1
                self.node_state.status = ExecutionStatus.USER_INTERACT
                await self.await_by_stream_callback(
                    stream_callback, question_format, debug_info.get("span")
                )
            else:
                self.node_state.status = ExecutionStatus.END
            if self.component_config.get("userFields") and self.component_config.get(
                "userFields"
            ).get("outputs"):
                self.node_state.extracted_key_fields = Type.transform_questioner_type(
                    self.component_config["userFields"]["outputs"],
                    self.node_state.extracted_key_fields,
                )
            questioner_output.update_key_field(self.node_state.extracted_key_fields)
            return questioner_output
        raise JiuWenBaseException(
            message=StatusCode.WORKFLOW_QUESTIONER_EXCEED_LOOP.errmsg,
            error_code=StatusCode.WORKFLOW_QUESTIONER_EXCEED_LOOP.code,
        )

    async def reply_directly_with_extraction_rule_base_interact(
        self, debug_info, stream_callback
    ):
        """
        Construct question in rule-based approach in processing branch of replying directly with
         key fields extractions.
        """
        if self.node_state.response_num < self.questioner_config.max_response:
            questioner_output = QuestionerOutput()
            await self.update_key_fields(debug_info)
            if self.get_extract_all_key_fields():
                question = self.construct_question_based_on_rule()
                await process_on_invoke_info(
                    component_metadata=debug_info.get("component_metadata"),
                    runtime_context=debug_info.get("runtime_context"),
                    data=dict(assistant=question),
                    span=debug_info.get("span"),
                )
                question_format = format_interaction_res(
                    result=question, node_type=NodeType.QUESTIONER.value
                )
                questioner_output.question = question
                self.node_state.response_num += 1
                await self.await_by_stream_callback(
                    stream_callback, question_format, debug_info.get("span")
                )
            else:
                self.node_state.status = ExecutionStatus.END
            if self.component_config.get("userFields") and self.component_config.get(
                "userFields"
            ).get("outputs"):
                self.node_state.extracted_key_fields = Type.transform_questioner_type(
                    self.component_config["userFields"]["outputs"],
                    self.node_state.extracted_key_fields,
                )
            questioner_output.update_key_field(self.node_state.extracted_key_fields)
            return questioner_output
        raise JiuWenBaseException(
            message=StatusCode.WORKFLOW_QUESTIONER_EXCEED_LOOP.errmsg,
            error_code=StatusCode.WORKFLOW_QUESTIONER_EXCEED_LOOP.code,
        )

    async def ainvoke(self, inputs: dict[str, Any], **kwargs):
        """
        Questioner ainvoke
        """
        self.conversation_id = self.runtime_context.get(
            "workflow_execute_debug_info", {}
        ).get("workflow_conversation_id", "")
        logger.info(f"conversation_id: {self.conversation_id}, questioner start invoke")
        inputs = inputs.get(USER_FIELDS)
        stream_callback: WorkflowStreamCallback = kwargs.get("stream_callback")
        # initial user response with the most recent one
        conv_history = self.get_latest_chat_history()
        user_intent = self.get_latest_user_query_intent()
        logger.info(
            f"conversation_id: {self.conversation_id}, questioner invoke with chat history {conv_history}",
            simple_log=f"conversation_id: {self.conversation_id}, questioner invoke with chat history",
        )
        self.user_response = (
            conv_history[-1].get(QuestionerKeyword.CONTENT) if conv_history else ""
        )
        debug_info = dict(
            component_metadata=kwargs.get("component_metadata", {}),
            runtime_context=kwargs.get("runtime_context", {}),
            span=kwargs.get("span"),
        )
        await process_on_invoke_info(
            component_metadata=debug_info.get("component_metadata"),
            runtime_context=debug_info.get("runtime_context"),
            data=dict(user=self.user_response),
            span=debug_info.get("span"),
        )

        if self.node_state.status == ExecutionStatus.START:
            if self.questioner_config.response_type == ResponseType.ReplyDirectly.value:
                self.inputs = inputs
                # 有预设问题时直接返回预设问题
                if self.questioner_config.question_content:
                    self.node_state.status = ExecutionStatus.USER_INTERACT
                    res = await self.reply_directly_without_extraction(
                        inputs, debug_info, stream_callback
                    )
                    logger.info(
                        f"conversation_id: {self.conversation_id}, questioner return response: {res}",
                        simple_log=f"conversation_id: {self.conversation_id}, questioner return response successfully",
                    )
                    self.node_state.inputs = inputs
                    return {USER_FIELDS: res.as_dict()}
                # 直接回答，不提取参数时，未获取到开发者预设问题时抛出异常
                if (
                    not self.questioner_config.extract_fields_from_response
                    and not self.questioner_config.question_content
                ):
                    raise JiuWenBaseException(
                        message=StatusCode.WORKFLOW_QUESTIONER_QUESTION_EMPTY_DIRECT_COLLECTION.errmsg,
                        error_code=StatusCode.WORKFLOW_QUESTIONER_QUESTION_EMPTY_DIRECT_COLLECTION.code,
                    )

                # 从用户输入中提取参数
                if (
                    self.questioner_config.question_construction_method
                    == QuestionConstructionMethod.RuleBased.value
                ):
                    res = await self.reply_directly_with_extraction_rule_base_start(
                        debug_info, stream_callback
                    )
                    return {USER_FIELDS: res.as_dict()}
                if (
                    self.questioner_config.question_construction_method
                    == QuestionConstructionMethod.LLMBased.value
                ):
                    # implement logic that extract key fields from user response with rule-based method
                    raise NotImplementedError()
                raise JiuWenBaseException(
                    message=StatusCode.WORKFLOW_QUESTIONER_INVALID_QUESTION_METHOD.errmsg,
                    error_code=StatusCode.WORKFLOW_QUESTIONER_INVALID_QUESTION_METHOD.code,
                )
            if (
                self.questioner_config.response_type
                == ResponseType.ReplyBasedOnOptions.value
            ):
                # implement logic that ask user question based on options
                raise NotImplementedError()
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_QUESTIONER_INVALID_RESPONSE.errmsg,
                error_code=StatusCode.WORKFLOW_QUESTIONER_INVALID_RESPONSE.code,
            )
        if self.node_state.status == ExecutionStatus.USER_INTERACT:
            if self.questioner_config.response_type == ResponseType.ReplyDirectly.value:
                self.inputs = inputs
                # reply directly without extraction params
                if not self.questioner_config.extract_fields_from_response:
                    if user_intent and len(user_intent) > 1:
                        # 说明query已经被使用
                        inputs = self.node_state.inputs or {}
                        res = await self.reply_directly_without_extraction(
                            inputs, debug_info, stream_callback, True
                        )
                        self.node_state.status = ExecutionStatus.USER_INTERACT
                        return {USER_FIELDS: res.as_dict()}
                    questioner_output = QuestionerOutput(
                        user_response=self.user_response
                    ).as_dict()
                    self.node_state.status = ExecutionStatus.END
                    return {USER_FIELDS: questioner_output}

                # reply directly with extraction params
                if (
                    self.questioner_config.question_construction_method
                    == QuestionConstructionMethod.RuleBased.value
                ):
                    res = await self.reply_directly_with_extraction_rule_base_interact(
                        debug_info, stream_callback
                    )
                    return {USER_FIELDS: res.as_dict()}
                if (
                    self.questioner_config.question_construction_method
                    == QuestionConstructionMethod.LLMBased.value
                ):
                    # implement logic that extract key fields from user response with rule-based method
                    raise NotImplementedError()
                raise JiuWenBaseException(
                    message=StatusCode.WORKFLOW_QUESTIONER_INVALID_QUESTION_METHOD.errmsg,
                    error_code=StatusCode.WORKFLOW_QUESTIONER_INVALID_QUESTION_METHOD.code,
                )
        raise JiuWenBaseException(
            message=StatusCode.WORKFLOW_QUESTIONER_INVALID_QUESTION_METHOD.errmsg,
            error_code=StatusCode.WORKFLOW_QUESTIONER_INVALID_QUESTION_METHOD.code,
        )

    async def _invoke_llm_with_insights(self, inputs, debug_info):
        await process_on_invoke_info(
            component_metadata={},
            runtime_context=self.runtime_context,
            data={LLM_INPUTS: inputs},
            span=debug_info.get("span"),
        )
        new_response = await self.llm.ainvoke(inputs)
        new_response = new_response.content
        await process_on_invoke_info(
            component_metadata={},
            runtime_context=self.runtime_context,
            data={LLM_OUTPUTS: new_response},
            span=debug_info.get("span"),
        )
        return new_response

    def _is_only_hash_content(self, question):
        """检查返回结果是否只包含'#'号"""
        if not question:
            return False

        # 去除空白字符后检查是否只包含'#'号
        content = question.strip()
        return len(content) > 0 and all(char == "#" for char in content)

    def _get_latest_non_hash_response(self):
        """从对话历史中获取最近一条不是'#'的内容"""
        chat_history = self.runtime_context.get(WORKFLOW_CHAT_HISTORY, None)
        if chat_history:
            chat_history = chat_history.get_conversation_history()
        else:
            chat_history = []

        # 从后往前找最后一条role为"user"的记录
        for chat in reversed(chat_history):
            content = chat.get(QuestionerKeyword.CONTENT, "").strip()
            role = chat.get("role", "").strip()

            # 检查role为assistant且内容不是只有'#'号
            if (
                role == "assistant"
                and content
                and not self._is_only_hash_content(content)
            ):
                return content

        # 如果没有找到符合条件的内容，返回空字符串
        return ""
