#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
Default UI Component
"""

import json

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.executor_sdk.operator.common.constant import StreamCode
from jiuwen.executor_sdk.operator.common.model.stream_data import StreamData
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.utils import Input, Output


class DefaultUIComponent(Invokable):
    """Default UI Component"""

    def __init__(self):
        super().__init__()
        self.execute_mode = None

    def init(self, conf: str) -> int:
        """init component"""
        self.get_logger().info("Start to Call DefaultUIComponent.init()")
        config_list = json.loads(conf)["configs"]
        config = {
            config_item.get("name"): config_item.get("value")
            for config_item in config_list
        }
        self.execute_mode = config.get("executeMode") or "batch"
        return 0

    def invoke(self, inputs: Input, **kwargs) -> Output:
        """invoke with stream mode"""
        self.get_logger().info("Start to Call DefaultUIComponent.invoke()")
        runtime_context = kwargs.get("runtime_context")
        if not runtime_context:
            raise JiuWenBaseException(
                StatusCode.RUNTIME_CONTEXT_ERROR.code,
                StatusCode.RUNTIME_CONTEXT_ERROR.errmsg,
            )
        if self.execute_mode == "stream":
            stream_info = inputs.get("streamInfo") or {}
            code = (
                StreamCode.FINISH
                if stream_info.get("streamType") == "final"
                else StreamCode.PARTIAL_CONTENT
            )
            item_data = json.dumps(stream_info, ensure_ascii=False)
            item = StreamData(code=code, data=item_data)
            runtime_context.enqueue_side_output("", item)
        self.get_logger().info("End to Call DefaultUIComponent.invoke()")

        return inputs
