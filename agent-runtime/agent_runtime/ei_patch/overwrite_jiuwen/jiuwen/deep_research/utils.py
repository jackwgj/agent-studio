# -*- coding: utf-8 -*-

# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""This module defines DeepResearch util functions for ir execution."""

import asyncio
import copy
import json
import os
import time
import traceback
from typing import Iterable, AsyncGenerator

from fastapi.responses import StreamingResponse
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger, performance_logger
from jiuwen.serve.controllers.execution.enum import ResponseMode
from jiuwen.serve.controllers.execution.types import ExecutionData
from jiuwen.serve.schemas.orchestration_mgr import ExecutionRequest

_UTF_8 = "utf-8"


async def execute_deepresearch(req: ExecutionRequest, execution_data: ExecutionData):
    """
    DeepResearch execution request handler.
    """
    deepresearch_agent = execution_data.instance

    # 安全提取并备份 req.params，防止后续流程篡改原始引用
    # 使用 copy.deepcopy 确保彻底隔离
    original_params = copy.deepcopy(req.params)

    if (
        req.params.interrupt_feedback != "accepted"
        and req.params.deepresearch_agent_config.get("has_template", False)
    ):
        result = await deepresearch_agent.generate_template(
            req.params.file_name,
            req.params.file_stream,
            req.params.deepresearch_agent_config.get("is_template", False),
            req.params.deepresearch_agent_config,
        )
        if result.get("status") != "success":
            logger.error(
                f"generate template error with: {result.get('error_message', '')}"
            )

        # 重新赋值LLMkey，避免在生成模板过程中被修改
        req.params = original_params
        req.params.report_template = result.get("template_content", "")

    # 流式调用
    if req.response_mode == ResponseMode.STREAMING:
        resp = deepresearch_streaming_output(req=req, execution_data=execution_data)
        return StreamingResponse(resp, media_type="text/event-stream")

    # 非流式调用
    raise JiuWenBaseException(
        error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
        message="responseMode Blocking is not supported yet.",
    )


async def deepresearch_streaming_output(
    req: ExecutionRequest, execution_data: ExecutionData
):
    """
    Processes the original deepresearch streaming output result in a structured manner.
    Processing may be accompanied by state update operations.
    """
    params = req.params
    deepresearch_agent = execution_data.instance
    event_queue = asyncio.Queue()
    asyncio.create_task(
        _process_deepresearch_streaming_output(
            deepresearch_agent.run(
                message=params.message,
                conversation_id=req.conversation_id,
                report_template=params.report_template,
                interrupt_feedback=params.interrupt_feedback,
                agent_config=params.deepresearch_agent_config,
            ),
            event_queue,
        )
    )
    heartbeat = {
        "conversation_id": req.conversation_id,
        "message_id": "heartbeat",
        "agent": "heartbeat",
        "content": "",
        "message_type": "message_chunk",
        "event": "heartbeat",
    }
    heartbeat_interval = int(os.getenv("DEEP_RESEARCH_HEARTBEAT_INTERVAL", 10))
    try:
        while True:
            try:
                event = await asyncio.wait_for(
                    event_queue.get(), timeout=heartbeat_interval
                )
                if event == "END":
                    break
                yield event
            except asyncio.TimeoutError:
                logger.debug(f"heartbeat for {req.conversation_id}")
                heartbeat["created_time"] = int(round(time.time() * 1000))
                yield f"data: {json.dumps(heartbeat)}\n\n".encode(_UTF_8)
    except Exception:
        error_msg = traceback.format_exc()
        logger.error(error_msg)
    performance_logger.info(f"conversation {req.conversation_id} has ended execution")


async def _process_deepresearch_streaming_output(
    origin_output: Iterable, event_queue
) -> AsyncGenerator:
    """
    Processes the original deepresearch streaming output result in a structured manner.
    """
    try:
        async for item in origin_output:
            await event_queue.put(f"data: {item}\n\n".encode(_UTF_8))
    except Exception:
        error_msg = traceback.format_exc()
        logger.error(error_msg)
    await event_queue.put("END")
