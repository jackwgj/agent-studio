#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""Base class of the request model service"""

from os import environ as os_environ
from typing import ClassVar, Dict, Union, Tuple, Any

from jiuwen.insight.manager import TraceManager
from jiuwen.insight.utils import get_instance_info
from jiuwen.memory.conversation import ConversationMemory
from jiuwen.orchestration.base import Invokable
from jiuwen.orchestration.model_io.types import (
    ModelErrStatus,
    ModelInvokeError,
    InputType,
    MessageBatch,
)
from pydantic import (
    HttpUrl,
    BaseModel as pydantic_BaseModel,
    Field as pydantic_Field,
    ConfigDict,
)


class BaseModel(Invokable[InputType, MessageBatch], pydantic_BaseModel):
    """
    Defines the common code of the request model service and the basic class of the process.
    Objects of this class cannot be constructed directly.

    This class uses :class:`pydantic.BaseModel` to provide the validity check capability.
    This class has preset default configurations. During construction, ``pydantic`` attempts
    to read configuration information from initialization parameters and store it as a member
    variable (overwriting the preset default value).

    If ``_URL_ENV_KEY`` is set to a non-null value for a subclass, and a non-null URL is not
    passed in the constructor function, the system attempts to read a value from the environment
    variable as the value of the member ``url`` during construction.

    Args:
        url (str, optional): Model service path character string, which must contain the complete
            protocol field (HTTP/HTTPS). If the environment variable name is set by the subclass
            and is not empty, and the URL set by the environment variable exists, this parameter
            overwrites the environment variable setting. **At least one of the environment variables
            and constructors must be used to configure the URL. Otherwise, an error is reported.**
        config_kwargs (Any, optional): Indicates other optional configuration information. You can set this
            parameter to any value (not repeated).

    Raises:
        pydantic.error_wrappers.ValidationError: Thrown when the specified parameter verification
            does not meet the requirements (type and value range).
    """

    # Pydantic v2 配置
    model_config = ConfigDict(
        validate_assignment=True,
        extra="allow",  # 替代 Extra.allow
        arbitrary_types_allowed=True,
        # 注意：ignored_types 在 v2 中不再使用
    )

    memory: ConversationMemory = None

    DEFAULT_HTTP_HEADER: ClassVar[Dict[str, str]] = {
        "Content-Type": "application/json; charset=utf-8",
        "Accept-Charset": "utf-8",
    }
    DEFAULT_TIMEOUT: ClassVar = (5, 60)
    """See attribute ``timeout``."""

    url: HttpUrl = pydantic_Field(..., exclude=True, strict=True)
    """str, model service path. Must completely include the protocol field."""

    timeout: Union[float, Tuple[float, float]] = pydantic_Field(
        DEFAULT_TIMEOUT, exclude=True, gt=0
    )
    """
    Union[float, Tuple[float, float]], sets the request timeout interval.

    Currently, the following two modes are supported:
    - Transfer a tuple with two tuples of the ``float`` type, indicating *connection setup timeout threshold*
      and *response read timeout threshold*, respectively. The unit is second.
    - One ``float`` type value indicates two timeout thresholds. The unit is still second.
    Default: ``(5, 60)`` , indicating the connection timeout interval is 5 seconds and
    the response timeout interval is 60 seconds.
    """

    extra_headers: dict = pydantic_Field(dict(), exclude=True)
    """
    Additional request header carried in the request sent to the model service.
    Default: only necessary headers are attached.
    """

    _URL_ENV_KEY: ClassVar[str] = None
    """
    Name of the model service URL in the environment variable.
    If the subclass changes this attribute to a non-null value, the constructor function
    will attempt to read the environment variable whose name is the same as its value as
    the URL when url is not passed to the constructor.
    """

    def __init__(self, url: str = None, **config_kwargs):
        super().__init__(
            url=url
            if url or (not self._URL_ENV_KEY)
            else os_environ.get(self._URL_ENV_KEY),
            **config_kwargs,
        )

        for k, v in self.DEFAULT_HTTP_HEADER.items():
            if not self.extra_headers.get(k):
                self.extra_headers[k] = v

    def _before_action(self, inputs, kwargs: Dict[str, Any]) -> TraceManager:
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None),
            get_instance_info(
                self,
                blacklist=[
                    "extra_headers",
                    "functions",
                    "url",
                    "user_id",
                    "message_id",
                ],
            ),
        )
        trace_manager.on_llm_start(inputs)

        if not self.url:
            mie = ModelInvokeError(
                ModelErrStatus.MISS_ARGUMENTS,
                "Url of model service is required but get empty/None.",
            )
            trace_manager.on_llm_error(mie)
            raise mie
        return trace_manager
