#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
import json
from typing import List

from jiuwen.common.llm_service.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.common.base import VariableResult, ProfileTopicResult
from jiuwen.context.memory_engine.common.llm_json_output_parser import (
    LLMJsonOutputParser,
)
from jiuwen.context.memory_engine.config.config import (
    MemoryEngineConfig,
    MemoryScopeConfig,
    ProfileTopicConfig,
)
from jiuwen.context.memory_engine.prompt.memory_analyzer import (
    MEMORY_ANALYZER_PROMPT,
    MEMORY_ANALYZER_VARIABLE_PROMPT,
    MEMORY_ANALYZER_USER_PROFILE_PROMPT,
    OUTPUT_PROMPT,
)
from pydantic import BaseModel, Field


class MemoryAnalyzerResult(BaseModel):
    category: List[str] = Field(default_factory=list)
    variables: List[VariableResult] = Field(default_factory=list)
    user_profile_topics: List[ProfileTopicResult] = Field(default_factory=list)


class MemoryAnalyzer:
    def __init__(self):
        pass

    @staticmethod
    async def analyze(
        messages: list[BaseMessage],
        history_messages: list[BaseMessage],
        base_chat_model: BaseChatModel,
        engine_config: MemoryEngineConfig,
        scope_config: MemoryScopeConfig,
        profile_topics: list[ProfileTopicConfig],
        retries: int = 3,
    ) -> MemoryAnalyzerResult | None:
        """
        based on the input messages, configurations, and LLM client,
        perform memory classification, user variable extraction, and user profile generation.
        """
        user_profile_topics_info, model_input = MemoryAnalyzer._build_model_input(
            messages=messages,
            history_messages=history_messages,
            engine_config=engine_config,
            scope_config=scope_config,
            profile_topics=profile_topics,
        )
        logger.debug(f"Start to analyze memory, input: {model_input}")
        parser = LLMJsonOutputParser()
        for attempt in range(retries):
            try:
                response = await base_chat_model.ainvoke(model_input)
                ret = await parser.parse(response.content)
                ret = MemoryAnalyzer._filter_extra_extractions(
                    user_profile_topics_info, ret
                )
                logger.debug(f"Succeed to get categories, result: {ret}")
                return MemoryAnalyzerResult.model_validate(ret)
            except json.JSONDecodeError as e:
                if attempt < retries - 1:
                    continue
                logger.error(f"categories model output format error: {e.msg}")
            except BaseException as e:
                if attempt < retries - 1:
                    continue
                logger.error(f"categories model output format error: {str(e)}")
        return None

    @staticmethod
    def _merge_user_profile_topic(
        engine_config: MemoryEngineConfig, profile_topics: list[ProfileTopicConfig]
    ) -> tuple[list, list]:
        tag_nums = 0
        profile_topics_dict: dict[str, dict[str, str]] = {}
        for engine_profile_config in engine_config.default_profile_topics:
            if tag_nums >= engine_config.max_profile_tag_number:
                break
            if engine_profile_config.topic not in profile_topics_dict:
                profile_topics_dict[engine_profile_config.topic] = {}
            for tag in engine_profile_config.tags:
                if tag_nums >= engine_config.max_profile_tag_number:
                    break
                if tag.name not in profile_topics_dict[engine_profile_config.topic]:
                    tag_nums += 1
                profile_topics_dict[engine_profile_config.topic][tag.name] = (
                    tag.description
                )

        for scope_profile_config in profile_topics:
            if scope_profile_config.topic not in profile_topics_dict:
                if tag_nums >= engine_config.max_profile_tag_number:
                    continue
                profile_topics_dict[scope_profile_config.topic] = {}
            for tag in scope_profile_config.tags:
                if tag.name not in profile_topics_dict[scope_profile_config.topic]:
                    if tag_nums >= engine_config.max_profile_tag_number:
                        continue
                    tag_nums += 1
                profile_topics_dict[scope_profile_config.topic][tag.name] = (
                    tag.description
                )

        user_profile_topics_info = []
        user_profile_topics_template = []
        for topic, tags in profile_topics_dict.items():
            if len(tags) == 0:
                continue
            user_profile_topics_info.append(
                {
                    "topic": topic,
                    "tags": [
                        {
                            "name": name,
                            "description": description,
                        }
                        for name, description in tags.items()
                    ],
                }
            )
            user_profile_topics_template.append(
                {
                    "topic": topic,
                    "tags": [
                        {
                            "name": name,
                            "value": "",
                        }
                        for name, _ in tags.items()
                    ],
                }
            )
        return user_profile_topics_info, user_profile_topics_template

    @staticmethod
    def _assemble_prompt(
        messages: list[BaseMessage],
        history_messages: list[BaseMessage],
        variables_description_json: str,
        variables_output_format_json: str,
        user_profile_topics_info_json: str,
        user_profile_topics_template_json: str,
        user_profile_forbidden_topic_json: str,
        variables_output_format: list,
        user_profile_topics_template: list,
    ) -> list:
        sys_prompt = MEMORY_ANALYZER_PROMPT
        output_format = OUTPUT_PROMPT
        step = 2

        if len(variables_output_format) > 0:
            sys_prompt += MEMORY_ANALYZER_VARIABLE_PROMPT
            sys_prompt = sys_prompt.replace(
                "variables_description_template", variables_description_json
            ).replace("STEP", str(step))
            step += 1
            output_format = output_format.replace(
                "variables_template", ',\n"variables":' + variables_output_format_json
            )
        else:
            output_format = output_format.replace("variables_template", "")

        if len(user_profile_topics_template) > 0:
            sys_prompt += MEMORY_ANALYZER_USER_PROFILE_PROMPT
            sys_prompt = (
                sys_prompt.replace(
                    "user_profile_topic_description_template",
                    user_profile_topics_info_json,
                ).replace(
                    "user_profile_forbidden_topic_template",
                    user_profile_forbidden_topic_json,
                )
            ).replace("STEP", str(step))
            output_format = output_format.replace(
                "user_profile_topic_template",
                ',\n"user_profile_topics":' + user_profile_topics_template_json,
            )
        else:
            output_format = output_format.replace("user_profile_topic_template", "")

        sys_prompt += output_format

        user_input = {}
        if history_messages:
            user_input["historical_messages"] = "".join(
                f"{msg.type}: {msg.content}\n" for msg in history_messages
            )
        user_input["current_messages"] = "".join(
            f"{msg.type}: {msg.content}\n" for msg in messages
        )

        return [
            {"role": "system", "content": sys_prompt},
            {"role": "user", "content": json.dumps(user_input, ensure_ascii=False)},
        ]

    @staticmethod
    def _build_model_input(
        messages: list[BaseMessage],
        history_messages: list[BaseMessage],
        engine_config: MemoryEngineConfig,
        scope_config: MemoryScopeConfig,
        profile_topics: list[ProfileTopicConfig],
    ) -> tuple[list, list]:
        variables_description = []
        variables_output_format = []
        for mem_variable in scope_config.mem_variables:
            variables_description.append(
                {
                    "variable_key": mem_variable.name,
                    "variable_value": mem_variable.description,
                }
            )

            variables_output_format.append(
                {
                    "variable_key": mem_variable.name,
                    "variable_value": "",
                }
            )
        if scope_config.enable_user_profile:
            user_profile_topics_info, user_profile_topics_template = (
                MemoryAnalyzer._merge_user_profile_topic(
                    engine_config=engine_config, profile_topics=profile_topics
                )
            )
        else:
            logger.debug(
                "Not enable user profile in scope config, skip extract topic profile"
            )
            user_profile_topics_info = ""
            user_profile_topics_template = ""

        variables_description_json = json.dumps(
            variables_description, ensure_ascii=False
        )
        variables_output_format_json = json.dumps(
            variables_output_format, ensure_ascii=False
        )
        user_profile_topics_info_json = json.dumps(
            user_profile_topics_info, ensure_ascii=False
        )
        user_profile_topics_template_json = json.dumps(
            user_profile_topics_template, ensure_ascii=False
        )
        user_profile_forbidden_topic_json = json.dumps(
            [
                {
                    "name": single_tag.model_dump()["name"],
                    "description": single_tag.model_dump()["description"],
                }
                for single_tag in engine_config.user_profile_forbidden_topic
            ],
            ensure_ascii=False,
        )

        return user_profile_topics_info, MemoryAnalyzer._assemble_prompt(
            messages=messages,
            history_messages=history_messages,
            variables_description_json=variables_description_json,
            variables_output_format_json=variables_output_format_json,
            user_profile_topics_info_json=user_profile_topics_info_json,
            user_profile_topics_template_json=user_profile_topics_template_json,
            user_profile_forbidden_topic_json=user_profile_forbidden_topic_json,
            variables_output_format=variables_output_format,
            user_profile_topics_template=user_profile_topics_template,
        )

    @staticmethod
    def _filter_extra_extractions(
        profile_topics_info: list[dict], extracted_data: dict
    ) -> dict:
        """
        Filter out the content extracted by the large model and only retain the tags defined in the configuration

        Args:
            profile_topics_info: The original configuration defines the tags that are allowed to be extracted
            extracted_data: The results of extracting large models

        Returns:
           The filtered result only includes tags defined in the configuration
        """
        filtered_topics = []

        config_tags_map = {}
        for topic_info in profile_topics_info:
            topic = topic_info["topic"]
            config_tags_map[topic] = {tag["name"] for tag in topic_info["tags"]}

        for extracted_topic in extracted_data.get("user_profile_topics", []):
            topic = extracted_topic["topic"]
            if topic not in config_tags_map:
                continue

            filtered_tags = []
            for tag in extracted_topic["tags"]:
                if tag["name"] in config_tags_map.get(topic, set()):
                    filtered_tags.append(tag)

            filtered_topics.append({"topic": topic, "tags": filtered_tags})

        extracted_data["user_profile_topics"] = filtered_topics
        return extracted_data
