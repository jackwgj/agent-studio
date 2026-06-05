#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from enum import Enum


class MemoryType(Enum):
    USER_MEMORY = "user_memory"
    USER_PROFILE = "user_profile"
    TOPIC_PROFILE = "topic_profile"
    VARIABLE = "variable"
    OTHERS = "others"
    UNKNOWN = "unknown"
