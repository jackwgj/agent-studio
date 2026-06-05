#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from dataclasses import dataclass
from enum import Enum
from typing import Optional, Dict, Any, List

from jiuwen.controller.context_manager.workflow_context import WorkflowContext


@dataclass
class WorkflowContextState:
    """WorkflowContext的简化状态类，只保留必要信息"""

    workflow_id: str
    workflow_name: str
    description: str
    workflow_parameters: Optional[dict] = None
    type: Optional[str] = None  # 存储枚举的字符串值
    status: Optional[str] = None  # 存储枚举的字符串值

    @classmethod
    def from_workflow_context(cls, context: WorkflowContext) -> "WorkflowContextState":
        """从WorkflowContext对象创建状态对象，只保留必要字段"""
        return cls(
            workflow_name=context.workflow_name,
            workflow_id=context.workflow_id,
            description=context.description,
            workflow_parameters=context.workflow_parameters,
            type=context.type.value if isinstance(context.type, Enum) else context.type,
            status=context.status.value
            if isinstance(context.status, Enum)
            else context.status,
        )


@dataclass
class TaskState:
    """任务状态类，用于Task的序列化与反序列化"""

    id: str
    task_type: str  # 使用字符串表示枚举值
    sub_task_type: str  # 使用字符串表示枚举值
    input_data: Dict[str, Any]
    status: str
    created_at: float
    result: Any
    error: Any
    is_restored: bool
    workflow_context: Optional[WorkflowContextState] = None


@dataclass
class TaskQueueState:
    """任务队列状态类，用于TaskQueue的序列化与反序列化"""

    pending_tasks: List[TaskState]
    running_tasks: List[TaskState]
    completed_tasks: List[TaskState]
    failed_tasks: List[TaskState]
    task_maps: Dict[str, TaskState]
    status_maps: Dict[str, str]  # 存储TaskStatus枚举的字符串值
