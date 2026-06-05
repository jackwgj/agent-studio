#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from dataclasses import dataclass
from typing import List

from jiuwen.controller.common.config import AgentConfig


@dataclass
class GroupSettings:
    """组设置"""

    max_concurrent_agents: int = 5
    timeout: int = 300  # 秒


@dataclass
class AgentGroupConfig:
    """智能体组配置"""

    group_id: str
    group_name: str
    description: str
    main_agent: AgentConfig
    agents: List[AgentConfig]
    group_settings: GroupSettings
    max_agent_calls: int = 10
