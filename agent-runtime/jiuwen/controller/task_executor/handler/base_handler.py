#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from abc import ABC, abstractmethod
from typing import Dict, Any, Union, AsyncGenerator

from jiuwen.controller.common.message import Message
from jiuwen.controller.common.task import Task
from jiuwen.controller.context_manager.context_manager import ContextManager


class BaseHandler(ABC):
    """处理器基类"""

    def __init__(self, context_manager: ContextManager):
        self.context_manager = context_manager

    @staticmethod
    def _get_nested_value(data: Dict[str, Any], path: str) -> Any:
        """从嵌套 dict 中安全取值，路径如 'a.b.c'"""
        if not isinstance(data, dict):
            return None
        keys = path.split(".")
        current = data
        for key in keys:
            if isinstance(current, dict) and key in current:
                current = current[key]
            else:
                return None
        return current

    @staticmethod
    def _set_nested_value(target: Dict[str, Any], path: str, value: Any) -> None:
        """将 value 设置到 dict 的 path 路径上（如 'a.b.c'）"""
        keys = path.split(".")
        current = target
        for key in keys[:-1]:
            if key not in current or not isinstance(current[key], dict):
                current[key] = {}
            current = current[key]
        current[keys[-1]] = value

    @abstractmethod
    def handle(self, task: Task, context: Dict[str, Any], **kwargs) -> Message:
        """非流式处理任务

        Args:
            task: 任务对象
            context: 当前上下文
            **kwargs: 额外参数

        Returns:
            Message: 处理结果消息
        """
        pass

    @abstractmethod
    async def astream_handle(
        self, task: Task, **kwargs
    ) -> AsyncGenerator[Union[Dict, str, Message], None]:
        """异步流式处理任务

        Args:
            task: 任务对象
            **kwargs: 额外参数

        Yields:
            流式处理结果，可以是字典、字符串或消息对象
        """
        pass
