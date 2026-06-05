# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""This module contains an DeepResearch IRConverter."""

from dataclasses import asdict
from typing import Tuple

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.security.cryptor import Crypt
from jiuwen.controller.agent.agent import Agent
from jiuwen.deep_research.config import DeepResearchAgentConfig
from jiuwen.deep_research.open_utils import async_template_load
from jiuwen.deep_research.status_code import DSStatusCode
from jiuwen.deep_research.types import DeepResearchIrValidator
from jiuwen.orchestration.flow.components.util import (
    format_pydantic_validation_error_message,
)
from jiuwen.serve.schemas.orchestration_mgr import ExecutionRequest
from pydantic import ValidationError


class DSIRConverter:
    """DeepResearch IR转换工具类。

    该类用于识别和转换中间表示(IR)数据，包括验证IR版本、创建DeepResearch配置、
    从IR数据创建DeepResearch Agent和DeepResearch Agent Group配置、
    将IR数据转换为DeepResearch Agent或DeepResearch Agent Group实例等。
    """

    @staticmethod
    async def create_deepresearch_config(
        ir_data: dict, conversation_id: str
    ) -> DeepResearchAgentConfig:
        """
        Create a new DeepResearch config.
        """
        ir_configs = ir_data.get("configs", {})
        template_path = ir_data.get("template_path", "")

        llm_config = ir_configs.get("llm_config", {})
        llm_dict = ModelFactory().model_resolver(
            llm_config.get("model_name"), llm_config.get("model_type")
        )
        llm_config["base_url"] = llm_dict.get("api_base", "")
        curr_work_api = Crypt()
        llm_config["api_key"] = bytearray(
            curr_work_api.decrypt(llm_dict.get("api_key", "")), encoding="utf-8"
        )
        llm_config["hyper_parameters"] = llm_config.pop("hyperParameters", {})

        web_search_engine_config = ir_configs.get("web_search_engine_config")
        web_search_engine_config["search_api_key"] = bytearray(
            web_search_engine_config.get("search_api_key", ""), encoding="utf-8"
        )
        local_search_engine_config = ir_configs.get("local_search_engine_config")
        local_search_engine_config["search_api_key"] = bytearray(
            local_search_engine_config.get("search_api_key", ""), encoding="utf-8"
        )

        agent_config = DeepResearchAgentConfig(
            conversation_id=conversation_id,
            template_path=template_path,
            execute_mode="commercial",
            workflow_human_in_the_loop=ir_configs.get("workflow_human_in_the_loop"),
            outliner_max_section_num=ir_configs.get("outliner_max_section_num"),
            source_tracer_research_trace_source_switch=ir_configs.get(
                "source_tracer_research_trace_source_switch"
            ),
            llm_config=llm_config,
            info_collector_search_method=ir_configs.get("info_collector_search_method"),
            web_search_engine_config=web_search_engine_config,
            local_search_engine_config=local_search_engine_config,
            custom_web_search_config=ir_configs.get("custom_web_search_config"),
            custom_local_search_config=ir_configs.get("custom_local_search_config"),
            has_template=ir_configs.get("has_template"),
            is_template=ir_configs.get("is_template"),
        )

        return agent_config

    @staticmethod
    async def create_deepresearch_agent(
        agent_config: DeepResearchAgentConfig, req: ExecutionRequest
    ) -> Tuple[Agent, bytes]:
        """
        Create a new DeepResearch agent.
        """

        template = None
        if agent_config.template_path != "":
            is_external_path = bool(req.params.file_url)
            template = await async_template_load(
                agent_config.template_path, is_external_path
            )
        agent_config.has_template = bool(template)

        from jiuwen_deepsearch.framework.jiuwen.agent.agent_factory import AgentFactory

        agent_factory = AgentFactory()
        deepresearch_agent = agent_factory.create_agent(asdict(agent_config))

        return deepresearch_agent, template

    @staticmethod
    async def ir_to_deepresearch(
        ir_data: dict, req: ExecutionRequest
    ) -> Tuple[Agent, dict, bytes]:
        """
        Convert IR data to an DeepResearch Agent instance.
        :param ir_data: Dictionary parsed from JSON IR file.
        :param req: ExecutionRequest.
        :return: DeepResearch Agent instance.
        """
        try:
            DeepResearchIrValidator(**ir_data)
        except ValidationError as e:
            error_message = format_pydantic_validation_error_message(e)
            raise JiuWenBaseException(
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.code,
                DSStatusCode.LLM_DR_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg=f"DeepResearch_ir validate failed, root_case={error_message}"
                ),
            ) from e

        # 创建agent配置
        deepresearch_agent_config = await DSIRConverter.create_deepresearch_config(
            ir_data, req.conversation_id
        )

        # 创建或恢复agent
        agent, template = await DSIRConverter.create_deepresearch_agent(
            deepresearch_agent_config, req
        )

        return agent, asdict(deepresearch_agent_config), template

    @staticmethod
    async def ir_align_req(ir_data: dict, req: ExecutionRequest) -> None:
        """
        Align IR data to the request (currently focus on template alignment).
        :param ir_data: Dictionary parsed from JSON IR file.
        :param req: ExecutionRequest.
        :return: None.
        """

        ir_configs = ir_data.get("configs", {})

        if req.params.file_url:
            ir_data["template_path"] = req.params.file_url
            ir_configs["has_template"] = True
            ir_configs["is_template"] = req.params.is_template
