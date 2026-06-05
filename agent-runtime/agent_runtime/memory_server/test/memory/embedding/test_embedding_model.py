import unittest
from unittest.mock import MagicMock, AsyncMock, patch

from memory_service.exception.error_code import ErrorCode
from memory_service.exception.memory_service_exception import MemoryServiceException
from memory_service.memory.embedding.embedding_model import (
    initialize_embedding,
    get_embedding_instance,
)


class TestEmbeddingModelService(unittest.IsolatedAsyncioTestCase):
    def setUp(self):
        """设置测试环境"""
        self.endpoint = "http://example.com"
        self.api_key = "test_key"
        self.model_name = "test_model"

    def tearDown(self):
        """清理测试环境"""
        if hasattr(self, "patcher"):
            self.patcher.stop()
        if hasattr(self, "patcher2"):
            self.patcher2.stop()

    def _create_mock_response(self, response_data):
        """创建模拟的HTTP响应"""
        mock_response = MagicMock()
        mock_response.status = 200
        mock_response.raise_for_status = AsyncMock()
        mock_response.json = AsyncMock(return_value=response_data)

        # 创建模拟的异步上下文管理器
        mock_context_manager = MagicMock()
        mock_context_manager.__aenter__ = AsyncMock(return_value=mock_response)
        mock_context_manager.__aexit__ = AsyncMock(return_value=None)

        return mock_response, mock_context_manager

    def _setup_mock_session(self, response_data):
        """设置模拟的aiohttp会话"""
        mock_response, mock_context_manager = self._create_mock_response(response_data)

        # 开始patch并保存，以便在测试中使用
        # 需要patch EmbeddingModelService内部创建的aiohttp.ClientSession
        self.patcher = patch(
            "memory_service.memory.embedding.embedding_model.EmbeddingModelService._initialize_session"
        )
        mock_initialize_session = self.patcher.start()

        # 替换_make_request方法以返回模拟数据
        async def mock_make_request(method, url, data=None):
            mock_make_request.has_been_called = True
            mock_make_request.last_call_args = (method, url, data)
            return response_data

        mock_make_request.has_been_called = False
        mock_make_request.last_call_args = None

        # 替换实际的_make_request方法
        self.patcher2 = patch(
            "memory_service.memory.embedding.embedding_model.EmbeddingModelService._make_request",
            side_effect=mock_make_request,
        )
        self.mock_make_request = self.patcher2.start()

        return mock_make_request

    def _initialize_embedding(self, dimension=5):
        """初始化嵌入服务"""
        initialize_embedding(
            endpoint=self.endpoint,
            api_key=self.api_key,
            model_name=self.model_name,
            dimension=dimension,
        )

    async def test_embed_documents(self):
        """测试embed_documents方法正常情况"""
        response_data = {"data": [{"embedding": [0.5, 0.4, 0.3, 0.2, 0.1]}]}

        mock_make_request = self._setup_mock_session(response_data)
        self._initialize_embedding(dimension=5)

        test_instance = get_embedding_instance()

        # 调用方法
        result = await test_instance.embed_documents(texts=["test"])

        # 验证
        self.assertTrue(mock_make_request.has_been_called)
        self.assertEqual(
            mock_make_request.last_call_args,
            (
                "POST",
                "http://example.com/embeddings",
                {"model": "test_model", "input": ["test"]},
            ),
        )
        self.assertEqual(len(result), 1)
        self.assertEqual(len(result[0]), 5)

    async def test_embed_documents_throw_exception_with_dimension_not_match(self):
        """测试embed_documents方法维度不匹配时抛出异常"""
        response_data = {"data": [{"embedding": [0.1, 0.2, 0.3, 0.4, 0.5]}]}

        self._initialize_embedding(
            dimension=10
        )  # 设置维度为10，但返回的embedding只有5维

        mock_make_request = self._setup_mock_session(response_data)
        test_instance = get_embedding_instance()

        # 调用方法，期望抛出MemoryServiceException异常
        with self.assertRaises(MemoryServiceException) as context:
            await test_instance.embed_documents(texts=["test"])

        # 验证异常类型
        self.assertIsInstance(context.exception, MemoryServiceException)

        # 验证异常的错误码是否为期待的值 EMBEDDING_DIMENSION_MISMATCH (1010)
        expected_error_code = ErrorCode.EMBEDDING_DIMENSION_MISMATCH
        self.assertEqual(context.exception.error_code, expected_error_code)

    async def test_embed_documents_throw_exception_with_embedding_return_null(self):
        """测试embed_documents方法向量化时返回为None"""
        response_data = {"data": [{"embedding": None}]}
        self._initialize_embedding(dimension=10)
        mock_make_request = self._setup_mock_session(response_data)

        test_instance = get_embedding_instance()

        # 调用方法，期望抛出MemoryServiceException异常
        with self.assertRaises(MemoryServiceException) as context:
            await test_instance.embed_documents(texts=["test"])

        # 验证异常类型
        self.assertIsInstance(context.exception, MemoryServiceException)

        # 验证异常的错误码是否为期待的值 EMBEDDING_MODEL_RETURN_ERROR_DATA
        expected_error_code = ErrorCode.EMBEDDING_MODEL_RETURN_ERROR_DATA
        self.assertEqual(context.exception.error_code, expected_error_code)

    async def test_embed_query(self):
        """测试embed_query方法正常情况"""
        response_data = {"data": [{"embedding": [0.1, 0.2, 0.3, 0.4, 0.5]}]}

        mock_make_request = self._setup_mock_session(response_data)
        self._initialize_embedding(dimension=5)

        test_instance = get_embedding_instance()

        # 调用方法
        result = await test_instance.embed_query(text="test")

        # 验证
        self.assertTrue(mock_make_request.has_been_called)
        self.assertEqual(
            mock_make_request.last_call_args,
            (
                "POST",
                "http://example.com/embeddings",
                {"model": "test_model", "input": "test"},
            ),
        )
        self.assertEqual(len(result), 5)

    async def test_embed_query_throw_exception_with_dimension_not_match(self):
        """测试embed_query方法维度不匹配时抛出异常"""
        response_data = {"data": [{"embedding": [0.1, 0.2, 0.3, 0.4, 0.5]}]}

        mock_make_request = self._setup_mock_session(response_data)
        self._initialize_embedding(
            dimension=10
        )  # 设置维度为10，但返回的embedding只有5维

        test_instance = get_embedding_instance()

        # 调用方法，期望抛出MemoryServiceException异常
        with self.assertRaises(MemoryServiceException) as context:
            await test_instance.embed_query(text="test")

        # 验证异常类型
        self.assertIsInstance(context.exception, MemoryServiceException)

        # 验证异常的错误码是否为期待的值 EMBEDDING_DIMENSION_MISMATCH (1010)
        expected_error_code = ErrorCode.EMBEDDING_DIMENSION_MISMATCH
        self.assertEqual(context.exception.error_code, expected_error_code)

    async def test_embed_query_throw_exception_with_embedding_return_null(self):
        """测试embed_query方法向量化时返回为None"""
        response_data = {"data": [{"embedding": None}]}

        mock_make_request = self._setup_mock_session(response_data)
        self._initialize_embedding(dimension=10)

        test_instance = get_embedding_instance()

        # 调用方法，期望抛出MemoryServiceException异常
        with self.assertRaises(MemoryServiceException) as context:
            await test_instance.embed_query(text="test")

        # 验证异常类型
        self.assertIsInstance(context.exception, MemoryServiceException)

        # 验证异常的错误码是否为期待的值 EMBEDDING_MODEL_RETURN_ERROR_DATA
        expected_error_code = ErrorCode.EMBEDDING_MODEL_RETURN_ERROR_DATA
        self.assertEqual(context.exception.error_code, expected_error_code)
