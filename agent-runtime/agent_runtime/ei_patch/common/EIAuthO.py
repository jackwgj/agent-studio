import datetime
import os
import traceback
from functools import wraps

import requests
from cachetools import TTLCache
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.serve.common.context import (
    g as current_g,
)  # 基本等效于 flask.g, 但要先执行 .get
from jiuwen.serve.common.context import (
    request as current_request,
)  # 基本等效于 flask.request, 但要先执行 .get

# token 缓存； 最大1000条， ttl 25小时（略大于token有效期 24小时）
iam_token_cache = TTLCache(1000, 25 * 60 * 60)


class AuthorizationError(JiuWenBaseException):
    """This exception is thrown when the authentication fails."""

    ...


class AuthException(Exception):
    """认证/鉴权异常类"""

    def __init__(self, message=""):
        self.message = message
        Exception.__init__(self, message)


class IamClient:
    """对接IAM"""

    # 校验token有效性
    def auth_token(self, token) -> dict:
        # token是否合法
        decode_token = self.decode_token(token)

        # 是否过期
        expire_at = datetime.datetime.strptime(
            decode_token.get("expires_at"), "%Y-%m-%dT%H:%M:%S.%fZ"
        )
        if expire_at < datetime.datetime.utcnow():
            raise AuthException(message="invalid token")
        return decode_token

    # 校验token 是否合法
    def decode_token(self, token) -> dict:
        if iam_token_cache.get(token):
            return iam_token_cache.get(token)

        headers = {"X-Auth-Token": token, "X-Subject-Token": token}

        if "/v1/token/resolve" in os.environ["IAM_ENDPOINT"]:
            url = os.environ["IAM_ENDPOINT"]
        else:
            url = os.environ["IAM_ENDPOINT"] + "/v3/auth/tokens"

        response = requests.get(
            url, headers=headers, timeout=5, verify=bool(os.environ.get("IAM_VERIFY"))
        )
        if response.status_code > 300:
            resp = response.json()
            message = (
                "[ {} ] {}".format(resp["error"]["code"], resp["error"]["message"])
                if resp.get("error")
                else "[ {} ] {}".format(resp["error_code"], resp["error_msg"])
            )
            raise AuthException(message=message)
        else:
            iam_token_cache[token] = response.json()["token"]
            return response.json()["token"]


def _not_equal_or_blank(src: str, dst: str):
    """判断两个值中存在空值或不相等"""
    return not src or not dst or src != dst


class EIAuth:
    @staticmethod
    def no_authenticate(func):
        """认证"""

        @wraps(func)
        def decorator(*args, **kwargs):
            try:
                headers = current_request.get().headers
                # 信息传递给后端
                current_g.get().update(
                    {"project_id": headers.get("x-owner-project-id", "0")}
                )
                current_g.get().update(
                    {"domain_id": headers.get("x-owner-domain-id", "0")}
                )
            except Exception as e:
                logger.error(
                    "Authentication failed: %s",
                    "".join(traceback.format_exception(None, e, e.__traceback__)),
                )
                # Convert to upstream framework error code
                raise AuthorizationError(
                    error_code=StatusCode.AUTH_CHECK_FAILED_ERROR.code,
                    message="unknown error",
                )

            return func(*args, **kwargs)

        return decorator

    @staticmethod
    def authenticate(func):
        """认证"""

        @wraps(func)
        def decorator(*args, **kwargs):
            try:
                # token合法性校验，同时提取token信息。
                decode_token = IamClient().auth_token(
                    current_request.get().headers.get("x-auth-token")
                )

                # 信息传递给后端
                current_g.get().update(
                    {"project_id": decode_token.get("project").get("id")}
                )
                current_g.get().update(
                    {
                        "domain_id": decode_token.get("user", {})
                        .get("domain", {})
                        .get("id", "")
                    }
                )
            except AuthException as e:
                logger.error(
                    "Authentication failed: %s",
                    "".join(traceback.format_exception(None, e, e.__traceback__)),
                )
                # Convert to upstream framework error code
                raise AuthorizationError(
                    error_code=StatusCode.AUTH_CHECK_FAILED_ERROR.code,
                    message=e.message,
                )
            except AuthorizationError as e:
                logger.error(
                    "Authentication failed: %s",
                    "".join(traceback.format_exception(None, e, e.__traceback__)),
                )
                # Convert to upstream framework error code
                raise AuthorizationError(
                    error_code=StatusCode.AUTH_CHECK_FAILED_ERROR.code,
                    message=e.message,
                )
            except Exception as e:
                logger.error(
                    "Authentication failed: %s",
                    "".join(traceback.format_exception(None, e, e.__traceback__)),
                )
                # Convert to upstream framework error code
                raise AuthorizationError(
                    error_code=StatusCode.AUTH_CHECK_FAILED_ERROR.code,
                    message="unknown error",
                )

            return func(*args, **kwargs)

        return decorator
