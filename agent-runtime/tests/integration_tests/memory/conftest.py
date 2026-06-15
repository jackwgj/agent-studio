# pylint: disable=redefined-outer-name,protected-access,broad-exception-caught
"""Shared fixtures for memory integration tests.

Requires running services:
  - OpenSearch on https://127.0.0.1:9200
  - Redis on 127.0.0.1:6379
  - DashScope API key for embedding and LLM

Configuration is read from test_config.env in the same directory.
If the file is missing, falls back to environment variables.
See test_config.env.example for the expected format.
"""

import os
import sys
import uuid
from pathlib import Path
from unittest.mock import MagicMock

import pytest
import pytest_asyncio
import redis.asyncio as aioredis

# Pre-mock heavy jiuwen modules so imports don't fail
for _mod in [
    "jiuwen.orchestration", "jiuwen.orchestration.callbacks",
    "jiuwen.orchestration.callbacks.manager", "jiuwen.orchestration.base",
    "jiuwen.common.log", "jiuwen.common.log.base",
    "jiuwen.serve", "jiuwen.serve.common", "jiuwen.serve.common.context",
    "agent_runtime.memory.global_vals_callback_handler",
    "utils", "utils.constants",
]:
    if _mod not in sys.modules:
        sys.modules[_mod] = MagicMock()


def _load_config():
    """Load config from test_config.env, then environment variables as fallback."""
    config = {}

    env_file = Path(__file__).parent / "test_config.env"
    if env_file.exists():
        with open(env_file) as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                if "=" in line:
                    key, _, value = line.partition("=")
                    config[key.strip()] = value.strip()

    # Environment variables take precedence over file values
    for key in [
        "EMBEDDING_MODEL", "EMBEDDING_BASE_URL", "EMBEDDING_API_KEY",
        "LLM_MODEL", "LLM_BASE_URL", "LLM_API_KEY",
        "OPENSEARCH_HOSTS", "OPENSEARCH_USERNAME", "OPENSEARCH_PASSWORD",
        "REDIS_HOST", "REDIS_PORT", "REDIS_PASSWORD",
    ]:
        env_val = os.environ.get(key)
        if env_val is not None:
            config[key] = env_val

    return config


_cfg = _load_config()

EMBEDDING_MODEL = _cfg.get("EMBEDDING_MODEL", "text-embedding-v4")
EMBEDDING_BASE_URL = _cfg.get("EMBEDDING_BASE_URL", "")
EMBEDDING_API_KEY = _cfg.get("EMBEDDING_API_KEY", "")

LLM_MODEL = _cfg.get("LLM_MODEL", "deepseek-v4-pro")
LLM_BASE_URL = _cfg.get("LLM_BASE_URL", "")
LLM_API_KEY = _cfg.get("LLM_API_KEY", "")

OPENSEARCH_HOSTS = _cfg.get("OPENSEARCH_HOSTS", "https://127.0.0.1:9200")
OPENSEARCH_USERNAME = _cfg.get("OPENSEARCH_USERNAME", "admin")
OPENSEARCH_PASSWORD = _cfg.get("OPENSEARCH_PASSWORD", "")

REDIS_HOST = _cfg.get("REDIS_HOST", "127.0.0.1")
REDIS_PORT = int(_cfg.get("REDIS_PORT", "6379"))
REDIS_PASSWORD = _cfg.get("REDIS_PASSWORD", "")


@pytest_asyncio.fixture
async def redis_client():
    """Real async Redis client."""
    client = aioredis.Redis(
        host=REDIS_HOST, port=REDIS_PORT, password=REDIS_PASSWORD, decode_responses=False
    )
    await client.ping()
    yield client
    await client.aclose()


@pytest.fixture
def unique_prefix():
    """Generate a unique prefix for test collections/keys to avoid collisions."""
    return f"test_{uuid.uuid4().hex[:8]}"


@pytest_asyncio.fixture
async def cleanup_opensearch():
    """Track OpenSearch indices created during test and clean them up."""
    from agent_runtime.memory.adapter.opensearch_vector_store import OpenSearchVectorStore

    indices = []

    def track(name):
        indices.append(name)
        return name

    yield track

    vs = OpenSearchVectorStore(
        hosts=[OPENSEARCH_HOSTS],
        http_auth=(OPENSEARCH_USERNAME, OPENSEARCH_PASSWORD),
        use_ssl=True,
        verify_certs=False,
        ssl_show_warn=False,
    )
    for idx in indices:
        try:
            await vs.delete_collection(idx)
        except Exception as e:  # noqa: G.ERR.07
            import logging
            logging.getLogger(__name__).debug("cleanup: %s", e)


@pytest_asyncio.fixture
async def cleanup_redis(request):
    """Track Redis keys created during test and clean them up."""
    _rc = request.getfixturevalue("redis_client")
    keys = []

    def track(key):
        keys.append(key)
        return key

    yield track

    for k in keys:
        try:
            await _rc.delete(k)
        except Exception as e:  # noqa: G.ERR.07
            import logging
            logging.getLogger(__name__).debug("cleanup: %s", e)
