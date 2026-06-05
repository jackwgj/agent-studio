#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import List, Union

from jiuwen.controller.common.enum import RetCode
from jiuwen.planner.common.enum_class import PlanStatusCode
from jiuwen.plugin.models.function import Function
from rag.controller.context_manager import ContextManager
from rag.controller.workspace import WorkSpace
from rag.models.tool import Tool
from rag.plan_modes import ReAct
from rag.planner.planner_config import PlanConfig

CODE = "ret_code"
RESULT = "result"
SIGNAL = "control_signal"
REFLECTION = "reflection"


class Controller:
    def __init__(
        self,
        workspace: WorkSpace = None,
        context_manager: ContextManager = None,
        plan_config: PlanConfig = None,
        model=None,
        plan_model=None,
    ):
        self.context_manager = context_manager
        self.react = ReAct(
            plan_config, model, workspace, context_manager, plan_model=plan_model
        )

    def __call__(
        self, query: str, prompt_info: dict, functions: List[Tool] = None, **kwargs
    ):
        """
        run planning assistance engine

        Args:
            query (str): query
            prompt_info (dict): prompt format information
            functions (List): accessible functions
            flows (List): accessible flows

        Returns:
            - errorCode, 0,success, non-0, failure
            - result, fill in the result if successful, fill in the error description if failed
        """
        msg_context = self.context_manager.msg_context
        plan_status_code = self.react(
            query=query, prompt_info=prompt_info, functions=functions, **kwargs
        )
        status_code = self._post_process_plan_result(plan_status_code)

        if status_code in [RetCode.SUCCESS, RetCode.WAIT_USER_INPUT]:
            result = msg_context.report_message
            result = result if result is not None else dict(result="")
        else:
            result = {RESULT: "Planning Failure!"}

        if msg_context.is_finished:
            msg_context.clear()
        return status_code, result

    @staticmethod
    def _post_process_plan_result(plan_status_code: PlanStatusCode) -> RetCode:
        if plan_status_code == PlanStatusCode.ERROR:
            status_code = RetCode.FAILED
        elif plan_status_code == PlanStatusCode.WAIT_INPUT:
            status_code = RetCode.WAIT_USER_INPUT
        else:
            status_code = RetCode.SUCCESS

        return status_code

    @staticmethod
    def _get_final_result(input_data, msg_context):
        if input_data == RetCode.SUCCESS:
            report_message = msg_context.report_message
            return_dict = {CODE: input_data, RESULT: report_message.get(RESULT, "")}
        else:
            return_dict = {CODE: RetCode.FAILED, RESULT: input_data}
        if msg_context.is_finished:
            msg_context.clear()
        return return_dict

    async def stream(
        self,
        query: str,
        prompt_info: dict,
        functions: List[Union[Function, Tool]] = None,
        **kwargs,
    ):
        """
        run planning assistance engine, streaming output LLM's output and plugins' output

        Args:
            query (str): query。
            prompt_info (dict): info to be formatted in prompt
            functions (List): accessible functions
            flows (List): accessible flows

        Returns:
            - return dict: contain "ret_code" (RetCode) and specific infos
        """
        msg_context = self.context_manager.msg_context
        yield_result = self.react.stream(
            query=query, prompt_info=prompt_info, functions=functions, **kwargs
        )
        async for item in yield_result:
            if isinstance(item, dict):
                if item.get("function_call") is not None:
                    return_dict = dict(
                        ret_code=RetCode.FUNC_CALL_GEN, function_call_generation=item
                    )
                elif item.get("result") is not None:
                    return_dict = dict(
                        ret_code=RetCode.API_EXEC_RESULT, api_result=item
                    )
                elif item.get("error_msg") is not None:
                    return_dict = dict(ret_code=RetCode.API_EXCEPTION, exception=item)
                elif item.get("content_opt_result") is not None:
                    return_dict = dict(
                        ret_code=RetCode.CONT_OPT_RESULT, content_optimization=item
                    )
                elif item.get(SIGNAL) is not None:
                    return_dict = dict(
                        ret_code=RetCode.UX_SIGNAL, control_signal=item.get(SIGNAL)
                    )
                elif item.get("role", "") == "assistant":
                    return_dict = dict(
                        ret_code=RetCode.STREAM_OUTPUT_LLM,
                        stream_output_char=item.get("content", ""),
                    )
                elif item.get(REFLECTION) is not None:
                    return_dict = dict(
                        ret_code=RetCode.STREAM_REFLECTION,
                        reflection=item.get(REFLECTION),
                    )
                elif item.get("tool_msg") is not None:
                    return_dict = dict(
                        ret_code=RetCode.INTERMEDIATE_MESSAGE,
                        intermediate_message=item.get("tool_msg"),
                    )
                else:
                    return_dict = self._get_final_result(item, msg_context)
            else:
                return_dict = self._get_final_result(item, msg_context)
            yield return_dict
