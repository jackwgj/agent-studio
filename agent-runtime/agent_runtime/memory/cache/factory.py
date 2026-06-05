#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import threading

from .base_cache import BaseCache


class CacheFactory:
    # 会话缓存
    _chat_cache_class = None
    _chat_cache = None
    # 工作流调用关系缓存
    _call_workflow_cache_class = None
    _call_workflow_cache = None
    _lock = threading.Lock()

    @classmethod
    def register_chat_cache(cls, chat_cache):
        CacheFactory._chat_cache_class = chat_cache

    @classmethod
    def register_call_workflow_cache(cls, call_workflow_cache):
        CacheFactory._call_workflow_cache_class = call_workflow_cache

    @classmethod
    def get_chat_cache(cls) -> BaseCache:
        if cls._chat_cache is None:
            with cls._lock:
                if cls._chat_cache is None:
                    cls._chat_cache = cls._chat_cache_class()
        return cls._chat_cache

    @classmethod
    def get_call_workflow_cache(cls) -> BaseCache:
        if cls._call_workflow_cache is None:
            with cls._lock:
                if cls._call_workflow_cache is None:
                    cls._call_workflow_cache = cls._call_workflow_cache_class()
        return cls._call_workflow_cache
