# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
import asyncio

from jiuwen.common.llm_service.messages import HumanMessage
from jiuwen.extension.memory.client.memory_client import MemoryClient
from openjiuwen.core.common.logging import memory_logger
from openjiuwen.core.common.logging.events import LogEventType
from openjiuwen.core.memory.long_term_memory import LongTermMemory
from openjiuwen.core.memory.prompts.prompt_applier import PromptApplier


class MemoryProxy:
    _custom_memory_client: MemoryClient | None = None
    DEFAULT_VALUE: str = "__default__"

    def __init__(self, memory_config=None):
        self._local_memory_engine = LongTermMemory()

    @classmethod
    def register_client(cls, custom_mem_client: MemoryClient | None = None):
        if custom_mem_client:
            if isinstance(custom_mem_client, MemoryClient):
                cls._custom_memory_client = custom_mem_client
            else:
                memory_logger.error(
                    "Invalid custom_mem_client format.",
                    event_type=LogEventType.MEMORY_INIT,
                )

    def _get_client(self) -> MemoryClient:
        if self._custom_memory_client:
            return self._custom_memory_client
        return self._local_memory_engine

    def __getattr__(self, name: str):
        client = self._get_client()
        attr = getattr(client, name)
        return attr

    async def get_related_memory_msg(
        self, user_id: str, scope_id: str, query: str
    ) -> HumanMessage | None:
        has_mem = False
        memory_content = ""
        search_mems = await self._get_client().search_user_mem(
            query=query, num=20, user_id=user_id, scope_id=scope_id
        )
        for mem in search_mems:
            if mem is None:
                continue
            mem_content = (
                mem.mem_info.content if hasattr(mem, "mem_info") else mem.get("mem", "")
            )
            if mem_content:
                memory_content += f"<mem>{mem_content}</mem>\n"
                has_mem = True
        search_summary_mems = await self._get_client().search_user_history_summary(
            query=query, num=5, user_id=user_id, scope_id=scope_id
        )
        for mem in search_summary_mems:
            if mem is None:
                continue
            mem_content = (
                mem.mem_info.content if hasattr(mem, "mem_info") else mem.get("mem", "")
            )
            if mem_content:
                memory_content += f"<history_summary>{mem_content}</history_summary>\n"
                has_mem = True
        if not has_mem:
            memory_logger.info(
                "No memory found for query: %s",
                query,
                event_type=LogEventType.MEMORY_RETRIEVE,
                user_id=user_id,
                scope_id=scope_id,
            )
            return None
        msg = PromptApplier().apply(
            "memory_usage_prompt", {"MEMORY_CONTENT": memory_content}
        )
        return HumanMessage(content=msg)

    def set_scope_config_sync(self, scope_id: str, memory_scope_config) -> bool:
        try:
            loop = asyncio.get_running_loop()
        except RuntimeError:
            memory_logger.debug(
                "No running event loop found, create a new one.",
                event_type=LogEventType.MEMORY_INIT,
            )
            asyncio.run(
                self._get_client().set_scope_config(scope_id, memory_scope_config)
            )
            return
        asyncio.run_coroutine_threadsafe(
            self._get_client().set_scope_config(scope_id, memory_scope_config), loop
        )

    def add_messages_sync(
        self,
        messages,
        agent_config,
        *,
        user_id=DEFAULT_VALUE,
        scope_id=DEFAULT_VALUE,
        session_id=DEFAULT_VALUE,
        timestamp=None,
        gen_mem=True,
        gen_mem_with_history_msg_num=2,
    ):
        try:
            loop = asyncio.get_running_loop()
        except RuntimeError:
            memory_logger.debug(
                "No running event loop found, create a new one.",
                event_type=LogEventType.MEMORY_INIT,
            )
            asyncio.run(
                self._get_client().add_messages(
                    messages,
                    agent_config,
                    user_id=user_id,
                    scope_id=scope_id,
                    session_id=session_id,
                    timestamp=timestamp,
                    gen_mem=gen_mem,
                    gen_mem_with_history_msg_num=gen_mem_with_history_msg_num,
                )
            )
            return
        asyncio.run_coroutine_threadsafe(
            self._get_client().add_messages(
                messages,
                agent_config,
                user_id=user_id,
                scope_id=scope_id,
                session_id=session_id,
                timestamp=timestamp,
                gen_mem=gen_mem,
                gen_mem_with_history_msg_num=gen_mem_with_history_msg_num,
            ),
            loop,
        )
