#!/usr/bin/python3.11
# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved
"""
@Copyright: Copyright (C) 2025-2026 Huawei Inc
@Author: EI
@Description: Agent Node
"""

import ast
import re
from copy import deepcopy
from dataclasses import dataclass
from enum import Enum
from typing import Any

from i18n.language_processer import LanguageManager
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.common.utils.nl2json import NL2JSONProcessor
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.callbacks.tracer import Span
from jiuwen.orchestration.flow.components.interactive_base import InteractiveComponent
from jiuwen.orchestration.flow.components.util import (
    format_interaction_res,
    process_on_invoke_info,
    process_output_of_interaction_component,
)
from jiuwen.orchestration.flow.constant import (
    QuestionerKeyword,
    USER_FIELDS,
    WORKFLOW_API_CONFIGS,
    WORKFLOW_CHAT_HISTORY,
    WORKFLOW_CHAT_MANAGER,
    WORKFLOW_LLM_EXTRA_CONFIGS,
)
from jiuwen.orchestration.flow.enum import ExecutionStatus, NodeType
from jiuwen.orchestration.flow.model.workflow_data_class import (
    WorkflowMetadata,
    WorkflowStreamingData,
)
from jiuwen.orchestration.flow.state import State
from jiuwen.orchestration.flow.stream.workflow_streaming import WorkflowStreamCallback
from jiuwen.orchestration.utils import Input, Output
from jiuwen.planner.planning_modules.base import ChatContent, PromptPlanningModule
from jiuwen.plugin import PluginManager
from jiuwen.plugin.common.utils.ir_utils import PluginIRConverter
from jiuwen.prompt import TemplateManager

LLM_INPUTS = "llm_inputs"
LLM_OUTPUTS = "llm_outputs"
DEFAULT_ERROR_INFO = {"message": "", "error_code": 0}


def refix_llm_output(input_str):
    res = input_str
    json_pattern = r"\{.*\}"
    match = re.search(json_pattern, input_str, re.DOTALL)
    if match:
        res = match.group(0)
        res = (
            res.replace("false", "False")
            .replace("true", "True")
            .replace("null", "None")
        )
    else:
        return input_str
    if "</cot>" in res:
        tmp = res.split("</cot>")
        res = tmp[-1]
    return res


class ConfigPrevious(Enum):
    TEMPLATE_CONTENT = "templateContent"
    TEMPERATURE = "temperature"
    QUESTION = "question"
    EXTRACONFIG = "extraConfig"
    LLM_MODEL = "llm_model"
    MODEL = "model"
    MODEL_NAME = "model_name"


class ConfigCurrent(Enum):
    TEMPERATURE = "temperature"
    WITH_CHAT_HISTORY = "with_chat_history"  # 默认携带历史对话
    MAX_RESPONSE = "max_iteration"  # 轮次
    PROMPT_TEMPLATE = "prompt_template"
    MODEL = "model"
    MODEL_NAME = "modelName"
    MODEL_TYPE = "modelType"
    HYPER_PARAMETERS = "hyper_parameters"
    TOP_P = "top_p"
    ALLOW_NODE_BREAK = "enable_intent_break"  # 意图退出
    SYSTEM_PROMPT = "system_prompt"  # 系统提示词
    BREAK_PLUGIN_IDS = "break_plugin_ids"  # 结束插件列表
    PLUGIN_LIST = "plugins"  # 插件列表


@dataclass
class AgentState(State):
    user_response: str = ""
    response_num: int = 0
    status: ExecutionStatus = ExecutionStatus.START
    tool_choose: str = ""
    extracted_key_fields: dict = None
    tool_result: list[dict] = None
    tool_result_summary: list[dict] = None
    cur_tool_name: str = ""


@dataclass
class AgentConfig:
    model_name: str
    model_type: str = "agentBuilder"
    chat_history_max_rounds: int = 10  # 默认历史对话轮次
    with_chat_history: bool = True
    max_iteration: int = 20
    hyper_parameters: dict = None
    extension: dict = None
    prompt_template: list[dict] = None
    enable_intent_break: bool = False  # 前端开关
    user_break: bool = False
    plugins: list[dict] = None
    system_prompt: str = ""
    break_plugin_ids: list[str] = None


