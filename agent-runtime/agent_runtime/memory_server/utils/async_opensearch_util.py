"""
AsyncOpenSearchUtil - 异步 OpenSearch 向量库对接客户端

支持单例模式、环境变量配置、索引管理、数据 CRUD 操作和向量检索功能
"""

import logging
from typing import Optional, Dict, List, Any, Union

from memory_service.config.settings import (
    OPENSEARCH_HOST,
    OPENSEARCH_PORT,
    OPENSEARCH_SSL_ENABLE,
    OPENSEARCH_USER,
    OPENSEARCH_PASSWORD,
    OPENSEARCH_HTTP_COMPRESS,
    OPENSEARCH_MAX_RETRIES,
    OPENSEARCH_RETRY_ON_TIMEOUT,
    OPENSEARCH_TIMEOUT,
)
from opensearchpy import AsyncOpenSearch

logger = logging.getLogger(__name__)

# 全局单例变量，一个进程只允许初始化一个 OpenSearch 客户端
_opensearch_client_instance: Optional["AsyncOpenSearchUtil"] = None


class AsyncOpenSearchUtil:
    """
    异步 OpenSearch 向量库对接客户端，支持单例模式
    """

    def __init__(self):
        """初始化 AsyncOpenSearchUtil 实例"""
        self._client: Optional[AsyncOpenSearch] = None
        self._initialized = False

    def initialize(self):
        """
        初始化 OpenSearch 客户端连接
        使用环境变量配置连接参数
        """
        if self._initialized:
            logger.warning("AsyncOpenSearchUtil already ignored，skip it")
            return

        try:
            # 构建连接配置
            hosts = [{"host": OPENSEARCH_HOST, "port": OPENSEARCH_PORT}]

            # 基础认证配置
            http_auth = None
            if OPENSEARCH_USER and OPENSEARCH_PASSWORD:
                from memory_service.scc.crypto_utils import get_crypto_util

                opensearch_pwd_raw = get_crypto_util().decrypt(OPENSEARCH_PASSWORD)
                http_auth = (OPENSEARCH_USER, opensearch_pwd_raw)

            # 创建异步 OpenSearch 客户端
            self._client = AsyncOpenSearch(
                hosts=hosts,
                http_auth=http_auth,
                use_ssl=OPENSEARCH_SSL_ENABLE,
                verify_certs=False,
                ssl_show_warn=False,
                http_compress=OPENSEARCH_HTTP_COMPRESS,
                max_retries=OPENSEARCH_MAX_RETRIES,
                retry_on_timeout=OPENSEARCH_RETRY_ON_TIMEOUT,
                timeout=OPENSEARCH_TIMEOUT,
            )

            self._initialized = True
            logger.info(
                f"AsyncOpenSearchUtil initial success: {OPENSEARCH_HOST}:{OPENSEARCH_PORT}"
            )

        except Exception as e:
            logger.error(f"AsyncOpenSearchUtil initial failed: {e}", exc_info=True)
            raise

    async def _get_client(self) -> AsyncOpenSearch:
        """
        获取 OpenSearch 客户端实例

        Returns:
            AsyncOpenSearch: OpenSearch 异步客户端

        Raises:
            RuntimeError: 如果客户端未初始化
        """
        if not self._initialized or self._client is None:
            logger.error(
                "AsyncOpenSearchUtil not initialized, please call initialize()"
            )
            raise RuntimeError("AsyncOpenSearchUtil not initialized")

        return self._client

    async def create_index(
        self, index_name: str, index_mapping: Dict[str, Any]
    ) -> bool:
        """
        创建索引，如果索引已存在则不创建

        Args:
            index_name: 索引名称
            index_mapping: 索引映射配置（包含 settings 和 mappings）

        Returns:
            bool: 创建成功返回 True，索引已存在或创建失败返回 False
        """
        client = await self._get_client()

        try:
            # 检查索引是否已存在
            exists = await client.indices.exists(index=index_name)
            if exists:
                logger.info(f"Index {index_name} exists, skip it.")
                return True

            # 创建索引
            await client.indices.create(index=index_name, body=index_mapping)
            logger.info(f"Create index {index_name} success")
            return True

        except Exception as e:
            logger.error(f"Create index {index_name} failed: {e}", exc_info=True)
            # 不抛出异常，只记录错误日志
            return False

    async def insert_data(
        self, index_name: str, data: Union[Dict[str, Any], List[Dict[str, Any]]]
    ) -> bool:
        """
        写入数据到指定索引

        Args:
            index_name: 索引名称
            data: 单条数据或数据列表（字典格式）

        Returns:
            bool: 写入成功返回 True，失败返回 False
        """
        client = await self._get_client()

        try:
            # 如果是单条数据，转换为列表
            if isinstance(data, dict):
                data_list = [data]
            else:
                data_list = data

            # 批量写入数据
            success_count = 0
            for item in data_list:
                try:
                    # 使用 index 方法写入数据，自动生成 doc_id
                    await client.index(index=index_name, body=item)
                    success_count += 1
                except Exception as e:
                    logger.error(
                        f"Write data into index {index_name} failed: {e}", exc_info=True
                    )

            logger.info(
                f"Write {success_count}/{len(data_list)} datas into index {index_name}"
            )
            return success_count == len(data_list)

        except Exception as e:
            logger.error(
                f"Batch write data into index {index_name} failed: {e}", exc_info=True
            )
            raise e

    async def update_data_by_id(
        self, index_name: str, doc_id: str, data: Dict[str, Any]
    ) -> bool:
        """
        根据 ID 更新文档数据

        Args:
            index_name: 索引名称
            doc_id: 文档 ID
            data: 要更新的数据（字典格式）

        Returns:
            bool: 更新成功返回 True，失败返回 False
        """
        client = await self._get_client()

        try:
            # 使用 update 方法更新文档
            await client.update(index=index_name, id=doc_id, body={"doc": data})
            logger.info(f"Update document {doc_id} in index {index_name} success.")
            return True

        except Exception as e:
            logger.error(
                f"Update document {doc_id} failed in index {index_name}: {e}",
                exc_info=True,
            )
            raise e

    async def delete_index(self, index_name: str) -> bool:
        """
        删除整个索引

        Args:
            index_name: 索引名称

        Returns:
            bool: 删除成功返回 True，索引不存在或删除失败返回 False
        """
        client = await self._get_client()

        try:
            # 使用 indices.delete 方法删除索引
            await client.indices.delete(index=index_name)
            logger.info(f"Delete index {index_name} success.")
            return True

        except Exception as e:
            logger.error(f"Delete index {index_name} failed: {e}", exc_info=True)
            raise e

    async def delete_data_by_id(self, index_name: str, doc_id: str) -> bool:
        """
        根据 ID 删除文档

        Args:
            index_name: 索引名称
            doc_id: 文档 ID

        Returns:
            bool: 删除成功返回 True，文档不存在或删除失败返回 False
        """
        client = await self._get_client()

        try:
            # 使用 delete 方法删除文档
            await client.delete(index=index_name, id=doc_id)
            logger.info(f"Delete document {doc_id} in index {index_name} success.")
            return True

        except Exception as e:
            logger.error(
                f"Delete document {doc_id} from index {index_name} failed: {e}",
                exc_info=True,
            )
            raise e

    async def search_data(
        self, index_name: str, query_vector: List[float], vector_field: str, **kwargs
    ) -> List[Dict[str, Any]]:
        """
        向量检索，根据 memory_content_vector 字段进行向量搜索

        Args:
            index_name: 索引名称
            query_vector: 查询向量（列表格式）
            vector_field: 向量字段名，默认 'memory_content_vector'
            **kwargs: 其他可选参数
                - size: 返回结果数量，默认 10
                - min_score: 最小相似度分数，默认 0.0
                - filter_query: 过滤条件，默认 None
                - k: KNN 搜索的 k 值，默认 10

        Returns:
            List[Dict[str, Any]]: 搜索结果列表，每个元素包含文档内容和分数
        """
        client = await self._get_client()

        # 解析可选参数
        size = kwargs.get("size", 10)
        min_score = kwargs.get("min_score", 0.0)
        filter_query = kwargs.get("filter_query", None)
        k = kwargs.get("k", size)

        try:
            # 构建 KNN 查询
            if filter_query:
                # 有过滤条件时，使用bool查询包装knn查询
                knn_query = {
                    "size": size,
                    "min_score": min_score,
                    "query": {
                        "bool": {
                            "must": [
                                {
                                    "knn": {
                                        vector_field: {"vector": query_vector, "k": k}
                                    }
                                }
                            ],
                            "filter": (
                                filter_query
                                if isinstance(filter_query, dict)
                                else [filter_query]
                            ),
                        }
                    },
                }
            else:
                # 无过滤条件时，直接使用knn查询
                knn_query = {
                    "size": size,
                    "min_score": min_score,
                    "query": {"knn": {vector_field: {"vector": query_vector, "k": k}}},
                }

            # 执行搜索
            response = await client.search(index=index_name, body=knn_query)

            # 提取搜索结果
            results = []
            for hit in response["hits"]["hits"]:
                result = {
                    "_id": hit["_id"],
                    "_score": hit["_score"],
                    "_source": hit["_source"],
                }
                results.append(result)

            logger.info(
                f"Vector search success in index {index_name},get {len(results)} result"
            )
            return results

        except Exception as e:
            logger.error(f"Vector search failed {index_name}: {e}", exc_info=True)
            raise e

    async def index_exists(self, index_name: str) -> bool:
        """
        检查索引是否存在

        Args:
            index_name: 索引名称

        Returns:
            bool: 索引存在返回 True，不存在返回 False
        """
        client = await self._get_client()

        try:
            exists = await client.indices.exists(index=index_name)
            return exists
        except Exception as e:
            logger.error(
                f"Check index existence {index_name} failed: {e}", exc_info=True
            )
            return False

    async def query_data_list(
        self,
        index_name: str,
        filters: Optional[Dict[str, Any]] = None,
        offset: int = 0,
        limit: int = 10,
        sort_field: Optional[str] = None,
        sort_order: str = "asc",
    ) -> Dict[str, Any]:
        """
        分页查询数据，支持过滤条件和排序

        Args:
            index_name: 索引名称
            filters: 过滤条件（字典格式），默认 None
            offset: 跳过的记录数（从 0 开始），默认 0
            limit: 返回的最大记录数，默认 10
            sort_field: 排序字段，默认 None
            sort_order: 排序方向，'asc' 或 'desc'，默认 'asc'

        Returns:
            Dict[str, Any]: 包含数据和分页信息的字典
                - data: 数据列表
                - total: 总记录数
                - offset: 当前偏移量
                - limit: 返回记录数限制
        """
        client = await self._get_client()

        try:
            # 构建查询体
            query_body = {"from": offset, "size": limit, "query": {"match_all": {}}}

            # 添加过滤条件
            if filters:
                # 检查是否是 ids 查询
                if isinstance(filters.get("ids"), dict) and "values" in filters["ids"]:
                    # ids 查询直接作为主查询，更高效
                    query_body["query"] = filters
                else:
                    # 其他过滤条件使用 bool 查询
                    query_body["query"] = {"bool": {"filter": []}}

                    for field, value in filters.items():
                        if isinstance(value, list):
                            # 数组值使用 terms 查询
                            if field != "ids":  # 避免重复处理 ids 查询
                                query_body["query"]["bool"]["filter"].append(
                                    {"terms": {field: value}}
                                )
                        elif isinstance(value, dict):
                            # 复杂条件直接添加
                            if field != "ids":  # 避免重复处理 ids 查询
                                query_body["query"]["bool"]["filter"].append(
                                    {field: value}
                                )
                        else:
                            # 单个值使用 term 查询
                            if field != "ids":  # 避免重复处理 ids 查询
                                query_body["query"]["bool"]["filter"].append(
                                    {"term": {field: value}}
                                )

            # 添加排序
            if sort_field:
                query_body["sort"] = [{sort_field: {"order": sort_order.lower()}}]

            # 执行查询
            response = await client.search(index=index_name, body=query_body)

            # 提取结果
            total = (
                response["hits"]["total"]["value"]
                if "total" in response["hits"]
                else len(response["hits"]["hits"])
            )
            results = [hit for hit in response["hits"]["hits"]]

            result = {"data": results, "total": total, "offset": offset, "limit": limit}

            return result

        except Exception as e:
            logger.error(
                f"List document failed from index {index_name}: {e}", exc_info=True
            )
            raise e

    async def close(self):
        """
        关闭 OpenSearch 客户端连接
        """
        try:
            if self._client is not None:
                await self._client.close()
                self._initialized = False
                logger.info("AsyncOpenSearchUtil connection closed")
        except Exception as e:
            logger.error(
                f"Connection in AsyncOpenSearchUtil closed failed: {e}", exc_info=True
            )

    async def test_connection(self) -> bool:
        """
        测试 OpenSearch 连接

        Returns:
            bool: 连接成功返回 True，失败返回 False
        """
        try:
            client = await self._get_client()
            # 使用 ping 方法测试连接
            result = await client.ping()
            if result:
                logger.info("OpenSearch connection test success.")
                return True
            else:
                logger.warning("OpenSearch connection failed：ping return False")
                return False
        except Exception as e:
            logger.error(f"OpenSearch connection failed: {e}", exc_info=True)
            raise e


def get_async_opensearch_util() -> AsyncOpenSearchUtil:
    """
    获取全局唯一的 AsyncOpenSearchUtil 实例（单例模式）

    Returns:
        AsyncOpenSearchUtil: OpenSearch 工具类实例
    """
    global _opensearch_client_instance
    if _opensearch_client_instance is None:
        _opensearch_client_instance = AsyncOpenSearchUtil()
        _opensearch_client_instance.initialize()
    return _opensearch_client_instance
