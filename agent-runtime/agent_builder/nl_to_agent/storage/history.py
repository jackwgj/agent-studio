# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
# N2L临时九问补丁，若jiuwen已更新该代码，则删除
import json
import os
from typing import List

from jiuwen.common.configs.env_constants import DATASOURCE_REDIS_TTL_KEY
from jiuwen.common.store.redis import get_redis_instance
from jiuwen.common.utils.singleton import Singleton
from jiuwen.memory.store.history import EXPIRATION_TIME

from .base import BaseConversationMemory


class HistoryStorage(metaclass=Singleton):
    """对话历史存储"""

    _initialized = False
    _storage_medium = None

    def __new__(cls, *args, **kwargs):
        instance = super().__new__(cls)
        if not cls._initialized:
            instance._init_storage()
            cls._initialized = True
        return instance

    def add(self, key: object, value: object) -> bool:
        """新增对话历史记录"""
        return self._storage_medium.add(key, value)

    def get_all(self, key: object) -> List[object]:
        """获取对话历史记录列表"""
        return self._storage_medium.get_all(key)

    def get_nearest_k(self, key: object, k: int) -> List[object]:
        """获取最近的k条对话历史记录"""
        return self._storage_medium.get_nearest_k(key, k)

    def delete(self, key: object) -> bool:
        """删除对话历史记录"""
        return self._storage_medium.delete(key)

    def _init_storage(self):
        """初始化存储中间件"""
        self._storage_medium = ImRedisConversation()


class ImRedisConversation(BaseConversationMemory):
    """
    history conversation memory in redis
    """

    def __init__(self):
        self.redis_db = get_redis_instance()

    def add(self, key: object, value: object) -> bool:
        ttl = int(os.getenv(DATASOURCE_REDIS_TTL_KEY, EXPIRATION_TIME))
        self.redis_db.list_append(key, json.dumps(value, ensure_ascii=False), ttl=ttl)
        return True

    def get_nearest_k(self, key: object, k: int) -> List[object]:
        if k <= 0:
            return []
        length = self.redis_db.list_length(key)
        if k > length:
            k = length
        start = length - k
        return self.redis_db.list_get(key, start, -1)

    def get_all(self, key: object) -> List[object]:
        return self.redis_db.list_get(key, 0, -1)

    def get(self, key: object) -> object:
        raise self.redis_db.get(key)

    def contains(self, key: object) -> bool:
        return self.redis_db.list_contain(key)

    def delete(self, key: object) -> bool:
        return self.redis_db.delete(key)
