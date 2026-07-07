# pylint: disable=redefined-outer-name,protected-access,no-self-use
"""End-to-end tests for the memory pipeline with real services.

Requires:
  - OpenSearch
  - Redis
  - Embedding + LLM API keys
"""

import asyncio

import pytest
import pytest_asyncio
import redis.asyncio as aioredis
from openjiuwen.core.foundation.llm.schema.config import ModelClientConfig, ModelRequestConfig
from openjiuwen.core.foundation.llm.schema.message import BaseMessage
from openjiuwen.core.foundation.store.base_embedding import EmbeddingConfig
from openjiuwen.core.memory.config.config import (
    AgentMemoryConfig,
    MemoryEngineConfig,
    MemoryScopeConfig,
)
from openjiuwen.core.memory.long_term_memory import LongTermMemory
from openjiuwen.core.retrieval.embedding.api_embedding import APIEmbedding

from agent_runtime.memory.adapter.opensearch_vector_store import OpenSearchVectorStore
from agent_runtime.memory.adapter.redis_kv_store import RedisKVStore
from agent_runtime.memory.adapter.sqlite_db_store import SqliteDbStore

from .conftest import (
    EMBEDDING_MODEL, EMBEDDING_BASE_URL, EMBEDDING_API_KEY,
    LLM_MODEL, LLM_BASE_URL, LLM_API_KEY,
    OPENSEARCH_HOSTS, OPENSEARCH_USERNAME, OPENSEARCH_PASSWORD,
    REDIS_HOST, REDIS_PORT, REDIS_PASSWORD,
)


def _make_scope_config():
    return MemoryScopeConfig(
        model_cfg=ModelRequestConfig(model=LLM_MODEL),
        model_client_cfg=ModelClientConfig(
            client_provider="OpenAI",
            api_key=LLM_API_KEY,
            api_base=LLM_BASE_URL,
            verify_ssl=False,
        ),
        embedding_cfg=EmbeddingConfig(
            model_name=EMBEDDING_MODEL,
            base_url=EMBEDDING_BASE_URL,
            api_key=EMBEDDING_API_KEY,
        ),
    )


@pytest_asyncio.fixture(scope="session", name="ltm_e2e")
async def _ltm_e2e():
    """Module-scoped LTM instance for e2e tests."""
    redis_client = aioredis.Redis(
        host=REDIS_HOST, port=REDIS_PORT, password=REDIS_PASSWORD, decode_responses=False
    )
    await redis_client.ping()

    kv_store = RedisKVStore(redis_client)
    vector_store = OpenSearchVectorStore(
        hosts=[OPENSEARCH_HOSTS],
        http_auth=(OPENSEARCH_USERNAME, OPENSEARCH_PASSWORD),
        use_ssl=True,
        verify_certs=False,
        ssl_show_warn=False,
    )
    embedding_model = APIEmbedding(config=EmbeddingConfig(
        model_name=EMBEDDING_MODEL,
        base_url=EMBEDDING_BASE_URL,
        api_key=EMBEDDING_API_KEY,
    ))
    db_store = SqliteDbStore()

    engine_cfg = MemoryEngineConfig()
    engine_cfg.default_model_cfg = ModelRequestConfig(model=LLM_MODEL)
    engine_cfg.default_model_client_cfg = ModelClientConfig(
        client_provider="OpenAI",
        api_key=LLM_API_KEY,
        api_base=LLM_BASE_URL,
        verify_ssl=False,
    )

    ltm = LongTermMemory()
    await ltm.register_store(
        kv_store=kv_store, vector_store=vector_store,
        db_store=db_store, embedding_model=embedding_model,
    )
    ltm.set_config(engine_cfg)

    yield ltm

    await redis_client.aclose()
    await vector_store._client.close()


