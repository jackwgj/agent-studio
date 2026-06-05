#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.

from datetime import datetime, timezone
from typing import Any

from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.manage.base_memory_manager import BaseMemoryManager
from jiuwen.context.memory_engine.manage.data_id_manager import DataIdManager
from jiuwen.context.memory_engine.mem_unit.memory_unit import (
    ConflictType,
    BaseMemoryUnit,
    UserMemoryUnit,
)
from jiuwen.context.memory_engine.store.base_memory_index import (
    BaseMemoryIndex,
    MemoryDoc,
)


class UserMemoryManager(BaseMemoryManager):
    def __init__(self, data_id_generator: DataIdManager):
        self.data_id_generator = data_id_generator
        self.memory_index: BaseMemoryIndex | None = None

    async def add(self, memory: BaseMemoryUnit, **kwargs):
        """add a new user memory"""
        if not isinstance(memory, BaseMemoryUnit):
            raise ValueError("Add user memory Must pass BaseMemoryUnit class.")
        if not memory.user_id:
            raise ValueError("Add user memory must pass user_id")
        if not memory.scope_id:
            raise ValueError("Add user memory must pass scope_id")
        if isinstance(memory, UserMemoryUnit):
            new_memory = memory.mem
        else:
            raise ValueError(f"not support memory type: {memory.mem_type}")
        if not self.memory_index:
            raise ValueError("Add user memory must pass memory index")
        for conflict in memory.conflict_info:
            conf_id = conflict["id"]
            conf_mem = conflict["text"]
            conf_event = conflict["event"]
            time = datetime.now(timezone.utc)
            if not conf_mem:
                continue
            if conf_id == "-1" and conf_event == ConflictType.ADD.value:
                mem_id = str(
                    await self.data_id_generator.generate_next_id(
                        user_id=memory.user_id
                    )
                )
                new_memory_doc = MemoryDoc(
                    id=mem_id, text=conf_mem, type=memory.mem_type.value, timestamp=time
                )
                await self.memory_index.add_memories(
                    user_id=memory.user_id,
                    scope_id=memory.scope_id,
                    memories=[new_memory_doc],
                )
                logger.info(
                    f"add or update conflict info: {conflict}, new_memory: {new_memory}"
                )
            elif conf_event == ConflictType.NONE.value:
                logger.info(f"none conflict info: {conflict}, new_memory: {new_memory}")
            elif conf_event == ConflictType.UPDATE.value:
                await self.update(
                    user_id=memory.user_id,
                    scope_id=memory.scope_id,
                    mem_id=conf_id,
                    new_memory=new_memory,
                    mem_type=memory.mem_type.value,
                )
                logger.info(
                    f"update conflict info: {conflict}, update_memory: {new_memory}"
                )
            elif conf_event == ConflictType.DELETE.value:
                logger.info(
                    f"delete conflict info: {conflict}, new_memory: {new_memory}"
                )
                await self.memory_index.delete_memories(
                    user_id=memory.user_id, scope_id=memory.scope_id, ids=[conf_id]
                )

    async def update(
        self, user_id: str, scope_id: str, mem_id: str, new_memory: str, **kwargs
    ) -> bool:
        """update user memory with the specified mem id"""
        if not self.memory_index:
            raise ValueError("Update user memory must pass memory index")
        mem_type = kwargs.get("mem_type")
        if not mem_type:
            old_memory_doc = await self.memory_index.get_by_id(
                user_id=user_id, scope_id=scope_id, mem_id=mem_id
            )
            if not old_memory_doc:
                logger.error(f"{mem_id} not exist, update failed")
                return False
            mem_type = old_memory_doc.type
        time = datetime.now(timezone.utc)
        new_memory_doc = MemoryDoc(
            id=mem_id, text=new_memory, type=mem_type, timestamp=time
        )
        await self.memory_index.delete_memories(
            user_id=user_id, scope_id=scope_id, ids=[mem_id]
        )
        await self.memory_index.add_memories(
            user_id=user_id, scope_id=scope_id, memories=[new_memory_doc]
        )
        return True

    async def search(
        self, user_id: str, scope_id: str, query: str, top_k: int, **kwargs
    ):
        """search user memories by query and return up to top_k results"""
        if not self.memory_index:
            raise ValueError("Search user memory must pass memory index")
        try:
            results = await self.memory_index.search(
                user_id=user_id, scope_id=scope_id, query=query, top_k=top_k
            )
            result_dict = [
                {
                    "id": doc.id,
                    "user_id": user_id or "",
                    "scope_id": scope_id or "",
                    "mem": doc.text,
                    "mem_type": doc.type,
                    "timestamp": doc.timestamp,
                    "score": score,
                }
                for doc, score in results
            ]
            result_dict.sort(key=lambda x: x["score"], reverse=True)
        except Exception:
            return None
        return result_dict

    async def get(
        self, user_id: str, scope_id: str, mem_id: str, **kwargs
    ) -> dict[str, Any] | None:
        """get user memory with the specified mem id"""
        if not self.memory_index:
            raise ValueError("Get user memory must pass memory index")
        doc = await self.memory_index.get_by_id(
            user_id=user_id, scope_id=scope_id, mem_id=mem_id
        )
        return {
            "id": doc.id,
            "user_id": user_id or "",
            "scope_id": scope_id or "",
            "mem": doc.text,
            "mem_type": doc.type,
            "timestamp": doc.timestamp,
        }

    async def delete(self, user_id: str, scope_id: str, mem_id: str, **kwargs):
        """delete user memory with the specified mem id"""
        if not self.memory_index:
            raise ValueError("Delete user memory must pass memory index")
        data = await self.memory_index.get_by_id(
            user_id=user_id, scope_id=scope_id, mem_id=mem_id
        )
        if data is None:
            logger.error(
                f"Delete user_memory in store failed, the mem of mem_id({mem_id}) is not exist."
            )
            return False
        await self.memory_index.delete_memories(
            user_id=user_id, scope_id=scope_id, ids=[mem_id]
        )
        return True

    async def delete_by_user_id(self, user_id: str, scope_id: str, **kwargs):
        """delete all user memories with the specified id"""
        if not self.memory_index:
            logger.info(
                "memory_index is none, skip delete_by_user_id in user memory manager"
            )
            return True
        if scope_id is None:
            await self.memory_index.delete_by_user(user_id=user_id)
        else:
            await self.memory_index.delete_by_user_and_scope(
                user_id=user_id, scope_id=scope_id
            )
        return True

    async def delete_by_scope_id(self, scope_id: str, **kwargs):
        if not self.memory_index:
            logger.info(
                "memory_index is none, skip delete_by_scope_id in user memory manager"
            )
            return True
        await self.memory_index.delete_by_scope(scope_id=scope_id)

    async def list_memories(
        self, user_id: str, scope_id: str, offset: int = 0, size: int = 10, **kwargs
    ) -> list[dict[str, Any]]:
        """list all user memory datas by the specified id and type"""
        if not self.memory_index:
            raise ValueError("List user memory must pass memory index")
        memory_docs = await self.memory_index.list_memories(
            user_id=user_id, scope_id=scope_id, offset=offset, limit=size
        )
        return [
            {
                "id": doc.id,
                "user_id": user_id or "",
                "scope_id": scope_id or "",
                "mem": doc.text,
                "mem_type": doc.type,
                "timestamp": doc.timestamp,
            }
            for doc in memory_docs
        ]
