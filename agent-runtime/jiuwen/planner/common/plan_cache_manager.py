#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2024-02-23
@LastEditTime: 2023-02-23 17:01:34
@Description: Plan内部缓存处理器
"""

from jiuwen.planner.common.enum_class import PlanCacheKey


class PlanCacheManager:
    """Plan cache manager"""

    _local_data = {}

    @classmethod
    def init(cls, cache_id):
        """
        init PlanCache
        """
        cls._local_data[cache_id] = {}

    @classmethod
    def clear(cls, cache_id):
        """
        init global vars
        """
        cls._local_data[cache_id] = {}

    @classmethod
    def add_cache(cls, cache_id, cache_key: PlanCacheKey, cache_value):
        """
        init global vars
        """
        cls._local_data[cache_id][cache_key] = cache_value

    @classmethod
    def get_cache(cls, cache_id, cache_key: PlanCacheKey):
        """
        init global vars
        """
        return cls._local_data[cache_id][cache_key]
