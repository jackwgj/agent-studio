#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
IRWorkflowBuilder 单元测试
"""

from unittest.mock import MagicMock

import pytest

# 测试用的 IR 配置
SAMPLE_IR_JSON = {
    "workflowId": "test-workflow-001",
    "workflowName": "TestWorkflow",
    "workflowVersion": "draft",
    "schemaVersion": "0.6.0",
    "components": [
        {
            "name": "开始",
            "id": "node_start",
            "type": "jiuwen.start",
            "configs": {
                "userFields": {"inputs": [], "outputs": []},
                "systemFields": {
                    "inputs": [
                        {
                            "sourceType": "input",
                            "description": "用户输入",
                            "id": "query",
                            "type": "string",
                            "required": True,
                        }
                    ],
                    "outputs": [],
                },
            },
            "inputs": {"userFields": {}, "systemFields": {"query": "", "sys": {}}},
            "outputs": {"userFields": {}, "systemFields": {"query": "", "sys": {}}},
        },
        {
            "name": "大模型",
            "id": "node_llm",
            "type": "jiuwen.LLMComponent",
            "configs": {
                "stream": True,
                "deployMode": "workflow",
                "userFields": {
                    "inputs": [
                        {
                            "sourceType": "ref",
                            "description": "用户输入",
                            "id": "query",
                            "type": "string",
                            "required": True,
                        }
                    ],
                    "outputs": [
                        {
                            "sourceType": "input",
                            "description": "模型原始输出",
                            "id": "raw_output",
                            "type": "string",
                            "required": True,
                        }
                    ],
                },
                "systemFields": {"inputs": [], "outputs": []},
                "enableHistory": False,
                "templateContent": [
                    {"content": "你是一个助手", "role": "system"},
                    {"content": "{{query}}", "role": "user"},
                ],
                "responseFormat": {"type": "text"},
                "model": {
                    "modelName": "test-model",
                    "extension": {
                        "safetyBarrier": False,
                        "deploymentId": "test",
                        "authId": "test-auth",
                    },
                    "hyperParameters": {
                        "top_p": 0.5,
                        "frequency_penalty": 0,
                        "max_tokens": 4096,
                        "temperature": 0.5,
                    },
                    "modelType": "LLM",
                },
                "historySize": 3,
            },
            "inputs": {"userFields": {"query": "${node_start.systemFields.query}"}},
            "outputs": {"userFields": {"raw_output": ""}},
        },
        {
            "name": "结束",
            "id": "node_end",
            "type": "jiuwen.end",
            "configs": {
                "responseMode": "directResponse",
                "userFields": {
                    "inputs": [
                        {
                            "sourceType": "ref",
                            "description": "最终输出",
                            "id": "result",
                            "type": "string",
                            "required": False,
                        }
                    ],
                    "outputs": [],
                },
                "responseTemplate": "最终结果：{{{result}}}",
                "struct_output_template": '{"answer": "{{_NODE_OUTPUT}}"}',
                "systemFields": {"inputs": [], "outputs": []},
                "isStreamOut": True,
                "enable_history": True,
                "isStructMessage": False,
            },
            "inputs": {"userFields": {"result": "${node_llm.userFields.raw_output}"}},
            "outputs": {"userFields": {}, "responseContent": ""},
        },
    ],
    "connections": [
        {
            "source": {"componentId": "node_start"},
            "target": {"componentId": "node_llm"},
        },
        {"source": {"componentId": "node_llm"}, "target": {"componentId": "node_end"}},
    ],
}


def _make_mock_model_provider():
    """创建 mock ModelConfigProvider"""
    from agent_runtime.ir_runner.interfaces import ModelConfigProvider

    provider = MagicMock(spec=ModelConfigProvider)
    provider.get_llm_config.return_value = MagicMock()
    return provider


class TestIRWorkflowBuilder:
    """测试 IRWorkflowBuilder"""

    def test_validate_ir_valid(self):
        """测试有效的 IR JSON 校验"""
        from agent_runtime.ir_runner.builder import IRWorkflowBuilder

        builder = IRWorkflowBuilder(model_provider=_make_mock_model_provider())
        assert builder._validate_ir(SAMPLE_IR_JSON) is True

    def test_validate_ir_missing_components(self):
        """测试缺少 components 的 IR JSON 校验"""
        from agent_runtime.ir_runner.builder import IRWorkflowBuilder

        builder = IRWorkflowBuilder(model_provider=_make_mock_model_provider())
        invalid_ir = {"connections": []}
        assert builder._validate_ir(invalid_ir) is False

    def test_validate_ir_invalid_type(self):
        """测试非字典类型的 IR JSON 校验"""
        from agent_runtime.ir_runner.builder import IRWorkflowBuilder

        builder = IRWorkflowBuilder(model_provider=_make_mock_model_provider())
        assert builder._validate_ir("not a dict") is False
        assert builder._validate_ir(None) is False

    def test_generate_node_id_map(self):
        """测试节点 ID 映射生成"""
        from agent_runtime.ir_runner.builder import IRWorkflowBuilder

        builder = IRWorkflowBuilder(model_provider=_make_mock_model_provider())
        node_id_map = builder._generate_node_id_map(SAMPLE_IR_JSON["components"])

        assert node_id_map["node_start"] == "node_start"
        assert node_id_map["node_llm"] == "node_llm"
        assert node_id_map["node_end"] == "node_end"

    def test_build_workflow_structure(self):
        """测试工作流结构"""
        from agent_runtime.ir_runner.builder import IRWorkflowBuilder

        builder = IRWorkflowBuilder(model_provider=_make_mock_model_provider())
        workflow = builder.build(SAMPLE_IR_JSON)

        assert workflow is not None
        assert workflow.card.id == "test-workflow-001"
        assert workflow.card.name == "TestWorkflow"

    def test_build_requires_model_provider(self):
        """测试 model_provider 为必填参数"""
        from agent_runtime.ir_runner.builder import IRWorkflowBuilder

        with pytest.raises(TypeError):
            IRWorkflowBuilder()


class TestRefConversion:
    """测试引用格式转换"""

    def test_convert_ir_ref_to_workflow_ref(self):
        """测试 IR 引用格式转换为工作流引用格式"""
        from agent_runtime.ir_runner.components.llm import LLMHandler

        handler = LLMHandler()

        # 测试 userFields 引用转换
        result = handler._convert_ir_ref_to_workflow_ref(
            "${node_llm.userFields.raw_output}"
        )
        assert result == "${node_llm.output}"

        # 测试 systemFields 引用转换
        result = handler._convert_ir_ref_to_workflow_ref(
            "${node_start.systemFields.query}"
        )
        assert result == "${node_start.query}"

    def test_convert_ir_ref_to_workflow_ref_invalid(self):
        """测试无效引用格式转换"""
        from agent_runtime.ir_runner.components.llm import LLMHandler

        handler = LLMHandler()

        # 测试无效格式
        assert handler._convert_ir_ref_to_workflow_ref("not a ref") == "not a ref"
        assert handler._convert_ir_ref_to_workflow_ref("${simple}") == "${simple}"
        assert handler._convert_ir_ref_to_workflow_ref(None) is None

    def test_end_handler_ref_conversion(self):
        """测试 End 节点处理器引用转换"""
        from agent_runtime.ir_runner.components.end import EndHandler

        handler = EndHandler()

        # 测试引用转换
        result = handler._convert_ir_ref_to_workflow_ref(
            "${node_llm.userFields.raw_output}"
        )
        assert result == "${node_llm.raw_output}"


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
