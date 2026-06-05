#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
status code
"""

from enum import Enum
from typing import Final


class StatusCode(Enum):
    """
    StatusCode definition.
    """

    SUCCESS = (0, "success")
    PARAM_ERROR = (1, "param check failed")
    INTER_ERROR = (2, "internal error")
    NOT_FOUND = (3, "resource not found")
    REQUEST_ERROR = (4, "request failed")
    CONNECTION_ERROR = (5, "connection failed")

    def __init__(self, code, message):
        self.code = code
        self.message = message


class TaskStatus:
    """
    TaskStatus definition.
    """

    TASK_STATUS: Final[str] = "status"
    TASK_RUNNING: Final[str] = "running"
    TASK_FINISHED: Final[str] = "finished"
    TASK_FAILED: Final[str] = "failed"
    TASK_DELETED: Final[str] = "deleted"
    TASK_STOPPED: Final[str] = "stopped"
    TASK_QUEUED: Final[str] = "queued"
