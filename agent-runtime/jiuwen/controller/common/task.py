#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import time
import uuid
from dataclasses import dataclass, field
from enum import Enum
from typing import Dict, Any, Optional

from jiuwen.controller.common.message import Message
from jiuwen.controller.common.state import TaskState, WorkflowContextState
from jiuwen.controller.common.task_type import TaskType, TaskSubType
from jiuwen.controller.context_manager.workflow_context import WorkflowContext


def filter_input_data(input_data):
    """过滤input_data中不需要保存的字段"""
    if not isinstance(input_data, dict):
        return input_data

    filtered_data = {}
    for key, value in input_data.items():
        if key == "runtime_data" and isinstance(value, dict):
            # 特殊处理runtime_data，过滤掉trace_handlers
            filtered_runtime_data = {
                k: v for k, v in value.items() if k != "trace_handlers"
            }
            filtered_data[key] = filtered_runtime_data
        else:
            filtered_data[key] = value

    return filtered_data


@dataclass
class Task:
    """任务类，用于控制器分发任务"""

    task_type: TaskType
    input_data: Dict[str, Any] = field(default_factory=dict)
    workflow_context: Optional[WorkflowContext] = None
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    status: str = "pending"
    created_at: float = field(default_factory=time.time)
    result: Any = None
    error: Any = None
    sub_task_type: TaskSubType = field(default=TaskSubType.NONE)
    is_restored: bool = False

    def __post_init__(self):
        # 确保task_type是TaskType枚举类型
        if isinstance(self.task_type, str):
            self.task_type = TaskType.from_string(self.task_type)

        # 确保sub_task_type是TaskSubType枚举类型
        if isinstance(self.sub_task_type, str):
            self.sub_task_type = TaskSubType.from_string(self.sub_task_type)

        # 对于workflow相关任务，验证必要的属性
        if self.is_workflow_task() and not self.workflow_context:
            raise ValueError(
                f"Workflow related task must have workflow_context: {self.task_type}"
            )

    @property
    def workflow_id(self) -> Optional[str]:
        """获取工作流ID"""
        return self.workflow_context.workflow_id if self.workflow_context else None

    @staticmethod
    def generate_id() -> str:
        """生成唯一任务ID

        Returns:
            生成的任务ID
        """
        return str(uuid.uuid4())

    def is_workflow_task(self) -> bool:
        """判断是否为工作流相关任务"""
        return self.task_type in {
            TaskType.WORKFLOW_START,
            TaskType.WORKFLOW_INTERRUPT,
            TaskType.WORKFLOW_RESUME,
            TaskType.WORKFLOW_COMPLETION,
        }

    def get_state(self) -> TaskState:
        """获取当前任务的状态对象

        Returns:
            TaskState: 任务状态对象
        """
        # 处理workflow_context，只保留必要字段
        workflow_context_state = None
        if self.workflow_context:
            workflow_context_state = WorkflowContextState.from_workflow_context(
                self.workflow_context
            )
        # 过滤input_data中不需要保存的字段
        filtered_input_data = filter_input_data(self.input_data)
        # 创建TaskState对象
        return TaskState(
            id=self.id,
            task_type=self.task_type.value
            if isinstance(self.task_type, Enum)
            else self.task_type,
            sub_task_type=self.sub_task_type.value
            if isinstance(self.sub_task_type, Enum)
            else self.sub_task_type,
            input_data=filtered_input_data,
            status=self.status,
            created_at=self.created_at,
            result=self.result,
            error=self.error,
            is_restored=self.is_restored,
            workflow_context=workflow_context_state,
        )


@dataclass
class TaskExecution:
    """任务执行对象，包含任务类型、输入和处理函数"""

    task: Task
    handler: Any
    last_message: Optional[Message] = None

    def __init__(self, task: Task, handler: Any):
        self.task = task
        self.handler = handler
