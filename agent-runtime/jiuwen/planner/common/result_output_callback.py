#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2024-03-07
@LastEditTime: 2024-03-07 17:06:16
@Description: callback interface for stream output
"""

import json
from abc import ABC, abstractmethod

from jiuwen.controller.common.enum import OutputResultType, ControlSignal
from jiuwen.planner.common.plan_constants import TimerInfo, SPLITER


class BaseResultOutputCallback(ABC):
    """
    abstract class of callback interfaces, when output result
    """

    @abstractmethod
    def on_llm_output_stream_started(self):
        """
        interfaces of stream begin to output
        """
        pass

    @abstractmethod
    def on_llm_output_stream_end(self, generation_type: OutputResultType):
        """
        interfaces of stream output finish
        """
        pass

    @abstractmethod
    def on_function_call_generated(self, func_call: str, time_info: TimerInfo):
        """
        interfaces of output function call
        """
        pass

    @abstractmethod
    def on_llm_content_generated(self, content: str, time_info: TimerInfo):
        """
        interfaces of output llm content
        """
        pass

    @abstractmethod
    def on_tool_executed(
        self,
        plugin_name: str,
        function_name: str,
        result_data,
        if_success: bool,
        time_info: TimerInfo,
    ):
        """
        interfaces of tool execution result
        """
        pass


class ResultOutputStreamCallback(BaseResultOutputCallback):
    """
    default implementation of callback interfaces, when output result
    """

    def on_llm_output_stream_started(self):
        """
        interfaces of stream begin to output
        """
        yield {"control_signal": ControlSignal.STREAM_START}

    def on_llm_output_stream_end(self, generation_type: OutputResultType):
        """
        interfaces of stream output finish
        """
        if generation_type == OutputResultType.LLM_FUNCTION_CALL:
            yield {"control_signal": ControlSignal.FUNCTION_CALL_END}
        elif generation_type == OutputResultType.LLM_CONTENT:
            yield {"control_signal": ControlSignal.CONTENT_GEN_END}

    def on_function_call_generated(
        self, func_call: str, time_info: TimerInfo, workflow_state: bool = False
    ):
        """
        interfaces of output function call
        """
        yield {
            "function_call": func_call,
            "time_consumption": time_info,
            "is_workflow": workflow_state,
        }  # 附带计时信息

    def on_llm_content_generated(self, content: str, time_info: TimerInfo):
        """
        interfaces of output llm content
        """
        yield {
            "reflection": f"{content}{SPLITER}{json.dumps(time_info, ensure_ascii=False)}",
            "time_consumption": time_info,
        }

    def on_tool_executed(
        self,
        plugin_name: str,
        function_name: str,
        result_data,
        if_success: bool,
        time_info: TimerInfo,
    ):
        """
        interfaces of tool execution result
        """
        if if_success:
            yield {
                "plugin_name": plugin_name,
                "function_name": function_name,
                "result": result_data,
                "latency": time_info.total_latency,
            }
        else:
            yield {
                "plugin_name": plugin_name,
                "function_name": function_name,
                "result": result_data,
                "latency": time_info.total_latency,
            }

    def on_tool_execute_end(self, tool_msg):
        """
        为工具调用命令消息和工具执行结果消息增加新的事件类型`INTERMEDIATE_MESSAGE`流式传出
        """
        intermediate_message = {"tool_msg": tool_msg}
        yield intermediate_message
