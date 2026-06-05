# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
meta template type
"""

from enum import Enum


class MetaTemplateType(Enum):
    """MetaTemplate enumeration class。"""

    AUTO_MATCH_META = "auto_match_meta"
    APE_META = "ape_meta"
    BROKE_META = "broke_meta"
    ICIO_META = "icio_meta"
    RACE_META = "race_meta"
    ROSES_META = "roses_meta"
    TRACE_META = "trace_meta"
    AUTOGPT_META = "auto_gpt_meta"
    AUTOGPT_SUFFIX = "auto_gpt_suffix"
    CRISPE_META = "crispe_meta"
    MINI_ROLE_META = "mini_role_meta"
    ROLE_META = "role_meta"
    PLAN_META = "plan_meta"
    REC_META = "rec_meta"
    APP_META = "app_meta"
    GPTS_META = "gpts_meta"
    MODULAR_BUILDER_META = "modular_builder_meta"
    MODULAR_BUILDER_ROLE_META = "modular_builder_role_meta"
    MODULAR_BUILDER_SKILLS_META = "modular_builder_skills_meta"
    MODULAR_BUILDER_WORKFLOW_META = "modular_builder_workflow_meta"
    MODULAR_BUILDER_CONSTRAINTS_META = "modular_builder_constraints_meta"
    GENERAL_META_EPO = "general_meta_epo"
    XIAOYI_GENERAL_META_EPO = "xiaoyi_general_meta_epo"
    XIAOYI_PLAN_META_EPO = "xiaoyi_plan_meta_epo"
    ROLE_TOOL_META = "role_tool_meta"
    PLAN_META_EPO = "plan_meta_epo"
    PLAN_META_EPO_MIX = "plan_meta_epo_mix"
    RAG_META_EPO = "rag_meta_epo"
    VQATEMPLATE_GPT4_MM = "vqatemplate_gpt4_mm"
