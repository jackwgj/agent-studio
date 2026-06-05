#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from abc import ABC, abstractmethod
from typing import Any

from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.context.memory_engine.common.base import (
    ProfileTopicResult,
    ProcessedMemResult,
)
from jiuwen.context.memory_engine.config.config import (
    MemoryScopeConfig,
    ProfileTopicConfig,
    ProcessMemoryMode,
)
from jiuwen.context.memory_engine.store import BaseKVStore, BaseDbStore


class MemoryClient(ABC):
    """
    Abstract base class defining a unified interface for memory clients.
    """

    @abstractmethod
    async def list_user_variables(self, user_id: str, scope_id: str) -> dict[str, str]:
        """
        List all user-defined variables for a user in a scope.
        Args:
            user_id (str): User identifier.
            scope_id (str): scope identifier.

        Returns:
            dict[str, str]: Mapping from variable names to their string values.
        """
        pass

    @abstractmethod
    async def get_topic_profile_by_topics(
        self, user_id: str, scope_id: str, topics: list[str]
    ) -> list[ProfileTopicResult]:
        """
        Get topic profile by topic list.
        Args:
            user_id: User identifier.
            scope_id: scope identifier.
            topics: user profile topic list.
        Returns:
            list[ProfileTopicResult]: list of profile topic results.
        """
        pass

    @abstractmethod
    async def set_scope_config(self, scope_id: str, config: MemoryScopeConfig):
        """
        Set configuration for a memory scope.

        Tries custom memory client first, then falls back to local memory engine.
        Logs error if no memory engine is available.

        Args:
            scope_id: The scope identifier
            config: Memory scope configuration object

        Returns:
            Result from memory engine or None
        """
        pass

    @abstractmethod
    async def process_messages(
        self,
        messages: list[BaseMessage],
        user_id: str,
        scope_id: str,
        session_id: str,
        topics: list[ProfileTopicConfig] | None = None,
        mode: ProcessMemoryMode = ProcessMemoryMode.BATCH_MODE,
    ) -> ProcessedMemResult:
        """
        Process a list of messages for memory management.

        Attempts to process messages using the custom memory client first, then falls back
        to the local memory engine. Returns default result if no memory engine is available.

        Args:
            messages: List of BaseMessage objects to process
            user_id: Identifier for the user
            scope_id: Memory scope identifier
            session_id: Session identifier
            topics: Optional list of topics to process (default: None)
            mode: Mode of processing
                REALTIME_MODE: Only extract memory instruct
                BATCH_MODE: Extract user memory without memory instruct

        Returns:
            ProcessedMemResult containing processed variables and profile topics

        Note:
            - Uses custom memory client if available, otherwise falls back to local engine
            - Returns empty result with error logging if no memory engine is available
        """
        pass

    @abstractmethod
    def init_base_llm(self, llm: BaseChatModel) -> bool:
        """
        Initialize the default LLM for memory processing tasks.

        Args:
            llm: Language model client
        Returns:
            True if initialization succeeded
        """
        pass

    def set_scope_llm(self, scope_id: str, llm: BaseChatModel) -> bool:
        """
        Set a dedicated LLM for a specific scope.

        Enables scopes to use different language models tailored to their
        specific needs or domains.

        Args:
            scope_id: Unique identifier for the scope
            llm: Language model client instance
        Returns:
            True if setting succeeded
        """
        pass

    def register_store(
        self, kv_store: BaseKVStore, db_store: BaseDbStore | None = None
    ):
        """
        Register store instance.

        Args:
            kv_store: Key-value store for fast structured data access
            db_store: Database store for persistent data storage
        """
        pass

    def register_plugin(self, name: str, cls: type, params: dict[str, Any]):
        """
        Register plugin.

        Args:
            name: plugin name
            cls: class name of plugin
            params: plugin class parameters
        """
        pass

    async def search_user_mem(
        self, user_id: str, scope_id: str, query: str, num: int
    ) -> list[dict]:
        """
        Search user memory by query.
        Args:
            user_id: User identifier.
            scope_id: Scope identifier.
            query: Query statement.
            num: Retrieve num.
        Returns:
            list[dict]: top-num results, including id, mem, mem_type and others.
        """
        pass

    async def list_user_mem(
        self, user_id: str, scope_id: str, offset: int = 0, size: int = 10
    ) -> list[dict[str, Any]]:
        """
        List user memories with pagination support.

        Retrieves memories in chronological order, suitable for displaying
        conversation history or memory browsing interfaces.

        Args:
            user_id: User identifier to search within
            scope_id: Unique identifier for the scope
            offset: Start index for listing memories
            size: Number of memories to list

        Returns:
            List of memory information
        """
        pass

    async def delete_mem_by_id(self, mem_id: str, user_id: str, scope_id: str):
        """
        Delete a specific memory by ID.

        Args:
            user_id: Unique identifier for the user
            scope_id: Unique identifier for the scope
            mem_id: Unique identifier of the memory to delete
        """
        pass

    async def delete_mem_by_user_id(self, user_id: str, scope_id: str | None = None):
        """
        Delete all type memories for a user with scope id.

        Useful for implementing "forget me" functionality or cleaning up user data.

        Args:
            user_id: User identifier whose memories should be deleted
            scope_id: Unique identifier for the scope
        """
        pass

    async def delete_memory_by_scope_id(self, scope_id: str):
        """
        Delete all type memories for a scope id.

        Args:
            scope_id: Unique identifier for the scope
        """
        pass

    async def update_mem_by_id(
        self, mem_id: str, memory: str, user_id: str, scope_id: str
    ):
        """
        Update the content of an existing memory entry.

        Args:
            mem_id: Unique identifier of the memory to update
            memory: New content for the memory
            user_id: Unique identifier for the user
            scope_id: Unique identifier for the scope
        """
        pass
