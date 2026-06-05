#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2023-10-18
@LastEditTime: 2023-10-18 17:01:52
@Description: Assembling Planning Engine Operators Based on Policies
"""

import json
import time
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Union, List

from jiuwen.orchestration import Invokable
from jiuwen.planner.common.plan_runtime_context import PlanRuntimeContext

ROLE = "role"
CONTENT = "content"
RESULT = "result"
ASSISTANT = "assistant"
NAME = "name"
NON_EXIST_FUNC = "大模型生成不存在的工具，无法执行"
NON_VALID_RESULT = "返回结果为空。"
FUNC_EXEC_ERROR = "工具执行异常"


@dataclass
class StrategyAuxInput:
    """auxiliary info for planning plan_modes input"""

    flows: List[str] = None
    chains: Union[dict, Invokable] = None


class StrategyBase(ABC):
    """
    Base Class for planning plan_modes
    """

    def __call__(self, query, prompt_info, functions, **kwargs):
        return self.invoke(query, prompt_info, functions, **kwargs)

    @staticmethod
    def execute_function_from_function_call(function_call, functions, **kwargs):
        """execute function from function call"""
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
                function_exec_res = func.invoke(
                    inputs={"tool_name": tool_name, "tool_arguments": tool_arguments},
                    runtime_auth={
                        "headers": runtime_context.api_config.get("headers", {})
                    },
                    **kwargs,
                )
                exec_latency = round(time.time() - start_time, 2)  # 工具执行耗时
                break
        return function_exec_res, exec_latency

    @staticmethod
    def search_plugin_and_func_name(function_call, functions):
        """search plugin and func name"""
        plugin_name = ""
        func_name = ""
        for tool in functions:
            final_plugin_name, final_func_name = tool.search_plugin_func_name(
                function_call.get(NAME)
            )
            if final_plugin_name and final_func_name:
                plugin_name = final_plugin_name
                func_name = final_func_name
        return {"plugin_name": plugin_name, "func_name": func_name}

    @staticmethod
    def process_function_exec_res(result):
        """process function execute result"""
        if isinstance(result, dict):
            error_code = result.get("errCode", -1)
            error_message = result.get("errMessage", FUNC_EXEC_ERROR)
        else:
            error_code = (
                result.err_code
                if result is not None and hasattr(result, "err_code")
                else -1
            )
            error_message = (
                result.err_msg
                if result is not None and hasattr(result, "err_msg")
                else FUNC_EXEC_ERROR
            )
        if error_code != 0:
            processed_result = error_message
        else:
            processed_result = result.get("data")
            if processed_result is None:
                processed_result = NON_VALID_RESULT
        return error_code, processed_result

    @staticmethod
    def is_valid_function_response(error_code, result):
        """check is valid function response"""
        return result != NON_VALID_RESULT and error_code == 0

    @abstractmethod
    def invoke(self, query, prompt_info, functions, **kwargs):
        """Do invoke"""
        pass

    @abstractmethod
    def stream(self, query, prompt_info, functions, **kwargs):
        """
        stream output planning plan_modes's result
        """
        pass


class BasicStrategy(StrategyBase, ABC):
    """Basic plan_modes"""

    def __init__(self, plan_config):
        super().__init__()
        self.plan_config = plan_config

    @staticmethod
    def _format_yield_from_llm_output_with_timer(result, time_dict: dict):
        for item in result:
            if isinstance(item, dict) and "time_consumption" in item:
                time_dict.update(item.get("time_consumption"))
            else:
                yield item

    @staticmethod
    def _post_process_compress_result(compress_result, planning_state):
        ret_value = compress_result.replace('"', "").replace("[", "").replace("]", "")
        if ret_value in [NON_VALID_RESULT, NON_EXIST_FUNC]:
            planning_state.add(
                role=ASSISTANT, thought={ROLE: ASSISTANT, CONTENT: f"{ret_value}"}
            )
            planning_state.report_message = {RESULT: f"{ret_value}"}
        else:
            planning_state.add(
                role=ASSISTANT, thought={ROLE: ASSISTANT, CONTENT: FUNC_EXEC_ERROR}
            )
            planning_state.report_message = {RESULT: FUNC_EXEC_ERROR}

    def stream(self, query, prompt_info, functions, **kwargs):
        """stream"""
        yield
