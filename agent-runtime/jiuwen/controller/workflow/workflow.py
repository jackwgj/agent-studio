#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Union, Generator, Dict, Any, AsyncGenerator

from jiuwen.controller.common.message import Message
from jiuwen.graph_engine.graph_engine import GraphEngine
from jiuwen.graph_engine.spiffworkflow.spiffworkflow_graph_engine import (
    SpiffWorkflowGraphEngine,
)
from jiuwen.orchestration.flow.workflow import Workflow


class ControllerWorkflow:
    """工作流基类"""

    def __init__(
        self, workflow_id: str, description: str, params: Dict[str, Any], graph_instance
    ):
        """初始化工作流控制器

        Args:
            workflow_id (str): 工作流唯一标识符
            description (str): 工作流描述信息
            params (Dict[str, Any]): 工作流参数配置
            graph_instance: 图引擎实例
        """
        self.workflow_id = workflow_id
        self.description = description
        self.params = params
        self.graph_engine = self._build_graph(graph_instance)

    def stream(self, inputs, params: dict, **kwargs) -> Union[Message, Generator]:
        """流式启动工作流

        Args:
            inputs: 工作流输入数据
            params (dict): 工作流参数
            **kwargs: 其他关键字参数

        Returns:
            Union[Message, Generator]: 消息对象或生成器
        """
        return self.graph_engine.run_workflow(inputs, params, **kwargs)

    async def astream(
        self, inputs, params: dict, **kwargs
    ) -> Union[Message, AsyncGenerator]:
        """异步流式启动工作流

        Args:
            inputs: 工作流输入数据
            params (dict): 工作流参数
            **kwargs: 其他关键字参数

        Returns:
            Union[Message, AsyncGenerator]: 消息对象或异步生成器
        """
        return await self.graph_engine.arun_workflow(inputs, params, **kwargs)

    def resume(self, inputs, params: dict, **kwargs) -> Union[Message, Generator]:
        """恢复被中断的工作流

        Args:
            inputs: 工作流输入数据
            params (dict): 工作流参数
            **kwargs: 其他关键字参数

        Returns:
            Union[Message, Generator]: 消息对象或生成器
        """
        return self.graph_engine.resume_workflow(inputs, params, **kwargs)

    async def cleanup(self) -> None:
        """Clean workflow runtime resources after execution."""
        graph_instance = getattr(self.graph_engine, "graph_instance", None)
        clean_up = getattr(graph_instance, "async_clean_up", None)
        if clean_up:
            await clean_up()

    def mark_interrupted(self) -> None:
        """Mark current workflow execution as interrupted by questioner/user interaction."""
        graph_instance = getattr(self.graph_engine, "graph_instance", None)
        if graph_instance is not None:
            graph_instance.stop_bpmn_flag = True

    def get_workflow_execute_status(self):
        """Return current workflow execute status from the workflow instance."""
        graph_instance = getattr(self.graph_engine, "graph_instance", None)
        get_status = getattr(graph_instance, "get_workflow_execute_status", None)
        if get_status:
            return get_status()
        return None

    def get_workflow_state(self):
        """Return resumable workflow state from the workflow instance."""
        graph_instance = getattr(self.graph_engine, "graph_instance", None)
        get_state = getattr(graph_instance, "get_state", None)
        if get_state:
            return get_state()
        return None

    def _build_graph(self, graph_instance) -> GraphEngine:
        """构建工作流图,子类需要实现

        Args:
            graph_instance: 图实例对象

        Returns:
            GraphEngine: 图引擎实例

        Raises:
            NotImplementedError: 子类必须实现此方法
        """
        raise NotImplementedError


class SpiffWorkflowControllerWorkflow(ControllerWorkflow):
    """Spiff工作流控制器工作流类"""

    def __init__(
        self, workflow_id: str, description: str, params: Dict[str, Any], graph_instance
    ):
        """初始化Spiff工作流控制器

        Args:
            workflow_id (str): 工作流唯一标识符
            description (str): 工作流描述信息
            params (Dict[str, Any]): 工作流参数配置
            graph_instance: 图引擎实例
        """
        super().__init__(workflow_id, description, params, graph_instance)

    def _build_graph(self, graph_instance: Workflow) -> SpiffWorkflowGraphEngine:
        """构建Spiff工作流图引擎

        Args:
            graph_instance (Workflow): 工作流实例

        Returns:
            SpiffWorkflowGraphEngine: Spiff工作流图引擎实例
        """
        return SpiffWorkflowGraphEngine(graph_instance)

    def get_runtime_context(self):
        return self.graph_engine.graph_instance.runtime_context
