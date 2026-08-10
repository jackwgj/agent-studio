# -*- coding: UTF-8 -*-
"""ReActAgent 配置构建 —— 监督者与子 Agent 共用。

⚠️ 语义标注（避免后续误解）：
- 本模块的入参叫 `model_deployment_id`（部署 id），不是模型名。这是 D0-8 实证结论：
  路由(MODEL_ROUTER_API=31113) 接受"部署 id"并解析成真实模型名后调 LLM；传模型名（如
  `deepseek-v4-flash`）反而报"模型策略信息为空"。
- openjiuwen 原生 `ReActAgentConfig.model_name` / `ModelRequestConfig.model_name` 是 SDK 对
  "发给 LLM 接口的 model 字符串"的通用命名（直接连 LLM 时它确实是模型名）。在我们的路由
  架构里，这个字段装的是**部署 id**，由路由解析。因此我们自己的入参语义用
  `model_deployment_id`，与 SDK 的 `model_name` 字段名不同属正常。
"""

from openjiuwen.core.foundation.llm import ModelClientConfig, ModelRequestConfig
from openjiuwen.core.single_agent.agents.react_agent import ReActAgentConfig

from agent_runtime.common.config import settings


def build_react_config(
    system_prompt: str, model_deployment_id: str
) -> ReActAgentConfig:
    """构建 ReActAgentConfig（复用新路模型配置：MODEL_ROUTER_API）。

    Args:
        system_prompt: 系统提示词
        model_deployment_id: 模型部署 id（非模型名，D0-8）。由路由解析成真实模型名后调 LLM。
    """
    model_client_config = ModelClientConfig(
        client_provider="openai",
        api_key=settings.llm.api_key,
        api_base=settings.llm.api_base,  # MODEL_ROUTER_API -> http://127.0.0.1:31113/v1/agent-builder
        timeout=settings.llm.timeout,
    )
    model_request_config = ModelRequestConfig(
        model_name=model_deployment_id,
        temperature=0.7,
        top_p=1.0,
    )
    return ReActAgentConfig(
        model_name=model_deployment_id,
        model_provider="openai",
        api_key=settings.llm.api_key,
        api_base=settings.llm.api_base,
        max_iterations=5,
        prompt_template=[{"role": "system", "content": system_prompt}]
        if system_prompt
        else [],
        model_client_config=model_client_config,
        model_config_obj=model_request_config,
    )
