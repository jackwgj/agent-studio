#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""tools for RESTFUL service."""

from functools import wraps

import yaml
from jiuwen.common.log.base import set_thread_session, logger
from jiuwen.serve.common.context import request


def convert_upload_contents_into_list(uploaded_files):
    """
    convert upload file into a list of tuple, each tuple include (filename, file_content), where the file content
    will be str if the uploaded file is text file, be dict if the uploaded file is yaml file.

    Args:
        uploaded_files(List): http request uploaded file list

    Returns: List[Tuple], content after reading.

    """
    file_contents = []
    for file in uploaded_files:
        filename, suffix = file.filename.split(".")
        if suffix in ["yaml", "yml"]:
            file_tuple = (filename, yaml.safe_load(file))
        else:
            file_tuple = (filename, file.read().decode("utf-8"))
        file_contents.append(file_tuple)
    return file_contents


def with_trace(f):
    """Set trace id."""

    @wraps(f)
    def wrap(*args, **kwargs):
        trace_id = request.get().headers.get("TraceId", "")
        set_thread_session(trace_id)
        ret = f(*args, **kwargs)
        set_thread_session("")
        return ret

    return wrap


class InterfaceLogHelper:
    """Records the start, end, and raise of the RESTFUL interface."""

    def __init__(self, function_name: str):
        self.msg = function_name
        self.args = ()
        self.return_value = None

    @wraps(type(logger).info)
    def start(self, msg: str, *args):
        """Set log prefix, and then log it as the function starts."""
        self.msg, self.args = msg, args
        logger.info("Start to call " + msg, *args, stacklevel=2)


def function_trace(f):
    """
    为函数`f`添加函数追踪样式的日志记录。

    这个装饰器会为被装饰的函数添加日志记录功能，包括记录函数调用的成功或失败信息。

    Args:
        f (function): 需要添加日志记录的函数。

    Returns:
        function: 返回一个被装饰的函数，该函数会在调用时自动记录日志。

    Raises:
        Exception: 如果函数调用过程中抛出异常，会记录错误日志并重新抛出异常。

    Notes:
        - 使用`InterfaceLogHelper`来帮助生成日志信息。
        - 日志记录包括函数调用失败时的错误信息和成功时的返回值。
    """

    @wraps(f)
    def _traced():
        helper = InterfaceLogHelper(getattr(f, "__name__", type(f).__name__))
        try:
            ret = f(helper)
        except Exception as e:
            logger.error(
                "Failed to call " + helper.msg + " raise %s: %s",
                *helper.args,
                type(e).__name__,
                e,
            )
            raise
        logger.info(
            "Succeed to call " + helper.msg + " = %s",
            *helper.args,
            helper.return_value or ret,
        )
        return ret

    return _traced
