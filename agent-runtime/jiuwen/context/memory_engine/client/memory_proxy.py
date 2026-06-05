#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
import asyncio
from typing import Any

from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.llm_service.messages import HumanMessage, BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.client.local_memory_client import LocalMemoryClient
from jiuwen.context.memory_engine.client.memory_client import MemoryClient
from jiuwen.context.memory_engine.common.base import (
    ProfileTopicResult,
    ProcessedMemResult,
)
from jiuwen.context.memory_engine.config.config import (
    MemoryScopeConfig,
    ProfileTopicConfig,
    ProcessMemoryMode,
)
from jiuwen.context.memory_engine.config.memory_ir_config import MemoryIrConfig
from jiuwen.context.memory_engine.prompt.memory_usage import MEMORY_USAGE_PROMPT
from jiuwen.context.memory_engine.store import BaseKVStore, BaseDbStore


class MemoryProxy:
    _custom_mem_client: MemoryClient | None = None
    DEFAULT_VALUE: str = "__default__"

    def __init__(self, memory_config: MemoryIrConfig = None):
        self.local_mem_engine = LocalMemoryClient()
        if memory_config:
            self.enable_memory = memory_config.enable_memory
            self.enable_user_profile = memory_config.enable_user_profile
            self.topics = list(
                set(
                    [
                        topic.get("name", "")
                        for topic in memory_config.user_profile_topics
                        if "name" in topic
                    ]
                )
            )
        else:
            self.enable_memory: bool = False
            self.enable_user_profile: bool = False
            self.topics: list[str] = []

    @classmethod
    def register_client(cls, custom_mem_client: MemoryClient | None = None):
        """
        register custom memory client
        """
        if custom_mem_client is not None:
            if issubclass(custom_mem_client.__class__, MemoryClient):
                cls._custom_mem_client = custom_mem_client
            else:
                logger.error("custom_mem_client must be subclass of MemoryClient")

    async def list_user_variables(self, user_id: str, scope_id: str) -> dict[str, str]:
        """
        List all user-defined variables for a user in a scope.
        Args:
            user_id (str): User identifier.
            scope_id (str): scope identifier.

        Returns:
            dict[str, str]: Mapping from variable names to their string values.
        """
        if self._custom_mem_client is not None:
            return await self._custom_mem_client.list_user_variables(user_id, scope_id)

        if self.local_mem_engine is not None:
            return await self.local_mem_engine.list_user_variables(user_id, scope_id)

        logger.error("no memory engine available")
        return {}

    async def get_related_memory_msg(
        self, user_id: str, scope_id: str, query: str
    ) -> HumanMessage | None:
        """
        Get related memory message.
        Args:
            user_id: User identifier.
            scope_id: scope identifier.
            query: query
        Returns:
            HumanMessage: related memory message
            or None if not found or not enable memory or not enable user profile.
        """
        if not self.enable_memory or not self.enable_user_profile:
            return None
        topic_profiles = await self._get_topic_profile_by_topics(user_id, scope_id)
        msg = MEMORY_USAGE_PROMPT
        has_mem = False
        memory_content = ""
        if topic_profiles:
            for topic_profile in topic_profiles:
                if not topic_profile.tags:
                    continue
                has_mem = True
                memory_content += f"<topic>{topic_profile.topic}"
                for tag_result in topic_profile.tags:
                    memory_content += (
                        f"<tag>{tag_result.name}: {tag_result.value}</tag>\n"
                    )
                memory_content += "</topic>\n"
        search_mems = await self._search_user_mem(user_id, scope_id, query, num=20)
        for mem in search_mems:
            mem_content = mem.get("mem", "")
            if mem_content:
                memory_content += f"<mem>{mem_content}</mem>\n"
                has_mem = True
        if not has_mem:
            logger.debug("related memory is empty")
            return None
        msg = msg.replace("MEMORY_CONTENT", memory_content)
        return HumanMessage(content=msg)

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
        if self._custom_mem_client is not None:
            await self._custom_mem_client.set_scope_config(
                scope_id=scope_id, config=config
            )
            return

        if self.local_mem_engine is not None:
            await self.local_mem_engine.set_scope_config(
                scope_id=scope_id, config=config
            )
            return

        logger.error("no memory engine available")

    def set_scope_config_sync(self, scope_id: str, config: MemoryScopeConfig):
        """
        Synchronous wrapper for async set_scope_config method.

        Creates a new event loop to run the asynchronous set_scope_config
        method and closes the loop after completion.
        """
        try:
            loop = asyncio.get_running_loop()
        except RuntimeError:
            logger.debug("no running loop, create a new one")
            asyncio.run(self.set_scope_config(scope_id=scope_id, config=config))
            return
        asyncio.run_coroutine_threadsafe(
            self.set_scope_config(scope_id=scope_id, config=config), loop
        )

    async def process_messages(
        self,
        messages: list[BaseMessage],
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
        session_id: str = DEFAULT_VALUE,
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
        if self._custom_mem_client is not None:
            return await self._custom_mem_client.process_messages(
                messages=messages,
                user_id=user_id,
                scope_id=scope_id,
                session_id=session_id,
                topics=topics,
                mode=mode,
            )

        if self.local_mem_engine is not None:
            return await self.local_mem_engine.process_messages(
                messages=messages,
                user_id=user_id,
                scope_id=scope_id,
                session_id=session_id,
                topics=topics,
                mode=mode,
            )

        logger.error("no memory engine available")
        return ProcessedMemResult(variables=[], profile_topics=[], user_memory=[])

    def process_messages_sync(
        self,
        messages: list[BaseMessage],
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
        session_id: str = DEFAULT_VALUE,
        topics: list[ProfileTopicConfig] | None = None,
        mode: ProcessMemoryMode = ProcessMemoryMode.BATCH_MODE,
    ):
        """
        Synchronous wrapper for process_messages that runs in a new event loop.

        Args:
            messages: List of BaseMessage objects to process
            user_id: User identifier (default: DEFAULT_VALUE)
            scope_id: Memory scope identifier (default: DEFAULT_VALUE)
            session_id: Session identifier (default: DEFAULT_VALUE)
            topics: Optional list of topics to process
            mode: Mode of processing
                REALTIME_MODE: Only extract memory instruct
                BATCH_MODE: Extract user memory without memory instruct
        """
        try:
            loop = asyncio.get_running_loop()
        except RuntimeError:
            logger.debug("no running loop, create a new one")
            asyncio.run(
                self.process_messages(
                    messages=messages,
                    user_id=user_id,
                    scope_id=scope_id,
                    session_id=session_id,
                    topics=topics,
                    mode=mode,
                )
            )
            return
        asyncio.run_coroutine_threadsafe(
            self.process_messages(
                messages=messages,
                user_id=user_id,
                scope_id=scope_id,
                session_id=session_id,
                topics=topics,
                mode=mode,
            ),
            loop,
        )

    def init_base_llm(self, llm: BaseChatModel) -> bool:
        """
        Initialize the default LLM for memory processing tasks.

        Args:
            llm: Language model client
        Returns:
            True if initialization succeeded
        """
        if self._custom_mem_client is not None:
            return self._custom_mem_client.init_base_llm(llm=llm)

        if self.local_mem_engine is not None:
            return self.local_mem_engine.init_base_llm(llm=llm)

        logger.error("no memory engine available")
        return False

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
        if self._custom_mem_client is not None:
            return self._custom_mem_client.set_scope_llm(scope_id=scope_id, llm=llm)

        if self.local_mem_engine is not None:
            return self.local_mem_engine.set_scope_llm(scope_id=scope_id, llm=llm)

        logger.error("no memory engine available")
        return False

    def register_store(
        self, kv_store: BaseKVStore, db_store: BaseDbStore | None = None
    ):
        """
        Register store instance.

        Args:
            kv_store: Key-value store for fast structured data access
            db_store: Database store for persistent data storage
        """
        if self._custom_mem_client is not None:
            self._custom_mem_client.register_store(kv_store=kv_store, db_store=db_store)
            return

        if self.local_mem_engine is not None:
            self.local_mem_engine.register_store(kv_store=kv_store, db_store=db_store)
            return

        logger.error("no memory engine available")

    def register_plugin(self, name: str, cls: type, params: dict[str, Any]):
        """
        Register plugin.

        Args:
            name: plugin name
            cls: class name of plugin
            params: plugin class parameters
        """
        if self._custom_mem_client is not None:
            self._custom_mem_client.register_plugin(name, cls, params)
            return

        if self.local_mem_engine is not None:
            self.local_mem_engine.register_plugin(name, cls, params)
            return

        logger.error("no memory engine available")

    async def _get_topic_profile_by_topics(
        self, user_id: str, scope_id: str
    ) -> list[ProfileTopicResult]:
        """
        Get topic profile by topic list.
        Args:
            user_id: User identifier.
            scope_id: scope identifier.
        Returns:
            list[ProfileTopicResult]: topic to profile info mapping, every info has all tag name to value mapping.
        """
        if self._custom_mem_client is not None:
            return await self._custom_mem_client.get_topic_profile_by_topics(
                user_id, scope_id, self.topics
            )

        if self.local_mem_engine is not None:
            return await self.local_mem_engine.get_topic_profile_by_topics(
                user_id, scope_id, self.topics
            )

        logger.error("no memory engine available")
        return []

    async def _search_user_mem(
        self, user_id: str, scope_id: str, query: str, num: int = 10
    ) -> list[dict]:
        """
        Search user memory by qyery.
        Args:
            user_id: User identifier.
            scope_id: Scope identifier.
            query: Query statement.
            num: Retrive num.
        Returns:
            list[dict]: top-num results, including id, mem, mem_type and others.
        """
        if self._custom_mem_client is not None:
            return await self._custom_mem_client.search_user_mem(
                user_id, scope_id, query, num
            )

        if self.local_mem_engine is not None:
            return await self.local_mem_engine.search_user_mem(
                user_id, scope_id, query, num
            )

        logger.error("no memory engine available")
        return []
