#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""Variable class for processing retrieved data"""

from typing import Callable, List, Any

from jiuwen.prompt.assemble.variables.textable import TextableVariable
from jiuwen.prompt.assemble.variables.variable import Variable
from jiuwen.prompt.common.exception.assemble import VariableInitError, InputKeyError
from jiuwen.prompt.common.retriever.base import BaseRetriever


class RetrievableVariable(Variable):
    """Variable class for processing retrieved data.

    Args:
        retriever (BaseRetriever): an instance of `BaseRetriever`, used for getting documents.
        query_key (str): name of the argument used as the query for the retriever and selector.
        name (str, optional): name of the variable. Default: ``default``.
        item_format (str, optional): a string with placeholders that defines the format of each item of the retrieved \
            data. Supported placeholders in item_format include item and index, the former represents a specific item \
            in the iterable object and the latter represents its index (starting from 1). You can use dot operator (.) \
            to get the attribute of the item by name (if item is a class object) or get the value of the item by key \
            (if item is a dict), e.g., {{item.name}}. Other operators such as index operator {{item[0]}} are not \
            supported. Default: ``None``.
        join_str (str, optional): a string for concatenating each item of the input data. Default: ``'\\n'``.
        format_func (Callable, optional): a function for converting data into a string. Default: ``None``.
        selector (Selector, optional): a selector object for optimizing the variable value. Default: ``None``.

    Raises:
        VariableInitError: when pass in wrong arguments for instantiate the variable object.
        InputKeyError: when pass in wrong arguments for updating the variable.
    """

    retriever: BaseRetriever
    format_func: Callable
    cached_data: List[Any] = []

    def __init__(
        self,
        retriever: BaseRetriever,
        query_key: str,
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
            if "item" not in format_variable.input_keys:
                raise VariableInitError(
                    "`item_format` should contain the following placeholders: "
                    "item and index (optional)"
                )
            unexpected_keys = set(format_variable.input_keys) - {"item", "index"}
            if len(unexpected_keys) > 0:
                raise VariableInitError(
                    f"The following placeholders are not supported in `item_format`: "
                    f"{list(unexpected_keys)}"
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

        if not isinstance(query_key, str) or len(query_key) == 0:
            raise VariableInitError("Argument query_key must be a non-empty string.")

        self.retriever = retriever
        self.format_func = format_func
        self.join_str = join_str
        self.query_key = query_key
        super().__init__(
            name=name,
            input_keys=[query_key],
        )

    def update(self, **kwargs):
        """Retrieve documents using the retriever, convert retrieved data into string, cache the data and string.

        Args:
            **kwargs:  arguments passed in as key-value pairs for updating the variable.
        """
        query = kwargs.get(self.query_key)
        if not query:
            raise InputKeyError(f"Missing input key {self.query_key} in the arguments.")
        data = self.retriever.get_relevant_documents(query=query)
        self.cached_data = data
        self.value = self.format_func(self.cached_data)
