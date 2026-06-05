#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
EI.qa 节点处理器
"""

from typing import Any

from agent_runtime.extension.workflow_node.flow_qa import FlowQA, FlowQAConfig

from .base import BaseComponentHandler, BuildContext


class QAHandler(BaseComponentHandler):
    """EI.qa 节点处理器"""

    IR_TYPE = "EI.qa"

    def create_component(
        self,
        ir_node: dict,
        context: BuildContext,
    ) -> Any:
        """
        创建 FlowQA 组件

        Args:
            ir_node: IR 节点配置
            context: 构造上下文

        Returns:
            FlowQA 组件实例
        """
        configs = ir_node.get("configs", {})

        name = ir_node.get("name", "")
        need_reply = configs.get("needReply", False)
        qa_strategy = configs.get("qaStrategy", "random")
        options = configs.get("options", [])
        struct_output_template = configs.get("struct_output_template", "")
        enable_struct_message = configs.get("isStructMessage", False)
        enable_history = configs.get("enable_history", True)
        index_key = configs.get("index_key", "index")

        config = FlowQAConfig(
            name=name,
            need_reply=need_reply,
            qa_strategy=qa_strategy,
            options=options,
            struct_output_template=struct_output_template,
            enable_struct_message=enable_struct_message,
            enable_history=enable_history,
            index_key=index_key,
        )

        return FlowQA(config)

    def get_inputs_schema(self, ir_node: dict) -> dict:
        """
        获取 FlowQA 组件的输入 schema

        Args:
            ir_node: IR 节点配置

        Returns:
            dict: 输入 schema
        """
        inputs = ir_node.get("inputs", {})
        user_fields = inputs.get("userFields", {})

        converted_inputs = {}
        for key, value in user_fields.items():
            if isinstance(value, str):
                converted_inputs[key] = self.convert_ref_format(value, {})
            else:
                converted_inputs[key] = value

        return converted_inputs
