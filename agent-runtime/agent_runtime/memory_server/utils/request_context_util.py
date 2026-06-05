import contextvars
from typing import Optional

from fastapi import Request
from memory_service.i18n.i18n_constant import Locale
from memory_service.utils.iam_service import TokenResult

"""
存储当前请求对象的上下文变量。

类型: ContextVar[Request | None]
默认值: None
"""
request_context: contextvars.ContextVar[Request | None] = contextvars.ContextVar(
    "request", default=None
)

token_result_context: contextvars.ContextVar[Optional[TokenResult]] = (
    contextvars.ContextVar("auth_token", default=None)
)

language_context: contextvars.ContextVar[Optional[Locale]] = contextvars.ContextVar(
    "language", default=Locale.ZH_CN
)


def get_request_auth_token() -> str:
    return request_context.get().headers.get("X-Auth-Token", "")


def _get_token_result() -> TokenResult:
    return token_result_context.get()


def get_user_domain_id() -> str:
    token_result = _get_token_result()
    if token_result.domain is not None:
        return token_result.domain.id
    if token_result.project is not None and token_result.project.domain is not None:
        return token_result.project.domain.id
    return ""


def get_user_id() -> str:
    token_result = _get_token_result()
    if token_result.user is not None:
        return _get_token_result().user.id
    return ""


def get_request_project_id() -> str:
    token_result = _get_token_result()
    if token_result.project is not None:
        return token_result.project.id
    return ""
