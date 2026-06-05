#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2026. All rights reserved.

"""
This module is used to define the abstract class that interactive nodes need to implement.
"""

from abc import ABC, abstractmethod


class InteractiveComponent(ABC):
    """
    交互组件接口

    所有需要用户交互的工作流组件都应该实现此接口。

    核心特性：
    1. 极简设计：只需实现一个方法 should_interrupt()
    2. 零侵入性：不影响组件的现有实现
    3. 自动化：框架自动调用并传递判断结果到控制器
    """

    @abstractmethod
    def should_interrupt(self) -> bool:
        """
        判断当前是否需要中断工作流等待用户输入

        这是框架用来统一识别交互中断的接口。

        Returns:
            True 表示需要中断工作流等待用户输入
            False 表示不需要中断
        """
        pass
