#!/usr/bin/python3.9
# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved
"""
@Copyright: Copyright (C) 2023-2024 Huawei Inc
@Author: jiuwen
@Description: Extractor
"""

import json
from copy import deepcopy
from dataclasses import dataclass, field
from enum import Enum
from typing import Any

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.controller.task_planner.planning_modules.prompt_module import ChatContent
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.components.util import (
    process_on_invoke_info,
    filter_enable_history,
)
from jiuwen.orchestration.flow.constant import (
    WORKFLOW_CHAT_MANAGER,
    WORKFLOW_LLM_EXTRA_CONFIGS,
    WORKFLOW_CHAT_HISTORY,
    ExtractorKeyword,
    USER_FIELDS,
)
from jiuwen.orchestration.utils import Input, Output
from jiuwen.planner.planning_modules.base import PromptPlanningModule
from jiuwen.prompt import TemplateManager


class Role(Enum):
    USER = "user"
    ASSISTANT = "assistant"


@dataclass
class ModelConfig:
    name: str
    source: str
    url: str


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
    WITH_CHAT_HISTORY = "with_chat_history"
    CHAT_HISTORY_MAX_ROUNDS = "chat_history_max_rounds"
    PROMPT_TEMPLATE = "prompt_template"
    EXAMPLE_CONTENT = "example_content"
    MODEL = "model"
    MODEL_NAME = "modelName"
    MODEL_TYPE = "modelType"
    HYPER_PARAMETERS = "hyper_parameters"
    TOP_P = "top_p"


@dataclass
class ExtractorField:
    extracted_key_fields: dict = field(default_factory=dict)
    user_response: str = ""
    inputs: dict = field(default_factory=dict)


@dataclass
class ExtractorConfig:
    model_name: str = "ei-agentBuilder-38b-v24"
    model_type: str = "poc_agentBuilder"
    with_chat_history: bool = False
    chat_history_max_rounds: int = 0
    extra_prompt_for_fields_extraction: str = None
    question_content: str = None
    cn_fields_name: dict = field(default_factory=dict)
    option_content: list = field(default_factory=list)
    key_fields: list[dict] = field(default_factory=list)
    hyper_parameters: dict = None
    extension: dict = None
    prompt_template: list[dict] = None
    example_content: str = ExtractorKeyword.DEFAULT_SHOT


@dataclass
class ExtractorOutput:
    """
    define data class of extractor component
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
        transfer ExtractorOutput to dict and output it
        """
        extractor_output = {}
        if self.user_response:
            extractor_output[ExtractorKeyword.USER_RESPONSE] = self.user_response
        if self.question:
            extractor_output[ExtractorKeyword.QUESTION] = self.question
        extractor_output.update(self.key_fields)
        return extractor_output


LLM_INPUTS = "llm_inputs"
LLM_OUTPUTS = "llm_outputs"
DEFAULT_ERROR_INFO = {"message": "", "error_code": 0}


