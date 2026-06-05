#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""IRModelConfigProvider (model_providers 副本) thinking → extra_body 单元测试"""

from unittest.mock import MagicMock, patch

import pytest
from agent_runtime.common.model_providers import IRModelConfigProvider


def _make_ir_node(thinking_config):
    """构建带 thinking 配置的 IR 节点"""
    hyper_params = {}
    if thinking_config is not None:
        hyper_params["thinking"] = thinking_config

    return {
        "id": "node_llm",
        "type": "jiuwen.LLMComponent",
        "configs": {
            "model": {
                "modelName": "test-model",
                "modelType": "LLM",
                "hyperParameters": hyper_params,
                "extension": {"authId": "test_auth"},
            },
        },
    }


class TestThinkingEnabled:
    """thinking.type = 'enabled' → extra_body 传递"""

    def test_thinking_enabled(self):
        ir_node = _make_ir_node({"type": "enabled"})
        provider = IRModelConfigProvider()

        with patch("agent_runtime.common.model_providers._request_ctx") as mock_ctx:
            mock_ctx_instance = MagicMock()
            mock_ctx_instance.headers = {"X-Auth-Token": "test_token"}
            mock_ctx.get.return_value = mock_ctx_instance

            result = provider.get_llm_config(ir_node)

        extra_body = getattr(result.model_config, "extra_body", None)
        assert extra_body is not None
        assert extra_body == {"thinking": {"type": "enabled"}}


class TestThinkingAbsent:
    """不配置 thinking → extra_body 为 None"""

    def test_no_thinking(self):
        ir_node = _make_ir_node(None)
        provider = IRModelConfigProvider()

        with patch("agent_runtime.common.model_providers._request_ctx") as mock_ctx:
            mock_ctx_instance = MagicMock()
            mock_ctx_instance.headers = {"X-Auth-Token": "test_token"}
            mock_ctx.get.return_value = mock_ctx_instance

            result = provider.get_llm_config(ir_node)

        extra_body = getattr(result.model_config, "extra_body", None)
        assert extra_body is None


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
