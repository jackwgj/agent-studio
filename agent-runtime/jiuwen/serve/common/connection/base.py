# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Base definition of persistent connection service."""

from abc import ABC, abstractmethod
from typing import Any, Callable, Generic, TypeVar, TypedDict

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.log.base import logger

_C = TypeVar("_C", bound="BaseConnection")
_S = TypeVar("_S", bound="BaseServer")
ConnFunction = Callable[["BaseConnection", Any], None]


class Config(TypedDict, total=False):
    """Configuration file type comment, usually provided by `start_connection_server()`.

    `start_connection_server()` only checks that `mode` must be present and a string.
    """

    mode: str
    host: str
    port: int
    path: str


class BaseConnection(ABC, Generic[_S]):
    """Persistent connection Base Class."""

    def __init__(self, server: _S, sdk_id: str):
        self._server: _S = server
        self._sdk_id: str = sdk_id

    @property
    def sdk_id(self) -> str:
        """A str that can identify this connection to the server to which this connection belongs."""
        return self._sdk_id

    @property
    def server(self) -> _S:
        """The server to which this instance belongs."""
        return self._server

    @abstractmethod
    def send(self, name: str, data: Any):
        """Send `data` and `name` to the client of this connection."""

    @abstractmethod
    def on_close(self):
        """Never call this method manually outside module `jiuwen.serve.common.connection`"""


class BaseServer(ABC, Generic[_C]):
    """Basic definition of any type of persistent connection server."""

    def __init__(
        self, config: Config, function_dict: dict[str, ConnFunction] = None, **_
    ):
        self._description: str = type(self).__name__
        self.__config = config
        self._functions: dict[str, ConnFunction] = (
            dict() if function_dict is None else function_dict
        )

    @abstractmethod
    def __contains__(self, sdk_id: str) -> bool:
        """Check whether `sdk_id` represents a connection to this server."""
        pass

    @property
    def config(self) -> Config:
        """The configuration used to start this server that from settings.yaml."""
        return self.__config

    @property
    def description(self) -> str:
        """A short description about this server."""
        return self._description

    @abstractmethod
    def connection_of(self, sdk_id: str) -> _C:
        """Obtaining a Link Using `sdk_id`.

        If the connection represented by `sdk_id` does not exist, an exception is thrown.
        """
        pass

    def register(self, func: ConnFunction, name: str) -> None:
        """Register a callback function to this server.

        If this server was created by `start_connection_server()`,
        it will also register with other servers created using this method.
        """
        self._functions[name] = func
        logger.info("%s register new function named %r.", self._description, name)

    def call(self, conn: _C, name: str, data: Any) -> None:
        """Call the method registered as `name` on the client connected to this instance and pass `data`."""
        if name not in self._functions:
            # not raise: do not need to catch in websocket handler
            logger.error(
                "SDK (id=%s) attempted to call an nonexistent method '%s' on %s",
                conn.sdk_id,
                name,
                self._description,
            )
        self._functions[name](conn, data)


class ConnectionServerException(JiuWenBaseException):
    """Base class of all persistent connection server exceptions."""
