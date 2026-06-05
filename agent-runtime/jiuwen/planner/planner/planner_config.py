#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2023-10-19
@LastEditTime: 2023-10-19 17:23:19
@Description: Data structure of planning configs
"""

from dataclasses import dataclass, field
from pathlib import Path

import yaml
from jiuwen.planner.common.exception import PlannerConfigCheckFailedException
from jiuwen.prompt import Template


class OperatorConfig:
    """config of planning operator"""

    def __init__(
        self,
        prompt_template_name: str = None,
        is_chat: bool = False,
        reserved_max_chat_rounds: int = 10,
        operator_name: str = None,
    ):
        self.prompt_template_name = prompt_template_name
        self.is_chat = is_chat
        self.reserved_max_chat_rounds = reserved_max_chat_rounds
        self.operator_name = operator_name


class ConstrainConfig:
    """config of planning constrain"""

    def __init__(self, max_iteration: int = 10, **kwargs):
        self.max_iteration = max_iteration


class SOPPlanConfig:
    """config of sop planning"""

    def __init__(
        self,
        prompts=None,
        term_en_cn_mapping=None,
        if_selection_ensure=None,
        if_selection=None,
        if_chat_check=None,
        if_multi_turn=None,
    ):
        self.prompts = prompts
        self.term_en_cn_mapping = term_en_cn_mapping
        self.if_selection_ensure = if_selection_ensure
        self.if_selection = if_selection
        self.if_chat_check = if_chat_check
        self.if_multi_turn = if_multi_turn


@dataclass
class IntentDetectionPlanConfig:
    intent_detection_template: Template
    default_class: str = "分类1"
    enable_history: bool = False
    enable_input: bool = True
    chat_history_max_turn: int = 5
    example_content: list[str] = field(default_factory=list)
    user_prompt: str = ""
    category_info: str = ""
    category_list: list[str] = field(default_factory=list)


class PlanConfig:
    """
    规划引擎的配置文件
    """

    def __init__(self, constrain_config=None, sop_plan_config=None, **kwargs):
        self.propose_next_config = kwargs.get("propose_next_config")
        self.sop_plan_config = sop_plan_config
        self.constrain_config = (
            constrain_config if constrain_config else ConstrainConfig()
        )
        self.workflow_switch_enable = kwargs.get("workflow_switch_enable")

    @classmethod
    def from_config_file(cls, plan_config_file):
        """Constructs a new PlanConfig instance by loading configuration from a specified YAML file."""
        # 路径规范化，确保不会被 .. 绕过
        config_path = Path(plan_config_file).resolve()

        if not config_path.exists():
            raise PlannerConfigCheckFailedException(
                "Planner's init config file is not existed!"
            )

        with config_path.open(encoding="utf-8") as f:
            yaml_data = yaml.safe_load(f)
        if not yaml_data:
            return cls(**dict())
        return PlanConfig.from_config_dict(yaml_data)

    @classmethod
    def from_config_dict(cls, input_dict: dict):
        """Constructs a new PlanConfig instance by loading configuration from a specified dict."""
        configs = {}
        for config_type, config_value in input_dict.items():
            if config_type == "constrain":
                configs["constrain_config"] = ConstrainConfig(**config_value)
                continue
            if config_type == "sop_plan":
                configs["sop_plan_config"] = SOPPlanConfig(**config_value)
                continue
            if config_type == "workflow_switch_enable":
                configs["workflow_switch_enable"] = config_value
                continue
            configs[f"{config_type}_config"] = OperatorConfig(**config_value)
        return cls(**configs)
