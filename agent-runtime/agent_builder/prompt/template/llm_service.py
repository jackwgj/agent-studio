#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
common llm service

Uses OpenAICompatibleService from the adapter layer instead of jiuwen's ModelFactory.
All LLM calls now go through OpenAI-compatible HTTP API.
"""

import json
import os
import traceback
from abc import abstractmethod
from typing import List, Dict

from agent_builder.adapter.exception_bridge import JiuWenBaseException
from agent_builder.adapter.llm_bridge import OpenAICompatibleService
from agent_builder.adapter.logger_bridge import logger
from agent_builder.common.exception.status_code import StatusCode
from agent_builder.common.llm_service.common_llm.xiaoyi_llmservice import XiaoyiLLM
from agent_builder.prompt.common.config import LLMModelInfo
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
    """
    Ei cloud service using OpenAI-compatible HTTP API.

    Replaces the original ModelFactory-based implementation with
    direct HTTP calls via OpenAICompatibleService.
    """

    system_message: list = Field(default=[])

    def full_chat(
        self, messages: List[dict], extra_info: Dict = None
    ) -> Dict[str, str]:
        """full chat using OpenAI-compatible API"""
        if self.add_prefix:
            messages = self.system_message + messages
        try:
            service = OpenAICompatibleService(model_info=self.model_info)
            result = service.full_chat(messages, extra_info=extra_info)
            return result
        except JiuWenBaseException as error:
            traceback_error_msg = traceback.format_exc()
            code = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code
            msg = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                error_msg="Inner exception"
            )
            logger.error(
                f"Full request agent builder llm failed! code: {error.error_code}, detail: {error.message}, "
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
                f"Full request agent builder llm failed! Inner code: {code}, detail: {msg}, traceback: {traceback_error_msg}"
            )
            raise JiuWenBaseException(error_code=code, message=msg) from error

    def streaming_chat(
        self, messages: List[dict], extra_info: Dict = None
    ) -> Dict[str, str]:
        """streaming chat using OpenAI-compatible API"""
        if self.add_prefix:
            messages = self.system_message + messages
        try:
            service = OpenAICompatibleService(model_info=self.model_info)
            yield from service.streaming_chat(messages, extra_info=extra_info)
        except JiuWenBaseException as error:
            logger.error(
                f"request agent builder llm failed! code: {error.error_code}, detail: {error.message}"
            )
            yield dict(code=error.error_code, message=error.message, data="")
        except Exception as _:
            code = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code
            msg = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                error_msg="Inner exception"
            )
            logger.error(
                f"request agent builder llm failed! code: {code}, detail: {msg}"
            )
            yield dict(code=code, message=msg, data="")

    async def astreaming_chat(
        self, messages: List[dict], extra_info: Dict = None
    ):
        """async streaming chat using OpenAI-compatible API"""
        if self.add_prefix:
            messages = self.system_message + messages
        try:
            service = OpenAICompatibleService(model_info=self.model_info)
            async for item in service.astreaming_chat(messages, extra_info=extra_info):
                yield item
        except JiuWenBaseException as error:
            logger.error(
                f"request agent builder llm failed! code: {error.error_code}, detail: {error.message}"
            )
            yield dict(code=error.error_code, message=error.message, data="")
        except Exception as _:
            code = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code
            msg = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                error_msg="Inner exception"
            )
            logger.error(
                f"request agent builder llm failed! code: {code}, detail: {msg}"
            )
            yield dict(code=code, message=msg, data="")


class XiaoYiLLMService(BaseLLMService):
    """inherit from default basellm"""

    system_message: list = Field(default=[])

    def __init__(self):
        system_message = [
            {
                "role": "system",
                "content": ""
                "当简洁回复更好时，你会生成简明扼要的回复。",
            }
        ]
        super().__init__(system_message=system_message)

    def full_chat(
        self, messages: List[dict], extra_info: Dict = None
    ) -> Dict[str, str]:
        """full chat"""
        streamed_replies = self.streaming_chat(messages, extra_info, stream=False)
        for reply in streamed_replies:
            reply_code = reply.get("code", StatusCode.LLM_FALSE_RESULT_ERROR.code)
            if reply_code != 0:
                return dict(
                    code=reply.get("code"), message=reply.get("message"), data=""
                )
            if reply.get("message") == "success":
                return reply
        return dict(
            code=StatusCode.LLM_FALSE_RESULT_ERROR.code,
            message=StatusCode.LLM_FALSE_RESULT_ERROR.errmsg,
            data="",
        )

    def streaming_chat(
        self, messages: List[dict], extra_info: Dict = None, stream=True
    ) -> Dict[str, str]:
        """streaming chat"""
        if self.add_prefix:
            messages = self.system_message + messages
        try:
            response = XiaoyiLLM(model_info=self.model_info).stream_chat(
                messages=messages
            )
            for content in response:
                if content:
                    if content.startswith("data:"):
                        content = content[5:]
                    result = (
                        json.loads(content).get("result", {}).get("choices", [{}])[0]
                    )
                    if result.get("finish_reason", "") == "null" and stream:
                        line_content = result.get("message", {}).get("content", "")
                        yield dict(code=0, message="success", data=line_content)
                    if not stream:
                        if result.get("finish_reason", "") == "null":
                            line_content = result.get("message", {}).get("content", "")
                            yield dict(
                                code=0,
                                message="stream_data",
                                data=line_content,
                                function_call=None,
                            )
                        elif result.get("finish_reason", "") == "stop":
                            line_content = result.get("message", {}).get("content", "")
                            function_call = result.get("message", {}).get(
                                "function_call", {}
                            )
                            yield dict(
                                code=0,
                                message="success",
                                data=line_content,
                                function_call=function_call,
                            )
        except JiuWenBaseException as error:
            logger.error(f"Request xiaoyi llm failed! code: {error.error_code}")
            yield dict(code=error.error_code, message=error.message, data="")
        except Exception as _:
            code = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.code
            msg = StatusCode.PROMPT_LLM_GENERATION_FAILED_ERROR.errmsg.format(
                error_msg="Inner exception"
            )
            logger.error(
                f"Request Xiaoyi LLM failed with unexpected error! code: {code}, detail: {msg}"
            )
            yield dict(code=code, message=msg, data="")

    def astreaming_chat(
        self, messages: List[dict], extra_info: Dict = None, stream=True
    ) -> Dict[str, str]:
        pass


class LLMServiceManager(BaseModel):
    """LLMServiceManager"""

    llm_service: BaseLLMService

    @classmethod
    def get_llm_backend(cls):
        """get llm backend interface"""
        service_name = os.getenv("SERVICE_TYPE", "xiaoyi")
        llm_model_mapping = dict(
            agentBuilder=EiCloudLLMService(), xiaoyi=XiaoYiLLMService()
        )
        return cls(llm_service=llm_model_mapping.get(service_name))

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
