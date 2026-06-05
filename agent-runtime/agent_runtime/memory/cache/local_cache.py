#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import copy
import threading
from typing import Dict

from .base_cache import BaseCache
from .factory import CacheFactory
from ..config import long_memory_config, CacheTypeEnum

MAX_LENGTH = 1000
DELETE_DELAY = 60


class LocalCache(BaseCache):
    @classmethod
    def register(cls):
        if long_memory_config.chat_cache_type == CacheTypeEnum.MEMORY.value:
            CacheFactory.register_chat_cache(cls)
        if long_memory_config.call_workflow_cache_type == CacheTypeEnum.MEMORY.value:
            CacheFactory.register_call_workflow_cache(cls)

    def __init__(self):
        self.chat_map: Dict[str, list] = {}
        self.lock_map: Dict[str, threading.Lock] = {}
        self.lock = threading.Lock()

    def save(self, key, data):
        current_list = self.get_chat_list(key)
        current_lock = self.get_chat_lock(key)
        with current_lock:
            current_list.append(copy.deepcopy(data))

    def pop(self, key):
        current_list = self.get_chat_list(key)
        current_lock = self.get_chat_lock(key)
        with current_lock:
            return current_list.pop() if current_list else None

    def read_tail(self, key, length=1):
        current_lock = self.get_chat_lock(key)
        with current_lock:
            return self.get_chat_list(key)[-length:] if length > 0 else []

    def delete(self, key):
        self.chat_map.pop(key, None)
        self.lock_map.pop(key, None)

    def get_chat_lock(self, key) -> threading.Lock:
        if key not in self.lock_map:
            with self.lock:
                self.lock_map.setdefault(key, threading.Lock())
        return self.lock_map[key]

    def get_chat_list(self, key) -> list:
        if key not in self.chat_map:
            with self.get_chat_lock(key):
                self.chat_map.setdefault(key, [])
        return self.chat_map[key]

    def get_all(self, key):
        return self.get_chat_list(key)
