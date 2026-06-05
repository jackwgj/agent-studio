#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""base exception for promptengine"""

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode


class BasePromptException(JiuWenBaseException):
    """
    Base Prompt Exception
    """

    def __init__(self, err_code: StatusCode, message: str = None):
        super().__init__(
            error_code=err_code.value[0],
            message=(
                f"{err_code.value[1]}, root cause={message}"
                if message
                else f"{err_code.value[1]}"
            ),
        )


class NetworkError(BasePromptException):
    """HTTP request exception"""

    def __init__(self, message: str = None):
        super().__init__(err_code=StatusCode.HTTP_NET_WORK_ERROR, message=message)
