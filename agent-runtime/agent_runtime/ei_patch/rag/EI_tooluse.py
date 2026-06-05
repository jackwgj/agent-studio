#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""工具调用优先
支持工具调用优先模式，先进行工具选择，参数提取。
中间参数信息当做历史信息记录到多轮对话中
"""

import ast
import json
import os
import time
from dataclasses import dataclass
from datetime import datetime
from typing import List, Optional, Union, AsyncGenerator

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.controller.common.enum import RetCode
from jiuwen.orchestration.flow.enum import NodeType
from jiuwen.orchestration.flow.stream.base import StreamCode
from jiuwen.planner.common.enum_class import ProviderType
from jiuwen.planner.common.exception import LLMInvocationFailedException
from jiuwen.planner.common.plan_constants import DEFAULT_MAX_ITERATION, TimerInfo
from jiuwen.planner.common.plan_runtime_context import PlanRuntimeContext
from jiuwen.planner.common.result_output_callback import ResultOutputStreamCallback
from jiuwen.planner.common.utils import get_workflow_func
from jiuwen.planner.planning_modules import ProposeNext
from jiuwen.planner.planning_modules.base import ChatContent, PromptPlanningModule
from jiuwen.prompt import TemplateManager
from json_repair import json_repair
from mcp.types import TextContent
from rag.controller.msg_context import MsgContext as PlanState
from rag.ei_agent_utils import resolve_variable_references
from rag.models.tool import Tool
from rag.stream_post_utils import stream_function_call

ROLE = "role"
SYS = "system"
USER = "user"
TOOL = "tool"
ASS = "assistant"
NAME = "name"
RESULT = "result"
FUNCTION = "function"
CONTENT = "content"
LLM_INPUTS = "llm_inputs"
LLM_OUTPUTS = "llm_outputs"
RESPONSE_EMPTY_RESULT = "返回结果为空。"
IS_WORKFLOW = "is_workflow"
LATENCY = "latency"
ANSWER = "answer"
ORIGIN_ANSWER = "origin_answer"
QUERY = "query"
PLUGIN_NAME = "plugin_name"
NON_EXIST_FUNC = "大模型生成不存在的工具，无法执行"
WORKFLOW_STATE = "workflow_state"
WORKFLOW_STATE_RUN = "running"
STOPWORD = "执行失败"
DEFAULT_MAX_PROMPT_LENGTH = 10000


def get_mcp_func(func_name: str, functions: List[Tool]):
    """search workflow by func name"""
    select_func = None
    for func in functions:
        if func_name in func.tool_id and func.provider_type == ProviderType.MCP:
            select_func = func
            break
    return select_func


@dataclass
class ExecutionContext:
    exec_result: Optional[dict]
    exec_latency: Optional[float]
    search_res: dict
    function_call: Union[dict, str]
    planning_state: PlanState
    time_dict: dict
    func_info: dict


def _exec_func(planning_state, function_name, result, exec_latency):
    """process function result"""
    if isinstance(result, dict):
        error_code = result.get("errCode")
        error_message = result.get("errMessage")
    else:
        error_code = (
            result.err_code
            if result is not None and hasattr(result, "err_code")
            else -1
        )
        error_message = (
            result.err_msg
            if result is not None and hasattr(result, "err_msg")
            else "工具执行异常"
        )

    if error_code != 0:
        output = error_message
        function_thought = {
            ROLE: FUNCTION,
            NAME: function_name,
            CONTENT: "工具执行失败",
        }
        # 工具执行失败，需要先把对应的function信息添加到规划状态中
        planning_state.add(role=FUNCTION, thought=function_thought)
    else:
        output = result.get("data")
    return dict(error_code=error_code, result=output, latency=exec_latency)


def refix_llm_output(input_str):
    malformed_json = input_str.replace("None", "null").replace('"None"', "null")
    repaired_json = json_repair.repair_json(malformed_json)
    res = {
        k: v for k, v in json.loads(repaired_json).items() if v is not None and str(v)
    }
    return res


def start_Tool_line(ir_data):
    try:
        configs_info = ir_data["configs"]
        if configs_info["mode"] != "ToolUse":
            return False
        elif "workflow" in configs_info:
            return False
        return True
    except Exception as e:
        logger.error("use tool error", e)
        return False


class EIToolUse:
    def __init__(self, plan_config, model, context_manager, plan_model=None):
        self.llm = model
        self.plan_model = plan_model if plan_model else model
        self.context_manager = context_manager
        self.propose = ProposeNext(plan_config=plan_config, model=model)
        self.result_output_callback = ResultOutputStreamCallback()
        self._max_iteration = (
            plan_config.constrain_config.max_iteration or DEFAULT_MAX_ITERATION
        )
        self.initial_prompt()
        self.extracted_key_fields = {}
        self.chat_history = []
        self.user_response = ""
        self.states_info_body = {}
        self.tool_params_full = False
        self.prompt_sys = ""

    def initial_prompt(self):
        self.prompt_engine = PromptPlanningModule()
        tool_int = TemplateManager().get(
            name="agent_tool_choose", filters={"model_name": self.plan_model.model_name}
        )
        if tool_int is not None and hasattr(tool_int, "content"):
            self.prompt_react_tool_choose = tool_int.content
        else:
            raise JiuWenBaseException(
                message=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR.errmsg,
                error_code=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR.code,
            )
        # 工具总结prompts
        prompt_tool_summary = TemplateManager().get(
            name="agent_tool_summary", filters={"model_name": self.llm.model_name}
        )
        if prompt_tool_summary is not None and hasattr(prompt_tool_summary, "content"):
            self.prompt_tool_summary = prompt_tool_summary.content

        # 追问话术拟人化 prompt
        llm_auto_ask = TemplateManager().get(
            name="questioner_llm_auto_ask", filters={"model_name": self.llm.model_name}
        )
        if llm_auto_ask is not None and hasattr(llm_auto_ask, "content"):
            self.llm_auto_ask = llm_auto_ask.content

        # 单跳出模式选型
        intent_only_break = TemplateManager().get(
            name="agent_flow_break", filters={"model_name": self.llm.model_name}
        )
        if intent_only_break is not None and hasattr(intent_only_break, "content"):
            self.intent_only_break = intent_only_break.content

    def get_tool_des(self, functions):
        res = ""
        num = 2
        res_dict = {0: "", 1: ""}
        for item in functions:
            tool_name = item.tool_id
            try:
                tool_des = item.invokable.description
            except Exception:
                tool_des = item.parameters["description"]
            res += (
                "分类ID {}: 根据历史信息和最近一轮输入，判断是否有要涉及工具{}({})的调用。如有则选择此选项。\n"
            ).format(num, tool_name, tool_des)
            res_dict[num] = tool_name
            num += 1
        return res, res_dict

    def get_sys_prompts(self, sys_prompts_list):
        res = ""
        for item in sys_prompts_list:
            if item[ROLE] == SYS:
                if res != "":
                    res += "\n"
                res += item[CONTENT]
        return res

    @staticmethod
    def _format_yield_from_llm_output_not_yield(result, llm_output: dict):
        for item in result:
            if (
                isinstance(item, dict) and "llm_output" in item
            ):  # 大模型的最终输出，不需要流式输出
                llm_output.update(item.get("llm_output"))

    def _get_states_info_body(self):
        new_body = {
            "tool_list": [],
            "tool_choose": "",
            "tool_result_summary": "",
            "tool_result": [],
            "extracted_key_fields": [],
        }
        if self.context_manager.states_info:
            new_body = self.context_manager.states_info.pop()
        return new_body

    def get_chat_history(self, planning_state):
        step = planning_state.context.get("steps")
        current_time = datetime.now()
        query_time = f"今天是{current_time}"
        quert_time_sys = {ROLE: SYS, CONTENT: query_time}

        # 修复历史信息截断逻辑 - 保留最近的reserved_max_chat_rounds轮对话
        reserved_rounds = self.propose.op_config.reserved_max_chat_rounds or 3
        if len(step) > reserved_rounds:
            recent_steps = step[-reserved_rounds:]
        else:
            recent_steps = step

        chat_history = self.prompt_sys + [quert_time_sys] + recent_steps
        return chat_history

    def get_user_break_int(self, query):
        res_int = 2
        prompt_template = [
            ChatContent(role=p.get(ROLE), content=p.get(CONTENT))
            for p in self.intent_only_break
        ]
        chat_history = self.chat_history
        prompt_template_input = {"input": query, "chat_history": chat_history}
        prompt_int = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        try:
            response = self.plan_model.invoke(prompt_int).content
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e
        try:
            llm_output_dict_fix = refix_llm_output(response)
            res_int = llm_output_dict_fix.get("result", 2)
            # 如果没有跳出，则继续
            if res_int != 1:
                res_int = 2
        except Exception:
            res_int = 2
        return res_int

    def get_user_int_and_tool_name(
        self, query, sys_prompts, functions, planning_state, **kwargs
    ):
        res_int = 0
        res_fun = ""
        # 如果上一把调用了某个工具，本次就选择不调用该工具
        prompt_template = [
            ChatContent(role=p.get(ROLE), content=p.get(CONTENT))
            for p in self.prompt_react_tool_choose
        ]
        sys_prompts_str = self.get_sys_prompts(sys_prompts)
        chat_history = self.chat_history
        try:
            tool_result_summary = self.states_info_body["tool_result_summary"]
        except Exception:
            tool_result_summary = ""
        try:
            tool_choose = str(self.states_info_body["tool_choose"])
        except Exception:
            tool_choose = ""
        tool_class, tool_name_dict = self.get_tool_des(functions)
        prompt_template_input = {
            "input": query,
            "chat_history": chat_history,
            "sys_prompt": sys_prompts_str,
            "tool_class": tool_class,
            "tool_result_summary": tool_result_summary,
            "tool_choose": tool_choose,
        }
        prompt_int = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        try:
            response = self.plan_model.invoke(prompt_int).content
        except Exception as e:
            logger.error("Exception", e)
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e
        try:
            llm_output_dict_fix = refix_llm_output(response)
            res_int = int(llm_output_dict_fix.get("result", 0))
            res_fun = tool_name_dict[res_int]
        except Exception as e:
            logger.error(f"refix llm output error, reason is {str(e)}")
            res_fun = ""
            res_int = 0
        if res_fun != "" and res_fun in tool_choose and STOPWORD in tool_choose:
            res_fun = ""
            res_int = 0
        return res_int, res_fun

    def get_fun_info(self, res_fun, functions):
        for item in functions:
            if item.tool_id == res_fun:
                return item
        return []

    def extract_input_parameters(self, input_parameters: dict):
        if not input_parameters:
            return {}
        inputs_params = self.context_manager.get_global_variables("agent_inputs", {})
        mem_params = self.context_manager.get_global_variables("memory_variables", {})
        for key, value in input_parameters.items():
            if not value:
                continue
            if value.startswith("{{") and value.endswith("}}"):
                ref = value[2:-2]
                if ref.startswith("inputs."):
                    input_parameters[key] = inputs_params.get(ref[7:])
                elif ref.startswith("memory."):
                    input_parameters[key] = mem_params.get(ref[7:])
        return input_parameters

    def interact_nl2json(self, func_info):
        no_visible = []
        if func_info.provider_type.name == "WORKFLOW":
            arguments_dict = func_info.parameters["parameters"]["properties"]
            args_list = []
            arguments = []
            for key in arguments_dict:
                args_list.append(key)
                arguments.append(
                    {
                        "name": key,
                        "desc": arguments_dict[key].get("description", ""),
                        "cn_name": "",
                        "type": arguments_dict[key]["type"],
                    }
                )
        elif func_info.provider_type.name == "MCP":
            arguments_dict = func_info.parameters["properties"]
            args_list = []
            arguments = []
            for key in arguments_dict:
                args_list.append(key)
                arguments.append(
                    {
                        "name": key,
                        "desc": arguments_dict[key].get("description", ""),
                        "cn_name": "",
                        "type": arguments_dict[key]["type"],
                    }
                )
        else:
            input_parameters = self.extract_input_parameters(
                func_info.invokable.input_parameters
            )

            args_list = []
            arguments = []
            no_visible = {}
            for arg in func_info.invokable.params:
                if arg.name in input_parameters:
                    ref_val = input_parameters.get(arg.name)
                    if ref_val is not None:
                        arg.default_value = ref_val
                if arg.visible is False:
                    no_visible[arg.name] = arg.default_value
                else:
                    arguments.append(arg.__dict__)
                    args_list.append(arg.name)

        chat_history = self.chat_history
        query = self.user_response
        model = self.plan_model
        mode = "speed"
        extracted_key_fields = (
            self.extracted_key_fields if self.extracted_key_fields else {}
        )

        from jiuwen.common.utils.nl2json import NL2JSONProcessor

        nl2json = NL2JSONProcessor(
            chat_history, query, model, arguments, mode, extracted_key_fields
        )
        json_res = nl2json.invoke()
        llm_info = json_res.get("llm_info")
        extracted_fields = json_res.get("args", {})
        keys = [k for k, v in extracted_fields.items() if v is not None]
        miss_arg_name = []
        for key in args_list:
            if key not in keys:
                miss_arg_name.append(key)
        collectflag = True
        if func_info.provider_type.name == "WORKFLOW":
            # workflow的情况下没收集满就不行
            if miss_arg_name:
                collectflag = False
        else:
            for arg in arguments:
                if arg.get("required") and arg.get("name") in miss_arg_name:
                    collectflag = False

        extracted_fields.update(no_visible)
        return miss_arg_name, extracted_fields, llm_info, collectflag

    def llm_tool_summary(
        self, tool_name, tool_result, debug_info, planning_state, **kwargs
    ) -> str:
        prompt_template_input = {
            "tool_name": tool_name,
            "tool_result": tool_result,
        }
        prompt_template = [
            ChatContent(role=p.get(ROLE), content=p.get(CONTENT))
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
            # 这个流式带修复 response = yield from self._on_pre_invoke_llm(llm_inputs, [], planning_state, **kwargs)
            response = self.llm.invoke(llm_inputs).content
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e

        return response

    def get_llm_auto_ask(self, unextracted_field_names):
        """
        通过大模型 构造更人性化的追问语句
        """
        chat_history = self.chat_history
        chat_history_str = "\n".join(
            [
                "{role}：{content}".format(
                    role=unit.get(ROLE), content=unit.get(CONTENT)
                )
                for unit in chat_history[:-1]
            ]
        )
        prompt_template_input = {
            "unextracted_cn_field_names": unextracted_field_names,
            "chat_history": chat_history_str,
        }
        prompt_template = [
            ChatContent(role=p.get(ROLE), content=p.get(CONTENT))
            for p in self.llm_auto_ask
        ]
        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        llm_inputs = LanguageModelInput(
            messages=ModelUtil.switch_message(prompt), tools=[]
        )
        try:
            new_response = self.llm.invoke(llm_inputs).content
        except Exception:
            new_response = ""
            self.error_message["message"] = StatusCode.INVOKE_LLM_FAILED.errmsg
            self.error_message["error_code"] = StatusCode.INVOKE_LLM_FAILED.code

        return new_response

    @staticmethod
    def _format_yield_from_llm_output(result, llm_output: dict, time_dict: dict):
        for item in result:
            if (
                isinstance(item, dict) and "llm_output" in item
            ):  # 大模型的最终输出，不需要流式输出
                llm_output.update(item.get("llm_output"))
            elif isinstance(item, dict) and "time_consumption" in item:
                time_dict.update(item.get("time_consumption"))
            else:
                yield item

    @staticmethod
    def _search_last_function_result(planning_state):
        if planning_state.dealing_node.get(ROLE, "") == FUNCTION:
            res = planning_state.dealing_node.get(CONTENT)
            return res if RESPONSE_EMPTY_RESULT not in res else ""
        return ""

    def _on_pre_invoke_llm(self, prompt_input, functions, planning_state, **kwargs):
        time_dict = dict()
        yield from self.result_output_callback.on_llm_output_stream_started()
        functions = self.propose.format_tools(functions, **kwargs) if functions else []

        llm_output_dict = dict()

        # 添加prompt长度检查和日志
        try:
            # 如果prompt_input包含ConversationMessage对象，先转换为字典
            if hasattr(prompt_input, "__iter__") and len(prompt_input) > 0:
                from jiuwen.context.history import ConversationHistory

                # 检查列表中的元素类型
                contains_conversation_message = any(
                    hasattr(item, "model_dump") for item in prompt_input
                )
                contains_dict = any(isinstance(item, dict) for item in prompt_input)

                if contains_conversation_message and not contains_dict:
                    # 纯ConversationMessage对象列表，使用转换方法
                    prompt_dict = (
                        ConversationHistory.convert_messages_to_chat_history_dict(
                            prompt_input
                        )
                    )
                elif contains_conversation_message and contains_dict:
                    # 混合类型列表，需要分别处理
                    formatted_messages = []
                    for msg in prompt_input:
                        if isinstance(msg, dict):
                            # 已经是字典格式，直接添加
                            formatted_messages.append(msg)
                        elif hasattr(msg, "model_dump"):
                            # ConversationMessage对象，转换为字典
                            msg_dict = msg.model_dump(exclude_none=True)
                            # 确保只包含必要的字段
                            filtered_dict = {
                                k: v
                                for k, v in msg_dict.items()
                                if k
                                in ["role", "content", "name", "tool_call_id", "files"]
                            }
                            formatted_messages.append(filtered_dict)
                    prompt_dict = formatted_messages
                else:
                    # 纯字典列表，直接使用
                    prompt_dict = prompt_input
            else:
                prompt_dict = prompt_input

            prompt_str = json.dumps(prompt_dict, ensure_ascii=False)
            logger.info(
                f"LLM调用 - prompt长度: {len(prompt_str)}, 工具数量: {len(functions) if functions else 0}"
            )

            # 检查prompt长度是否超过限制
            max_prompt_length = int(
                os.getenv("MAX_PROMPT_LENGTH", DEFAULT_MAX_PROMPT_LENGTH)
            )
            if len(prompt_str) > max_prompt_length:
                logger.warning(
                    f"Prompt长度超过限制: {len(prompt_str)} > {max_prompt_length}"
                )
                raise LLMInvocationFailedException(
                    f"Prompt长度超过限制: {len(prompt_str)}"
                )

            res = stream_function_call(
                prompt_dict,
                functions,
                self.plan_model,
                contexts=kwargs.get("contexts"),
                trace_handlers=kwargs.get("trace_handlers", None),
                multimodal_image=kwargs.get("multimodal_image"),
                round_id=kwargs.get("round_id"),
                session_id=kwargs.get("session_id"),
            )
            yield from self._format_yield_from_llm_output(
                res, llm_output_dict, time_dict
            )
            logger.info("LLM Response: Received")
        except Exception as e:
            logger.error(
                f"LLM调用失败 - prompt长度: {len(prompt_str) if 'prompt_str' in locals() else 'unknown'}, "
                f"工具数量: {len(functions) if functions else 0}, "
                f"错误详情: {str(e)}"
            )
            raise LLMInvocationFailedException(
                f"Failed to stream calling LLM - prompt长度: {len(prompt_str) if 'prompt_str' in locals() else 'unknown'}, "
                f"工具数量: {len(functions) if functions else 0}, 错误: {str(e)}"
            ) from e

        updated_state = {
            "llm_output": llm_output_dict,
            "planning_state": planning_state,
        }
        self.propose.update_planning_state(**updated_state)

    async def _execute_function_from_function_call(
        self, function_call, functions, **kwargs
    ):
        runtime_context: PlanRuntimeContext = (
            kwargs.get("runtime_context") or PlanRuntimeContext()
        )
        function_exec_res, exec_latency = (
            dict(errCode=-1, errMessage=NON_EXIST_FUNC),
            0.0,
        )
        tool_name = function_call.get("name")
        tool_arguments = function_call.get("arguments")
        if tool_name == "python_interpreter":
            tool_arguments_dict = json.loads(tool_arguments)
            tool_arguments = json.dumps(tool_arguments_dict, ensure_ascii=False)
        for func in functions:
            if tool_name == func.tool_id or func.tool_id.startswith(tool_name + "#*"):
                start_time = time.time()
                function_exec_res = await func.ainvoke(
                    inputs={"tool_name": tool_name, "tool_arguments": tool_arguments},
                    runtime_auth={
                        "headers": runtime_context.api_config.get("headers", {})
                    },
                    **kwargs,
                )
                if func.provider_type == ProviderType.MCP:
                    function_call["is_mcp"] = True
                    function_call["server_name"] = func.invokable.server_name
                else:
                    function_call["is_mcp"] = False
                exec_latency = round(time.time() - start_time, 2)  # 工具执行耗时
                break
        return function_exec_res, exec_latency

    async def _exec_function(self, functions, function_call, **kwargs):
        args = function_call.get("arguments", "{}")
        check_result, trans_result = ModelUtil.check_and_trans2json(args)
        args = trans_result if check_result else ast.literal_eval(args)
        function_call["arguments"] = json.dumps(args, ensure_ascii=False)
        return await self._execute_function_from_function_call(
            function_call, functions, **kwargs
        )

    async def _process_stream_function_call(
        self, functions: list, exec_context: ExecutionContext, **kwargs
    ):
        exec_result, exec_latency = await self._exec_function(
            functions, exec_context.function_call, **kwargs
        )
        exec_context.exec_result = exec_result
        exec_context.exec_latency = exec_latency
        # 创建 ExecutionContext 对象
        if exec_context.func_info.get(IS_WORKFLOW, False):
            # 执行工作流流式输出
            async for item in self._exec_stream_workflow(exec_context):
                yield item
        else:
            # 执行插件流式输出
            async for item in self._exec_stream_function(exec_context):
                yield item

    async def _post_process_stream_workflow(
        self, function_call, planning_state, func_res, search_res, time_dict
    ):
        """后处理工作流流式结果"""
        report_answer, total_result = "", None
        if isinstance(func_res, AsyncGenerator):
            last_item = None
            start_time = time.time()
            async for item in func_res:
                item = dict(errCode=-1, errMessage=NON_EXIST_FUNC) if not item else item
                item_res = self._exec_func(planning_state, function_call, item, None)
                last_item = item_res
                if item_res.get(RESULT, {}).get("code") != StreamCode.MESSAGE_END.value:
                    if item_res.get("error_code") != 0:
                        yield {
                            "function_call": function_call,
                            "time_consumption": TimerInfo(**time_dict),
                            "is_workflow": True,
                        }
                        yield {
                            "plugin_name": function_call.get(NAME),
                            "function_name": function_call.get(NAME),
                            "result": item,
                            "latency": item_res.get("latency"),
                        }
                    else:
                        answer_dict = item_res.get(RESULT, {}).get(ANSWER, {})
                        # 只有code为StreamCode.FINISH才输出，防止前端接收到不必要的信息
                        if (
                            item_res.get(RESULT, {}).get("code", {})
                            == StreamCode.FINISH.value
                            and isinstance(answer_dict, dict)
                            and answer_dict.get(ANSWER)
                        ):
                            # 复用message 流式传递workflow
                            yield {
                                "role": "assistant",
                                "content": answer_dict.get(ANSWER),
                            }
                elif (
                    not item_res.get(RESULT, {}).get(ANSWER, {}).get("parentNodeId", "")
                    or item_res.get(RESULT, {}).get(ANSWER, {}).get("node_type", "")
                    == NodeType.QUESTIONER.value
                ):
                    # 如果存在结构化消息，需要将原始信息存入上下文
                    report_answer += item_res.get(RESULT, {}).get(ANSWER, {}).get(
                        ORIGIN_ANSWER
                    ) or item_res.get(RESULT, {}).get(ANSWER, {}).get(ANSWER)
            exec_latency = round(time.time() - start_time, 2)
            workflow_state = last_item.get(RESULT, {}).get(WORKFLOW_STATE)
            if item_res.get("error_code") != 0:
                report_answer = last_item.get(RESULT, {}).get(ANSWER, {})
            else:
                total_result = {
                    RESULT: {ANSWER: report_answer, WORKFLOW_STATE: workflow_state},
                    LATENCY: exec_latency,
                }
        yield total_result, report_answer

    def _reset_pending_functions(self, function_call, planning_state):
        planning_state.last_func = dict()
        if planning_state.functions.get(function_call.get(NAME), None):
            del planning_state.functions[function_call.get(NAME)]

    def _stream_out_tool_exec_result(self, exec_result, search_res, function_call):
        if exec_result.get("error_code") != 0:
            yield from self.result_output_callback.on_tool_executed(
                search_res.get(PLUGIN_NAME),
                function_call.get(NAME),
                exec_result.get(RESULT),
                False,
                TimerInfo(total_latency=exec_result.get(LATENCY)),
            )
        else:
            yield from self.result_output_callback.on_tool_executed(
                search_res.get(PLUGIN_NAME),
                function_call.get(NAME),
                exec_result.get(RESULT),
                True,
                TimerInfo(total_latency=exec_result.get(LATENCY)),
            )

    async def _exec_stream_workflow(self, context: ExecutionContext):
        """执行工作流流式输出"""
        time_dict = context.time_dict
        async for output in self._post_process_stream_workflow(
            context.function_call,
            context.planning_state,
            context.exec_result,
            context.search_res,
            context.time_dict,
        ):
            if not isinstance(output, tuple):
                yield output
                continue

            total_result, answer = output
            if total_result:
                time_dict["total_latency"] += total_result.get(LATENCY, 0.0)
                for item in self.result_output_callback.on_function_call_generated(
                    func_call=context.function_call,
                    time_info=TimerInfo(**context.time_dict),
                    workflow_state=True,
                ):
                    yield item
                for item in self._stream_out_tool_exec_result(
                    total_result, context.search_res, context.function_call
                ):
                    yield item

            context.planning_state.report_message = {RESULT: answer}
            # context.planning_state.is_terminated = True
            tool_msg = []
            if context.planning_state.tool_msg:
                tool_msg.append(context.planning_state.tool_msg)
            tool_msg.append(
                {
                    ROLE: FUNCTION,
                    NAME: context.function_call.get(NAME),
                    CONTENT: json.dumps(
                        context.planning_state.report_message, ensure_ascii=False
                    ),
                }
            )
            if len(tool_msg) == 2:
                for item in self.result_output_callback.on_tool_execute_end(tool_msg):
                    yield item

    @staticmethod
    def _post_process_retrieval_result(result):
        return RESPONSE_EMPTY_RESULT if not (result or result == []) else result

    @staticmethod
    def _compress_result(result, function_call, planning_state):
        compress_result = None
        if isinstance(result, str):
            compress_result = result
        elif (
            isinstance(result, list)
            and len(result) > 0
            and isinstance(result[0], TextContent)
        ):
            compress_result = json.dumps([r.text for r in result], ensure_ascii=False)
        else:
            compress_result = json.dumps(result, ensure_ascii=False)
        function_thought = {
            ROLE: FUNCTION,
            NAME: function_call.get(NAME),
            CONTENT: compress_result,
        }
        planning_state.add(function_thought)

    def _post_process(self, exec_result, function_call, planning_state):
        if exec_result.get("error_code") == 0:
            result = self._post_process_retrieval_result(exec_result.get(RESULT))
            self._compress_result(result, function_call, planning_state)

    async def _exec_stream_function(self, context: ExecutionContext):
        """执行插件流式输出"""
        exec_result = self._exec_func(
            context.planning_state,
            context.function_call,
            context.exec_result,
            context.exec_latency,
        )
        context.time_dict["total_latency"] += exec_result.get(LATENCY, 0.0)
        for item in self.result_output_callback.on_function_call_generated(
            func_call=context.function_call,
            time_info=TimerInfo(**context.time_dict),
            workflow_state=False,
        ):
            yield item
        for item in self._stream_out_tool_exec_result(
            exec_result, context.search_res, context.function_call
        ):
            yield item
        self._post_process(exec_result, context.function_call, context.planning_state)
        tool_msg = []
        if context.planning_state.tool_msg:
            tool_msg.append(context.planning_state.tool_msg)
        if exec_result.get("error_code") == 0:
            tool_msg.append(context.planning_state.dealing_node)
        if len(tool_msg) == 2:
            for item in self.result_output_callback.on_tool_execute_end(tool_msg):
                yield item

    @staticmethod
    def _exec_func(planning_state, function_call, result, exec_latency):
        """process function result"""
        if isinstance(result, dict):
            error_code = result.get("errCode")
            error_message = result.get("errMessage")
        else:
            error_code = (
                result.err_code
                if result is not None and hasattr(result, "err_code")
                else -1
            )
            error_message = (
                result.err_msg
                if result is not None and hasattr(result, "err_msg")
                else "工具执行异常"
            )

        if error_code != 0:
            output = error_message
            function_thought = {
                ROLE: FUNCTION,
                NAME: function_call.get(NAME),
                CONTENT: "工具执行失败",
            }
            # 工具执行失败，需要先把对应的function信息添加到规划状态中
            planning_state.add(function_thought)
        else:
            output = result.get("data")
        return dict(error_code=error_code, result=output, latency=exec_latency)

    @staticmethod
    def search_plugin_and_func_name(function_call, functions):
        """search plugin and func name"""
        plugin_name = ""
        func_name = ""
        for tool in functions:
            final_plugin_name, final_func_name = tool.search_plugin_func_name(
                function_call.tool_id
            )
            if final_plugin_name and final_func_name:
                plugin_name = final_plugin_name
                func_name = final_func_name
        return {"plugin_name": plugin_name, "func_name": func_name}

    def _wrap_func_exist_info(self, function_call: dict, functions):
        """打包function_call函数查询信息"""
        if_non_exist_func = True
        is_workflow_func = (
            get_workflow_func(function_call.get(NAME), functions) is not None
        )
        is_mcp_func = get_mcp_func(function_call.get(NAME), functions) is not None
        return dict(
            not_exist=if_non_exist_func,
            is_workflow=is_workflow_func,
            is_mcp=is_mcp_func,
        )

    async def stream(self, query, prompt_info, functions, **kwargs):
        planning_state = self.context_manager.msg_context
        self.states_info_body = self._get_states_info_body()

        agent_inputs = self.context_manager.get_global_variables("agent_inputs", {})
        memory_vars = self.context_manager.get_global_variables("memory_variables", {})

        template_manager = TemplateManager()
        template = template_manager.get(self.propose.op_config.prompt_template_name)
        # 解析并替换变量引用
        processed_template = resolve_variable_references(
            template.content[0].get("content"), agent_inputs, memory_vars
        )

        # 获取Agent提示词，只能运行时获取
        self.prompt_sys = self.prompt_engine.format(
            prompt_template=[ChatContent(role="system", content=processed_template)],
            keywords=prompt_info,
        )
        self.user_response = query
        debug_info = dict(
            component_metadata=kwargs.get("component_metadata", {}),
            runtime_context=kwargs.get("runtime_context", {}),
            span=kwargs.get("span"),
        )
        index = 0
        # 后续弹出之前历史信息都单独赋值了
        self.chat_history = self.get_chat_history(planning_state)
        userquestion = {ROLE: USER, CONTENT: self.user_response}
        self.chat_history.append(userquestion)
        while index < self._max_iteration:
            # 先进行工具选择，这里暂时不支持退出功能
            time_dict = {
                "total_latency": 0.0,
                "model_latency": None,
                "wait_latency": None,
            }
            # 继续上一次的function_call - 修复第二轮对话工具选择逻辑
            if planning_state.last_func:
                last_tool_name = planning_state.last_func.get(NAME)
                if get_workflow_func(last_tool_name, functions):
                    # 如果上次调用的工具仍然可用，询问是否继续使用
                    res_int = self.get_user_break_int(query)
                    function_call = planning_state.last_func
                    res_fun = function_call[NAME]
                    time_dict = {
                        "total_latency": 0.0,
                        "model_latency": None,
                        "wait_latency": None,
                    }
                else:
                    # 如果上次调用的工具不再可用，重新进行工具选择
                    logger.info(
                        f"上次调用的工具 {last_tool_name} 不再可用，重新进行工具选择"
                    )
                    res_int, res_fun = self.get_user_int_and_tool_name(
                        self.user_response,
                        self.prompt_sys,
                        functions,
                        planning_state,
                        **kwargs,
                    )
            else:
                res_int, res_fun = self.get_user_int_and_tool_name(
                    self.user_response,
                    self.prompt_sys,
                    functions,
                    planning_state,
                    **kwargs,
                )
            if res_int in [0, 1]:
                # 执行正常问答回复
                for item in self._on_pre_invoke_llm(
                    self.chat_history, [], planning_state, **kwargs
                ):
                    yield item
                planning_state.report_message = {
                    RESULT: f"{planning_state.dealing_node.get(CONTENT)}"
                }
                self.states_info_body["tool_choose"] = "没有工具调用意图，正常闲聊"
                self.context_manager.states_info.append(self.states_info_body)
                planning_state.is_terminated = True
                yield RetCode.SUCCESS
                break
            else:
                # 收集参数信息，之前已经判定过了，所以不存在没有工具调用信息的情况
                # 如果进行调用工具，则必须进行参数提取
                # workflow可以不再次提取参数，Workflow先不支持
                func_info = self.get_fun_info(res_fun, functions)
                reconfirm_fields, extracted_fields, llm_info, collectflag = (
                    self.interact_nl2json(func_info)
                )
                self.extracted_key_fields.update(extracted_fields)
            if collectflag:
                # 参数调用齐全，发起调用
                input_arguments = {
                    "arguments": str(self.extracted_key_fields),
                    "name": res_fun,
                }
                func_basc_info = self._wrap_func_exist_info(input_arguments, functions)
                self.chat_history.append(
                    {
                        ROLE: ASS,
                        CONTENT: "需要调用"
                        + str(res_fun)
                        + "工具回答用户信息，参数收集已齐全发起工具调用"
                        + str(input_arguments),
                    }
                )
                try:
                    exec_context = ExecutionContext(
                        exec_result=None,
                        exec_latency=None,
                        search_res=self.search_plugin_and_func_name(
                            func_info, functions
                        ),
                        function_call=input_arguments,
                        planning_state=planning_state,
                        time_dict=time_dict,
                        func_info=func_basc_info,
                    )
                    async for item in self._process_stream_function_call(
                        functions, exec_context, **kwargs
                    ):
                        yield item
                    exec_res, llm_tool_summary = self.llm_tool_summary_with_exception(
                        debug_info, exec_context, kwargs, planning_state, res_fun
                    )
                    # 历史信息补充信息
                    self.chat_history.append(
                        {
                            ROLE: USER,
                            CONTENT: res_fun + "工具调用结果：" + llm_tool_summary,
                            "name": res_fun,
                        }
                    )
                    self.states_info_body["tool_list"].append(res_fun)
                    if llm_tool_summary == "":
                        self.states_info_body["tool_choose"] = res_fun + "工具执行失败"
                    else:
                        self.states_info_body["tool_choose"] = res_fun + "工具执行完毕"
                    self.states_info_body["tool_result_summary"] += (
                        "\n" + res_fun + "工具调用结果：" + llm_tool_summary
                    )
                    self.states_info_body["tool_result"].append(exec_res)
                    self.states_info_body["extracted_key_fields"] = (
                        self.extracted_key_fields
                    )
                    self.context_manager.states_info.append(self.states_info_body)

                    # 工具执行完成后，清除last_func状态，避免下一轮重复使用
                    planning_state.last_func = dict()
                    self.extracted_key_fields = {}
                except JiuWenBaseException:
                    raise
                except Exception as e:
                    logger.error("Exception", e)
                    raise JiuWenBaseException(
                        error_code=StatusCode.WORKFLOW_API_EXECUTE_ERROR.code,
                        message="Flow API executed failed",
                    ) from e
                # 这里进行工具调用状态更新
            else:
                unextracted_field_names = self.gen_unextracted_filed_names(
                    func_info, reconfirm_fields
                )
                default_question = "请您提供{unextracted_field_names}相关的信息".format(
                    unextracted_field_names=", ".join(unextracted_field_names)
                )
                llm_auto_ask = self.get_llm_auto_ask(unextracted_field_names)
                question = llm_auto_ask if llm_auto_ask else default_question
                llm_info.update({"assistant": question})
                self.chat_history.append({ROLE: ASS, CONTENT: question})
                self.states_info_body["tool_choose"] = res_fun + "工具正在参数收集"
                self.states_info_body["extracted_key_fields"] = (
                    self.extracted_key_fields
                )
                self.context_manager.states_info.append(self.states_info_body)
                planning_state.report_message = {RESULT: f"{question}"}
                # 参数存储
                # planning_state.is_terminated = True
            if planning_state.is_terminated:  # 退出ReAct循环
                yield RetCode.SUCCESS
                break
            index += 1
        if index >= self._max_iteration:
            result = self._search_last_function_result(planning_state)
            planning_state.report_message = {
                RESULT: result if result else f"ReAct迭代次数超出最大限制：{index}次"
            }
            yield RetCode.SUCCESS

    def gen_unextracted_filed_names(self, func_info, reconfirm_fields):
        unextracted_field_names = []
        arguments = func_info.invokable.params
        for key in reconfirm_fields:
            for arg in arguments:
                if arg.name == key:
                    unextracted_field_names.append(arg.description)
        return unextracted_field_names

    def llm_tool_summary_with_exception(
        self, debug_info, exec_context, kwargs, planning_state, res_fun
    ):
        try:
            if exec_context.func_info.get(IS_WORKFLOW, False):
                exec_res = exec_context.planning_state.report_message[RESULT]
            else:
                exec_res = exec_context.exec_result["data"]
            if exec_res != "":
                # 工具调用总结，流式输出
                ## 这里需要再次试试，回退旧版本。llm_tool_summary =  yield from self.llm_tool_summary(res_fun, exec_res,debug_info,planning_state,**kwargs)
                llm_tool_summary = self.llm_tool_summary(
                    res_fun, exec_res, debug_info, planning_state, **kwargs
                )
            else:
                llm_tool_summary = "工具调用失败，没有搜索到结果"
        except Exception:
            exec_res = "工具调用失败，没有搜索到结果"
            llm_tool_summary = exec_res
        return exec_res, llm_tool_summary
