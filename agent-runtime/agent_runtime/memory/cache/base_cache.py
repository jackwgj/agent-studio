#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from abc import abstractmethod


class BaseCacheMeta(type):
    def __new__(cls, name, bases, attrs):
        new_class = super().__new__(cls, name, bases, attrs)
        if hasattr(new_class, "register") and callable(getattr(new_class, "register")):
            new_class.register()
        return new_class


class BaseCache(metaclass=BaseCacheMeta):
    @classmethod
    @abstractmethod
    def register(cls):
        """初始化，自注册"""

    @abstractmethod
    def save(self, key, data):
        """保存数据"""

    @abstractmethod
    def pop(self, key):
        """弹出队列最新存入的数据"""

    @abstractmethod
    def read_tail(self, key, length=1):
        """返回队列最新的若干个值"""

    @abstractmethod
    def get_all(self, key):
        """返回队列所有值"""

    @abstractmethod
    def delete(self, key):
        """删除缓存"""
