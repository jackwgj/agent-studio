#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2024-05-15
@LastEditTime: 2024-05-15 14:54:38
@Description: stream post
"""

import copy
import json
import time
from typing import List

from jiuwen.common.llm_service.language_model.base import (
    LanguageModelInput,
    BaseChatModel,
)
from jiuwen.common.llm_service.messages import AIMessage
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.insight.manager import TraceManager
from jiuwen.planner.common.exception import LLMInvocationFailedException
from jiuwen.planner.common.plan_constants import LlmOutput, FunctionCallSchema

LATENCY = "latency"
MODEL_LATENCY = "model_latency"
TOTAL_LATENCY = "total_latency"
WAIT_LATENCY = "wait_latency"
CONTENT = "content"
MODEL_STAT = "model_stat"


def convert_ai_message_to_llm_output(llm_message: AIMessage) -> LlmOutput:
    """convert AIMessage to LLM output"""
    role = "assistant" if llm_message.type == "ai" else llm_message.type
    content = llm_message.content if isinstance(llm_message.content, str) else ""
    if not llm_message.tool_calls:
        return LlmOutput(role=role, content=content)
    function_name = llm_message.tool_calls.name
    if not function_name:
        logger.info(
            f"After invoke LLM, No function call, Only content: {dict(content=content)}"
        )
        return LlmOutput(role=role, content=content)

    function_call = FunctionCallSchema(
        name=function_name, tool_call_id=llm_message.tool_calls.id
    )
    try:
        arguments = json.dumps(llm_message.tool_calls.args, ensure_ascii=False)
        function_call.arguments = arguments
        logger.info(
            f"After invoke LLM, content is generated, function call result = {function_call.dict()}"
        )
    except ValueError:
        logger.error("Failed to dump LLM generated function call's arguments")
    return LlmOutput(role=role, content=content, function_call=function_call)


def _format_yield_result(result, llm_output, time_dict, model_stat=None):
    total_time = 0.0
    start_time = time.time()
    llm_output.update(dict(role="assistant", content="", latency=0.0))
    for item in result:
        total_time += (
            (time.time() - start_time) if (time.time() - start_time) > 0 else 0.0
        )
        if isinstance(item, AIMessage):
            if item.usage_metadata.finish_reason in [
                "stop",
                "function_call",
            ]:  # only process last message
                llm_output.update(
                    convert_ai_message_to_llm_output(item).dict(exclude_defaults=True)
                )
            else:
                if item.content:
                    yield dict(role="assistant", content=item.content)
        start_time = time.time()
    yield dict(llm_output=llm_output)
    time_dict.update(dict(total_latency=round(total_time, 2)))


def _process_time_info_by_llm_output(llm_output, time_dict):
    if llm_output.get(LATENCY):
        time_dict[MODEL_LATENCY] = round(llm_output.get(LATENCY), 2)
        if time_dict.get(MODEL_LATENCY) > time_dict.get(TOTAL_LATENCY):
            time_dict[TOTAL_LATENCY] = time_dict.get(MODEL_LATENCY)
        time_dict[WAIT_LATENCY] = round(
            time_dict.get(TOTAL_LATENCY) - time_dict.get(MODEL_LATENCY), 2
        )
        time_dict[WAIT_LATENCY] = (
            0.0 if time_dict[WAIT_LATENCY] < 0.0 else time_dict[WAIT_LATENCY]
        )
    else:
        time_dict[MODEL_LATENCY] = None
        time_dict[WAIT_LATENCY] = None
    yield dict(time_consumption=time_dict)


def create_trace_manager(llm: BaseChatModel, functions, trace_handlers):
    """create trace manager"""
    model_class = type(llm).__name__
    model_name = llm.model_name if hasattr(llm, "model_name") else "undefined"

    return TraceManager.generate_manager(
        trace_handlers,
        {
            "class_name": model_class,
            "instance_attributes": dict(
                model=model_name,
                temperature=llm.temperature if hasattr(llm, "temperature") else 1.0,
                top_p=llm.top_p if hasattr(llm, "top_p") else 0.95,
                functions=functions,
            ),
        },
    )


def insert_contexts_before_user_query(contexts, messages):
    """insert contexts before user query"""
    insert_index = -1
    for index, item in enumerate(messages):
        if item.get("role", "") != "system":
            insert_index = index
            break
    if insert_index >= 0:
        user_msgs = contexts[0::2]
        assistant_msgs = contexts[1::2]
        contexts_msg = []
        for user, assistant in zip(user_msgs, assistant_msgs):
            contexts_msg.append(dict(role="user", content=user))
            contexts_msg.append(dict(role="assistant", content=assistant))
        return messages[:insert_index] + contexts_msg + messages[insert_index:]
    return messages


def stream_function_call(
    messages: List[dict],
    functions,
    llm: BaseChatModel,
    contexts: List[str] = None,
    trace_handlers=None,
    **kwargs,
):
    """Get llm stream output with functions"""
    if contexts:
        messages = insert_contexts_before_user_query(contexts, messages)
    trace_manager = create_trace_manager(llm, functions, trace_handlers)
    trace_manager.on_llm_start(messages)

    llm_output, time_dict, model_stat = dict(), dict(), dict()
    try:
        result = llm.stream(
            LanguageModelInput(
                messages=ModelUtil.switch_message(messages), tools=functions
            ),
            **kwargs,
        )
        yield from _format_yield_result(result, llm_output, time_dict, model_stat)
    except Exception as e:
        trace_manager.on_llm_error(e)
        raise LLMInvocationFailedException("Failed to stream calling LLM") from e
    yield from _process_time_info_by_llm_output(llm_output, time_dict)

    # display output on insights, including timer info
    output_on_insights: dict = copy.deepcopy(llm_output)
    output_on_insights.update({LATENCY: time_dict})
    output_on_insights.update({MODEL_STAT: model_stat})
    trace_manager.on_llm_end(output_on_insights)
