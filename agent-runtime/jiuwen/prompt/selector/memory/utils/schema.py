#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from typing import Union, Dict, List, Tuple

from pydantic import BaseModel


### General Purpose - for structured output support


def wrap_schema_in_openai_format(
    schema: Union[Dict, BaseModel], name: str = "structured_output", strict: str = True
):
    """Wrap structured output schema in an OpenAI-style format"""
    if isinstance(schema, BaseModel):
        schema = schema.model_dump()
    else:
        schema = schema.copy()
    schema["additionalProperties"] = False

    return {
        "type": "json_schema",
        "json_schema": {"strict": strict, "schema": schema, "name": name},
    }


### For Variable Extraction

# Schema template for each variable in extraction task

var_extract_def = dict(
    type="object",
    properties=dict(value={"type": "string"}),
    required=["value"],
)  # Simply extract value

var_extract_def_cot = dict(
    type="object",
    properties=dict(reasoning={"type": "string"}, value={"type": "string"}),
    required=["reasoning", "value"],
)  # Extract reasoning before value

var_validate_def = dict(
    type="object",
    properties=dict(correctness={"type": "boolean"}),
    required=["correctness"],
)  # Simply validate value

var_validate_def_cot = dict(
    type="object",
    properties=dict(reasoning={"type": "string"}, correctness={"type": "boolean"}),
    required=["reasoning", "correctness"],
)  # Extract reasoning before value

# Helper functions


def create_extract_schema(
    var_list: Union[List, Tuple, Dict], include_reasoning: bool = False
) -> Dict:
    """Create schema for variable extraction (with structured output support)"""
    template = var_extract_def_cot if include_reasoning else var_extract_def
    properties = {v: template for v in var_list}
    return dict(type="object", properties=properties, required=list(var_list))


def create_validate_schema(
    var_list: Union[List, Tuple, Dict], include_reasoning: bool = False
) -> Dict:
    """Create schema for variable validation (with structured output support)"""
    template = var_validate_def if include_reasoning else var_validate_def_cot
    properties = {v: template for v in var_list}
    return dict(type="object", properties=properties, required=list(var_list))
