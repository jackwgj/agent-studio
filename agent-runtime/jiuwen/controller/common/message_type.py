#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from enum import Enum


class MessageType(Enum):
    """消息类型枚举"""

    USER_INPUT = "user_input"
    WORKFLOW_INTERRUPT = "workflow_interrupt"
    WORKFLOW_COMPLETION = "workflow_completion"
    WORKFLOW_FAILED = "workflow_failed"
    WORKFLOW_MESSAGE = "workflow_message"
    PLUGIN_COMPLETION = "plugin_completion"
    MCP_COMPLETION = "mcp_completion"

    @classmethod
    def from_string(cls, s: str) -> "MessageType":
        """从字符串转换为枚举值

        Args:
            s (str): 消息类型字符串

        Returns:
            MessageType: 对应的消息类型枚举值

        Raises:
            ValueError: 当字符串不匹配任何枚举值时抛出异常
        """
        for type_enum in cls:
            if type_enum.value == s:
                return type_enum
        raise ValueError("Invalid message type")


class MessageSubType(Enum):
    """消息子类型枚举"""

    NONE = "none"
    PLUGIN_PARAM_MISS = "plugin_param_miss"
    PLUGIN_CALL_CONFIRM = "plugin_call_confirm"
    PLUGIN_PARAM_ERROR = "plugin_param_error"
    QUESTIONER_ASK = "question_ask"
    CONTROLLER_ASK = "controller_ask"
    GLOBAL_INTENT = "global_intent_interrupt"
    TERMINATE_TASK = "terminate"

    @classmethod
    def from_string(cls, s: str) -> "MessageSubType":
        """从字符串转换为枚举值

        Args:
            s (str): 消息子类型字符串

        Returns:
            MessageSubType: 对应的消息子类型枚举值，如果未找到则返回NONE
        """
        for type_enum in cls:
            if type_enum.value == s:
                return type_enum
        return cls.NONE