def format_extractor_configs(configs: dict) -> dict:
    """
    Preprocessing compatibility configuration information
    """
    formatted_extractor_configs = {}

    # llm config
    if isinstance(configs.get(ConfigCurrent.MODEL.value), dict) and configs.get(
        ConfigCurrent.MODEL.value, {}
    ).get(ConfigCurrent.MODEL_NAME.value, ""):
        formatted_extractor_configs[ExtractorKeyword.MODEL_NAME] = configs.get(
            ConfigCurrent.MODEL.value, {}
        ).get(ConfigCurrent.MODEL_NAME.value, "")
    else:
        formatted_extractor_configs[ExtractorKeyword.MODEL_NAME] = configs.get(
            ConfigPrevious.MODEL.value
        )
    if isinstance(configs.get(ConfigCurrent.MODEL.value), dict) and configs.get(
        ConfigCurrent.MODEL.value, {}
    ).get(ConfigCurrent.MODEL_TYPE.value, ""):
        formatted_extractor_configs[ExtractorKeyword.MODEL_TYPE] = configs.get(
            ConfigCurrent.MODEL.value, {}
        ).get(ConfigCurrent.MODEL_TYPE.value, "")
    formatted_extractor_configs[ExtractorKeyword.HYPER_PARAMETERS] = configs.get(
        ExtractorKeyword.MODEL, {}
    ).get(ExtractorKeyword.HYPER_PARAMETERS_CANAL, {})
    top_p = (
        configs.get(ExtractorKeyword.MODEL, {})
        .get(ExtractorKeyword.HYPER_PARAMETERS_CANAL, {})
        .get(ConfigCurrent.TOP_P.value, 0.15)
    )
    temperature = (
        configs.get(ExtractorKeyword.MODEL, {})
        .get(ExtractorKeyword.HYPER_PARAMETERS_CANAL, {})
        .get(ConfigCurrent.TEMPERATURE.value, 0.1)
    )
    formatted_extractor_configs[ExtractorKeyword.HYPER_PARAMETERS][
        ExtractorKeyword.TOP_P_KEYWORD
    ] = top_p
    formatted_extractor_configs[ExtractorKeyword.HYPER_PARAMETERS][
        ConfigCurrent.TEMPERATURE.value
    ] = temperature
    formatted_extractor_configs["extension"] = configs.get(
        ExtractorKeyword.MODEL, {}
    ).get("extension", {})

    # other config
    formatted_extractor_configs[ConfigCurrent.QUESTION_CONTENT.value] = configs.get(
        ConfigCurrent.QUESTION_CONTENT.value, configs.get(ConfigPrevious.QUESTION.value)
    )
    if ConfigCurrent.EXTRA_PROMPT_FOR_FIELDS_EXTRACTION.value in configs:
        formatted_extractor_configs[
            ConfigCurrent.EXTRA_PROMPT_FOR_FIELDS_EXTRACTION.value
        ] = configs.get(ConfigCurrent.EXTRA_PROMPT_FOR_FIELDS_EXTRACTION.value)

    if configs.get(ConfigCurrent.FIELD_NAMES.value):
        formatted_extractor_configs[ExtractorKeyword.CN_FIELDS_NAME] = {
            item.get(ExtractorKeyword.FIELD_NAME): item.get(
                ExtractorKeyword.CN_FIELD_NAME
            )
            for item in configs.get(ConfigCurrent.FIELD_NAMES.value)
        }
        formatted_extractor_configs[ExtractorKeyword.KEY_FIELDS] = [
            {
                ExtractorKeyword.NAME: item.get(ExtractorKeyword.FIELD_NAME, ""),
                ExtractorKeyword.DESC: item.get(ExtractorKeyword.DESCRIPTION, ""),
                "default_value": item.get("default_value"),
            }
            for item in configs.get(ConfigCurrent.FIELD_NAMES.value)
        ]
    elif configs.get(ConfigPrevious.EXTRACONFIG.value):
        formatted_extractor_configs[ExtractorKeyword.CN_FIELDS_NAME] = {
            item.get(ExtractorKeyword.NAME): item.get(ExtractorKeyword.QUESTIONKEYWORD)
            for item in configs.get(ConfigPrevious.EXTRACONFIG.value)
        }
        formatted_extractor_configs[ExtractorKeyword.KEY_FIELDS] = [
            {
                ExtractorKeyword.NAME: item.get(ExtractorKeyword.NAME, ""),
                ExtractorKeyword.DESC: item.get(ExtractorKeyword.DESCRIPTION, ""),
            }
            for item in configs.get(ConfigPrevious.EXTRACONFIG.value)
        ]

    if ConfigCurrent.WITH_CHAT_HISTORY.value in configs:
        formatted_extractor_configs[ConfigCurrent.WITH_CHAT_HISTORY.value] = (
            configs.get(ConfigCurrent.WITH_CHAT_HISTORY.value)
        )
    if formatted_extractor_configs.get(ConfigCurrent.WITH_CHAT_HISTORY.value):
        formatted_extractor_configs[ExtractorKeyword.CHAT_HISTORY_MAX_ROUNDS] = (
            configs.get(ConfigCurrent.CHAT_HISTORY_MAX_ROUNDS.value)
        )

    if ConfigCurrent.PROMPT_TEMPLATE.value in configs:
        formatted_extractor_configs[ConfigCurrent.PROMPT_TEMPLATE.value] = configs.get(
            ConfigCurrent.PROMPT_TEMPLATE.value
        )

    if configs.get(ConfigCurrent.EXAMPLE_CONTENT.value):
        formatted_extractor_configs[ConfigCurrent.EXAMPLE_CONTENT.value] = configs.get(
            ConfigCurrent.EXAMPLE_CONTENT.value
        )

    return formatted_extractor_configs


