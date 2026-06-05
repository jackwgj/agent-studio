from enum import Enum

from fastapi import status


class ErrorCode(Enum):
    """错误码枚举类"""

    # 记忆模块的错误码
    memory_common_error = "0330"

    # 服务内部错误
    SERVER_INTERNAL_ERROR = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "0000",
    )

    # 向量化模块错误码
    EMBEDDING_REQUEST_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "0001",
    )
    EMBEDDING_RESPONSE_PARSE_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "0002",
    )
    EMBEDDING_MODEL_RETURN_ERROR_DATA = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "0003",
    )
    EMBEDDING_DIMENSION_MISMATCH = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "0004",
    )

    # 向量存储模块错误码
    VECTOR_STORE_ADD_DOCUMENT_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "0005",
    )
    VECTOR_STORE_SEARCH_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "0006",
    )
    VECTOR_STORE_DELETE_DOCUMENTS_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "0007",
    )

    # 业务通用错误码
    INVALID_PARAMETER = (status.HTTP_400_BAD_REQUEST, memory_common_error, "1001")
    NO_PERMISSION = (
        status.HTTP_403_FORBIDDEN,
        memory_common_error,
        "1002",
    )
    RESOURCE_NOT_EXIST = (status.HTTP_404_NOT_FOUND, memory_common_error, "1003")
    AUTH_TOKEN_REQUIRED = (status.HTTP_401_UNAUTHORIZED, memory_common_error, "1004")
    AUTH_TOKEN_INVALID = (status.HTTP_401_UNAUTHORIZED, memory_common_error, "1005")

    # 长期记忆索引模块错误码
    LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "1006",
    )
    LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "1007",
    )
    LONG_TERM_MEMORY_SEARCH_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "1008",
    )
    LONG_TERM_MEMORY_GET_BY_ID_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "1009",
    )

    # 记忆管理私有API模块错误码
    EXTRACT_MEMORY_FAILED = (
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        memory_common_error,
        "1010",
    )

    @property
    def http_code(self) -> int:
        """HTTP状态码"""
        return int(self.value[0])

    @property
    def module_code(self) -> str:
        """HTTP状态码"""
        return self.value[1]

    @property
    def full_code(self) -> str:
        """错误码"""
        return f"AgentBuilder.{self.value[1]}{self.value[2]}"
