#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
Contains a collection of string utility functions
"""

import re

from jiuwen.orchestration.flow.constant import (
    BPMN_VARIABLE_POOL_SEPARATOR,
    MAXIMUM_LENGTH_OF_A_REGULAR_STRING,
)


def split_string(origin_str: str) -> list:
    """
    @param origin_str: 原始字符串，例如："a_1.b.c[1].d"
    @return: 原始字符串切分后的列表，对应参数示例的结果为：["a_1", "b", "c", 1, "d"]
    """
    # 使用正则表达式进行分割
    param_list = origin_str.split(BPMN_VARIABLE_POOL_SEPARATOR)
    final_list = []
    pattern = r"^([\w]+)((?:\[\d+\])*)$"
    for param in param_list:
        # 限制待匹配字符串长度，避免ReDos攻击
        match = re.match(pattern, param[:MAXIMUM_LENGTH_OF_A_REGULAR_STRING])
        if match:
            final_list.append(match.group(1))
            index = match.group(2)
            numbers = re.findall(r"\d+", index)
            final_list += [
                int(number) if str.isdigit(number) else number for number in numbers
            ]
    return final_list


def get_ref_str(origin_str: str):
    """
    从匹配形如"${start123.p2}"的字符串中提取引用字符串 "start123.p2"
    """
    pattern = re.compile(r"\$\{(.+?)\}")
    # 限制待匹配字符串长度，避免ReDos攻击
    match = pattern.search(origin_str[:MAXIMUM_LENGTH_OF_A_REGULAR_STRING])
    if match:
        return match.group(1)
    return ""


def is_boolean_string(input_string: str) -> bool:
    """check input_string is boolean string"""
    if not isinstance(input_string, str):
        raise ValueError(
            f"input_string should be a string, but got {type(input_string)}"
        )
    return input_string.strip("'\"").lower() in ["true", "false"]


def string_to_bool(input_string: str) -> bool:
    """convert string to boolean value"""
    if not isinstance(input_string, str):
        raise ValueError(
            f"input_string should be a string, but got {type(input_string)}"
        )
    return input_string.strip("'\"").lower() == "true"
