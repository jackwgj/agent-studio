import json
from urllib.parse import urlencode, urljoin

import requests
from jiuwen.common.log.base import logger
from jiuwen.common.security.cryptor import Crypt


def auth(
    auth_credentials: dict, headers: dict, query_params: dict, context_id: str
) -> None:
    logger.info("start to execute auth credential!")
    scope = auth_credentials["scope"]

    # 公共请求方法
    def make_auth_request(url: str, payload: dict) -> dict:
        try:
            response = requests.post(
                url,
                data=json.dumps(payload),
                verify=False,
                headers={"Content-Type": "application/json"},
                timeout=100,
            )
            response.raise_for_status()  # 自动处理HTTP错误

            return response.json()
        except requests.exceptions.RequestException as e:
            logger.warning("Auth认证异常：%s", e)
            error_msg = f"API请求失败: {str(e)}"
            if e.response is not None:
                error_msg += f"\n状态码: {e.response.status_code}"
                try:
                    error_details = e.response.json()
                    error_msg += f"\n错误详情: {json.dumps(error_details, indent=2)}"
                except json.JSONDecodeError:
                    error_msg += f"\n响应内容: {e.response.text[:500]}"
            raise RuntimeError(error_msg)
        except json.JSONDecodeError:
            raise RuntimeError("API响应不是有效的JSON格式")

    # 认证类型路由
    if scope == "OAUTH":
        url = auth_credentials["endpointUrl"]
        client_secret = Crypt().decrypt(auth_credentials["clientSecret"])

        # 查询参数
        query_params = {
            "client_id": auth_credentials["clientId"],
            "client_secret": client_secret,
            "grant_type": "client_credentials",
        }
        url_with_params = urljoin(url, "?" + urlencode(query_params))

        # 发送认证请求
        auth_data = make_auth_request(url_with_params, payload={})
        headers["Authorization"] = "Bearer " + auth_data["access_token"]
