#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""async client of trace module"""

import asyncio
import json
import os
from typing import Optional, List, Dict, Any

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
    """Async client for the insight api; intended for async environment only.

    send_invoke is kept synchronous for easy integration. When the queue is full,
    items are buffered in a pending list so no message is dropped and FIFO order
    is preserved on consume.
    """

    def __init__(
        self, insight_project_id: Optional[str] = None, maxsize: int = 0
    ) -> None:
        """Initialize async client.

        Args:
            insight_project_id: Optional project ID for insight.
            maxsize: Maximum size of the async queue. 0 means unlimited.
        """
        self.insight_project_id = insight_project_id
        self.data_list: List[Dict[str, Any]] = []
        self.data_queue: asyncio.Queue[Dict[str, Any]] = asyncio.Queue(maxsize=maxsize)
        self.agent_debug_info = bool(
            str(os.environ.get(INSIGHT_EI_DEBUG_INFO_ENABLE)).lower() == "true"
        )
        self.use_memory = bool(os.environ.get(ENVIRONMENT_INSIGHT_MEMORY))

    def send_invoke(self, invoke_data: dict) -> None:
        """Send invoke data synchronously (async environment only).

        Puts into the async queue; when full, buffers in pending so no message is dropped.
        Consumption order is preserved (queue first, then pending).

        Args:
            invoke_data: Dictionary containing invoke data to send.

        Raises:
            JiuWenBaseException: If insight_project_id is not set.
            ValueError: If JSON serialization fails.
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
                meta = invoke_data.get("meta_data")
                agent_data.meta_data = json.loads(
                    json.dumps(
                        meta,
                        ensure_ascii=False,
                        default=lambda _obj: (
                            f"<<non-serializable: {type(_obj).__qualname__}>>"
                        ),
                    )
                )
                agent_data = agent_data.model_copy(deep=True)
                self.data_queue.put_nowait(
                    {"data": agent_data.model_dump(mode="json", by_alias=True)}
                )
                return
            if self.use_memory:
                serialized = json.loads(json.dumps(invoke_data, default=serialize_json))
                self.data_list.append(serialized)
                return
        except json.decoder.JSONDecodeError as err:
            logger.error("save invokable data error")
            raise ValueError("related_info error: Decoder error") from err
