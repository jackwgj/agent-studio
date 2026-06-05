#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Init log config for JiuWen Restful server."""

from logging import getLogger
from os import environ
from threading import Lock

# Prevent TGF LOGGER from creating files
environ["TGF_LOG_TO_FILE"] = "false"
# Will only be locked once and never be released, to implement single invocation only.
# Always use `blocking=False` to acquire it.
once = Lock()


def init_logger():
    """
    初始化日志记录器，重置JiuWen/TGF/root的日志处理器。此函数应仅被调用一次。

    功能描述：
    1. 从TGF日志记录器和根日志记录器中移除日志处理器（如果存在）。
    2. 将JiuWen的日志处理器移动到根日志记录器，并设置日志级别为info。
    3. 将JiuWen接口的日志记录器的传播属性设置为False。

    Args:
        无参数。

    Returns:
        无返回值。

    Raises:
        无特定异常抛出。

    Notes:
        - 此函数通过`once.acquire(blocking=False)`确保仅被调用一次。
        - 从`jiuwen.common.log.base`和`jiuwen.executor_sdk.operator.impl.common.utils`导入的日志记录器用于处理日志处理器。
        - `interface_logger`、`prompt_builder_interface_logger`和`performance_logger`的传播属性被设置为False，以防止日志消息传播到父记录器。
    """
    if not once.acquire(blocking=False):
        return

    from jiuwen.common.log.base import (
        logger as jiuwen_logger,
        interface_logger,
        performance_logger,
        prompt_builder_interface_logger,
    )
    from jiuwen.executor_sdk.operator.impl.common.utils import logger as tgf_logger

    root = getLogger()
    for log in [tgf_logger, root]:
        for h in log.handlers[:]:
            log.removeHandler(h)  # handlers are closed by its destructor

    for h in jiuwen_logger.handlers[:]:
        root.addHandler(h)
        tgf_logger.addHandler(h)
        jiuwen_logger.removeHandler(h)

    interface_logger.propagate = False
    prompt_builder_interface_logger.propagate = False
    performance_logger.propagate = False
