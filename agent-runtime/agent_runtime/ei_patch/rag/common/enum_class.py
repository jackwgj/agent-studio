#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2024-05-15
@LastEditTime: 2024-05-15 14:54:38
@Description: enum
"""

from enum import Enum


class OpType(Enum):
    """Enum of operator types"""

    ProposeNext = "propose_next"
    DetectIntention = "detect_intention"


class PlanStrategyType(Enum):
    """Enum of plan plan_modes types"""

    ReAct = "react"
    Controller = "controller"
    ToolUse = "tool_use"
    PlanExecute = "plan_execute"
    RAG = "RAG"


class PlanStatusCode(Enum):
    """Enum of plan status codes"""

    ERROR = -1
    Finish = 0
    WAIT_INPUT = 1
    TERMINATE = 2


class PlanCacheKey(Enum):
    """Enum of plan cache keys"""

    PLAN_STATE = "plan_state"


class ProviderType(Enum):
    """Enum of provider types"""

    WORKFLOW = "workflow"
    PLUGIN = "plugin"
    MCP = "mcp"


class ExecutionPlatformType(Enum):
    """Enum of execution platform type"""

    SPIFF_WORKFLOW = "spiffworkflow"
    TGF = "tgf"
