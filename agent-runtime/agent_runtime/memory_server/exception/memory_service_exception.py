"""
MemoryServiceException - Memory服务自定义异常类

此模块定义了Memory服务使用的自定义异常类，用于封装业务逻辑中出现的各种错误情况。
"""

from typing import Optional

from memory_service.exception.error_code import ErrorCode


class MemoryServiceException(Exception):
    """
    Memory服务自定义异常类

    用于封装业务逻辑中出现的各种错误情况，包含错误码、错误信息、
    错误原因和修复建议等相关信息。

    Attributes:
        error_code (ErrorCode): 错误码枚举
        error_message (str): 错误信息
        error_reason (Optional[str]): 错误原因
        error_suggestion (Optional[str]): 修复建议
    """

    def __init__(
        self,
        error_code: ErrorCode,
        error_message: str,
        error_reason: Optional[str] = None,
        error_suggestion: Optional[str] = None,
    ):
        """
        初始化MemoryServiceException

        Args:
            error_code: 错误码枚举
            error_message: 自定义错误信息
            error_reason: 错误原因
            error_suggestion: 修复建议
        """
        self.error_code = error_code
        self.error_reason = error_reason
        self.error_suggestion = error_suggestion
        self.error_message = error_message

        # 构造完整的错误信息
        super().__init__(self._build_full_message())

    def _build_full_message(self) -> str:
        """
        构造完整的错误信息

        Returns:
            str: 包含错误码、错误信息和可能原因的完整信息
        """
        parts = [f"[{self.error_code.full_code}] {self.error_message}"]
        if self.error_reason:
            parts.append(f"可能原因: {self.error_reason}")
        return " | ".join(parts)

    def __str__(self) -> str:
        """返回异常的字符串表示"""
        return self.error_message

    def __repr__(self) -> str:
        """返回异常的详细字符串表示"""
        return (
            f"MemoryServiceException("
            f"error_code={self.error_code.full_code}, "
            f"error_message='{self.error_message}', "
            f"http_status={self.error_code.http_code}, "
            f"reason='{self.error_reason or 'N/A'}', "
            f"suggestion='{self.error_suggestion or 'N/A'}')"
        )
