#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from enum import Enum


class RetCode(Enum):
    """Enum of return codes"""

    FAILED = -1
    SUCCESS = 0
    WAIT_USER_INPUT = 1
    FUNC_CALL_GEN = 3
    API_EXEC_RESULT = 4
    API_EXCEPTION = 5
    CONT_OPT_RESULT = 6
    UX_SIGNAL = 7
    STREAM_OUTPUT_LLM = 8
    STREAM_REFLECTION = 9
    INTERMEDIATE_MESSAGE = 10
    STREAM_THINK_LLM = 11


class OutputResultType(Enum):
    """Enum of output result types"""

    LLM_CONTENT = 1
    LLM_FUNCTION_CALL = 2


class ControlSignal(Enum):
    """Enum of control signal"""

    STREAM_START = 0
    CONTENT_GEN_END = 1
    FUNCTION_CALL_END = 2


class HandlerType(Enum):
    """Enum of handler types"""

    WORKFLOW = "Workflow"
    TEXT = "Text"


class WorkflowType(Enum):
    """Enum of handler types"""

    START = 0
    END = 1
    GLOBAL = 2
    GENERAL = 3
    DEFAULT = 4
    INTENT = 5


class ActionAfterCompletionType(Enum):
    """Enum of action after completion types"""

    WAITING_USER_INPUT = "WaitUserInput"
    CONTINUE = "Continue"
    TERMINATE = "Terminal"

    @classmethod
    def from_string(cls, value_str):
        """通过字符串获取对应的枚举实例

        Args:
            value_str (str): 枚举值的字符串表示

        Returns:
            ActionAfterCompletionType: 对应的枚举实例，如果不存在则返回None
        """
        for action_type in cls:
            if action_type.value == value_str:
                return action_type
        return None


class PlanAndExecuteCode(Enum):
    """计划执行代码枚举类

    定义了计划执行过程中的各种状态代码。
    """

    PLANNING = "PLANNING"
    SUCCESS = "SUCCESS"
    WAIT_FOR_TOOL_COMPLETION = "WAIT_FOR_TOOL_COMPLETION"


class IntentIdentificationMethod(Enum):
    """意图识别方法枚举"""

    WORKFLOW = "WORKFLOW"
    API = "API"
    DEFAULT = "DEFAULT"

    @classmethod
    def from_string(cls, value_str):
        """通过字符串获取对应的枚举实例

        Args:
            value_str (str): 枚举值的字符串表示

        Returns:
            IntentIdentificationMethod: 对应的枚举实例，如果不存在则返回None
        """
        for intent_type in cls:
            if intent_type.value == value_str:
                return intent_type
        return cls.DEFAULT


class HandoffType(Enum):
    """代理交接类型枚举类

    定义了代理之间进行交接操作的各种类型。
    """

    AGENT_HANDOFF = "agent_handoff"
    AGENT_HANDOFF_TO_CHILD = "agent_handoff_to_child"
    AGENT_HANDOFF_TO_FATHER = "agent_handoff_to_father"
