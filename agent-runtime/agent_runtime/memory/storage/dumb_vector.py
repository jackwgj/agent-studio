#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""conversation and plan message definition"""

from .base_vector import BaseVector


class DumpVector(BaseVector):
    """
    解决向量数据库起来慢，导致服务长时间不可用的问题，在真正向量数据库起来前，使用这个类来返回
    """

    @classmethod
    def register(cls):
        pass

    @classmethod
    def search(
        cls,
        query: str,
        condition: str,
        recall_threshold: float,
        recall_num: int,
        *args,
        **kwargs,
    ) -> list:
        return []

    @classmethod
    def save(cls, file_data: list, **kwargs):
        pass

    @classmethod
    def remove(cls, **kwargs):
        pass

    @classmethod
    async def asearch(
        cls,
        query: str,
        condition: str,
        recall_threshold: float,
        recall_num: int,
        *args,
        **kwargs,
    ) -> list:
        return cls.search(
            query, condition, recall_threshold, recall_num, *args, **kwargs
        )
