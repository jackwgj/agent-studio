# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""input rails' input and output schema"""

from dataclasses import field
from typing import Dict, Any

from pydantic.dataclasses import dataclass


@dataclass
class ExecutionRailsInputSchema:
    arguments: Dict[str, Any] = field(default_factory=dict)

    @staticmethod
    def validate(data: Dict[str, Any]) -> "ExecutionRailsInputSchema":
        """validate and output InputSchema"""
        pass


@dataclass
class ExecutionRailsOutputSchema:
    arguments: Dict[str, Any] = field(default_factory=dict)

    @staticmethod
    def validate(data: Dict[str, Any]) -> "ExecutionRailsOutputSchema":
        """validate"""
        if "arguments" not in data:
            raise ValueError("Missing 'arguments' field.")

        # 创建并返回数据类实例
        return ExecutionRailsOutputSchema(arguments=data.get("arguments", ""))
