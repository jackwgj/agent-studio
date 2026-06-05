import json

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

            if scope == "CUSTOM_IAM":
                return {"X-Subject-Token": response.headers["X-Subject-Token"]}
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
    if scope == "HIS_IAM":
        # 解密密钥
        secret = Crypt().decrypt(auth_credentials["secret"])

        # 构造HIS_IAM认证请求
        auth_data = make_auth_request(
            url=auth_credentials["url"],
            payload={
                "data": {
                    "type": "token",
                    "attributes": {
                        "account": auth_credentials["account"],
                        "secret": secret,
                        "project": auth_credentials["project"],
                        "enterprise": auth_credentials["enterprise"],
                    },
                }
            },
        )
        headers["Authorization"] = auth_data["access_token"]

    elif scope == "SGOV":
        credential = Crypt().decrypt(auth_credentials["credential"])
        # 构造SGOV认证请求
        auth_data = make_auth_request(
            url=auth_credentials["url"],
            payload={"appId": auth_credentials["appId"], "credential": credential},
        )
        headers["Authorization"] = auth_data["result"]
    elif scope == "CUSTOM_IAM":
        if "iamPassword" in auth_credentials and auth_credentials["iamPassword"]:
            password = Crypt().decrypt(auth_credentials["iamPassword"])
            # 使用密码认证
            payload = {
                "auth": {
                    "identity": {
                        "password": {
                            "user": {
                                "name": auth_credentials["iamUser"],
                                "password": password,
                                "domain": {"name": auth_credentials["iamDomain"]},
                            }
                        },
                        "methods": ["password"],
                    },
                    "scope": {"project": {"name": auth_credentials["iamProject"]}},
                }
            }
        else:
            sk = Crypt().decrypt(auth_credentials["iamSK"])
            payload = {
                "auth": {
                    "identity": {
                        "hw_ak_sk": {
                            "access": {"key": auth_credentials["iamAK"]},
                            "secret": {"key": sk},
                        },
                        "methods": ["hw_ak_sk"],
                    },
                    "scope": {"project": {"name": auth_credentials["iamProject"]}},
                }
            }
        # 发送认证请求
        auth_data = make_auth_request(url=auth_credentials["iamUrl"], payload=payload)
        headers["X-Auth-Token"] = auth_data["X-Subject-Token"]
