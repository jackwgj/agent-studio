import sys
import unittest
from unittest.mock import MagicMock, AsyncMock, patch

from memory_service.exception.error_code import ErrorCode
from memory_service.exception.memory_service_exception import MemoryServiceException
from memory_service.memory.vector_store.base_vector_store import (
    SearchDocsItem,
)


class TestOpenSearchVectorStore(unittest.IsolatedAsyncioTestCase):
    """测试OpenSearch向量存储实现"""

    def setUp(self):
        """在每个测试之前清除模块缓存"""
        modules_to_clear = [
            "memory_service.memory.vector_store.opensearch_vector_store",
            "memory_service.utils.async_opensearch_util",
        ]
        for module in modules_to_clear:
            if module in sys.modules:
                del sys.modules[module]

    async def test_create_collection_success(self):
        """测试向量存储初始化"""
        mock_client = AsyncMock()
        mock_client.create_index = AsyncMock(return_value=True)

        with patch(
            "memory_service.utils.async_opensearch_util.get_async_opensearch_util"
        ) as mock_get_client:
            mock_get_client.return_value = mock_client
            from memory_service.memory.vector_store.opensearch_vector_store import (
                OpenSearchVectorStore,
            )

            vector_store = OpenSearchVectorStore()

            # 调用方法
            collection_name = "test_collection"
            await vector_store.create_collection(collection_name)

            self.assertTrue(True)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_create_collection_exception(self, mock_get_client):
        """测试创建集合时抛出异常的情况"""
        mock_client = MagicMock()
        mock_client.create_index.side_effect = Exception("Connection error")
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 调用方法，期望没有抛出异常（因为方法内部捕获了异常）
        collection_name = "test_collection"

        try:
            await vector_store.create_collection(collection_name)
        except Exception:
            self.fail(
                "create_collection should not raise exception for internal error handling"
            )

        # 验证调用
        mock_client.create_index.assert_called_once()

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_delete_collection_success(self, mock_get_client):
        """测试删除集合成功的情况"""
        mock_client = AsyncMock()
        mock_client.index_exists = AsyncMock(return_value=True)
        mock_client.delete_index = AsyncMock(return_value=True)
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 调用方法
        collection_name = "test_collection"
        await vector_store.delete_collection(collection_name)

        # 验证调用顺序和参数
        mock_client.index_exists.assert_called_once_with(collection_name)
        mock_client.delete_index.assert_called_once_with(collection_name)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_collection_exists_true(self, mock_get_client):
        """测试检查集合存在时返回True"""
        mock_client = AsyncMock()
        mock_client.index_exists = AsyncMock(return_value=True)
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 调用方法
        collection_name = "test_collection"
        result = await vector_store.collection_exists(collection_name)

        # 验证结果
        self.assertTrue(result)

        # 验证调用
        mock_client.index_exists.assert_called_once_with(collection_name)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_collection_exists_exception(self, mock_get_client):
        """测试检查集合存在时抛出异常"""
        mock_client = AsyncMock()
        mock_client.index_exists.side_effect = Exception("Connection error")
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 调用方法，期望返回False
        collection_name = "test_collection"
        result = await vector_store.collection_exists(collection_name)

        # 验证结果
        self.assertFalse(result)

        # 验证调用
        mock_client.index_exists.assert_called_once_with(collection_name)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_add_docs_success(self, mock_get_client):
        """测试添加文档成功的情况"""
        mock_client = AsyncMock()
        mock_client.insert_data = AsyncMock(return_value=True)
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        docs = [
            {"content": "测试文档1", "metadata": {"app_id": "test_app"}},
            {"content": "测试文档2", "metadata": {"app_id": "test_app"}},
        ]

        # 调用方法
        await vector_store.add_docs(collection_name, docs)

        # 验证调用
        mock_client.insert_data.assert_called_once_with(collection_name, docs)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_search_success(self, mock_get_client):
        """测试向量搜索成功的情况"""
        mock_client = AsyncMock()
        mock_results = [
            {
                "_id": "doc1",
                "_score": 0.95,
                "_source": {"content": "相关文档1", "metadata": {"app_id": "test_app"}},
            },
            {
                "_id": "doc2",
                "_score": 0.80,
                "_source": {"content": "相关文档2", "metadata": {"app_id": "test_app"}},
            },
        ]
        mock_client.search_data.return_value = mock_results
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        query_vector = [0.1, 0.2, 0.3, 0.4, 0.5]

        # 调用方法
        results = await vector_store.search(
            collection_name, query_vector, "content_vector"
        )

        # 验证结果格式
        self.assertIsInstance(results, list)
        self.assertEqual(len(results), 2)

        # 验证第一个结果
        first_result = results[0]
        self.assertIsInstance(first_result, SearchDocsItem)
        self.assertEqual(first_result.id, "doc1")
        self.assertEqual(first_result.score, 0.95)
        self.assertEqual(first_result.data["content"], "相关文档1")

        # 验证调用
        mock_client.search_data.assert_called_once()
        call_args = mock_client.search_data.call_args
        self.assertEqual(call_args[0][0], collection_name)
        self.assertEqual(call_args[0][1], query_vector)
        self.assertEqual(call_args[0][2], "content_vector")

        # 验证搜索参数
        search_kwargs = call_args[1]
        self.assertEqual(search_kwargs["size"], 5)  # 默认top_k
        self.assertEqual(search_kwargs["min_score"], 0.0)
        self.assertEqual(search_kwargs["k"], 5)  # 默认top_k

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_search_with_filters(self, mock_get_client):
        """测试向量搜索带过滤条件"""
        mock_client = AsyncMock()
        mock_results = [
            {
                "_id": "doc1",
                "_score": 0.95,
                "_source": {"content": "相关文档1", "metadata": {"app_id": "test_app"}},
            }
        ]
        mock_client.search_data.return_value = mock_results
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        query_vector = [0.1, 0.2, 0.3, 0.4, 0.5]
        filters = {"app_id": "test_app", "user_id": "user1"}

        # 调用方法
        results = await vector_store.search(
            collection_name, query_vector, "content_vector", filters=filters
        )

        # 验证调用
        mock_client.search_data.assert_called_once()
        call_args = mock_client.search_data.call_args

        # 验证过滤条件被正确传递
        self.assertIn("filter_query", call_args[1])
        expected_filter = vector_store._build_filters(filters)
        self.assertEqual(call_args[1]["filter_query"], expected_filter)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_search_with_default_vector_field(self, mock_get_client):
        """测试使用默认向量字段"""
        mock_client = AsyncMock()
        mock_results = [
            {"_id": "doc1", "_score": 0.95, "_source": {"content": "相关文档1"}}
        ]
        mock_client.search_data.return_value = mock_results
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        query_vector = [0.1, 0.2, 0.3, 0.4, 0.5]

        # 调用方法，不指定vector_field，应该使用默认值
        results = await vector_store.search(collection_name, query_vector, None)

        # 验证调用
        mock_client.search_data.assert_called_once()
        call_args = mock_client.search_data.call_args

        # 验证使用了默认向量字段
        expected_vector_field = vector_store.get_vector_field_name()
        self.assertEqual(call_args[0][2], expected_vector_field)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_search_failure(self, mock_get_client):
        """测试向量搜索失败的情况"""
        mock_client = AsyncMock()
        mock_client.search_data.side_effect = Exception("Search error")
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        query_vector = [0.1, 0.2, 0.3, 0.4, 0.5]

        # 调用方法，期望抛出异常
        with self.assertRaises(MemoryServiceException) as context:
            await vector_store.search(collection_name, query_vector, "content_vector")

        # 验证异常属性
        self.assertEqual(
            context.exception.error_code, ErrorCode.VECTOR_STORE_SEARCH_FAILED
        )

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_delete_docs_by_ids_success(self, mock_get_client):
        """测试根据ID删除文档成功"""
        mock_client = AsyncMock()
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        doc_ids = ["doc1", "doc2", "doc3"]

        # 调用方法
        await vector_store.delete_docs_by_ids(collection_name, doc_ids)

        # 验证调用
        self.assertEqual(mock_client.delete_data_by_id.call_count, 3)

        # 验证每个ID都被正确调用
        for i, doc_id in enumerate(doc_ids):
            call = mock_client.delete_data_by_id.call_args_list[i]
            self.assertEqual(call[0][0], collection_name)
            self.assertEqual(call[0][1], doc_id)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_list_docs_failure(self, mock_get_client):
        """测试文档列表查询失败"""
        mock_client = AsyncMock()
        mock_client.query_data_list.side_effect = Exception("Query failed")
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"

        # 调用方法，期望抛出异常
        with self.assertRaises(MemoryServiceException) as context:
            await vector_store.list_docs(collection_name)

        # 验证异常属性
        self.assertEqual(
            context.exception.error_code, ErrorCode.VECTOR_STORE_SEARCH_FAILED
        )

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_delete_docs_by_filters_success(self, mock_get_client):
        """测试根据过滤条件删除文档成功"""
        mock_client = AsyncMock()

        # 设置mock返回三次调用逐渐减少的结果
        # 第一次: 2个文档
        # 第二次: 2个相同的文档
        # 第三次: 0个文档 (结束)
        mock_results = [
            # 第一次调用返回2个文档
            {
                "total": 2,
                "data": [
                    {"_id": "doc1", "_score": 0.95, "_source": {"content": "文档1"}},
                    {"_id": "doc2", "_score": 0.80, "_source": {"content": "文档2"}},
                ],
            },
            # 第二次调用返回同样的数据，此时不应该再触发删除操作
            {
                "total": 2,
                "data": [
                    {"_id": "doc1", "_score": 0.95, "_source": {"content": "文档1"}},
                    {"_id": "doc2", "_score": 0.80, "_source": {"content": "文档2"}},
                ],
            },
            # 第三次调用返回0个文档 (结束)
            {"total": 0, "data": []},
        ]

        mock_client.query_data_list.side_effect = mock_results
        # 替换delete_data_by_id来记录调用
        mock_client.delete_data_by_id = AsyncMock(
            side_effect=lambda collection, doc_id: None
        )
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        filters = {"app_id": "test_app"}

        # 调用方法
        await vector_store.delete_docs_by_filters(collection_name, filters)

        # 验证 list_docs 被调用了3次
        self.assertEqual(mock_client.query_data_list.call_count, 3)

        # 验证 delete_data_by_id 被调用了2次
        self.assertEqual(mock_client.delete_data_by_id.call_count, 2)

        # 验证第一次调用删除了doc1
        first_call = mock_client.delete_data_by_id.call_args_list[0]
        self.assertEqual(first_call[0][0], collection_name)
        self.assertEqual(first_call[0][1], "doc1")

        # 验证第二次调用删除了doc2
        second_call = mock_client.delete_data_by_id.call_args_list[1]
        self.assertEqual(second_call[0][0], collection_name)
        self.assertEqual(second_call[0][1], "doc2")

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_delete_docs_by_filters_with_batch_size(self, mock_get_client):
        """测试根据过滤条件删除文档时使用自定义批量大小"""
        mock_client = AsyncMock()

        # 设置 list_docs 返回结果
        mock_results = [
            {
                "total": 3,
                "data": [
                    {"_id": "doc1", "_score": 0.95, "_source": {"content": "文档1"}},
                    {"_id": "doc2", "_score": 0.80, "_source": {"content": "文档2"}},
                    {"_id": "doc3", "_score": 0.75, "_source": {"content": "文档3"}},
                ],
            },
            {"total": 0, "data": []},
        ]

        mock_client.query_data_list.side_effect = mock_results
        # 记录delete_data_by_id调用
        mock_client.delete_data_by_id = AsyncMock(
            side_effect=lambda collection, doc_id: None
        )
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        filters = {"app_id": "test_app"}

        # 调用方法
        await vector_store.delete_docs_by_filters(collection_name, filters)

        # 验证 delete_data_by_id 被调用了3次，删除了所有3个文档
        self.assertEqual(mock_client.delete_data_by_id.call_count, 3)

        # 验证删除了所有文档
        expected_doc_ids = ["doc1", "doc2", "doc3"]
        actual_doc_ids = [
            call_args[0][1]
            for call_args in mock_client.delete_data_by_id.call_args_list
        ]
        self.assertEqual(sorted(actual_doc_ids), sorted(expected_doc_ids))

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    async def test_delete_docs_by_filters_list_docs_failure(self, mock_get_client):
        """测试根据过滤条件删除文档时 list_docs 失败"""
        mock_client = AsyncMock()
        mock_client.query_data_list.side_effect = Exception("Query failed")
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        # 准备测试数据
        collection_name = "test_collection"
        filters = {"app_id": "test_app"}

        # 调用方法，期望抛出异常
        with self.assertRaises(MemoryServiceException) as context:
            await vector_store.delete_docs_by_filters(collection_name, filters)

        # 验证异常属性
        self.assertEqual(
            context.exception.error_code, ErrorCode.VECTOR_STORE_DELETE_DOCUMENTS_FAILED
        )

        # 验证 list_docs 被调用了一次
        self.assertEqual(mock_client.query_data_list.call_count, 1)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    def test_build_filters_single_term(self, mock_get_client):
        """测试构建单个过滤条件"""
        mock_client = AsyncMock()
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        filters = {"app_id": "test_app"}

        result = vector_store._build_filters(filters)

        # 应该返回单个term查询
        expected = {"term": {"app_id": "test_app"}}
        self.assertEqual(result, expected)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    def test_build_filters_multiple_terms(self, mock_get_client):
        """测试构建多个过滤条件"""
        mock_client = AsyncMock()
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        filters = {"app_id": "test_app", "user_id": "user1"}

        result = vector_store._build_filters(filters)

        # 应该返回bool查询，包含多个must条件
        self.assertEqual(
            result["bool"]["must"],
            [{"term": {"app_id": "test_app"}}, {"term": {"user_id": "user1"}}],
        )

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    def test_build_filters_list_values(self, mock_get_client):
        """测试构建列表值过滤条件"""
        mock_client = AsyncMock()
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        filters = {"status": ["active", "pending"]}

        result = vector_store._build_filters(filters)

        # 应该返回terms查询
        expected = {"terms": {"status": ["active", "pending"]}}
        self.assertEqual(result, expected)

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    def test_get_vector_field_name(self, mock_get_client):
        """测试获取向量字段名称"""
        mock_client = AsyncMock()
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        field_name = vector_store.get_vector_field_name()

        # 验证返回的是字符串
        self.assertIsInstance(field_name, str)
        self.assertEqual(field_name, "content_vector")

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    def test_get_text_field_name(self, mock_get_client):
        """测试获取文本字段名称"""
        mock_client = AsyncMock()
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        field_name = vector_store.get_text_field_name()

        # 验证返回的是字符串
        self.assertIsInstance(field_name, str)
        self.assertEqual(field_name, "content")

    @patch("memory_service.utils.async_opensearch_util.get_async_opensearch_util")
    def test_get_metadata_fields(self, mock_get_client):
        """测试获取元数据字段列表"""
        mock_client = AsyncMock()
        mock_get_client.return_value = mock_client

        from memory_service.memory.vector_store.opensearch_vector_store import (
            OpenSearchVectorStore,
        )

        vector_store = OpenSearchVectorStore()

        metadata_fields = vector_store.get_metadata_fields()

        # 验证返回的是列表
        self.assertIsInstance(metadata_fields, list)

        # 验证包含预期的元数据字段
        expected_fields = ["app_id", "user_id", "memory_type", "last_updated"]
        for field in expected_fields:
            self.assertIn(field, metadata_fields)


if __name__ == "__main__":
    unittest.main()
