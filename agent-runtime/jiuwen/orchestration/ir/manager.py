# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Manager Interface of AgentIR `IrMgrBase`, and default implement `IrManager`."""

__all__ = (
    "StreamCode",
    "StreamData",
    "StreamWriter",
    "StreamingCallback",
    "IrMgrException",
)

from abc import abstractmethod, ABC
from enum import Enum, unique as enum_unique
from typing import Optional, Dict

from jiuwen.orchestration.ir.ir_types import IrMgrException
from pydantic import BaseModel as PydanticBase


@enum_unique
class StreamCode(int, Enum):
    """Streaming status code during StreamingCallback."""

    PARTIAL_CONTENT = 0
    CONTINUE = 1
    FINISH = 2
    # stream callback use mq, mapper to tgf StreamCode -2
    SYSTEM_STREAM_MQ = 3
    # Task id message
    SYSTEM_TASKID_MQ = 4
    ERROR = -1


class StreamData(PydanticBase):
    """Streaming data block definition during StreamingCallback."""

    code: StreamCode
    msg: str
    data: str
    header: Optional[Dict[str, str]] = None


class StreamWriter(ABC):
    """Base class for streaming writes."""

    @abstractmethod
    def write(self, data: StreamData) -> None:
        """This interface is used to write back to the running AgentIR which calls `on_recv()`."""


class StreamingCallback(ABC):
    """Callback interface to use streaming output. User implements this class and pass its instance to `invoke()`."""

    @abstractmethod
    def on_recv(self, data: StreamData, writer: StreamWriter) -> None:
        """Called while the running AgentIR send one streaming data block."""
        raise NotImplementedError

    @abstractmethod
    def on_close(self) -> None:
        """Called while the running AgentIR is going to exit."""
        raise NotImplementedError
