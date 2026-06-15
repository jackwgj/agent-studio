# pylint: disable=redefined-outer-name,protected-access,no-self-use
"""Integration test for memory extraction with strategy-based scope config.

Verifies the pipeline: set scope config with strategy prompts -> add messages
with AgentMemoryConfig -> LLM extracts memory -> search confirms results.

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
from openjiuwen.core.foundation.store.base_embedding import EmbeddingConfig
from openjiuwen.core.memory.config.config import (
    AgentMemoryConfig,
    MemoryEngineConfig,
    MemoryScopeConfig,
)
from openjiuwen.core.memory.long_term_memory import LongTermMemory
from openjiuwen.core.retrieval.embedding.api_embedding import APIEmbedding
from openjiuwen.core.foundation.llm.schema.message import BaseMessage

from agent_runtime.memory.adapter.opensearch_vector_store import OpenSearchVectorStore
from agent_runtime.memory.adapter.redis_kv_store import RedisKVStore
from agent_runtime.memory.adapter.sqlite_db_store import SqliteDbStore

from .conftest import (
    EMBEDDING_MODEL, EMBEDDING_BASE_URL, EMBEDDING_API_KEY,
    LLM_MODEL, LLM_BASE_URL, LLM_API_KEY,
    OPENSEARCH_HOSTS, OPENSEARCH_USERNAME, OPENSEARCH_PASSWORD,
    REDIS_HOST, REDIS_PORT, REDIS_PASSWORD,
)


def _make_scope_config_with_prompts():
    """Scope config with custom extraction prompts matching the IR strategy flow."""
    cfg = MemoryScopeConfig(
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
    cfg.user_profile_definition = (
        "Extract user profile information including name, "
        "preferences, occupation, and location"
    )
    cfg.semantic_memory_definition = "Extract factual knowledge and semantic information from the conversation"
    return cfg


@pytest_asyncio.fixture(scope="session")
async def ltm_extraction():
    """Module-scoped LTM instance for extraction integration tests."""
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


class TestMemoryExtractionIntegration:
    """Integration tests for the _extract_memory pipeline with scope config."""

    @pytest.mark.asyncio
    async def test_scope_config_prompts_guide_extraction(self, request, unique_prefix):
        _ltm = request.getfixturevalue("ltm_extraction")
        """Custom definition prompts in scope config guide what LLM extracts."""
        scope_id = f"{unique_prefix}_extract_prompts"
        user_id = "extract_user_001"

        scope_cfg = _make_scope_config_with_prompts()
        await _ltm.set_scope_config(scope_id, scope_cfg)

        stored_cfg = await _ltm.get_scope_config(scope_id)
        assert stored_cfg is not None
        assert stored_cfg.user_profile_definition is not None

        messages = [
            BaseMessage(role="user", content="我叫王五，在杭州做产品经理，平时喜欢爬山和摄影"),
            BaseMessage(role="assistant", content="你好王五！产品经理是很锻炼人的岗位，爬山和摄影都是很好的户外爱好。"),
        ]

        agent_cfg = AgentMemoryConfig(
            enable_user_profile=True,
            enable_semantic_memory=True,
            enable_episodic_memory=False,
        )
        await _ltm.add_messages(
            messages, agent_cfg, user_id=user_id, scope_id=scope_id, gen_mem=True
        )

        await asyncio.sleep(25)

        results = await _ltm.search_user_mem(
            "王五的职业是什么", 5, user_id=user_id, scope_id=scope_id, threshold=0.1
        )
        assert len(results) > 0, "Expected at least one memory result after extraction"
        mem_content = " ".join(r.mem_info.content for r in results)
        assert "产品经理" in mem_content or "王五" in mem_content, \
            f"Expected memory about occupation/name, got: {mem_content}"

        await _ltm.delete_mem_by_scope(scope_id)
        await _ltm.delete_scope_config(scope_id)

    @pytest.mark.asyncio
    async def test_strategy_specific_enable_flags(self, request, unique_prefix):
        _ltm = request.getfixturevalue("ltm_extraction")
        """Verify AgentMemoryConfig enable flags are accepted without error."""
        scope_id = f"{unique_prefix}_extract_flags"
        user_id = "extract_user_002"

        scope_cfg = _make_scope_config_with_prompts()
        await _ltm.set_scope_config(scope_id, scope_cfg)

        messages = [
            BaseMessage(role="user", content="我最近去了趟云南旅游，去了大理和丽江，风景太美了"),
            BaseMessage(role="assistant", content="云南确实很美！大理的洱海和丽江的古城都是必去的景点。"),
        ]

        agent_cfg = AgentMemoryConfig(
            enable_user_profile=False,
            enable_semantic_memory=True,
            enable_episodic_memory=False,
        )
        await _ltm.add_messages(
            messages, agent_cfg, user_id=user_id, scope_id=scope_id, gen_mem=True
        )

        await asyncio.sleep(25)

        results = await _ltm.search_user_mem(
            "云南旅游", 5, user_id=user_id, scope_id=scope_id, threshold=0.1
        )
        assert isinstance(results, list), "search_user_mem should return a list"

        await _ltm.delete_mem_by_scope(scope_id)
        await _ltm.delete_scope_config(scope_id)

    @pytest.mark.asyncio
    async def test_scope_config_created_before_extraction(self, request, unique_prefix):
        _ltm = request.getfixturevalue("ltm_extraction")
        """Scope config must exist before add_messages for custom prompts to take effect."""
        scope_id = f"{unique_prefix}_extract_config_first"
        user_id = "extract_user_003"

        no_config = await _ltm.get_scope_config(scope_id)
        assert no_config is None, "Precondition: scope should not have config yet"

        scope_cfg = _make_scope_config_with_prompts()
        await _ltm.set_scope_config(scope_id, scope_cfg)

        stored_cfg = await _ltm.get_scope_config(scope_id)
        assert stored_cfg is not None, "Config should exist after set_scope_config"

        messages = [
            BaseMessage(role="user", content="我养了两只猫，一只叫小白一只叫小花，都是流浪猫收养的"),
            BaseMessage(role="assistant", content="收养流浪猫很有爱心！小白和小花很幸运遇到你。"),
        ]

        agent_cfg = AgentMemoryConfig(
            enable_user_profile=True,
            enable_semantic_memory=True,
            enable_episodic_memory=False,
        )
        await _ltm.add_messages(
            messages, agent_cfg, user_id=user_id, scope_id=scope_id, gen_mem=True
        )

        await asyncio.sleep(25)

        results = await _ltm.search_user_mem(
            "猫", 5, user_id=user_id, scope_id=scope_id, threshold=0.1
        )
        assert len(results) > 0, "Expected memory results after scope config setup"

        await _ltm.delete_mem_by_scope(scope_id)
        await _ltm.delete_scope_config(scope_id)
