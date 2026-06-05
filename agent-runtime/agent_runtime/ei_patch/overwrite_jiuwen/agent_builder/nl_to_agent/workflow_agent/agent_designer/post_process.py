# @override(jiuwen) Fix: added error codes for model capability and response format issues
# -*- coding:utf-8 -*-
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
import re

from agent_builder.common.exception.status_code import StatusCode
from agent_builder.common.logging.base import logger

# @override(jiuwen) added error code: model capability insufficient / response format error
from jiuwen.common.exception.base import JiuWenBaseException


# @override(jiuwen) added error code: model capability insufficient / response format error


def clean_uml(uml_string):
    """将uml进行规整"""
    # 替换换行符并去掉多余的引号
    clean_string = uml_string.replace(r"\n", "\n").replace(r"\"", '"').strip()
    if "```mermaid" in clean_string:
        clean_string = clean_string.replace("```mermaid", "").replace("```", "").strip()
    return clean_string


def split_text(input_text):
    """使用正则表达式从输入文本中分割出 UML"""
    uml_pattern = r"@startuml[^@]*@enduml"
    # 查找 UML 部分
    matches = re.findall(uml_pattern, input_text, re.DOTALL)
    # 提取匹配的文本
    uml = matches[0] if matches else input_text
    return uml


def extract_info(input_text):
    """从输入文本中提取名称、介绍和Mermaid流程"""
    pattern = re.compile(
        r"@中文名称：(?P<name>[^@]*?)@英文名称：(?P<name_en>[^@]*?)"
        r"@介绍：(?P<description>[^@]*?)@Mermaid流程",
        re.DOTALL,
    )
    match = pattern.search(input_text)
    if match:
        result = {key: value.strip() for key, value in match.groupdict().items()}
        result["mermaid"] = input_text.split("@Mermaid流程：")[-1]
        return result
    # @override(jiuwen) added error code: model capability insufficient / response format error
    else:
        error_context = (
            "Schema mismatch: The output didn't follow the '@Tag：Content' format."
        )
        logger.error(f"{error_context} | Raw start: {input_text}")

        raise JiuWenBaseException(
            StatusCode.NL2AGENT_LLM_PARSE_FAILED.code,
            StatusCode.NL2AGENT_LLM_PARSE_FAILED.errmsg.format(
                error_msg=f"model response: {input_text[:50]}..."
            ),
        )
    # @override(jiuwen) added error code: model capability insufficient / response format error