class Extractor(Invokable):
    """
    Extractor is a sub class of invokable, as a type of node in workflow
    """

    def __init__(self):
        """
        Initialization of Extractor
        """
        super().__init__()
        self.component_config = None
        self.runtime_context = None
        self._workflow_llm_extra_configs = {}
        self.model_map = {}
        self.inputs = dict()
        self.user_response = ""
        self.metadata = None
        self.extractor_config = ExtractorConfig()
        self.chat_manager = None
        self.llm = None
        self.prompt_engine = None
        self.node_field = ExtractorField()
        self.error_message = DEFAULT_ERROR_INFO
        self.conversation_id = None

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
                    "Prompt Template should be a list in extractor configuration.",
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
                        "Prompt Template should be with role and content in extractor configuration.",
                    )
        return False, ""

    def init(self, conf: dict, **kwargs):
        """
        Initialization of Extractor
        """
        # initialize extractor state
        self.node_field = ExtractorField()
        runtime_context = kwargs.get("runtime_context")
        metadata = kwargs.get("metadata")
        self.component_config = deepcopy(conf)
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

        # initialize extractor config
        self.extractor_config = ExtractorConfig(**format_extractor_configs(conf))

        # get chat manager for interact with user
        self.chat_manager = runtime_context.get(WORKFLOW_CHAT_MANAGER)
        # initialize llm service for extract key fields
        self.llm = ModelFactory().get_model(
            model_type=self.extractor_config.model_type,
            model_name=self.extractor_config.model_name,
            runtime_context=self.runtime_context,
            **self.extractor_config.hyper_parameters,
            **self.extractor_config.extension,
        )

        # initialize extractor state
        self.node_field = ExtractorField()

        # initial prompt
        self.initial_prompt()

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
            return True, "Model name or model type is not in extractor configuration."
        temperature = (
            conf.get("model", {}).get("hyperParameters", {}).get("temperature", 0)
        )
        match_temperature = not isinstance(temperature, (int, float))
        if match_temperature:
            return True, "Temperature is illegal in extractor configuration."

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
            return True, "Extra prompt is illegal in extractor configuration."

        # check question content. string
        question_content_match = conf.get("question_content") is not None and (
            not isinstance(conf.get("question_content"), str)
        )
        if question_content_match:
            return True, "Question content is illegal in extractor configuration."

        # check bool configuration
        valid_types = (bool, type(None))
        for key in [
            "input_complement",
            "with_chat_history",
            "extract_fields_from_response",
        ]:
            if not isinstance(conf.get(key), valid_types):
                return True, "{key} is illegal in extractor configuration.".format(
                    key=key
                )

        # check field names. List. cn_field_name, description and field_name should be in field_names
        if not isinstance(conf.get("field_names"), (list, type(None))):
            return True, "field_names should be a list in extractor configuration."
        for unit in conf.get("field_names", []):
            if not all(
                key in unit for key in ["field_name", "description", "cn_field_name"]
            ):
                return (
                    True,
                    "field_name, description and cn_field_name should be in field_names "
                    "in extractor configuration.",
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

    def initial_prompt(self):
        """
        initial prompt
        """
        # initialize inference prompt
        self.prompt_engine = PromptPlanningModule()
        if self.extractor_config.prompt_template:
            self.prompt = self.extractor_config.prompt_template
        else:
            prompt = TemplateManager().get(
                name="flow_extractor",
                filters={"model_name": self.extractor_config.model_name},
            )
            if prompt is not None and hasattr(prompt, "content"):
                self.prompt = prompt.content
            else:
                raise JiuWenBaseException(
                    message=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR.errmsg,
                    error_code=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR.code,
                )

    async def create_prompt_template_input(self, chat_history: list) -> dict:
        """
        create input for prompt template
        """
        required_params = []
        for i, param in enumerate(self.extractor_config.key_fields):
            number = i + 1
            required_params.append(
                "变量{number}名称：{name}，变量{number}的描述：{desc} ".format(
                    number=str(number),
                    name=param.get(ExtractorKeyword.NAME, ""),
                    desc=param.get("desc", ""),
                )
            )

        required_name = []
        for _, item in self.extractor_config.cn_fields_name.items():
            required_name.append(item)
        required_name = (
            "、".join(required_name)
            + f"{len(self.extractor_config.cn_fields_name.items())}个必要信息"
        )

        if self.user_response:
            user_response = self.user_response
        elif chat_history:  # 增加判断，如果 chat_history 不为空
            user_response = chat_history[-1].get(ExtractorKeyword.CONTENT)
        else:
            # 处理 chat_history 和 user_response 都为空的情况，例如赋值为""
            user_response = ""

        required_params_list = []
        for _, param in enumerate(self.extractor_config.key_fields):
            required_params_list.append(
                "{name}:{desc} ".format(
                    name=param.get(ExtractorKeyword.NAME, ""),
                    desc=param.get("desc", ""),
                )
            )
        required_params_list = "\n".join(required_params_list)

        chat_history = filter_enable_history(chat_history)
        dig_history = "\n".join(
            [
                "{role}：{content}".format(
                    role=unit.get(ExtractorKeyword.ROLE),
                    content=unit.get(ExtractorKeyword.CONTENT),
                )
                for unit in chat_history[:-1]
                + [{"role": "user", "content": user_response}]
            ]
        )

        extra_info = (
            self.extractor_config.extra_prompt_for_fields_extraction
            if self.extractor_config.extra_prompt_for_fields_extraction
            else ""
        )
        # Placeholder Filling
        required_params = " \n " + " \n ".join(required_params)

        return {
            "required_params": required_params,
            "example": self.extractor_config.example_content,
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
                role=p.get(ExtractorKeyword.ROLE),
                content=p.get(ExtractorKeyword.CONTENT),
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
            # json.loads() parse JSON
            parsed_data = json.loads(response)

            cur_extracted_key_fields = {
                k: v if v is not None and str(v) else "" for k, v in parsed_data.items()
            }

            return cur_extracted_key_fields

        except json.JSONDecodeError as e:
            # parse JSON failed
            logger.error(
                f"Failed to parse LLM response as JSON: {e}",
                simple_log="Failed to parse LLM response as JSON",
            )
            return {}
        except Exception as e:
            # unknown error
            logger.error(
                f"An unknown error occurred during parsing: {e}",
                simple_log="An unknown error occurred during parsing",
            )
            return {}

    def get_latest_chat_history(self, k=None):
        """
        get the latest chat history from runtime_context
        """
        if not self.extractor_config.with_chat_history:
            k = 0
        chat_history = deepcopy(self.runtime_context.get(WORKFLOW_CHAT_HISTORY, None))
        if chat_history:
            chat_history = chat_history.get_conversation_history()
        else:
            chat_history = []
        chat_history = self.get_latest_k_rounds_chat(chat_history, k)
        # get the latest k chat history
        for history in chat_history:
            history.update(
                {ExtractorKeyword.CONTENT: history.get(ExtractorKeyword.CONTENT)}
            )
        return chat_history

    async def update_key_fields(self, debug_info):
        """
        updating key fields
        """
        chat_history = self.get_latest_chat_history(
            k=self.extractor_config.chat_history_max_rounds
        )
        cur_extracted_key_fields = await self.extract_key_fields(
            chat_history, debug_info
        )
        cur_extracted_key_fields = {
            k: v
            for k, v in cur_extracted_key_fields.items()
            if k in self.extractor_config.cn_fields_name
        }
        self.node_field.extracted_key_fields.update(cur_extracted_key_fields)

    def invoke(self, inputs: Input, **kwargs) -> Output:
        pass

    async def reply_key_fields(self, debug_info):
        """
        Reply key fields output.
        """
        extractor_output = ExtractorOutput(user_response=self.user_response)
        await self.update_key_fields(debug_info)
        extractor_output.update_key_field(self.node_field.extracted_key_fields)
        return extractor_output

    async def ainvoke(self, inputs: dict[str, Any], **kwargs):
        """
        Extractor ainvoke
        """
        self.conversation_id = self.runtime_context.get(
            "workflow_execute_debug_info", {}
        ).get("workflow_conversation_id", "")
        logger.info(
            f"conversation_id: {self.conversation_id}, extractor start invoke",
            simple_log="extractor start invoke",
        )
        inputs = inputs.get(USER_FIELDS)
        # initial user response with the most recent one
        conv_history = self.get_latest_chat_history()
        logger.info(
            f"conversation_id: {self.conversation_id}, extractor invoke with chat history {conv_history}",
            simple_log="extractor invoke with chat history",
        )
        self.user_response = (
            conv_history[-1].get(ExtractorKeyword.CONTENT) if conv_history else ""
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
        self.inputs = inputs
        # 从用户输入中提取参数
        res = await self.reply_key_fields(debug_info)
        return {USER_FIELDS: res.as_dict()}

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
