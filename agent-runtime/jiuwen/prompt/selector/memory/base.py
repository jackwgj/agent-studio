#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

import asyncio
from asyncio import Future
from concurrent.futures import ThreadPoolExecutor
from enum import Enum
from logging import StreamHandler
from logging.handlers import RotatingFileHandler
from typing import Optional, Dict, List, Tuple, Literal

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.prompt.selector.memory.extract_variables import (
    clean_extraction_result,
    clean_validation_result,
)
from jiuwen.prompt.selector.memory.extract_variables import (
    create_variable_operation_messages,
)
from jiuwen.prompt.selector.memory.extract_variables import parse_llm_response_as_json
from jiuwen.prompt.selector.memory.query_llm import LLMClient
from jiuwen.prompt.selector.memory.update_summary import (
    construct_formatted_summary_prompt,
)
from jiuwen.prompt.selector.memory.utils.conversation import conv2str


class ExtractorError(Enum):
    SUCCESS = 0
    FAILED = 1


class SummarizationError(Enum):
    SUCCESS = 0
    FAILED = 1


class MemoryExtractor:
    """Memory Variable Extractor"""

    def __init__(
        self,
        default_client: Optional[LLMClient] = None,
        async_strategy: Literal["none", "threads"] = "none",
    ):
        self.default_client = default_client
        self.__async_strategy = "none"
        self.executor = None
        self.async_strategy = async_strategy
        self.status_success = ExtractorError.SUCCESS
        self.status_failed = ExtractorError.FAILED

    def __del__(self):
        if self.executor:
            self.executor.shutdown()

            def is_logger_handler_open(handler: object) -> bool:
                if isinstance(handler, (RotatingFileHandler, StreamHandler)):
                    stream_ = handler.stream
                    return bool(stream_ and not stream_.closed)
                return True

            # Check if logger is already closing, if so, don't log
            if all(is_logger_handler_open(handler) for handler in logger.handlers):
                logger.info("MemoryExtractor thread pool deleted")
        self.executor = None

    @property
    def async_strategy(self) -> str:
        """异步调用策略：threads会创建1条线程的ThreadPoolExecutor"""
        return self.__async_strategy

    @async_strategy.setter
    def async_strategy(self, async_strategy: Literal["none", "threads"]):
        """异步调用策略：threads会创建1条线程的ThreadPoolExecutor"""
        if async_strategy != self.__async_strategy:
            self.__async_strategy = async_strategy
            logger.info(
                f'MemoryExtractor\'s async strategy is set to "{async_strategy}"'
            )
        if self.executor is None and async_strategy != "none":
            self.executor = ThreadPoolExecutor(max_workers=1)
            logger.info("MemoryExtractor created a thread pool with 1 thread")
        elif self.executor is not None and async_strategy == "none":
            logger.info("MemoryExtractor shutting down thread pool")
            self.executor.shutdown()
            self.executor = None
            logger.info("MemoryExtractor thread pool deleted")

    @staticmethod
    def _process_input_dictionaries(
        summaries_params: List[Dict],
        params_value: List[Dict],
        summaries: Optional[Dict],
    ) -> Tuple[Dict, Dict, Dict]:
        """Convert summaries_params, params_value, summaries format to required format for prompt generation"""
        var_def, var_val = dict(), dict()

        for param in summaries_params:
            var_def[param["name"]] = param["description"]
            val = param.get("default_value")
            if val is not None:
                var_val[param["name"]] = val

        for param in params_value:
            var_val[param["name"]] = param["value"]

        if summaries is None:
            return var_def, var_val, {}

        return var_def, var_val, summaries

    @staticmethod
    def _process_output_dict(
        var_def: Dict[str, str], var_val: Dict[str, str], null_val: str = ""
    ) -> Dict[str, str]:
        """Process return value to ensure all defined variable is returned"""
        var_val = clean_extraction_result(var_def, var_val)
        return {name: var_val.get(name, null_val) for name in var_def}

    @staticmethod
    def _create_client_from_model_info(
        model_info: Optional[Dict[str, str]],
    ) -> Optional[LLMClient]:
        """Create LLM Client from model_info dictionary"""
        if model_info:
            model_info = model_info.copy()
            model_type, model_name = (
                model_info.pop("model_source"),
                model_info.pop("model"),
            )
            return LLMClient(model_type, model_name, **model_info)
        return None

    @staticmethod
    def _prepare_extraction_query(
        conversation_history: List[Dict],
        var_def: Dict,
        var_val: Dict,
        summaries: Dict,
        language: str = "zh_CN",
    ) -> Tuple[List[Dict], Dict]:
        """Constructs the messages and schema required for LLM call"""
        summary_in = summaries.get("content", "")
        messages, schema = create_variable_operation_messages(
            conversation_history,
            var_def,
            var_val,
            language,
            operation="extract",
            include_reasoning=True,
            example_json=True,
            summary_in=summary_in,
        )
        return messages, schema

    @staticmethod
    def _prepare_validation_query(
        conversation_history: List[Dict],
        var_def: Dict,
        var_val: Dict,
        summaries: Dict,
        language: str = "zh_CN",
    ) -> Tuple[List[Dict], Dict]:
        """Constructs the messages and schema required for LLM call"""
        summary_in = summaries.get("content", "")
        messages, schema = create_variable_operation_messages(
            conversation_history,
            var_def,
            var_val,
            language,
            operation="validate",
            include_reasoning=True,
            example_json=True,
            summary_in=summary_in,
        )
        return messages, schema

    @staticmethod
    async def _async_extraction_task_wrapper(
        extraction_future: Future,
    ) -> Tuple[ExtractorError, Dict]:
        """Wraps self._async_extraction_task_internal future, trim down unnecessary returned values"""
        result = await extraction_future
        return result[:2]

    def query(
        self,
        conversation_history: List[Dict],
        summaries_params: List[Dict],
        params_value: List[Dict],
        model_info: Optional[Dict] = None,
        summaries: Optional[Dict] = None,
        validation: Literal["none", "all"] = "none",
        language: str = "zh_CN",
    ) -> Tuple[ExtractorError, Dict]:
        """Entry point for variable extraction query"""
        # Return status code: 0 - success, 1 - failed to generate extraction json, 2 - failed to generate validation
        # 1. Create LLM Client
        client = self._create_client_from_model_info(model_info) or self.default_client

        # 2. Prepare variable definition information
        var_def, var_val, summaries = self._process_input_dictionaries(
            summaries_params, params_value, summaries
        )

        # 3. Generate extraction prompt
        messages, schema = self._prepare_extraction_query(
            conversation_history, var_def, var_val, summaries, language
        )

        # 4. Get extraction result from LLM
        status_code, extraction_result, var_val = self._process_extraction_result(
            response=client.query(messages, response_format=schema), var_def=var_def
        )

        # If no validation, already done!
        if validation == "none" or status_code == self.status_failed:
            return status_code, extraction_result

        # 5. Generate validation prompt
        messages, schema = self._prepare_validation_query(
            conversation_history, var_def, var_val, summaries, language
        )

        # 6. Get validation result from LLM
        return self._process_validation_result(
            response=client.query(messages, response_format=schema),
            var_def=var_def,
            var_val=var_val,
        )

    async def async_query(
        self,
        conversation_history: List[Dict],
        summaries_params: List[Dict],
        params_value: List[Dict],
        model_info: Optional[Dict] = None,
        summaries: Optional[Dict] = None,
        validation: Literal["none", "all"] = "none",
        language: str = "zh_CN",
    ):
        """Async entry point for variable extraction query"""
        # Return status code: 0 - success, 1 - failed to generate extraction json, 2 - failed to generate validation
        # 1. Create LLM Client
        client = self._create_client_from_model_info(model_info) or self.default_client

        # 2. Prepare variable definition information
        var_def, var_val, summaries = self._process_input_dictionaries(
            summaries_params, params_value, summaries
        )

        # 3. Generate extraction prompt
        messages, schema = self._prepare_extraction_query(
            conversation_history, var_def, var_val, summaries, language
        )

        # 4. Create extraction task future
        return await self._create_async_future(
            client,
            messages,
            schema,
            var_def,
            validation,
            validation_info=dict(
                conversation_history=conversation_history,
                summaries=summaries,
                language=language,
            ),
        )

    def _process_extraction_result(
        self, response: BaseMessage, var_def: Dict[str, str]
    ) -> Tuple[ExtractorError, Dict, Dict]:
        """Constructs the messages and schema required for LLM call"""
        # Get extraction result from LLM
        success, result = parse_llm_response_as_json(response, attempt_raw_decode=True)
        if not success:
            return self.status_failed, self._process_output_dict(var_def, result), {}
        var_val = clean_extraction_result(var_def, result)
        extraction_result = self._process_output_dict(var_def, var_val)
        return self.status_success, extraction_result, var_val

    def _process_validation_result(
        self, response: BaseMessage, var_def: Dict[str, str], var_val: Dict
    ) -> Tuple[ExtractorError, Dict[str, str]]:
        """Constructs the messages and schema required for LLM call"""
        # Get validation result from LLM
        success, result = parse_llm_response_as_json(response, attempt_raw_decode=True)
        if not success:
            return self.status_failed, self._process_output_dict(var_def, var_val)
        result = clean_validation_result(var_def, result)
        # Use validation result to filter extraction result
        var_val = {
            name: value for name, value in var_val.items() if result.get(name, True)
        }
        return self.status_success, self._process_output_dict(var_def, var_val)

    def _create_async_future(
        self,
        client: LLMClient,
        messages: List[Dict],
        schema: Dict,
        var_def: Dict,
        validation: str,
        validation_info: Dict[str, object],
    ) -> Future[Tuple[ExtractorError, Dict]]:
        """Assemble the async futures to return by self.async_query"""
        loop = asyncio.get_running_loop()

        internal_extraction_task = loop.create_task(
            self._async_extraction_task_internal(client, messages, schema, var_def),
            name="__internal_extraction_task",
        )  # for internal use

        extraction_future = loop.create_task(
            self._async_extraction_task_wrapper(internal_extraction_task),
            name="extraction_task",
        )  # actually returned extraction future

        # If no validation, return a dummy future for it
        if validation == "none":
            return extraction_future

        # 5. Create validation task future
        validation_future = loop.create_task(
            self._async_validation_task(
                client,
                var_def=var_def,
                extraction_future=internal_extraction_task,
                **validation_info,
            ),
            name="validation_task",
        )

        return validation_future

    async def _async_extraction_task_internal(
        self, client: LLMClient, messages: List[Dict], schema: Dict, var_def: Dict
    ) -> Tuple[ExtractorError, Dict, Dict]:
        """Performs value extraction of variables asynchronously, need to use self._async_extraction_task_wrapper"""
        if self.__async_strategy == "threads":
            response = await client.async_query(
                messages, response_format=schema, __executor=self.executor
            )
        else:
            response = client.query(messages, response_format=schema)
        status_code, extraction_result, var_val = self._process_extraction_result(
            response, var_def
        )
        return status_code, extraction_result, var_val

    async def _async_validation_task(
        self,
        client: LLMClient,
        conversation_history: List[Dict],
        var_def: Dict,
        extraction_future: Future,
        summaries: Dict,
        language: str = "zh_CN",
    ) -> Tuple[ExtractorError, Dict]:
        """Performs validation of extracted variable values asynchronously"""
        status_code, extraction_result, var_val = await extraction_future

        if status_code != self.status_success:
            return ExtractorError.FAILED, extraction_result

        messages, schema = self._prepare_validation_query(
            conversation_history, var_def, var_val, summaries, language
        )
        if self.__async_strategy == "threads":
            response = await client.async_query(
                messages, response_format=schema, __executor=self.executor
            )
        else:
            response = client.query(messages, response_format=schema)
        status_code, validated_result = self._process_validation_result(
            response, var_def, var_val
        )

        if status_code != self.status_success:
            return status_code, extraction_result

        return status_code, validated_result


