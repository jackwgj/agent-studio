# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""rails output schema"""

from typing import Dict, Any, List

from pydantic import Field
from pydantic.dataclasses import dataclass


@dataclass
class InputRailsOutputSchema:
    query: str
    plugins: List = Field(default_factory=list)
    workflows: List = Field(default_factory=list)

    @staticmethod
    def validate(data: Dict[str, Any]) -> "InputRailsOutputSchema":
        """validate"""
        if "query" not in data:
            raise ValueError("Missing 'rewritten_query' field.")

        # 创建并返回数据类实例
        return InputRailsOutputSchema(
            query=data.get("query", ""),
            plugins=data.get("plugins", list()),
            workflows=data.get("workflows", list()),
        )
