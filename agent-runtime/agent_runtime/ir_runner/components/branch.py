#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
jiuwen.branch 节点处理器

分支节点需要特殊处理：
1. 先注册 BranchComponent 到工作流
2. 解析 connections 时，逐条添加 branch 条件
3. 所有 connections 解析完成后，调用 add_conditional_connection
"""

import re
from typing import Any

from openjiuwen.core.workflow.components.flow.branch_comp import BranchComponent

from .base import BaseComponentHandler, BuildContext
from ..types import IR_TYPE_BRANCH


class BranchHandler(BaseComponentHandler):
    """jiuwen.branch 节点处理器"""

    IR_TYPE = IR_TYPE_BRANCH

    def create_component(
        self,
        ir_node: dict,
        context: BuildContext,
    ) -> Any:
        """
        创建 BranchComponent

        Args:
            ir_node: IR 节点配置
            context: 构造上下文

        Returns:
            BranchComponent 组件实例
        """
        return BranchComponent()

    def register(
        self,
        workflow: Any,
        component: Any,
        node_id: str,
        ir_node: dict,
        context: BuildContext,
    ) -> None:
        """
        注册 BranchComponent 到工作流

        分支组件先注册，但暂时不添加 conditional_connection。
        conditional_connection 会在所有 connections 解析完成后添加。

        Args:
            workflow: 工作流实例
            component: BranchComponent 组件
            node_id: 注册到工作流的节点 ID
            ir_node: IR 节点配置
            context: 构造上下文
        """
        # 将分支组件保存到上下文，供后续处理连接时使用
        context.branch_components[node_id] = {
            "component": component,
            "ir_node": ir_node,
        }

        # 先注册到工作流（不添加 conditional_connection）
        workflow.add_workflow_comp(node_id, component)

    def handle_branch_connection(
        self,
        workflow: Any,
        conn: dict,
        context: BuildContext,
    ) -> None:
        """
        处理分支连接

        从 connections 中提取 branch 条件，添加到对应的 BranchComponent。

        Args:
            workflow: 工作流实例
            conn: 连接配置，格式为：
                  {
                      "source": {"componentId": "node_if"},
                      "branchId": "node_if-if",
                      "target": {"componentId": "node_branch_a"}
                  }
            context: 构造上下文
        """
        source_id = conn["source"]["componentId"]
        target_id = conn["target"]["componentId"]
        branch_id = conn.get("source", {}).get("branchId", "")

        if source_id not in context.node_id_map:
            return

        registered_source_id = context.node_id_map[source_id]

        if registered_source_id not in context.branch_components:
            return

        branch_info = context.branch_components[registered_source_id]
        branch_component = branch_info["component"]
        ir_node = branch_info["ir_node"]

        # 从 IR 节点中获取 branch 配置
        configs = ir_node.get("configs", {})
        branches = configs.get("branches", [])

        # 查找对应的 branch 配置
        condition = None
        for branch in branches:
            if branch.get("id") == branch_id:
                bool_expr = branch.get("boolExpression", "")
                if bool_expr:
                    # 转换条件表达式中的节点引用
                    condition = self._convert_condition(bool_expr, context.node_id_map)
                else:
                    # default 分支，条件为 True
                    condition = "true"
                break

        if condition is None:
            # 未找到对应的 branch 配置，默认为 True
            condition = "true"

        # 添加 branch 条件
        branch_component.add_branch(condition, target_id, branch_id=branch_id)

    def _convert_condition(
        self,
        condition: str,
        node_id_map: dict[str, str],
    ) -> str:
        """
        转换条件表达式中的节点引用

        例如: ${node_start.systemFields.query} == '111'
              -> ${start_1.query} == '111'

        Args:
            condition: 原始条件表达式
            node_id_map: 节点 ID 映射表

        Returns:
            str: 转换后的条件表达式
        """
        if not isinstance(condition, str):
            return condition

        def replace_ref(match):
            full_match = match.group(0)
            inner = match.group(1)
            parts = inner.split(".")

            if len(parts) < 2:
                return full_match

            # 转换节点 ID
            source_node = parts[0]
            target_node = node_id_map.get(source_node, source_node)

            # 提取字段名
            if parts[1] == "userFields":
                field_name = parts[2] if len(parts) >= 3 else ""
                if field_name == "raw_output":
                    field_name = "output"
            elif parts[1] == "systemFields":
                field_name = parts[2] if len(parts) >= 3 else ""
            else:
                field_name = parts[1]

            return f"${{{target_node}.{field_name}}}"

        pattern = r"\$\{([^}]+)\}"
        return re.sub(pattern, replace_ref, condition)

    def finalize(
        self,
        workflow: Any,
        context: BuildContext,
    ) -> None:
        """
        在所有 connections 解析完成后调用

        为所有分支组件添加 conditional_connection。

        Args:
            workflow: 工作流实例
            context: 构造上下文
        """
        for node_id, branch_info in context.branch_components.items():
            branch_component = branch_info["component"]
            workflow.add_conditional_connection(node_id, branch_component.router())
