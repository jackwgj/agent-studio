# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""Rails runtime context"""

import copy
from typing import Dict, Any, List


class RailsContext:
    """Context of rails runtime"""

    def __init__(self):
        self._var_space: Dict[str, Any] = dict()

    @property
    def var_space(self):
        """var space"""
        return self._clone_all_context()

    def clear(self):
        """clear context information"""
        self._var_space.clear()

    def batch_update(self, inputs: Dict[str, Any]):
        """batch update context k-v information"""
        if inputs is not None and isinstance(inputs, dict):
            self._var_space.update(inputs)

    def update(self, key: str, value: Any):
        """update context k-v information"""
        self._var_space.update({key: value})

    def get_context_by_keys(self, keys: List[str]):
        """get current context's duplicate, with designated keys"""
        res: Dict[str, Any] = dict()
        clone_context = copy.deepcopy(self._var_space)
        for k in keys:
            if k in clone_context:
                res.update({k: clone_context.pop(k)})
        return res

    def _clone_all_context(self):
        """get current context's duplicate"""
        return copy.deepcopy(self._var_space)
