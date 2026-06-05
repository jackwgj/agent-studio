#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""Configuration class to store the state of bools for different scripts access."""

import json
from typing import Union

from agent_builder.common.exception.status_code import StatusCode
from jiuwen.common.exception import JiuWenBaseException
from jiuwen.prompt.common.config import LLMModelInfo as JiuwenLLMModelInfo
from pydantic import Field, field_validator

HEADER_TOTAL_LENGTH_LIMIT = 65535  # 临时修改覆盖65535以适配X-AUTH-TOKEN


class PromptConfig:
    """
    Configuration class to store the state of bools for different scripts access.
    """

    def __init__(self) -> None:
        """Initialize the Config class"""
        try:
            self._load_from_application_yaml()
        except Exception as error:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_MODULE_INIT_ERROR.code,
                message=StatusCode.PROMPT_MODULE_INIT_ERROR.errmsg.format(
                    error_msg="Prompt config init error!"
                ),
            ) from error

    def __getitem__(self, items):
        return self.__dict__.get(items)

    def _load_from_application_yaml(self):
        """load from application yaml"""
        self.template_store_type = "in_memory"


class LLMModelInfo(JiuwenLLMModelInfo):
    """
    A configuration class for storing and validating model-related information.
    This class is used to define the parameters required to connect to and use a specific LLM model.
    """

    url: str = Field(default="", min_length=0, max_length=128)
    model: str = Field(default="", min_length=0, max_length=128)
    type: str = Field(default="xiaoyi_llm", min_length=0, max_length=128)
    headers: Union[dict, None] = Field(default={})
    model_source: str = Field(default="", min_length=0, max_length=128)
    api_key: str = Field(default="", min_length=0, max_length=128)
    top_p: float = Field(default=0.1, ge=0, le=1)
    temperature: float = Field(default=0.1, ge=0, le=1)
    token: str = Field(default="", min_length=0, max_length=128)

    @field_validator("headers")
    @classmethod
    def validate_headers_length(cls, value):
        """header length limit"""
        json_str = json.dumps(value, separators=(",", ":"))
        item_length = len(json_str)
        if item_length > HEADER_TOTAL_LENGTH_LIMIT:
            raise ValueError(f"total length exceed limit {HEADER_TOTAL_LENGTH_LIMIT}")
        return value
