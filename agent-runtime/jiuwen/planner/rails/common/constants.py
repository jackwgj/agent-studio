# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""constants"""

from enum import Enum
from typing import Dict, List, Any

from jiuwen.planner.rails.schema.rails_execution import (
    ExecutionRailsInputSchema,
    ExecutionRailsOutputSchema,
)
from jiuwen.planner.rails.schema.rails_input import InputRailsInputSchema
from jiuwen.planner.rails.schema.rails_output import InputRailsOutputSchema


class RailsType(Enum):
    Undefined = "undefined"
    InputRails = "input_rails"
    OutputRails = "output_rails"
    DialogueRails = "dialogue_rails"
    ExecutionRails = "execution_rails"
    RetrievalRails = "retrieval_rails"


class TriggerType(Enum):
    DefaultTrigger = "default"
    KeywordsTrigger = "keywords"
    IntentTrigger = "intent"


class BuiltinTrigger(Enum):
    """builtin triggers"""

    ALWAYS_ON = "_always_on_trigger"


def get_dataclass_member(field):
    """get members of dataclass"""
    return [field_name for field_name, _ in field.items()]


Input_Keywords_Map: Dict[str, List[Any]] = {
    RailsType.InputRails.value: get_dataclass_member(
        InputRailsInputSchema.__dataclass_fields__
    ),
    RailsType.ExecutionRails.value: get_dataclass_member(
        ExecutionRailsInputSchema.__dataclass_fields__
    ),
}

Output_Keywords_Map: Dict[str, List[Any]] = {
    RailsType.InputRails.value: get_dataclass_member(
        InputRailsOutputSchema.__dataclass_fields__
    ),
    RailsType.ExecutionRails.value: get_dataclass_member(
        ExecutionRailsOutputSchema.__dataclass_fields__
    ),
}
