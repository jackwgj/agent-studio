#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""Template exception"""

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.prompt.common.exception.base import BasePromptException


class MetaTemplateNotSupportedException(BasePromptException):
    """meta template not support exception define

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_META_TEMPLATE_NOT_SUPPORTED_ERROR,
            message=message,
        )


class MetaTemplateNotExistException(BasePromptException):
    """meta template not exist exception define

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_META_TEMPLATE_NOT_EXIST_ERROR, message=message
        )


class MetaTemplateToolParseException(BasePromptException):
    """meta template tool parse exception define

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_META_TEMPLATE_TOOL_PARSE_ERROR, message=message
        )


class MetaTemplateBuildFailedException(BasePromptException):
    """meta template build failed exception define

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_META_TEMPLATE_BUILD_FAILED_ERROR, message=message
        )


class MetaTemplateMissingKeysException(BasePromptException):
    """meta template missing key exception define

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_META_TEMPLATE_MISSING_KEYS_ERROR, message=message
        )


class PromptLLMNotSupportedException(BasePromptException):
    """prompt llm not support exception define.

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_LLM_TYPE_NOT_SUPPORTED_ERROR, message=message
        )


class PromptLLMGenerationFailedException(BasePromptException):
    """prompt llm generation failed exception define.

    Args:
        message (str): exception message.
    """

    def __init__(self, message: str = None):
        super().__init__(
            err_code=StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR, message=message
        )
