# -*- coding: utf-8 -*-
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
import time
from typing import Optional

from jiuwen.common.intent_detection.config import MultiLayerIntentDetectionConfig
from jiuwen.common.intent_detection.layers.base import IntentInput, IntentOutput
from jiuwen.common.intent_detection.layers.llm_matcher import LLMMatcher
from jiuwen.common.intent_detection.layers.query_matcher import QueryMatcher
from jiuwen.common.intent_detection.layers.rule_matcher import RuleMatcher
from jiuwen.common.intent_detection.layers.semantic_matcher import SemanticMatcher
from jiuwen.common.log.base import logger
from jiuwen.orchestration.common.perf_log import wf_performance_buffer

RULE_MATCHER = RuleMatcher.name
QUERY_MATCHER = QueryMatcher.name
SEMANTIC_MATCHER = SemanticMatcher.name
LLM_MATCHER = LLMMatcher.name

MATCHER_METADATA = [
    {
        "name": RuleMatcher.name,
        "matcher_class": RuleMatcher,
        "config": "rule_matcher_config",
    },
    {
        "name": QueryMatcher.name,
        "matcher_class": QueryMatcher,
        "config": "query_matcher_config",
    },
    {
        "name": SemanticMatcher.name,
        "matcher_class": SemanticMatcher,
        "config": "semantic_matcher_config",
    },
    {
        "name": LLMMatcher.name,
        "matcher_class": LLMMatcher,
        "config": "llm_matcher_config",
    },
]


