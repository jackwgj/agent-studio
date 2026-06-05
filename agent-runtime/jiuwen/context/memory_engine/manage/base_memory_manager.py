#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from abc import abstractmethod, ABC
from typing import Any

from jiuwen.context.memory_engine.mem_unit.memory_unit import BaseMemoryUnit


class BaseMemoryManager(ABC):
    """
    Simplified abstract base class for memory manager implementations.
    Managing a specific type of memory data.
    """

    @abstractmethod
    async def add(self, memory: BaseMemoryUnit, **kwargs):
        """add memory."""
        pass

    @abstractmethod
    async def update(
        self, user_id: str, scope_id: str, mem_id: str, new_memory: str, **kwargs
    ):
        """update memory by its id."""
        pass

    @abstractmethod
    async def delete(self, user_id: str, scope_id: str, mem_id: str, **kwargs):
        """delete memory by its id."""
        pass

    @abstractmethod
    async def delete_by_user_id(self, user_id: str, scope_id: str, **kwargs):
        """delete memory by user id and scope id."""
        pass

    @abstractmethod
    async def delete_by_scope_id(self, scope_id: str, **kwargs):
        """delete memory by user id and scope id."""
        pass

    @abstractmethod
    async def get(
        self, user_id: str, scope_id: str, mem_id: str, **kwargs
    ) -> dict[str, Any] | None:
        """get memory by its id."""
        pass

    @abstractmethod
    async def list_memories(
        self, user_id: str, scope_id: str, offset: int, size: int, **kwargs
    ) -> dict[str, Any] | None:
        """get memory by its id."""
        pass

    @abstractmethod
    async def search(
        self, user_id: str, scope_id: str, query: str, top_k: int, **kwargs
    ):
        """query memory, return top k results"""
        pass
