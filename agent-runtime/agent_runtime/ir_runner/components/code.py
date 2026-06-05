#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
jiuwen.code 节点处理器
"""

from typing import Any

from agent_runtime.extension.workflow_node.flow_code import FlowCode

from .base import BaseComponentHandler, BuildContext
from ..types import IR_TYPE_CODE


class CodeHandler(BaseComponentHandler):
    """jiuwen.code 节点处理器"""

    IR_TYPE = IR_TYPE_CODE

    def create_component(
        self,
        ir_node: dict,
        context: BuildContext,
    ) -> Any:
        configs = ir_node.get("configs", {})
        conf = {
            "code": configs.get("code", ""),
            "userFields": configs.get("userFields", {}),
        }
        return FlowCode(conf)

    def get_inputs_schema(self, ir_node: dict) -> dict:
        inputs = ir_node.get("inputs", {})
        user_fields = inputs.get("userFields", {})
        converted_inputs = {}
        for key, value in user_fields.items():
            if isinstance(value, str):
                converted_inputs[key] = self.convert_ref_format(value, {})
            else:
                converted_inputs[key] = value
        return converted_inputs
