# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
from dataclasses import dataclass


@dataclass
class AgentInfo:
    """
    Agent info class.
    """

    id: str
    version: str


@dataclass
class WorkflowInfo:
    """
    Workflow info class.
    """

    id: str
    version: str
    name: str
