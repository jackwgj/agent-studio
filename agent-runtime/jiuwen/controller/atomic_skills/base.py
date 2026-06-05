#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import abc
from typing import Dict, Any, Optional


class Skill:
    """控制器原子操作基类，提供LLM和意图理解的原子操作"""

    def __init__(self, tools, llm, context_manager):
        self.llm = llm
        self.context_manager = context_manager
        self.tools = tools

    @abc.abstractmethod
    def invoke(self, context: Optional[Dict[str, Any]] = None):
        """skill基类的invoke方法"""
        pass
