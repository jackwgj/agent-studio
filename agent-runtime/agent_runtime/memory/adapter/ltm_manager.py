from typing import Optional

from openjiuwen.core.common.logging import memory_logger as logger
from openjiuwen.core.foundation.llm.schema.config import ModelClientConfig, ModelRequestConfig
from openjiuwen.core.foundation.store.base_embedding import EmbeddingConfig
from agent_runtime.memory.adapter.opensearch_vector_store import OpenSearchVectorStore
from agent_runtime.memory.adapter.redis_kv_store import RedisKVStore
from openjiuwen.core.foundation.store.db.default_db_store import DefaultDbStore
from sqlalchemy.ext.asyncio import create_async_engine
from openjiuwen.core.memory.config.config import MemoryEngineConfig, MemoryScopeConfig
from openjiuwen.core.memory.long_term_memory import LongTermMemory
from openjiuwen.core.retrieval.embedding.api_embedding import APIEmbedding
from redis.asyncio import Redis
from redis.asyncio.cluster import RedisCluster

from agent_runtime.common.config import settings

_ltm_instance: Optional[LongTermMemory] = None


async def init_ltm(redis_client: Redis | RedisCluster) -> bool:
    """Initialize LongTermMemory with Redis KV store, OpenSearch vector store, and embedding model.

    Returns True if LTM was initialized successfully, False if degraded (memory disabled).
    """
    global _ltm_instance

    mem_settings = settings.memory

    if not mem_settings.enabled:
        logger.info("Memory library disabled by MEMORY_ENABLED=false")
        return False

    # 1. Redis KV store adapter
    kv_store = RedisKVStore(redis_client)

    # 2. OpenSearch vector store — ping first for graceful degradation
    os_settings = settings.opensearch
    hosts = [h.strip() for h in os_settings.hosts.split(",") if h.strip()]
    os_kwargs: dict = {}
    if os_settings.username:
        os_kwargs["http_auth"] = (os_settings.username, os_settings.password)

    # Auto-detect use_ssl from hosts URL scheme when not explicitly set
    use_ssl = os_settings.use_ssl
    if use_ssl is None:
        use_ssl = any(h.startswith("https://") for h in hosts)

    if use_ssl:
        os_kwargs["use_ssl"] = True
        os_kwargs["verify_certs"] = os_settings.ssl_verify
        if not os_settings.ssl_verify:
            os_kwargs["ssl_show_warn"] = False
    else:
        os_kwargs["use_ssl"] = False
        os_kwargs["verify_certs"] = False

    vector_store = OpenSearchVectorStore(hosts=hosts, **os_kwargs)
    try:
        alive = await vector_store.ping()
        if not alive:
            logger.warning(
                "OpenSearch ping returned False — memory library not available (non-critical)"
            )
            return False
    except Exception as e:
        logger.warning(
            "OpenSearch unreachable: %s — memory library not available (non-critical)", e
        )
        return False

    # 3. Embedding model
    embedding_model = None
    if mem_settings.embedding_base_url:
        emb_config = EmbeddingConfig(
            model_name=mem_settings.embedding_model,
            base_url=mem_settings.embedding_base_url,
            api_key=mem_settings.embedding_api_key,
        )
        embedding_model = APIEmbedding(config=emb_config)
    else:
        logger.warning(
            "MEMORY_EMBEDDING_BASE_URL not set — embedding disabled, search/extraction will fail"
        )

    # 4. MySQL db_store (persistent text storage)
    _ensure_database(
        host=mem_settings.db_host,
        port=mem_settings.db_port,
        user=mem_settings.db_user,
        password=mem_settings.db_password,
        database=mem_settings.db_name,
    )

    mysql_url = (
        f"mysql+aiomysql://{mem_settings.db_user}:{mem_settings.db_password}"
        f"@{mem_settings.db_host}:{mem_settings.db_port}/{mem_settings.db_name}?charset=utf8mb4"
    )
    engine = create_async_engine(
        mysql_url, pool_size=5, max_overflow=10, pool_recycle=3600
    )
    db_store = DefaultDbStore(engine)

    # 5. Build MemoryEngineConfig with LLM settings for extraction
    engine_config = _build_engine_config(mem_settings)

    # 6. Initialize LTM singleton
    ltm = LongTermMemory()
    await ltm.register_store(
        kv_store=kv_store,
        vector_store=vector_store,
        db_store=db_store,
        embedding_model=embedding_model,
    )

    # set_config is synchronous — do NOT await
    ltm.set_config(engine_config)

    _ltm_instance = ltm
    logger.info(
        "Memory library enabled (embedding=%s, opensearch=%s)",
        mem_settings.embedding_model,
        os_settings.hosts,
    )
    return True


def get_ltm() -> Optional[LongTermMemory]:
    """Return the global LTM instance, or None if not initialized."""
    return _ltm_instance


def _ensure_database(host: str, port: int, user: str, password: str, database: str) -> None:
    """Create the MySQL database if it does not exist (synchronous, idempotent)."""
    import pymysql

    conn = pymysql.connect(host=host, port=port, user=user, password=password)
    try:
        with conn.cursor() as cur:
            cur.execute(
                f"CREATE DATABASE IF NOT EXISTS `{database}` "
                "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            )
        conn.commit()
    finally:
        conn.close()


def build_scope_config() -> MemoryScopeConfig:
    """Build a MemoryScopeConfig from current environment settings."""
    mem_settings = settings.memory
    scope_cfg = MemoryScopeConfig()

    if mem_settings.llm_model:
        scope_cfg.model_cfg = ModelRequestConfig(model=mem_settings.llm_model)
    if mem_settings.llm_base_url:
        scope_cfg.model_client_cfg = ModelClientConfig(
            client_provider="OpenAI",
            api_key=mem_settings.llm_api_key or "sk-placeholder",
            api_base=mem_settings.llm_base_url,
            verify_ssl=False,
        )
    if mem_settings.embedding_base_url:
        scope_cfg.embedding_cfg = EmbeddingConfig(
            model_name=mem_settings.embedding_model,
            base_url=mem_settings.embedding_base_url,
            api_key=mem_settings.embedding_api_key,
        )

    return scope_cfg


def _build_engine_config(mem_settings) -> MemoryEngineConfig:
    cfg = MemoryEngineConfig()
    if mem_settings.llm_model:
        cfg.default_model_cfg = ModelRequestConfig(model=mem_settings.llm_model)
    if mem_settings.llm_base_url:
        cfg.default_model_client_cfg = ModelClientConfig(
            client_provider="OpenAI",
            api_key=mem_settings.llm_api_key or "sk-placeholder",
            api_base=mem_settings.llm_base_url,
            verify_ssl=False,
        )
    return cfg