class SummaryUpdator:
    """Summary Updator"""

    def __init__(
        self,
        default_client: Optional[LLMClient] = None,
        async_strategy: Literal["none", "threads"] = "none",
    ):
        self.default_client = default_client
        self.async_strategy = async_strategy
        self.status_success = SummarizationError.SUCCESS
        self.status_failed = SummarizationError.FAILED

    @staticmethod
    def _create_client_from_model_info(
        model_info: Optional[Dict[str, str]],
    ) -> Optional[LLMClient]:
        """Create LLM Client from model_info dictionary"""
        # this function is taken from the MemoryExtractor class
        if model_info:
            model_info = model_info.copy()
            model_type, model_name = (
                model_info.pop("model_source"),
                model_info.pop("model"),
            )
            return LLMClient(model_type, model_name, **model_info)
        return None

    @staticmethod
    def _prepare_summary_update_query(
        conversation_history: List[dict],
        topics: List[str] = None,
        language: str = "zh_CN",
        summary: str = "",
    ) -> List[Dict]:
        """Constructs the messages required for the LLM call"""

        topics_str = ", ".join(topics) if len(topics) else ""
        linearized_conv = conv2str(conversation_history, language=language)

        try:
            prompt = construct_formatted_summary_prompt(
                conversation=linearized_conv,
                topics=topics_str,
                summary=summary,
                language=language,
            )
            return prompt
        except Exception as e:
            logger.info(f"Cannot construct the prompt. Error message:{e}")
            logger.info("Aborting. Returning empty messages.")
            return [{}]

    def prepare_summary_prompt(
        self,
        conversation_history: List[Dict],
        summary: str = "",
        window_size: int = 256,
        topics: List[str] = None,
        language: str = "zh_CN",
        model_info: Optional[Dict] = None,
    ) -> Tuple[LLMClient, List[Dict]]:
        """Prepare summary prompt"""
        # 1. Create a client
        client = self._create_client_from_model_info(model_info) or self.default_client
        if client is None:
            raise ValueError("Cannot create LLM Client. Aborting.")

        # 2. Extracting on a specific part of the conversation
        sub_conv = conversation_history[-window_size:]

        # 3. Build Prompt
        messages = self._prepare_summary_update_query(
            conversation_history=sub_conv,
            topics=topics,
            language=language,
            summary=summary,
        )

        return client, messages

    def update_summary(
        self,
        conversation_history: List[Dict],
        summary: str = "",
        window_size: int = 256,
        topics: List[str] = None,
        language: str = "zh_CN",
        model_info: Optional[Dict] = None,
    ) -> Tuple[SummarizationError, Dict]:
        """Update summary synchronously"""
        # 1-3. Create Client and Build Prompt
        client, messages = self.prepare_summary_prompt(
            conversation_history, summary, window_size, topics, language, model_info
        )

        # 4. Summarize (prompt the LLM)
        try:
            updated_summary = client.query(messages=messages).content
        except JiuWenBaseException as e:
            raise e
        except ValueError as ve:
            logger.info(f"Cannot update summary - Check your messages type:{ve}")
            updated_summary = summary
            out = {"updated_summary": updated_summary}
            return self.status_failed, out
        except Exception:
            logger.info("Unexpected error when updating summary")
            logger.info("Summary unchanged. Aborting.")
            updated_summary = summary
            out = {"updated_summary": updated_summary}
            return self.status_failed, out

        # 5. Returning updated summary
        out = {"updated_summary": updated_summary}
        return self.status_success, out

    async def async_update_summary(
        self,
        conversation_history: List[Dict],
        summary: str = "",
        window_size: int = 256,
        topics: List[str] = None,
        language: str = "zh_CN",
        model_info: Optional[Dict] = None,
    ) -> Tuple[SummarizationError, Dict]:
        """Update summary asynchronously"""
        # 1-3. Create Client and Build Prompt
        client, messages = self.prepare_summary_prompt(
            conversation_history, summary, window_size, topics, language, model_info
        )

        # 4. Summarize (prompt the LLM)
        try:
            response = await client.async_query(messages=messages)
            updated_summary = response.content
        except JiuWenBaseException as e:
            raise e
        except ValueError as ve:
            logger.info(f"Cannot update summary - Check your messages type:{ve}")
            updated_summary = summary
            out = {"updated_summary": updated_summary}
            return self.status_failed, out
        except Exception:
            logger.info("Summary unchanged. Aborting.")
            updated_summary = summary
            out = {"updated_summary": updated_summary}
            return self.status_failed, out

        # 5. Returning updated summary
        out = {"updated_summary": updated_summary}
        return self.status_success, out
