#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
组件处理器单元测试
"""

from unittest.mock import MagicMock

import pytest

SAMPLE_LLM_NODE = {
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
}


SAMPLE_END_NODE = {
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
}


SAMPLE_START_NODE = {
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
}


class TestLLMHandler:
    """测试 LLMHandler"""

    def test_get_inputs_schema(self):
        """测试获取 LLM 组件输入 schema"""
        from agent_runtime.ir_runner.components.llm import LLMHandler

        handler = LLMHandler()
        schema = handler.get_inputs_schema(SAMPLE_LLM_NODE)

        # 验证输入 schema
        assert "query" in schema
        assert schema["query"] == "${node_start.query}"

    def test_convert_ir_ref_to_workflow_ref_user_fields(self):
        """测试转换 userFields 引用"""
        from agent_runtime.ir_runner.components.llm import LLMHandler

        handler = LLMHandler()
        result = handler.convert_ref_format(
            "${node_llm.userFields.raw_output}", {}
        )
        assert result == "${node_llm.output}"

    def test_convert_ir_ref_to_workflow_ref_system_fields(self):
        """测试转换 systemFields 引用"""
        from agent_runtime.ir_runner.components.llm import LLMHandler

        handler = LLMHandler()
        result = handler.convert_ref_format(
            "${node_start.systemFields.query}", {}
        )
        assert result == "${node_start.query}"

    def test_convert_ir_ref_to_workflow_ref_simple(self):
        """测试简单引用（不需要转换）"""
        from agent_runtime.ir_runner.components.llm import LLMHandler

        handler = LLMHandler()

        # 已经是工作流格式的引用
        result = handler.convert_ref_format("${node.output}", {})
        assert result == "${node.output}"

    def test_convert_ir_ref_to_workflow_ref_invalid(self):
        """测试无效引用格式"""
        from agent_runtime.ir_runner.components.llm import LLMHandler

        handler = LLMHandler()

        assert handler.convert_ref_format("invalid", {}) == "invalid"
        assert handler.convert_ref_format("${simple}", {}) == "${simple}"
        assert handler.convert_ref_format(None, {}) is None


class TestEndHandler:
    """测试 EndHandler"""

    def test_get_inputs_schema(self):
        """测试获取 End 节点输入 schema"""
        from agent_runtime.ir_runner.components.end import EndHandler

        handler = EndHandler()
        schema = handler.get_inputs_schema(SAMPLE_END_NODE)

        # 验证输入 schema
        assert "result" in schema
        assert schema["result"] == "${node_llm.raw_output}"

    def test_convert_ir_ref_to_workflow_ref(self):
        """测试转换 End 节点引用"""
        from agent_runtime.ir_runner.components.end import EndHandler

        handler = EndHandler()
        result = handler.convert_ref_format(
            "${node_llm.userFields.raw_output}", {}
        )
        assert result == "${node_llm.raw_output}"

    def test_register_streaming_mode(self):
        """测试注册流式输出模式的 End 节点"""
        from agent_runtime.ir_runner.components.end import EndHandler
        from unittest.mock import MagicMock  # noqa: redefined-outer-name

        handler = EndHandler()

        # 创建 mock
        mock_workflow = MagicMock()
        mock_component = MagicMock()
        mock_context = MagicMock()

        # 注册组件
        handler.register(
            mock_workflow, mock_component, "node_end", SAMPLE_END_NODE, mock_context
        )

        # 验证 set_end_comp 被调用，且 isStreamOut=True 时使用 stream_inputs_schema
        mock_workflow.set_end_comp.assert_called_once()
        call_args = mock_workflow.set_end_comp.call_args
        # set_end_comp 的位置参数是 (node_id, component, ...)
        assert call_args[0][0] == "node_end"
        call_kwargs = call_args[1]
        assert call_kwargs["stream_inputs_schema"] is not None
        assert call_kwargs["response_mode"] == "streaming"


class TestStartHandler:
    """测试 StartHandler"""

    def test_create_component(self):
        """测试创建 Start 组件"""
        from agent_runtime.ir_runner.components.start import StartHandler
        from openjiuwen.core.workflow import Start

        handler = StartHandler()
        component = handler.create_component(SAMPLE_START_NODE, MagicMock())

        assert isinstance(component, Start)

    def test_register(self):
        """测试注册 Start 组件"""
        from agent_runtime.ir_runner.components.start import StartHandler
        from unittest.mock import MagicMock  # noqa: redefined-outer-name

        handler = StartHandler()

        # 创建 mock
        mock_workflow = MagicMock()
        mock_component = MagicMock()
        mock_context = MagicMock()

        # 注册组件
        handler.register(
            mock_workflow, mock_component, "node_start", SAMPLE_START_NODE, mock_context
        )

        # 验证 set_start_comp 被调用
        mock_workflow.set_start_comp.assert_called_once()
        call_args = mock_workflow.set_start_comp.call_args
        # set_start_comp 的位置参数是 (start_comp_id, component, ...)
        assert call_args[0][0] == "node_start"
        call_kwargs = call_args[1]
        assert call_kwargs["inputs_schema"] is not None


class TestRegistry:
    """测试组件注册表"""

    def test_get_handler_for_llm(self):
        """测试获取 LLM 组件处理器"""
        from agent_runtime.ir_runner.registry import get_handler

        handler = get_handler("jiuwen.LLMComponent")
        assert handler is not None
        assert handler.IR_TYPE == "jiuwen.LLMComponent"

    def test_get_handler_for_start(self):
        """测试获取 Start 组件处理器"""
        from agent_runtime.ir_runner.registry import get_handler

        handler = get_handler("jiuwen.start")
        assert handler is not None
        assert handler.IR_TYPE == "jiuwen.start"

    def test_get_handler_for_end(self):
        """测试获取 End 组件处理器"""
        from agent_runtime.ir_runner.registry import get_handler

        handler = get_handler("jiuwen.end")
        assert handler is not None
        assert handler.IR_TYPE == "jiuwen.end"

    def test_get_handler_unknown(self):
        """测试获取未知组件处理器"""
        from agent_runtime.ir_runner.registry import get_handler

        handler = get_handler("unknown.type")
        assert handler is None


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
