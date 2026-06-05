#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from dataclasses import dataclass


@dataclass
class DeepResearchAgentConfig:
    """DeepResearch配置类，用于封装DeepResearch初始化参数"""

    conversation_id: str = None
    template_path: str = None
    # DeepResearch config
    execute_mode: str = "commercial"
    workflow_human_in_the_loop: bool = False
    outliner_max_section_num: int = 3
    source_tracer_research_trace_source_switch: bool = False
    llm_config: dict = None
    info_collector_search_method: str = "web"
    # search engine config
    web_search_engine_config: dict = None
    local_search_engine_config: dict = None
    # custom search tool config
    custom_web_search_config: dict = None
    custom_local_search_config: dict = None
    # template config
    has_template: bool = False
    is_template: bool = False
