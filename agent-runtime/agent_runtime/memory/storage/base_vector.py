#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""conversation and plan message definition"""

from abc import abstractmethod


class BaseVectorMeta(type):
    def __new__(cls, name, bases, attrs):
        new_class = super().__new__(cls, name, bases, attrs)
        if hasattr(new_class, "register") and callable(getattr(new_class, "register")):
            new_class.register()
        return new_class


class BaseVector(metaclass=BaseVectorMeta):
    """
    向量数据库存储类
    """

    @classmethod
    @abstractmethod
    def register(cls):
        """初始化，自注册"""

    @abstractmethod
    def search(
        self,
        query: str,
        condition: str,
        recall_threshold: float,
        recall_num: int,
        *args,
        **kwargs,
    ) -> dict:
        """根据关键字查询信息"""

    @abstractmethod
    async def asearch(
        self,
        query: str,
        condition: str,
        recall_threshold: float,
        recall_num: int,
        *args,
        **kwargs,
    ) -> list:
        """根据关键字查询信息"""

    @abstractmethod
    def save(self, data: str, **kwargs):
        """存储信息"""

    @abstractmethod
    def remove(self, **kwargs):
        """删除信息"""

    @abstractmethod
    def pop_old_data(self, user_id, workflow_id):
        """删除并返回过期数据"""
