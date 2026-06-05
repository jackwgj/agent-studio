#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

__all__ = [
    "get_template_formatter",
    "ROLE_LOOKUP",
    "TOOLCALL_TEMPLATE",
    "TOOLCALL_SEP_FMT",
    "TOOLCALL_FAILURE",
    "PROMPT_EXTRACT_VAR",
    "PROMPT_VALIDATE_VAR",
    "PROMPT_UPDATE_SUMMARY",
    "PROMPT_UPDATE_SUMMARY_NO_TOPICS",
    "PROMPT_GENERATE_SUMMARY_NO_TOPICS",
    "PROMPT_EXTRACT_VAR_SUMMARY_IN",
    "VARIABLE_OPERATION_TASK_NAME",
    "VAR_FMT",
]

import os.path

from jiuwen.prompt import TemplateManager
from jiuwen.prompt.selector.memory.prompt_library import common
from jiuwen.prompt.selector.memory.prompt_library import extract_var
from jiuwen.prompt.selector.memory.prompt_library import validate_var
from jiuwen.prompt.selector.memory.prompt_library.utils import get_template_formatter

prompt_root_dir = os.path.join(os.path.split(__file__)[0], "templates")
manager = TemplateManager()
for subdir in ["summary_generator", "summary_updater", "variable_extractor"]:
    manager.register_in_bulk(os.path.join(prompt_root_dir, subdir))

PROMPT_EXTRACT_VAR_SUMMARY_IN = dict(zh_CN=extract_var.SUMMARY_INPUT_CN)

PROMPT_EXTRACT_VAR = dict(zh_CN=get_template_formatter("MEM_VAR_EXTRACTION_CN"))

PROMPT_VALIDATE_VAR = dict(zh_CN=get_template_formatter("MEM_VAR_VALIDATION_CN"))

PROMPT_UPDATE_SUMMARY = dict(zh_CN=get_template_formatter("MEM_SUM_UPDATE_SUMMARY_CN"))

PROMPT_UPDATE_SUMMARY_NO_TOPICS = dict(
    zh_CN=get_template_formatter("MEM_SUM_UPDATE_SUMMARY_NO_TOPICS_CN")
)

PROMPT_GENERATE_SUMMARY = dict(
    zh_CN=get_template_formatter("MEM_SUM_GENERATE_SUMMARY_CN")
)

PROMPT_GENERATE_SUMMARY_NO_TOPICS = dict(
    zh_CN=get_template_formatter("MEM_SUM_GENERATE_SUMMARY_NO_TOPICS_CN")
)


ROLE_LOOKUP = dict(zh_CN=common.ROLE_LOOKUP_CN)

TOOLCALL_TEMPLATE = dict(zh_CN=common.TOOLCALL_TEMPLATE_CN)

TOOLCALL_SEP_FMT = dict(zh_CN=common.TOOLCALL_SEP_FMT_CN)

TOOLCALL_FAILURE = dict(zh_CN=common.TOOLCALL_FAILURE_CN)

VAR_FMT = dict(zh_CN=extract_var.VAR_FMT_CN)

VARIABLE_OPERATION_TASK_NAME = dict(
    zh_CN=dict(extract=extract_var.TASK_NAME_CN, validate=validate_var.TASK_NAME_CN)
)

SUPPORTED_LANGUAGE = frozenset(
    {
        "zh_CN",
    }
)
