# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""Plan Module Runtime Context."""

from abc import ABC
from dataclasses import dataclass, field


@dataclass
class PlanRuntimeContext(ABC):
    """
    Plan Module Runtime Context
    """

    api_config: dict = field(default_factory=dict)
    agent_workflow_context: dict = field(default_factory=dict)
