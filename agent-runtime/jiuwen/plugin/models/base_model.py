#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Base Model class"""

import inspect
from functools import wraps
from typing import Union, Any, get_type_hints

from jiuwen.plugin.common.exception import ValidatorErrorMsg, ValidationException


def validator(field: str):
    """validator decorator"""

    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            return func(*args, **kwargs)

        wrapper.is_check_by_validator = True
        wrapper.check_param = field
        return wrapper

    return decorator


class Default:
    """Default value"""

    pass


class Field:
    """Field class"""

    def __init__(
        self,
        max_length: Union[int, Default] = Default(),
        min_length: Union[int, Default] = Default(),
        title: str = "",
        default: Any = Default(),
    ):
        self.title = title
        self.max_length = max_length
        self.min_length = min_length
        self.default = default


class BaseModel(object):
    """Base Model class"""

    def __init__(self, **kwargs):
        self._kwargs = kwargs
        error_msg = {}
        error_msg = self.handler_validator(error_msg, **self._kwargs)
        self._field_map = {
            m[0]: m[1] for m in inspect.getmembers(self) if isinstance(m[1], Field)
        }
        if getattr(self.Config, "extra", "") == "forbid":
            error_msg.update(
                {
                    k: ValidatorErrorMsg.ExtraFieldsErrorMsg
                    for k in self._kwargs
                    if k not in self._field_map
                }
            )
            self._kwargs = {
                k: v for k, v in self._kwargs.items() if k in self._field_map
            }

        if getattr(self.Config, "anystr_strip_whitespace", ""):
            self._kwargs = {
                k: v.strip() if isinstance(v, str) else v
                for k, v in self._kwargs.items()
            }

        # 没有指定default的默认一定要给出
        for k, v in self._field_map.items():
            if isinstance(v.default, Default) and k not in self._kwargs:
                error_msg[k] = ValidatorErrorMsg.FieldRequiredErrorMsg

        # max_length & min_length 校验
        for k, v in self._kwargs.items():
            if not self._field_map.get(k, ""):
                continue
            if (
                isinstance(self._field_map.get(k).max_length, int)
                and isinstance(v, str)
                and len(v) > self._field_map.get(k).max_length
            ):
                error_msg[k] = ValidatorErrorMsg.ValidateMaxLengthErrorMsg.format(
                    self._field_map.get(k).max_length
                )
            if (
                isinstance(self._field_map.get(k).min_length, int)
                and isinstance(v, str)
                and len(v) < self._field_map.get(k).min_length
            ):
                error_msg[k] = ValidatorErrorMsg.ValidateMinLengthErrorMsg.format(
                    self._field_map.get(k).min_length
                )

        # 强类型校验
        for k, v in self._kwargs.items():
            if k in self._field_map and not isinstance(v, get_type_hints(self).get(k)):
                error_msg[k] = ValidatorErrorMsg.TypeErrorMsg

        if error_msg:
            raise ValidationException(str(error_msg))

        for k, v in self._field_map.items():
            setattr(self, k, self._kwargs.get(k, v.default))
        if getattr(self.Config, "extra", "") != "forbid":
            for k, v in self._kwargs.items():
                setattr(self, k, v)

    def __setattr__(self, key, value):
        if (
            getattr(self.Config, "extra", "") != "forbid"
            or key.startswith("_")
            or key in self._field_map
        ):
            super().__setattr__(key, value)
        else:
            raise ValueError(
                ValidatorErrorMsg.AttributeErrorMsg.format(self.__class__.__name__, key)
            )

    class Config:
        """config"""

        pass

    def handler_validator(self, error_msg, **kwargs):
        """validator"""
        funcs = inspect.getmembers(self, inspect.ismethod)
        for func in funcs:
            if getattr(func[1], "is_check_by_validator", ""):
                field = getattr(func[1], "check_param", "")
                if field in kwargs:
                    try:
                        func[1](kwargs.get(field))
                    except Exception as e:
                        error_msg.update({field: e.__str__()})
        return error_msg

    def dict(self, exclude_unset: bool = False):
        """to dict"""
        if not exclude_unset:
            return {
                m[0]: m[1]
                for m in inspect.getmembers(self)
                if m[0] in self._field_map or m[0] in self._kwargs
            }
        res = {
            m[0]: m[1]
            for m in inspect.getmembers(self)
            if m[1] and m[0] in self._field_map or m[0] in self._kwargs
        }
        return res