def format_agent_configs(configs: dict) -> dict:
    """
    Preprocessing compatibility configuration information
    """
    formatted_agent_configs = {}

    # llm config
    if isinstance(configs.get(ConfigCurrent.MODEL.value), dict) and configs.get(
        ConfigCurrent.MODEL.value, {}
    ).get(ConfigCurrent.MODEL_NAME.value, ""):
        formatted_agent_configs[QuestionerKeyword.MODEL_NAME] = configs.get(
            ConfigCurrent.MODEL.value, {}
        ).get(ConfigCurrent.MODEL_NAME.value, "")
    else:
        formatted_agent_configs[QuestionerKeyword.MODEL_NAME] = configs.get(
            ConfigPrevious.MODEL.value
        )
    if isinstance(configs.get(ConfigCurrent.MODEL.value), dict) and configs.get(
        ConfigCurrent.MODEL.value, {}
    ).get(ConfigCurrent.MODEL_TYPE.value, ""):
        formatted_agent_configs[QuestionerKeyword.MODEL_TYPE] = configs.get(
            ConfigCurrent.MODEL.value, {}
        ).get(ConfigCurrent.MODEL_TYPE.value, "")
    formatted_agent_configs[QuestionerKeyword.HYPER_PARAMETERS] = configs.get(
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
    formatted_agent_configs[QuestionerKeyword.HYPER_PARAMETERS][
        QuestionerKeyword.TOP_P_KEYWORD
    ] = top_p
    formatted_agent_configs[QuestionerKeyword.HYPER_PARAMETERS][
        ConfigCurrent.TEMPERATURE.value
    ] = temperature
    formatted_agent_configs["extension"] = configs.get(QuestionerKeyword.MODEL, {}).get(
        "extension", {}
    )

    # other config
    if ConfigCurrent.ALLOW_NODE_BREAK.value in configs:
        formatted_agent_configs[ConfigCurrent.ALLOW_NODE_BREAK.value] = configs.get(
            ConfigCurrent.ALLOW_NODE_BREAK.value
        )

    if formatted_agent_configs.get(ConfigCurrent.WITH_CHAT_HISTORY.value):
        formatted_agent_configs[QuestionerKeyword.CHAT_HISTORY_MAX_ROUNDS] = (
            QuestionerKeyword.CHAT_HISTORY_MAX_ROUNDS_NUMBER
        )

    if ConfigCurrent.MAX_RESPONSE.value in configs:
        formatted_agent_configs[ConfigCurrent.MAX_RESPONSE.value] = configs.get(
            ConfigCurrent.MAX_RESPONSE.value
        )

    if ConfigCurrent.PROMPT_TEMPLATE.value in configs:
        formatted_agent_configs[ConfigCurrent.PROMPT_TEMPLATE.value] = configs.get(
            ConfigCurrent.PROMPT_TEMPLATE.value
        )

    if ConfigCurrent.SYSTEM_PROMPT.value in configs:
        formatted_agent_configs[ConfigCurrent.SYSTEM_PROMPT.value] = configs.get(
            ConfigCurrent.SYSTEM_PROMPT.value
        )

    if ConfigCurrent.BREAK_PLUGIN_IDS.value in configs:
        formatted_agent_configs[ConfigCurrent.BREAK_PLUGIN_IDS.value] = configs.get(
            ConfigCurrent.BREAK_PLUGIN_IDS.value
        )

    if ConfigCurrent.PLUGIN_LIST.value in configs:
        formatted_agent_configs[ConfigCurrent.PLUGIN_LIST.value] = configs.get(
            ConfigCurrent.PLUGIN_LIST.value
        )

    if ConfigCurrent.WITH_CHAT_HISTORY.value in configs:
        formatted_agent_configs[ConfigCurrent.WITH_CHAT_HISTORY.value] = configs.get(
            ConfigCurrent.WITH_CHAT_HISTORY.value
        )

    return formatted_agent_configs


class Agent(Invokable, InteractiveComponent):
    """
    Agent is a sub class of invokable, as a type of node in workflow
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
        self.agent_config = AgentConfig("")
        self.chat_manager = None
        self.llm = None
        self.node_state = AgentState()
        self.error_message = DEFAULT_ERROR_INFO
        self.plugins = []
        self.plugins_list = []
        self.language = ""
        self.language_config = dict()
        super().__init__()

    def init(self, conf: dict, **kwargs):
        """
        Initialization of Agent
        """
        if self.node_state.status == ExecutionStatus.USER_INTERACT:
            return
        # initialize questioner state
        self.node_state = AgentState()
        runtime_context = kwargs.get("runtime_context")
        metadata = kwargs.get("metadata")
        self.component_config = deepcopy(conf)
        conf = self.dict_keys_camel_to_snake(conf)  # ir内容
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

        # 获取语言配置
        language = (
            runtime_context.get("workflow_execute_requests_headers_name", {})
            .get("cust_headers", {})
            .get("X-Language")
        )
        if language:
            self.language = language
        else:
            self.language = "zh-cn"

        self.language_config = LanguageManager().get_language_context_for_node(
            self.language, "agent"
        )

        # initialize agent config
        self.agent_config = AgentConfig(**format_agent_configs(conf))
        self.plugins_list = self.agent_config.plugins
        self.plugins = self._get_plugin_list_from_config(self.agent_config)
        self.plugins_dict = {item.name: item for item in self.plugins}
        # get chat manager for interact with user
        self.chat_manager = runtime_context.get(WORKFLOW_CHAT_MANAGER)
        # initialize llm service for extract key fields
        self.llm = ModelFactory().get_model(
            model_type=self.agent_config.model_type,
            model_name=self.agent_config.model_name,
            runtime_context=self.runtime_context,
            **self.agent_config.hyper_parameters,
            **self.agent_config.extension,
        )

        # initialize agent state
        self.node_state = AgentState()

        # initial prompt
        self.initial_prompt()

    def should_interrupt(self) -> bool:
        """
        check if should be interrupted

        Returns:
            bool: The result of the interruption.
        """
        return self.node_state.status == ExecutionStatus.USER_INTERACT

    @staticmethod
    def _get_plugin_list_from_config(conf):
        plugin_conf_list = conf.plugins or []
        plugin_instance_list = []
        plugin_ids = []
        for plugin_conf in plugin_conf_list:
            if "url" in plugin_conf:
                # 有url表示此plugin_conf为完整的插件IR信息
                plugin_instance_list.append(PluginIRConverter.ir_to_plugin(plugin_conf))
            else:
                plugin_ids.append(plugin_conf.get("id"))

        try:
            plugin_ids = [int(plugin_id) for plugin_id in plugin_ids]
        except Exception as e:
            raise JiuWenBaseException(
                error_code=StatusCode.WORKFLOW_COMPONENT_EXECUTE_ERROR.code,
                message=StatusCode.WORKFLOW_COMPONENT_EXECUTE_ERROR.errmsg.format(
                    NodeType.TASK.value, "The plugin id must be of the int type"
                ),
            ) from e

        if plugin_ids:
            try:
                plugin_manager = PluginManager()

                res = plugin_manager.get_plugin_by_id(plugin_ids)
                if "body" in res:
                    plugin_instance_list.extend(res.get("body"))
                else:
                    raise JiuWenBaseException(
                        error_code=StatusCode.WORKFLOW_COMPONENT_EXECUTE_ERROR.code,
                        message=StatusCode.WORKFLOW_COMPONENT_EXECUTE_ERROR.errmsg.format(
                            NodeType.TASK.value,
                            "Failed to instantiate the plugin based on the id",
                        ),
                    )
            except JiuWenBaseException:
                raise
            except Exception as e:
                raise JiuWenBaseException(
                    error_code=StatusCode.WORKFLOW_COMPONENT_EXECUTE_ERROR.code,
                    message=StatusCode.WORKFLOW_COMPONENT_EXECUTE_ERROR.errmsg.format(
                        NodeType.TASK.value, "Unknown error"
                    ),
                ) from e

        return plugin_instance_list

    def dict_keys_camel_to_snake(self, input_dict):
        """
        convert camel format configuration dictionary into snake format
        """
        output_dict = {}
        for key, value in input_dict.items():
            snake_key = self.camel_to_snake(key)
            output_dict[snake_key] = value
        return output_dict

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
    def sub_placeholder(text: str, variables: dict) -> str:
        """
        Substitute placeholders in a string with values from a dictionary.
        """

        def replace(match):
            key = match.group(1)
            return str(variables.get(key))

        try:
            result = re.sub(r"\{\{([^}]*)\}\}", replace, text)
            return result
        except (KeyError, TypeError, AttributeError):
            return ""

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

    def initial_prompt(self):
        """
        initial prompt
        """
        # initialize inference prompt
        self.prompt_engine = PromptPlanningModule()
        if self.agent_config.prompt_template:
            self.prompt = self.agent_config.prompt_template
        else:
            prompt = TemplateManager().get(
                name=self.language_config.get("llm_template"),
                filters={"model_name": self.agent_config.model_name},
            )
            if prompt is not None and hasattr(prompt, "content"):
                self.prompt = prompt.content
            else:
                raise JiuWenBaseException(
                    message=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR.errmsg,
                    error_code=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR.code,
                )

        # 意图判断和工具选择prompts
        prompt_react_tool_choose = TemplateManager().get(
            name=self.language_config.get("agent_tool_choose_template"),
            filters={"model_name": self.agent_config.model_name},
        )
        if prompt_react_tool_choose is not None and hasattr(
            prompt_react_tool_choose, "content"
        ):
            self.prompt_react_tool_choose = prompt_react_tool_choose.content

        # 工具总结prompts
        prompt_tool_summary = TemplateManager().get(
            name=self.language_config.get("agent_tool_summary_template"),
            filters={"model_name": self.agent_config.model_name},
        )
        if prompt_tool_summary is not None and hasattr(prompt_tool_summary, "content"):
            self.prompt_tool_summary = prompt_tool_summary.content

        # 追问话术拟人化 prompt
        llm_auto_ask = TemplateManager().get(
            name=self.language_config.get("questioner_llm_auto_ask_template"),
            filters={"model_name": self.agent_config.model_name},
        )
        if llm_auto_ask is not None and hasattr(llm_auto_ask, "content"):
            self.llm_auto_ask = llm_auto_ask.content

        # 闲聊 prompt
        llm_chat = TemplateManager().get(
            name=self.language_config.get("agent_chat_template"),
            filters={"model_name": self.agent_config.model_name},
        )
        if llm_chat is not None and hasattr(llm_chat, "content"):
            self.llm_chat = llm_chat.content

    def invoke(self, inputs: Input, **kwargs) -> Output:
        pass

    async def await_by_stream_callback(self, stream_callback, answer, span: Span):
        """适配流式以及非流式调用"""
        if stream_callback:
            answer = answer.get("answer")
        stream_related_info = WorkflowStreamingData(
            stream_callback=stream_callback, execution_id=span.trace_id
        )
        if isinstance(self.metadata, dict):
            self.metadata["should_interrupt"] = self.should_interrupt()
        elif isinstance(self.metadata, WorkflowMetadata):
            setattr(self.metadata, "should_interrupt", self.should_interrupt())
        await process_output_of_interaction_component(
            stream_related_info=stream_related_info,
            origin_output=answer,
            chat_manager=self.chat_manager,
            metadata=self.metadata,
        )

    async def ainvoke(self, inputs: dict[str, Any], **kwargs):
        """
        Agent ainvoke
        """
        inputs = inputs.get(USER_FIELDS)  # 输入参数变量
        stream_callback: WorkflowStreamCallback = kwargs.get("stream_callback")
        # initial user response with the most recent one
        conv_history = self.get_latest_chat_history()
        self.user_response = (
            conv_history[-1].get(QuestionerKeyword.CONTENT) if conv_history else ""
        )  # 最后一轮用户对话
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
        res = await self.agent_base_interact(debug_info, inputs, stream_callback)
        return {USER_FIELDS: res}

    async def get_user_int_and_tool_name(self, inputs):
        """
        通过大模型 进行意图分类判断
        """
        res_int = 3
        res_fun = ""
        if (
            self.agent_config.enable_intent_break
            and self.user_response in self.language_config.get("user_break_info")
        ):
            self.agent_config.user_break = True
            res_int = 0
            return res_int, res_fun
        chat_history = self.get_latest_chat_history(
            k=self.agent_config.chat_history_max_rounds
        )
        chat_history_str = "\n".join(
            [
                "{role}：{content}".format(
                    role=unit.get(QuestionerKeyword.ROLE),
                    content=unit.get(QuestionerKeyword.CONTENT),
                )
                for unit in chat_history[:-1]
            ]
        )
        prompt_template = [
            ChatContent(
                role=p.get(QuestionerKeyword.ROLE),
                content=p.get(QuestionerKeyword.CONTENT),
            )
            for p in self.prompt_react_tool_choose
        ]
        # 存所有tool name，留着后面做校验
        tool_name_list = []
        tool_intent_list_str = ""
        # 如果有工具list
        if self.plugins_list:
            for i in range(len(self.plugins_list)):
                tool_name = self.plugins_list[i]["name"]
                tool_des = self.plugins_list[i]["description"]
                tool_intent_list_str += (
                    self.language_config.get("tool_intent_str")
                ).format(i + 2, tool_name, tool_des)
                tool_name_list.append(tool_name)
        # 工具历史执行结果
        tool_his = ""
        if self.node_state.tool_result_summary:
            for tool_exec in self.node_state.tool_result_summary:
                tool_name = tool_exec.get("tool_name")
                tool_args = tool_exec.get("tool_args")
                tool_result = tool_exec.get("tool_result_summary")
                tool_his += self.language_config.get("tool_his_str").format(
                    tool_name, tool_args, tool_result
                )

        # 获取sysprompt
        system_prompt = self.agent_config.system_prompt
        sys_prompt = self.sub_placeholder(deepcopy(system_prompt), inputs)
        prompt_template_input = {
            "input": self.user_response,
            "chat_history": chat_history_str,
            "sys_prompt": sys_prompt,
            "tool_class": tool_intent_list_str,
            "tool_result_summary": tool_his,
            "tool_choose": self.node_state.tool_choose,
        }

        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        llm_inputs = LanguageModelInput(
            messages=ModelUtil.switch_message(prompt), tools=[]
        )
        try:
            result = await self.llm.ainvoke(llm_inputs)
            new_response = result.content
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e
        try:
            new_response = refix_llm_output(new_response)
            new_response = ast.literal_eval(new_response)
            llm_result = new_response.get("result")
            if isinstance(llm_result, str):
                llm_result = int(llm_result)
            if llm_result == 1:
                self.agent_config.user_break = True
            else:
                self.agent_config.user_break = False

            # 1、解析大模型结果；2、获取类别；3、获取工具名称
            # 暂时只校验类别和工具名称，逻辑：1、有跳出意图执行跳出意图，工具名称为空；2、在参数收集过程中如果工具变动，优先执行参数收集任务；3、工具名称必须在工具候选列表中，否则类别3；
            res_int, res_fun = self.process_res_int_and_func(
                llm_result, res_int, res_fun, tool_name_list
            )
        except Exception as e:
            logger.info(e)
            self.agent_config.user_break = False
        return res_int, res_fun

    def process_res_int_and_func(self, llm_result, res_int, res_fun, tool_name_list):
        if llm_result == 1:
            if self.agent_config.enable_intent_break:  # 如果前端意图退出按钮打开
                res_int = llm_result
            else:
                res_int = 0
            res_fun = ""
        elif llm_result == 0:
            res_int = llm_result
            res_fun = ""
        else:  # 有工具名称
            llm_tool_name = tool_name_list[llm_result - 2]
            res_int = llm_result
            res_fun = llm_tool_name
        return res_int, res_fun

    def create_prompt_template_input(self, chat_history: list, inputs) -> dict:
        """
        create input for prompt template
        """
        user_response = (
            self.user_response
            if self.user_response
            else chat_history[-1].get(QuestionerKeyword.CONTENT)
        )

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

        system_prompt = self.agent_config.system_prompt

        sys_prompt = self.sub_placeholder(deepcopy(system_prompt), inputs)

        return {
            "dig_history": dig_history,
            "sys_prompt": sys_prompt,
        }

    def get_state(self):
        """
        get questioner state
        """
        return self.node_state

    def load_state(self, state: AgentState):
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

    async def interact_with_user(self, debug_info=None) -> str:
        chat_history = self.get_latest_chat_history(
            k=self.agent_config.chat_history_max_rounds
        )
        chat_history_str = "\n".join(
            [
                "{role}：{content}".format(
                    role=unit.get(QuestionerKeyword.ROLE),
                    content=unit.get(QuestionerKeyword.CONTENT),
                )
                for unit in chat_history[:-1]
            ]
        )

        prompt_template_input = {
            "input": self.user_response,
            "chat_history": chat_history_str,
        }
        prompt_template = [
            ChatContent(
                role=p.get(QuestionerKeyword.ROLE),
                content=p.get(QuestionerKeyword.CONTENT),
            )
            for p in self.llm_chat
        ]

        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        llm_inputs = LanguageModelInput(
            messages=ModelUtil.switch_message(prompt), tools=[]
        )
        debug_info = {} if debug_info is None else debug_info
        try:
            result = await self.llm.ainvoke(llm_inputs)
            response = result.content
            # LLM 调用insight
            llm_info = {
                "llm_info": {LLM_INPUTS: llm_inputs, LLM_OUTPUTS: response},
                "assistant": response,
            }
            await process_on_invoke_info(
                component_metadata=debug_info.get("component_metadata"),
                runtime_context=self.runtime_context,
                data=llm_info,
                span=debug_info.get("span"),
            )
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e

        return response

    async def llm_tool_summary(self, tool_name, tool_result, debug_info=None) -> str:
        prompt_template_input = {
            "tool_name": tool_name,
            "tool_result": tool_result,
        }
        prompt_template = [
            ChatContent(
                role=p.get(QuestionerKeyword.ROLE),
                content=p.get(QuestionerKeyword.CONTENT),
            )
            for p in self.prompt_tool_summary
        ]

        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        llm_inputs = LanguageModelInput(
            messages=ModelUtil.switch_message(prompt), tools=[]
        )
        debug_info = {} if debug_info is None else debug_info

        try:
            result = await self.llm.ainvoke(llm_inputs)
            response = result.content
            # LLM 调用insight
            llm_info = {
                "llm_info": {LLM_INPUTS: self.user_response, LLM_OUTPUTS: response},
                "assistant": response,
            }
            await process_on_invoke_info(
                component_metadata=debug_info.get("component_metadata"),
                runtime_context=self.runtime_context,
                data=llm_info,
                span=debug_info.get("span"),
            )
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e

        return response

    def has_target_plugin(self, tool_name):
        tool_id = ""
        for item in self.agent_config.plugins:
            if item.get("name") == tool_name:
                tool_id = item.get("id")
                break
        if tool_id in self.agent_config.break_plugin_ids:
            return True
        return False

    async def interact_nl2json(self, arguments):
        # 拿到必须提取的参数名
        args_list = [arg.get("name") for arg in arguments if arg.get("required")]
        chat_history = self.get_latest_chat_history(
            k=self.agent_config.chat_history_max_rounds
        )
        # 去掉最后一轮query，nl2json内部再拼
        chat_history = chat_history[:-1]
        query = self.user_response
        model = dict(
            model_type=self.agent_config.model_type,
            model_name=self.agent_config.model_name,
            runtime_context=self.runtime_context,
            **self.agent_config.hyper_parameters,
            **self.agent_config.extension,
        )
        mode = "effect"
        extracted_key_fields = (
            self.node_state.extracted_key_fields
            if self.node_state.extracted_key_fields
            else {}
        )
        nl2json = NL2JSONProcessor(
            chat_history, query, model, arguments, mode, extracted_key_fields
        )
        json_res = await nl2json.ainvoke()
        llm_info = json_res.get("llm_info")
        extracted_fields = json_res.get("args", {})
        self.node_state.extracted_key_fields = extracted_fields

        keys = [
            k for k, v in self.node_state.extracted_key_fields.items() if v is not None
        ]
        ret = []
        for key in args_list:
            if key not in keys:
                ret.append(key)
        return ret, llm_info

    async def get_llm_auto_ask(self, unextracted_field_names):
        """
        通过大模型 构造更人性化的追问语句
        """
        chat_history = self.get_latest_chat_history(
            k=self.agent_config.chat_history_max_rounds
        )
        chat_history_str = "\n".join(
            [
                "{role}：{content}".format(
                    role=unit.get(QuestionerKeyword.ROLE),
                    content=unit.get(QuestionerKeyword.CONTENT),
                )
                for unit in chat_history[:-1]
            ]
        )
        prompt_template_input = {
            "unextracted_cn_field_names": unextracted_field_names,
            "chat_history": chat_history_str,
        }
        prompt_template = [
            ChatContent(
                role=p.get(QuestionerKeyword.ROLE),
                content=p.get(QuestionerKeyword.CONTENT),
            )
            for p in self.llm_auto_ask
        ]
        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        llm_inputs = LanguageModelInput(
            messages=ModelUtil.switch_message(prompt), tools=[]
        )
        try:
            result = await self.llm.ainvoke(llm_inputs)
            new_response = result.content
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e

        return new_response

    def update_tool_result(self, tool_result):
        if not self.node_state.tool_result:
            self.node_state.tool_result = tool_result
        else:
            self.node_state.tool_result.extend(tool_result)

    def update_tool_result_summary(self, tool_summary):
        if not self.node_state.tool_result_summary:
            self.node_state.tool_result_summary = tool_summary
        else:
            self.node_state.tool_result_summary.extend(tool_summary)

    def concate_unextracted_field(self, reconfirm_fields, arguments):
        unextracted_field_names = []
        for key in reconfirm_fields:
            for arg in arguments:
                if arg.get("name") == key:
                    unextracted_field_names.append(arg.get("description"))
        return unextracted_field_names

    async def agent_base_interact(self, debug_info, inputs, stream_callback):
        """
        交互节点
        """
        self.node_state.status = ExecutionStatus.USER_INTERACT
        if self.node_state.response_num < self.agent_config.max_iteration:
            # 这里改了请求
            res_int, func_name = await self.get_user_int_and_tool_name(inputs)
            if res_int == 1:  # 意图退出
                self.node_state.status = ExecutionStatus.END
                return {"output": self.language_config.get("quit_msg")}
            # 意图闲聊
            elif res_int == 0:
                self.node_state.response_num += 1
                # 获取大模型结果
                llm_response = await self.interact_with_user(debug_info)
                llm_content = llm_response
                response_format = format_interaction_res(
                    result=llm_content, node_type=NodeType.AGENT.value
                )
                await self.await_by_stream_callback(
                    stream_callback, response_format, debug_info.get("span")
                )
                self.node_state.tool_choose = self.language_config.get(
                    "tool_choose_str"
                ).format(res_int, llm_content)
                return {"output": llm_content}
            else:
                self.node_state.response_num += 1
                if self.node_state.cur_tool_name != func_name:
                    self.node_state.cur_tool_name = func_name
                    self.node_state.extracted_key_fields = {}

                arguments = {}
                for item in self.agent_config.plugins:
                    if item.get("name") == func_name:
                        arguments = item.get("arguments")
                        break
                reconfirm_fields, llm_info = await self.interact_nl2json(arguments)

                logger.info(
                    f"nl2json extracted parameters:  {self.node_state.extracted_key_fields}"
                )
                if not reconfirm_fields:
                    function_call = self.plugins_dict.get(func_name)
                    if function_call:
                        try:
                            api_configs = self.runtime_context.get(WORKFLOW_API_CONFIGS)
                            # 插件异步调用
                            invokable_output = await function_call.ainvoke(
                                inputs=self.node_state.extracted_key_fields,
                                span=debug_info.get("span"),
                                runtime_auth_headers=api_configs,
                            )
                            # 工具调用总结
                            llm_tool_summary = await self.llm_tool_summary(
                                func_name, invokable_output, debug_info
                            )
                            debug_info = {} if debug_info is None else debug_info
                            tool_info = {
                                "tool_info": {
                                    LLM_INPUTS: self.user_response,
                                    LLM_OUTPUTS: llm_tool_summary,
                                },
                                "assistant": llm_tool_summary,
                            }
                            await process_on_invoke_info(
                                component_metadata=debug_info.get("component_metadata"),
                                runtime_context=self.runtime_context,
                                data=tool_info,
                                span=debug_info.get("span"),
                            )

                        except JiuWenBaseException:
                            raise
                        except Exception as e:
                            raise JiuWenBaseException(
                                error_code=StatusCode.WORKFLOW_API_EXECUTE_ERROR.code,
                                message="Flow API executed failed",
                            ) from e
                        # 相关信息更新到全局变量
                        func_output = format_interaction_res(
                            result=llm_tool_summary, node_type=NodeType.AGENT.value
                        )
                        await self.await_by_stream_callback(
                            stream_callback, func_output, debug_info.get("span")
                        )
                        if self.has_target_plugin(func_name):
                            self.node_state.status = ExecutionStatus.END
                        self.node_state.tool_choose = self.language_config.get(
                            "tool_choose_plugin"
                        ).format(
                            res_int,
                            func_name,
                            self.node_state.extracted_key_fields,
                            llm_tool_summary,
                        )
                        tool_result = [
                            {
                                "tool_name": func_name,
                                "tool_args": self.node_state.extracted_key_fields,
                                "tool_result": invokable_output,
                            }
                        ]
                        self.update_tool_result(tool_result)

                        tool_summary = [
                            {
                                "tool_name": func_name,
                                "tool_args": self.node_state.extracted_key_fields,
                                "tool_result_summary": llm_tool_summary,
                            }
                        ]
                        self.update_tool_result_summary(tool_summary)
                        return {"output": llm_tool_summary}
                else:
                    unextracted_field_names = self.concate_unextracted_field(
                        reconfirm_fields, arguments
                    )
                    default_question = self.language_config.get(
                        "default_question"
                    ).format(unextracted_field_names=", ".join(unextracted_field_names))
                    llm_auto_ask = await self.get_llm_auto_ask(unextracted_field_names)
                    question = llm_auto_ask if llm_auto_ask else default_question
                    llm_info.update({"assistant": question})
                    await process_on_invoke_info(
                        component_metadata=debug_info.get("component_metadata"),
                        runtime_context=self.runtime_context,
                        data=llm_info,
                        span=debug_info.get("span"),
                    )
                    question_format = format_interaction_res(
                        result=question, node_type=NodeType.AGENT.value
                    )
                    await self.await_by_stream_callback(
                        stream_callback, question_format, debug_info.get("span")
                    )
                    self.node_state.tool_choose = self.language_config.get(
                        "tool_choose_extracted_params"
                    ).format(
                        res_int,
                        func_name,
                        self.node_state.extracted_key_fields,
                        unextracted_field_names,
                        question,
                    )
                    return {"output": question}

        self.node_state.status = ExecutionStatus.END
        return {"output": self.language_config.get("max_interation_quit_msq")}
