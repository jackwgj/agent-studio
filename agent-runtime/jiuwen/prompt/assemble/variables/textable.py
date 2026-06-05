#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""Variable class for processing string-type placeholders"""

import re

from jiuwen.common.log.base import logger
from jiuwen.prompt.assemble.variables.variable import Variable
from jiuwen.prompt.common.exception.assemble import VariableInitError, InputKeyError


class TextableVariable(Variable):
    """Variable class for processing string-type placeholders.

    Args:
        text (str, optional): a string containing placeholders enclosed by {{ and }}.
        name (str, optional): name of the variable. Default: ``default``.

    Raises:
        VariableInitError: when pass in wrong arguments for instantiate the variable object.
        InputKeyError: when pass in wrong arguments for updating the variable.
    """

    def __init__(self, text: str, name: str = "default"):
        clean_text = text
        placeholders = []
        input_keys = []
        placeholder_matches = re.finditer(r"\{\{([^{}]*)\}\}", text)
        for match in placeholder_matches:
            placeholder = match.group(1).strip()
            if len(placeholder) == 0:
                raise VariableInitError("Placeholders cannot be empty string.")
            if placeholder not in placeholders:
                placeholders.append(placeholder)
            input_key = placeholder.split(".")[0]
            if input_key not in input_keys:
                input_keys.append(input_key)
            clean_text = clean_text.replace(match.group(0), "{{" + placeholder + "}}")
        self.text = clean_text
        self.placeholders = placeholders
        super().__init__(name=name, input_keys=input_keys)

    def update(self, **kwargs):
        """Replace placeholders in the text with passed-in key-values and update `self.value`.

        Args:
            **kwargs: arguments passed in as key-value pairs for updating the variable.

        """
        formatted_text = self.text
        for placeholder in self.placeholders:
            value = kwargs
            try:
                for node in placeholder.split("."):
                    if isinstance(value, dict):
                        value = value.get(node)
                    else:
                        value = getattr(value, node)
            except Exception as error:
                raise InputKeyError(
                    f"Error parsing the placeholder `{placeholder}`."
                ) from error
            if not isinstance(value, (str, int, float, bool)):
                logger.info(
                    f"Converting non-string value `{placeholder}` using str(). "
                    f"Please check if the style is desirable.\n"
                )
            formatted_text = formatted_text.replace(
                "{{" + placeholder + "}}", str(value)
            )
        self.value = formatted_text
