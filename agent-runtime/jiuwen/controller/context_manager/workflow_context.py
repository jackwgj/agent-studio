#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from dataclasses import dataclass
from enum import Enum
from typing import Optional

from jiuwen.controller.common.enum import WorkflowType, ActionAfterCompletionType
from jiuwen.orchestration.flow.state import WorkflowState


class InterruptedReason(Enum):
    """工作流中断类型"""

    TOOL_PARAMETER_MISSING = "tool_parameter_missing"
    TOOL_PARAMETER_PARSE_ERROR = "tool_parameter_parse_error"
    WAIT_FOR_USER_INPUT = "wait_for_user_input"
    ASK_FOR_USER_CONFIRMATION = "ask_for_confirmation"
    LLM_INPUT_TOO_LONG = "llm_input_too_long"


class WorkflowStatus(Enum):
    """工作流状态"""

    PENDING = "pending"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"
    INTERRUPTED = InterruptedReason

    @classmethod
    def is_interrupted(cls, status):
        """判断是否为中断状态"""
        return isinstance(status, InterruptedReason)


@dataclass
class NodeExecutionInfo:
    """工作流节点运行信息"""

    node_id: str
    node_name: str
    node_type: str
    execution_id: str
    workflow_id: str
    workflow_name: str


@dataclass
class WorkflowContext:
    """工作流上下文"""

    workflow_id: str
    workflow_name: str
    description: str
    status: WorkflowStatus = WorkflowStatus.PENDING
    current_node_info: Optional[NodeExecutionInfo] = None
    workflow_ir: Optional[dict] = None
    workflow_parameters: Optional[dict] = None
    action_after_completion: ActionAfterCompletionType = None
    state: Optional[WorkflowState] = None
    type: Optional[WorkflowType] = None
    config_name: str = None
    intent_name: str = None
    intent_description: str = None
