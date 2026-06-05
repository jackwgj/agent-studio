#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
# @override(jiuwen) lines 223-229: unified logging with jiuwen logger
"""logger for common and interface."""

__all__ = (
    "logger",
    "interface_logger",
    "prompt_builder_interface_logger",
    "set_thread_session",
)


# 使用示例 - 直接使用jiuwen的logger实例
from jiuwen.common.log.base import (
    logger as jiuwen_logger,
    interface_logger as jiuwen_interface_logger,
    prompt_builder_interface_logger as jiuwen_prompt_builder_interface_logger,
    set_thread_session as jiuwen_set_thread_session,
)

logger = jiuwen_logger
interface_logger = jiuwen_interface_logger
prompt_builder_interface_logger = jiuwen_prompt_builder_interface_logger
set_thread_session = jiuwen_set_thread_session
