#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
节点处理器基类

所有节点处理器需继承 BaseComponentHandler，实现统一接口。
新增节点类型只需添加新的 handler 文件并注册到 registry。
"""

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Any, Optional

from openjiuwen.core.workflow import Workflow as InvokableWorkflow


@dataclass
class BuildContext:
    """构造工作流的上下文"""

    ir_json: dict = field(default_factory=dict)
    workflow: Optional[InvokableWorkflow] = None
    node_id_map: dict[str, str] = field(default_factory=dict)
    branch_components: dict[str, Any] = field(default_factory=dict)
    model_provider: Any = None
    config: dict = field(default_factory=dict)


class BaseComponentHandler(ABC):
    """节点处理器基类"""

    # IR 中的节点类型标识（子类覆盖）
    IR_TYPE: str = ""

    def parse_config(self, ir_node: dict) -> dict:
        """
        解析 IR 节点的 configs，转为 handler 内部配置格式

        Args:
            ir_node: IR 节点配置

        Returns:
            dict: 解析后的配置
        """
        return ir_node.get("configs", {})

    def get_inputs_schema(self, ir_node: dict) -> dict:
        """
        获取组件的输入 schema（引用表达式）

        Args:
            ir_node: IR 节点配置

        Returns:
            dict: 输入 schema，格式为 {input_name: "${node_id.field}"}
        """
        inputs = ir_node.get("inputs", {})
        user_fields = inputs.get("userFields", {})
        return user_fields

    def get_outputs_schema(self, ir_node: dict) -> dict:
        """
        获取组件的输出 schema

        Args:
            ir_node: IR 节点配置

        Returns:
            dict: 输出 schema
        """
        outputs = ir_node.get("outputs", {})
        user_fields = outputs.get("userFields", {})
        return user_fields

    def convert_ref_format(self, ref: str, node_id_map: dict[str, str]) -> str:
        """
        转换引用格式

        商用格式: ${node_start.systemFields.query}
        目标格式: ${start_1.query}

        Args:
            ref: 原始引用字符串
            node_id_map: 节点 ID 映射表

        Returns:
            str: 转换后的引用字符串
        """
        if not isinstance(ref, str):
            return ref

        if not ref.startswith("${") or not ref.endswith("}"):
            return ref

        inner = ref[2:-1]
        parts = inner.split(".")

        if len(parts) < 2:
            return ref

        # 转换节点 ID
        source_node = parts[0]
        target_node = node_id_map.get(source_node, source_node)

        # 提取字段名
        if parts[1] == "userFields":
            field_name = parts[2] if len(parts) >= 3 else ""
            # 处理 raw_output -> output 的映射
            if field_name == "raw_output":
                field_name = "output"
        elif parts[1] == "systemFields":
            field_name = parts[2] if len(parts) >= 3 else ""
        else:
            field_name = parts[1]

        return f"${{{target_node}.{field_name}}}"

    @abstractmethod
    def create_component(
        self,
        ir_node: dict,
        context: BuildContext,
    ) -> Any:
        """
        实例化对应的 agent-core 组件

        Args:
            ir_node: IR 节点配置
            context: 构造上下文

        Returns:
            agent-core 组件实例
        """
        pass

    def register(
        self,
        workflow: InvokableWorkflow,
        component: Any,
        node_id: str,
        ir_node: dict,
        context: BuildContext,
    ) -> None:
        """
        将组件注册到工作流

        默认实现调用 workflow.add_workflow_comp，子类可覆盖

        Args:
            workflow: 工作流实例
            component: 组件实例
            node_id: 注册到工作流的节点 ID
            ir_node: IR 节点配置
            context: 构造上下文
        """
        inputs_schema = self.get_inputs_schema(ir_node)
        outputs_schema = self.get_outputs_schema(ir_node)
        workflow.add_workflow_comp(
            node_id,
            component,
            inputs_schema=inputs_schema,
            outputs_schema=outputs_schema,
        )
