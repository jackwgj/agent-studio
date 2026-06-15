#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
common llm service
"""

import json
import traceback
from abc import abstractmethod
from typing import List, Dict

from agent_builder.common.exception.status_code import StatusCode
from agent_builder.common.logging.base import logger
from agent_builder.prompt.common.config import LLMModelInfo
from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from pydantic import BaseModel, Field


class BaseLLMService(BaseModel):
    """base llm service"""

    add_prefix: bool = Field(default=True)
    model_info: LLMModelInfo = Field(default=LLMModelInfo())

    @abstractmethod
    def streaming_chat(self, messages: List[dict], extra_info: Dict = None):
        """streaming chat abstract method"""

    @abstractmethod
    def astreaming_chat(self, messages: List[dict], extra_info: Dict = None):
        """streaming chat abstract method"""


class EiCloudLLMService(BaseLLMService):
    """ei cloud service"""

    system_message: list = Field(default=[])

    def full_chat(
        self, messages: List[dict], extra_info: Dict = None
    ) -> Dict[str, str]:
        """full chat"""
        if self.add_prefix:
            messages = self.system_message + messages
        try:
            chat_agent = ModelFactory().get_model(
                model_type=self.model_info.model_source,
                model_name=self.model_info.model,
                temperature=self.model_info.temperature,
                top_p=self.model_info.top_p,
                **self.model_info.headers,
            )
            result = chat_agent.invoke(
                LanguageModelInput(
                    messages=ModelUtil.switch_message(messages=messages), tools=None
                )
            )

            return dict(code=0, message="success", data=result.content)
        except JiuWenBaseException as error:
            traceback_error_msg = traceback.format_exc()
            code = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code
            msg = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                error_msg="Inner exception"
            )
            logger.error(
                f"Full request agent-builderllm failed! code: {error.error_code}, detail: {error.message}, "
                f"traceback: {traceback_error_msg}"
            )
            raise JiuWenBaseException(error_code=code, message=msg) from error
        except Exception as error:
            traceback_error_msg = traceback.format_exc()
            code = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code
            msg = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                error_msg="Inner exception"
            )
            logger.error(
                f"Full request agent-builderllm failed! Inner code: {code}, detail: {msg}, traceback: {traceback_error_msg}"
            )
            raise JiuWenBaseException(error_code=code, message=msg) from error

    def streaming_chat(
        self, messages: List[dict], extra_info: Dict = None
    ) -> Dict[str, str]:
        """streaming chat"""
        if self.add_prefix:
            messages = self.system_message + messages
        try:
            chat_agent = ModelFactory().get_model(
                model_type=self.model_info.model_source,
                model_name=self.model_info.model,
                temperature=self.model_info.temperature,
                top_p=self.model_info.top_p,
                **self.model_info.headers,
            )
            result = chat_agent.stream(
                LanguageModelInput(
                    messages=ModelUtil.switch_message(messages=messages), tools=None
                )
            )
            for item in result:
                if item.usage_metadata.code != 0:
                    yield dict(
                        code=StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code,
                        message=StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                            error_msg=item.usage_metadata.errmsg
                        ),
                        data="",
                    )
                if item.usage_metadata.finish_reason == "null":
                    yield dict(code=0, message="success", data=item.content)
        except JiuWenBaseException as error:
            logger.error(
                f"Request agent-builderllm failed! code: {error.error_code}, detail: {error.message}"
            )
            yield dict(code=error.error_code, message=error.message, data="")
        except Exception as _:
            code = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code
            msg = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                error_msg="Inner exception"
            )
            logger.error(
                f"Request agent-builderllm failed! code: {code}, detail: {msg}"
            )
            yield dict(code=code, message=msg, data="")

    async def astreaming_chat(
        self, messages: List[dict], extra_info: Dict = None
    ):
        """streaming chat"""
        if self.add_prefix:
            messages = self.system_message + messages
        try:
            chat_agent = ModelFactory().get_model(
                model_type=self.model_info.model_source,
                model_name=self.model_info.model,
                temperature=self.model_info.temperature,
                top_p=self.model_info.top_p,
                **self.model_info.headers,
            )
            result = chat_agent.stream(
                LanguageModelInput(
                    messages=ModelUtil.switch_message(messages=messages), tools=None
                )
            )
            async for item in result:
                if item.usage_metadata.code != 0:
                    yield dict(
                        code=StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code,
                        message=StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                            error_msg=item.usage_metadata.errmsg
                        ),
                        data="",
                    )
                if item.usage_metadata.finish_reason == "null":
                    yield dict(code=0, message="success", data=item.content)
        except JiuWenBaseException as error:
            logger.error(
                f"Request agent-builderllm failed! code: {error.error_code}, detail: {error.message}"
            )
            yield dict(code=error.error_code, message=error.message, data="")
        except Exception as _:
            code = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code
            msg = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                error_msg="Inner exception"
            )
            logger.error(
                f"Request agent-builderllm failed! code: {code}, detail: {msg}"
            )
            yield dict(code=code, message=msg, data="")


class LLMServiceManager(BaseModel):
    """LLMServiceManager"""

    llm_service: BaseLLMService

    @classmethod
    def get_llm_backend(cls):
        """get llm backend interface"""
        return cls(llm_service=EiCloudLLMService())

    def chat(
        self,
        messages: List[dict],
        extra_info: Dict = None,
        model_info: LLMModelInfo = None,
        method: str = "stream",
        add_prefix: bool = True,
    ):
        """llm service manager chat interface"""
        self.llm_service.add_prefix = add_prefix
        if model_info:
            self.llm_service.model_info = model_info
        if method == "stream":
            return self.llm_service.streaming_chat(
                messages=messages, extra_info=extra_info
            )
        if method == "full_chat":
            return self.llm_service.full_chat(messages=messages, extra_info=extra_info)
        raise JiuWenBaseException(
            StatusCode.LLM_FALSE_RESULT_ERROR.code,
            StatusCode.LLM_FALSE_RESULT_ERROR.errmsg.format(
                error_msg="llm service should be stream call"
            ),
        )

    def achat(
        self,
        messages: List[dict],
        extra_info: Dict = None,
        model_info: LLMModelInfo = None,
        method: str = "stream",
        add_prefix: bool = True,
    ):
        """llm service manager chat interface"""
        self.llm_service.add_prefix = add_prefix
        if model_info:
            self.llm_service.model_info = model_info
        if method == "stream":
            return self.llm_service.astreaming_chat(
                messages=messages, extra_info=extra_info
            )
        if method == "full_chat":
            return self.llm_service.full_chat(messages=messages, extra_info=extra_info)
        raise JiuWenBaseException(
            StatusCode.LLM_FALSE_RESULT_ERROR.code,
            StatusCode.LLM_FALSE_RESULT_ERROR.errmsg.format(
                error_msg="llm service should be stream call"
            ),
        )
