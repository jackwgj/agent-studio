# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
agentBuilder chain Template evaluator
"""

from enum import Enum
from typing import List, Dict, Optional, Literal

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.prompt.common.check import CheckInfo
from jiuwen.prompt.common.config import LLMModelInfo
from pydantic import BaseModel, field_validator, Field, FieldValidationInfo

TASK_NAME_LENGTH_MIN_LIMIT = 1
TASK_NAME_LENGTH_MAX_LIMIT = 32

TASK_DESC_LENGTH_MIN_LIMIT = 1
TASK_DESC_LENGTH_MAX_LIMIT = 256

GET_JOBS_UPPER_BOUND = 50
GET_JOBS_MIN_LIMIT = 1
GET_JOBS_MAX_LIMIT = 50
GET_JOBS_LIMIT_DEFAULT = 10


class Case(BaseModel):
    """Definition of prompt optimization user cases."""

    messages: List[Dict] = Field(default=[])
    tools: Optional[List[Dict]] = Field(default=None)

    @field_validator("messages")
    @classmethod
    def check_message_list_content(
        cls, value: List[Dict], info: FieldValidationInfo
    ) -> List[Dict]:
        """check messages"""
        return CheckInfo.check_message_list_content(value, info.field_name)


class TaskInfo:
    """Definition of prompt optimization base task info."""

    task_id: str
    name: str
    desc: str
    create_time: str
    run_time: int

    def __init__(self, task_id: str, name: str, desc: str, create_time: str):
        """init"""
        self.task_id = task_id
        self.name = name
        self.desc = desc
        self.create_time = create_time


class OptimizeInfo(BaseModel):
    """Definition of prompt optimization info."""

    cases: List[Case] = Field(default=[])
    num_iter: int = Field(default=3)
    llm_parallel: Optional[int] = Field(default=1, gt=0, le=10)
    pop_size: Optional[int] = Field(default=3)
    eval_size: Optional[int] = Field(default=10, gt=0)
    retry_times: Optional[int] = Field(default=5)
    example_num: Optional[int] = Field(default=3, ge=0, le=10)
    cot_example_num: Optional[int] = Field(default=1, ge=0, le=5)
    optimize_method: Literal["JOINT", "EVO"] = Field(default="JOINT")
    placeholder: Optional[List] = Field(default=[])
    eval_mode: Literal["LLM", "JSON"] = Field(default="LLM")

    @field_validator("num_iter")
    @classmethod
    def check_int_content(cls, value: int, info: FieldValidationInfo) -> int:
        """check num_iter"""
        return CheckInfo.check_int_content(value, info.field_name)

    @field_validator("cases")
    @classmethod
    def check_list_info(
        cls, value: List[Case], info: FieldValidationInfo
    ) -> List[Case]:
        """check cases"""
        return CheckInfo.check_list_content(value, info.field_name)


class CompareRes(Enum):
    """Enumeration of LLM evaluate compare flag."""

    BAD = "A"
    GOOD = "B"
    SAME = "C"


class Placeholder(BaseModel):
    """Definition of prompt placeholder."""

    name: str
    description: str


class LLMModelProcess:
    """Definition of LLM invoke process."""

    chat_llm = None

    def __init__(self, model_info: LLMModelInfo):
        """init"""
        if model_info is None:
            raise JiuWenBaseException(
                error_code=StatusCode.LLM_MODEL_SOURCE_ERROR.code,
                message=StatusCode.LLM_MODEL_SOURCE_ERROR.errmsg.format(
                    error_msg="the model info input is missing."
                ),
            )
        self.chat_llm = ModelFactory().get_model(
            model_type=model_info.model_source,
            model_name=model_info.model,
            temperature=model_info.temperature,
            top_p=model_info.top_p,
        )

    def chat(self, messages: List[dict], tools=None):
        """chat"""
        result = self.chat_llm.invoke(
            LanguageModelInput(
                messages=ModelUtil.switch_message(messages=messages), tools=tools
            )
        )

        return dict(content=result.content)


def check_not_none_with_alias(value, alias):
    """check_none_with_alias"""
    if value is None:
        raise JiuWenBaseException(
            error_code=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.code,
            message=StatusCode.PROMPT_OPTIMIZE_INVALID_PARAMS_ERROR.errmsg.format(
                error_msg=f"{alias} is None"
            ),
        )
