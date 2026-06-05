# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Supports persistent connections, which can be implemented through protocol switching."""

from jiuwen.serve.common.connection.api import (
    start_connection_server,
    get_connection_server,
    register_connection_callback,
)
from jiuwen.serve.common.connection.base import (
    BaseConnection,
    BaseServer,
    ConnFunction,
    ConnectionServerException,
)
