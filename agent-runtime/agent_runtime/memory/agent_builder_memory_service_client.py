from __future__ import annotations

import json
from datetime import datetime
from typing import Optional, Any

from openjiuwen.core.common.logging import memory_logger as logger
from openjiuwen.core.foundation.store.base_embedding import EmbeddingConfig
from openjiuwen.core.foundation.llm.schema.config import ModelClientConfig, ModelRequestConfig
from openjiuwen.core.memory.config.config import (
    AgentMemoryConfig,
    MemoryScopeConfig,
    ProcessMemoryMode,
)
from openjiuwen.core.memory.long_term_memory import LongTermMemory

from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.context.memory_engine.client.local_memory_client import LocalMemoryClient
from jiuwen.context.memory_engine.client.memory_client import MemoryClient
from jiuwen.context.memory_engine.common.base import ProcessedMemResult
from jiuwen.context.memory_engine.config.config import (
    MemoryScopeConfig as LegacyMemoryScopeConfig,
    ProcessMemoryMode as LegacyProcessMemoryMode,
)
from jiuwen.context.memory_engine.engine.memory_engine import MemoryEngine
from jiuwen.context.memory_engine.store import (
    BaseKVStore,
    BaseSemanticStore,
    BaseDbStore,
)
from jiuwen.serve.common.context import request as current_request
from memory.kv_store_redis_impl import KVStoreRedisImpl
from pydantic import BaseModel, Field

from agent_runtime.memory.adapter.ltm_manager import get_ltm, build_scope_config


class VariableValue(BaseModel):
    """Variable value data structure."""

    variable_key: str = Field(..., description="Variable key")
    variable_value: str = Field(..., description="Variable value")
    agent_id: str = Field(..., description="Agent ID")
    user_id: str = Field(..., description="User ID")


class VariableList(BaseModel):
    """Variable list."""

    variables: Optional[list[VariableValue]] = Field([], description="Variable list")


class VariableKeyList(BaseModel):
    """Variable key list."""

    variable_keys: Optional[list[str]] = Field([], description="Variable key list")


