#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Callable, Optional, Dict, Any

from jiuwen.common.log.base import logger
from jiuwen.controller.common.task import Task, TaskExecution
from jiuwen.controller.common.task_type import TaskType
from jiuwen.controller.task_executor import WorkflowHandler, BaseHandler
from jiuwen.controller.task_executor.handler.mcp_handler import McpHandler
from jiuwen.controller.task_executor.handler.plugin_handler import PluginHandler


class TaskDispatcher:
    """任务分发器，负责管理处理函数并执行任务"""

    def __init__(self, context_manager=None):
        """初始化任务分发器

        Args:
            context_manager: 上下文管理器
        """
        self.context_manager = context_manager

        # 处理函数注册表
        self.handlers = {}

        # 任务执行计数统计
        self.dispatch_count = 0
        self.handler_stats = {}
        # 初始化处理器
        self._initialize_handlers()

    def register_handler(self, task_type: TaskType, handler: BaseHandler):
        """注册任务处理函数

        Args:
            task_type: 任务类型
            handler: 处理函数
        """
        self.handlers[task_type] = handler
        self.handler_stats[task_type] = 0
        logger.debug(f"Registered handler for task type: {task_type}")

    def get_handler_for_task(self, task: Task) -> Optional[Callable]:
        """获取处理指定任务的函数

        Args:
            task: 任务

        Returns:
            Optional[Callable]: 处理函数，如果没有找到则返回None
        """
        return self.handlers.get(task.task_type)

    def dispatch(self, task: Task) -> Any:
        """分发任务到相应的处理函数

        Args:
            task: 要分发的任务
            context: 任务上下文

        Returns:
            Any: 任务执行结果
        """
        self.dispatch_count += 1

        # 获取任务处理函数
        handler = self.get_handler_for_task(task)

        if handler is None:
            error_msg = f"No handler found for task type: {task.task_type}"
            logger.error(error_msg)
            return error_msg
        # 更新统计
        self.handler_stats[task.task_type] = (
            self.handler_stats.get(task.task_type, 0) + 1
        )

        # 创建任务执行对象
        return TaskExecution(task, handler)

    def get_stats(self) -> Dict[str, Any]:
        """获取分发统计信息

        Returns:
            Dict[str, Any]: 分发统计信息
        """
        handler_type_stats = {}

        # 按处理函数类型分组
        for task_type, count in self.handler_stats.items():
            handler_type = str(task_type).split(".")[0]  # 提取类型前缀
            if handler_type not in handler_type_stats:
                handler_type_stats[handler_type] = 0
            handler_type_stats[handler_type] += count

        return {
            "total_dispatched": self.dispatch_count,
            "by_task_type": self.handler_stats,
            "by_handler_type": handler_type_stats,
        }

    def _initialize_handlers(self):
        """初始化所有处理器"""
        # 创建处理器实例
        workflow_handler: WorkflowHandler = WorkflowHandler(self.context_manager)
        plugin_handler: PluginHandler = PluginHandler(self.context_manager)
        mcp_handler: McpHandler = McpHandler(self.context_manager)
        # 注册处理器
        self.register_handler(TaskType.WORKFLOW_START, workflow_handler)
        self.register_handler(TaskType.WORKFLOW_RESUME, workflow_handler)
        self.register_handler(TaskType.WORKFLOW_COMPLETION, workflow_handler)
        self.register_handler(TaskType.WORKFLOW_MESSAGE, workflow_handler)
        self.register_handler(TaskType.PLUGIN_START, plugin_handler)
        self.register_handler(TaskType.MCP_START, mcp_handler)

        logger.debug("Task handlers initialized")
