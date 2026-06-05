#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
jiuwen.message 节点处理器

用于在工作流执行过程中发送中间消息。
"""

from typing import Any, Optional

from .base import BaseComponentHandler, BuildContext
from ..types import IR_TYPE_MESSAGE


class MessageHandler(BaseComponentHandler):
    """jiuwen.message 节点处理器"""

    IR_TYPE = IR_TYPE_MESSAGE

    def create_component(
        self,
        ir_node: dict,
        context: BuildContext,
    ) -> Any:
        configs = ir_node.get("configs", {})

        from agent_runtime.extension.workflow_node.flow_message import FlowMessage

        conf_dict = {
            "name": ir_node.get("name"),
            "template": configs.get("template", ""),
            "struct_output_template": configs.get("struct_output_template", ""),
            "isStructMessage": configs.get("isStructMessage", False),
            "enable_history": configs.get("enable_history", True),
        }

        return FlowMessage(conf=conf_dict)

    def register(
        self,
        workflow: Any,
        component: Any,
        node_id: str,
        ir_node: dict,
        context: BuildContext,
    ) -> None:
        inputs = ir_node.get("inputs", {})
        user_fields = inputs.get("userFields", {})

        converted_inputs = {}
        stream_inputs = {}

        configs = ir_node.get("configs", {})
        is_stream_out = configs.get("isStreamOut", False)
        enable_streaming = bool(is_stream_out)

        for key, value in user_fields.items():
            if isinstance(value, str):
                ref = self._extract_ref(value)
                if ref:
                    source_id = ref.split(".")[0] if "." in ref else ref
                    converted_ref = self.convert_ref_format(value, context.node_id_map)
                    if enable_streaming and source_id in context.node_id_map:
                        stream_inputs[key] = converted_ref
                    else:
                        converted_inputs[key] = converted_ref
                else:
                    converted_inputs[key] = value
            else:
                converted_inputs[key] = value

        if enable_streaming and stream_inputs:
            workflow.add_workflow_comp(
                node_id,
                component,
                inputs_schema=converted_inputs if converted_inputs else None,
                stream_inputs_schema=stream_inputs,
                response_mode="streaming",
            )
            for key, value in stream_inputs.items():
                if (
                    isinstance(value, str)
                    and value.startswith("${")
                    and value.endswith("}")
                ):
                    inner = value[2:-1]
                    source_id = inner.split(".")[0]
                    if source_id in context.node_id_map:
                        workflow.add_stream_connection(source_id, node_id)
        else:
            workflow.add_workflow_comp(
                node_id,
                component,
                inputs_schema=converted_inputs if converted_inputs else None,
            )

    def _extract_ref(self, value: str) -> Optional[str]:
        if not isinstance(value, str):
            return None
        if value.startswith("${") and value.endswith("}"):
            return value[2:-1]
        return None
