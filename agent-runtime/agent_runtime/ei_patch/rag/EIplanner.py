from typing import List, Union

from jiuwen.controller.common.enum import RetCode
from jiuwen.orchestration import Invokable
from jiuwen.planner.common.enum_class import PlanCacheKey
from jiuwen.planner.common.plan_cache_manager import PlanCacheManager
from jiuwen.plugin.models.function import Function
from rag.models.tool import Tool
from rag.plan_modes.base import StrategyAuxInput

CODE = "ret_code"
RESULT = "result"
SIGNAL = "control_signal"
REFLECTION = "reflection"


def ei_stream(
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
    planning_state = PlanCacheManager.get_cache(self.cache_id, PlanCacheKey.PLAN_STATE)
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
                return_dict = dict(ret_code=RetCode.API_EXEC_RESULT, api_result=item)
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
                    ret_code=RetCode.STREAM_REFLECTION, reflection=item.get(REFLECTION)
                )
            elif "time_consumption" in item:
                continue
            else:
                return_dict = self._get_final_result(item, planning_state)
        else:
            return_dict = self._get_final_result(item, planning_state)
        yield return_dict
