#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""Assemble module for prompt engine."""

__all__ = [
    "PromptAssembler",
    "Variable",
    "TextableVariable",
    "IterableVariable",
    "RetrievableVariable",
]

from jiuwen.prompt.assemble.base import PromptAssembler
from jiuwen.prompt.assemble.variables.iterable import IterableVariable
from jiuwen.prompt.assemble.variables.retrievable import RetrievableVariable
from jiuwen.prompt.assemble.variables.textable import TextableVariable
from jiuwen.prompt.assemble.variables.variable import Variable
