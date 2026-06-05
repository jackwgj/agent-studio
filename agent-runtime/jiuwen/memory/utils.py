#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""memory utils methods"""

import hashlib
import secrets
import threading
from typing import Any, Union


class ThreadSafeIDGenerator:
    """
    Thread safe ID Generator
    """

    def __init__(self):
        self._lock = threading.Lock()

    def generate_safe_str(self):
        """
        Generate safe string.
        """
        with self._lock:
            return hashlib.sha256(secrets.token_bytes(16)).hexdigest()

    def get_lock(self):
        """
        Get lock object.
        """
        return self._lock


def to_tuple(data: Union[tuple, list, any]):
    """
    Convert a given param into tuple if it's not, otherwise keep it unchanged.

    Args:
        data (Union[tuple, list, any]): A tuple or not tuple data.

    Returns:
        Tuple, Converted tuple representation of input data.

    """
    if isinstance(data, tuple):
        return data
    if isinstance(data, list):
        return tuple(data)
    return (data,)


def batch_check_type(type_dict: dict):
    """
    Check types of a batch of dict.

    Args:
        type_dict (dict): Type dictionary, where the key is the param name, and value is a tuple of the parameter
            to check and its target type(s), e.g., {"network": (network, nn.Cell)}.

    Raises:
        TypeError: If any check in the dict fails.

    """
    check_type(type_dict, "type_dict", dict)
    for param_name, (param, target_type) in type_dict.items():
        check_type(param, param_name, target_type)


def check_type(
    input_param: Any, param_name: str, target_type: Any = None, exclude_type: Any = None
):
    """
    Check types of a given parameter.

    Args:
        input_param (any): Parameter to check type.
        param_name (str): Parameter name.
        target_type (Union[type, None, tuple[type|None]]): Target type(s) for the parameter. Single `None` would skip
            target type check.
        exclude_type (Union[type, tuple]): Exclude type(s) for the parameter. Single `None` would skip exclude type
            check.

    Raises:
        TypeError: If check fails.

    """
    target_type = to_tuple(target_type)
    if None in target_type:
        if len(target_type) > 1 and input_param is not None:
            target_type = tuple(_ for _ in target_type if _ is not None)
            if not isinstance(input_param, target_type):
                raise TypeError(
                    f"The input parameter '{param_name}' should be of type(s) {target_type}, "
                    f"but got {type(input_param)}"
                )
    elif not isinstance(input_param, target_type):
        raise TypeError(
            f"The input parameter '{param_name}' should be of type(s) {target_type}, "
            f"but got {type(input_param)}"
        )
    if exclude_type and isinstance(input_param, exclude_type):
        raise TypeError(
            f"The input parameter {param_name} should not be instance of {exclude_type},"
            f" but {type(input_param)}"
        )
