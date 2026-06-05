import logging

from starlette.middleware.base import BaseHTTPMiddleware

logger = logging.getLogger(__name__)
from starlette.requests import Request
from starlette.responses import Response
import re
from typing import List

from memory_service.exception.common_response import CommonErrorResponse
from memory_service.exception.error_code import ErrorCode
from memory_service.utils.iam_service import token_result_cache
from memory_service.utils.request_context_util import (
    request_context,
    token_result_context,
)
from memory_service.i18n.i18n_util import i18n_util
from memory_service.i18n.i18n_constant import I18nMessageCode


class IamAuthFilterMiddleware(BaseHTTPMiddleware):
    def __init__(self, app, whitelist_routes: List[str] = None):
        super().__init__(app)
        self.whitelist_routes = whitelist_routes or []

    async def dispatch(self, request: Request, call_next) -> Response:
        # 获取当前请求路径
        current_route = request.url.path
        request_context.set(request)
        # 检查白名单
        if any(
            self.pattern_match(current_route, pattern)
            for pattern in self.whitelist_routes
        ):
            logger.info(f"uri in white list, ignore check iam_token: {current_route}")
            return await call_next(request)

        # 校验IAM Token的有效性
        auth_token = request.headers.get("X-Auth-Token")
        if not auth_token:
            from starlette.responses import JSONResponse

            error_response = CommonErrorResponse(
                error_code=ErrorCode.AUTH_TOKEN_REQUIRED.full_code,
                error_msg=i18n_util.get_message(I18nMessageCode.MISSING_TOKEN_MESSAGE),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.MISSING_TOKEN_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.MISSING_TOKEN_SUGGESTION
                ),
            )
            return JSONResponse(
                content=error_response.model_dump(),
                status_code=ErrorCode.AUTH_TOKEN_REQUIRED.http_code,
            )
        try:
            token_result = await token_result_cache.get(auth_token)
            token_result_context.set(token_result)
        except Exception as e:
            logger.error(
                f"analyze iam auth token for request {request.url} failed: %s",
                e,
                exc_info=True,
            )
            from starlette.responses import JSONResponse

            error_response = CommonErrorResponse(
                error_code=ErrorCode.AUTH_TOKEN_REQUIRED.full_code,
                error_msg=i18n_util.get_message(
                    I18nMessageCode.TOKEN_VALIDATE_ERROR_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.TOKEN_VALIDATE_ERROR_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.TOKEN_VALIDATE_ERROR_SUGGESTION
                ),
            )
            return JSONResponse(
                content=error_response.model_dump(),
                status_code=ErrorCode.AUTH_TOKEN_REQUIRED.http_code,
            )
        return await call_next(request)

    @staticmethod
    def pattern_match(path: str, pattern: str) -> bool:
        """支持通配符匹配"""
        regex_pattern = pattern.replace("*", ".*").replace("?", ".?")
        return bool(re.match(f"^{regex_pattern}$", path))
