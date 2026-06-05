#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
IR 节点类型常量定义
"""

# 流控组件
IR_TYPE_START = "jiuwen.start"
IR_TYPE_END = "jiuwen.end"
IR_TYPE_BRANCH = "jiuwen.branch"
IR_TYPE_LOOP = "jiuwen.LOOP"

# LLM 组件
IR_TYPE_LLM = "jiuwen.LLMComponent"

# 消息组件
IR_TYPE_MESSAGE = "jiuwen.message"

# 工具组件
IR_TYPE_HTTP = "jiuwen.HTTPRequest"
IR_TYPE_CODE = "jiuwen.code"
IR_TYPE_QUESTION = "jiuwen.Question"

# 意图识别
IR_TYPE_INTENT = "jiuwen.Intent"

# QA 组件
IR_TYPE_QA = "EI.qa"


# 映射：IR 类型 → handler key
IR_TYPE_TO_HANDLER = {
    IR_TYPE_START: "start",
    IR_TYPE_END: "end",
    IR_TYPE_LLM: "llm",
    IR_TYPE_BRANCH: "branch",
    IR_TYPE_LOOP: "loop",
    IR_TYPE_MESSAGE: "message",
    IR_TYPE_HTTP: "http",
    IR_TYPE_CODE: "code",
    IR_TYPE_QUESTION: "question",
    IR_TYPE_INTENT: "intent",
    IR_TYPE_QA: "qa",
}
