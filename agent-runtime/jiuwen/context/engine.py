#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Union, Dict, List, Optional, Any

from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.context.accessor.handler import ContextHandler
from jiuwen.context.accessor.memory_accessor import MemoryAccessor
from jiuwen.context.base import ContextConfig
from jiuwen.context.executor.executor import ContextExecutor
from jiuwen.context.history import ConversationHistory, ConversationMessage
from jiuwen.context.memory import MemoryContext
from jiuwen.serve.common.context import request_json
from jiuwen.serve.controllers.execution.enum import MessageRole


class ContextEngine:
    def __init__(
        self,
        context_config: ContextConfig = None,
        history: ConversationHistory = None,
        model: BaseChatModel = None,
    ):
        self.history = history if history else ConversationHistory()
        self.memory_context = MemoryContext()
        self.model = model
        self._executor: ContextExecutor = ContextExecutor(
            handler=ContextHandler(self.history, self.memory_context), model=model
        )
        self.conf = (
            context_config if context_config else ContextConfig.from_config_dict({})
        )
        if self.conf.enable_memory and self.conf.mem_variables:
            self.__init_memory_accessor()

    def add_messages(self, messages: Union[List[Dict], List[ConversationMessage]]):
        """add list messages to history"""
        self.history.add_messages(messages)

    def get_messages(self, filters: Optional[Dict] = None) -> List[ConversationMessage]:
        """
        get history messages, If enable_memory configuration is enabled。
        If memory search enable configured:
        queries long-term memory and memory variables,
        and appends the messages as a user message.
        If memory user profile enabled:
        query user profile from memory client
        and appends the messages as a user message.
        :param filters: 过滤历史上下文的条件，支持字段：
                {
                    "agent_id": None,               是否通过agent_id过滤筛选
                    "intent": None,                 是否通过intent过滤筛选
                    "enable_history": True/False,   是否通过enable_history过滤筛选
                    "num": -1                       是否直接截断取倒数num轮对话记录
                }
        :return:
        """
        # 短期对话历史
        return (
            self.history.get_messages(**filters)
            if filters
            else self.history.get_all_messages()
        )

    def get_latest_k_chat_history_dict(self, num: int) -> List[Dict[str, Any]]:
        """Get recent 'num' turns of conversation history, in dictionary format."""
        messages = self.get_messages(filters={"num": num})
        return self.history.convert_messages_to_chat_history_dict(messages)

    def add_message(self, message: ConversationMessage):
        """add one history message"""
        self.history.add(message)

        # 添加长期记忆
        if self.conf.enable_memory and self.conf.mem_variables:
            self.add_message_to_memory(message)

    def add_user_message(
        self,
        content: str,
        intent: Optional[str] = None,
        files: Optional[List[Dict[str, Any]]] = None,
        enable_history: Optional[bool] = True,
        agent_id: Optional[str] = None,
    ):
        """add user message"""
        self.add_message(
            ConversationMessage(
                content=content,
                files=files,
                role=MessageRole.USER.value,
                enable_history=enable_history,
                function_call=[],
                agent_id=agent_id,
            )
        )
        if intent:
            self.history.set_latest_intent(intent)

    def add_assistant_message(
        self,
        content: str,
        intent: Optional[str] = None,
        function_call: Optional[Dict[str, Any]] = None,
        agent_id: Optional[str] = None,
        enable_history: Optional[bool] = True,
    ):
        """add assistant message"""
        self.add_message(
            ConversationMessage(
                content=content,
                role=MessageRole.ASSISTANT.value,
                function_call=function_call if function_call else [],
                enable_history=enable_history,
                agent_id=agent_id,
            )
        )
        if intent:
            self.history.set_latest_intent(intent)

    def add_short_term_memory(self, key, state_key, val):
        """add short term memory"""
        self.memory_context.add_short_term_memory(key, state_key, val)

    def get_short_term_memory(self, key):
        """get short term memory"""
        return self.memory_context.memory_variable.get(key, None)

    def get_param_extraction_context(self, key):
        """get ExtractParamsState from memory variable"""
        return self.memory_context.get_param_extraction_context(key)

    def get_tool_results(self, key):
        """get tool results from memory variable"""
        return self.memory_context.get_tool_results(key)

    def add_message_to_memory(self, message: ConversationMessage):
        """add long term memory
        Args:
            message (ConversationMessage): conversation message
        """
        mem_content = message.content.strip()
        if not mem_content:
            return

        user_id = request_json.get().get("userId", "")
        agent_id = request_json.get().get("agentId", "")
        if not user_id or not agent_id:
            logger.error(
                "Failed to add memory: Missing userId or agentId in request_json."
            )
            return

        mem_message = BaseMessage(type=message.role, content=message.content)
        self.memory_accessor.add_memory(
            user_id=user_id, agent_id=agent_id, message=mem_message
        )
        return

    def __init_memory_accessor(self):
        """init memory accessor and set mem engine config"""
        self.memory_accessor = MemoryAccessor(self.model)
        self.memory_accessor.set_variable_config(self.conf.mem_variables)