class IntentDetection:
    """
    意图识别模块
    """

    def __init__(self, config: MultiLayerIntentDetectionConfig):
        """
        初始化

        Args:
            config: 全局配置，根据各层的enabled开关决定是否初始化该层
        """
        self.config = config

        # 导入config
        self.matcher_configs = []
        for metadata in MATCHER_METADATA:
            matcher_config_obj = getattr(config, metadata["config"])
            matcher_config = self._create_matcher_config(
                metadata["name"], metadata["matcher_class"], matcher_config_obj
            )
            self.matcher_configs.append(matcher_config)

        # 初始化matcher
        self.matchers = {}
        for matcher_config in self.matcher_configs:
            matcher = self._init_matcher(
                matcher_config["enabled"],
                matcher_config["matcher_class"],
                matcher_config["config"],
            )
            self.matchers[matcher_config["name"]] = matcher

        # 候选层matcher（用于LLMMatcher召回候选意图 ）
        self.candidate_layer_matcher = None

    @staticmethod
    def _create_matcher_config(name: str, matcher_class, matcher_config_obj):
        """
        {matcher：config} pair

        Args:
            name: matcher
            matcher_class: matcher class
            matcher_config_obj: config

        Returns:
            dict:
        """
        return {
            "name": name,
            "enabled": matcher_config_obj.enabled,
            "matcher_class": matcher_class,
            "config": matcher_config_obj,
        }

    @staticmethod
    def _matcher_name_to_index(matcher_name: str) -> Optional[int]:
        """
        根据matcher name获取索引

        Args:
            matcher_name: matcher 名称（如 "rule", "query", "semantic", "llm"）

        Returns:
            int: matcher在配置列表中的索引，如果不存在则返回None
        """
        for index, metadata in enumerate(MATCHER_METADATA):
            if metadata["name"] == matcher_name:
                return index
        return None

    @staticmethod
    def _init_matcher(enabled: bool, matcher_class, config):
        """
        初始化matcher

        Args:
            enabled: 是否启用该层
            matcher_class: matcher class
            config: matcher config

        Returns:
            matcher实例
        """
        if not enabled:
            return None

        return matcher_class(config)

    async def invoke(self, input_data: IntentInput, **kwargs) -> IntentOutput:
        """
        多级意图匹配

        Args:

        Returns:
            IntentOutput: 最终匹配结果
        """
        # 保存候选意图用于LLMMatcher
        candidate_intents = []

        # rule -> query -> semantic
        results = {}
        for matcher_config in self.matcher_configs:
            if matcher_config["name"] == LLMMatcher.name:
                continue  # LLM层单独处理

            matcher = self.matchers.get(matcher_config["name"])
            if not matcher:
                continue
            matcher_name = matcher_config["name"]
            result = await self._process_layer(matcher, input_data, matcher_name)

            results[matcher_name] = result

            # 匹配成功
            if result.is_matched:
                return result

            # 如果当前层是候选层，保存候选意图
            if (
                self.config.llm_matcher_config.enabled
                and self.config.candidate_layer == matcher_name
            ):
                candidate_intents = result.candidates

        # LLM匹配层
        llm_matcher = self.matchers.get(LLMMatcher.name)
        if llm_matcher:
            # 如果指定的候选matcher没有启用，临时启用获取候选意图
            llm_matching_start = time.perf_counter()
            if not self.matchers.get(self.config.candidate_layer):
                candidate_intents = await self._get_candidate_intents(
                    input_data, self.config.candidate_layer, self.config.candidate_top_k
                )

            llm_result = await llm_matcher.invoke(
                input_data, self.config.candidate_top_k, candidates=candidate_intents
            )

            llm_matching_end = time.perf_counter()
            matching_time = round((llm_matching_end - llm_matching_start) * 1000)
            wf_performance_buffer.info(
                "time for intent detection with llm matcher",
                matching_time,
                is_record=True,
            )

            return llm_result

        return IntentOutput()

    def _create_candidate_layer_matcher(self, matcher_name: str):
        """创建候选层matcher"""
        if not matcher_name:
            return

        matcher_index = self._matcher_name_to_index(matcher_name)
        if matcher_index is not None and 0 <= matcher_index < len(self.matcher_configs):
            matcher_config = self.matcher_configs[matcher_index]
            matcher_class = matcher_config["matcher_class"]
            # 检查是否需要创建新的matcher实例
            if self.candidate_layer_matcher is None or not isinstance(
                self.candidate_layer_matcher, matcher_class
            ):
                self.candidate_layer_matcher = matcher_class(matcher_config["config"])

    async def _get_candidate_intents(
        self, input_data: IntentInput, matcher_name: str, top_k: int
    ) -> list:
        """
        召回候选意图

        Args:
            input_data: 输入数据
            matcher_name: matcher名称 (rule/query/semantic/llm)
            top_k: 候选意图数量

        Returns:
            list: 候选意图
        """
        try:
            # 优先使用已启用的matcher
            matcher = self.matchers.get(matcher_name)

            # 如果matcher未启用，动态创建一个临时matcher
            if not matcher:
                self._create_candidate_layer_matcher(matcher_name)
                matcher = self.candidate_layer_matcher

            if matcher:
                result = await matcher.invoke(input_data, top_k=top_k)
                return result.candidates

            return []
        except Exception as e:
            logger.error(
                f"Error getting candidate intents from {matcher_name}: {e}",
                exc_info=True,
            )
            return []

    async def _process_layer(
        self, matcher, input_data: IntentInput, matcher_name: str
    ) -> IntentOutput:
        """
        处理单层匹配

        Args:
            matcher: matcher实例
            input_data: 输入
            matcher_name: matcher

        Returns:
            tuple: (匹配结果, 候选意图列表)
        """
        if not matcher:
            logger.info(f"Matcher {matcher_name} not found")
            return IntentOutput()

        # 如果当前层是候选层且LLM matcher启用，传入candidate_top_k
        top_k = None
        if (
            self.config.llm_matcher_config.enabled
            and self.config.candidate_layer == matcher_name
        ):
            top_k = self.config.candidate_top_k

        try:
            start = time.perf_counter()
            result = await matcher.invoke(
                input_data, top_k=top_k, domains=self.config.intent_pkgs
            )
            end = time.perf_counter()
            matching_time = round((end - start) * 1000)
            wf_performance_buffer.info(
                f"time for intent detection with {matcher_name}",
                matching_time,
                is_record=True,
            )
            return result
        except Exception as e:
            logger.error(f"Error in {matcher_name} matcher: {e}", exc_info=True)
            return IntentOutput()
