#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""Exception classes defined for assemble module."""

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.prompt.common.exception.base import BasePromptException


class VariableInitError(BasePromptException):
    """Wrong arguments for initializing the variable."""

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_ASSEMBLER_VARIABLE_INIT_ERROR, message=message
        )


class AssemblerInitError(BasePromptException):
    """Wrong arguments for initializing the assembler."""

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_ASSEMBLER_INIT_ERROR, message=message
        )


class InputKeyError(BasePromptException):
    """Missing or unexpected key-value pairs passed in as arguments for the assembler or variable when updating."""

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_ASSEMBLER_INPUT_KEY_ERROR, message=message
        )


class TemplateFormatError(BasePromptException):
    """Errors occur when formatting the template content due to wrong format."""

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_ASSEMBLER_TEMPLATE_FORMAT_ERROR, message=message
        )
