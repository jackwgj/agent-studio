#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""Variable class for process iterable objects"""

from copy import deepcopy
from typing import Callable

from jiuwen.prompt.assemble.variables.textable import TextableVariable
from jiuwen.prompt.assemble.variables.variable import Variable
from jiuwen.prompt.common.exception.assemble import VariableInitError, InputKeyError


class IterableVariable(Variable):
    """Variable class for process iterable objects. Support processing of objects that can be iterated over,  \
        include lists, sets, tuples, generators, etc.

    Args:
        data_key (str): name of the argument that represents the input data.
        name (str, optional): name of the variable. Default: ``default``.
        item_format (str, optional): a string with placeholders that defines the format of each item of the input \
            data. Supported placeholders in item_format include item and index, the former represents a specific item \
            in the iterable object and the latter represents its index (starting from 1). You can use dot operator (.) \
            to get the attribute of the item by name (if item is a class object) or get the value of the item by key \
            (if item is a dict), e.g., {{item.name}}. Other operators such as index operator {{item[0]}} are not \
            supported. Default: ``None``.
        join_str (str, optional): a string for concatenating each item of the input data. Default: ``'\\n'``.
        format_func (Callable, optional): a function for converting data into a string. Default: ``None``.

    Raises:
        VariableInitError: when pass in wrong arguments for instantiate the variable object.
        InputKeyError: when pass in wrong arguments for updating the variable.

    """

    def __init__(
        self,
        data_key: str,
        name: str = "default",
        item_format: str = None,
        join_str: str = "\n",
        format_func: Callable = None,
    ):
        if item_format and format_func:
            raise VariableInitError(
                "Arguments item_format and format_func cannot be given at the same time."
            )
        if not (item_format or format_func):
            raise VariableInitError(
                "At least one of the arguments item_format and format_func should be given."
            )
        if item_format is not None:
            format_variable = TextableVariable(text=item_format)
            unexpected_keys = set(format_variable.input_keys) - {"item", "index"}
            if len(unexpected_keys) > 0:
                raise VariableInitError(
                    f"The following placeholders are not supported in `item_format`: "
                    f"{list(unexpected_keys)}"
                )
            if "item" not in format_variable.input_keys:
                raise VariableInitError(
                    "Placeholders in `item_format` should at least include 'item'"
                )

            def format_func_define(data):
                if "index" in format_variable.input_keys:
                    return join_str.join(
                        format_variable.eval(index=str(index), item=item)
                        for index, item in enumerate(data, start=1)
                    )
                return join_str.join(
                    format_variable.eval(item=item)
                    for index, item in enumerate(data, start=1)
                )

            format_func = format_func_define

        if not isinstance(data_key, str):
            raise VariableInitError("Argument data_key must be a string.")
        input_keys = [data_key]
        self.data_key = data_key
        self.format_func = format_func
        self.join_str = join_str
        self.cached_data = []
        super().__init__(
            name=name,
            input_keys=input_keys,
        )

    def update(self, **kwargs):
        """Convert input data list into string by invoking `self.format_func`, cache the data list and the string.

        Args:
            **kwargs:  arguments passed in as key-value pairs for updating the variable.
        """
        data = kwargs.get(self.data_key)
        if not data:
            raise InputKeyError(
                f"Missing input key `{self.data_key}` in the arguments."
            )
        self.cached_data = deepcopy(list(data))
        self.value = self.format_func(self.cached_data)
