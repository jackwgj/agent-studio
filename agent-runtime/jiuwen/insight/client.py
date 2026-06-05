#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""client of trace module"""

import json
import os
import queue
from typing import Optional

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.insight.handlers.data import InvokeData4Agent
from jiuwen.insight.utils import (
    serialize_json,
    ENVIRONMENT_INSIGHT_MEMORY,
    INSIGHT_EI_DEBUG_INFO_ENABLE,
)


class Client:
    """Client for interacting with the insight api"""

    def __init__(self, insight_project_id: Optional[str] = None) -> None:
        """Initialize client"""
        self.insight_project_id = insight_project_id
        self.data_list = []
        self.data_queue = queue.Queue()
        self.agent_debug_info = bool(
            str(os.environ.get(INSIGHT_EI_DEBUG_INFO_ENABLE)).lower() == "true"
        )

    def send_invoke(self, invoke_data: dict) -> None:
        """
        send invoke
        """
        if not self.insight_project_id:
            raise JiuWenBaseException(
                StatusCode.INSIGHT_PROJECT_ID_EMPTY_ERROR.code,
                StatusCode.INSIGHT_PROJECT_ID_EMPTY_ERROR.errmsg,
            )
        try:
            if self.agent_debug_info:
                agent_data = InvokeData4Agent()
                agent_data.dict_to_attrs(invoke_data)
                agent_data.meta_data = json.loads(
                    json.dumps(
                        invoke_data.get("meta_data"),
                        ensure_ascii=False,
                        default=lambda _obj: (
                            f"<<non-serializable: {type(_obj).__qualname__}>>"
                        ),
                    )
                )
                self.data_queue.put({"data": agent_data.model_dump(by_alias=True)})
                return
            invoke_data = json.loads(json.dumps(invoke_data, default=serialize_json))
            if os.environ.get(ENVIRONMENT_INSIGHT_MEMORY):
                self.data_list.append(invoke_data)
                return
        except json.decoder.JSONDecodeError as err:
            logger.error("save invokable data error")
            raise ValueError("related_info error: Decoder error") from err
