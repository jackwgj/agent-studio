#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from dataclasses import dataclass
from enum import Enum
from typing import Optional

from jiuwen.context.memory_engine.mem_unit.memory_type import MemoryType


class ConflictType(Enum):
    ADD = "ADD"
    DELETE = "DELETE"
    UPDATE = "UPDATE"
    NONE = "NONE"


@dataclass
class BaseMemoryUnit:
    """a single memory data item"""

    mem_type: MemoryType
    user_id: str
    scope_id: str


@dataclass
class UserMemoryUnit(BaseMemoryUnit):
    mem: str
    conflict_info: Optional[list[dict]] = None
    message_mem_id: Optional[str] = None
    operation_type: Optional[str] = None


@dataclass
class UserProfileUnit(BaseMemoryUnit):
    profile_type: str
    profile_mem: str
    score: Optional[float] = None  # Relevance Scoring
    conflict_info: Optional[list[dict]] = None  # Including new user profile mem
    message_mem_id: Optional[str] = None  # Corresponding Message ID
    mem_id: str = ""


@dataclass
class TopicProfileUnit(BaseMemoryUnit):
    topic_name: str
    tag_name: str
    tag_mem: str


@dataclass
class VariableUnit(BaseMemoryUnit):
    variable_name: str
    variable_mem: str
    mem_id: str = ""
