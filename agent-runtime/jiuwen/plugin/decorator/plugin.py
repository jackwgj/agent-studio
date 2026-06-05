#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""decorator function_plugin"""

import inspect

from jiuwen.plugin.common.exception import PluginParamException, ExceptionsMessage
from jiuwen.plugin.models import plugin as plugin_models
from jiuwen.plugin.models.function import Function


def function_plugin(
    name: str,
    description: str,
    dependency: str = "",
    label: str = "",
):
    """
    A decorator that warps a function to a FunctionPlugin adding meta information

    Args:
        name (str): plugin name
        description(str): plugin description
        dependency(str): the dependency of the user define functions

    Returns:
        FunctionPlugin, A FunctionPlugin that warps the function
    """

    def decorator(cls):
        class FunctionPlugin(plugin_models.FunctionPlugin):
            """function plugin"""

            def __init__(
                self,
            ):
                super().__init__(name, description, dependency)

                funcs = inspect.getmembers(cls(), inspect.ismethod)
                for _, func in funcs:
                    native_func = Function()
                    native_func.name = func.__func_name__
                    native_func.params = check_param(func, func.__params__)
                    native_func.description = func.__description__
                    native_func.plugin_name = name
                    native_func.plugin_desc = description
                    native_func.plugin_depe = dependency
                    native_func.label = label
                    native_func.callable_function = func
                    self.function_list.append(native_func)
                    self.function_map[native_func.name] = native_func
                    self.function_map_with_function_name[getattr(func, "__name__")] = (
                        native_func
                    )

        return FunctionPlugin

    return decorator


def check_param(function, params):
    """check param"""
    # 当前校验param校验逻辑如下：
    # param的名字与个数需要与func的实际参数的名字与个数对应，否则报错。
    # param的类型、是否必须、默认值等信息根据func自动生成。
    params_map = {}
    if params:
        params_map = {param.name: param for param in params}
    func_params = inspect.signature(function).parameters
    if len(func_params) != len(params_map):
        raise PluginParamException(
            message=ExceptionsMessage.ParamsInconsistentErrorJson
        )
    for _, func_param in func_params.items():
        if not params_map.get(func_param.name, None):
            raise PluginParamException(
                message=ExceptionsMessage.ParamsInconsistentErrorJson
            )
        # func参数没有默认值，即是必要的参数
        if func_param.default is func_param.empty:
            # param中的required=True，default_value=None，type=func_param.annotation
            param = params_map.get(func_param.name)
            param.required = True
            param.default_value = None
            param.type = func_param.annotation.__name__
            params_map[func_param.name] = param
        # func参数有默认值，即非必要参数
        else:
            # param中的required=False，default_value=func_param.default，type=func_param.annotation
            param = params_map.get(func_param.name)
            param.required = False
            param.default_value = str(func_param.default)
            param.type = func_param.annotation.__name__
            params_map[func_param.name] = param
    return list(params_map.values())
