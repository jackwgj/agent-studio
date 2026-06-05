#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
节点处理器注册表

新增节点类型：
1. 在 components/ 目录下创建新的 handler 文件
2. 在本文件中 import 并注册
"""

from .base import BaseComponentHandler, BuildContext
from .branch import BranchHandler
from .code import CodeHandler
from .end import EndHandler
from .llm import LLMHandler
from .message import MessageHandler
from .qa_comp import QAHandler
from .start import StartHandler

# 节点类型注册表
# key: IR 节点类型（jiuwen.xxx）
# value: Handler 实例
NODE_HANDLERS = {
    "jiuwen.start": StartHandler(),
    "jiuwen.end": EndHandler(),
    "jiuwen.LLMComponent": LLMHandler(),
    "jiuwen.branch": BranchHandler(),
    "jiuwen.code": CodeHandler(),
    # "jiuwen.message": MessageHandler(),  # 待实现后启用
    "EI.qa": QAHandler(),
}


__all__ = [
    "BaseComponentHandler",
    "BuildContext",
    "StartHandler",
    "EndHandler",
    "LLMHandler",
    "BranchHandler",
    "CodeHandler",
    "MessageHandler",
    "NODE_HANDLERS",
]