class AgentBuilderMemoryClient(MemoryClient):
    """Memory client using LongTermMemory directly instead of HTTP calls to memory-service.

    Variable operations still use LocalMemoryClient with Redis KV store.
    """

    def __init__(self):
        self.local_mem_engine = LocalMemoryClient()
        self.kv_store = KVStoreRedisImpl()
        engine_instance = MemoryEngine()
        engine_instance.register_store(self.kv_store)

    async def _ensure_scope_config(self, scope_id: str, ir_data: dict | None = None) -> None:
        """Ensure MemoryScopeConfig exists for the given scope in LTM.

        Creates config from env settings on first use per scope.
        If ir_data contains strategies, enriches scope config with custom prompts.
        Non-blocking: logs warning on failure.
        """
        ltm = get_ltm()
        if ltm is None or not scope_id:
            return
        try:
            existing = await ltm.get_scope_config(scope_id)
            if existing is not None:
                return
            scope_cfg = build_scope_config()

            if ir_data:
                configs = ir_data.get("configs") or {}
                memory_config = configs.get("memory") or {}
                strategies = memory_config.get("strategies") or []
                for strategy in strategies:
                    strategy_type = strategy.get("type", "")
                    prompt = strategy.get("prompt", "")
                    if not prompt:
                        continue
                    if strategy_type == "user_profile":
                        scope_cfg.user_profile_definition = prompt
                    elif strategy_type == "semantic_memory":
                        scope_cfg.semantic_memory_definition = prompt
                    elif strategy_type == "episodic_memory":
                        scope_cfg.episodic_memory_definition = prompt

            await ltm.set_scope_config(scope_id, scope_cfg)
            logger.info("Created scope config for scope_id=%s (with_ir_strategies=%s)", scope_id, bool(ir_data))
        except Exception as e:
            logger.warning(
                "Failed to ensure scope config for %s (non-blocking): %s",
                scope_id,
                e,
            )

    # ── Variable operations (unchanged, use LocalMemoryClient) ──

    async def list_user_variables(self, user_id: str, scope_id: str) -> dict[str, str]:
        return await self.local_mem_engine.list_user_variables(user_id, scope_id)

    async def set_scope_config(self, scope_id: str, config: MemoryScopeConfig):
        await self.local_mem_engine.set_scope_config(scope_id, config)

    async def delete_scope_config(self, scope_id: str) -> bool:
        return await self.local_mem_engine.delete_scope_config(scope_id)

    async def process_messages(
        self,
        messages: list[BaseMessage],
        user_id: str,
        scope_id: str,
        session_id: str,
        mode: ProcessMemoryMode = ProcessMemoryMode.BATCH_MODE,
        timestamp: datetime | None = None,
    ) -> ProcessedMemResult:
        return await self.local_mem_engine.process_messages(
            messages, user_id, scope_id, session_id, mode, timestamp
        )

    async def init_base_llm(self, llm: BaseChatModel) -> bool:
        return self.local_mem_engine.init_base_llm(llm)

    # ── Long-term memory operations (LTM direct calls) ──

    async def search_user_mem(
        self,
        user_id: str,
        scope_id: str,
        query: str,
        num: int,
        mem_type: Optional[str] = None,
        messages: Optional[list[BaseMessage]] = None,
    ) -> list[Any] | None:
        """Search user memories via LTM directly."""
        try:
            ltm = get_ltm()
            if ltm is None:
                return []

            if not query:
                return []

            await self._ensure_scope_config(scope_id)

            results = await ltm.search_user_mem(
                query=query,
                num=num,
                user_id=user_id,
                scope_id=scope_id,
            )

            memories = []
            for r in results:
                info = r.mem_info
                memories.append(
                    {
                        "id": info.mem_id,
                        "mem": info.content,
                        "mem_type": info.type.value if hasattr(info.type, "value") else str(info.type),
                    }
                )
            return memories

        except Exception as e:
            logger.error("LTM search_user_mem failed: %s", e)

    async def extract_memory_real_time(
        self, messages: list[BaseMessage], conversation_id: str, memory_repo_id: str,
        ir_data: dict | None = None,
    ):
        """Extract memories from conversation via LTM directly."""
        try:
            ltm = get_ltm()
            if ltm is None:
                return

            if not messages:
                return

            await self._ensure_scope_config(memory_repo_id, ir_data=ir_data)

            agent_config = AgentMemoryConfig()

            if ir_data:
                configs = ir_data.get("configs") or {}
                memory_config = configs.get("memory") or {}
                strategies = memory_config.get("strategies") or []
                strategy_types = {s.get("type") for s in strategies if s.get("type")}
                if strategy_types:
                    agent_config.enable_user_profile = "user_profile" in strategy_types
                    agent_config.enable_semantic_memory = "semantic_memory" in strategy_types
                    agent_config.enable_episodic_memory = "episodic_memory" in strategy_types

            await ltm.add_messages(
                messages=messages,
                agent_config=agent_config,
                user_id="",
                scope_id=memory_repo_id,
                session_id=conversation_id,
            )
            logger.info("LTM extract_memory_real_time completed for scope=%s", memory_repo_id)

        except Exception as e:
            logger.error("LTM extract_memory_real_time failed: %s", e)

    async def delete_memory_by_scope_id(self, scope_id: str) -> bool:
        """Delete all memory data and scope config for a memory repo."""
        ltm = get_ltm()
        if ltm is None:
            return True

        try:
            await ltm.delete_mem_by_scope(scope_id)
        except Exception as e:
            logger.error("LTM delete_mem_by_scope failed for %s: %s", scope_id, e)
            return False

        try:
            await ltm.delete_scope_config(scope_id)
        except Exception as e:
            logger.warning("LTM delete_scope_config failed for %s (non-critical): %s", scope_id, e)

        return True

    # ── Variable CRUD (unchanged) ──

    async def retrieve_user_variables(
        self, user_id: str, agent_id: str, variable_keys: Optional[list[str]]
    ) -> VariableList:
        all_variables = await self.list_user_variables(user_id, agent_id)
        if not all_variables:
            return VariableList(variables=[])

        if not variable_keys:
            variables = []
            for key, value in all_variables.items():
                variables.append(
                    VariableValue(
                        variable_key=key,
                        variable_value=value,
                        agent_id=agent_id,
                        user_id=user_id,
                    )
                )
            return VariableList(variables=variables)

        variables = []
        for key in variable_keys:
            if key in all_variables:
                variables.append(
                    VariableValue(
                        variable_key=key,
                        variable_value=all_variables[key],
                        agent_id=agent_id,
                        user_id=user_id,
                    )
                )
        return VariableList(variables=variables)

    async def batch_delete_user_variables(
        self, user_id: str, agent_id: str, variable_keys: Optional[list[str]]
    ) -> list[str]:
        if not variable_keys:
            return []

        all_variables = await self.list_user_variables(user_id, agent_id)
        if not all_variables:
            return []

        deleted_keys = []
        for key in variable_keys:
            if key in all_variables:
                del all_variables[key]
                deleted_keys.append(key)

        redis_key = self._build_variable_memory_key(user_id, agent_id)
        if all_variables:
            await self.kv_store.set(
                redis_key, json.dumps(all_variables, ensure_ascii=False)
            )
        else:
            await self.kv_store.delete(redis_key)

        return deleted_keys

    async def reset_user_variable_memory(self, user_id: str, agent_id: str) -> bool:
        redis_key = self._build_variable_memory_key(user_id, agent_id)
        await self.kv_store.delete(redis_key)
        return True

    def set_scope_llm(self, scope_id: str, llm: BaseChatModel) -> bool:
        return self.local_mem_engine.init_base_llm(llm)

    def register_store(
        self,
        kv_store: BaseKVStore,
        semantic_store: BaseSemanticStore | None = None,
        db_store: BaseDbStore | None = None,
    ):
        return self.local_mem_engine.register_store(kv_store)

    def _build_variable_memory_key(self, user_id: str, agent_id: str) -> str:
        return f"user_var/{user_id}/{agent_id}"

    def _get_project_id(self) -> str:
        return current_request.get().headers.get("x-owner-project-id", "")

    def _get_auth_token(self) -> str:
        return current_request.get().headers.get("X-Auth-Token")

    def get_common_headers(self) -> dict:
        headers = {"X-Auth-Token": self._get_auth_token()}
        return headers
