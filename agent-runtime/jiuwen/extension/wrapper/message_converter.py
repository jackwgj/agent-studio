#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""消息转换器：将旧框架的 ConversationMessage 转换为 agent-core 的 ModelContext 消息。"""

from typing import List, Optional

from jiuwen.extension.wrapper.conversation_message_ext import (
    ConversationUserMessage,
    ConversationAssistantMessage,
)
from openjiuwen.core.context_engine import ModelContext
from openjiuwen.core.foundation.llm import BaseMessage


class WorkflowMessageConverter:
    """将旧框架 conversation_history 中的字典消息转换为 agent-core BaseMessage 列表。"""

    @staticmethod
    def conversation_messages_to_model_context(
        conv_messages: List[dict],
        context: ModelContext,
    ) -> None:
        """将对话消息列表写入 ModelContext。

        Args:
            conv_messages: 旧框架格式的对话消息列表，每条消息为 dict
            context: agent-core 的 ModelContext 实例
        """
        base_messages = []
        for conv_msg in conv_messages:
            base_msg = WorkflowMessageConverter._to_base_message(conv_msg)
            if base_msg is not None:
                base_messages.append(base_msg)
        if base_messages:
            context.set_messages(base_messages)

    @staticmethod
    def _to_base_message(conv_msg: dict) -> Optional[BaseMessage]:
        """将单条对话消息字典转换为 BaseMessage 子类。

        Args:
            conv_msg: 对话消息字典，包含 role、content 等字段

        Returns:
            BaseMessage 子类实例，或 None（如果消息角色不需要转换）
        """
        role, content, name, enable_history = (
            WorkflowMessageConverter._extract_msg_fields(conv_msg)
        )

        if role == "user":
            return ConversationUserMessage(
                content=content,
                name=name or "",
                enable_history=enable_history,
            )
        elif role == "assistant":
            return ConversationAssistantMessage(
                content=content,
                name=name or "",
                tool_calls=None,
                enable_history=enable_history,
            )
        elif role in ("function", "tool"):
            return None
        return BaseMessage(role=role, content=content)

    @staticmethod
    def _extract_msg_fields(conv_msg: dict) -> tuple:
        """从对话消息字典中提取字段。

        Args:
            conv_msg: 对话消息字典

        Returns:
            (role, content, name, enable_history) 元组
        """
        role = conv_msg.get("role", "")
        content = conv_msg.get("content", "")
        name = conv_msg.get("name", "")
        enable_history = conv_msg.get("enable_history", True)
        return role, content, name, enable_history
