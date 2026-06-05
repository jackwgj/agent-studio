# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""This module defines DeepResearch util functions for ir execution."""

import traceback
from typing import Iterable, AsyncGenerator

from fastapi.responses import StreamingResponse
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.log.base import logger, performance_logger
from jiuwen.common.security.cryptor import Crypt
from jiuwen.serve.controllers.execution.enum import ResponseMode
from jiuwen.serve.controllers.execution.types import ExecutionData
from jiuwen.serve.schemas.orchestration_mgr import ExecutionRequest

_UTF_8 = "utf-8"


async def execute_deepresearch(req: ExecutionRequest, execution_data: ExecutionData):
    """
    DeepResearch execution request handler.
    """
    deepresearch_agent = execution_data.instance
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
        req.params.report_template = result.get("template_content", "")

        # 需要重新赋值llm_api_key
        curr_work_api = Crypt()
        llm_config = req.params.deepresearch_agent_config.get("llm_config", {})
        llm_dict = ModelFactory().model_resolver(
            llm_config.get("model_name"), llm_config.get("model_type")
        )
        llm_config["api_key"] = bytearray(
            curr_work_api.decrypt(llm_dict.get("api_key", "")), encoding="utf-8"
        )

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
    try:
        async for output_data in _process_deepresearch_streaming_output(
            deepresearch_agent.run(
                message=params.message,
                conversation_id=req.conversation_id,
                report_template=params.report_template,
                interrupt_feedback=params.interrupt_feedback,
                agent_config=params.deepresearch_agent_config,
            )
        ):
            yield output_data
    except Exception:
        error_msg = traceback.format_exc()
        logger.error(type(error_msg))
    performance_logger.info(f"conversation {req.conversation_id} has ended execution")


async def _process_deepresearch_streaming_output(
    origin_output: Iterable,
) -> AsyncGenerator:
    """
    Processes the original deepresearch streaming output result in a structured manner.
    """
    async for item in origin_output:
        yield f"data: {item}\n\n".encode(_UTF_8)
