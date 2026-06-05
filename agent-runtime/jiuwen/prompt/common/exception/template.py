#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""Template exception"""

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.prompt.common.exception.base import BasePromptException


class TemplateDuplicatedException(BasePromptException):
    """
    Template Duplicated Exception define

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_TEMPLATE_DUPLICATED_ERROR, message=message
        )


class TemplateNotFoundException(BasePromptException):
    """
    Template Not Found Exception define

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_TEMPLATE_NOT_FOUND_ERROR, message=message
        )
