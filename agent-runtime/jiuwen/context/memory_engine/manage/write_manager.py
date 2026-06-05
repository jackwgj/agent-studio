#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.manage.base_memory_manager import BaseMemoryManager
from jiuwen.context.memory_engine.mem_unit.memory_type import MemoryType
from jiuwen.context.memory_engine.mem_unit.memory_unit import BaseMemoryUnit
from jiuwen.context.memory_engine.store.user_mem_store import UserMemStore


class WriteManager:
    def __init__(self, managers: dict[str, BaseMemoryManager], mem_store: UserMemStore):
        self.managers = managers
        self.mem_store = mem_store

    async def add_mem(self, mem_units: list[BaseMemoryUnit]):
        """add processed memory data to store"""
        for mem_unit in mem_units:
            mem_type = mem_unit.mem_type
            if mem_type in [MemoryType.USER_PROFILE, MemoryType.OTHERS]:
                mem_type = MemoryType.USER_MEMORY
            if mem_type.value in self.managers:
                await self.managers[mem_type.value].add(memory=mem_unit)
            else:
                logger.warning(f"Unsupported memory type: {mem_type.value}")

    async def update_mem_by_id(
        self, user_id: str, scope_id: str, mem_id: str, memory: str
    ):
        """update memory by its id"""
        await self.managers[MemoryType.USER_MEMORY.value].update(
            user_id, scope_id, mem_id, memory
        )

    async def delete_mem_by_id(self, user_id: str, scope_id: str, mem_id: str):
        """delete memory by its id"""
        await self.managers[MemoryType.USER_MEMORY.value].delete(
            user_id, scope_id, mem_id
        )

    async def delete_mem_by_user_id(self, user_id: str, scope_id: str):
        """delete all memory datas with the specified id"""
        for manager in self.managers:
            await self.managers[manager].delete_by_user_id(
                user_id=user_id, scope_id=scope_id
            )

    async def delete_mem_by_scope_id(self, scope_id: str):
        """delete all memory datas with the specified id"""
        for manager in self.managers:
            await self.managers[manager].delete_by_scope_id(scope_id=scope_id)

    async def __get_mem_type_from_store(
        self, user_id: str, scope_id: str, mem_id: str
    ) -> str | None:
        data = None
        try:
            data = await self.mem_store.get(
                user_id=user_id, scope_id=scope_id, mem_id=mem_id
            )
        except Exception as e:
            logger.error(f"Failed to get memory: {e}")
            return None
        if data is None:
            logger.error(
                f"Failed to get memory, mem_id:{mem_id}, user_id:{user_id}, scope_id:{scope_id}"
            )
            return None
        if "mem_type" not in data:
            logger.error(
                f"The mem_type field does not exist, mem_id:{mem_id}, user_id:{user_id}, scope_id:{scope_id}"
            )
            return None
        mem_type = data["mem_type"]
        if mem_type not in self.managers:
            logger.error(
                f"Unsupported mem_type:{mem_type}, mem_id:{mem_id}, user_id:{user_id}, scope_id:{scope_id}"
            )
            return None
        return mem_type
