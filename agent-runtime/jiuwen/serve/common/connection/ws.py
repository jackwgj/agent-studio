# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Websocket (with SSL support) server support."""

import json
from asyncio import (
    AbstractEventLoop,
    new_event_loop,
    run_coroutine_threadsafe,
    set_event_loop,
)
from ssl import SSLContext
from threading import Lock as ThreadLock, Thread
from time import time as curr_time, sleep as thread_sleep
from typing import Any, Optional, TYPE_CHECKING
from uuid import uuid4

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.serve.common.connection.base import (
    BaseConnection,
    BaseServer,
    ConnectionServerException,
    _C,
)
from websockets.exceptions import ConnectionClosedOK, ConnectionClosed

if TYPE_CHECKING:
    from websockets.server import (
        serve as ws_serve,
        WebSocketServerProtocol,
        WebSocketServer,
    )  # noqa: type-hint only

    from jiuwen.serve.common.connection.base import Config
else:
    from websockets import serve as ws_serve


class WsException(ConnectionServerException):
    """Exception while creating and using JiuWen wrapped Websocket server."""


class WsConnection(BaseConnection["WsServer"]):
    """Persistent connection of the client connected through the websocket protocol."""

    def __init__(
        self, server: "WsServer", sdk_id: str, protocol: "WebSocketServerProtocol"
    ):
        super().__init__(server, sdk_id)
        self.__protocol: "WebSocketServerProtocol" = protocol
        self.__closed = False
        self.__name = f"WsConnection(port={server.port}, id={sdk_id})"

    def __str__(self):
        return self.__name

    def on_close(self):
        """Never call this method outside `ws_server.py`: only called by handler."""
        self._server.conn_logout(self._sdk_id, self)
        self.__closed = True

    def send(self, name: str, data: Any) -> None:
        """Send `data` and `name` to the client of this websocket connection."""
        if self.__closed:
            code = StatusCode.CONNECTION_SEND_TO_CLOSED
            msg = code.errmsg.format(name=name, sdk_id=self._sdk_id)
            logger.error("[%d]%s", code.code, msg)
            raise WsException(code.code, msg)
        try:
            run_coroutine_threadsafe(
                self.__do_send(
                    json.dumps(dict(name=name, data=data), ensure_ascii=False)
                ),
                self._server.async_event_loop,
            )
        except ConnectionClosedOK as e:
            code = StatusCode.CONNECTION_SEND_TO_CLOSED
            msg = code.errmsg.format(name=name, sdk_id=self._sdk_id)
            logger.error("[%d]%s", code.code, msg)
            raise WsException(code.code, msg) from e

    async def __do_send(self, content: str):
        logger.info("Start to send data (len=%d) via %s", len(content), self)
        await self.__protocol.send(content)


