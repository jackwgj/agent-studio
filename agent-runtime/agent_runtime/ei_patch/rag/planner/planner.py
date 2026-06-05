#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2023-10-18
@LastEditTime: 2023-10-18 17:01:43
@Description: planning assistance engine interface
"""

from typing import List, Union

from jiuwen.controller.common.enum import RetCode
from jiuwen.orchestration import Invokable
from jiuwen.planner.common.exception import PlannerConfigCheckFailedException
from jiuwen.plugin.models.function import Function
from rag.common.enum_class import PlanCacheKey, PlanStatusCode, PlanStrategyType
from rag.common.exception import PlannerModelMissingException
from rag.common.plan_cache_manager import PlanCacheManager
from rag.models.tool import Tool
from rag.plan_modes import ALL_PLAN_STRATEGIES
from rag.plan_modes.base import StrategyAuxInput
from rag.planner.planner_config import PlanConfig

CODE = "ret_code"
RESULT = "result"
SIGNAL = "control_signal"
REFLECTION = "reflection"


class Planner:
    """
    The interface of the planning assistance engine

    Assist large models to disassemble and solve complex problems more accurately and efficiently
    based on policies and provided plugins.
    """

    def __init__(
        self,
        plan_strategy_type: PlanStrategyType = None,
        task_id: str = None,
        plan_config_file: str = None,
        plan_config: PlanConfig = None,
        model=None,
    ):
        if task_id is None:
            import secrets

            task_id = secrets.token_hex(16)
        self.task_id = task_id
        self.plan_strategy_type = plan_strategy_type
        self.model = model
        self.cache_id = self.task_id
        if not plan_config_file and not plan_config:
            raise PlannerConfigCheckFailedException(
                "Planner should init by plan_configs or plan config file!"
            )
        self.plan_config = (
            PlanConfig.from_config_file(plan_config_file=plan_config_file)
            if plan_config_file
            else PlanConfig()
        )
        if plan_config:
            for key, value in plan_config.__dict__.items():
                if value:
                    setattr(self.plan_config, key, value)

        if not self.model:
            raise PlannerModelMissingException("Planner require model param!")

        self.plan_strategy = ALL_PLAN_STRATEGIES.get(self.plan_strategy_type.value)(
            model=model, plan_config=self.plan_config
        )
        self._init_plan_cache()

    def __call__(
        self,
        query: str,
        prompt_info: dict,
        functions: List[Tool] = None,
        chains: Union[dict, Invokable] = None,
        flows: List[str] = None,
        **kwargs,
    ):
        """
        run planning assistance engine

        Args:
            query (str): query
            prompt_info (dict): prompt format information
            functions (List): accessible functions
            chains (List): accessible chains
            flows (List): accessible flows

        Returns:
            - errorCode, 0,success, non-0, failure
            - result, fill in the result if successful, fill in the error description if failed
        """
        planning_state = PlanCacheManager.get_cache(
            self.cache_id, PlanCacheKey.PLAN_STATE
        )
        plan_status_code = self.plan_strategy(
            query=query,
            prompt_info=prompt_info,
            functions=functions,
            cache_id=self.cache_id,
            aux_input=StrategyAuxInput(flows=flows, chains=chains),
            **kwargs,
        )
        status_code = self._post_process_plan_result(plan_status_code)

        if status_code in [RetCode.SUCCESS, RetCode.WAIT_USER_INPUT]:
            result = planning_state.report_message
            result = result if result is not None else dict(result="")
        else:
            result = {RESULT: "Planning Failure!"}

        if planning_state.is_finished:
            planning_state.clear()
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
    def _get_final_result(input_data, planning_state):
        if input_data == RetCode.SUCCESS:
            report_message = planning_state.report_message
            return_dict = {CODE: input_data, RESULT: report_message.get(RESULT, "")}
        else:
            return_dict = {CODE: RetCode.FAILED, RESULT: input_data}
        if planning_state.is_finished:
            planning_state.clear()
        return return_dict

    def stream(
        self,
        query: str,
        prompt_info: dict,
        functions: List[Union[Function, Tool]] = None,
        chains: Union[dict, Invokable] = None,
        flows: List[str] = None,
        **kwargs,
    ):
        """
        run planning assistance engine, streaming output LLM's output and plugins' output

        Args:
            query (str): query。
            prompt_info (dict): info to be formatted in prompt
            functions (List): accessible functions
            chains (List): accessible chains
            flows (List): accessible flows

        Returns:
            - return dict: contain "ret_code" (RetCode) and specific infos
        """
        planning_state = PlanCacheManager.get_cache(
            self.cache_id, PlanCacheKey.PLAN_STATE
        )
        yield_result = self.plan_strategy.stream(
            query=query,
            prompt_info=prompt_info,
            functions=functions,
            cache_id=self.cache_id,
            aux_input=StrategyAuxInput(flows=flows, chains=chains),
            **kwargs,
        )
        for item in yield_result:
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
                elif "time_consumption" in item:
                    continue
                else:
                    return_dict = self._get_final_result(item, planning_state)
            else:
                return_dict = self._get_final_result(item, planning_state)
            yield return_dict

    def _init_plan_cache(self):
        """initialize planning cache"""
        PlanCacheManager.init(self.cache_id)
