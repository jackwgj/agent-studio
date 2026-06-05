#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from typing import Optional, Any

from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.manage.base_memory_manager import BaseMemoryManager
from jiuwen.context.memory_engine.manage.topic_profile_manager import (
    TopicProfileManager,
)
from jiuwen.context.memory_engine.manage.variable_manager import VariableManager
from jiuwen.context.memory_engine.mem_unit.memory_type import MemoryType


class SearchManager:
    user_mem_manager_list = [MemoryType.USER_MEMORY.value]
    all_mem_manager_list = [MemoryType.USER_MEMORY.value]

    def __init__(self, managers: dict[str, BaseMemoryManager]):
        self.managers = managers

    async def search(
        self,
        user_id: str,
        scope_id: str,
        query: str,
        top_k: int = 5,
        threshold: float = 0.3,
        search_type: Optional[str] = None,
        **kwargs,
    ) -> list[dict[str, Any]] | None:
        """
        Search for user-related memory content

        Parameters:
            user_id (str): User ID
            scope_id (str): Scope ID
            query (str): Search query text
            top_k (int): Maximum number of results to return, default is 5
            threshold (float): Minimum score threshold for results, default is 0.3
            search_type (Optional[str]): Specific search type, if None searches all available types
            **kwargs: Additional parameters passed to specific search methods

        Returns:
            list[dict[str, Any]] | None: List of search results with score information, None if no results
        """
        # search_type is illegal
        if search_type is not None and search_type not in self.all_mem_manager_list:
            raise ValueError(f"{search_type} is not a valid search type")
        # search_type is valid, but the corresponding manager has not been initialized
        if search_type and not self.managers.get(search_type):
            raise ValueError(f"{search_type} memory manager not inited")
        result = []

        if search_type is None:
            for mem_type, manager in self.managers.items():
                if mem_type in self.user_mem_manager_list:
                    res = await manager.search(
                        user_id=user_id,
                        scope_id=scope_id,
                        query=query,
                        top_k=top_k,
                        **kwargs,
                    )
                    if res is not None:
                        result.extend(res)

        # call the manager corresponding to search_type
        else:
            res = await self.managers[search_type].search(
                user_id=user_id, scope_id=scope_id, query=query, top_k=top_k, **kwargs
            )
            if res:
                result.extend(res)

        # sort and truncate multiple search_type results based on score
        if len(result) > top_k:
            result.sort(key=lambda item: item["score"], reverse=True)
        return [item for item in result if item["score"] >= threshold][:top_k]

    async def list_user_mem(
        self, user_id: str, scope_id: str, offset: int, size: int, **kwargs
    ) -> list[dict[str, Any]] | None:
        """List user memory items within a specific range based on offset and size"""
        if offset < 0 or size <= 0:
            logger.error(
                f"list_user_mem failed, offset and size must be positive, offset:{offset}, size:{size}"
            )
            return None
        user_mem_manager = self.managers.get(MemoryType.USER_MEMORY.value)
        if not user_mem_manager:
            raise ValueError("list_user_mem failed, user_mem_manager not inited")
        return await user_mem_manager.list_memories(
            user_id=user_id, scope_id=scope_id, offset=offset, size=size
        )

    async def get_user_variable(
        self, user_id: str, scope_id: str, var_name: str
    ) -> str | None:
        """Retrieve a specific user variable value by name"""
        if MemoryType.VARIABLE.value not in self.managers:
            raise ValueError(f"{MemoryType.VARIABLE.value} memory manager not inited")
        if not isinstance(self.managers[MemoryType.VARIABLE.value], VariableManager):
            raise ValueError(
                f"{MemoryType.VARIABLE.value} manager class is not VariableManager"
            )
        res = await self.managers[MemoryType.VARIABLE.value].query_variable(
            user_id=user_id, scope_id=scope_id, name=var_name
        )
        if res is None:
            return None
        return res[var_name]

    async def get_all_user_variable(
        self, user_id: str, scope_id: str
    ) -> dict[str, Any]:
        """Retrieve all user variables for a given user and scope"""
        if MemoryType.VARIABLE.value not in self.managers:
            raise ValueError(f"{MemoryType.VARIABLE.value} memory manager not inited")
        if not isinstance(self.managers[MemoryType.VARIABLE.value], VariableManager):
            raise ValueError(
                f"{MemoryType.VARIABLE.value} manager class is not VariableManager"
            )
        return await self.managers[MemoryType.VARIABLE.value].query_variable(
            user_id=user_id, scope_id=scope_id
        )

    async def get_topic_profile(
        self, user_id: str, scope_id: str, topic_name: str
    ) -> dict[str, Any]:
        """Retrieve topic profile information for a specific user, scope, and topic name"""
        if MemoryType.TOPIC_PROFILE.value not in self.managers:
            raise ValueError(
                f"{MemoryType.TOPIC_PROFILE.value} memory manager not inited"
            )
        if not isinstance(
            self.managers[MemoryType.TOPIC_PROFILE.value], TopicProfileManager
        ):
            raise ValueError(
                f"{MemoryType.TOPIC_PROFILE.value} manager class is not TopicProfileManager"
            )
        return await self.managers[MemoryType.TOPIC_PROFILE.value].get_topic_profile(
            user_id=user_id, scope_id=scope_id, topic_name=topic_name
        )

    async def get_all_topic_profile(
        self, user_id: str, scope_id: str
    ) -> dict[str, dict[str, Any]]:
        """Retrieve all topic profile information for a specific user and scope"""
        if MemoryType.TOPIC_PROFILE.value not in self.managers:
            raise ValueError(
                f"{MemoryType.TOPIC_PROFILE.value} memory manager not inited"
            )
        if not isinstance(
            self.managers[MemoryType.TOPIC_PROFILE.value], TopicProfileManager
        ):
            raise ValueError(
                f"{MemoryType.TOPIC_PROFILE.value} manager class is not TopicProfileManager"
            )
        return await self.managers[
            MemoryType.TOPIC_PROFILE.value
        ].get_all_topic_profile(user_id=user_id, scope_id=scope_id)
