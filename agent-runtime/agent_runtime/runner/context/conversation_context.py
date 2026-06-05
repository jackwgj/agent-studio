# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

"""Conversation Context 模块 - 处理历史消息与 ModelContext 创建"""

from typing import List

from jiuwen.serve.schemas.orchestration_mgr import ConversationHistoryMessage
from openjiuwen.core.common.logging import workflow_logger as logger
from openjiuwen.core.context_engine.context.context import SessionModelContext
from openjiuwen.core.context_engine.schema.config import ContextEngineConfig
from openjiuwen.core.foundation.llm import (
    BaseMessage,
    UserMessage,
    AssistantMessage,
    SystemMessage,
)


def convert_conversation_history(
    history: list[ConversationHistoryMessage],
) -> List[BaseMessage]:
    """将请求格式的历史消息转换为 BaseMessage

    Args:
        history: 请求中的 conversationHistory 列表，格式如：
                 [{"role": "user", "content": "你好"}, {"role": "assistant", "content": "你好"}]

    Returns:
        List[BaseMessage]: 转换后的消息列表

    Note:
        - 跳过 content 为空的消息
        - 跳过未知的 role，记录 warning 日志
    """
    result = []
    for msg in history:
        msg = msg.model_dump()
        role = msg.get("role", "").lower()
        content = msg.get("content", "")
        if not content:
            logger.warning(f"跳过空消息: {msg}")
            continue
        if role == "user":
            result.append(UserMessage(role="user", content=content))
        elif role == "assistant":
            result.append(AssistantMessage(role="assistant", content=content))
        elif role == "system":
            result.append(SystemMessage(role="system", content=content))
        else:
            logger.warning(f"未知消息角色: {role}，跳过")
            continue
    return result


def create_conversation_context(
    context_id: str,
    session_id: str,
    history: list[ConversationHistoryMessage],
    config: ContextEngineConfig = None,
) -> SessionModelContext:
    """创建对话上下文 ModelContext

    Args:
        context_id: 上下文 ID（通常使用 ir_path）
        session_id: 会话 ID（使用 conversation_id）
        history: 对话历史列表
        config: ContextEngineConfig 配置（可选，默认使用空配置）

    Returns:
        SessionModelContext: 配置好的上下文实例
    """
    history_messages = convert_conversation_history(history)
    config = config or ContextEngineConfig()

    logger.info(
        f"create ConversationContext: context_id={context_id}, session_id={session_id}, "
        f"history_count={len(history_messages)}"
    )

    return SessionModelContext(
        context_id=context_id,
        session_id=session_id,
        config=config,
        history_messages=history_messages,
        processors=[],
    )
