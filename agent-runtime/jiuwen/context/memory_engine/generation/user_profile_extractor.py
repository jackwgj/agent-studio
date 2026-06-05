#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
import json
from typing import List, Dict

from jiuwen.common.llm_service.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.common.llm_json_output_parser import (
    LLMJsonOutputParser,
)
from jiuwen.context.memory_engine.prompt.user_profile_extractor import (
    USER_PROFILE_EXTRACTOR_PROMPT,
)


def _get_message(user_define: Dict[str, str] = None) -> str:
    if user_define and len(user_define) > 0:
        user_define_description = ""
        user_define_format = ""
        for key in user_define.keys():
            value = user_define[key]
            user_define_description += f"    *   **{key}:** {value}等相关信息\n"
            user_define_format += f',\n    "{key}": []'
        sym_prompt = USER_PROFILE_EXTRACTOR_PROMPT.format(
            user_define_description=user_define_description,
            user_define_format=user_define_format,
        )
    else:
        sym_prompt = USER_PROFILE_EXTRACTOR_PROMPT.format(
            user_define_description="", user_define_format=""
        )
    return sym_prompt


class UserProfileExtractor:
    def __init__(self) -> None:
        pass

    @staticmethod
    def get_model_input(
        messages: List[BaseMessage], history_messages: List[BaseMessage], prompt: str
    ) -> List[dict]:
        """generate model input based on the provided messages and prompt"""
        history = ""
        if history_messages and len(history_messages) > 0:
            for msg in history_messages:
                history += f"{msg.type}: {msg.content}\n"
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
    async def get_user_profile(
        messages: List[BaseMessage],
        history_messages: List[BaseMessage],
        base_chat_model: BaseChatModel,
        user_define: Dict[str, str] = None,
        retries: int = 3,
    ) -> Dict[str, str]:
        """extract user profile based on the provided messages and user define keyword"""
        sym_prompt = _get_message(user_define)
        model_input = UserProfileExtractor.get_model_input(
            messages, history_messages, sym_prompt
        )
        logger.debug(f"Start to get user profile, input: {model_input}")
        parser = LLMJsonOutputParser()
        for attempt in range(retries):
            try:
                response = await base_chat_model.ainvoke(model_input)
                result = await parser.parse(response.content)
                logger.debug(f"Succeed to get user profile, result: {result}")
                if isinstance(result, dict):
                    return result
            except json.JSONDecodeError as e:
                if attempt < retries - 1:
                    continue
                logger.error(
                    f"user profile extractor model output format error: {e.msg}"
                )
        return {}
