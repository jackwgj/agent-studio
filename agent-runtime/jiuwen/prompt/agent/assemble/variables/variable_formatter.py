# coding:utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
Agent TextableVariable
"""

import re
from typing import Optional

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.executor_sdk.operator.impl.common.utils import executor_logger
from jiuwen.prompt.common.exception.assemble import VariableInitError, InputKeyError


class VariableFormatter:
    """Class for processing placeholders.

    Args:
        text (str, optional): a string containing placeholders enclosed by ${ and }.
        variable_format_schema (dict, optional): json schema for iterable variables.

    Raises:
        VariableInitError: when pass in wrong arguments for instantiate the variable object.
        InputKeyError: when pass in wrong arguments for updating the variable.
    """

    def __init__(
        self,
        text: str,
        variable_format_schema: Optional[dict] = None,
        list_join_string: Optional[str] = None,
    ):
        self.variable_format_schema = variable_format_schema or {}
        self.list_join_string = list_join_string
        self.value = None
        self.clean_text = ""
        self.placeholders = []
        self.input_keys = []
        self.iterables = {}
        self.full_placeholders = {}

        clean_text = text
        placeholders = []
        input_keys = []
        iterables = {}
        placeholder_matches = re.finditer(r"\$\{([^{}]*)\}", text)
        for match in placeholder_matches:
            # 去掉全部空格
            placeholder = match.group(1).replace(" ", "")
            if len(placeholder) == 0:
                continue

            input_iterable = placeholder.split("=>")
            if len(input_iterable) == 2:
                iterables[input_iterable[0]] = input_iterable[1]
                input_key = input_iterable[0].split(".")[0]
            elif len(input_iterable) == 1:
                input_key = placeholder.split(".")[0]
                # placeholders只存不可迭代类型的占位符
                if placeholder not in placeholders:
                    placeholders.append(placeholder)
            else:
                raise VariableInitError(
                    f"placeholders format error: placeholder[{placeholder}]"
                )
            if "[" in input_key:
                input_key = input_key[: input_key.index("[")]
            if input_key not in input_keys:
                input_keys.append(input_key)

            clean_text = clean_text.replace(match.group(0), "${" + placeholder + "}")
        self.clean_text = clean_text
        self.placeholders = placeholders
        self.input_keys = input_keys

        self.iterables = iterables

    def eval_placeholder(self, placeholder: str, variables: dict):
        """evaluate the placeholder"""
        value = variables
        for node in placeholder.split("."):
            indices = re.findall(r"\[(\d+)\]", node)
            if indices:
                node_name = node[: node.index(f"[{indices[0]}]")]
            else:
                node_name = node
            if isinstance(value, dict) and node_name in value:
                value = value[node_name]
            else:
                raise TypeError(
                    "placeholder format error, variables is not a dictionary."
                )
            for index in indices:
                if not isinstance(value, list) or int(index) >= len(value):
                    error_msg = (
                        f"In placeholder `{placeholder}`, node `{node_name}` is not a list type or "
                        f"index out of its range"
                    )
                    raise JiuWenBaseException(
                        StatusCode.PROMPT_VARIABLE_FORMAT_ERROR.code,
                        StatusCode.PROMPT_VARIABLE_FORMAT_ERROR.errmsg.format(
                            error_msg=error_msg
                        ),
                    )
                value = value[int(index)]
            if isinstance(value, list) and self.list_join_string is not None:
                try:
                    value = self.list_join_string.join([str(item) for item in value])
                except Exception as e:
                    error_msg = f"Encounter invalid item in list {value}"
                    raise JiuWenBaseException(
                        StatusCode.PROMPT_VARIABLE_FORMAT_ERROR.code,
                        StatusCode.PROMPT_VARIABLE_FORMAT_ERROR.errmsg.format(
                            error_msg=error_msg
                        ),
                    ) from e
            if value is None:
                value = ""
        return value

    def format(self, **kwargs):
        """format the template using kwargs and return the formatted text"""
        self.update(**kwargs)
        return self.generate()

    def update(self, **kwargs):
        """Replace placeholders in the `clean_text` with `kwargs`, then update and return `self.value`

        Args:
            **kwargs: arguments passed in as key-value pairs for updating the variable.

        """
        executor_logger().info("Variable Formatter gets the following variables")
        self.full_placeholders = {}
        # step 1: 处理不可迭代类型的占位符
        for placeholder in self.placeholders:
            try:
                value = self.eval_placeholder(placeholder, kwargs)
                self.full_placeholders[placeholder] = (0, str(value))
            except JiuWenBaseException as error:
                executor_logger().error(error.message)
                raise error
            except Exception:
                executor_logger().warning(
                    f"Placeholder {placeholder} cannot be parsed into a valid value and "
                    f"will not be replaced"
                )

        # step 2: 处理可迭代类型的占位符
        for input_key, format_schema in self.iterables.items():
            try:
                value = self.eval_placeholder(input_key, kwargs)
                schema_dict = self.variable_format_schema.get(format_schema, {})
                iterable_text = self._get_iterable_text(
                    value=value, schema_dict=schema_dict
                )
                self.full_placeholders[input_key] = (1, iterable_text)
            except JiuWenBaseException as error:
                executor_logger().error(error.message)
                raise error
            except Exception:
                executor_logger().warning(
                    f"Placeholder {input_key} cannot be parsed into a valid value and "
                    f"will not be replaced"
                )

    def update_and_generate_with_index(self, index, **kwargs):
        """Replace placeholders in the `clean_text` with `index` and `kwargs`, then update and return `self.value`.
        This method should be used to deal with placeholders in iterable variables.

        Args:
            index: the index in the iterable variables
            **kwargs: arguments passed in as key-value pairs for updating the variable.

        """
        formatted_text = self.clean_text
        # step 1: 处理不可迭代类型的占位符
        for placeholder in self.placeholders:
            if placeholder == "index":
                formatted_text = formatted_text.replace(
                    "${" + placeholder + "}", str(index)
                )
            else:
                try:
                    value = self.eval_placeholder(placeholder, kwargs)
                    formatted_text = formatted_text.replace(
                        "${" + placeholder + "}", str(value)
                    )
                except JiuWenBaseException as error:
                    executor_logger().error(error.message)
                    raise error
                except Exception:
                    executor_logger().warning(
                        f"Placeholder {placeholder} cannot be parsed into a valid value and "
                        f"will not be replaced"
                    )

        # step 2: 处理可迭代类型的占位符
        for input_key, format_schema in self.iterables.items():
            try:
                value = self.eval_placeholder(input_key, kwargs)
                schema_dict = self.variable_format_schema.get(format_schema, {})
                iterable_text = self._get_iterable_text(
                    value=value, schema_dict=schema_dict
                )
                formatted_text = formatted_text.replace(
                    "${" + input_key + "=>" + format_schema + "}", str(iterable_text)
                )
            except JiuWenBaseException as error:
                executor_logger().error(error.message)
                raise error
            except Exception:
                executor_logger().warning(
                    f"Placeholder {input_key} cannot be parsed into a valid value and "
                    f"will not be replaced"
                )
        self.value = formatted_text
        return self.value

    def generate(self):
        """filling placeholders."""
        formatted_text = self.clean_text
        for placeholder in self.placeholders:
            if placeholder in self.full_placeholders:
                formatted_text = formatted_text.replace(
                    "${" + placeholder + "}", self.full_placeholders[placeholder][1]
                )

        for input_key, format_schema in self.iterables.items():
            if input_key in self.full_placeholders:
                formatted_text = formatted_text.replace(
                    "${" + input_key + "=>" + format_schema + "}",
                    self.full_placeholders[input_key][1],
                )

        self.value = formatted_text
        return self.value

    def _get_iterable_text(self, value, schema_dict):
        """_get_iterable_text"""
        if not isinstance(value, list):
            raise InputKeyError(f"Input '{value}' in variables not a list")

        textable = VariableFormatter(
            text=schema_dict["item_format"],
            variable_format_schema=self.variable_format_schema,
        )
        value = [{"item": item} for item in value]
        iterable_text = schema_dict["join_string"].join(
            textable.update_and_generate_with_index(index=index, **dict_variable)
            for index, dict_variable in enumerate(value, start=1)
        )
        return iterable_text
