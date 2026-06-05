#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""Connection pool"""

import asyncio
import os

from aiohttp import TCPConnector
from jiuwen.common.configs.env_constants import PLUGIN_SSL_API_CERT_KEY
from jiuwen.common.log.base import logger
from jiuwen.common.utils.singleton import Singleton
from jiuwen.common.utils.utils import safe_json_loads


def _get_ssl_config() -> bool:
    """获取SSL配置，确保返回bool类型，默认为True，解析失败时返回True"""
    api_cert_env = os.environ.get(PLUGIN_SSL_API_CERT_KEY)
    if not api_cert_env:
        return True

    api_cert = safe_json_loads(api_cert_env, True)

    if isinstance(api_cert, bool):
        return api_cert

    # 类型不匹配，返回True并记录警告
    logger.warning(
        f"SSL config must be bool, key: {PLUGIN_SSL_API_CERT_KEY} got {type(api_cert)}, using True"
    )
    return True


class Connector(metaclass=Singleton):
    """
    单例模式的Connector类，用于管理TCP连接池。

    在初始化时，根据全局事件循环的状态，决定是否创建一个TCPConnector实例。
    提供获取TCP连接器和关闭连接器的方法。

    使用了Singleton元类，确保整个应用程序中只有一个Connector实例。
    """

    def __init__(self):
        """
        初始化Connector实例。

        如果当前的事件循环是全局的，则根据SSL配置创建一个TCPConnector。
        """
        self.tcp_connector = None
        loop = asyncio.get_event_loop()
        if getattr(loop, "_is_global", False):
            api_cert = _get_ssl_config()
            self.tcp_connector = TCPConnector(ssl=api_cert)

    def get_tcp_connector(self):
        """
        获取TCP连接器。

        如果当前的事件循环是全局的，则返回TCP连接器实例，否则返回None。

        Returns:
            TCPConnector或None: 返回TCP连接器实例或None。
        """
        loop = asyncio.get_running_loop()
        if getattr(loop, "_is_global", False):
            return self.tcp_connector
        return None

    async def close(self):
        """
        异步关闭TCP连接器。

        如果TCP连接器存在，则异步关闭它。
        """
        if self.tcp_connector:
            await self.tcp_connector.close()
