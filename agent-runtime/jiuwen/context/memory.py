#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Optional, Dict

from jiuwen.controller.atomic_skills.extract_params import ExtractParamsState

EXTRACT_PARAMS = "extract_params"
TOOL = "tool"


class MemoryContext:
    def __init__(self):
        self.memory_variable: Optional[Dict] = {}

    def get_memory_variable(self):
        """get memory variable"""
        return self.memory_variable

    def add_short_term_memory(self, key, state_key, val):
        """append short term memory"""
        if key not in self.memory_variable:
            self.memory_variable[key] = {state_key: val}
        else:
            self.memory_variable[key][state_key] = val

    def get_param_extraction_context(self, key):
        """get ExtractParamsState from memory variable"""
        cur_memory_variable = self.memory_variable.get(key, {})
        return cur_memory_variable.get(EXTRACT_PARAMS, ExtractParamsState())

    def get_tool_results(self, key):
        """get tool results from memory variable"""
        cur_memory_variable = self.memory_variable.get(key, {})
        return cur_memory_variable.get(TOOL, [])
