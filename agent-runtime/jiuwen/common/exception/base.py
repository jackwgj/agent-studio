#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""Declaration of the exception handling base class"""

from dataclasses import dataclass

from jiuwen.common.exception.status_code import StatusCode

MAGIC_CODE = "\t"


# This magic code is used for identify JiuWenBaseException thrown in TGF runtime.


class JiuWenException(Exception):
    """JiuWen异常类，继承自Python的基类Exception。

    用于表示JiuWen相关的错误，并提供初始化方法设置错误信息和其他参数。
    """

    def __init__(self, message: str, *args, **kwargs) -> None:
        """初始化JiuWenException类的新实例。

        Args:
            message (str): 错误信息，提供关于错误的详细描述。
            *args: 可变参数，用于传递额外的非关键字参数。
            **kwargs: 可变关键字参数，用于传递额外的关键字参数。

        Returns:
            None
        """
        super().__init__(message, args, kwargs)


class JiuWenBaseException(Exception):
    """
    JiuWen异常基类，包含状态码。
    """

    def __init__(
        self,
        error_code: int,
        message: str,
        node_id: str = None,
        node_name: str = None,
        node_type: str = None,
    ) -> None:
        """
        用于表示JiuWen相关的错误，并提供初始化方法设置错误代码、错误信息和其他参数。

        Args:
            error_code (int): 错误代码，表示错误的状态码。
            message (str): 错误信息，提供关于错误的详细描述。
            node_id (str, optional): 节点ID，用于标识发生错误的节点。默认为None。
            node_name (str, optional): 节点名称，用于标识发生错误的节点。默认为None。
            node_type (str, optional): 节点类型，用于标识发生错误的节点类型。默认为None。

        Raises:
            Exception: 继承自Python的基类Exception。
        """
        super().__init__(error_code, message)
        self._error_code = error_code
        self._message = message
        self.node_id = node_id
        self.node_name = node_name
        self.node_type = node_type

    def __str__(self):
        """
        返回异常的字符串表示，包含错误代码和错误信息。

        Returns:
            str: 异常的字符串表示。
        """
        return f"[{self._error_code}]{self._message}{MAGIC_CODE}"

    @property
    def error_code(self):
        """
        返回错误代码。

        Returns:
            int: 错误代码。
        """
        return self._error_code

    @property
    def message(self):
        """
        返回错误信息。

        Returns:
            str: 错误信息。
        """
        return self._message


@dataclass
class ErrorBody:
    error_message: str
    error_code: str

    def to_alias_dict(self):
        """to alias dict"""
        return {"errorMessage": self.error_message, "errorCode": self.error_code}


@dataclass
class InnerException:
    is_success: bool
    error_body: ErrorBody = None

    def to_alias_dict(self):
        """to alias dict"""
        return {
            "isSuccess": self.is_success,
            "errorBody": self.error_body.to_alias_dict() if self.error_body else None,
        }


class InterruptException(JiuWenBaseException):
    """InterruptException class with status codes"""

    def __init__(self, error_code: int, message: str) -> None:
        super().__init__(error_code, message)
        self._error_code = error_code
        self._message = message


class WorkflowAbortException(JiuWenBaseException):
    """异常结束组件抛出的异常，用于终止流程"""

    def __init__(
        self,
        data: dict,
        node_id: str = None,
        node_name: str = None,
        node_type: str = None,
    ) -> None:
        super().__init__(
            StatusCode.WORKFLOW_EXCEPTION_END_ERROR.code,
            StatusCode.WORKFLOW_EXCEPTION_END_ERROR.errmsg,
            node_id=node_id,
            node_name=node_name,
            node_type=node_type,
        )
        self.data = data

    def to_payload(self) -> dict:
        """
        Returns:
            dict: 包含异常信息的字典。
        """
        return self.data
