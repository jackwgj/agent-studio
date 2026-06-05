#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Used to define orchestration module exceptions."""

from jiuwen.common.exception.base import JiuWenBaseException


class OrchestrationException(JiuWenBaseException):
    """Base class of orchestration module exceptions"""

    def __init__(
        self,
        error_code: int,
        message: str,
    ) -> None:
        """Initializes a new instance of the OrchestrationException class.

        Arguments:
            message {str} -- The error message.
        """
        super().__init__(error_code=error_code, message=message)


class TaskPluginException(OrchestrationException):
    """Throws while in __init__ when the specific plugins is unavailable."""

    def __init__(self, error_code: int, message: str):
        super().__init__(error_code=error_code, message=message)


class WorkflowExecuteException(JiuWenBaseException):
    """Workflow execute error."""

    ...
