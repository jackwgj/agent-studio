#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
jiuwen.LLMComponent 节点处理器
"""

import re
from typing import Any

from openjiuwen.core.foundation.llm import UserMessage, SystemMessage
from openjiuwen.core.workflow.components.llm.llm_comp import LLMComponent, LLMCompConfig

from .base import BaseComponentHandler, BuildContext
from ..types import IR_TYPE_LLM


class LLMHandler(BaseComponentHandler):
    """jiuwen.LLMComponent 节点处理器"""

    IR_TYPE = IR_TYPE_LLM

    def create_component(
        self,
        ir_node: dict,
        context: BuildContext,
    ) -> Any:
        """
        创建 LLM 组件

        Args:
            ir_node: IR 节点配置
            context: 构造上下文

        Returns:
            LLMComponent 组件实例
        """
        configs = ir_node.get("configs", {})

        # 获取模型配置
        llm_config = context.model_provider.get_llm_config(
            ir_node, context.ir_json.get("configs")
        )

        # 处理 template_content
        template_content = self._build_template_content(configs, context)

        # 处理 output_config
        output_config = self._build_output_config(configs)

        # 构建 LLMCompConfig
        # 使用 system_prompt_template 来避免 template_content 的验证
        # 因为 template_content 需要是 dict 格式，而我们用的是 BaseMessage 对象
        config = LLMCompConfig(
            model_id=llm_config.model_id,
            model_client_config=llm_config.model_client_config,
            model_config=llm_config.model_config,
            template_content=[],  # 使用空列表，通过 system_prompt_template 传递
            system_prompt_template=(
                SystemMessage(
                    content="\n".join(
                        msg.content
                        for msg in template_content
                        if isinstance(msg, SystemMessage)
                    )
                )
                if any(isinstance(msg, SystemMessage) for msg in template_content)
                else None
            ),
            user_prompt_template=(
                UserMessage(
                    content="\n".join(
                        msg.content
                        for msg in template_content
                        if isinstance(msg, UserMessage)
                    )
                )
                if any(isinstance(msg, UserMessage) for msg in template_content)
                else None
            ),
            response_format={
                "type": configs.get("responseFormat", {}).get("type", "text")
            },
            output_config=output_config,
            enable_history=configs.get("enableHistory", False),
        )

        return LLMComponent(config)

    def _build_template_content(
        self,
        configs: dict,
        context: BuildContext,
    ) -> list[UserMessage]:
        """
        构建 template_content

        Args:
            configs: IR 节点的 configs
            context: 构造上下文

        Returns:
            list[UserMessage]: 消息列表
        """
        raw_templates = configs.get("templateContent", [])

        converted_templates = []
        for msg in raw_templates:
            content = msg.get("content", "")

            # 转换引用格式
            if isinstance(content, str):
                content = self._convert_template_refs(content, context.node_id_map)

            role = msg.get("role", "user")
            if role == "system":
                converted_templates.append(SystemMessage(content=content))
            else:
                converted_templates.append(UserMessage(content=content))

        return converted_templates

    def _convert_template_refs(
        self,
        template: str,
        node_id_map: dict[str, str],
    ) -> str:
        """
        转换模板中的引用

        例如: "{{query}}" -> "${start_1.query}"

        注意：PromptTemplate 使用 {{var}} 格式作为 placeholder，
        但 agent-core 的引用格式是 ${var.field}，
        所以需要将 IR 中的节点引用转换为 ${var.field} 格式。
        """

        # 转换 ${node.field} 格式的引用
        def replace_ref(match):
            inner = match.group(1)
            parts = inner.split(".")

            if len(parts) < 2:
                # 简单变量名，保持 {{var}} 格式
                return f"{{{{{inner}}}}}"

            # 路径格式，转换为 $${node.field}
            source_node = parts[0]
            target_node = node_id_map.get(source_node, source_node)
            field_name = parts[1]

            return f"${{{target_node}.{field_name}}}"

        # 先处理 IR 中的 ${node.field} 引用
        pattern = r"\$\{([^}]+)\}"
        template = re.sub(pattern, replace_ref, template)

        # 再处理 {{var}} 格式（这些是简单变量，保留格式）
        # 不需要转换，保持原样

        return template

    def _build_output_config(self, configs: dict) -> dict:
        """
        构建 output_config

        Args:
            configs: IR 节点的 configs

        Returns:
            dict: 输出配置
        """
        outputs = configs.get("userFields", {}).get("outputs", [])
        output_config = {}

        for output in outputs:
            field_id = output.get("id", "")
            if field_id:
                # raw_output -> output 映射
                if field_id == "raw_output":
                    field_id = "output"
                output_config[field_id] = {
                    "type": output.get("type", "string"),
                    "description": output.get("description", ""),
                    "required": output.get("required", False),
                }

        return output_config

    def get_inputs_schema(self, ir_node: dict) -> dict:
        """
        获取 LLM 组件的输入 schema

        Args:
            ir_node: IR 节点配置

        Returns:
            dict: 输入 schema
        """
        inputs = ir_node.get("inputs", {})
        user_fields = inputs.get("userFields", {})

        # 转换引用格式
        converted_inputs = {}
        for key, value in user_fields.items():
            if isinstance(value, str):
                converted_inputs[key] = self.convert_ref_format(value, {})
            else:
                converted_inputs[key] = value

        return converted_inputs
