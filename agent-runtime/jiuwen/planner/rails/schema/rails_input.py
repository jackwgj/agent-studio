# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""input rails' input and output schema"""

from typing import Dict, Any, List

from pydantic import Field
from pydantic.dataclasses import dataclass


@dataclass
class InputRailsInputSchema:
    query: str = ""
    tools: List = Field(default_factory=list)

    @staticmethod
    def validate(data: Dict[str, Any]) -> "InputRailsInputSchema":
        """validate and output InputSchema"""
        if not isinstance(data, dict):
            raise ValueError("Input must be a dictionary.")

        if "query" not in data:
            raise ValueError("Missing 'query' field.")

        return InputRailsInputSchema(
            query=data["query"], tools=data.get("tools", list())
        )
