# coding:utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
Agent PluginSelector
"""

from typing import List


class PluginSelector:
    """Definition of Prompt PluginSelector."""

    def select(self, query: str, plugins: List[dict]) -> List[dict]:
        """Prompt select."""
        pass
