#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
exceptions
"""

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode


class BaseJiuwenServerException(JiuWenBaseException):
    """
    基础服务器异常类，用于JiuWen服务中的异常处理。

    Attributes:
        err_code (StatusCode): 异常状态码。
        message (str): 异常信息。

    Raises:
        JiuWenBaseException: 如果发生未处理的异常。
    """

    def __init__(self, err_code: StatusCode, message: str = None):
        """
        初始化基础服务器异常类。

        Args:
            err_code (StatusCode): 异常状态码。
            message (str, optional): 异常信息。默认为None。

        Raises:
            JiuWenBaseException: 如果发生未处理的异常。
        """
        super().__init__(
            error_code=err_code.value[0],
            message=f"Server {err_code.value[1]}, root cause={message}"
            if message
            else f"{err_code.value[1]}",
        )


class ResourceNotFoundException(BaseJiuwenServerException):
    """
    资源未找到异常类，继承自BaseJiuwenServerException。

    当请求的资源未找到时抛出此异常。

    Attributes:
        message (str): 异常信息。
    """

    def __init__(self, message: str = None):
        """
        初始化资源未找到异常类。

        Args:
            message (str, optional): 异常信息。默认为None。
        """
        super().__init__(err_code=StatusCode.RESOURCE_NOT_FOUND_ERROR, message=message)


class AuthCheckFailedException(BaseJiuwenServerException):
    """
    授权检查失败异常类，继承自BaseJiuwenServerException。

    当授权检查失败时抛出此异常。

    Attributes:
        message (str): 异常信息。
    """

    def __init__(self, message: str = None):
        """
        初始化授权检查失败异常类。

        Args:
            message (str, optional): 异常信息。默认为None。
        """
        super().__init__(err_code=StatusCode.AUTH_CHECK_FAILED_ERROR, message=message)


class ParamCheckFailedException(BaseJiuwenServerException):
    """
    参数检查失败异常类，继承自BaseJiuwenServerException。

    当参数检查失败时抛出此异常。

    Attributes:
        message (str): 异常信息。
    """

    def __init__(self, message: str = None):
        """
        初始化参数检查失败异常类。

        Args:
            message (str, optional): 异常信息。默认为None。
        """
        super().__init__(err_code=StatusCode.PARAM_CHECK_FAILED_ERROR, message=message)
