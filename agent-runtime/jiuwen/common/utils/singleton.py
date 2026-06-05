#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""singleton metaclass"""

import abc
import threading

singleton_lock = threading.Lock()


class Singleton(abc.ABCMeta, type):
    """Singleton metaclass"""

    _instances = {}

    def __call__(cls, *args, **kwargs):
        """singleton metaclass."""
        with singleton_lock:
            if cls not in cls._instances:
                cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
            return cls._instances[cls]
