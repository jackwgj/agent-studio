# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""trigger of action"""

from typing import Dict, Any, List

from jiuwen.planner.rails.common import utils
from jiuwen.planner.rails.common.constants import TriggerType
from jiuwen.planner.rails.config import TriggerConfig, ActionMetadata


class Trigger:
    def __init__(
        self,
        trigger_config: TriggerConfig,
        trigger_include_actions: List[ActionMetadata] = None,
    ):
        self.trigger_config = trigger_config
        self.trigger_include_actions: List[ActionMetadata] = (
            trigger_include_actions or []
        )

    def should_trigger(self, context_dict: Dict[str, Any]) -> bool:
        """execute trigger functionality, check if included actions should be executed"""
        if not self.trigger_config:
            return True
        if self.trigger_config.trigger_type == TriggerType.DefaultTrigger:
            return True
        if self.trigger_config.trigger_type == TriggerType.KeywordsTrigger:
            query = context_dict.get("query")
            return utils.if_str_contain_trigger_words(
                query,
                self.trigger_config.keywords,
                self.trigger_config.exclude_keywords,
            )
        return False
