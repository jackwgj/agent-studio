#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""Base variable class"""

from abc import abstractmethod
from copy import deepcopy
from typing import Optional

from jiuwen.prompt.common.exception.assemble import InputKeyError, VariableInitError


class Variable:
    """Base class for variables.

    Args:
        name (str): name of the variable.
        value (str): current content of the variable after updating.
        input_keys (list[str]): list of argument names for updating the variable.
    """

    def __init__(self, name: str, input_keys: Optional[str] = None):
        self.name = name
        self.value = ""
        self.input_keys = input_keys

    @classmethod
    def load_from_config(cls, config):
        """
        Get a variable object by providing a config dict.

        Args:
            config (dict): contains the arguments for initializing a variable object. The dict should contain at least \
                a "type" key, whose valid values include "textable", "iterable" and "retrievable". Other valid keys \
                depend on the specific variable class.

        Returns:
            Variable: an object of class that inherits Variable.

        """
        local_config = deepcopy(config)
        var_cls_name = local_config.pop("type", "unknown")
        if not var_cls_name:
            raise VariableInitError("Missing variable class in the config.")
        if var_cls_name == "textable":
            from jiuwen.prompt import TextableVariable

            var_cls = TextableVariable
        elif var_cls_name == "iterable":
            from jiuwen.prompt import IterableVariable

            var_cls = IterableVariable
        elif var_cls_name == "retrievable":
            from jiuwen.prompt import RetrievableVariable

            var_cls = RetrievableVariable
        else:
            raise VariableInitError(f"Unknown variable class {var_cls_name}.")
        return var_cls(**local_config)

    @abstractmethod
    def update(self, **kwargs):
        """This method should implement the process of updating `self.value` based on input arguments.

        Args:
            **kwargs: arguments passed in as key-value pairs for updating the variable.
        """

    def eval(self, **kwargs) -> str:
        """Validate the input key-values, update `self.value`, perform selection (if there is), and return value.

        Args:
            **kwargs: arguments passed in as key-value pairs for update the variable.

        Returns:
            str: updated value of variable.
        """
        input_kwargs = self._prepare_inputs(**kwargs)
        self.update(**input_kwargs)
        return self.value

    def _prepare_inputs(self, **kwargs) -> dict:
        missing_keys = set(self.input_keys) - set(kwargs.keys())
        if missing_keys:
            raise InputKeyError("Missing keys for updating the variable")
        unexpected_keys = set(kwargs.keys()) - set(self.input_keys)
        if unexpected_keys:
            raise InputKeyError("Unexpected keys for updating the variable")
        input_kwargs = {k: v for k, v in kwargs.items() if k in self.input_keys}
        return input_kwargs
