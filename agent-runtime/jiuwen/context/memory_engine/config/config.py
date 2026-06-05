#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
from enum import Enum

from pydantic import BaseModel, Field


class ProfileTopicTag(BaseModel):
    name: str = Field(default="")
    description: str = Field(default="")
    update_policy: str = Field(default="OVERWRITE")


class ProfileTopicConfig(BaseModel):
    topic: str = Field(default="")
    tags: list[ProfileTopicTag] = Field(default_factory=list)


class Variable(BaseModel):
    name: str = Field(default="")
    description: str = Field(default="")


class MemoryEngineConfig(BaseModel):
    user_profile_forbidden_topic: list[ProfileTopicTag] = Field(default_factory=list)
    default_profile_topics: list[ProfileTopicConfig] = Field(
        default_factory=list
    )  # profile topic config
    max_profile_tag_number: int = Field(default=256)  # max profile tag numbers


class MemoryScopeConfig(BaseModel):
    mem_variables: list[Variable] = Field(
        default_factory=list
    )  # memory variables config
    enable_long_term_mem: bool = Field(default=True)  # enable long term memory or not
    enable_user_profile: bool = Field(
        default=True
    )  # enable topic profile & user profile


class ProcessMemoryMode(Enum):
    REALTIME_MODE: str = "realtime"
    BATCH_MODE: str = "batch"
