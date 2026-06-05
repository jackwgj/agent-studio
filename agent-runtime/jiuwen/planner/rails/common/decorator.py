# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""action wrapper module"""

import functools

from jiuwen.common.log.base import logger
from jiuwen.planner.rails.runtime.action_manager import ActionManagerSingleton


def action(func):
    """decorator for action function"""

    @functools.wraps(func)  # 保留原函数的元数据
    def wrapper(*args, **kwargs):
        name = func.__name__
        logger.info(f"start to register action {name}")
        extra_input_parameters = [
            k for k, _ in kwargs.items() if k not in ["context", "config"]
        ]
        if extra_input_parameters:
            logger.info(
                f"start to register action's input parameters = {type(extra_input_parameters)}"
            )
        ActionManagerSingleton.register_action(name, func, extra_input_parameters)
        return func

    return wrapper
