#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.

"""
This module provides some common methods for custom components.
"""

from typing import Generator

from jiuwen.orchestration.callbacks.span import Span
from jiuwen.orchestration.flow.components.util import (
    get_data_of_streaming_with_metadata,
)
from jiuwen.orchestration.flow.constant import USER_FIELDS
from jiuwen.orchestration.flow.enum import StreamDataMsg
from jiuwen.orchestration.flow.model.workflow_data_class import WorkflowMetadata
from jiuwen.orchestration.flow.stream.base import StreamData, StreamCode


def stream_output_to_event(metadata: WorkflowMetadata, result: Generator, span: Span):
    """
    Convert generator of AIMessage class into StreamData class.

    metadata: WorkflowMetadata class, includes node id, type and name.
    result: Generator consisting of AIMessage structure.
    span: Span class, includes span_id, trace_id and parent_id.
    """
    final_output = ""
    execution_id = span.trace_id if span else ""
    partial_content_index = 0
    for item in result:
        if item.usage_metadata.code != 0:
            yield StreamData(
                code=StreamCode.ERROR.value,
                msg=item.usage_metadata.errmsg,
                data=get_data_of_streaming_with_metadata(answer="", metadata=metadata),
                execution_id=execution_id,
            )
        if item.usage_metadata.finish_reason == "null":
            yield StreamData(
                code=StreamCode.PARTIAL_CONTENT.value,
                msg=StreamDataMsg.SUCCESS.value,
                data=get_data_of_streaming_with_metadata(
                    answer=item.content, metadata=metadata
                ),
                execution_id=execution_id,
                index=partial_content_index,
            )
            partial_content_index += 1
            final_output = final_output + str(item.content)
        if item.usage_metadata.finish_reason == "stop" and not final_output:
            yield StreamData(
                code=StreamCode.PARTIAL_CONTENT.value,
                msg=StreamDataMsg.SUCCESS.value,
                data=get_data_of_streaming_with_metadata(
                    answer=item.content, metadata=metadata
                ),
                execution_id=execution_id,
            )
            final_output = str(item.content)
    yield StreamData(
        code=StreamCode.FINISH.value,
        msg=StreamDataMsg.FINISH.value,
        data=get_data_of_streaming_with_metadata(
            answer={USER_FIELDS: final_output}, metadata=metadata
        ),
        execution_id=execution_id,
    )
