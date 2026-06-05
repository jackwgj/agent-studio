#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
from typing import Any

from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
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
from jiuwen.context.memory_engine.engine.memory_engine import MemoryEngine
from jiuwen.context.memory_engine.store import BaseKVStore, BaseDbStore


class LocalMemoryClient(MemoryClient):
    def __init__(self):
        pass

    async def list_user_variables(self, user_id: str, scope_id: str) -> dict[str, str]:
        return await MemoryEngine().get_user_variables(
            user_id=user_id, scope_id=scope_id
        )

    async def get_topic_profile_by_topics(
        self, user_id: str, scope_id: str, topics: list[str]
    ) -> list[ProfileTopicResult]:
        return await MemoryEngine().get_topic_profile_by_topics(
            user_id, scope_id, topics
        )

    async def set_scope_config(self, scope_id: str, config: MemoryScopeConfig):
        return await MemoryEngine().set_scope_config(scope_id, config)

    async def process_messages(
        self,
        messages: list[BaseMessage],
        user_id: str,
        scope_id: str,
        session_id: str,
        topics: list[ProfileTopicConfig] | None = None,
        mode: ProcessMemoryMode = ProcessMemoryMode.BATCH_MODE,
    ) -> ProcessedMemResult:
        return await MemoryEngine().process_messages(
            messages=messages,
            user_id=user_id,
            scope_id=scope_id,
            session_id=session_id,
            topics=topics,
            mode=mode,
        )

    def init_base_llm(self, llm: BaseChatModel) -> bool:
        return MemoryEngine().init_base_llm(llm=llm)

    def set_scope_llm(self, scope_id: str, llm: BaseChatModel) -> bool:
        return MemoryEngine().set_scope_llm(scope_id=scope_id, llm=llm)

    def register_store(
        self, kv_store: BaseKVStore, db_store: BaseDbStore | None = None
    ):
        MemoryEngine().register_store(kv_store=kv_store, db_store=db_store)

    def register_plugin(self, name: str, cls: type, params: dict[str, Any]):
        """
        Register plugin.

        Args:
            name: plugin name
            cls: class name of plugin
            params: plugin class parameters
        """
        MemoryEngine().register_plugin(name, cls, params)

    async def search_user_mem(
        self, user_id: str, scope_id: str, query: str, num: int
    ) -> list[dict]:
        return await MemoryEngine().search_user_mem(user_id, scope_id, query, num)

    async def list_user_mem(
        self, user_id: str, scope_id: str, offset: int = 0, size: int = 10
    ) -> list[dict[str, Any]]:
        return await MemoryEngine().list_user_mem(user_id, scope_id, offset, size)

    async def delete_mem_by_id(self, mem_id: str, user_id: str, scope_id: str):
        return await MemoryEngine().delete_mem_by_id(mem_id, user_id, scope_id)

    async def delete_mem_by_user_id(self, user_id: str, scope_id: str | None = None):
        """
        Delete all type memories for a user with scope id.

        Useful for implementing "forget me" functionality or cleaning up user data.

        Args:
            user_id: User identifier whose memories should be deleted
            scope_id: Unique identifier for the scope
        """
        return await MemoryEngine().delete_mem_by_user_id(user_id, scope_id)

    async def delete_memory_by_scope_id(self, scope_id: str):
        """
        Delete all type memories for a scope id.

        Args:
            scope_id: Unique identifier for the scope
        """
        return await MemoryEngine().delete_memory_by_scope_id(scope_id)

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
        return await MemoryEngine().update_mem_by_id(mem_id, memory, user_id, scope_id)
