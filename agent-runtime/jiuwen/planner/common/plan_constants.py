#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2024-02-23
@LastEditTime: 2023-02-23 17:01:34
@Description: 常量定义
"""

from typing import Union, List, Optional

from pydantic import BaseModel as pydantic_BaseModel, Field

DEFAULT_MAX_ITERATION = 10
SPLITER = "#*#*#"


class FunctionCallSchema(pydantic_BaseModel):
    """schema of LLM generated function call"""

    name: Union[str, None] = Field(
        default="", description="LLM generate function's name"
    )
    arguments: Union[str, None] = Field(
        default="{}", description="json string of LLM generated function's arguments"
    )
    tool_call_id: Optional[str] = Field(
        default="", description="LLM generate function's tool call id"
    )


class LlmOutput(pydantic_BaseModel):
    """schema of LLM output"""

    role: str
    content: Union[str, None]
    function_call: FunctionCallSchema = Field(default=FunctionCallSchema())
    function_call_list: List[FunctionCallSchema] = Field(default_factory=list)


class TimerInfo(dict):
    """time statistics info"""

    def __init__(
        self,
        total_latency: float = None,
        model_latency: float = None,
        wait_latency: float = None,
    ):
        self.total_latency = total_latency
        self.model_latency = model_latency
        self.wait_latency = wait_latency
        super().__init__(
            total_latency=total_latency,
            model_latency=self.model_latency,
            wait_latency=self.wait_latency,
        )
