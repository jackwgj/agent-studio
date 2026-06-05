#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

import json
import re
from typing import Tuple, Dict

from jiuwen.common.log.base import logger

_RAW_DECODE_MAX_LEN = 1_000_000
_regex_extract_json_code_block = re.compile(r"(?s)```(\?*)json\s{1, 5}n([\s\S]*?)```")
_regex_extract_any_code_block = re.compile(
    r"(?s)```(\?*)[A-Za-z]*\s{1,5}\n([\s\S]*?)```"
)
_regex_clean_null = re.compile(r"[\s'\"](null)[\s'\"]")


def try_load_json(json_str: str) -> Tuple[bool, Dict]:
    """Attempt to load json from string"""
    try:
        return True, json.loads(json_str)
    except json.JSONDecodeError:
        return False, {}


def _try_match_code_block(
    response: str, pattern: re.Pattern, max_len: int = _RAW_DECODE_MAX_LEN
):
    """Attempt to load json from specific code block pattern in string"""
    # Trim parts of response text that are absolutely irrelevant
    first_code_block_symbol = response.find("```")
    if first_code_block_symbol < 0:
        return False, {}
    last_code_block_symbol = response.rfind("```")

    # Do not attempt to regex match full string if input string is too long
    if last_code_block_symbol - first_code_block_symbol > max_len:
        block = pattern.search(
            response[first_code_block_symbol : first_code_block_symbol + max_len]
        )
        if block is None:
            block = pattern.search(
                response[last_code_block_symbol - max_len : last_code_block_symbol]
            )
    else:
        block = pattern.search(
            response, first_code_block_symbol, last_code_block_symbol + 3
        )

    if block is None:
        return False, {}
    return try_load_json(block.group(2))


def _try_load_json_block(response: str, max_len: int = _RAW_DECODE_MAX_LEN):
    """Attempt to load json from json code block in string"""
    return _try_match_code_block(response, _regex_extract_json_code_block, max_len)


def _try_load_any_block(response: str, max_len: int = _RAW_DECODE_MAX_LEN):
    """Attempt to load json from code block in string"""
    return _try_match_code_block(response, _regex_extract_any_code_block, max_len)


def _raw_decode(response: str, max_len: int = _RAW_DECODE_MAX_LEN):
    """Attempt to load json object from string by raw-decode"""
    # Trim parts of response text that are absolutely irrelevant
    first_left_curly = response.find("{")
    if first_left_curly < 0:
        return False, {}
    last_right_curly = response.rfind("}")
    text = response[first_left_curly : last_right_curly + 1][
        :max_len
    ]  # trim to max length
    del response

    decoder = json.JSONDecoder()
    attempt_counter, i, n = 0, 0, len(text)

    # Attempt to parse at any possible start
    while i < n:
        if text[i] == "{":  # only matches json object
            attempt_counter += 1
            try:
                result_dict, _ = decoder.raw_decode(text, i)
                if result_dict and any(
                    isinstance(v, dict) for v in result_dict.values()
                ):
                    logger.info(
                        f"Successfully raw-decoded LLM response as JSON [in {attempt_counter} attempts]"
                    )
                    return True, result_dict
                return False, {}
            except json.JSONDecodeError:
                if not i % 100:
                    logger.info(
                        f"Attempting to raw-decode LLM response as JSON [{attempt_counter} attempts so far]"
                    )
        i += 1

    return False, {}


def try_parse_json(response: str) -> Tuple[bool, Dict]:
    """Attempt to parse json object from string by matching code blocks and raw-decode"""

    # Before trying anything, let's see if it's simple cases like [null"] or ["null]
    response = _regex_clean_null.sub(r"\1", response)
    success, result_dict = try_load_json(response)
    if success:
        return success, result_dict

    # Decoding attempts
    for func in [_try_load_json_block, _raw_decode]:
        success, result_dict = func(response)
        if success:
            return success, result_dict

    # Output warning to help locate LLM-response-related issues
    logger.warning(f"Unable to load JSON from LLM response:\n{response}")

    return False, {}
