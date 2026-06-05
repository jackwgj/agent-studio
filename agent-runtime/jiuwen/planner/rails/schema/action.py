# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""action schema"""

import inspect
from copy import deepcopy
from typing import Callable, List, Dict, Any

from jiuwen.common.log.base import logger
from jiuwen.planner.rails.config import ActionMetadata


class Action:
    """Action, includes action's metadata and callable function"""

    def __init__(
        self,
        action_metadata: ActionMetadata,
        callable_action: Callable = None,
        extra_input_keywords: List[str] = None,
    ):
        self._action_metadata: ActionMetadata = action_metadata
        self._callable_action: Callable = callable_action
        self._extra_input_keywords: List[str] = extra_input_keywords or []

    @property
    def action_metadata(self):
        """action metadata"""
        return self._action_metadata

    def invoke(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """invoke"""
        context, extra_inputs = self._prepare_invoke_params(context)
        result = self._callable_action(
            context=context, config=self._action_metadata.config, **extra_inputs
        )
        return result

    async def ainvoke(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """async invoke - 自动判断同步/异步"""
        context, extra_inputs = self._prepare_invoke_params(context)

        # 检查 _callable_action 是否为异步函数
        if inspect.iscoroutinefunction(self._callable_action):
            # 异步 action：使用 await
            result = await self._callable_action(
                context=context, config=self._action_metadata.config, **extra_inputs
            )
        else:
            # 同步 action：直接调用
            result = self._callable_action(
                context=context, config=self._action_metadata.config, **extra_inputs
            )

        return result

    def _prepare_invoke_params(self, context: Dict[str, Any]) -> tuple:
        """准备调用参数的公共逻辑"""
        rmv_list = list()
        extra_inputs = dict()
        for key in self._extra_input_keywords:
            extra_inputs.update({key: context.get(key)})
            rmv_list.append(key)
        for _ in rmv_list:
            context.pop(_)

        context_log = deepcopy(context)
        context_log.pop("runtime_context", None)
        logger.info(
            f"Before invoke action, action's input context = {type(context_log)}, extra_inputs = {type(extra_inputs)}"
        )

        return context, extra_inputs
