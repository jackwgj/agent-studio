#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.


class ChatContent:
    """prompt engine chat content"""

    def __init__(self, role: str, content: str):
        self.role = role
        self.content = content
