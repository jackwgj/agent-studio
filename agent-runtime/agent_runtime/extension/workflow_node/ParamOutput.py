#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
ParamOutput - 参数透传组件

功能：将输入中的 userFields 透传到输出，不做任何转换。
用于复合节点（如对象提取）的入口/出口/循环开始等位置。
"""

from openjiuwen.core.common.constants.constant import USER_FIELDS
from openjiuwen.core.context_engine import ModelContext
from openjiuwen.core.graph.executable import Input, Output
from openjiuwen.core.session.node import Session
from openjiuwen.core.workflow.components.component import WorkflowComponent

PARAM_OUTPUT_TYPE = "EI.ParamOutput"


class ParamOutput(WorkflowComponent):
    """参数透传组件，将 inputs 中的 userFields 直接透传到输出。"""

    def __init__(self, conf: dict | None = None):
        super().__init__()
        self._config = conf or {}

    async def invoke(self, inputs: Input, session: Session, context: ModelContext) -> Output:
        if isinstance(inputs, dict):
            result = {USER_FIELDS: inputs.get(USER_FIELDS, {})}
            system_fields = inputs.get("systemFields")
            if system_fields is not None:
                result["systemFields"] = system_fields
            return result
        return {USER_FIELDS: inputs}

    @property
    def node_type(self) -> str:
        return PARAM_OUTPUT_TYPE

    @property
    def config(self) -> dict:
        return self._config
