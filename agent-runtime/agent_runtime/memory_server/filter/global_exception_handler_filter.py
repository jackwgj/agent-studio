import logging

from starlette.middleware.base import BaseHTTPMiddleware

# 创建logger实例
logger = logging.getLogger(__name__)
from starlette.requests import Request
from starlette.responses import JSONResponse

from memory_service.exception.common_response import CommonErrorResponse
from memory_service.exception.memory_service_exception import MemoryServiceException
from memory_service.exception.error_code import ErrorCode
from memory_service.i18n.i18n_util import i18n_util
from memory_service.i18n.i18n_constant import I18nMessageCode


class GlobalExceptionMiddleware(BaseHTTPMiddleware):
    def __init__(self, app):
        super().__init__(app)

    async def dispatch(self, request: Request, call_next):
        try:
            response = await call_next(request)
            return response
        except MemoryServiceException as exc:
            # 处理自定义异常
            logger.error(
                "MemoryServiceException occurred: %s",
                exc,
                exc_info=True,
                extra={
                    "error_code": exc.error_code.full_code,
                    "http_code": exc.error_code.http_code,
                    "path": request.url.path,
                    "method": request.method,
                },
            )
            return JSONResponse(
                status_code=exc.error_code.http_code,
                content=CommonErrorResponse(
                    error_code=exc.error_code.full_code,
                    error_msg=exc.error_message,
                    error_reason=exc.error_reason,
                    error_suggestion=exc.error_suggestion,
                ).model_dump(),
            )
        except Exception as exc:
            # 处理其他异常
            logger.error(
                "Unhandled system exception occurred: %s",
                exc,
                exc_info=True,
                extra={
                    "path": request.url.path,
                    "method": request.method,
                    "exception_type": type(exc).__name__,
                    "user_agent": request.headers.get("user-agent"),
                },
            )
            return JSONResponse(
                status_code=ErrorCode.SERVER_INTERNAL_ERROR.http_code,
                content=CommonErrorResponse(
                    error_code=ErrorCode.SERVER_INTERNAL_ERROR.full_code,
                    error_msg=i18n_util.get_message(
                        I18nMessageCode.SYSTEM_ERROR_MESSAGE
                    ),
                    error_reason=i18n_util.get_message(
                        I18nMessageCode.SYSTEM_ERROR_REASON
                    ),
                    error_suggestion=i18n_util.get_message(
                        I18nMessageCode.SYSTEM_ERROR_SUGGESTION
                    ),
                ).model_dump(),
            )
