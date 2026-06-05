# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2023-10-18
@LastEditTime: 2023-10-18 17:01:52
@Description: 基于路径搜索策略组装规划引擎算子
"""

from jiuwen.planner.common import PlanStrategyType
from rag.plan_modes.react import ReAct

ALL_PLAN_STRATEGIES = {
    PlanStrategyType.ReAct.value: ReAct,
}
