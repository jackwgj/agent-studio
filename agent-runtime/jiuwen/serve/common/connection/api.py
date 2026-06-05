# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Default persistent connection server interface.

Creates and obtains the default persistent connection server, and registers
callback functions with it for bidirectional communication.
"""

__all__ = (
    "start_connection_server",
    "get_connection_server",
    "register_connection_callback",
)

from ssl import SSLContext
from typing import Optional, TYPE_CHECKING, Union

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.serve.common.connection.base import ConnectionServerException
from jiuwen.serve.common.ssl_ctx import ContexConfigDict, create_context

if TYPE_CHECKING:
    from jiuwen.serve.common.connection.base import BaseServer, Config, ConnFunction

_current_server: Optional["BaseServer"] = None
_global_callback: "dict[str, ConnFunction]" = dict()


def start_connection_server(
    config: "Config", ssl_context: Union[SSLContext, ContexConfigDict] = None, **_
) -> None:
    """
    根据参数`config`创建一个新的服务器，并用它替换默认服务器。

    功能描述：
    此函数根据提供的配置创建一个特定类型的连接服务器，并替换全局的当前服务器实例。

    Args:
        config (Config): 服务器配置字典，必须包含"mode"键。
        ssl_context (Union[SSLContext, ContexConfigDict], 可选): SSL上下文或上下文配置字典。默认为None。
        **_ (可变关键字参数): 其他可选参数。

    Returns:
        无返回值。

    Raises:
        NotImplementedError: 如果服务器模式不是"ws"或"wss"，则抛出此异常。

    Notes:
        - 函数首先检查`config`是否为字典且包含"mode"键，如果不满足条件，记录日志并返回。
        - 根据`config`中的"mode"值，调用`_check_ssl_context`函数检查和处理SSL上下文。
        - 如果模式为"ws"或"wss"，则创建一个`WsServer`实例并将其设置为当前服务器。
        - 如果模式不是"ws"或"wss"，则抛出`NotImplementedError`异常。
    """
    if not isinstance(config, dict) or not config.get("mode"):
        logger.info("Connection server is configured to be disabled.")
        return
    mode = config.get("mode")
    ssl_context = _check_ssl_context(mode, ssl_context)
    if mode in ("ws", "wss"):
        global _current_server
        from jiuwen.serve.common.connection.ws import WsServer

        _current_server = WsServer(config, ssl_context, function_dict=_global_callback)
        return
    raise NotImplementedError


def get_connection_server() -> "BaseServer":
    """Get the running server.

    If no server is running, raise `ConnectionServerException` with code `CONNECTION_NO_AVAILABLE_SERVER`.
    """
    global _current_server
    if _current_server is None:
        raise ConnectionServerException(
            StatusCode.CONNECTION_NO_AVAILABLE_SERVER.code,
            StatusCode.CONNECTION_NO_AVAILABLE_SERVER.errmsg,
        )
    return _current_server


def register_connection_callback(func: "ConnFunction", name: str) -> None:
    """Register a callback function to every server created by `start_connection_server()`"""
    global _global_callback
    _global_callback[name] = func
    logger.info("global dict register new function named %r.", name)


def _check_ssl_context(
    mode: str, ssl_context: Union[SSLContext, ContexConfigDict, None]
) -> Optional["SSLContext"]:
    """If `mode` should use ssl_context, check and convert it, otherwise do nothing."""
    if mode == "wss":
        if ssl_context is None:
            code = StatusCode.CONNECTION_SSL_CONTEXT_REQUIRED
            msg = code.errmsg.format(mode=mode)
            logger.error("[%d]%s", code.code, msg)
            raise ConnectionServerException(code.code, msg)
        if not isinstance(ssl_context, SSLContext):
            return create_context(ssl_context)
    return ssl_context
