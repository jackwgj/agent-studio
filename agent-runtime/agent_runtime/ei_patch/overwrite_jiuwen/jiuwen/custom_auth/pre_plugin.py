import json
import os

import requests
from jiuwen.common.log.base import logger

AGENT_MANAGER_HOST = os.getenv("AGENT_MANAGER_HOST", "")


# 定义配额不足的异常类
class FreeQuotaExceededError(Exception):
    """免费配额不足异常"""

    def __init__(
        self,
        message="Failed to invoke the API because the free quota is insufficient.",
        code=105005,
    ):
        self.message = message
        self.code = code  # 自定义错误码
        super().__init__(self.message)

    def __str__(self):
        return f"[错误码 {self.code}] {self.message}"


def auth(
    auth_credentials: dict, headers: dict, query_params: dict, context_id: str, **kwargs
) -> None:
    logger.info("start to execute auth credential!")
    domain_id = auth_credentials.get("domainId")
    limit = auth_credentials.get("limit")
    usage = auth_credentials.get("usage")
    base_url = AGENT_MANAGER_HOST
    project_id = auth_credentials.get("projectId")
    workspace_id = auth_credentials.get("workspaceId")
    isfree = headers.get("x-asset-app-invoke-in-free")
    if kwargs.get("runtime_context") is not None:
        header = kwargs.get("runtime_context").api_config.get("headers")
    else:
        header = kwargs.get("runtime_auth").get("headers")
    token = header.get("x-auth-token")

    if isfree == "false":
        return  # 预置模板调用插件 不消耗次数

    if not token:
        raise ValueError("x-auth-token header is missing")
    # 参数验证
    if not all([domain_id, base_url is not None, project_id is not None]):
        raise ValueError(
            "auth_credentials must contain domainId, base_url, and project_id"
        )

    if usage >= limit:
        logger.error(
            f"Usage ({usage}) exceeds limit ({limit}), calling freetrialincrement API"
        )

    query_params = {"plugin_id": context_id, "workspace_id": workspace_id}

    # 公共请求方法
    def make_auth_request(url: str, params: dict = None) -> dict:
        """
        发送认证请求（只有query参数，没有body数据）

        Args:
            url: 请求URL
            params: URL query参数（字典形式）
        """
        try:
            response = requests.post(
                url,
                params=params,  # 直接传字典，requests会自动转换为?key=value格式
                verify=False,
                headers={"Content-Type": "application/json", "x-auth-token": token},
                timeout=100,
            )
            response.raise_for_status()  # 自动处理HTTP错误
            return response.json()

        except requests.exceptions.RequestException as e:
            logger.error("Auth认证异常：", e)
            error_msg = f"API请求失败: {str(e)}"
            if e.response is not None:
                error_msg += f"\n状态码: {e.response.status_code}"
                try:
                    error_details = e.response.json()
                    error_msg += f"\n错误详情: {json.dumps(error_details, indent=2, ensure_ascii=False)}"
                except json.JSONDecodeError:
                    error_msg += f"\n响应内容: {e.response.text[:500]}"
            raise RuntimeError(error_msg)
        except json.JSONDecodeError:
            raise RuntimeError("API响应不是有效的JSON格式")
        except Exception as e:
            raise RuntimeError(f"认证过程中发生未知错误: {str(e)}")

    # 构建完整请求URL
    url = f"{base_url}/v1/{project_id}/agent-manager/plugins/{domain_id}/freetrialincrement"

    # 发送认证请求
    try:
        response_data = make_auth_request(url=url, params=query_params)
        # 处理响应数据
        logger.info("Request succeeded.:", response_data)
        logger.info(
            f"freetrialincrement API response: {json.dumps(response_data, indent=2)}"
        )
        if response_data.get("code") == 0:
            logger.error("Insufficient free quota, call failed.")
            raise FreeQuotaExceededError(
                "The free quota has been used up. Please configure authentication yourself."
            )

    except RuntimeError as e:
        logger.error(f"Failed to invoke the API freetrialincrement : {e}")
        # 根据需要处理错误
        raise  # 或者返回错误信息
