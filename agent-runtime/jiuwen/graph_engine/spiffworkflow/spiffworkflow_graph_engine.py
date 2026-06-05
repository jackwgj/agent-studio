#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import json
from typing import Iterable, Union

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.controller.common.message import Message
from jiuwen.controller.common.message_type import MessageSubType, MessageType
from jiuwen.graph_engine.graph_engine import GraphEngine
from jiuwen.orchestration.flow.stream.base import StreamData
from jiuwen.orchestration.flow.workflow import Workflow


class SpiffWorkflowGraphEngine(GraphEngine):
    def __init__(self, graph_instance: Workflow):
        super(SpiffWorkflowGraphEngine, self).__init__(graph_instance)
        self.graph_instance = graph_instance

    def run_workflow(
        self, inputs, params: dict, **kwargs
    ) -> Iterable[Union[Message, StreamData]]:
        streaming_output = self.graph_instance.stream(inputs, params)
        return _process_streaming_output(origin_output=streaming_output)

    async def arun_workflow(
        self, inputs, params: dict, **kwargs
    ) -> Iterable[Union[Message, StreamData]]:
        streaming_output = await self.graph_instance.astream(inputs, params)
        return _process_streaming_output_async(origin_output=streaming_output)

    def resume_workflow(
        self, inputs, params: dict, **kwargs
    ) -> Iterable[Union[Message, StreamData]]:
        streaming_output = self.graph_instance.stream(inputs, params)
        return _process_streaming_output(origin_output=streaming_output)


def _process_streaming_output(
    origin_output: Iterable[StreamData],
) -> Iterable[Union[Message, StreamData]]:
    """
    将流式数据转换为消息或原始数据。
    Args:
        origin_output (Iterable[StreamData]): 输入的流式数据集合。
    Yields:
        Union[Message, StreamData]: 处理后的消息或原始数据。
    """
    for output in origin_output:
        if is_interrupt(output):
            yield _interrupt_message_wrapper(output.data.get("message"))
        else:
            # 非中断信息，原始数据返回 StreamData
            yield output


async def _process_streaming_output_async(
    origin_output: Iterable[StreamData],
) -> Iterable[Union[Message, StreamData]]:
    async for output in origin_output:
        if is_interrupt(output):
            yield _interrupt_message_wrapper(output.data.get("message"))
        else:
            # 非中断信息，原始数据返回 StreamData
            yield output


def is_interrupt(stream_data: StreamData) -> bool:
    """is interrupted"""
    return (
        stream_data is not None
        and stream_data.data.get("code") == StatusCode.CONTROLLER_INTERRUPT_ERROR.code
    )


def _interrupt_message_wrapper(message_str: str) -> Message:
    """interrupt message wrapper"""
    try:
        message = json.loads(message_str)
    except json.decoder.JSONDecodeError as e:
        raise JiuWenBaseException(
            error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
            message="Interrupt message could not be parsed.",
        ) from e
    message_type = message.get("type", "")
    if message_type in [
        MessageSubType.PLUGIN_PARAM_ERROR.value,
        MessageSubType.PLUGIN_PARAM_MISS.value,
        MessageSubType.PLUGIN_CALL_CONFIRM.value,
        MessageSubType.GLOBAL_INTENT.value,
    ]:
        return Message(
            message_type=MessageType.WORKFLOW_INTERRUPT,
            sub_type=MessageSubType.from_string(message_type),
            content=message_str,
        )
    raise JiuWenBaseException(
        error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
        message="Interrupt message sub type error: {}".format(message_type),
    )
