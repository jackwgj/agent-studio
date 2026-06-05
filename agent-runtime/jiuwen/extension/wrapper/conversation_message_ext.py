#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""扩展消息类型，为 BaseMessage 添加 enable_history 字段。"""

from typing import Optional

from openjiuwen.core.foundation.llm import UserMessage, AssistantMessage


class ConversationUserMessage(UserMessage):
    """带 enable_history 标记的用户消息。

    组件通过 getattr(msg, 'enable_history', True) 读取，
    子类兼容 agent-core 内部代码。
    """

    enable_history: Optional[bool] = True


class ConversationAssistantMessage(AssistantMessage):
    """带 enable_history 标记的助手消息。

    组件通过 getattr(msg, 'enable_history', True) 读取，
    子类兼容 agent-core 内部代码。
    """

    enable_history: Optional[bool] = True
