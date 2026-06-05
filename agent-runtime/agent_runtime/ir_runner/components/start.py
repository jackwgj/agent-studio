#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
jiuwen.start 节点处理器
"""

from typing import Any

from openjiuwen.core.workflow import Start

from .base import BaseComponentHandler, BuildContext
from ..types import IR_TYPE_START


class StartHandler(BaseComponentHandler):
    """jiuwen.start 节点处理器"""

    IR_TYPE = IR_TYPE_START

    def create_component(
        self,
        ir_node: dict,
        context: BuildContext,
    ) -> Any:
        """
        创建 Start 组件

        Args:
            ir_node: IR 节点配置
            context: 构造上下文

        Returns:
            Start 组件实例
        """
        return Start()

    def register(
        self,
        workflow: Any,
        component: Any,
        node_id: str,
        ir_node: dict,
        context: BuildContext,
    ) -> None:
        """
        注册 Start 组件到工作流

        Args:
            workflow: 工作流实例
            component: Start 组件
            node_id: 注册到工作流的节点 ID
            ir_node: IR 节点配置
            context: 构造上下文
        """
        # 从 systemFields.inputs 中提取输入 schema
        configs = ir_node.get("configs", {})
        system_fields = configs.get("systemFields", {})
        inputs_schema = system_fields.get("inputs", [])

        # 构建 inputs_schema 字典
        schema_dict = {}
        for field in inputs_schema:
            field_id = field.get("id", "")
            if field_id:
                schema_dict[field_id] = f"${{{field_id}}}"

        # 注册到工作流
        workflow.set_start_comp(node_id, component, inputs_schema=schema_dict)
