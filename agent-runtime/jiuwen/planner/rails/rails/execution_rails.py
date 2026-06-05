# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""input rails interface"""

from jiuwen.common.log.base import logger
from jiuwen.planner.rails.common.constants import RailsType
from jiuwen.planner.rails.config import RailsConfig
from jiuwen.planner.rails.rails.base import Rails
from jiuwen.planner.rails.runtime.runtime import RailsRuntime
from jiuwen.planner.rails.schema.rails_execution import ExecutionRailsOutputSchema
from pydantic import ValidationError


class ExecutionRails(Rails):
    def __init__(self, config: RailsConfig):
        super().__init__(config)
        self.runtime = RailsRuntime(config, RailsType.ExecutionRails)

    def validate_input(self, inputs: dict) -> None:
        pass

    def validate_output(self, outputs: dict) -> None:
        try:
            validated_instance = ExecutionRailsOutputSchema.validate(outputs)
            logger.info(f"validated_instance: {validated_instance}")
        except ValidationError as e:
            logger.error(f"Validation error: {e}", simple_log="Validation error")
        pass

    def process(self, inputs: dict, **kwargs) -> dict:
        return self.runtime.run(inputs)

    async def aprocess(self, inputs: dict, **kwargs) -> dict:
        return await self.runtime.arun(inputs)
