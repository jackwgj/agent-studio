#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Any

from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.constant import USER_FIELDS
from jiuwen.orchestration.utils import Input


class ParamOutput(Invokable):
    """
    Input node.
    EI.ParamOutput
    """

    def __init__(self):
        self.conf = None
        self.runtime_context = None
        self.metadata = None
        super().__init__()

    def init(self, conf: dict, **kwargs):
        runtime_context = kwargs.get("runtime_context")
        metadata = kwargs.get("metadata")
        self.conf = conf
        self.runtime_context = runtime_context
        self.metadata = metadata
        super().__init__()

    def invoke(self, inputs: dict[str, Any], **kwargs):
        pass

    async def ainvoke(self, inputs: Input, **kwargs):
        return {USER_FIELDS: inputs.get("userFields")}
