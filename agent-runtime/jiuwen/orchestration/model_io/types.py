#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
A type module used to manage various enumeration types and exception classes.
"""

__all__ = (
    "ModelErrStatus",
    "ModelInvokeError",
    "Message",
    "FunctionCall",
    "MessageBatch",
    "Dialog",
    "InputType",
    "DictChunk",
    "ListChunk",
    "MessageChunk",
    "ResponseIterConfig",
    "SkipThisIteration",
    "UniFunctionSpec",
    "ModelConfig",
)


import dataclasses
from enum import Enum, unique as enum_unique
from typing import (
    Any,
    Dict,
    List,
    Tuple,
    Optional,
    Literal,
    TYPE_CHECKING,
    no_type_check,
    Union,
    TypedDict,
    ClassVar,
)

from pydantic import BaseModel as pydantic_BaseModel, Field, HttpUrl, model_validator


# Exception and error code definition.


@enum_unique
class ModelErrStatus(Enum):
    """Indicates the result of the current model service invoking."""

    MISS_ARGUMENTS = "Required parameters are missing."

    DISCONNECT = "Cannot connect to remote server (HTTP connection timeout)."
    READ_TIMEOUT = (
        "Timeout while receiving data from remote server (HTTP read timeout)."
    )
    NETWORK_ERROR = "No response is received (Unmarked network error)."

    REMOTE_DENIED = "Unexpected HTTP response code (Unidentified exception response)."
    REMOTE_INTERNAL_ERROR = (
        "Error occurs on the remote server. Response is parsed successfully."
    )
    CHARSET_EXCEPTION = "Error on decoding the response body. Specify another character set and try again."

    FORMAT_UNKNOWN = "Failed to parse the response body. The format may be incorrect."
    EMPTY_RESPONSE = "No response got from remote unexpectedly."


class ModelInvokeError(RuntimeError):
    """
    An exception class provides error information for developers to locate model invoking errors.
    """

    def __init__(
        self, status_code: ModelErrStatus, msg: str = None, input_: Any = None
    ):
        super().__init__(f"[{status_code.name}] {msg if msg else status_code.value}")
        self.input_ = input_
        self.code = status_code
        self.msg = msg


# Input / Output schema definition.


class FunctionCall(TypedDict):
    """
    Defines FunctionCall class.
    """

    name: str
    arguments: str
    id: Optional[str]


class Message(TypedDict, total=False):
    """
    Defines Message class.
    """

    role: Literal["assistant", "function", "system", "user"]
    content: Optional[str]
    name: Optional[str]
    function_call: Optional[FunctionCall]


def _merge_anything(former, later):
    if former is None:
        return later
    if later is None:
        return former
    return former + later


class DictChunk(dict):
    """
    Defines DictChunk class.
    """

    def __add__(self, other):
        if not isinstance(other, DictChunk):
            return NotImplemented
        return self.__merge(type(self)(self), other)

    def __radd__(self, other):
        if other is None:
            return type(self)(self)
        return NotImplemented

    def __iadd__(self, other):
        if not isinstance(other, DictChunk):
            return NotImplemented
        return self.__merge(self, other)

    @staticmethod
    def __merge(to: dict, later: dict) -> dict:
        for k, v in later.items():
            if k not in to:
                to[k] = v
            else:
                to[k] = _merge_anything(to[k], v)
        return to


class ListChunk(list):
    """
    Defines ListChunk class.
    This class implements three special list operations: list merging, list accumulation, and list completion.
    """

    def __add__(self, other):
        if not isinstance(other, ListChunk):
            return super().__add__(other)
        ret: list = ListChunk()
        comm_len = min(len(self), len(other))
        for i in range(comm_len):
            ret.append(_merge_anything(self[i], other[i]))
        ret.extend(self[comm_len:])
        ret.extend(other[comm_len:])
        return ret

    def __radd__(self, other):
        if other is None:
            return ListChunk(self)
        return NotImplemented

    def __iadd__(self, other):
        if not isinstance(other, ListChunk):
            return super().__iadd__(other)
        comm_len = min(len(self), len(other))
        for i in range(comm_len):
            self[i] = _merge_anything(self[i], other[i])
        self.extend(other[comm_len:])
        return self

    def finish(self):
        """
        This method implements the list completion operation.
        """
        return _end_chunk(self)


def _end_chunk(obj):
    if isinstance(obj, list):
        return list((_end_chunk(x) for x in obj))
    if isinstance(obj, dict):
        return dict({k: _end_chunk(v) for k, v in obj.items()})
    return obj


if TYPE_CHECKING:

    class MessageChunk(Message):
        """
        Defines MessageChunk class for type check.
        """

        pass
else:

    @no_type_check
    class MessageChunk(DictChunk):
        """
        Defines MessageChunk class without type check.
        """

        __role_key: ClassVar[str] = "role"

        def __add__(self, other):
            if not isinstance(other, MessageChunk):
                return NotImplemented
            role = self.get(self.__role_key)
            ret = super().__add__(other)
            if (ret is not NotImplemented) and (role is not None):
                ret[self.__role_key] = role
            return ret

        def __iadd__(self, other):
            role = self.get(self.__role_key)
            ret = super().__iadd__(other)
            if (ret is not NotImplemented) and (role is not None):
                ret[self.__role_key] = role
            return ret

        def finish(self):
            """
            Used to complete the construction of message chunks.
            """
            return _end_chunk(self)


# Type alias.


MessageBatch = List[Message]
Dialog = List[Message]
InputType = Union[str, Dialog]


# Schema definition for stream mode.


@dataclasses.dataclass
class ResponseIterConfig:
    """
    Defines ResponseIterConfig class.
    """

    situation: Union[Literal["line", "raw"], int] = "line"
    """call handler every line or every chunk (if is int, means raw mode and chunk size=situation)."""
    charset: Optional[str] = None
    """Take effect only situation=='line', None means auto charset."""
    extras: dict = dataclasses.field(default_factory=lambda: {})
    """extra data for subclass."""


class SkipThisIteration(RuntimeError):
    """
    Defines a new exception class.
    Throw this exception when you need to skip the current iteration.
    """

    ...


# Schema for loading a Model from json object.


class UniFunctionSpec(pydantic_BaseModel):
    """
    Defines UniFunctionSpec class.
    """

    name: str
    description: str = None
    parameters: Dict[str, Any] = None
    principle: str = None
    results: str = None


class ModelConfig(pydantic_BaseModel):
    """
    Defines ModelConfig class.
    This class is mainly used to configure the parameters of the model.
    """

    class Args(pydantic_BaseModel):
        """
        This class imposes constraints on the parameters of the model.
        """

        url: HttpUrl = None
        model: str = None

        temperature: float = Field(None, ge=0, le=2)
        top_p: float = Field(None, ge=0, le=1, alias="topP")
        top_k_num: int = Field(None, ge=1, alias="topK")

        functions: List[UniFunctionSpec] = None
        stop: str = None

        connect_timeout: float = Field(None, ge=0, alias="connectTimeout")
        read_timeout: float = Field(None, ge=0, alias="readTimeout")
        timeout: Union[float, Tuple[float, float]] = None

        extra_headers: Dict[str, str] = Field(None, alias="extraHeaders")

        @model_validator(mode="after")
        def fill_timeout(self):
            """
            Used to populate or update the "timeout" field of a class instance.
            If the "timeout" field is empty, it uses the values of the "connect_timeout" and "read_timeout" fields
            to populate the "timeout" field.
            """
            if self.timeout is None:
                conn = self.connect_timeout
                read = self.read_timeout
                if conn is not None or read is not None:
                    self.timeout = (conn, read)
            return self

    name: str
    content: Args

    @model_validator(mode="before")  # noqa: model_validator accepts a classmethod
    @classmethod
    def rename_config(cls, values: dict):
        """
        Preprocess the input dictionary to ensure that
        the key name of the dictionary complies with the expected format or naming rules.
        """
        top_p = "top_p"
        config = values.get("config")
        if config is not None:
            if top_p in config:
                config["topP"] = config[top_p]
                del config[top_p]
            values["content"] = config
            del values["config"]
        return values
