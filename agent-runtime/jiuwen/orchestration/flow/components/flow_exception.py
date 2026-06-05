#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from jiuwen.common.exception.base import WorkflowAbortException
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.constant import USER_FIELDS
from jiuwen.orchestration.utils import Input, Output


class ExceptionInfo(Invokable):
    """
    异常结束组件
    到达该节点时，直接抛出 WorkflowAbortException
    """

    def __init__(self, node_id: str, node_name: str, node_type: str):
        self.node_id = node_id
        self.node_name = node_name
        self.node_type = node_type

    def invoke(self, inputs: Input, **kwargs) -> Output:
        # 取出inputs中的dict，抛异常
        if inputs is None:
            user_fields = {}
        else:
            user_fields = inputs.get(USER_FIELDS, {})
        raise WorkflowAbortException(
            user_fields, self.node_id, self.node_name, self.node_type
        )
