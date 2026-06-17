#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
模型配置提供者 — 部署级别的模型配置实现

提供可插拔的模型配置来源：
1. EnvVarModelConfigProvider - 从环境变量读取
2. OBSModelConfigProvider - 从 OBS 模型网关读取（后续开发）
3. IRModelConfigProvider - 从 IR 配置读取，环境变量作为默认值兜底（推荐用于商用九问 IR 兼容）
4. Nl2ModelConfigProvider - 从 NL2 Agent 的 modelInfo 读取模型配置，用于 nl_to_agent 模块调用模型
"""

from typing import Optional

from agent_runtime.common.config import settings
from agent_runtime.common.ir_interfaces import ModelConfigProvider
from agent_runtime.context.request_context import _request_ctx
from openjiuwen.core.foundation.llm import Model, ModelClientConfig, ModelRequestConfig
from openjiuwen.core.workflow.components.llm.llm_comp import LLMCompConfig


def _extract_auth_headers(headers: dict) -> dict:
    """从 headers 中提取九问平台认证头

    支持两种 key 格式：
    - 下划线格式（modelInfo 原始格式）: auth_id, x_auth_token, deployment_id
    - HTTP 头格式（Flask request.headers 合并后）: X-Auth-Id, X-Auth-Token, X-Deployment-Id

    Returns:
        dict: 仅包含 X-Auth-Id, X-Auth-Token, X-Deployment-Id 的认证头
    """
    if not headers:
        return {}

    custom_headers = {}

    # auth_id
    auth_id = (
        headers.get("auth_id", "")
        or headers.get("Auth_id", "")
        or headers.get("X-Auth-Id", "")
        or headers.get("x-auth-id", "")
    )
    if auth_id:
        custom_headers["X-Auth-Id"] = auth_id

    # x_auth_token
    x_auth_token = (
        headers.get("x_auth_token", "")
        or headers.get("X_auth_token", "")
        or headers.get("X-Auth-Token", "")
        or headers.get("x-auth-token", "")
    )
    if x_auth_token:
        custom_headers["X-Auth-Token"] = x_auth_token

    # deployment_id
    deployment_id = (
        headers.get("deployment_id", "")
        or headers.get("Deployment_id", "")
        or headers.get("X-Deployment-Id", "")
        or headers.get("x-deployment-id", "")
    )
    if deployment_id:
        custom_headers["X-Deployment-Id"] = deployment_id

    return custom_headers


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


class Nl2ModelConfigProvider:
    """从 NL2 Agent 的 modelInfo 读取模型配置

    适用于 nl_to_agent 模块调用模型的场景：
    - 从前端传入的 modelInfo（LLMModelInfo）中提取模型名称、认证信息等
    - 环境变量作为 api_key、api_base 等连接参数的兜底默认值
    - authId 和 authToken 从 modelInfo.headers 和 HTTP 请求头透传
    """

    @staticmethod
    def get_llm_config(model_info) -> LLMCompConfig:
        """从 modelInfo 构建 LLM 组件配置

        Args:
            model_info: LLMModelInfo 实例，包含 model、model_source、headers 等字段

        Returns:
            LLMCompConfig: LLM 组件配置
        """
        base = settings.llm

        headers = model_info.headers or {}
        # 模型路由服务需要用 deploymentId（UUID）作为 model 参数
        model_name = model_info.model or base.model_name
        auth_id = headers.get("auth_id", "")
        x_auth_token = headers.get("x_auth_token", "")

        ctx = _request_ctx.get()
        if ctx and ctx.headers:
            if not x_auth_token:
                x_auth_token = ctx.headers.get("x-auth-token", "") or ctx.headers.get("X-Auth-Token", "")

        custom_headers = {}
        if auth_id:
            custom_headers["X-Auth-Id"] = auth_id
        if x_auth_token:
            custom_headers["X-Auth-Token"] = x_auth_token
        deployment_id = headers.get("deployment_id", "")
        if deployment_id:
            custom_headers["X-Deployment-Id"] = deployment_id

        model_client_config = ModelClientConfig(
            client_provider="openai",
            api_key=base.api_key,
            api_base=base.api_base,
            timeout=base.timeout,
            verify_ssl=base.ssl_verify,
            custom_headers=custom_headers,
        )

        model_request_config = ModelRequestConfig(
            model=model_name,
            temperature=model_info.temperature if model_info.temperature is not None else base.temperature,
            top_p=model_info.top_p if model_info.top_p is not None else base.top_p,
        )

        return LLMCompConfig(
            model_client_config=model_client_config,
            model_config=model_request_config,
            cache_stream=True,
        )


def get_nl2_model(model_info) -> Model:
    """工厂方法：根据 modelInfo 创建 openjiuwen Model 实例

    用于 nl_to_agent 模块中 Nl2AgentProcessor 获取模型实例，
    替代原来通过 LLMServiceManager -> ModelFactory -> Nl2AgentBuilderModel 的调用链。

    Args:
        model_info: LLMModelInfo 实例，包含 model、model_source、headers 等字段

    Returns:
        Model: openjiuwen 的 Model 实例，支持 invoke/stream 等调用方式
    """
    llm_config = Nl2ModelConfigProvider.get_llm_config(model_info)
    return Model(
        model_client_config=llm_config.model_client_config,
        model_config=llm_config.model_config,
    )


class PromptOptimizeModelProvider:
    """提示词优化专用的模型配置提供者

    适用于 agent_builder 的 prompt 管理特性（prompt.py / mmapo.py）。
    配置逻辑与 Nl2ModelConfigProvider 完全一致。
    """

    @staticmethod
    def get_llm_config(model_info) -> LLMCompConfig:
        """从 modelInfo 构建提示词优化专用的 LLM 组件配置

        逻辑完全照抄 Nl2ModelConfigProvider.get_llm_config()，
        唯一区别是认证头提取使用 _extract_auth_headers() 兼容 HTTP 头格式
        （因为 prompt 场景的 headers 由 Flask request.headers 合并，key 为 X-Auth-Token 格式，
        而 Nl2 的 headers 由服务端拼装，key 为 x_auth_token 下划线格式）。

        Args:
            model_info: LLMModelInfo 实例

        Returns:
            LLMCompConfig: LLM 组件配置
        """
        base = settings.llm

        headers = model_info.headers or {}
        model_name = model_info.model or base.model_name

        # 认证头提取：兼容下划线格式（Java body）和 HTTP 头格式（Flask request.headers）
        custom_headers = _extract_auth_headers(headers)

        # 兜底：从 _request_ctx 补全 x_auth_token（与 Nl2 一致）
        if not custom_headers.get("X-Auth-Token"):
            try:
                ctx = _request_ctx.get()
                if ctx and ctx.headers:
                    x_auth_token = ctx.headers.get("x-auth-token", "") or ctx.headers.get("X-Auth-Token", "")
                    if x_auth_token:
                        custom_headers["X-Auth-Token"] = x_auth_token
            except Exception:
                pass

        model_client_config = ModelClientConfig(
            client_provider="openai",
            api_key=base.api_key,
            api_base=base.api_base,
            timeout=base.timeout,
            verify_ssl=base.ssl_verify,
            custom_headers=custom_headers,
        )

        model_request_config = ModelRequestConfig(
            model=model_name,
            temperature=model_info.temperature if model_info.temperature is not None else base.temperature,
            top_p=model_info.top_p if model_info.top_p is not None else base.top_p,
        )

        return LLMCompConfig(
            model_client_config=model_client_config,
            model_config=model_request_config,
            cache_stream=True,
        )


def get_prompt_optimize_model(model_info) -> Model:
    """工厂方法：为提示词优化特性创建 openjiuwen Model 实例

    用于 agent_builder 的 prompt 管理模块（prompt.py / mmapo.py）获取模型实例，
    替代原来通过 LLMServiceManager -> EiCloudLLMService -> OpenAICompatibleService 的调用链。

    Args:
        model_info: LLMModelInfo 实例（agent_builder.prompt.common.config.LLMModelInfo），
                    包含 model、headers、temperature、top_p 等字段

    Returns:
        Model: openjiuwen 的 Model 实例，支持 invoke/stream 等调用方式
    """
    llm_config = PromptOptimizeModelProvider.get_llm_config(model_info)
    return Model(
        model_client_config=llm_config.model_client_config,
        model_config=llm_config.model_config,
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
