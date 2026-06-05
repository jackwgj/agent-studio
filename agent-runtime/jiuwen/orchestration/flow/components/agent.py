#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from typing import Any, Dict

from jiuwen.orchestration import Invokable


class Agent(Invokable):
    """Start node."""

    def __init__(self, conf: dict):
        self.conf = conf

    def invoke(self, inputs: Dict[str, Any], **kwargs):
        """Invoke to assemble final answer."""
        return dict()
