# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Defines the data type required by the IR."""

from enum import Enum
from typing import Dict, List, Any, Optional, TypedDict, Union

from jiuwen.common.exception.base import JiuWenBaseException
from pydantic import BaseModel as pydantic_BaseModel, Field, StrictStr


class DebugInfo(TypedDict):
    """Information for debug (graph/component input/outputs)"""

    key: str
    value: Any


class IrMgrException(JiuWenBaseException):
    """Throws when an exception happened in Manager of IR."""

    def __init__(self, error_code: int, message: str):
        super().__init__(error_code=error_code, message=message)


class IrInvokeException(IrMgrException):
    """Throws when an exception happened while running IR."""

    def __init__(
        self,
        error_code: int,
        message: str,
        debug: list[DebugInfo] = None,
        should_wait_onclose: bool = False,
    ):
        super().__init__(error_code=error_code, message=message)
        self.debug_info: Optional[list[DebugInfo]] = debug
        self.should_wait_onclose = should_wait_onclose


class EdgeType(str, Enum):
    """About Enumeration Classes for Edge Types"""

    CONDITION = "Condition"


class Position(pydantic_BaseModel):
    """Define class of position"""

    pass


class BaseConnection(pydantic_BaseModel):
    """
    Base class for connections.

    Note that the key 'source' and 'target' are used independently in `IrConnCheck`
    """

    source: StrictStr
    target: StrictStr


class ExecutorConnection(BaseConnection):
    """
    This class represents a connection between two nodes
    """

    type: Union[EdgeType, None] = None
    source_item: Union[StrictStr, None] = Field(None, alias="sourceItem")
    target_item: Union[StrictStr, None] = Field(None, alias="targetItem")
    condition_id: Union[StrictStr, None] = Field(None, alias="conditionId")


class Connection(BaseConnection):
    """
    This class represents a connection between two nodes
    """

    branch_id: Union[StrictStr, None] = Field(None, alias="branchId")


class EndPoint(pydantic_BaseModel):
    """Stores the end point data."""

    type: StrictStr
    url: StrictStr
    host: StrictStr
    port: int
    hosts: List[StrictStr]
    ports: List[StrictStr]
    path: List[StrictStr]
    connection_timeout_ms: int = Field(alias="connectionTimeoutMs")
    auth_key_id: StrictStr = Field(alias="authKeyId")
    extension: Dict[str, Any]


class AgentId(pydantic_BaseModel):
    """Stores agent info."""

    agent_id: str = Field(alias="agentId", strict=True)
    version: str = Field(strict=True)


class AgentDeleteParam(AgentId):
    """As a type alias of `AgentId`"""
