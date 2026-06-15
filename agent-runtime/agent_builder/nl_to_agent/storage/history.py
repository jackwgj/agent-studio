# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
# N2L临时九问补丁，若jiuwen已更新该代码，则删除
import json
import os
from typing import List

import redis

from agent_runtime.common.config import settings as global_settings
from agent_runtime.common.logger import logger as rt_logger
from jiuwen.common.configs.env_constants import DATASOURCE_REDIS_TTL_KEY
from jiuwen.common.utils.singleton import Singleton
from jiuwen.memory.store.history import EXPIRATION_TIME

from .base import BaseConversationMemory


def _create_sync_redis_client() -> redis.Redis:
    """使用 agent_runtime 的 Redis 配置创建同步 Redis 客户端"""
    redis_settings = global_settings.redis
    if not redis_settings.host and not redis_settings.cluster_nodes:
        raise RuntimeError(
            "Redis not configured in agent_runtime settings, "
            "please check REDIS_HOST / REDIS_CLUSTER_NODES"
        )
    connection_kwargs = {
        "host": redis_settings.host,
        "port": redis_settings.port,
        "db": redis_settings.db,
        "decode_responses": False,
    }
    if redis_settings.password:
        connection_kwargs["password"] = redis_settings.password
    if redis_settings.ssl_enabled:
        connection_kwargs["ssl"] = True
    rt_logger.info(
        "HistoryStorage: creating sync Redis client: host=%s:%s, db=%s",
        redis_settings.host,
        redis_settings.port,
        redis_settings.db,
    )
    return redis.Redis(**connection_kwargs)


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
    使用 agent_runtime 的 Redis 配置，与 agent_runtime 其他接口保持一致
    """

    def __init__(self):
        self.redis_db = _create_sync_redis_client()

    def add(self, key: object, value: object) -> bool:
        ttl = int(os.getenv(DATASOURCE_REDIS_TTL_KEY, EXPIRATION_TIME))
        self.redis_db.rpush(key, json.dumps(value, ensure_ascii=False))
        self.redis_db.expire(key, ttl)
        return True

    def get_nearest_k(self, key: object, k: int) -> List[object]:
        if k <= 0:
            return []
        length = self.redis_db.llen(key)
        if k > length:
            k = length
        start = length - k
        return self.redis_db.lrange(key, start, -1)

    def get_all(self, key: object) -> List[object]:
        return self.redis_db.lrange(key, 0, -1)

    def get(self, key: object) -> object:
        return self.redis_db.get(key)

    def contains(self, key: object) -> bool:
        return self.redis_db.exists(key) > 0

    def delete(self, key: object) -> bool:
        return self.redis_db.delete(key) > 0
