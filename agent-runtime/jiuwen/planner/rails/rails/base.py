# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""rails interface"""

from abc import ABC, abstractmethod

from jiuwen.orchestration import Invokable
from jiuwen.orchestration.utils import Input, Output
from jiuwen.planner.rails.config import RailsConfig
from jiuwen.planner.rails.runtime.runtime import RailsRuntime


class Rails(Invokable, ABC):
    """Rails based on a given configuration."""

    config: RailsConfig
    runtime: RailsRuntime

    def __init__(self, config: RailsConfig):
        """Initializes the Rails instance.

        Args:
            config: A rails configuration.
        """
        self.config = config

    @abstractmethod
    def validate_input(self, inputs: Input) -> None:
        """Validate the input schema."""
        pass

    @abstractmethod
    def validate_output(self, outputs: Output) -> None:
        """Validate the output schema."""
        pass

    @abstractmethod
    def process(self, inputs: Input, **kwargs) -> Output:
        """Process the inputs and return outputs.

        Args:
            inputs: The input data to process.

        Returns:
            The processed output data.
        """
        pass

    @abstractmethod
    async def aprocess(self, inputs: Input, **kwargs) -> Output:
        pass

    def invoke(self, inputs: Input, **kwargs) -> Output:
        """Invoke the processing of inputs and validate outputs.

        Args:
            inputs: The input data to process.

        Returns:
            The processed output data.
        """
        self.validate_input(inputs)  # 校验输入
        outputs = self.process(inputs, **kwargs)  # 调用 process 方法
        self.validate_output(outputs)  # 校验输出
        return outputs

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        self.validate_input(inputs)
        outputs = await self.aprocess(inputs, **kwargs)
        self.validate_output(outputs)
        return outputs
