# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""Plan Module Runtime Context."""

from abc import ABC
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class PlanRuntimeContext(ABC):
    """
    Plan Module Runtime Context
    """

    api_config: dict = field(default_factory=dict)
    agent_workflow_context: dict = field(default_factory=dict)
    # 新增：sys_operation 配置卡片，用于动态创建 SysOperation；为 None 时不注入技能工具
    sys_operation_card: Optional["SysOperationCard"] = None
