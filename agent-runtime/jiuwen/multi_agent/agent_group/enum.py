#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from enum import Enum


class ExecutionAction(Enum):
    """执行动作枚举"""

    CONTINUE = "continue"
    HANDOFF = "handoff"
    INTERRUPT = "interrupt"
    FINISH = "finish"
    ERROR = "error"
