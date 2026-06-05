#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

import json
from typing import List, Dict, Tuple, Literal

from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.prompt.selector.memory.prompt_library import (
    PROMPT_EXTRACT_VAR,
    PROMPT_VALIDATE_VAR,
)
from jiuwen.prompt.selector.memory.prompt_library import PROMPT_EXTRACT_VAR_SUMMARY_IN
from jiuwen.prompt.selector.memory.prompt_library import (
    VARIABLE_OPERATION_TASK_NAME,
    VAR_FMT,
)
from jiuwen.prompt.selector.memory.utils.conversation import conv2str
from jiuwen.prompt.selector.memory.utils.parse_result import (
    try_load_json,
    try_parse_json,
)
from jiuwen.prompt.selector.memory.utils.schema import (
    create_extract_schema,
    create_validate_schema,
)
from jiuwen.prompt.selector.memory.utils.schema import wrap_schema_in_openai_format


def _format_var_def(var_info: dict, language: str) -> str:
    """Format variable definitions (name, description, value) into a single string"""
    var_fmt = VAR_FMT[language]
    return "\n- ".join(
        (
            var_fmt.format(name=name, desc=desc, val=val)
            for name, (desc, val) in var_info.items()
        )
    )


def _construct_formatted_query(
    history: List[Dict],
    operation: Literal["extract", "validate"],
    language: str,
    var_info: Dict,
    extra_prompt: str,
    summary_in: str,
) -> List[Dict]:
    prompt = (
        PROMPT_EXTRACT_VAR[language]
        if operation == "extract"
        else PROMPT_VALIDATE_VAR[language]
    )
    summary = (
        PROMPT_EXTRACT_VAR_SUMMARY_IN[language].format(summary_in) if summary_in else ""
    )
    return prompt.format(
        conversation=conv2str(history, language=language),
        var_def=_format_var_def(var_info, language),
        extra=extra_prompt,
        summary=summary,
    )


def _filter_out_null(val: str) -> str:
    """Filter out `null` value in returned string"""
    if isinstance(val, str) and val and (val.strip("`").casefold() != "null"):
        return val
    return ""


def create_variable_operation_messages(
    history: List,
    var_def: Dict,
    var_val: Dict,
    language: str = "zh_CN",
    operation: Literal["extract", "validate"] = "extract",
    include_reasoning: bool = True,
    example_json: bool = True,
    summary_in: str = "",
) -> Tuple[List[Dict], Dict]:
    """Create messages and response_format for querying LLM regarding variable extraction/validation"""
    schema_name = VARIABLE_OPERATION_TASK_NAME[language][operation]
    if operation == "extract":
        response_schema = wrap_schema_in_openai_format(
            create_extract_schema(var_def, include_reasoning), name=schema_name
        )
        schema_fields = (
            {"reasoning": "string", "value": "string"}
            if include_reasoning
            else {"value": "string"}
        )
    elif operation == "validate":
        # Trim down variables without values - they don't need to be validated
        var_def = {name: desc for name, desc in var_def.items() if var_val.get(name)}
        response_schema = wrap_schema_in_openai_format(
            create_validate_schema(var_def, include_reasoning), name=schema_name
        )
        schema_fields = (
            {"reasoning": "string", "correctness": "boolean"}
            if include_reasoning
            else {"correctness": "boolean"}
        )
        # Filter out empty string values
        var_val = {name: value for name, value in var_val.items() if value}
    else:
        raise ValueError(
            'Unknown variable operation type, valid values are "extract" and "validate".'
        )

    if example_json:
        extra_prompt = "\n" + json.dumps(
            {var_name: schema_fields for var_name in var_def}
        )
    else:
        extra_prompt = ""

    messages = _construct_formatted_query(
        history,
        operation,
        language,
        {name: (desc, var_val.get(name, "null")) for name, desc in var_def.items()},
        extra_prompt,
        summary_in,
    )

    return messages, response_schema


def parse_llm_response_as_json(
    response: BaseMessage, attempt_raw_decode: bool = False
) -> Tuple[bool, Dict]:
    """Attempt to parse LLM response as json object"""
    # Try to load response as json directly
    success, result_dict = try_load_json(response.content)
    if success:
        return success, result_dict

    # Try to raw-decode response as json
    if attempt_raw_decode:
        return try_parse_json(response.content)

    # Failure
    return False, {}


def clean_extraction_result(var_defs: Dict, result_dict: Dict) -> Dict:
    """Clean extraction result to a dictionary that maps name to value"""
    cleaned_dict = dict()
    for name, raw_val in result_dict.items():
        if name not in var_defs:
            continue
        val = None
        if isinstance(raw_val, str):
            val = _filter_out_null(raw_val)
        elif isinstance(raw_val, dict):
            val = _filter_out_null(raw_val.get("value", "null"))
        elif isinstance(raw_val, list) and len(raw_val) > 0:
            val = _filter_out_null(raw_val[-1])
        if val:
            cleaned_dict[name] = val
    return cleaned_dict


def clean_validation_result(var_defs: Dict, result_dict: Dict) -> Dict[str, bool]:
    """Clean validation result to a dictionary that maps name to correctness"""
    cleaned_dict = dict()
    for name, raw_val in result_dict.items():
        if name not in var_defs:
            continue
        val = None
        if isinstance(raw_val, bool):
            val = raw_val
        if isinstance(raw_val, str) and raw_val.casefold() in ["true", "false"]:
            val = raw_val[0].casefold() == "t"
        elif isinstance(raw_val, dict):
            val = raw_val.get("correctness", None)
        elif isinstance(raw_val, list) and len(raw_val) > 0:
            val = raw_val[-1]
        if isinstance(val, bool):
            cleaned_dict[name] = val
    return cleaned_dict
