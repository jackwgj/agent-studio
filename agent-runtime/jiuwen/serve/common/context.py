# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
This module provides a set of context variables and utility functions for managing request-specific data in an
asynchronous FastAPI application. It uses the `contextvars` module to store and retrieve request data, global
variables, and JSON data in a way that is safe for concurrent execution.
"""

import asyncio
import contextvars
from functools import wraps
from typing import Callable, Any

from fastapi import Request

request: contextvars.ContextVar[Request | None] = contextvars.ContextVar(
    "request", default=None
)
"""
存储当前请求对象的上下文变量。

类型: ContextVar[Request | None]
默认值: None
"""

g: contextvars.ContextVar[dict] = contextvars.ContextVar("g", default={})
"""
存储全局变量字典的上下文变量。

类型: ContextVar[dict]
默认值: 空字典
"""

request_json: contextvars.ContextVar[dict] = contextvars.ContextVar(
    "request_json", default={}
)
"""
存储请求JSON数据的上下文变量。

类型: ContextVar[dict]
默认值: 空字典
"""

request_ctx: contextvars.ContextVar[dict] = contextvars.ContextVar(
    "request_ctx", default={}
)
"""
存储请求上下文数据的上下文变量。

类型: ContextVar[dict]
默认值: 空字典
"""


def copy_current_request_context(func: Callable) -> Callable:
    """ "copy_current_request_context"""
    current_context = contextvars.copy_context()

    @wraps(func)
    def wrapper(*args: Any, **kwargs: Any) -> Any:
        return current_context.run(func, *args, **kwargs)

    @wraps(func)
    async def async_wrapper(*args: Any, **kwargs: Any) -> Any:
        return await current_context.run(func, *args, **kwargs)

    return async_wrapper if asyncio.iscoroutinefunction(func) else wrapper


def is_json(req: Request) -> bool:
    """is_json"""
    content_type = req.headers.get("Content-Type")
    return content_type is not None and "application/json" in content_type
