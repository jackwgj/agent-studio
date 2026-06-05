#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
exception handler
"""

import re
import traceback
from functools import wraps, partial
from typing import Type

from fastapi.responses import JSONResponse
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from pydantic import ValidationError

CODE = "code"
MESSAGE = "message"
HIDE_PATTERN = re.compile(r" object at 0x[0-9A-Fa-f]+")


class ExceptionHandler:
    """服务器异常处理类"""

    @staticmethod
    def process_exception(func, err):
        """
        处理异常

        根据异常类型进行处理，并返回相应的JSON响应。

        Args:
            func (function): 抛出异常的函数。
            err (Exception): 抛出的异常对象。

        Returns:
            JSONResponse: 包含错误代码和错误信息的JSON响应。

        """
        if isinstance(err, ValidationError):
            err: ValidationError
            err_msg = []
            log_err_msg = []
            sensitive_fields = {"password", "pass", "pwd", "secret"}
            data = err.errors()
            for i in data:
                loc = "".join(
                    ("." + seg) if isinstance(seg, str) else f"[{seg}]"
                    for seg in i["loc"]
                ).lstrip(".")
                err_msg.append(loc + ": " + i["msg"])
                # 检查是否包含敏感字段
                field_name = loc.split(".")[0]  # 检查顶层字段名
                if field_name in sensitive_fields:
                    safe_loc = "***"
                else:
                    safe_loc = loc
                log_err_msg.append(safe_loc + ": " + i["msg"])
            logger.error(
                f"{func.__name__} failed with {len(err_msg)} error(s):\n"
                + "\n".join(err_msg),
                simple_log="Process exception error.",
            )
            return JSONResponse(
                content={
                    CODE: StatusCode.PARAM_CHECK_FAILED_ERROR.code,
                    MESSAGE: StatusCode.PARAM_CHECK_FAILED_ERROR.errmsg
                    + ". Fields: "
                    + "; ".join(err_msg),
                },
                status_code=400,
            )
        if isinstance(err, JiuWenBaseException):
            logger.error(
                func.__name__ + " failed,  " + hide_security(err),
                simple_log=f"{func.__name__} failed.",
            )
            return JSONResponse(
                content={CODE: err.error_code, MESSAGE: err.message}, status_code=500
            )
        msg = hide_security(err)
        traceback_error_msg = traceback.format_exc()
        logger.error(func.__name__ + str(traceback_error_msg), simple_log="")
        logger.error(
            func.__name__ + " failed,  " + msg, simple_log=f"{func.__name__} failed."
        )
        logger.debug(f"msg to client: {msg}", exc_info=True, simple_log="msg to client")
        return JSONResponse(
            content={CODE: StatusCode.ERROR.code, MESSAGE: msg}, status_code=500
        )

    @staticmethod
    def catch_exception(func=None, *, pass_message: "tuple[Type[Exception]]" = None):
        """
        捕获异常的装饰器

        用于在函数执行时捕获异常，并调用process_exception进行处理。

        Args:
            func (function, optional): 要装饰的函数。如果为None，则返回一个偏函数。
            pass_message (tuple[Type[Exception]], optional): 传递的消息类型。

        Returns:
            function: 装饰后的函数。

        """
        if func is None:
            return partial(ExceptionHandler.catch_exception, pass_message=pass_message)

        @wraps(func)
        def wrapper(*args, **kwargs):
            try:
                return func(*args, **kwargs)
            except Exception as err:
                return ExceptionHandler.process_exception(func, err)

        return wrapper

    @staticmethod
    def async_catch_exception(
        func=None, *, pass_message: "tuple[Type[Exception]]" = None
    ):
        """
        捕获异步异常的装饰器

        用于在异步函数执行时捕获异常，并调用process_exception进行处理。

        Args:
            func (function, optional): 要装饰的异步函数。如果为None，则返回一个偏函数。
            pass_message (tuple[Type[Exception]], optional): 传递的消息类型。

        Returns:
            function: 装饰后的异步函数。

        """
        if func is None:
            return partial(ExceptionHandler.catch_exception, pass_message=pass_message)

        @wraps(func)
        async def wrapper(*args, **kwargs):
            try:
                return await func(*args, **kwargs)
            except Exception as err:
                return ExceptionHandler.process_exception(func, err)

        return wrapper


def hide_security(e: Exception) -> str:
    """Hide all address info"""
    msg: str = str(e)
    return HIDE_PATTERN.sub("", msg)
