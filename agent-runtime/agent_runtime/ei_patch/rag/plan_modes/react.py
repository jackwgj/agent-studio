#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2024-03-20
@LastEditTime: 2024-03-20 16:40:22
@Description: react plan_modes
"""

import ast
import json
import re
import time
from abc import ABC
from dataclasses import dataclass
from typing import List, Generator, Optional, Union

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.orchestration.flow.constant import WORKFLOW_STATE, WORKFLOW_STATE_RUN
from jiuwen.orchestration.flow.enum import NodeType
from jiuwen.orchestration.flow.stream.base import StreamCode
from jiuwen.planner.common.exception import PlannerConfigCheckFailedException
from jiuwen.planner.common.plan_constants import DEFAULT_MAX_ITERATION, TimerInfo
from jiuwen.planner.common.result_output_callback import ResultOutputStreamCallback
from jiuwen.planner.common.utils import get_workflow_func
from jiuwen.planner.planning_modules import ProposeNext
from mcp.types import TextContent
from rag.EI_ragchain import EIRagChain, rag_config
from rag.EI_tooluse import EIToolUse
from rag.common.enum_class import PlanStatusCode
from rag.controller.msg_context import MsgContext as PlanState
from rag.models.tool import Tool
from rag.plan_modes.base import NON_EXIST_FUNC, BasicStrategy

CHAT_HISTORY_MAX_TURN = 3

ROLE = "role"
USER = "user"
CONTENT = "content"
NAME = "name"
RESULT = "result"
FUNCTION = "function"
PLUGIN_NAME = "plugin_name"
LATENCY = "latency"
RESPONSE_EMPTY_RESULT = "返回结果为空。"
ANSWER = "answer"
IS_WORKFLOW = "is_workflow"
QUERY = "query"


def check_if_query_contain_finish_triggers(query):
    """check whether query contains task-finish trigger words"""
    from jiuwen.common.config import config

    finish_triggers = config.get("planner_config", {}).get("finish_triggers")
    if not finish_triggers:
        raise PlannerConfigCheckFailedException(
            message="finish_triggers in planner config cannot be null!"
        )
    pattern = r"|".join(r"^" + re.escape(word) + r"$" for word in finish_triggers)
    regex = re.compile(pattern, flags=re.IGNORECASE)
    return regex.search(query)


@dataclass
class ExecutionContext:
    exec_result: Optional[dict]
    exec_latency: Optional[float]
    search_res: dict
    function_call: Union[dict, str]
    planning_state: PlanState
    time_dict: dict
    func_info: dict


class ReAct(BasicStrategy, ABC):
    """implementation of ReAct planning"""

    def __init__(self, plan_config, model, workspace, context_manager, plan_model=None):
        super().__init__(plan_config=plan_config)
        self.llm = model
        self.plan_model = plan_model if plan_model else model
        self.workspace = workspace
        self.context_manager = context_manager
        self.propose = ProposeNext(plan_config=plan_config, model=model)
        self.result_output_callback = ResultOutputStreamCallback()
        self._max_iteration = (
            plan_config.constrain_config.max_iteration or DEFAULT_MAX_ITERATION
        )
        self._workflow_switch_enable = plan_config.workflow_switch_enable

    @staticmethod
    def _check_function_name_exist(input_func_name, func_candidates) -> dict:
        """check function name exist"""
        if_exist: bool = False
        func_name: str = ""
        for func in func_candidates:
            if input_func_name in func.tool_id:
                if_exist = True
                func_name = input_func_name  # 用备选工具中的工具名称 or id
                break

        return {
            "if_exist": if_exist,
            "func_name": func_name,
        }

    @staticmethod
    def _post_process_retrieval_result(result):
        return RESPONSE_EMPTY_RESULT if not (result or result == []) else result

    @staticmethod
    def _compress_result(result, function_call, planning_state):
        """"""
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
    def _search_last_function_result(planning_state):
        if planning_state.dealing_node.get(ROLE, "") == FUNCTION:
            res = planning_state.dealing_node.get(CONTENT)
            return res if RESPONSE_EMPTY_RESULT not in res else ""
        return ""

    def invoke(self, query, prompt_info, functions, **kwargs):
        planning_state = self.context_manager.msg_context
        if check_if_query_contain_finish_triggers(query):
            planning_state.add({"role": "user", "content": query})
            return PlanStatusCode.Finish

        self._add_query(query, planning_state)
        for _ in range(self._max_iteration):
            self.propose(
                query=query,
                prompt_info=prompt_info,
                functions=functions,
                planning_state=planning_state,
                **kwargs,
            )
            function_call = planning_state.dealing_node.get("function_call", {})
            if not function_call:
                planning_state.report_message = {
                    RESULT: f"{planning_state.dealing_node.get(CONTENT)}"
                }
                planning_state.is_terminated = True
            else:
                function_call[NAME] = function_call[NAME].strip()
                planning_state.dealing_node["function_call"] = function_call
                error_code, func_result = self._invoke_func_execution(
                    function_call, functions, planning_state, **kwargs
                )
                if not self.is_valid_function_response(error_code, func_result):
                    self._post_process_compress_result(func_result, planning_state)
                    planning_state.is_terminated = True
                    return PlanStatusCode.WAIT_INPUT

            if planning_state.is_terminated:
                return PlanStatusCode.WAIT_INPUT
        result = self._search_last_function_result(planning_state)
        planning_state.report_message = {
            RESULT: result
            if result
            else f"ReAct迭代次数超出最大限制：{self._max_iteration}次"
        }
        return PlanStatusCode.WAIT_INPUT

    async def stream(self, query, prompt_info, functions, **kwargs):
        # 这里已经进行初步判别了
        if self.plan_config.choose_mode == "ToolUse":
            tool_use_chain = EIToolUse(
                self.plan_config,
                self.llm,
                self.context_manager,
                plan_model=self.plan_model,
            )
            async for item in tool_use_chain.stream(
                query, prompt_info, functions, **kwargs
            ):
                yield item
        elif self.plan_config.choose_mode == "RAG":
            planning_state = self.context_manager.msg_context
            self._add_query(query, planning_state)
            Rag_chain_config = rag_config["Rag_chain_config"]
            rag_chain = EIRagChain(self.llm, Rag_chain_config, functions)
            async for item in rag_chain.stream(query, planning_state, **kwargs):
                yield item
        else:
            raise JiuWenBaseException(
                error_code=StatusCode.AGENT_INPUT_TYPE_ERROR.code,
                message=StatusCode.AGENT_INPUT_TYPE_ERROR.errmsg.format(
                    self.plan_config.choose_mode, "Unknown agent type"
                ),
            ) from e

    def _process_stream_function_call(
        self, functions: list, exec_context: ExecutionContext, **kwargs
    ):
        exec_result, exec_latency = self._exec_function(
            functions, exec_context.function_call, **kwargs
        )
        exec_context.exec_result = exec_result
        exec_context.exec_latency = exec_latency
        # 创建 ExecutionContext 对象
        if exec_context.func_info.get(IS_WORKFLOW, False):
            # 执行工作流流式输出
            yield from self._exec_stream_workflow(exec_context)
        else:
            # 执行插件流式输出
            yield from self._exec_stream_function(exec_context)

    def _exec_stream_workflow(self, context: ExecutionContext):
        """执行工作流流式输出"""
        (
            react_total_result,
            react_report_answer,
        ) = yield from self._post_process_stream_workflow(
            context.function_call,
            context.planning_state,
            context.exec_result,
            context.search_res,
            context.time_dict,
        )
        if react_total_result:
            context.time_dict["total_latency"] += react_total_result.get(LATENCY, 0.0)
            yield from self.result_output_callback.on_function_call_generated(
                func_call=context.function_call,
                time_info=TimerInfo(**context.time_dict),
                workflow_state=True,
            )
            yield from self._stream_out_tool_exec_result(
                react_total_result, context.search_res, context.function_call
            )
        context.planning_state.report_message = {RESULT: react_report_answer}
        context.planning_state.is_terminated = True
        react_tool_msg = []
        if context.planning_state.tool_msg:
            react_tool_msg.append(context.planning_state.tool_msg)
        react_tool_msg.append(
            {
                ROLE: FUNCTION,
                NAME: context.function_call.get(NAME),
                CONTENT: json.dumps(
                    context.planning_state.report_message, ensure_ascii=False
                ),
            }
        )
        if len(react_tool_msg) == 2:
            yield from self.result_output_callback.on_tool_execute_end(react_tool_msg)

    def _exec_stream_function(self, context: ExecutionContext):
        """执行插件流式输出"""
        react_exec_result = self._exec_func(
            context.planning_state,
            context.function_call,
            context.exec_result,
            context.exec_latency,
        )
        context.time_dict["total_latency"] += react_exec_result.get(LATENCY, 0.0)
        yield from self.result_output_callback.on_function_call_generated(
            func_call=context.function_call,
            time_info=TimerInfo(**context.time_dict),
            workflow_state=False,
        )
        yield from self._stream_out_tool_exec_result(
            react_exec_result, context.search_res, context.function_call
        )
        self._post_process(
            react_exec_result, context.function_call, context.planning_state
        )
        react_tool_msg = []
        if context.planning_state.tool_msg:
            react_tool_msg.append(context.planning_state.tool_msg)
        if react_exec_result.get("error_code") == 0:
            react_tool_msg.append(context.planning_state.dealing_node)
        if len(react_tool_msg) == 2:
            yield from self.result_output_callback.on_tool_execute_end(react_tool_msg)

    def _post_process_stream_workflow(
        self, function_call, planning_state, func_res, search_res, time_dict
    ):
        """后处理工作流流式结果"""
        react_report_answer, react_total_result = "", None
        if isinstance(func_res, Generator):
            last_item = None
            start_time = time.time()
            for react_item in func_res:
                react_item = (
                    dict(errCode=-1, errMessage=NON_EXIST_FUNC)
                    if not react_item
                    else react_item
                )
                react_item_res = self._exec_func(
                    planning_state, function_call, react_item, None
                )
                react_last_item = react_item_res
                if (
                    react_item_res.get(RESULT, {}).get("code")
                    != StreamCode.MESSAGE_END.value
                ):
                    if react_item_res.get("error_code") != 0:
                        yield from self.result_output_callback.on_function_call_generated(
                            func_call=function_call,
                            time_info=TimerInfo(**time_dict),
                            workflow_state=True,
                        )
                        yield from self._stream_out_tool_exec_result(
                            react_item_res, search_res, function_call
                        )
                    else:
                        answer_dict = react_item_res.get(RESULT, {}).get(ANSWER, {})
                        # 只有code为StreamCode.FINISH才输出，防止前端接收到不必要的信息
                        if (
                            react_item_res.get(RESULT, {}).get("code", {})
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
                    not react_item_res.get(RESULT, {})
                    .get(ANSWER, {})
                    .get("parentNodeId", "")
                    or react_item_res.get(RESULT, {})
                    .get(ANSWER, {})
                    .get("node_type", "")
                    == NodeType.QUESTIONER.value
                ):
                    react_report_answer += (
                        react_item_res.get(RESULT, {}).get(ANSWER, {}).get(ANSWER)
                    )
            exec_latency = round(time.time() - start_time, 2)
            workflow_state = react_last_item.get(RESULT, {}).get(WORKFLOW_STATE)
            if workflow_state != WORKFLOW_STATE_RUN:
                # 重置 function_call
                self._reset_pending_functions(function_call, planning_state)
            if react_item_res.get("error_code") != 0:
                react_report_answer = react_last_item.get(RESULT, {}).get(ANSWER, {})
            else:
                react_total_result = {
                    RESULT: {
                        ANSWER: react_report_answer,
                        WORKFLOW_STATE: workflow_state,
                    },
                    LATENCY: exec_latency,
                }
        if planning_state.last_func:
            self.context_manager.workflow_contexts.append(planning_state.last_func)
        if planning_state.functions:
            self.context_manager.workflow_functions.append(planning_state.functions)
        return react_total_result, react_report_answer

    def _reset_pending_functions(self, function_call, planning_state):
        planning_state.last_func = dict()
        if planning_state.functions.get(function_call.get(NAME), None):
            del planning_state.functions[function_call.get(NAME)]

    def _wrap_func_exist_info(self, function_call: dict, functions: List[Tool]):
        """打包function_call函数查询信息"""
        if_non_exist_func = not self._check_function_name_exist(
            function_call.get(NAME), functions
        ).get("if_exist")
        is_workflow_func = (
            get_workflow_func(function_call.get(NAME), functions) is not None
        )
        return dict(not_exist=if_non_exist_func, is_workflow=is_workflow_func)

    def _reflect_nonexist_function(self, planning_state, function_call, time_dict):
        """reflect nonexistent function"""
        function_thought = {
            ROLE: FUNCTION,
            NAME: function_call.get(NAME),
            CONTENT: "大模型生成不存在的工具，无法执行",
        }
        planning_state.add(function_thought)
        yield from self.result_output_callback.on_function_call_generated(
            function_call, time_info=TimerInfo(**time_dict)
        )
        yield from self.result_output_callback.on_tool_executed(
            "无有效的插件名",
            function_call.get(NAME),
            "大模型生成不存在的工具，无法执行",
            False,
            TimerInfo(total_latency=0.0),
        )

    def _add_query(self, query, planning_state):
        if self.plan_config.propose_next_config.is_chat:
            user_thought = {ROLE: USER, CONTENT: query}
            planning_state.add(user_thought)

    def _exec_function(self, functions, function_call, **kwargs):
        args = function_call.get("arguments", "{}")
        check_result, trans_result = ModelUtil.check_and_trans2json(args)
        args = trans_result if check_result else ast.literal_eval(args)
        function_call["arguments"] = json.dumps(args, ensure_ascii=False)
        return self.execute_function_from_function_call(
            function_call, functions, **kwargs
        )

    def _invoke_func_execution(
        self, function_call, functions, planning_state, **kwargs
    ):
        result, _ = self.execute_function_from_function_call(
            function_call, functions, **kwargs
        )
        error_code, compress_result = self.process_function_exec_res(result)

        function_thought = {
            ROLE: FUNCTION,
            NAME: function_call.get(NAME),
            CONTENT: compress_result
            if isinstance(compress_result, str)
            else json.dumps(compress_result, ensure_ascii=False),
        }

        planning_state.add(function_thought)

        return error_code, compress_result

    def _post_process(self, exec_result, function_call, planning_state):
        if exec_result.get("error_code") == 0:
            result = self._post_process_retrieval_result(exec_result.get(RESULT))
            self._compress_result(result, function_call, planning_state)

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

    def _on_pre_invoke_llm(self, query, prompt_info, functions, time_dict, **kwargs):
        yield from self.result_output_callback.on_llm_output_stream_started()
        yield_res = self.propose.stream(
            query=query, prompt_info=prompt_info, functions=functions, **kwargs
        )
        yield from self._format_yield_from_llm_output_with_timer(yield_res, time_dict)
