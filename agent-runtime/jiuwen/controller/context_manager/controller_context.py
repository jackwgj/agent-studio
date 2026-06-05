#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from dataclasses import dataclass, field


@dataclass
class ControllerContext:
    """控制器上下文，存储控制器状态"""

    current_workflow_index: int = 0
    start_workflow_executed: bool = False
    start_workflow_completed: bool = False
    end_workflow_executed: bool = False
    end_workflow_completed: bool = False
    global_iteration_count: int = 0
    processed_workflow_names: set = field(default_factory=set)
    task_end: bool = False
