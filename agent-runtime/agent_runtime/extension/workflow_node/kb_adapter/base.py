#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional


@dataclass
class KBSearchResult:
    """单条知识库检索结果。"""

    text: str = ""                              # 检索到的文本内容
    score: float = 0.0                          # 相关性分数
    source: str = ""                            # 来源知识库 ID（兼容旧字段，等同于 knowledge_base_id）
    metadata: Dict[str, Any] = field(default_factory=dict)  # 额外元数据
    knowledge_base_id: str = ""
    knowledge_base_type: str = ""               # INTERNAL / EXTERNAL
    file_id: str = ""
    document_name: str = ""
    subtitle: str = ""
    serial_number: int = 0                      # 每次请求从 1 自增
    retrieval_id: str = ""                      # 每次请求生成唯一 UUID
    type: str = "doc"                           # "doc" or "faq"


@dataclass
class DatasetSearchRequest:

    endpoint: str
    query: str
    dataset_ids: List[str]
    headers: Dict[str, Any]
    retrieval_params: Dict[str, Any] = field(default_factory=dict)
    extra_params: Dict[str, Any] = field(default_factory=dict)


@dataclass
class SingleKBSearchRequest:

    endpoint: str
    project_id: str
    app_id: str
    repo_id: str
    query: str
    top_k: int
    headers: Dict[str, Any]
    search_mode: str = "doc"
    tags: Optional[List[str]] = None


class KBServiceAdapter(ABC):
    @abstractmethod
    async def search(
        self,
        query: str,
        *,
        connection_config: dict,
        knowledge_bases: list,
        retrieval_params: dict,
    ) -> List[KBSearchResult]:

        pass
