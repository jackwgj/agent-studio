#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
模型配置提供者 — 部署级别的模型配置实现

提供可插拔的模型配置来源：
1. EnvVarModelConfigProvider - 从环境变量读取
2. OBSModelConfigProvider - 从 OBS 模型网关读取（后续开发）
3. IRModelConfigProvider - 从 IR 配置读取，环境变量作为默认值兜底（推荐用于商用九问 IR 兼容）
"""

from typing import Optional

from agent_runtime.common.config import settings
from agent_runtime.common.ir_interfaces import ModelConfigProvider
from agent_runtime.context.request_context import _request_ctx
from openjiuwen.core.foundation.llm import ModelClientConfig, ModelRequestConfig
from openjiuwen.core.workflow.components.llm.llm_comp import LLMCompConfig


class EnvVarModelConfigProvider(ModelConfigProvider):
    """从环境变量读取模型配置"""

    def get_llm_config(
        self,
        ir_node: dict,
        global_config: Optional[dict] = None,
    ) -> LLMCompConfig:
        """Get LLM configuration purely from settings/environment variables."""
        base = settings.llm

        # IR override for hyperparameters only
        configs = ir_node.get("configs", {})
        hyper_params = configs.get("hyperParameters", {})

        temperature = hyper_params.get("temperature", base.temperature)
        top_p = hyper_params.get("top_p", base.top_p)

        model_client_config = ModelClientConfig(
            client_provider="openai",
            api_key=base.api_key,
            api_base=base.api_base,
            timeout=base.timeout,
            verify_ssl=base.ssl_verify,
        )

        model_request_config = ModelRequestConfig(
            model=base.model_name,
            temperature=temperature,
            top_p=top_p,
        )

        return LLMCompConfig(
            model_client_config=model_client_config,
            model_config=model_request_config,
            cache_stream=True,
        )


class OBSModelConfigProvider(ModelConfigProvider):
    """从 OBS 模型网关读取模型配置（后续开发）"""

    def get_llm_config(
        self,
        ir_node: dict,
        global_config: Optional[dict] = None,
    ) -> LLMCompConfig:
        raise NotImplementedError("OBSModelConfigProvider 尚未实现")


class IRModelConfigProvider(ModelConfigProvider):
    """从 IR 配置读取模型参数，环境变量作为默认值兜底

    适用于商用九问 IR 文件兼容场景：
    - 从 IR 节点的 configs.model/modelConfig 读取模型配置
    - authId 从 IR 配置获取，authToken 从 HTTP 请求头透传
    - 环境变量作为兜底默认值
    """

    def get_llm_config(
        self,
        ir_node: dict,
        global_config: Optional[dict] = None,
    ) -> LLMCompConfig:
        """Get LLM configuration from settings with IR override.

        Priority: IR node config > environment variables (via settings) > defaults
        """
        # Extract IR model config
        configs = ir_node.get("configs", {})
        model_config = configs.get("modelConfig") or configs.get("model", {})
        hyper_params = model_config.get("hyperParameters", {})

        # Get base values from settings (environment variables auto-injected)
        base = settings.llm

        # Apply IR override with settings fallback
        model_name = (
            model_config.get("modelName")
            or model_config.get("model_name")
            or base.model_name
        )
        auth_id = model_config.get("extension", {}).get("authId", "")

        temperature = hyper_params.get("temperature", base.temperature)
        top_p = hyper_params.get("top_p", base.top_p)

        # Build auth headers
        ctx = _request_ctx.get()
        auth_token = ctx.headers.get("X-Auth-Token", "") if ctx else ""
        custom_headers = {"X-Auth-Id": auth_id, "X-Auth-Token": auth_token}

        # Build client config
        model_client_config = ModelClientConfig(
            client_provider="openai",
            api_key=base.api_key,
            api_base=base.api_base,
            timeout=base.timeout,
            verify_ssl=base.ssl_verify,
            custom_headers=custom_headers,
        )

        extra_body = None
        thinking = hyper_params.get("thinking")
        if thinking and isinstance(thinking, dict) and "type" in thinking:
            extra_body = {"thinking": thinking}

        model_request_config = ModelRequestConfig(
            model=model_name,
            temperature=temperature,
            top_p=top_p,
            extra_body=extra_body,
        )

        return LLMCompConfig(
            model_client_config=model_client_config,
            model_config=model_request_config,
            cache_stream=True,
        )
