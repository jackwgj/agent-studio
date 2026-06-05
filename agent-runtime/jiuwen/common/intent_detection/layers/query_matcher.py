# -*- coding: utf-8 -*-
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
query匹配层
"""

from typing import Optional, List

from jiuwen.common.intent_detection.config import QueryMatcherConfig
from jiuwen.common.intent_detection.layers.base import IntentInput, IntentOutput
from jiuwen.common.intent_detection.layers.es_matcher_base import (
    ESMatcherBase,
    ESRetrivalParams,
)
from jiuwen.common.log.base import logger

DEFAULT_QUERY_MATCHER_INDEX = "query"
DEFAULT_QUERY_MATCHER_TEXT_FIELD = "query"
DEFAULT_QUERY_MATCHER_VECTOR_FIELD = "embedding"
DEFAULT_QUERY_MATCHER_SEARCH_MODE = "semantic"
DEFAULT_QUERY_MATCHER_TOP_K = 20
DEFAULT_QUERY_MATCHER_HYBRID_WEIGHT = 0.9
DEFAULT_QUERY_MATCHER_SIMILARITY = "cosine"

QUERY_MATCHER_SPARSE_SEARCH_SOURCE_FIELDS = ["query", "intent"]
QUERY_MATCHER_DENSE_SEARCH_SOURCE_FIELDS = ["query", "intent"]


class QueryMatcher(ESMatcherBase):
    """
    QQ匹配
    """

    name = "query"

    def __init__(self, config: QueryMatcherConfig, **kwargs):
        """
        初始化
        """
        self.config = config
        self.es_retrival_config = self._get_es_retrival_config(config)

        super().__init__(es_retrival_config=self.es_retrival_config)

    async def invoke(
        self, input_data: IntentInput, top_k: Optional[int] = None, **kwargs
    ) -> IntentOutput:
        """
        异步匹配接口

        Args:
            input_data: 输入数据
            top_k: 返回给llm处理的候选意图数量
            **kwargs:

        Returns:
            IntentOutput: 匹配结果
        """
        try:
            retr_top_k = max(top_k or 0, self.es_retrival_config.top_k)
            self.es_retrival_config.top_k = retr_top_k

            if self.es_dao is None:
                await self.initialize()

            output = await self._es_search(input_data, kwargs.get("domains", None))
            logger.info(
                f"succeed to get er search output:{output}",
                simple_log="succeed to get er search output",
            )

            valid_detected_intents: List[str] = []  # 过滤不在valid intent中的意图
            best_intent = None
            best_score = None

            for intent, score in output.items():
                if intent not in input_data.valid_intents.keys():
                    continue
                valid_detected_intents.append(intent)
                if best_intent is None and score >= self.config.threshold:  # output有序
                    best_intent = intent
                    best_score = score

            if best_intent is None:
                return IntentOutput()
            intent_id = list(input_data.valid_intents.keys()).index(best_intent) + 1
            logger.info(
                f"succeed to get intent_id in query_matcher, intent_id:{intent_id},input_data:{input_data},"
                f" top_k:{top_k}, kwargs:{kwargs}",
                simple_log="succeed to get intent_id in query_matcher",
            )
            intent_output = IntentOutput(
                is_matched=bool(best_intent),
                intent_name=best_intent,
                intent_id=str(intent_id),
                score=best_score if best_score else 0,
                matched_layer=self.name if best_intent else None,
                candidates=valid_detected_intents[:top_k] if top_k else [],
            )
            logger.info(
                f"succeed to get intent_output in query_matcher:{intent_output}",
                simple_log="succeed to get intent_output in query_matcher",
            )
            return intent_output

        except Exception as e:
            logger.error(f"Error in QueryMatcher invoke: {e}", exc_info=True)
            return IntentOutput()

    def _get_sparse_search_source_fields(self) -> List[str]:
        """
        获取关键字检索的_source字段列表

        Returns:
            List[str]: _source字段列表
        """
        return QUERY_MATCHER_SPARSE_SEARCH_SOURCE_FIELDS

    def _get_dense_search_source_fields(self) -> List[str]:
        """
        获取向量检索的_source字段列表

        Returns:
            List[str]: _source字段列表
        """
        return QUERY_MATCHER_DENSE_SEARCH_SOURCE_FIELDS

    def _get_es_retrival_config(self, config):
        return ESRetrivalParams(
            index_name=DEFAULT_QUERY_MATCHER_INDEX,
            text_field=DEFAULT_QUERY_MATCHER_TEXT_FIELD,
            vector_field=DEFAULT_QUERY_MATCHER_VECTOR_FIELD,
            search_mode=DEFAULT_QUERY_MATCHER_SEARCH_MODE,
            top_k=DEFAULT_QUERY_MATCHER_TOP_K,
            hybrid_weight=DEFAULT_QUERY_MATCHER_HYBRID_WEIGHT,
            similarity=DEFAULT_QUERY_MATCHER_SIMILARITY,
            use_history=config.use_history,
            history_max_turns=config.history_max_turns,
        )
