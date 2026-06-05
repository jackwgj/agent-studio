#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from jiuwen.common.log.base import logger
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.workflow import Workflow
from orchestration.utils import Input, Output


class WorkflowInvokable(Invokable):
    def __init__(self, workflow: Workflow):
        super().__init__()
        self.workflow = workflow

    def invoke(self, *args, **kwargs):
        logger.info("Do invoke")

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        logger.info("Do ainvoke")
