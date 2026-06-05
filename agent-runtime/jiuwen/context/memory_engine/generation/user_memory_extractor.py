#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
import json
from typing import List

from jiuwen.common.llm_service.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.common.llm_json_output_parser import (
    LLMJsonOutputParser,
)
from jiuwen.context.memory_engine.config.config import (
    MemoryScopeConfig,
    ProcessMemoryMode,
)
from jiuwen.context.memory_engine.prompt.proactive_memory_extractor import (
    MEMORY_INSTRUCT_EXTRACT_PROMPT,
    USER_MEMORY_EXTRACT_PROMPT,
)


class UserMemoryExtractor:
    def __init__(self) -> None:
        pass

    @staticmethod
    def get_model_input(
        messages: List[BaseMessage], history_messages: List[BaseMessage], prompt: str
    ) -> List[dict]:
        """generate model input based on the provided messages and prompt"""
        history = ""
        if history_messages and len(history_messages) > 0:
            history_list = []
            for msg in history_messages:
                history_list.append(f"{msg.type}: {msg.content}\n")
            history = "".join(history_list)
        conversation = ""
        for msg in messages:
            conversation += f"{msg.type}: {msg.content}\n"
        model_input = [{"role": "system", "content": prompt}]
        user_input = {}
        if history != "":
            user_input["historical_messages"] = history
        user_input["current_messages"] = conversation
        model_input.append(
            {"role": "user", "content": json.dumps(user_input, ensure_ascii=False)}
        )
        return model_input

    @staticmethod
    async def get_user_memory(
        messages: List[BaseMessage],
        history_messages: List[BaseMessage],
        base_chat_model: BaseChatModel,
        categories: list[str],
        scope_config: MemoryScopeConfig,
        mode: ProcessMemoryMode,
        retries: int = 3,
    ) -> List:
        """extract user memory based on the provided messages and user define keyword"""
        if mode == ProcessMemoryMode.REALTIME_MODE:
            sym_prompt = MEMORY_INSTRUCT_EXTRACT_PROMPT
        else:
            if scope_config.enable_user_profile:
                sym_prompt = USER_MEMORY_EXTRACT_PROMPT
            else:
                logger.info("not enable any type of long term memory, return []")
                return []
        model_input = UserMemoryExtractor.get_model_input(
            messages, history_messages, sym_prompt
        )
        logger.debug(f"Start to get user memory, input: {model_input}")
        parser = LLMJsonOutputParser()
        for attempt in range(retries):
            try:
                response = await base_chat_model.ainvoke(model_input)
                logger.info(f"Finish to get user memory, response: {response}")
                result = await parser.parse(response.content)
                logger.debug(f"Succeed to get user memory, result: {result}")
                if isinstance(result, list):
                    return result
            except json.JSONDecodeError as e:
                if attempt < retries - 1:
                    continue
                logger.error(
                    f"user memory extractor model output format error: {e.msg}"
                )
        return []
