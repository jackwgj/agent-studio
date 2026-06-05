#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
@Copyright: Copyright (C) 2024-2024 Huawei Inc
@Project:
@Author: jiuwen
@Date: 2025-9-8
@LastEditTime: 2025-9-8 17:56:43
@Description:
"""

from enum import Enum
from typing import Optional


class OutputMode(Enum):
    """
    前端气泡展示方式枚举。
    """

    SEPARATE = "separate"
    APPEND = "append"
    REPLACE = "replace"

    @classmethod
    def from_str(cls, s: Optional[str]) -> Optional["OutputMode"]:
        """将字符串（大小写不敏感）解析为 OutputMode 枚举值。"""
        if s is None:
            return None
        try:
            return cls(s)
        except ValueError:
            return None
