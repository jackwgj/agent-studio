#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from fastapi import Request

from jiuwen.common.log.base import logger
from jiuwen.serve.common.context import request

REQUEST_TIMEOUT_WORKFLOW = "request_timeout_workflow"
REQUEST_TIMEOUT_MCP = "request_timeout_mcp"


def set_request_timeout(envs: dict):
    """
    Sets the request timeout value in the given environment dictionary.

    This function retrieves the 'keep-alive' header value from the current request,
    validates it to ensure it's an integer, and then sets it in the provided
    environment dictionary under the keys REQUEST_TIMEOUT_WORKFLOW and
    REQUEST_TIMEOUT_MCP.

    Args:
        envs (dict): The environment dictionary where the timeout values will be set.

    Returns:
        None
    """
    if request.get() is None or not isinstance(request.get(), Request):
        return
    request_timeout = request.get().headers.get("keep-alive")
    if request_timeout is None:
        return
    if isinstance(request_timeout, str):
        try:
            request_timeout = int(request_timeout)
        except ValueError:
            logger.warning(f"keep-alive value: {request_timeout} is not a number")
            return
    elif not isinstance(request_timeout, int):
        logger.warning(f"keep-alive value: {request_timeout} is not int type")
        return
    if request_timeout <= 0:
        logger.warning(f"keep-alive value: {request_timeout} is not greater than 0")
        return
    envs[REQUEST_TIMEOUT_WORKFLOW] = request_timeout
    envs[REQUEST_TIMEOUT_MCP] = request_timeout
