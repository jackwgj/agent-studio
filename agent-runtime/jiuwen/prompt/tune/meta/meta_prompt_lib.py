# -*- coding: UTF-8 -*-
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2025. All rights reserved.
"""
prompt_template
"""

import os
from dataclasses import dataclass

import yaml


@dataclass
class MetaPromptLib:
    def __init__(self):
        with open(
            os.path.join(
                os.path.dirname(os.path.abspath(__file__)), "meta_prompt_lib.yaml"
            ),
            encoding="utf-8",
        ) as f:
            self.template = yaml.safe_load(f)

    def function(self, template_type: str):
        """
        return function of template
        """
        return self.template.get(template_type, "")