class WsServer(BaseServer[WsConnection]):
    """Persistent connection server using the websocket protocol."""

    def __init__(
        self,
        config: "Config",
        ssl_context: "SSLContext" = None,
        start_check_interval: float = 0.2,
        start_check_timeout: float = 8,
        **kwargs,
    ):
        super().__init__(config, **kwargs)
        self.__conn_lock = ThreadLock()
        self.__host: Optional[str] = self.__get_host(config)
        self.__port: int = self.__get_port(config)
        self.__available_conn = dict()
        self.__async_event_loop = new_event_loop()
        configs = self.__load_config(config, ssl_context)
        self._description = (
            "Websocket server ("
            f"{self.__host + ':' if self.__host is not None else 'Port '}{self.__port}"
            f"{' with TLS' if configs.get('ssl') else ''})"
        )
        # These two variables will be set after `self.__thread` is started.
        self._server: Optional["WebSocketServer"] = None
        self.__server_exception: Optional[Exception] = None

        self.__thread = Thread(
            daemon=True,
            name=self._description,
            target=self.__thread_main,
            args=(configs,),
        )
        self.__thread.start()
        self.__wait_started(start_check_interval, start_check_timeout)

    def __contains__(self, sdk_id: str) -> bool:
        with self.__conn_lock:
            return sdk_id in self.__available_conn

    @property
    def async_event_loop(self) -> AbstractEventLoop:
        """The event loop used to run the websocket server side of this instance."""
        return self.__async_event_loop

    @property
    def port(self) -> int:
        """The listening port of this server."""
        return self.__port

    @staticmethod
    def __get_host(config: "Config") -> Optional[str]:
        host = config.get("host")
        if not ((host is None) or isinstance(host, str)):
            raise ConnectionServerException(
                StatusCode.CONNECTION_WS_WRONG_HOST.code,
                StatusCode.CONNECTION_WS_WRONG_HOST.errmsg.format(
                    typename=type(host).__name__
                ),
            )
        return host

    @staticmethod
    def __get_port(config: "Config") -> int:
        port = config.get("port")
        if not isinstance(port, int):
            raise ConnectionServerException(
                StatusCode.CONNECTION_WS_WRONG_PORT.code,
                StatusCode.CONNECTION_WS_WRONG_PORT.errmsg.format(
                    typename=type(port).__name__
                ),
            )
        return port

    def connection_of(self, sdk_id: str) -> _C:
        with self.__conn_lock:
            if sdk_id not in self.__available_conn:
                code = StatusCode.CONNECTION_NOT_EXIST
                msg = code.errmsg.format(sdk=sdk_id, server=self._description)
                logger.error("[%d]%s", code.code, msg)
                raise WsException(code.code, msg)
            return self.__available_conn[sdk_id]

    def conn_logout(self, sdk_id: str, conn: WsConnection) -> None:
        """Only called in a `finally` block: never throws an exception."""
        with self.__conn_lock:
            success = self.__available_conn.get(sdk_id) is conn
            if success:
                del self.__available_conn[sdk_id]
        if success:
            logger.debug(
                "Unregister a connection (sdk_id=%s) on port %d", sdk_id, self.__port
            )
        else:
            # not raise an Exception: otherwise, should catch in handler.
            logger.error(
                "Try to unregister a connection (sdk_id=%d) that doesn't belong to this server (port=%d).",
                sdk_id,
                self.__port,
            )

    async def _init_connection(
        self, protocol: "WebSocketServerProtocol"
    ) -> WsConnection:
        """Charset exception is handled by `_ws_handler()`"""
        conn = self.__new_connection(protocol)
        logger.debug(
            "New websocket connection on port %s start handshake (id=%s)",
            self.__port,
            conn.sdk_id,
            simple_log="New websocket connection start handshake ",
        )
        await protocol.send(conn.sdk_id)
        confirm = await protocol.recv()
        logger.debug(
            "Connection on port %s got handshake response, expected=%s, got=%s",
            self.__port,
            conn.sdk_id,
            confirm,
            simple_log="Connection got handshake response",
        )
        if confirm != conn.sdk_id:
            code = StatusCode.CONNECTION_WS_VALIDATE_FAIL
            msg = code.errmsg.format(
                server=self._description, sdk_id=conn.sdk_id, echo=confirm
            )
            logger.error("[%d]%s", code.code, msg)
            raise WsException(code.code, msg)
        return conn

    async def _ws_handler(self, protocol: "WebSocketServerProtocol") -> None:
        conn = None
        try:
            conn = await self._init_connection(protocol)
            while True:
                await (
                    protocol.recv()
                )  # no need to read, if receive anything, close this connection.
        except WsException as e:
            logger.error("%s raises an WsException: %s", conn, e)
            raise e
        except UnicodeDecodeError as e:
            logger.error("%s received a not unicode bytes: %s", conn, e)
            raise e
        except ConnectionClosedOK as e:
            logger.info("%s closed with code %s: %s", conn, e.code, e.reason)
        except ConnectionClosed as e:
            logger.warning("%s closed with error code %s: %s", conn, e.code, e.reason)
        finally:
            if conn:
                conn.on_close()
            logger.info("%s handler exit.", conn)

    def __load_config(self, config: "Config", ssl_context: "SSLContext") -> dict:
        wss = config.get("mode")
        ret = dict()
        if wss == "wss":
            if not isinstance(ssl_context, SSLContext):
                code = StatusCode.CONNECTION_WRONG_SSL_TYPE
                msg = code.errmsg.format(typename=type(ssl_context).__name__)
                logger.error("[%d]%s", code.code, msg)
                raise WsException(code.code, msg)
            ret["ssl"] = ssl_context
        return ret

    def __new_connection(self, protocol: "WebSocketServerProtocol") -> WsConnection:
        with self.__conn_lock:
            sdk_id = self.__new_sdk_id()
            ret = WsConnection(self, sdk_id, protocol)
            self.__available_conn[sdk_id] = ret
            logger.info(
                "WebSocket server on port %d register new connection with sdk_id=%s",
                self.__port,
                sdk_id,
            )
            return ret

    def __new_sdk_id(self) -> str:
        """Creates an identifier without any write operation.
        Should call this method before acquire `self.__lock`."""
        sdk_id: str = uuid4().hex
        if sdk_id not in self.__available_conn:
            return sdk_id
        retry = 0
        while True:
            retry = retry + 1
            sdk_id_2 = f"{sdk_id}{retry}"
            if sdk_id_2 not in self.__available_conn:
                return sdk_id_2

    def __wait_started(self, interval: float, timeout: float) -> None:
        start = curr_time()
        while True:
            thread_sleep(interval)
            if (curr_time() - start) > timeout:
                raise WsException(
                    StatusCode.CONNECTION_INIT_FAILED.code,
                    StatusCode.CONNECTION_INIT_FAILED.errmsg.format(key="timeout"),
                )
            if self.__server_exception:
                # logger is called while setting `self.__server_exception`.
                raise WsException(
                    StatusCode.CONNECTION_INIT_FAILED.code,
                    StatusCode.CONNECTION_INIT_FAILED.errmsg.format(key="exception"),
                ) from self.__server_exception
            if self._server is None:
                continue
            self._server: "WebSocketServer"
            if self._server.is_serving():
                logger.info("%s is considered successfully started.", self._description)
                return

    async def __async_main(self, configs: dict):
        try:
            self._server = await ws_serve(
                self._ws_handler, host=self.__host, port=self.__port, **configs
            )
        except Exception as e:
            logger.error("Start %s failed with: %s", self._description, e)
            self.__server_exception = e

    def __thread_main(self, ws_configs: dict) -> None:
        """Should only be used as a new thread entrance."""
        set_event_loop(self.__async_event_loop)
        run_coroutine_threadsafe(self.__async_main(ws_configs), self.__async_event_loop)
        logger.debug("Try to start websocket server on port %s", self.__port)
        self.__async_event_loop.run_forever()
