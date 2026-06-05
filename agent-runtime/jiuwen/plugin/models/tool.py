#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
import asyncio
import json
from typing import Union, List, Dict

from jiuwen.common.log.base import logger
from jiuwen.common.status import StatusCode
from jiuwen.common.types import ValueTypeEnum
from jiuwen.context.history import ConversationHistory
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.constant import (
    WORKFLOW_STATE,
    WORKFLOW_STATE_ERROR,
    WORKFLOW_STATE_RUN,
    WORKFLOW_STATE_END,
)
from jiuwen.orchestration.flow.stream.base import StreamCode, StreamData
from jiuwen.orchestration.utils import Input, Output
from jiuwen.planner.common.enum_class import ProviderType
from jiuwen.planner.common.exception import PanelWorkflowRunException
from jiuwen.plugin.models.function import Function
from jiuwen.plugin.models.restfulapi import RestFulAPI

NON_VALID_RESULT = "返回结果为空。"
FUNC_EXEC_ERROR = "工具执行异常"
WORKFLOW_PROMPT = "无需提供properties以外的参数信息"
WORKFLOW_END_TYPE = "jiuwen.end"
WORKFLOW_ANSWER = "answer"


class Tool(Invokable):
    def __init__(
        self,
        tool_id: str,
        provider_type: ProviderType = "",
        invokable: Union[Function, Invokable] = None,
        parameters: Union[List, Dict] = None,
    ):
        self.tool_id = tool_id
        self.provider_type = provider_type
        self.invokable = invokable
        self.parameters = parameters

    @staticmethod
    def _process_stream_output(item):
        """后处理流式输出结果"""
        if not isinstance(item, StreamData) or item.code == StreamCode.ERROR.value:
            message = "panel get stream data error"
            if isinstance(item, StreamData) and isinstance(item.data, dict):
                message = item.data.get("message", "")
            raise PanelWorkflowRunException(message=message)
        if isinstance(item.data, dict):
            answer_dict = item.data.get(WORKFLOW_ANSWER)
            if isinstance(answer_dict, dict) and answer_dict:
                item.data.update({WORKFLOW_ANSWER: answer_dict.get(WORKFLOW_ANSWER)})
        result = dict(answer=item.data, code=item.code, msg=item.msg)
        result.update({WORKFLOW_STATE: WORKFLOW_STATE_RUN})
        return result

    @staticmethod
    def _process_function_exec_res(result):
        """_process_function_exec_res"""
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
        if error_code != StatusCode.SUCCESS.code:
            processed_result = error_message
        else:
            processed_result = result.get("data")
            if processed_result is None:
                processed_result = NON_VALID_RESULT
        return error_code, processed_result

    def search_plugin_func_name(self, name: str):
        """search plugin func name"""
        plugin_name = ""
        func_name = ""
        if self.provider_type == ProviderType.PLUGIN:
            if name == self.invokable.name or self.invokable.name.startswith(
                name + "#*"
            ):
                plugin_name = self.invokable.plugin_name
                func_name = self.invokable.name
        elif self.provider_type == ProviderType.WORKFLOW:
            if name == self.tool_id:
                plugin_name = self.tool_id
                func_name = self.tool_id
        return plugin_name, func_name

    def invoke(self, inputs: Input, **kwargs) -> Output:
        tool_name = inputs.get("tool_name")
        arguments = inputs.get("tool_arguments")

        function_exec_res = None
        if self.provider_type == ProviderType.PLUGIN:
            if tool_name == self.invokable.name or self.invokable.name.startswith(
                tool_name + "#*"
            ):
                function_exec_res = self.function_filter(
                    self.invokable.invoke(json.loads(arguments), **kwargs)
                )
        elif self.provider_type == ProviderType.WORKFLOW:
            function_exec_res = self._exec_workflow(arguments, **kwargs)
        elif self.provider_type == ProviderType.MCP:
            function_exec_res = asyncio.run(
                self.invokable.ainvoke(json.loads(arguments), **kwargs)
            )
        return function_exec_res

    def function_filter(self, invocation_result: dict):
        """过滤visible为false的出参"""
        data = invocation_result.get("data")
        if not data:
            return invocation_result
        if self.invokable is not None and isinstance(self.invokable, RestFulAPI):
            self._function_filter_for_schema(
                self.invokable.response, invocation_result["data"]
            )
        return invocation_result

    def _function_filter_for_schema(self, output_params: list, data: dict):
        for output_param in output_params:
            if not output_param.visible and output_param.name in data:
                del data[output_param.name]
            elif (
                ValueTypeEnum.is_object(output_param.type) and output_param.name in data
            ):
                self._function_filter_for_schema(
                    output_param.schema, data[output_param.name]
                )

    def _get_generate_res(self, inputs, **kwargs):
        """获取迭代器返回消息"""
        err_func_res = dict(
            errMessage={
                WORKFLOW_ANSWER: WORKFLOW_STATE_ERROR,
                WORKFLOW_STATE: WORKFLOW_STATE_ERROR,
            },
            errCode=StatusCode.INTER_ERROR.code,
        )
        try:
            inputs_dict = json.loads(inputs)
        except json.JSONDecodeError:
            inputs_dict = dict()

        query = ""
        if "query" not in inputs_dict:
            logger.error(" workflow inputs do not have any query")
            err_msg = err_func_res.get("errMessage", {})
            err_msg.update({WORKFLOW_ANSWER: " workflow inputs do not have any query"})
        else:
            query = inputs_dict.pop("query")
        kwargs["conversation_history"] = (
            kwargs.get("runtime_context").agent_workflow_context.get(
                "workflow_chat_history", ConversationHistory()
            )
        ).get_all_messages()
        kwargs["plugin_configs"] = {
            "default": kwargs.get("runtime_context").api_config.get("headers", {})
        }
        kwargs.update(dict(global_variables=inputs_dict))
        try:
            res_generator = self.invokable.stream(query, kwargs)
            for func_res in res_generator:
                func_res = self._process_stream_output(func_res)
                if func_res.get(WORKFLOW_ANSWER, {}).get(
                    "node_type"
                ) == WORKFLOW_END_TYPE and func_res.get("code") in [
                    StreamCode.FINISH.value
                ]:
                    func_res.update({WORKFLOW_STATE: WORKFLOW_STATE_END})
                wrap_func_res = dict(
                    data=func_res,
                    errCode=StatusCode.SUCCESS.code,
                    errMessage=StatusCode.SUCCESS.message,
                )
                yield wrap_func_res
        except Exception as e:
            logger.error(
                f"Panel execute workflow error {e.__str__()}",
                simple_log="Panel execute workflow error with Exception",
            )
            err_msg = err_func_res.get("errMessage", {})
            err_msg.update({WORKFLOW_ANSWER: "Exception"})
            yield err_func_res

    def _exec_workflow(self, inputs, **kwargs):
        """执行工作流"""
        yield from self._get_generate_res(inputs, **kwargs)