class TestLTME2E:
    """End-to-end tests with gen_mem=True exercising the full pipeline."""

    @pytest.mark.asyncio
    async def test_full_pipeline_with_gen_mem(self, ltm_e2e, unique_prefix):
        """Add messages with gen_mem=True, LLM extracts memory, semantic search finds it."""
        scope_id = f"{unique_prefix}_e2e_full"
        user_id = "e2e_user_001"

        scope_cfg = _make_scope_config()
        await ltm_e2e.set_scope_config(scope_id, scope_cfg)

        messages = [
            BaseMessage(role="user", content="张三最喜欢编程和打篮球，周末经常去公园打球"),
            BaseMessage(role="assistant", content="你好张三！编程和打篮球都是很棒的爱好，周末去公园打球听起来很健康。"),
        ]
        agent_cfg = AgentMemoryConfig()
        await ltm_e2e.add_messages(
            messages, agent_cfg, user_id=user_id, scope_id=scope_id, gen_mem=True
        )

        await asyncio.sleep(20)

        results = await ltm_e2e.search_user_mem(
            "张三喜欢什么运动", 5, user_id=user_id, scope_id=scope_id, threshold=0.1
        )
        assert len(results) > 0, "Expected at least one memory result after gen_mem=True"
        mem_content = " ".join(r.mem_info.content for r in results)
        assert "篮球" in mem_content or "编程" in mem_content, \
            f"Expected memory about 篮球/编程, got: {mem_content}"

    @pytest.mark.asyncio
    async def test_search_after_multiple_conversations(self, ltm_e2e, unique_prefix):
        """Add multiple conversation topics, verify each is independently searchable."""
        scope_id = f"{unique_prefix}_e2e_multi"
        user_id = "e2e_user_002"

        scope_cfg = _make_scope_config()
        await ltm_e2e.set_scope_config(scope_id, scope_cfg)

        topics = [
            ("conv_food", "我特别喜欢吃四川火锅，又麻又辣", "火锅是四川的特色美食，非常受欢迎"),
            ("conv_sport", "我每周都会去游泳，坚持了三年了", "游泳是很好的全身运动，坚持三年很厉害"),
            ("conv_music", "我在学弹吉他，最近在练一首民谣", "学吉他很有意思，民谣很适合入门"),
        ]
        agent_cfg = AgentMemoryConfig()

        for conv_id, user_msg, assistant_msg in topics:
            messages = [
                BaseMessage(role="user", content=user_msg),
                BaseMessage(role="assistant", content=assistant_msg),
            ]
            await ltm_e2e.add_messages(
                messages, agent_cfg,
                user_id=user_id, scope_id=scope_id,
                session_id=conv_id, gen_mem=True,
            )
            await asyncio.sleep(15)

        results = await ltm_e2e.search_user_mem(
            "这个人在学什么乐器", 5, user_id=user_id, scope_id=scope_id, threshold=0.1
        )
        assert len(results) > 0, "Expected at least one result for music topic"
        mem_content = " ".join(r.mem_info.content for r in results)
        assert "吉他" in mem_content, f"Expected memory about 吉他, got: {mem_content}"

    @pytest.mark.asyncio
    async def test_cross_scope_isolation(self, ltm_e2e, unique_prefix):
        """Memories in scope A are not visible when searching scope B."""
        scope_a = f"{unique_prefix}_scope_a"
        scope_b = f"{unique_prefix}_scope_b"
        user_a = "e2e_user_isolation_a"
        user_b = "e2e_user_isolation_b"

        scope_cfg = _make_scope_config()
        await ltm_e2e.set_scope_config(scope_a, scope_cfg)
        await ltm_e2e.set_scope_config(scope_b, scope_cfg)

        messages_a = [
            BaseMessage(role="user", content="我家养了一只橘猫，叫小橘子"),
            BaseMessage(role="assistant", content="橘猫很可爱，小橘子是个好名字"),
        ]
        messages_b = [
            BaseMessage(role="user", content="我家养了一只金毛犬，叫大黄"),
            BaseMessage(role="assistant", content="金毛犬很温顺，大黄是个好名字"),
        ]
        agent_cfg = AgentMemoryConfig()

        await ltm_e2e.add_messages(
            messages_a, agent_cfg, user_id=user_a, scope_id=scope_a, gen_mem=True
        )
        await ltm_e2e.add_messages(
            messages_b, agent_cfg, user_id=user_b, scope_id=scope_b, gen_mem=True
        )
        await asyncio.sleep(20)

        results_a = await ltm_e2e.search_user_mem(
            "宠物", 5, user_id=user_a, scope_id=scope_a, threshold=0.1
        )
        if len(results_a) > 0:
            mem_content_a = " ".join(r.mem_info.content for r in results_a)
            assert "橘猫" in mem_content_a, f"Scope A should contain 橘猫, got: {mem_content_a}"
            assert "金毛" not in mem_content_a, f"Scope A should NOT contain 金毛, got: {mem_content_a}"

    @pytest.mark.asyncio
    async def test_delete_clears_search_results(self, ltm_e2e, unique_prefix):
        """After deleting memory by scope, search returns empty results."""
        scope_id = f"{unique_prefix}_e2e_delete"
        user_id = "e2e_user_003"

        scope_cfg = _make_scope_config()
        await ltm_e2e.set_scope_config(scope_id, scope_cfg)

        messages = [
            BaseMessage(role="user", content="我住在杭州西湖区，公司在文三路"),
            BaseMessage(role="assistant", content="杭州是个美丽的城市，西湖区环境很好"),
        ]
        agent_cfg = AgentMemoryConfig()
        await ltm_e2e.add_messages(
            messages, agent_cfg, user_id=user_id, scope_id=scope_id, gen_mem=True
        )
        await asyncio.sleep(20)

        results_before = await ltm_e2e.search_user_mem(
            "住在哪里", 5, user_id=user_id, scope_id=scope_id, threshold=0.1
        )
        assert len(results_before) > 0, "Precondition: should find memory before deletion"

        await ltm_e2e.delete_mem_by_scope(scope_id)

        results_after = await ltm_e2e.search_user_mem(
            "住在哪里", 5, user_id=user_id, scope_id=scope_id, threshold=0.1
        )
        assert len(results_after) == 0, "Expected empty results after deletion"

    @pytest.mark.asyncio
    async def test_scope_config_lifecycle(self, ltm_e2e, unique_prefix):
        """Scope config can be created, retrieved, and deleted."""
        scope_id = f"{unique_prefix}_e2e_config"

        scope_cfg = _make_scope_config()
        await ltm_e2e.set_scope_config(scope_id, scope_cfg)

        config = await ltm_e2e.get_scope_config(scope_id)
        assert config is not None, "Expected scope config to exist after set"

        await ltm_e2e.delete_scope_config(scope_id)

        config_after = await ltm_e2e.get_scope_config(scope_id)
        assert config_after is None, "Expected scope config to be None after deletion"
