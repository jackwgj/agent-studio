#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
common llm service
"""

import os
import traceback
from abc import abstractmethod
from typing import List, Dict, Iterator

from jiuwen.common.configs.env_constants import PROMPT_MODEL_TYPE_KEY
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.prompt.common.config import LLMModelInfo
from pydantic import BaseModel, Field


class BaseLLMService(BaseModel):
    """base llm service"""

    add_prefix: bool = Field(default=True)
    model_info: LLMModelInfo = Field(default=LLMModelInfo())

    @abstractmethod
    def full_chat(self, messages: List[dict], extra_info: Dict = None):
        """full chat abstract method"""

    @abstractmethod
    def streaming_chat(self, messages: List[dict], extra_info: Dict = None):
        """streaming chat abstract method"""


class AgentBuilderLLMService(BaseLLMService):
    """agentBuilder llm service"""

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
            )
            result = chat_agent.invoke(
                LanguageModelInput(
                    messages=ModelUtil.switch_message(messages=messages), tools=None
                )
            )

            return dict(content=result.content)
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
    ) -> Iterator[Dict[str, str]]:
        """streaming chat"""
        if self.add_prefix:
            messages = self.system_message + messages
        try:
            chat_agent = ModelFactory().get_model(
                model_type=self.model_info.model_source,
                model_name=self.model_info.model,
                temperature=self.model_info.temperature,
                top_p=self.model_info.top_p,
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
            logger.error(f"Request agent-builderllm failed! code: {error.error_code}")
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
        llm_service_name = os.getenv(PROMPT_MODEL_TYPE_KEY, "agentBuilder_llm")
        llm_model_mapping = dict(agentBuilder_llm=AgentBuilderLLMService())
        return cls(llm_service=llm_model_mapping.get(llm_service_name))

    def chat(
        self,
        messages: List[dict],
        extra_info: Dict = None,
        model_info: LLMModelInfo = None,
        method: str = "full",
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
        return self.llm_service.full_chat(messages=messages, extra_info=extra_info)
