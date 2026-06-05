#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any, AsyncGenerator, Union


class BaseAgent(ABC):
    @abstractmethod
    async def invoke(self, inputs: Union[dict, str], **kwargs) -> Any:
        """执行一次任务并返回最终结果。"""
        pass

    @abstractmethod
    async def astream(
        self, inputs: Union[dict, str], **kwargs
    ) -> AsyncGenerator[Any, None]:
        """以异步流式的方式产出中间/最终结果。"""
        pass
