#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""plan and conversation memory, stored in first-class cache"""

from typing import Dict

from cacheout import Cache, FIFOCache, LFUCache, LIFOCache, LRUCache
from jiuwen.memory.store.base import BaseConversationMemory


class BufferConversation(BaseConversationMemory):
    """conversation memory in first-class cache"""

    def __init__(self, maxsize=1000, ttl=0, policy="Cache"):
        self.maxsize = maxsize
        self.ttl = ttl
        self.policy = policy
        self.cache_type_dict = {
            "Cache": Cache,
            "FIFOCache": FIFOCache,
            "LFUCache": LFUCache,
            "LIFOCache": LIFOCache,
            "LRUCache": LRUCache,
        }
        if policy not in self.cache_type_dict:
            raise ValueError(
                f"policy can only be {', '.join(list(self.cache_type_dict.keys()))}."
            )
        self.cache: Cache = self.cache_type_dict.get(policy)(maxsize=maxsize, ttl=ttl)

    def __len__(self) -> int:
        return len(self.cache)

    def add(self, inputs: object, outputs: object) -> bool:
        self.cache.set(inputs, outputs)
        return True

    def get(self, key) -> object:
        return self.cache.get(key)

    def contains(self, key) -> bool:
        return key in self.cache

    def get_all(self) -> Dict[object, object]:
        item_dict = {}
        for key, value in self.cache.items():
            item_dict[key] = value
        return item_dict

    def get_nearest_k(self, k) -> Dict[object, object]:
        item_dict = {}
        for key, value in list(self.cache.items())[-k:]:
            item_dict[key] = value
        return item_dict

    def on_delete(self) -> int:
        return self.cache.delete_expired()

    def delete(self, key) -> bool:
        if key in self.cache.keys():
            return self.cache.delete(key) == 1
        return False

    def memorize(self, ttl, typed=False):
        return self.cache.memoize(ttl=ttl, typed=typed)
