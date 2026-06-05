#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
Tool functions for large model I/O.
"""

__all__ = (
    "SupportUniCreate",
    "SupportUniUserId",
    "SupportUniMsgId",
    "create_model_from",
)

from abc import ABC, abstractmethod
from typing import Union, TypeVar

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.orchestration.model_io import BaseModel
from jiuwen.orchestration.model_io.types import ModelConfig

# To support create an instance from some model config.


_model_cls_map: "dict[str, Union[type[BaseModel], type[SupportUniCreate]]]" = {}
_T = TypeVar("_T", bound=BaseModel)


class SupportUniCreate:
    """This is a class that supports the creation of classes via aliases."""

    def __init_subclass__(
        cls: "type[_T]", /, alias: "Union[str, tuple[str], list[str]]" = None, **kwargs
    ):
        super().__init_subclass__(**kwargs)
        _model_cls_map[cls.__name__] = cls
        if isinstance(alias, str):
            alias = [alias]
        if isinstance(alias, (tuple, list)):
            for name in alias:
                _model_cls_map[name] = cls
        return super().__init_subclass__(**kwargs)

    @classmethod
    def create_from_config(cls: "type[_T]", config: ModelConfig) -> "_T":
        """Create an instance of the class from a configuration."""
        return cls.parse_obj(config.content.model_dump(exclude_none=True))


class SupportUniUserId(ABC):
    """Inherit this class if you support setting the user id."""

    @abstractmethod
    def set_user_id(self, user_id: str) -> None:
        """Set user id."""
        raise NotImplementedError


class SupportUniMsgId(ABC):
    """Inherit this class if you support setting the message id."""

    @abstractmethod
    def set_message_id(self, message_id: str) -> None:
        """Set message id."""
        raise NotImplementedError


class ModelNotFound(JiuWenBaseException):
    """This exception is thrown when the model name does not exist."""

    def __init__(self, error_code: int, message: str):
        super().__init__(error_code=error_code, message=message)


def create_model_from(config: Union[ModelConfig, dict]) -> BaseModel:
    """Create a model based on a given configuration (either a ModelConfig object or a dictionary)."""
    if not isinstance(config, ModelConfig):
        config = ModelConfig.model_validate(config)
    config: ModelConfig
    if config.name not in _model_cls_map:
        raise ModelNotFound(
            StatusCode.MODEL_NAME_ERROR.code,
            StatusCode.MODEL_NAME_ERROR.errmsg.format(config.name),
        )
    return _model_cls_map[config.name].create_from_config(config)
