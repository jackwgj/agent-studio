import logging

from starlette.middleware.base import BaseHTTPMiddleware

logger = logging.getLogger(__name__)
from starlette.requests import Request
from starlette.responses import Response

from memory_service.utils.request_context_util import language_context
from memory_service.i18n.i18n_constant import normalize_locale


class LanguageFilterMiddleware(BaseHTTPMiddleware):
    def __init__(self, app):
        super().__init__(app)

    async def dispatch(self, request: Request, call_next) -> Response:
        language = request.headers.get("X-Language")
        locale = normalize_locale(language)
        language_context.set(locale)
        return await call_next(request)
