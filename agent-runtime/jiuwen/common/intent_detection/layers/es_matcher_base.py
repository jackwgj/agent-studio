# -*- coding: utf-8 -*-
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
ES匹配基类
"""

import copy
import os
from abc import ABC, abstractmethod
from typing import Optional, List, Dict, Any, Literal

from jiuwen.common.configs.env_constants import EMBEDDING_API_KEY
from jiuwen.common.intent_detection.layers.base import BaseMatcher, IntentInput
from jiuwen.common.intent_detection.utils.es_service.elastic_search import (
    get_async_elastic_search_instance,
)
from jiuwen.common.intent_detection.utils.util import call_text_embedding
from jiuwen.common.log.base import logger
from pydantic import BaseModel, Field

DEFAULT_HYBRID_WEIGHT = 0.9
DEFAULT_TOK_K = 20
DEFAULT_SEARCH_MODE = "semantic"
DEFAULT_SIMILARITY = "cosine"


def _get_embedding_key():
    return os.getenv(EMBEDDING_API_KEY, "")


class ESRetrivalParams(BaseModel):
    # 检索参数
    index_name: str = Field(min_length=1)
    text_field: str = Field(min_length=1)
    vector_field: str = Field(min_length=1)
    search_mode: Literal["keyword", "semantic", "hybrid"] = DEFAULT_SEARCH_MODE
    top_k: int = Field(default=DEFAULT_TOK_K)
    hybrid_weight: float = Field(default=DEFAULT_HYBRID_WEIGHT, ge=0.0, le=1.0)
    similarity: Literal["cosine", "dot_product", "l2_norm"] = DEFAULT_SIMILARITY

    use_history: bool = False
    history_max_turns: int = Field(default=3, ge=0)


class ESMatcherBase(BaseMatcher, ABC):
    """
    ES匹配基类
    """

    def __init__(
        self,
        es_retrival_config: ESRetrivalParams,
    ):
        """
        初始化ES matcher

        Args:
            es_config: ES配置
            api_key: API密钥，用于文本嵌入
            merge_method: 对话历史合并方法
        """
        self.es_retrival_config = es_retrival_config
        self.api_key = _get_embedding_key()

        self.es_dao = None

    async def initialize(self):
        """
        初始化ES Client
        """
        if self.es_dao is None:
            await self._init_es_client()

    @abstractmethod
    def _get_sparse_search_source_fields(self) -> List[str]:
        """
        获取关键字检索的_source字段列表

        Returns:
            List[str]: _source字段列表
        """
        pass

    @abstractmethod
    def _get_dense_search_source_fields(self) -> List[str]:
        """
        获取向量检索的_source字段列表

        Returns:
            List[str]: _source字段列表
        """
        pass

    @abstractmethod
    def _get_es_retrival_config(self, config):
        """
        构建检索配置项
        """
        pass

    async def _init_es_client(self):
        """
        初始化ES service
        """
        try:
            self.es_dao = await get_async_elastic_search_instance()
            is_index_exist = await self.es_dao.exist_index(
                self.es_retrival_config.index_name
            )
            if not is_index_exist:
                logger.warning(
                    "Index %s does not exist. Please initialize it before querying.",
                    self.es_retrival_config.index_name,
                )
        except Exception as e:
            logger.error(f"Failed to initialize ES client: {e}", exc_info=True)
            raise e

    def _build_query(self, input_data: IntentInput) -> str:
        """
        构建查询

        Args:
            input_data: 输入

        Returns:
            str: 查询文本
        """
        if self.es_retrival_config.use_history:
            history = input_data.dialogue_history[
                -self.es_retrival_config.history_max_turns :
            ]
            return "\n".join(
                [f"{message.role}:{message.content}" for message in history]
            )

        return input_data.query

    def _build_domain_filter(self, domains: Optional[List[str]]) -> List[dict]:
        """
        构建es请求体过滤字段

        Args:
            domains: 域列表

        Returns:
            List[dict]: es请求体过滤字段
        """
        if not domains:
            return []
        return [{"terms": {"domain": domains}}]

    def _assemble_search_body(
        self,
        query_clause: Dict[str, Any],
        source_fields: List[str],
        top_k: int,
    ) -> Dict[str, Any]:
        """
        组装es请求体

        Args:
            query_clause: query 字段
            source_fields: 返回字段（_source）
            top_k: 结果数

        Returns:
            完整请求体
        """
        return {"query": query_clause, "_source": source_fields, "size": top_k}

    async def _es_search_sparse(
        self,
        input_data: IntentInput,
        top_k: int,
        domains: Optional[List[str]] = None,
    ) -> List[Dict[str, Any]]:
        """
        关键词匹配

        Args:
            input_data: 输入
            top_k: 结果数
            domains: 过滤阈

        Returns:
            List[Dict]: 检索结果
        """
        try:
            query_text = self._build_query(input_data)
            domain_filters = self._build_domain_filter(domains)

            if domain_filters:
                query_clause = {
                    "bool": {
                        "must": [{"match": {"description": query_text}}],
                        "filter": domain_filters,
                    }
                }
            else:
                query_clause = {"match": {"description": query_text}}

            search_body = self._assemble_search_body(
                query_clause=query_clause,
                source_fields=self._get_sparse_search_source_fields(),
                top_k=top_k,
            )

            hits = await self.es_dao.search(
                index=self.es_retrival_config.index_name, body=search_body
            )
            return hits

        except Exception as e:
            logger.error(f"Failed to perform sparse search: {e}", exc_info=True)
            raise

    async def _es_search_dense(
        self,
        input_data: IntentInput,
        top_k: int,
        domains: Optional[List[str]] = None,
    ) -> List[Dict[str, Any]]:
        """
        向量相似度检索

        Args:
            input_data: 输入
            top_k: 结果数
            domains: 过滤阈

        Returns:
            List[Dict]: 检索结果
        """
        if not self.api_key:
            logger.warning("api_key not configured, cannot perform dense search")
            return []

        try:
            query_text = self._build_query(input_data)
            vector = await call_text_embedding(query=query_text, api_key=self.api_key)
            domain_filters = self._build_domain_filter(domains)

            if domain_filters:
                base_query = {"bool": {"filter": domain_filters}}
            else:
                base_query = {"match_all": {}}

            query_clause = {
                "script_score": {
                    "query": base_query,
                    "script": {
                        "source": "cosineSimilarity(params.query_vector, 'embedding')",
                        "params": {"query_vector": vector},
                    },
                }
            }

            search_body = self._assemble_search_body(
                query_clause=query_clause,
                source_fields=self._get_dense_search_source_fields(),
                top_k=top_k,
            )

            hits = await self.es_dao.search(
                index=self.es_retrival_config.index_name, body=search_body
            )
            return hits

        except Exception as e:
            logger.error(f"Failed to perform dense search: {e}", exc_info=True)
            raise

    async def _es_search(
        self,
        input_data: IntentInput,
        domains: Optional[List[str]] = None,
    ) -> Dict[str, float]:
        """
        执行 ES 搜索

        Args:
            input_data: 输入
            top_k: 返回结果数
            domains: 限定检索的域列表；如果为 None 或空，则在所有域中检索

        Returns:
            Dict[str, float]: {intent: final_score} 有序
        """
        intent_score_schema = {
            "sparse": 0.0,
            "dense": 0.0,
        }
        intent_score_weight = {
            "sparse": 1 - self.es_retrival_config.hybrid_weight,
            "dense": self.es_retrival_config.hybrid_weight,
        }

        if self.es_retrival_config.search_mode == "semantic":
            dense_hits = await self._es_search_dense(
                input_data, self.es_retrival_config.top_k, domains=domains
            )
            sparse_hits = []
        elif self.es_retrival_config.search_mode == "keyword":
            sparse_hits = await self._es_search_sparse(
                input_data, self.es_retrival_config.top_k, domains=domains
            )
            dense_hits = []
        else:
            sparse_hits = await self._es_search_sparse(
                input_data, self.es_retrival_config.top_k, domains=domains
            )
            dense_hits = await self._es_search_dense(
                input_data, self.es_retrival_config.top_k, domains=domains
            )

        intent_score_map: Dict[str, Dict[str, float]] = {}

        def update_intent_scores(hits: List[Dict[str, Any]], score_type: str):
            sources = [hit["_source"] for hit in hits]
            scores = [hit["_score"] for hit in hits]
            intent_num = {}
            for idx, source in enumerate(sources):
                intent = source["intent"]
                if intent not in intent_num:
                    intent_num[intent] = 0
                intent_num[intent] += 1
                if intent not in intent_score_map:
                    intent_score_map[intent] = copy.deepcopy(
                        intent_score_schema
                    )  # 初始化 sparse 和 dense 字段
                intent_score_map[intent][score_type] += scores[idx]
            for intent in intent_num:
                intent_score_map[intent][score_type] /= intent_num[intent]

        update_intent_scores(sparse_hits, "sparse")
        update_intent_scores(dense_hits, "dense")

        if not intent_score_map:
            return {}

        import pandas as pd

        df = pd.DataFrame.from_dict(intent_score_map, orient="index").fillna(0)
        if len(df) > 1:
            from sklearn.preprocessing import MinMaxScaler

            df[["sparse"]] = MinMaxScaler().fit_transform(df[["sparse"]])
        df["final_score"] = sum(
            df[col] * w for col, w in intent_score_weight.items() if col in df
        )

        intent_final_scores: Dict[str, float] = df["final_score"].to_dict()
        sorted_intent_scores: Dict[str, float] = dict(
            sorted(intent_final_scores.items(), key=lambda x: x[1], reverse=True)
        )

        return sorted_intent_scores
