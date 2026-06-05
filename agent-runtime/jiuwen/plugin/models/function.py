#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Function class"""

import inspect
from abc import ABC
from typing import List, Callable

from jiuwen.insight.manager import TraceManager
from jiuwen.insight.utils import get_instance_info
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.utils import Input, Output
from jiuwen.plugin.models.param import Param


class Function(Invokable, ABC):
    """Function class"""

    def __init__(
        self,
        name: str = "",
        description: str = "",
        params: List[Param] = None,
        func: Callable = None,
        plugin_name: str = "",
        plugin_desc: str = "",
        label: str = "",
        **kwargs,
    ):
        self.name: str = name
        self.description: str = description
        self.params: List[Param] = params
        self.callable_function: Callable = func
        self.plugin_name = plugin_name
        self.plugin_desc = plugin_desc
        self.label = label
        self.related_info: dict = kwargs.get("related_info")
        self.status: int = kwargs.get("status")
        self.principle: str = kwargs.get("principle")
        self.user_id: str = kwargs.get("user_id", "")

    def invoke(self, inputs: Input, **kwargs) -> Output:
        """
        Args:
            inputs: params
            **kwargs:
        Returns:
            Output
        """
        if not isinstance(inputs, dict):
            raise TypeError(
                "Plugin.invoke() excepted 'dict', but got {}".format(
                    type(inputs).__name__
                )
            )
        inputs = self._format_input_with_default_when_required(inputs)
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None), get_instance_info(self)
        )
        trace_manager.on_plugin_start(inputs)
        try:
            # 去掉cloudpickle以后，在当前运行程序运行状态下，invoke正常
            # 如果当前程序结束，开启新的程序，但不重新进行插件更新或者注册，从数据库中获取的callable_function将无法运行
            # 如果运行失败，这里的运行请考虑使用其他方式实现
            res = self.callable_function(**inputs)
            trace_manager.on_plugin_end(res)
            return res
        except Exception as error:
            trace_manager.on_plugin_error(error)
            raise error

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        if not isinstance(inputs, dict):
            raise TypeError(
                "Plugin.ainvoke() excepted 'dict', but got {}".format(
                    type(inputs).__name__
                )
            )
        inputs = self._format_input_with_default_when_required(inputs)
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None), get_instance_info(self)
        )
        trace_manager.on_plugin_start(inputs)
        try:
            res = self.callable_function(**inputs)
            if inspect.isawaitable(res):
                res = await res
            trace_manager.on_plugin_end(res)
            return res
        except Exception as error:
            trace_manager.on_plugin_error(error)
            raise error

    def _format_input_with_default_when_required(self, inputs: dict):
        params_default_dict = {}
        for param in self.params:
            if param.required and (param.default_value or param.default_value is False):
                params_default_dict[param.name] = param.default_value
        for field, value in params_default_dict.items():
            if not inputs.get(field) and inputs.get(field) is not False:
                inputs[field] = value
        return inputs
