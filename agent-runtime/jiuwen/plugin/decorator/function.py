#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""decorator func"""

from typing import List

from jiuwen.plugin.models.param import Param


def func(*, name: str, description: str, params: List[Param] = None):
    """
    A decorator that adds meta informateions to a function.

    Args:
        name (str): function name
        description(str): function description
        params(List[Param]): function parameters list. Param is the class of parameters which includes name,
                             description,default_value,required,type

    Returns:
        fucntion, A new function that decorates the original function

    """

    def decorator(ori_func):
        """decorator"""
        ori_func.__func_name__ = name
        ori_func.__description__ = description
        ori_func.__params__ = params
        return ori_func

    return decorator
