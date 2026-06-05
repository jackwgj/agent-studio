#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""Configuration class to store the state of bools for different scripts access."""

import os
from typing import Union

from jiuwen.common.configs.env_constants import (
    BATCH_SIZE_KEY,
    TOOL_SELECTOR_CSB_TOKEN_KEY,
    TOOL_SELECTOR_URL_KEY,
    TOOL_SELECTOR_TIMEOUT_KEY,
)
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.security.cryptor import Crypt
from jiuwen.common.utils.singleton import Singleton
from pydantic import BaseModel, Field

SQLITE_STRING = "sqlite"
SELECTOR_STRING = "selector"
DEFAULT_TOPK = 3
DEFAULT_THRESHOLD = 0.9
DEFAULT_BATCH_SIZE = 32
DEFAULT_TIMEOUT = 30


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


class ToolsSelectConfig:
    """
    Configuration class to store ToolsSelectConfig.
    """

    def __init__(self) -> None:
        """Initialize the Config class"""
        try:
            self._load_from_env()
        except Exception as error:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_MODULE_INIT_ERROR.code,
                message=StatusCode.PROMPT_MODULE_INIT_ERROR.errmsg.format(
                    error_msg=str(error)
                ),
            ) from error

    def __getitem__(self, items):
        return self.__dict__.get(items)

    @staticmethod
    def _password_transfer(value: str):
        """_password_transfer"""
        if value:
            return Crypt().decrypt(value)
        return ""

    def _load_from_env(self):
        """load from env"""
        self.selector_url = os.getenv(TOOL_SELECTOR_URL_KEY, "")
        self.selector_csb_token = self._password_transfer(
            os.getenv(TOOL_SELECTOR_CSB_TOKEN_KEY, "")
        )
        self.selector_timeout = os.getenv(TOOL_SELECTOR_TIMEOUT_KEY, DEFAULT_TIMEOUT)
        self.topk = os.getenv(TOOL_SELECTOR_TIMEOUT_KEY, DEFAULT_TOPK)
        self.threshold = os.getenv(TOOL_SELECTOR_TIMEOUT_KEY, DEFAULT_THRESHOLD)
        self.batch_size = os.getenv(BATCH_SIZE_KEY, DEFAULT_BATCH_SIZE)


class LLMModelInfo(BaseModel):
    """
    Configuration class for model info.
    """

    url: str = Field(default="", min_length=0, max_length=128)
    token: str = Field(default="", min_length=0, max_length=128)
    model: str = Field(default="", min_length=0, max_length=128)
    type: str = Field(default="agentBuilder_llm", min_length=0, max_length=128)
    timeout: int = Field(default=20, gt=0, le=200)
    headers: Union[dict, None] = Field(default=None)
    top_p: float = Field(default=0.1, ge=0, le=1)
    temperature: float = Field(default=0.1, ge=0, le=1)
    model_source: str = Field(default="", min_length=0, max_length=128)
    api_key: str = Field(default="", min_length=0, max_length=128)

    def set_url(self, url):
        """
        set model url

        Args:
            url (str): model url
        """
        self.url = url

    def set_token(self, token):
        """
        set model token

        Args:
            token (str): model token
        """
        self.token = token

    def set_model(self, model):
        """
        set model

        Args:
            model (str): LLM
        """
        self.model = model

    def set_model_type(self, model_type):
        """
        set model type

        Args:
            type (str): model type
        """
        self.type = model_type


class PromptGlobalConfig(metaclass=Singleton):
    """
    Configuration class for global model config.
    """

    def __init__(self):
        self.llm = LLMModelInfo()
