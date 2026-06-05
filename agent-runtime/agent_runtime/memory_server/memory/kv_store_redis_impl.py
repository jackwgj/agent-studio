import logging

from jiuwen.context.memory_engine.store.base_kv_store import BaseKVStore

# 创建logger实例
logger = logging.getLogger(__name__)


class KVStoreRedisImpl(BaseKVStore):
    def __init__(self):
        from memory_service.utils.async_redis_util import get_async_redis_client

        self.client = get_async_redis_client()

    async def set(self, key: str, value: str):
        await self.client.set(key, value)

    async def exclusive_set(
        self, key: str, value: str, expiry: int | None = None
    ) -> bool:
        return await self.client.set(key, value, expiry)

    async def get(self, key: str) -> str | None:
        return await self.client.get(key)

    async def exists(self, key: str) -> bool:
        return await self.client.exists(key)

    async def delete(self, key: str):
        await self.client.delete(key)

    async def get_by_prefix(self, prefix: str) -> dict[str, str]:
        result = await self.client.get_by_prefix(prefix)
        return {k: str(v) if v is not None else None for k, v in result.items()}

    async def delete_by_prefix(self, prefix: str):
        pattern = f"{prefix}*"
        deleted_count = await self.client.delete_by_pattern(pattern)
        logger.info(f"delete keys of prefix '{prefix}', delete {deleted_count} keys")

    async def mget(self, keys: list[str], default: any = None) -> list[str]:
        return await self.client.mget(keys)

    async def delete_by_pattern(self, pattern: str):
        # 当按照应用ID删除记忆时，九问传过来的pattern是app_id，不能直接使用，
        real_pattern = f"topic_profile/*{pattern}*"
        deleted_count = await self.client.delete_by_pattern(real_pattern)
        logger.info(f"delete keys of pattern '{pattern}', delete {deleted_count} keys")
