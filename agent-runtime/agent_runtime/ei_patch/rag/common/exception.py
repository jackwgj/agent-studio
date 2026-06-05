#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2024-05-15
@LastEditTime: 2024-05-15 14:54:38
@Description: planner's exception
"""

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode


class PlannerConfigCheckFailedException(JiuWenBaseException):
    """Planner config check failed exception"""

    err_code = StatusCode.PLANNER_CONFIG_CHECK_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class PlannerPromptMissingException(JiuWenBaseException):
    """Planner prompt missing exception"""

    err_code = StatusCode.PLANNER_PROMPT_NAME_MISSING

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class PlannerModelMissingException(JiuWenBaseException):
    """Planner model missing exception"""

    err_code = StatusCode.PLANNER_MODEL_MISSING

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class LLMInvocationFailedException(JiuWenBaseException):
    """LLM invocation failed exception"""

    err_code = StatusCode.INVOKE_LLM_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class PlannerRegExpressionException(JiuWenBaseException):
    """Planner regular expression failed exception"""

    err_code = StatusCode.PLANNER_REGULAR_PARSE_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class PlannerIndexException(JiuWenBaseException):
    """Planner index failed exception"""

    err_code = StatusCode.PLANNER_INDEX_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class PlannerCompareExpressionException(JiuWenBaseException):
    """Planner compare expression failed exception"""

    err_code = StatusCode.PLANNER_COMPARE_PARSE_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class PromptFormatFail(JiuWenBaseException):
    """Prompt Template format failed exception"""

    err_code = StatusCode.PLANNER_PROMPT_FORMAT_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class PlannerTimeParseException(JiuWenBaseException):
    """Planner raised when there is an error parsing the time."""

    err_code = StatusCode.PLANNER_RAILS_EXECUTION_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class WorkflowParseException(JiuWenBaseException):
    err_code = StatusCode.PLANNER_WORKFLOW_PARSE_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )


class PanelWorkflowRunException(JiuWenBaseException):
    err_code = StatusCode.PLANNER_WORKFLOW_RUN_FAILED

    def __init__(self, message: str = None):
        super().__init__(
            error_code=self.err_code.value[0],
            message=(
                f"{self.err_code.value[1]}, root cause={message}"
                if message
                else f"{self.err_code.value[1]}"
            ),
        )
