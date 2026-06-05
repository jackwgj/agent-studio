#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from enum import Enum


class TaskType(Enum):
    """任务类型枚举"""

    WORKFLOW_START = "workflow_start"
    WORKFLOW_INTERRUPT = "workflow_interrupt"
    WORKFLOW_RESUME = "workflow_resume"
    WORKFLOW_COMPLETION = "workflow_completion"
    WORKFLOW_MESSAGE = "workflow_message"
    PLUGIN_START = "plugin_start"
    MCP_START = "mcp_start"
    PLUGIN_COMPLETION = "plugin_completion"
    TASK_COMPLETION = "task_completion"
    CHAT = "chat"
    AGENT_HANDOFF = "agent_handoff"

    @classmethod
    def from_string(cls, s: str) -> "TaskType":
        """从字符串转换为枚举值"""
        for type_enum in cls:
            if type_enum.value == s:
                return type_enum
        raise ValueError(f"Invalid task type: {s}")


class TaskSubType(Enum):
    """任务子类型枚举"""

    NONE = "none"
    ASK_USER_FOR_PARAM = "ask_user_for_param"
    ASK_USER_FOR_CONFIRM = "ask_user_for_confirm"
    PLUGIN_PARAM_REVISE = "plugin_param_revise"
    MESSAGE_RESPONSE = "message_response"
    STARTED_FROM_REACT = "started_from_react"
    STARTED_FROM_CONTROLLER = "started_from_controller"
    STARTED_FROM_PLAN_EXECUTE = "started_from_plan_execute"
    HANDOFF_TO_CHILD = "handoff_to_child"
    HANDOFF_TO_FATHER = "handoff_to_father"

    @classmethod
    def from_string(cls, s: str) -> "TaskSubType":
        """从字符串转换为枚举值"""
        for type_enum in cls:
            if type_enum.value == s:
                return type_enum
        return cls.NONE


class TaskStatus(Enum):
    """任务状态枚举"""

    PENDING = "pending"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"

    @classmethod
    def from_string(cls, s: str) -> "TaskStatus":
        """从字符串转换为枚举值"""
        for status_enum in cls:
            if status_enum.value == s:
                return status_enum
        return cls.PENDING
