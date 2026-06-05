#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Exception 类"""

from enum import Enum

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode


class ErrorCode(Enum):
    """ErrorCode"""

    # 插件管理 105000~105999
    # 对现有代码进行适配
    SUCCESS = StatusCode.SUCCESS.code
    UNEXPECTED_ERROR = StatusCode.PLUGIN_UNEXPECTED_ERROR.code
    PLUGIN_ALREADY_EXISTS = StatusCode.PLUGIN_ALREADY_EXISTS.code
    PLUGIN_NOT_FOUND = StatusCode.PLUGIN_NOT_FOUND.code
    PLUGIN_EMBEDDING_ERROR = StatusCode.PLUGIN_EMBEDDING_ERROR.code
    PLUGIN_PARAMS_CHECK_ERROR = StatusCode.PLUGIN_PARAMS_CHECK_FAILED.code


class PluginCommonException(JiuWenBaseException):
    """Plugin Common Exception"""

    def __init__(
        self, code: StatusCode = StatusCode.PLUGIN_UNEXPECTED_ERROR, message: str = ""
    ):
        super().__init__(code.code, message=f"{code.errmsg}, root cause = {message}")

    def __str__(self):
        return f"<{type(self).__name__}: (code={self.code}, message={self.message})>"

    @property
    def code(self):
        """exception code"""
        return self._error_code

    @property
    def message(self):
        """exception message"""
        return self._message


class ValidationException(JiuWenBaseException):
    """ValidationException"""

    def __init__(self, message: str = None):
        super().__init__(
            StatusCode.PLUGIN_VALID.code,
            f"{StatusCode.PLUGIN_VALID.errmsg}, root case={message}",
        )

    def __str__(self):
        return f"<{type(self).__name__}: (message={self._message})>"

    @property
    def msg(self):
        """exception message"""
        return self._message


class PluginEmbeddingException(PluginCommonException):
    """PluginEmbeddingException"""

    def __init__(
        self, code: StatusCode = StatusCode.PLUGIN_EMBEDDING_ERROR, message: str = ""
    ):
        super(PluginEmbeddingException, self).__init__(code, message)


class PluginNotFoundException(PluginCommonException):
    """PluginNotFoundException"""

    def __init__(
        self, code: StatusCode = StatusCode.PLUGIN_NOT_FOUND, message: str = ""
    ):
        super(PluginNotFoundException, self).__init__(code, message)


class PluginDatabaseException(PluginCommonException):
    """PluginDatabaseException"""

    def __init__(
        self, code: StatusCode = StatusCode.PLUGIN_DATABASE_EXCEPTION, message: str = ""
    ):
        super(PluginDatabaseException, self).__init__(code, message)


class PluginExecuteSqlException(PluginCommonException):
    """PluginExecuteSqlException"""

    def __init__(
        self,
        code: StatusCode = StatusCode.PLUGIN_EXECUTE_SQLEXCEPTION,
        message: str = "",
    ):
        super(PluginExecuteSqlException, self).__init__(code, message)


class PluginParamException(PluginCommonException):
    """PluginParamException"""

    def __init__(
        self,
        code: StatusCode = StatusCode.PLUGIN_PARAMS_CHECK_FAILED,
        message: str = "",
    ):
        super(PluginParamException, self).__init__(code, message)


class PluginRestfulAuthException(PluginCommonException):
    """PluginParamException"""

    def __init__(
        self,
        code: StatusCode = StatusCode.PLUGIN_RESTFULAPI_AUTH_ERROR,
        message: str = "",
    ):
        super(PluginRestfulAuthException, self).__init__(code, message)


class ExceptionsMessage:
    """ExceptionsMessage"""

    QueryMissingErrorJson = "user query is missing"
    DescMissingErrorJson = "plugin description is missing"
    NotFoundErrorJson = "plugin does not exist"
    EmbeddingErrorJson = "query_embedding failed"
    AlreadyExistErrorJson = "plugin already exists"
    ParameterErrorJson = "missing required parameters"
    ParameterTooLongJson = "parameters too long"
    DatabaseConnectionErrorJson = "connect database failed"
    ExecuteSqlErrorJson = "execute sql failed"
    UnknownErrorJson = "unknown error"
    PluginFileErrorJson = "plugin file category is error"
    TypeError = "the input args type is error"
    HttpMethodError = " the http method is error"
    ParamsInconsistentErrorJson = (
        "function parameters are inconsistent with defined parameters"
    )
    OpenAIFunctionErrorJson = "open ai function json is not correct"


class ValidatorErrorMsg:
    """ValidatorErrorMsg"""

    ExtraFieldsErrorMsg = "extra fields not permitted"
    FieldRequiredErrorMsg = "field required"
    ValidateMinLengthErrorMsg = "ensure this value has at least {} characters"
    ValidateMaxLengthErrorMsg = "ensure this value has at most {} characters"
    TypeErrorMsg = "type error"
    AttributeErrorMsg = "{} object has no field {}"


class CommonMessage:
    """CommonMessage"""

    SuccessMsg = "success"
