# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Sync extends for orchestration."""

__all__ = ("Once",)

from threading import Lock
from typing import Callable


class Once:
    """Do once with thread-safe."""

    def __init__(self):
        self.__lock = Lock()
        self.__called = False
        self.__do = self.__todo

    @property
    def done(self):
        """Query if is called successfully."""
        with self.__lock:
            return self.__called

    @staticmethod
    def __done(f: Callable, /, *args, **kwargs) -> None:
        """Quick path for called."""

    def do(self, f: Callable, /, *args, **kwargs) -> None:
        """Call `f` if this instance hasn't been called.

        If exception happens, regard it as is not invoked.
        """
        return self.__do(f, *args, **kwargs)

    def __todo(self, f: Callable, /, *args, **kwargs) -> None:
        with self.__lock:
            if self.__called:
                return
            f(*args, **kwargs)
            self.__called = True
            self.__do = self.__done
