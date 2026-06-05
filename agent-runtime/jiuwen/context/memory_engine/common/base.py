#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Optional, Tuple

from pydantic import BaseModel, Field


class VariableResult(BaseModel):
    variable_key: str = Field(default="", description="variable key")
    variable_value: str = Field(default="", description="variable value")


class ProfileTopicTagResult(BaseModel):
    name: str = Field(default="", description="tag name")
    value: str = Field(default="", description="tag value")


class ProfileTopicResult(BaseModel):
    topic: str = Field(default="", description="topic name")
    tags: list[ProfileTopicTagResult] = Field(
        default_factory=list, description="tag list"
    )


class UserMemoryResult(BaseModel):
    memory_type: str = Field(default="", description="memory type")
    memory_content: str = Field(default="", description="memory content")
    operation_type: str = Field(default="", description="operation type")


class ProcessedMemResult(BaseModel):
    variables: list[VariableResult] = Field(
        default_factory=list, description="variable results"
    )
    profile_topics: list[ProfileTopicResult] = Field(
        default_factory=list, description="profile topic results"
    )
    user_memory: list[UserMemoryResult] = Field(
        default_factory=list, description="user memory results"
    )


def generate_idx_name(
    usr_id: str,
    scope_id: str,
    agent_id: Optional[str] = None,
    mem_type: Optional[str] = None,
):
    """generate vector idx name"""
    if agent_id:
        if mem_type:
            return "agent^{}^{}^{}^{}".format(usr_id, scope_id, agent_id, mem_type)
        return "agent^{}^{}^{}^null".format(usr_id, scope_id, agent_id)
    if mem_type:
        return "agent^{}^{}^null^{}".format(usr_id, scope_id, mem_type)
    return "agent^{}^{}^null^null".format(usr_id, scope_id)


def parse_memory_hit_infos(
    hits: list[Tuple[str, float]],
) -> tuple[list[str], dict[str, float]]:
    """parse memory hit information"""
    try:
        ids = [hit[0] for hit in hits] if hits else []
        scores = {hit[0]: hit[1] for hit in hits} if hits else {}
        return ids, scores
    except Exception as e:
        raise ValueError("Failed to parse memory hit infos") from e
