#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
@Copyright: Copyright (C) 2024-2024 Huawei Inc
@Project:
@Author: jiuwen
@Date: 2024-10-22
@LastEditTime: 2024-10-22 14:54:38
@Description: planner utils
"""

import json
from typing import Dict, List

from jiuwen.common.log.base import logger
from jiuwen.planner.common.enum_class import ProviderType
from jiuwen.plugin.models.tool import Tool


def get_workflow_func(func_name: str, functions: List[Tool]):
    """search workflow by func name"""
    select_func = None
    for func in functions:
        if func_name in func.tool_id and func.provider_type == ProviderType.WORKFLOW:
            select_func = func
            break
    return select_func


def replace_func_args(function_call: Dict, args: Dict):
    """replace_func_call_args"""
    if not isinstance(function_call, dict):
        logger.error("function call is not type dict")
        return
    arguments = function_call.get("arguments", dict())

    # 如果是字符串，尝试解析为字典
    if isinstance(arguments, str):
        try:
            arguments = json.loads(arguments)
        except json.JSONDecodeError:
            logger.error("parse function call arguments failed")
            return

    # 如果是字典，进行替换
    if isinstance(arguments, dict):
        for key, value in args.items():
            if key in arguments:
                arguments[key] = value

    function_call["arguments"] = json.dumps(arguments, ensure_ascii=False)
