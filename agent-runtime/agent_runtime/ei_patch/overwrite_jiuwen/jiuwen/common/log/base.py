# @override(upstream) permanent: stacklevel conflict in DS mode
# TypeError: logging.Logger.info() got multiple values for keyword argument 'stacklevel'
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""logger for common and interface."""

__all__ = (
    "logger",
    "interface_logger",
    "prompt_builder_interface_logger",
    "performance_logger",
    "get_thread_session",
    "set_thread_session",
)

import ast
import json
import logging
import os
import re
import sys
import threading
from functools import wraps
from logging.handlers import RotatingFileHandler
from typing import Callable, Any

from jiuwen.common.configs.base import config
from jiuwen.common.configs.env_constants import (
    JIUWEN_GRAY_DEBUG_KEY,
    JIUWEN_LOG_BACKUP_COUNT_KEY,
    JIUWEN_LOG_MAX_BYTES_KEY,
    JIUWEN_LOG_LEVEL_KEY,
    JIUWEN_LOG_PATH_KEY,
)
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.serve.common.context import request_json, request_ctx

CRITICAL = 50
FATAL = CRITICAL
ERROR = 40
WARNING = 30
WARN = WARNING
INFO = 20
DEBUG = 10
NOTSET = 0
DEFAULT_LOG_MAX_BYTES = 100 * 1024 * 1024  # 100MB
CONFIG_LOG_MAX_BYTES = 20 * 1024 * 1024  # 20MB
DEFAULT_BACKUP_COUNT = 20  # 20个

name_to_level = {
    "CRITICAL": CRITICAL,
    "FATAL": FATAL,
    "ERROR": ERROR,
    "WARN": WARNING,
    "WARNING": WARNING,
    "INFO": INFO,
    "DEBUG": DEBUG,
    "NOTSET": NOTSET,
}
# Log output method, output to the console
CONSOLE = "console"
# Unique identifier of the session, user tag log
TRACE_ID = "trace_id"
EXECUTION_ID = "x-execution-id"
REQUEST_ID = "x-request-id"

GRAY_DEBUG_ENABLED = "gray_debug_enabled"

ENV_BACKUP_COUNT = os.environ.get(JIUWEN_LOG_BACKUP_COUNT_KEY)
ENV_MAX_BYTES = os.environ.get(JIUWEN_LOG_MAX_BYTES_KEY)
LOG_VERBOSE_MODE = os.getenv("LOG_VERBOSE", "false").lower() == "true"

COMMON_LOG_FORMAT = (
    "%(asctime)s|%(log_type)s|%(filename)s:%(lineno)d|%(funcName)s|"
    "%(trace_id)s|%(execution_id)s|%(request_id)s|%(levelname)s|%(message)s"
)
PERFORMANCE_LOG_FORMAT = (
    "%(asctime)s|%(log_type)s|%(trace_id)s|%(execution_id)s|%(request_id)s|"
    "%(levelname)s|%(message)s"
)
DEFAULT_LOG_FORMAT = "%(asctime)s|%(log_type)s|%(trace_id)s|%(levelname)s|%(message)s"

# 使用方式，在需要注入session_id的参数地方，使用：thread_local_data.trace_id = trace_id
_thread_log_instance = threading.local()

JIUWEN_LOG_MSG_MAX_LENGTH_KEY = "JIUWEN_LOG_MSG_MAX_LENGTH"

DEFAULT_MSG_MAX_LENGTH = 1000


def get_msg_max_length() -> int:
    """获取日志消息最大长度配置"""
    try:
        max_length = int(
            os.environ.get(JIUWEN_LOG_MSG_MAX_LENGTH_KEY, DEFAULT_MSG_MAX_LENGTH)
        )
        if max_length <= 0:
            return DEFAULT_MSG_MAX_LENGTH
        return max_length
    except (ValueError, TypeError):
        return DEFAULT_MSG_MAX_LENGTH


def replace_crlf(message):
    """Replace newline, carriage return, tab, and backspace characters in a message with their escaped sequences.

    Args:
        message (str): The input message containing characters to be escaped.

    Returns:
        str: The modified message with special characters replaced by their escaped sequences.
    """
    if message:
        return (
            message.replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
        )
    return ""


def set_thread_session(trace_id):
    """
    Set the session ID for the current thread.

    Parameters:
    trace_id (int): The session ID to be set for the current thread.
    """
    request_ctx.get()[TRACE_ID] = trace_id


def set_x_request_id(request_id):
    """
    Set the request ID for the current thread.

    Parameters:
    trace_id (int): The request ID to be set for the current thread.
    """
    request_ctx.get()[REQUEST_ID] = request_id


def set_x_execution_id(execution_id):
    """
    Set the execution ID for the current thread.

    Parameters:
    trace_id (int): The execution ID to be set for the current thread.
    """
    request_ctx.get()[EXECUTION_ID] = execution_id


def set_thread_gray_debug(log_level: str):
    """
    Set the thread gray for the current thread
    """
    request_ctx.get()[GRAY_DEBUG_ENABLED] = log_level.upper() == "DEBUG"


def get_thread_gray_debug():
    """check if gray debug opened"""
    return request_ctx.get().get(GRAY_DEBUG_ENABLED, False)


def get_thread_session() -> str:
    """Returns the request traceId of current thread. Empty str if nothing found."""
    return request_ctx.get().get(TRACE_ID, "")


def get_x_request_id() -> str:
    """Returns the request executionId of current thread. Empty str if nothing found."""
    return request_ctx.get().get(REQUEST_ID, "")


def get_x_execution_id() -> str:
    """Returns the execution executionId of current thread. Empty str if nothing found."""
    return request_ctx.get().get(EXECUTION_ID, "")


def get_gray_debug_config(log_config) -> bool:
    """get gray debug switch, default false"""
    gray_debug_switch = os.environ.get(
        JIUWEN_GRAY_DEBUG_KEY, log_config.get("gray_debug", False)
    )
    if isinstance(gray_debug_switch, bool):
        return gray_debug_switch
    if isinstance(gray_debug_switch, str) and gray_debug_switch.lower() in [
        "true",
        "false",
    ]:
        return gray_debug_switch.lower() == "true"
    return False


def get_log_max_bytes(max_bytes_config):
    """Return the log max bytes."""
    try:
        max_bytes = int(max_bytes_config)
    except ValueError as e:
        raise JiuWenBaseException(
            error_code=StatusCode.LOG_MAX_BYTES_ERROR.code,
            message=StatusCode.LOG_MAX_BYTES_ERROR.errmsg,
        ) from e

    if max_bytes <= 0 or max_bytes > DEFAULT_LOG_MAX_BYTES:
        max_bytes = DEFAULT_LOG_MAX_BYTES  # 小于0或者超出默认最大限制，使用默认最大值

    return max_bytes


def get_list_from_env(env_var_name, default="[]"):
    """
    从环境变量中解析出list
    """
    env_var = os.getenv(env_var_name, default)
    try:
        return json.loads(env_var)
    except json.JSONDecodeError as e:
        raise ValueError(
            f"Invalid JSON format for environment variable {env_var_name}"
        ) from e


class SafeRotatingFileHandler(RotatingFileHandler):
    """
    for safe RotatingFileHandler.
    """

    def __init__(self, filename, *args, **kwargs):
        pid = os.getpid()
        filename = f"{filename}_{pid}"
        super().__init__(filename, *args, **kwargs)
        # 设置文件的初始权限为 640
        os.chmod(self.baseFilename, 0o640)

    def doRollover(self):
        # Rotate the file first.
        # 日志文件（正在记录）权限为640，日志文件（记录完毕或者已经归档）权限为440
        RotatingFileHandler.doRollover(self)
        for i in range(self.backupCount, 0, -1):
            sfn = f"{self.baseFilename}.{i}"
            if os.path.exists(sfn):
                os.chmod(sfn, 0o440)
        os.chmod(self.baseFilename, 0o640)


class SingletonLogger:
    """
    logger Singleton.
    """

    __instance: logging.Logger = None
    __interface_instance: logging.Logger = None
    __prompt_builder_interface_instance: logging.Logger = None
    __performance_instance: logging.Logger = None

    def __init__(self):
        singleton_flag = (
            SingletonLogger.__instance is not None
            or SingletonLogger.__interface_instance is not None
        )
        if (
            singleton_flag
            or SingletonLogger.__prompt_builder_interface_instance is not None
            or SingletonLogger.__performance_instance is not None
        ):
            raise Exception("This class is a singleton!")
        log_config = config["logging"]
        if log_config is None:
            return
        jiuwen_log_path = os.environ.get(
            JIUWEN_LOG_PATH_KEY, log_config.get("log_path")
        )
        if not os.path.exists(os.path.dirname(jiuwen_log_path)):
            # 日志文件目录权限为750
            os.makedirs(os.path.dirname(jiuwen_log_path), mode=0o750)
        log_file = os.path.join(jiuwen_log_path, log_config.get("log_file"))
        performance_log_file = os.path.join(
            jiuwen_log_path, log_config.get("performance_log_file")
        )
        output = log_config.get("output", CONSOLE)

        interface_log_file = os.path.join(
            jiuwen_log_path, log_config.get("interface_log_file")
        )
        prompt_builder_interface_log_file = os.path.join(
            jiuwen_log_path, log_config.get("prompt_builder_interface_log_file")
        )
        interface_output = log_config.get("interface_output", CONSOLE)
        performance_output = log_config.get("performance_output", CONSOLE)

        SingletonLogger.__instance = SingletonLogger._get_logger(
            "common", log_config, output, log_file
        )
        SingletonLogger.__interface_instance = SingletonLogger._get_logger(
            "interface", log_config, interface_output, interface_log_file
        )
        SingletonLogger.__prompt_builder_interface_instance = (
            SingletonLogger._get_logger(
                "interface_prompt_builder",
                log_config,
                interface_output,
                prompt_builder_interface_log_file,
            )
        )
        SingletonLogger.__performance_instance = SingletonLogger._get_logger(
            "performance", log_config, performance_output, performance_log_file
        )

    @staticmethod
    def get_logger():
        """This function returns the instance of the two SingletonLoggers.
        If the instance does not exist, it creates a new one.

        Returns:
        SingletonLogger.__instance: The instance of the SingletonLogger
        SingletonLogger.__interface_instance: The interface instance of the SingletonLogger
        SingletonLogger.__prompt_builder_interface_instance: The interface instance of the SingletonLogger
        SingletonLogger.__performance_instance: The interface instance of the SingletonLogger
        """
        singleton_flag = (
            SingletonLogger.__instance is None or SingletonLogger.__interface_instance
        )
        if (
            singleton_flag
            or SingletonLogger.__prompt_builder_interface_instance
            or SingletonLogger.__performance_instance
        ):
            SingletonLogger()
        return (
            SingletonLogger.__instance,
            SingletonLogger.__interface_instance,
            SingletonLogger.__prompt_builder_interface_instance,
            SingletonLogger.__performance_instance,
        )

    @classmethod
    def _get_logger(cls, log_type, log_config, output, log_file):
        level = os.environ.get(JIUWEN_LOG_LEVEL_KEY, log_config.get("level", "WARNING"))
        max_bytes = (
            get_log_max_bytes(ast.literal_eval(ENV_MAX_BYTES))
            if ENV_MAX_BYTES
            else get_log_max_bytes(log_config.get("max_bytes", CONFIG_LOG_MAX_BYTES))
        )
        if log_type == "common":
            log_format = log_config.get("format", COMMON_LOG_FORMAT)
        elif log_type == "performance":
            log_format = PERFORMANCE_LOG_FORMAT
        else:
            log_format = DEFAULT_LOG_FORMAT

        # 敏感日志掩码
        sensitive_words = get_list_from_env("LOG_MASKED_SENS_WORDS")
        common_logger = logging.getLogger(log_type)
        # 动态包装标准日志方法
        for method in ["debug", "info", "warning", "error", "critical"]:
            if hasattr(common_logger, method):
                setattr(
                    common_logger,
                    method,
                    LogRouter().route_log(getattr(common_logger, method)),
                )

        if get_gray_debug_config(log_config):
            common_logger.setLevel("DEBUG")
            level_no = logging.getLevelName(level)
            if not isinstance(level_no, int):
                raise Exception("level no should be integer in gray debug mode")
            common_logger.addFilter(DebugFilter(log_level_no=level_no))
        else:
            common_logger.setLevel(level)
        if CONSOLE in output:
            stream_handler = logging.StreamHandler(stream=sys.stdout)
            if TRACE_ID in log_format:
                stream_handler.addFilter(ThreadContextFilter(log_type))
            stream_handler.setFormatter(
                MaskingFormatter(
                    fmt=log_format,
                    sensitive_words=sensitive_words,
                    truncate_for_console=True,
                )
            )
            common_logger.addHandler(stream_handler)

        if "log" in output:
            if not os.path.exists(os.path.dirname(log_file)):
                # 日志文件目录权限为750
                os.makedirs(os.path.dirname(log_file), mode=0o750)
            file_handler = SafeRotatingFileHandler(
                filename=log_file,
                maxBytes=max_bytes,
                backupCount=ast.literal_eval(ENV_BACKUP_COUNT)
                if ENV_BACKUP_COUNT
                else log_config.get("backup_count", DEFAULT_BACKUP_COUNT),
                encoding="utf-8",
            )
            if TRACE_ID in log_format:
                file_handler.addFilter(ThreadContextFilter(log_type))
            file_handler.setFormatter(
                MaskingFormatter(
                    fmt=log_format,
                    sensitive_words=sensitive_words,
                    truncate_for_console=False,
                )
            )
            common_logger.addHandler(file_handler)

        return common_logger


class ThreadContextFilter(logging.Filter):
    """
    线程上下文过滤器。
    其主要用途是过滤日志记录，添加线程相关的信息，如线程ID等。
    """

    def __init__(self, log_type):
        super().__init__()
        self.log_type = log_type

    def filter(self, record):
        # 搜寻线程本地存储以获取 trace_id 如果存在的话
        trace_id = getattr(_thread_log_instance, "trace_id", "")
        record.trace_id = trace_id or request_json.get().get("conversationId", "")
        if self.log_type in ["common", "performance"]:
            record.request_id = get_x_request_id()
            record.execution_id = get_x_execution_id()
        record.log_type = "perf" if self.log_type == "performance" else self.log_type
        return True


class DebugFilter(logging.Filter):
    """Debug日志开关"""

    def __init__(self, log_level_no, *args, **kwargs):
        self.log_level_no = log_level_no
        super().__init__(*args, **kwargs)

    def filter(self, record):
        if get_thread_gray_debug():
            return record.levelno >= logging.DEBUG
        return record.levelno >= self.log_level_no


class MaskingFormatter(logging.Formatter):
    """
    日志掩码格式器。

    参数:
        fmt (str): 日志格式字符串。
        sensitive_words (list, optional): 敏感词列表，用于在日志中将该词及其后面部分掩码处理。默认为None，不做任何掩码处理。
        datefmt (str, optional): 日期格式字符串。默认为None。
        style (str, optional): 格式风格，可以是'%'、'{'或'$'。默认为'%'。

    属性:
        regex (re.Pattern): 用于匹配敏感词的正则表达式对象。如果没有敏感词，则为None。
    """

    def __init__(
        self,
        fmt: str,
        sensitive_words: list = None,
        datefmt=None,
        style="%",
        truncate_for_console=True,
    ):
        super().__init__(fmt, datefmt, style)
        sensitive_words = [word for word in sensitive_words if word]
        if sensitive_words:
            # 生成正则表达式，允许敏感词中包含空格
            sensi_str = "|".join(re.escape(word) for word in sensitive_words)
            self.regex = re.compile(rf"({sensi_str}).*", re.IGNORECASE)
        else:
            self.regex = None
        self.truncate_for_console = truncate_for_console
        if truncate_for_console:
            self.max_msg_length = get_msg_max_length()
        else:
            self.max_msg_length = None

    def format(self, record):
        original = super().format(record)
        normalized = replace_crlf(original)

        if self.regex:
            masked = self.regex.sub(lambda x: "*****", normalized)
            result = masked
        else:
            result = normalized

        # 长度检查和处理（仅对控制台日志）
        if self.truncate_for_console and self.max_msg_length:
            return self._truncate_message(result)
        return result

    def _truncate_message(self, message: str) -> str:
        """截断超过最大长度的消息"""
        if len(message) <= self.max_msg_length:
            return message

        # 截断并添加省略号
        truncate_length = self.max_msg_length - 3  # 保留3个字符给 "..."
        if truncate_length <= 0:
            return "..."

        return message[:truncate_length] + "..."


class LogRouter:
    def __init__(self):
        self._verbose_mode = os.getenv("LOG_VERBOSE", "false").lower() == "true"

    def route_log(self, original_method: Callable) -> Callable:
        """route log."""

        @wraps(original_method)
        def wrapper(msg: str, *args: Any, **kwargs: Any) -> None:
            simple_log = kwargs.pop("simple_log", msg)
            if self._verbose_mode:
                processed_msg = msg
            else:
                processed_msg = simple_log
                kwargs.pop("exc_info", None)  # 去掉异常堆栈

            # @override(jiuwen) permanent: stacklevel conflict in DS mode
            # 避免 TypeError: logging.Logger.info() got multiple values for keyword argument 'stacklevel'
            _ = kwargs.pop("stacklevel", None)
            # @override(jiuwen) permanent: stacklevel conflict in DS mode
            if LOG_VERBOSE_MODE:
                return original_method(msg, *args, stacklevel=2, **kwargs)
            return original_method(processed_msg, *args, stacklevel=1, **kwargs)

        return wrapper


# 使用示例
logger, interface_logger, prompt_builder_interface_logger, performance_logger = (
    SingletonLogger.get_logger()
)
