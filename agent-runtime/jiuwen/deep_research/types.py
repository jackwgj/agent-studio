# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""This module defines execution data types for DeepResearch."""

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.deep_research.status_code import DSStatusCode
from pydantic import BaseModel, Field, field_validator


class DRLLMConfigValidator(BaseModel):
    model_name: str = Field(..., description="模型名字", max_length=1000)
    model_type: str = Field(..., description="模型类型", max_length=1000)
    base_url: str = Field(..., description="模型url", max_length=1000)
    api_key: str = Field(..., description="模型key")
    hyperParameters: dict = Field(..., description="配置项列表")
    extension: dict = Field(default={}, description="配置项列表")

    # 使用 field_validator 校验 temperature 和 topP 范围
    @field_validator("hyperParameters", mode="before")
    @classmethod
    def check_hyper_parameters(cls, v):
        """validate hyper_parameters rule"""
        if not isinstance(v, dict):
            raise JiuWenBaseException(
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.code,
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="hyper_parameters should be dict"
                ),
            )
        top_p = v.get("top_p")
        if not isinstance(top_p, (int, float)) or top_p < 0 or top_p > 1:
            raise JiuWenBaseException(
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.code,
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="top_p must be between 0 and 1"
                ),
            )
        frequency_penalty = v.get("frequency_penalty")
        if (
            not isinstance(frequency_penalty, (int, float))
            or frequency_penalty < 0
            or frequency_penalty > 1
        ):
            raise JiuWenBaseException(
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.code,
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="frequency_penalty must be between 0 and 1"
                ),
            )
        max_tokens = v.get("max_tokens")
        if not isinstance(max_tokens, int) or max_tokens < 0 or max_tokens > 129000:
            raise JiuWenBaseException(
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.code,
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="max_tokens must be between 0 and 128k"
                ),
            )
        temperature = v.get("temperature")
        if (
            not isinstance(temperature, (float, int))
            or temperature < 0
            or temperature > 1
        ):
            raise JiuWenBaseException(
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.code,
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="temperature must be between 0 and 1"
                ),
            )
        return v


class DRWebSearchConfigValidator(BaseModel):
    search_engine_name: str = Field(..., description="web工具名字", max_length=1000)
    search_api_key: str = Field(..., description="web工具key")
    search_url: str = Field(..., description="web工具URL", max_length=1000)
    max_web_search_results: int = Field(default=5, description="web工具单次返回个数")
    extension: dict = Field(default={}, description="web工具配置项列表")


class DRLocalSearchConfigValidator(BaseModel):
    search_engine_name: str = Field(
        ..., description="本地知识库工具名字", max_length=1000
    )
    search_api_key: str = Field(..., description="本地知识库工具key")
    search_url: str = Field(default="", description="本地知识库工具URL", max_length=1000)
    search_datasets: list = Field(default_factory=list, description="本地知识库列表")
    knowledgeConfig: dict = Field(
        default_factory=dict, description="知识库检索配置，供 FlowKnowledgeRetrieval 消费"
    )
    max_local_search_results: int = Field(
        default=5, description="本地知识库工具单次返回个数"
    )
    similarity_threshold: float = Field(default=0.6, description="本地知识库相似度阈值")
    extension: dict = Field(default={}, description="本地知识库工具配置项列表")


class DRCustomWebSearchConfigValidator(BaseModel):
    custom_web_search_file: str = Field(
        ..., description="自定义web工具文件路径", max_length=1000
    )
    custom_web_search_func: str = Field(
        ..., description="自定义web工具函数名称", max_length=1000
    )
    extension: dict = Field(default={}, description="自定义web工具配置项列表")


class DRCustomLocalSearchConfigValidator(BaseModel):
    custom_local_search_file: str = Field(
        ..., description="自定义本地知识库工具文件路径", max_length=1000
    )
    custom_local_search_func: str = Field(
        ..., description="自定义本地知识库工具函数名称", max_length=1000
    )
    extension: dict = Field(default={}, description="自定义本地知识库工具配置项列表")


class DeepResearchIrValidator(BaseModel):
    schemaVersion: str = Field(..., description="IR版本号", max_length=1000)
    agentId: str = Field(..., description="Agent唯一标识id", max_length=1000)
    description: str = Field(..., description="Agent描述信息", max_length=1000)
    agentVersion: str = Field(..., description="agent版本号", max_length=1000)
    agentName: str = Field(..., description="Agent名称", max_length=1000)
    ir_path: str = Field(..., description="IR文件路径", max_length=1000)
    configs: dict = Field(..., description="配置项列表")

    @field_validator("configs", mode="before")
    @classmethod
    def validate_configs(cls, v):
        """validate configs rule"""
        if not isinstance(v, dict):
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="configs must be dictionaries"
                ),
            )
        execute_mode = v.get("deep_search_execute_mode")
        if execute_mode not in ["commercial", "general"]:
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg='deep search execute_mode must be "commercial" or "general"'
                ),
            )
        workflow_human_in_the_loop = v.get("workflow_human_in_the_loop")
        if not isinstance(workflow_human_in_the_loop, bool):
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="configs must be bool"
                ),
            )
        outliner_max_section_num = v.get("outliner_max_section_num")
        if not (
            isinstance(outliner_max_section_num, int)
            and 1 <= outliner_max_section_num <= 10
        ):
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="outliner_max_section_num must be an integer between 1 and 10"
                ),
            )
        source_tracer_research_trace_source_switch = v.get(
            "source_tracer_research_trace_source_switch"
        )
        if not isinstance(source_tracer_research_trace_source_switch, bool):
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="source_tracer_research_trace_source_switch must be bool"
                ),
            )
        llm_config = v.get("llm_config")
        DRLLMConfigValidator(**llm_config)
        info_collector_search_method = v.get("info_collector_search_method")
        if info_collector_search_method not in ["web", "local", "all"]:
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg='info_collector_search_method must be "web" or "local" or '
                    '"all"'
                ),
            )
        web_search_engine_config = v.get("web_search_engine_config")
        DRWebSearchConfigValidator(**web_search_engine_config)
        local_search_engine_config = v.get("local_search_engine_config")
        DRLocalSearchConfigValidator(**local_search_engine_config)
        custom_web_search_config = v.get("custom_web_search_config")
        DRCustomWebSearchConfigValidator(**custom_web_search_config)
        custom_local_search_config = v.get("custom_local_search_config")
        DRCustomLocalSearchConfigValidator(**custom_local_search_config)
        has_template = v.get("has_template")
        if not isinstance(has_template, bool):
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="has_template must be bool"
                ),
            )
        is_template = v.get("is_template")
        if not isinstance(is_template, bool):
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg="is_template must be bool"
                ),
            )

        return v
