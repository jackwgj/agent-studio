#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from typing import Any, Optional

from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.manage.base_memory_manager import BaseMemoryManager
from jiuwen.context.memory_engine.mem_unit.memory_unit import VariableUnit
from jiuwen.context.memory_engine.store.base_kv_store import BaseKVStore
from jiuwen.context.memory_engine.store.kv_store_wrapper import KVStoreWrapper


class VariableManager(BaseMemoryManager):
    KEY_PREFIX = "user_var"

    def __init__(self, kv_store: BaseKVStore):
        self.kv_store_wrapper: KVStoreWrapper = KVStoreWrapper(kv_store)

    @staticmethod
    def _check_user_and_scope_id(user_id, scope_id, context="Operation"):
        if not user_id or not user_id.strip():
            logger.error(f"{context} failed, user ID is empty")
        if not scope_id or not scope_id.strip():
            logger.error(f"{context} failed, scope ID is empty")

    async def add(self, memory: VariableUnit, **kwargs):
        """add Variable memory"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None")
            return

        await self.kv_store_wrapper.set(
            main_key=[VariableManager.KEY_PREFIX, memory.user_id, memory.scope_id],
            sub_key=[memory.variable_name],
            value=memory.variable_mem,
        )

    async def update(
        self, user_id: str, scope_id: str, mem_id: str, new_memory: str, **kwargs
    ):
        """not implemented in class VariableManager"""
        logger.warning("not implemented method update")
        pass

    async def update_user_variable(
        self, user_id: str, scope_id: str, var_name: str, var_mem: str
    ):
        """update the value of the specified variable name"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None")
            return

        await self.kv_store_wrapper.set(
            main_key=[VariableManager.KEY_PREFIX, user_id, scope_id],
            sub_key=[var_name],
            value=var_mem,
        )

    async def delete(self, user_id: str, scope_id: str, mem_id: str, **kwargs):
        """not implemented in class VariableManager"""
        logger.warning("not implemented method delete")
        pass

    async def delete_by_user_id(self, user_id: str, scope_id: str, **kwargs):
        """delete all variables with the specified id"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None")
            return
        if scope_id:
            await self.kv_store_wrapper.delete_main_key(
                main_key=[VariableManager.KEY_PREFIX, user_id, scope_id]
            )
        else:
            await self.kv_store_wrapper.delete_main_key_by_prefix(
                main_key=[VariableManager.KEY_PREFIX, user_id]
            )

    async def delete_by_scope_id(self, scope_id: str, **kwargs):
        """delete memory by scope id."""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None, skip delete by user id")
            return
        await self.kv_store_wrapper.delete_by_pattern(pattern=scope_id)

    async def delete_user_variable(self, user_id: str, scope_id: str, var_name: str):
        """delete user variable with the specified id and variable name"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None")
            return
        await self.kv_store_wrapper.delete(
            main_key=[VariableManager.KEY_PREFIX, user_id, scope_id],
            sub_key=[var_name],
        )

    async def get(
        self, user_id: str, scope_id: str, mem_id: str, **kwargs
    ) -> dict[str, Any] | None:
        """not implemented in class VariableManager"""
        logger.warning("not implemented method get")
        pass

    async def list_memories(
        self, user_id: str, scope_id: str, offset: int, size: int, **kwargs
    ) -> dict[str, Any] | None:
        """get memory by its id."""
        logger.warning("not implemented method list_memories in class VariableManager")
        pass

    async def search(
        self, user_id: str, scope_id: str, query: str, top_k: int, **kwargs
    ):
        """not implemented in class VariableManager"""
        logger.warning("not implemented method search")
        pass

    async def query_variable(
        self, user_id: str, scope_id: str, name: Optional[str] = None
    ) -> dict[str, Any]:
        """query variable by user_id, scope_id, variable_name return variable mem."""
        self._check_user_and_scope_id(user_id, scope_id, "Search")
        if not name:
            return await self.kv_store_wrapper.get_all_value(
                main_key=[VariableManager.KEY_PREFIX, user_id, scope_id]
            )

        kv_ret = await self.kv_store_wrapper.get(
            main_key=[VariableManager.KEY_PREFIX, user_id, scope_id],
            sub_key=[name],
        )
        return {name: kv_ret}
