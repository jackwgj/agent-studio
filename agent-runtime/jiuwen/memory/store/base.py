#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""abstract plan and conversation memory"""

from abc import abstractmethod, ABC

from typing import Dict


class BaseConversationMemory(ABC):
    """abstract conversation memory"""

    @abstractmethod
    def add(self, inputs: object, outputs: object) -> bool:
        """
        Add a k-v pair to the store
        """

    @abstractmethod
    def get(self, key: object) -> object:
        """
        Get the value of given key.

        Returns:
            object: item of given key in the store
        """

    @abstractmethod
    def contains(self, key: object) -> bool:
        """
        Get all items in the store.

        Returns:
            bool: whether the key is in the store.
        """

    @abstractmethod
    def get_all(self) -> Dict[object, object]:
        """
        Get all items in the store.

        Returns:
            Dict[object, object]: all items in the store.
        """

    @abstractmethod
    def get_nearest_k(self, k: int) -> Dict[object, object]:
        """
        Get the nearest k items in the store.

        Returns:
            Cache: all items in cache.
        """

    @abstractmethod
    def on_delete(self) -> int:
        """
        Delete expired keys and return number of entries deleted.

        Returns:
            int: Number of entries deleted.
        """

    @abstractmethod
    def delete(self, key: object) -> bool:
        """
        Delete key and return number of entries deleted (``1`` or ``0``).

        Args:
            key: Cache key to delete.

        Returns:
            int: True if key was deleted, False if key didn't exist.
        """

    @abstractmethod
    def memorize(self, ttl: int, typed=False):
        """
        Decorator that wraps a function with a memorizing callable and works on both synchronous and
        asynchronous functions.

        Each return value from the function will be cached using the function arguments as the cache
        key. The cache object can be accessed at ``<function>.cache``. The uncached version (i.e.
        the original function) can be accessed at ``<function>.uncached``. Each return value from
        the function will be cached using the function arguments as the cache key. Calculate the
        cache key for the provided function arguments for use in other operations of the
        ``<function>.cache`` object using the function accessible at ``<function>.cache_key``

        Keyword Args:
            ttl: TTL value. Defaults to ``None`` which uses :attr:`ttl`. Time units
                are determined by :attr:`timer`.
            typed: Whether to cache arguments of a different type separately. For
                example, ``<function>(1)`` and ``<function>(1.0)`` would be treated differently.
                Defaults to ``False``.
        """
