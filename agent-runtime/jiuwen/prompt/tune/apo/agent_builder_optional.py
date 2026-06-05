# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
agentBuilder chain Apo optimizer for agent-builderoptional
"""

import json

from jiuwen.common.exception.status_code import StatusCode
from jiuwen.prompt.common.exception.base import JiuWenBaseException
from jiuwen.prompt.tune.base import (
    MESSAGE_AGENT_BUILDER_RAW_CONTENT_KEY,
    AGENT_BUILDER_POC_MODEL_NAME,
)

poc_special_token = ["<unused0>", "<unused1>", "<unused2>", "<unused3>"]
special_token = ["[unused9]", "[unused10]", "[unused11]", "[unused12]"]


def agent_builder_label_message(label_message, name, args, model_name) -> dict:
    """agent_builder_label_message"""

    content_template = "{special_token1}{name}|{args}{special_token2}"
    try:
        if model_name == AGENT_BUILDER_POC_MODEL_NAME:
            raw_content = content_template.format(
                special_token1=poc_special_token[2],
                special_token2=poc_special_token[3],
                name=name,
                args=json.dumps(args, ensure_ascii=False),
            )
        else:
            raw_content = content_template.format(
                special_token1=special_token[2],
                special_token2=special_token[3],
                name=name,
                args=json.dumps(args, ensure_ascii=False),
            )
    except Exception as e:
        raise JiuWenBaseException(
            error_code=StatusCode.PROMPT_OPTIMIZE_CASE_CONTENT_ERROR.code,
            message=StatusCode.PROMPT_OPTIMIZE_CASE_CONTENT_ERROR.errmsg,
        ) from e
    label_message[MESSAGE_AGENT_BUILDER_RAW_CONTENT_KEY] = raw_content
    return label_message
