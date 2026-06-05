from dataclasses import dataclass, field, asdict
from typing import Dict, Any, List

from jiuwen.common.log.base import logger


# 1. 目标数据结构定义 (DTOs - Data Transfer Objects)


@dataclass
class OpenJiuwenLLMExtension:
    extra_headers: Dict[str, str] = field(default_factory=dict)


@dataclass
class OpenJiuwenLLMGeneral:
    model_name: str
    model_type: str
    base_url: str
    hyper_parameters: Dict[str, Any]
    api_key: bytearray
    extension: OpenJiuwenLLMExtension


@dataclass
class OpenJiuwenLLMConfig:
    general: OpenJiuwenLLMGeneral


@dataclass
class WebSearchEngineConfig:
    search_engine_name: str
    search_url: str
    max_web_search_results: int
    search_api_key: bytearray
    extension: Dict[str, Any] = field(default_factory=dict)


@dataclass
class LocalSearchEngineConfig:
    search_engine_name: str
    search_url: str
    recall_threshold: float
    search_datasets: List[str]
    search_api_key: bytearray


@dataclass
class OpenJiuwenAgentConfigDTO:
    """目标配置主结构"""

    conversation_id: str
    template_path: str
    execute_mode: str
    workflow_human_in_the_loop: bool
    outliner_max_section_num: int
    source_tracer_research_trace_source_switch: bool
    llm_config: OpenJiuwenLLMConfig
    info_collector_search_method: str
    web_search_engine_config: WebSearchEngineConfig
    local_search_engine_config: LocalSearchEngineConfig
    custom_web_search_config: Dict[str, str]
    custom_local_search_config: Dict[str, str]
    is_template: bool
    has_template: bool
    execution_method: str
    outline_interaction_enabled: bool


# 2. 适配器实现 (Adapter)


class DeepResearchConfigAdapter:
    """
    将原始 DeepResearchAgentConfig 字典转换为 OpenJiuwen 配置字典的适配器
    """

    @classmethod
    def _parse_llm_config(cls, raw_llm: Dict[str, Any]) -> OpenJiuwenLLMConfig:
        """解析并转换 LLM 配置，处理 Header 映射和 URL 裁剪"""
        ext = raw_llm.get("extension", {})
        hyper_params = raw_llm.get("hyper_parameters", {})

        # 安全解析原始 api_key，用于塞入 Header
        raw_api_key_bytes = raw_llm.get("api_key", bytearray())
        auth_token = (
            raw_api_key_bytes.decode("utf-8")
            if isinstance(raw_api_key_bytes, bytearray)
            else str(raw_api_key_bytes)
        )

        # 组装目标 Header
        extra_headers = {
            "safety-barrier": str(ext.get("safetyBarrier", False)).lower(),
            "X-Auth-Id": ext.get("authId", ""),
            "X-Auth-Token": auth_token,
            "cache-control": "no-cache",
        }

        # 处理 Base URL (裁剪掉多余的路径)
        base_url = raw_llm.get("base_url", "")
        if "/chat/completions" in base_url:
            base_url = base_url.split("/chat/completions")[0]

        general = OpenJiuwenLLMGeneral(
            model_name=raw_llm.get("model_name", ""),
            model_type=raw_llm.get("model_type", ""),
            base_url=base_url,
            hyper_parameters=hyper_params,
            api_key=bytearray(
                b"SEE_X_AUTH_TOKEN"
            ),  # 无用字段，但openJiuwenCore要求非空
            extension=OpenJiuwenLLMExtension(extra_headers=extra_headers),
        )
        return OpenJiuwenLLMConfig(general=general)

    @classmethod
    def _parse_web_search_config(cls, raw_web: Dict[str, Any]) -> WebSearchEngineConfig:
        """解析并转换 Web 搜索配置，处理 Token 前缀"""
        raw_key = raw_web.get("search_api_key", bytearray())
        key_str = (
            raw_key.decode("utf-8") if isinstance(raw_key, bytearray) else str(raw_key)
        )

        # 容错处理：剥离可能的 "Bearer " 前缀
        auth_prefix = "Bearer "
        if key_str.startswith(auth_prefix):
            key_str = key_str[len(auth_prefix) :]

        return WebSearchEngineConfig(
            search_engine_name=raw_web.get("search_engine_name", "petal"),
            search_url=raw_web.get("search_url", ""),
            max_web_search_results=raw_web.get("max_web_search_results", 5),
            search_api_key=bytearray(key_str, encoding="utf-8"),
            extension=raw_web.get("extension", {}),
        )

    @classmethod
    def adapt(cls, source_config: Dict[str, Any]) -> Dict[str, Any]:
        """
        核心适配方法：输入原始字典，输出目标字典
        """
        try:
            # 解析嵌套的子配置
            llm_config = cls._parse_llm_config(source_config.get("llm_config", {}))
            web_search_config = cls._parse_web_search_config(
                source_config.get("web_search_engine_config", {})
            )

            # 基础本地搜索配置透传 (如果有额外业务逻辑可以像 web search 一样抽离方法)
            raw_local = source_config.get("local_search_engine_config", {})
            local_search_config = LocalSearchEngineConfig(
                search_engine_name=raw_local.get("search_engine_name", ""),
                search_url=raw_local.get("search_url", ""),
                recall_threshold=raw_local.get("recall_threshold", 0.5),
                search_datasets=raw_local.get("search_datasets", []),
                search_api_key=raw_local.get("search_api_key", bytearray()),
            )

            # 组装最终的目标 DTO
            target_dto = OpenJiuwenAgentConfigDTO(
                conversation_id=source_config.get("conversation_id", ""),
                template_path=source_config.get("template_path", ""),
                execute_mode=source_config.get("execute_mode", "commercial"),
                workflow_human_in_the_loop=source_config.get(
                    "workflow_human_in_the_loop", True
                ),
                # 业务规则：强制覆写或提供默认值
                outliner_max_section_num=source_config.get(
                    "outliner_max_section_num", 5
                ),
                source_tracer_research_trace_source_switch=source_config.get(
                    "source_tracer_research_trace_source_switch", True
                ),
                llm_config=llm_config,
                info_collector_search_method=source_config.get(
                    "info_collector_search_method", "web"
                ),
                web_search_engine_config=web_search_config,
                local_search_engine_config=local_search_config,
                custom_web_search_config=source_config.get(
                    "custom_web_search_config", {}
                ),
                custom_local_search_config=source_config.get(
                    "custom_local_search_config", {}
                ),
                is_template=source_config.get("is_template", False),
                has_template=source_config.get("has_template", False),
                execution_method="parallel",  # 新增字段
                outline_interaction_enabled=False,  # 关闭大纲多轮交互
            )

            # 利用 dataclass 的内置方法安全输出标准字典
            return asdict(target_dto)

        except Exception as e:
            # 生产环境中务必记录错误上下文，防止 Silent Failure
            logger.error(f"Failed to adapt AgentConfig: {str(e)}", exc_info=True)
            raise ValueError(f"Configuration adaptation failed: {e}")
