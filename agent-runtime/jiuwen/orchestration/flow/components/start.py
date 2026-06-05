#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
from copy import deepcopy
from typing import Any

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.constant import USER_FIELDS, SYSTEM_FIELDS


class Start(Invokable):
    """Start node."""

    def __init__(self, conf: dict):
        self.conf = conf

    def invoke(self, inputs: dict[str, Any], **kwargs):
        """Invoke to assemble final answer."""
        self._validate_inputs(inputs)
        inputs_copy = self._fill_default_values(deepcopy(inputs))
        return dict(
            {
                SYSTEM_FIELDS: {
                    "query": inputs_copy.pop("query", ""),
                    "dialogueHistory": [],
                    "sys": inputs_copy.pop("sys", {}),
                    "conversationHistory": inputs_copy.pop("conversationHistory", []),
                },
                USER_FIELDS: inputs_copy,
            }
        )

    def _validate_inputs(self, inputs):
        """
        Check whether the user has passed in all global_variables defined in the config.
        """
        user_defined_variables = self.conf.get(USER_FIELDS, {}).get("inputs", [])
        variables_not_given = []
        for variable in [
            var for var in user_defined_variables if var.get("required", False)
        ]:
            # variable is a dict with four keys: 'id', 'required', 'sourceType', 'type'
            variable_name = variable.get("id", "")
            if variable_name not in inputs:
                variables_not_given.append(variable_name)
        if variables_not_given:
            raise JiuWenBaseException(
                StatusCode.WORKFLOW_START_MISSING_GLOBAL_VARIABLE_VALUE.code,
                StatusCode.WORKFLOW_START_MISSING_GLOBAL_VARIABLE_VALUE.errmsg.format(
                    variable_names=variables_not_given
                ),
            )

    def _fill_default_values(self, inputs):
        user_defined_variables = self.conf.get(USER_FIELDS, {}).get("inputs", [])
        default_maps = {
            var["id"]: var["default_value"]
            for var in user_defined_variables
            if "id" in var
            and var.get("default_value") is not None
            and var.get("default_value") != ""
        }
        return default_maps | inputs
