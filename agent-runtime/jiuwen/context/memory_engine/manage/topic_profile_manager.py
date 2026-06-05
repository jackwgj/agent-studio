#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from typing import Any

from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.manage.base_memory_manager import BaseMemoryManager
from jiuwen.context.memory_engine.mem_unit.memory_unit import TopicProfileUnit
from jiuwen.context.memory_engine.store.base_kv_store import BaseKVStore
from jiuwen.context.memory_engine.store.kv_store_wrapper import KVStoreWrapper


class TopicProfileManager(BaseMemoryManager):
    KEY_PREFIX = "topic_profile"

    def __init__(self, kv_store: BaseKVStore):
        self.kv_store_wrapper: KVStoreWrapper = KVStoreWrapper(kv_store)

    async def add(self, memory: TopicProfileUnit, **kwargs):
        """add a new topic profile"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None, skip add")
            return
        await self.kv_store_wrapper.set(
            main_key=[
                self.KEY_PREFIX,
                memory.user_id,
                memory.scope_id,
                memory.topic_name,
            ],
            sub_key=[memory.tag_name],
            value=memory.tag_mem,
        )

    async def update(
        self, user_id: str, scope_id: str, mem_id: str, new_memory: str, **kwargs
    ):
        """not implemented in class TopicProfileManager"""
        logger.warning("not implemented method update")
        pass

    async def delete(self, user_id: str, scope_id: str, mem_id: str, **kwargs):
        """not implemented in class TopicProfileManager"""
        logger.warning("not implemented method delete")
        pass

    async def delete_by_user_id(self, user_id: str, scope_id: str, **kwargs):
        """delete all topic profiles with the specified id"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None, skip delete by user id")
            return
        if scope_id:
            await self.kv_store_wrapper.delete_main_key_by_prefix(
                [self.KEY_PREFIX, user_id, scope_id]
            )
        else:
            await self.kv_store_wrapper.delete_main_key_by_prefix(
                [self.KEY_PREFIX, user_id]
            )

    async def delete_by_scope_id(self, scope_id: str, **kwargs):
        """delete memory by scope id."""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None, skip delete by user id")
            return
        await self.kv_store_wrapper.delete_by_pattern(pattern=scope_id)

    async def get(
        self, user_id: str, scope_id: str, mem_id: str, **kwargs
    ) -> dict[str, Any] | None:
        """not implemented in class TopicProfileManager"""
        logger.warning("not implemented method get")
        pass

    async def list_memories(
        self, user_id: str, scope_id: str, offset: int, size: int, **kwargs
    ) -> dict[str, Any] | None:
        """get memory by its id."""
        logger.warning(
            "not implemented method list_memories in class TopicProfileManager"
        )
        pass

    async def search(
        self, user_id: str, scope_id: str, query: str, top_k: int, **kwargs
    ):
        """not implemented in class TopicProfileManager"""
        logger.warning("not implemented method search")
        pass

    async def get_topic_profile(
        self, user_id: str, scope_id: str, topic_name: str
    ) -> dict[str, str]:
        """get all tag datas of specified topic"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None, skip get topic profile")
            return {}
        if not user_id or not user_id.strip():
            logger.error("get_topic_profile failed, user ID is empty")
            return {}
        if not scope_id or not scope_id.strip():
            logger.error("get_topic_profile failed, scope ID is empty")
            return {}
        return await self.kv_store_wrapper.get_all_value(
            main_key=[self.KEY_PREFIX, user_id, scope_id, topic_name]
        )

    async def get_all_topic_profile(
        self, user_id: str, scope_id: str
    ) -> dict[str, dict[str, Any]]:
        """get all topic datas of specified user and scope"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None, skip get all topic profile")
            return {}
        if not user_id or not user_id.strip():
            logger.error("get_all_topic_profile failed, user ID is empty")
            return {}
        if not scope_id or not scope_id.strip():
            logger.error("get_all_topic_profile failed, scope ID is empty")
            return {}
        all_values = await self.kv_store_wrapper.get_all_value_by_prefix(
            [self.KEY_PREFIX, user_id, scope_id]
        )
        seperator = self.kv_store_wrapper.get_seperator()
        return {k.rsplit(seperator, 1)[-1]: v for k, v in all_values.items()}

    async def update_topic_profile(
        self, user_id: str, scope_id: str, topic_name: str, tag_name: str, tag_desc: str
    ):
        """update topic profile with the specified tag name and tag description for the specified id and topic name"""
        if self.kv_store_wrapper is None:
            logger.error("kv_store_wrapper cannot be None, skip update topic profile")
            return
        await self.kv_store_wrapper.set(
            main_key=[self.KEY_PREFIX, user_id, scope_id, topic_name],
            sub_key=[tag_name],
            value=tag_desc,
        )

    async def delete_topic_profile_by_tag(
        self, user_id: str, scope_id: str, topic_name: str, tag_name: str
    ):
        """delete specified tag data by user_id, scope_id, topic_name and tag_name"""
        if self.kv_store_wrapper is None:
            logger.error(
                "kv_store_wrapper cannot be None, skip delete topic profile by tag"
            )
            return
        await self.kv_store_wrapper.delete(
            main_key=[self.KEY_PREFIX, user_id, scope_id, topic_name],
            sub_key=[tag_name],
        )

    async def delete_topic_profile_by_topic(
        self, user_id: str, scope_id: str, topic_name: str
    ):
        """delete all tag datas of specified topic for the specified id"""
        if self.kv_store_wrapper is None:
            logger.error(
                "kv_store_wrapper cannot be None, skip delete topic profile by topic"
            )
            return
        await self.kv_store_wrapper.delete_main_key(
            main_key=[self.KEY_PREFIX, user_id, scope_id, topic_name]
        )

    async def delete_all_topic_profile_by_tag(self, topic_name: str, tag_name: str):
        """delete specified tag data by topic_name and tag_name"""
        if self.kv_store_wrapper is None:
            logger.error(
                "kv_store_wrapper cannot be None, skip delete all topic profile by tag"
            )
            return
        if not topic_name or not tag_name:
            logger.error(
                "topic_name or tag_name cannot be empty, skip delete all topic profile by tag"
            )
            return
        await self.kv_store_wrapper.delete_by_prefix_suffix(
            main_key_prefix=[self.KEY_PREFIX],
            main_key_suffix=[topic_name],
            sub_key=[tag_name],
        )
