import asyncio
import logging
from datetime import datetime, timezone
from typing import List, Tuple, Optional

from jiuwen.context.memory_engine.store.base_memory_index import (
    BaseMemoryIndex,
    MemoryDoc,
)
from memory_service.config.settings import (
    VECTOR_STORE_DEFAULT_INDEX_NAME,
    USER_MAX_MEMORY_COUNT,
)
from memory_service.exception.error_code import ErrorCode
from memory_service.exception.memory_service_exception import MemoryServiceException
from memory_service.i18n.i18n_constant import I18nMessageCode
from memory_service.i18n.i18n_util import i18n_util
from memory_service.memory.embedding.base_embedding import Embedding
from memory_service.memory.vector_store.base_vector_store import (
    BaseVectorStore,
    SearchDocsItem,
)

logger = logging.getLogger(__name__)


class LongTermMemoryIndex(BaseMemoryIndex):
    """
    长期记忆索引实现

    采用单例模式，提供基于向量存储和嵌入模型的长期记忆管理功能。
    """

    _instance = None
    _initialized = False

    def __new__(cls, *args, **kwargs):
        """实现单例模式"""
        if cls._instance is None:
            cls._instance = super(LongTermMemoryIndex, cls).__new__(cls)
        return cls._instance

    def __init__(
        self,
        vector_store: Optional[BaseVectorStore] = None,
        embedding_model: Optional[Embedding] = None,
    ):
        """初始化长期记忆索引"""
        if self._initialized:
            return

        self._vector_store = vector_store
        self._embedding_model = embedding_model
        self._index_name = VECTOR_STORE_DEFAULT_INDEX_NAME
        self._initialized = True
        self._index_initialized = False  # 单独跟踪索引是否已初始化
        self._dependency_service = (
            vector_store is not None and embedding_model is not None
        )

        # 用于缓存记忆数量的字典 { (user_id, scope_id): count }
        self._memory_count_cache = {}
        self._last_cache_time = {}  # {(user_id, scope_id): last_update_time}
        self._cache_ttl = 30  # 缓存TTL为30秒
        self._max_cache_size = 10000  # 最大缓存数量

    def initialize_index_sync(self):
        """同步初始化向量索引（在已有事件循环的情况下调用）"""
        try:
            if self._vector_store is None:
                logger.warning(
                    "Vector store is not initialized, skipping index creation"
                )
                return

            # 创建同步的事件循环来执行异步初始化
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            try:
                loop.run_until_complete(self._initialize_index())
            finally:
                loop.close()

        except Exception as e:
            logger.error(
                f"Failed to create index {self._index_name}: {e}", exc_info=True
            )

    async def _initialize_index(self):
        """异步初始化向量索引（备用方法）"""
        try:
            if self._vector_store is None:
                logger.warning(
                    "Vector store is not initialized, skipping index creation"
                )
                return

            await self._vector_store.create_collection(collection_name=self._index_name)
            logger.info(f"Successfully created or ensured index: {self._index_name}")

        except Exception as e:
            logger.error(
                f"Failed to create index {self._index_name}: {e}", exc_info=True
            )

    async def _ensure_initialized(self):
        """确保类已正确初始化"""
        # 确保索引已初始化
        await self._ensure_index_initialized()

    async def _ensure_index_initialized(self):
        """确保向量索引已初始化"""
        if not self._index_initialized:
            await self._initialize_index()
            self._index_initialized = True

    def _build_filter(
        self, user_id: str, scope_id: str, mem_type: Optional[str] = None
    ) -> dict:
        """构建查询过滤条件"""
        filters = {"user_id": user_id, "app_id": scope_id}
        if mem_type:
            filters["memory_type"] = mem_type
        return filters

    def _get_cache_key(self, user_id: str, scope_id: str) -> tuple:
        """生成缓存键"""
        return (user_id, scope_id)

    def _is_cache_valid(self, cache_key: tuple) -> bool:
        """检查缓存是否有效"""
        if cache_key not in self._last_cache_time:
            return False

        current_time = datetime.now(timezone.utc).timestamp()
        return (current_time - self._last_cache_time[cache_key]) < self._cache_ttl

    def _cleanup_expired_cache(self):
        """清理过期的缓存"""
        current_time = datetime.now(timezone.utc).timestamp()
        keys_to_remove = []

        for key, last_time in self._last_cache_time.items():
            if (current_time - last_time) >= self._cache_ttl:
                keys_to_remove.append(key)

        for key in keys_to_remove:
            self._memory_count_cache.pop(key, None)
            self._last_cache_time.pop(key, None)

        # 清理过期缓存后仍超过最大大小，按LRU原则删除
        while len(self._memory_count_cache) > self._max_cache_size:
            # 找到最久未使用的缓存项
            oldest_key = min(
                self._last_cache_time.keys(), key=lambda k: self._last_cache_time[k]
            )
            self._memory_count_cache.pop(oldest_key, None)
            self._last_cache_time.pop(oldest_key, None)

    def _update_cache(self, cache_key: tuple, count: int):
        """更新缓存"""
        # 在更新前清理一次过期缓存
        self._cleanup_expired_cache()

        self._memory_count_cache[cache_key] = count
        self._last_cache_time[cache_key] = datetime.now(timezone.utc).timestamp()

    async def _get_memory_count(self, user_id: str, scope_id: str) -> int:
        """获取指定用户和范围的记忆总数，使用缓存减少远程调用次数"""
        cache_key = self._get_cache_key(user_id, scope_id)

        # 检查缓存是否有效
        if self._is_cache_valid(cache_key):
            cached_count = self._memory_count_cache[cache_key]
            if cached_count >= 0:  # >= 0表示有效的数量，-1表示查询失败
                return cached_count

        # 缓存无效或不存在，执行远程查询
        try:
            await self._ensure_initialized()
            filters = self._build_filter(user_id, scope_id)

            # 查询符合条件的数据，只查询一条来获取总数
            query_result = await self._vector_store.list_docs(
                self._index_name, filters=filters, limit=1, offset=0
            )

            total_count = getattr(query_result, "total", -1)
            if total_count is None:
                total_count = -1  # 如果vector_store不支持总数，返回-1

            self._update_cache(cache_key, total_count)
            return total_count

        except Exception as e:
            logger.error(f"Failed to get memory count for {cache_key}: {e}")
            # 查询失败时返回缓存的值（如果有），否则返回-1
            return self._memory_count_cache.get(cache_key, -1)

    def _memory_doc_to_store_doc(
        self, memory_doc: MemoryDoc, user_id: str, scope_id: str, embedding: List[float]
    ) -> dict:
        """将MemoryDoc转换为向量存储格式"""
        return {
            "user_id": user_id,
            "app_id": scope_id,
            "memory_type": memory_doc.type,
            "content": memory_doc.text,
            "content_vector": embedding,
            "last_updated": int(memory_doc.timestamp.timestamp() * 1000)
            if memory_doc.timestamp
            else int(datetime.now(timezone.utc).timestamp() * 1000),
        }

    def _store_doc_to_memory_doc(
        self, store_doc: SearchDocsItem
    ) -> Tuple[MemoryDoc, float]:
        """将向量存储结果转换为MemoryDoc并提取分数"""
        data = store_doc.data
        memory_doc = MemoryDoc(
            id=store_doc.id,
            text=data.get("content", ""),
            type=data.get("memory_type", ""),
            timestamp=datetime.fromtimestamp(data.get("last_updated") / 1000.0)
            if data.get("last_updated")
            else None,
        )
        score = store_doc.score
        return memory_doc, score

    async def add_memories(
        self, user_id: str, scope_id: str, memories: list[MemoryDoc]
    ):
        """添加记忆文档"""

        if not self._dependency_service:
            return

        if not memories:
            return

        await self._ensure_initialized()

        # 检查记忆的数量是否达到上限
        try:
            current_count = await self._get_memory_count(user_id, scope_id)

            if (current_count > USER_MAX_MEMORY_COUNT) or current_count == -1:
                # 记忆数量超限，暂时只记录日志，不抛出异常，避免影响其他的业务流程
                logger.warning(
                    f"Memory count {current_count} exceeds limit {USER_MAX_MEMORY_COUNT} for user_id={user_id}, scope_id={scope_id}"
                )
                return
        except Exception as e:
            # 如果检查数量失败，我们仍然允许添加，但在后期计数恢复后会限制
            logger.warning(
                f"Failed to check memory count before adding memories for user_id={user_id}, scope_id={scope_id}: {e}"
            )

        try:
            # 对每个记忆文档进行向量化
            docs_to_store = []
            for memory_doc in memories:
                if not memory_doc.text:
                    logger.warning(
                        f"Skipping memory with empty text for user_id={user_id}, scope_id={scope_id}"
                    )
                    continue

                # 对记忆文本进行向量化
                embedding = await self._embedding_model.embed_query(memory_doc.text)

                # 转换为向量存储格式
                store_doc = self._memory_doc_to_store_doc(
                    memory_doc, user_id, scope_id, embedding
                )
                docs_to_store.append(store_doc)

            # 批量插入向量存储
            if docs_to_store:
                await self._vector_store.add_docs(self._index_name, docs_to_store)
                logger.info(
                    f"Successfully added {len(docs_to_store)} memories to index '{self._index_name}'"
                )

                # 添加成功后，如果之前缓存了数量，更新缓存
                cache_key = self._get_cache_key(user_id, scope_id)
                if cache_key in self._memory_count_cache:
                    # 如果之前缓存了有效的数量，增加计数
                    if self._memory_count_cache[cache_key] >= 0:
                        self._update_cache(
                            cache_key,
                            self._memory_count_cache[cache_key] + len(docs_to_store),
                        )
                    else:
                        # 否则删除缓存，让下次查询重新获取
                        self._memory_count_cache.pop(cache_key, None)
                        self._last_cache_time.pop(cache_key, None)
                else:
                    # 缓存不存在，也主动清理一次
                    self._cleanup_expired_cache()
        except MemoryServiceException as e:
            raise e
        except Exception as e:
            logger.error(f"Failed to add memories: {e}", exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_SUGGESTION
                ),
            )

    async def delete_memories(self, user_id: str, scope_id: str, ids: list[str]):
        """删除指定的记忆文档"""
        if not self._dependency_service:
            return

        await self._ensure_initialized()

        try:
            # 删除符合过滤条件的文档
            await self._vector_store.delete_docs_by_ids(self._index_name, ids)
            logger.info(
                f"Successfully deleted memories for user_id={user_id}, scope_id={scope_id}, ids={ids}"
            )

            # 更新缓存：删除成功后，从缓存中减去删除的记忆数量
            cache_key = self._get_cache_key(user_id, scope_id)
            if (
                cache_key in self._memory_count_cache
                and self._memory_count_cache[cache_key] >= 0
            ):
                # 直接从缓存数量减去删除的记录数
                deleted_count = len(ids)
                new_count = max(0, self._memory_count_cache[cache_key] - deleted_count)
                self._update_cache(cache_key, new_count)
                logger.info(
                    f"Updated cache for {cache_key}: {self._memory_count_cache[cache_key]} -> {new_count}"
                )

        except MemoryServiceException as e:
            raise e
        except Exception as e:
            logger.error(f"Failed to delete memories: {e}", exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_SUGGESTION
                ),
            )

    async def delete_by_user(self, user_id: str):
        """删除指定用户的所有记忆"""
        if not self._dependency_service:
            return

        await self._ensure_initialized()

        try:
            filters = {"user_id": user_id}
            await self._vector_store.delete_docs_by_filters(self._index_name, filters)
            logger.info(f"Successfully deleted all memories for user_id={user_id}")

        except MemoryServiceException as e:
            raise e
        except Exception as e:
            logger.error(f"Failed to delete memories by user: {e}", exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_SUGGESTION
                ),
            )

    async def delete_by_scope(self, scope_id: str):
        """删除指定范围的所有记忆"""
        if not self._dependency_service:
            return

        await self._ensure_initialized()

        try:
            filters = {"app_id": scope_id}
            await self._vector_store.delete_docs_by_filters(self._index_name, filters)
            logger.info(f"Successfully deleted all memories for scope_id={scope_id}")

        except MemoryServiceException as e:
            raise e
        except Exception as e:
            logger.error(f"Failed to delete memories by scope: {e}", exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_SUGGESTION
                ),
            )

    async def delete_by_user_and_scope(self, user_id: str, scope_id: str):
        """删除指定用户和范围的所有记忆"""
        if not self._dependency_service:
            return

        await self._ensure_initialized()

        try:
            filters = self._build_filter(user_id, scope_id)
            await self._vector_store.delete_docs_by_filters(self._index_name, filters)
            logger.info(
                f"Successfully deleted memories for user_id={user_id}, scope_id={scope_id}"
            )

            # 更新缓存：删除所有记忆后，将缓存数量设置为0
            cache_key = self._get_cache_key(user_id, scope_id)
            self._update_cache(cache_key, 0)
            logger.info(f"Updated cache for {cache_key}: set to 0")

        except MemoryServiceException as e:
            raise e
        except Exception as e:
            logger.error(
                f"Failed to delete memories by user and scope: {e}", exc_info=True
            )
            raise MemoryServiceException(
                error_code=ErrorCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_SUGGESTION
                ),
            )

    async def search(
        self,
        user_id: str,
        scope_id: str,
        query: str,
        mem_type: str | None = None,
        top_k: int = 10,
    ) -> list[tuple[MemoryDoc, float]]:
        """搜索记忆文档"""
        if not self._dependency_service:
            return []

        await self._ensure_initialized()

        try:
            # 对查询文本进行向量化
            query_embedding = await self._embedding_model.embed_query(query)

            # 构建过滤条件
            filters = self._build_filter(user_id, scope_id, mem_type)

            # 执行向量搜索
            search_results = await self._vector_store.search(
                collection_name=self._index_name,
                query_vector=query_embedding,
                vector_field="content_vector",
                top_k=top_k,
                filters=filters,
            )

            # 转换结果格式
            memory_results = []
            for result in search_results:
                memory_doc, score = self._store_doc_to_memory_doc(result)
                memory_results.append((memory_doc, score))

            logger.info(
                f"Search completed, found {len(memory_results)} results for query: {query}"
            )
            return memory_results

        except MemoryServiceException as e:
            raise e
        except Exception as e:
            logger.error(f"Failed to search memories: {e}", exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.LONG_TERM_MEMORY_SEARCH_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_SUGGESTION
                ),
            )

    async def get_by_id(
        self, mem_id: str, user_id: str, scope_id: str
    ) -> MemoryDoc | None:
        """根据ID获取特定的记忆文档"""
        if not self._dependency_service:
            return None

        await self._ensure_initialized()

        try:
            # 构建精确查询的过滤条件
            filters = self._build_filter(user_id, scope_id)

            # 查询符合条件的数据（限制为1条）
            query_result = await self._vector_store.list_docs(
                self._index_name, filters=filters, offset=0, limit=1, ids=[mem_id]
            )

            if not query_result.data:
                return None

            # 转换为MemoryDoc格式
            store_doc = query_result.data[0]
            memory_doc = MemoryDoc(
                id=store_doc.id,
                text=store_doc.data.get("content", ""),
                type=store_doc.data.get("memory_type", ""),
                timestamp=datetime.fromtimestamp(
                    store_doc.data.get("last_updated") / 1000.0
                )
                if store_doc.data.get("last_updated")
                else None,
            )

            return memory_doc

        except MemoryServiceException as e:
            raise e
        except Exception as e:
            logger.error(f"Failed to get memory by ID: {e}", exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.LONG_TERM_MEMORY_GET_BY_ID_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_GET_BY_ID_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_GET_BY_ID_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_GET_BY_ID_FAILED_SUGGESTION
                ),
            )

    async def list_memories(
        self, user_id: str, scope_id: str, offset: int, limit: int
    ) -> list[MemoryDoc]:
        """获取指定用户和范围的记忆文档列表（支持分页）"""
        if not self._dependency_service:
            return []

        await self._ensure_initialized()

        try:
            # 构建过滤条件
            filters = self._build_filter(user_id, scope_id)

            # 查询符合条件的数据
            query_result = await self._vector_store.list_docs(
                self._index_name,
                filters=filters,
                limit=limit,
                offset=offset,
                order_field="last_updated",
                order_type="desc",
            )

            # 转换为MemoryDoc列表
            memories = []
            for store_doc in query_result.data:
                memory_doc = MemoryDoc(
                    id=store_doc.id,
                    text=store_doc.data.get("content", ""),
                    type=store_doc.data.get("memory_type", ""),
                    timestamp=datetime.fromtimestamp(
                        store_doc.data.get("last_updated") / 1000.0
                    )
                    if store_doc.data.get("last_updated")
                    else None,
                )
                memories.append(memory_doc)

            logger.info(
                f"Listed {len(memories)} memories for user_id={user_id}, scope_id={scope_id}"
            )
            return memories

        except MemoryServiceException as e:
            raise e
        except Exception as e:
            logger.error(f"Failed to list memories: {e}", exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.LONG_TERM_MEMORY_SEARCH_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_SUGGESTION
                ),
            )
