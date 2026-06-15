import logging
from typing import Any, List, Optional, Union

from openjiuwen.core.foundation.store.base_kv_store import BaseKVStore
from redis.asyncio import Redis
from redis.asyncio.cluster import RedisCluster

logger = logging.getLogger(__name__)


class RedisKVStore(BaseKVStore):
    """Agent-core BaseKVStore backed by the existing Redis client."""

    def __init__(self, redis_client: Union[Redis, RedisCluster]):
        self._redis = redis_client

    async def set(self, key: str, value: str | bytes):
        await self._redis.set(key, value)

    async def exclusive_set(
        self, key: str, value: str | bytes, expiry: int | None = None
    ) -> bool:
        kwargs: dict[str, Any] = {}
        if expiry is not None:
            kwargs["ex"] = expiry
        return bool(await self._redis.set(key, value, nx=True, **kwargs))

    async def get(self, key: str) -> str | bytes | None:
        val = await self._redis.get(key)
        if val is None:
            return None
        return val if isinstance(val, bytes) else val.encode()

    async def exists(self, key: str) -> bool:
        return bool(await self._redis.exists(key))

    async def delete(self, key: str):
        await self._redis.delete(key)

    async def get_by_prefix(self, prefix: str) -> dict[str, str | bytes]:
        result: dict[str, str | bytes] = {}
        cursor: int = 0
        while True:
            cursor, keys = await self._redis.scan(
                cursor=cursor, match=f"{prefix}*", count=200
            )
            if keys:
                values = await self._redis.mget(keys)
                for k, v in zip(keys, values):
                    if v is not None:
                        k_str = k.decode() if isinstance(k, bytes) else k
                        result[k_str] = v
            if cursor == 0:
                break
        return result

    async def delete_by_prefix(self, prefix: str, batch_size: Optional[int] = None):
        if batch_size is not None and batch_size <= 0:
            batch_size = None
        cursor: int = 0
        total_deleted = 0
        while True:
            cursor, keys = await self._redis.scan(
                cursor=cursor, match=f"{prefix}*", count=batch_size or 200
            )
            if keys:
                pipe = self._redis.pipeline()
                for k in keys:
                    pipe.delete(k)
                results = await pipe.execute()
                total_deleted += sum(1 for r in results if r)
            if cursor == 0:
                break
        logger.info("delete_by_prefix '%s': removed %d keys", prefix, total_deleted)

    async def mget(self, keys: List[str]) -> List[str | bytes | None]:
        if not keys:
            return []
        values = await self._redis.mget(keys)
        return [
            (v if isinstance(v, (bytes, str)) else None)
            for v in values
        ]

    async def batch_delete(
        self, keys: List[str], batch_size: Optional[int] = None
    ) -> int:
        if not keys:
            return 0
        if batch_size is not None and batch_size <= 0:
            batch_size = None
        deleted = 0
        chunk_size = batch_size or len(keys)
        for i in range(0, len(keys), chunk_size):
            chunk = keys[i:i + chunk_size]
            pipe = self._redis.pipeline()
            for k in chunk:
                pipe.delete(k)
            results = await pipe.execute()
            deleted += sum(1 for r in results if r)
        return deleted

    def pipeline(self) -> Any:
        return self._redis.pipeline()
