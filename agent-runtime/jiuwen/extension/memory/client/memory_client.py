# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
from abc import ABC, abstractmethod
from datetime import datetime

from openjiuwen.core.foundation.llm import BaseMessage
from openjiuwen.core.foundation.store.base_db_store import BaseDbStore
from openjiuwen.core.foundation.store.base_kv_store import BaseKVStore
from openjiuwen.core.foundation.store.base_vector_store import BaseVectorStore
from openjiuwen.core.memory.config.config import (
    MemoryEngineConfig,
    MemoryScopeConfig,
    AgentMemoryConfig,
)
from openjiuwen.core.memory.manage.mem_model.memory_unit import MemoryType
from openjiuwen.core.retrieval.embedding.base import Embedding
from pydantic import BaseModel, Field


class MemInfo(BaseModel):
    mem_id: str = Field(default="", description="memory id")
    content: str = Field(default="", description="memory content")
    type: MemoryType = Field(default=MemoryType.USER_PROFILE, description="memory type")


class MemResult(BaseModel):
    mem_info: MemInfo = Field(default=None, description="memory information")
    score: float = Field(default=0.0, description="memory score of relevance")


class MemoryClient(ABC):
    DEFAULT_VALUE: str = "__default__"

    @abstractmethod
    async def register_store(
        self,
        kv_store: BaseKVStore,
        vector_store: BaseVectorStore | None = None,
        db_store: BaseDbStore | None = None,
        embedding_model: Embedding | None = None,
    ):
        pass

    @abstractmethod
    def set_config(self, config: MemoryEngineConfig):
        pass

    @abstractmethod
    async def set_scope_config(
        self, scope_id: str, memory_scope_config: MemoryScopeConfig
    ) -> bool:
        pass

    @abstractmethod
    async def delete_mem_by_scope(self, scope_id: str) -> bool:
        pass

    @abstractmethod
    async def add_messages(
        self,
        messages: list[BaseMessage],
        agent_config: AgentMemoryConfig,
        *,
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
        session_id: str = DEFAULT_VALUE,
        timestamp: datetime | None = None,
        gen_mem: bool = True,
        gen_mem_with_history_msg_num: int = 2,
    ):
        pass

    @abstractmethod
    async def delete_messages_by_user_and_scope(
        self,
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
    ):
        pass

    @abstractmethod
    async def delete_mem_by_id(
        self, mem_id: str, user_id: str = DEFAULT_VALUE, scope_id: str = DEFAULT_VALUE
    ) -> bool:
        pass

    @abstractmethod
    async def delete_mem_by_user_id(
        self, user_id: str = DEFAULT_VALUE, scope_id: str = DEFAULT_VALUE
    ) -> bool:
        pass

    @abstractmethod
    async def update_mem_by_id(
        self,
        mem_id: str,
        memory: str,
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
    ):
        pass

    @abstractmethod
    async def get_variables(
        self,
        names: list[str] | str | None = None,
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
    ) -> dict[str, str]:
        pass

    @abstractmethod
    async def search_user_mem(
        self,
        query: str,
        num: int,
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
        threshold: float = 0.3,
    ) -> list[MemResult]:
        pass

    @abstractmethod
    async def search_user_history_summary(
        self,
        query: str,
        num: int,
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
        threshold: float = 0.3,
    ) -> list[MemResult]:
        pass

    @abstractmethod
    async def user_mem_total_num(
        self, user_id: str = DEFAULT_VALUE, scope_id: str = DEFAULT_VALUE
    ) -> int:
        pass

    @abstractmethod
    async def get_user_mem_by_page(
        self,
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
        page_size: int = 10,
        page_idx: int = 0,
        memory_type: MemoryType = MemoryType.UNKNOWN,
    ) -> list[MemInfo]:
        pass

    @abstractmethod
    async def update_variables(
        self,
        variables: dict[str, str],
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
    ):
        pass

    @abstractmethod
    async def delete_variables(
        self,
        names: list[str],
        user_id: str = DEFAULT_VALUE,
        scope_id: str = DEFAULT_VALUE,
    ) -> bool:
        pass
