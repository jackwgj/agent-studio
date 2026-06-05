#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from abc import ABC, abstractmethod


class GraphEngine(ABC):
    """
    GraphEngine 基类，包含两个抽象方法 run_workflow 和 resume_workflow。
    """

    def __init__(self, graph_instance):
        self.graph_instance = graph_instance

    @abstractmethod
    def run_workflow(self, inputs, params: dict, **kwargs):
        """
        启动工作流的抽象方法。
        """
        pass

    @abstractmethod
    async def arun_workflow(self, inputs, params: dict, **kwargs):
        """
        异步启动工作流的抽象方法。
        """
        pass

    @abstractmethod
    def resume_workflow(self, inputs, params: dict, **kwargs):
        """
        恢复工作流的抽象方法。
        """
        pass
