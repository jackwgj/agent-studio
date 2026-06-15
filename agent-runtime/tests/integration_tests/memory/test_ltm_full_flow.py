# pylint: disable=redefined-outer-name,protected-access,no-self-use
"""Full-flow integration test: init LTM with real services and exercise the memory pipeline.

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
    OPENSEARCH_HOSTS, OPENSEARCH_USERNAME, OPENSEARCH_PASSWORD,
    EMBEDDING_MODEL, EMBEDDING_BASE_URL, EMBEDDING_API_KEY,
    LLM_MODEL, LLM_BASE_URL, LLM_API_KEY,
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


@pytest_asyncio.fixture(scope="session")
async def ltm_module():
    """Module-scoped LTM instance shared across all tests in this module."""
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


class TestLTMFullFlow:
    @pytest.mark.asyncio
    async def test_set_scope_config(self, request, unique_prefix):
        _ltm = request.getfixturevalue("ltm_module")
        """LTM can accept a scope config for a unique scope."""
        scope_id = f"{unique_prefix}_scope1"
        scope_cfg = _make_scope_config()
        await _ltm.set_scope_config(scope_id, scope_cfg)

    @pytest.mark.asyncio
    async def test_add_messages_and_search(self, request, unique_prefix):
        _ltm = request.getfixturevalue("ltm_module")
        """Add conversation messages and search user memory."""
        scope_id = f"{unique_prefix}_scope2"
        user_id = "test_user_001"

        scope_cfg = _make_scope_config()
        await _ltm.set_scope_config(scope_id, scope_cfg)

        messages = [
            BaseMessage(role="user", content="我叫张三，我喜欢编程和打篮球"),
            BaseMessage(role="assistant", content="你好张三！编程和打篮球都是很棒的爱好。"),
        ]
        agent_cfg = AgentMemoryConfig()
        await _ltm.add_messages(
            messages, agent_cfg, user_id=user_id, scope_id=scope_id, gen_mem=False
        )

        await asyncio.sleep(2)

        results = await _ltm.search_user_mem(
            "张三喜欢什么", 3, user_id=user_id, scope_id=scope_id
        )
        assert isinstance(results, list)

    @pytest.mark.asyncio
    async def test_search_empty_scope(self, request, unique_prefix):
        _ltm = request.getfixturevalue("ltm_module")
        """Search on a scope with no messages returns empty results."""
        scope_id = f"{unique_prefix}_scope3"
        user_id = "test_user_002"

        scope_cfg = _make_scope_config()
        await _ltm.set_scope_config(scope_id, scope_cfg)

        results = await _ltm.search_user_mem(
            "test query", 3, user_id=user_id, scope_id=scope_id
        )
        assert isinstance(results, list)
        assert len(results) == 0

    @pytest.mark.asyncio
    async def test_delete_mem_by_scope(self, request, unique_prefix):
        _ltm = request.getfixturevalue("ltm_module")
        """Delete memory by scope ID works without error."""
        scope_id = f"{unique_prefix}_scope4"
        user_id = "test_user_003"

        scope_cfg = _make_scope_config()
        await _ltm.set_scope_config(scope_id, scope_cfg)

        messages = [
            BaseMessage(role="user", content="我喜欢吃火锅"),
            BaseMessage(role="assistant", content="火锅确实很好吃！"),
        ]
        agent_cfg = AgentMemoryConfig()
        await _ltm.add_messages(
            messages, agent_cfg, user_id=user_id, scope_id=scope_id, gen_mem=False
        )
        await asyncio.sleep(2)

        await _ltm.delete_mem_by_scope(scope_id)

    @pytest.mark.asyncio
    async def test_delete_scope_config(self, request, unique_prefix):
        _ltm = request.getfixturevalue("ltm_module")
        """Delete scope config works without error."""
        scope_id = f"{unique_prefix}_scope5"
        scope_cfg = _make_scope_config()
        await _ltm.set_scope_config(scope_id, scope_cfg)
        await _ltm.delete_scope_config(scope_id)
