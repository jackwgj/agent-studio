"""
向量化模型服务实现
"""

import json
import logging
from typing import Any, List, Optional

import aiohttp
from memory_service.exception.error_code import ErrorCode
from memory_service.exception.memory_service_exception import MemoryServiceException
from memory_service.i18n.i18n_constant import I18nMessageCode
from memory_service.i18n.i18n_util import i18n_util
from memory_service.memory.embedding.base_embedding import Embedding
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


class EmbeddingModelService(BaseModel, Embedding):
    """向量化模型服务实现类"""

    _instance = None

    # 配置参数
    endpoint: str = Field(description="向量化模型服务地址")
    api_key: Optional[str] = Field(default="", description="向量化模型API密钥")
    model_name: str = Field(description="向量化模型名称")
    embedding_dimension: int = Field(
        default=1024, description="向量化维度", alias="dimension"
    )
    timeout: int = Field(default=30, description="连接超时时间")
    custom_header: Optional[dict] = Field(default=None, description="自定义请求头")

    # HTTP 客户端（延迟初始化）
    _session: aiohttp.ClientSession = None

    def __init__(self, **data):
        super().__init__(**data)
        # 延迟初始化HTTP客户端，避免在同步上下文中创建
        self._session = None

    async def _initialize_session(self):
        """懒初始化HTTP客户端"""
        if self._session is None:
            # 创建 TCP 连接器，忽略SSL证书验证
            connector = aiohttp.TCPConnector(verify_ssl=False)

            headers = {"Content-Type": "application/json"}
            if self.api_key:
                headers["Authorization"] = f"Bearer {self.api_key}"
            if self.custom_header:
                headers.update(self.custom_header)

            self._session = aiohttp.ClientSession(
                timeout=aiohttp.ClientTimeout(total=self.timeout),
                connector=connector,
                headers=headers,
            )

    async def _ensure_session(self):
        """确保HTTP客户端已初始化"""
        if self._session is None:
            await self._initialize_session()

    async def _close_session(self):
        """关闭 HTTP 客户端"""
        if self._session:
            await self._session.close()

    async def _make_request(
        self, method: str, url: str, data: Optional[dict] = None
    ) -> dict:
        """发送HTTP请求"""
        await self._ensure_session()

        try:
            async with self._session.request(method, url, json=data) as response:
                response.raise_for_status()
                return await response.json()
        except aiohttp.ClientError as e:
            logger.error("HTTP error occurred: %s", e, exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.EMBEDDING_REQUEST_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.EMBEDDING_REQUEST_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.EMBEDDING_REQUEST_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.EMBEDDING_REQUEST_FAILED_SUGGESTION
                ),
            )
        except json.JSONDecodeError as e:
            logger.error("JSON decode error: %s", e, exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.EMBEDDING_RESPONSE_PARSE_FAILED,
                error_message=i18n_util.get_message(
                    I18nMessageCode.EMBEDDING_RESPONSE_PARSE_FAILED_MESSAGE
                ),
                error_reason=i18n_util.get_message(
                    I18nMessageCode.EMBEDDING_RESPONSE_PARSE_FAILED_REASON
                ),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.EMBEDDING_RESPONSE_PARSE_FAILED_SUGGESTION
                ),
            )
        except Exception as e:
            logger.error("Unexpected error: %s", e, exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.SERVER_INTERNAL_ERROR,
                error_message=i18n_util.get_message(
                    I18nMessageCode.SYSTEM_ERROR_MESSAGE
                ),
                error_reason=i18n_util.get_message(I18nMessageCode.SYSTEM_ERROR_REASON),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.SYSTEM_ERROR_SUGGESTION
                ),
            )

    async def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """嵌入多个文档文本"""
        if not texts:
            return []

        # 默认批处理大小，可以根据需要进行调整
        batch_size = 32

        all_embeddings = []

        for i in range(0, len(texts), batch_size):
            batch_texts = texts[i : i + batch_size]

            url = f"{self.endpoint}/embeddings"
            data = {"model": self.model_name, "input": batch_texts}

            try:
                result = await self._make_request("POST", url, data)
                if "data" not in result or len(result["data"]) != len(batch_texts):
                    raise MemoryServiceException(
                        error_code=ErrorCode.EMBEDDING_MODEL_RETURN_ERROR_DATA,
                        error_message=i18n_util.get_message(
                            I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_MESSAGE
                        ),
                        error_reason=i18n_util.get_message(
                            I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_REASON
                        ),
                        error_suggestion=i18n_util.get_message(
                            I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_SUGGESTION
                        ),
                    )

                batch_embeddings = []
                for item in result["data"]:
                    embedding = item.get("embedding")
                    if embedding is None:
                        raise MemoryServiceException(
                            error_code=ErrorCode.EMBEDDING_MODEL_RETURN_ERROR_DATA,
                            error_message=i18n_util.get_message(
                                I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_MESSAGE
                            ),
                            error_reason=i18n_util.get_message(
                                I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_MESSAGE
                            ),
                            error_suggestion=i18n_util.get_message(
                                I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_SUGGESTION
                            ),
                        )

                    if len(embedding) != self.embedding_dimension:
                        raise MemoryServiceException(
                            error_code=ErrorCode.EMBEDDING_DIMENSION_MISMATCH,
                            error_message=i18n_util.get_message(
                                I18nMessageCode.EMBEDDING_DIMENSION_MISMATCH_MESSAGE
                            ),
                            error_reason=i18n_util.get_message(
                                I18nMessageCode.EMBEDDING_DIMENSION_MISMATCH_REASON
                            ),
                            error_suggestion=i18n_util.get_message(
                                I18nMessageCode.EMBEDDING_DIMENSION_MISMATCH_SUGGESTION
                            ),
                        )

                    batch_embeddings.append(embedding)

                all_embeddings.extend(batch_embeddings)
            except MemoryServiceException:
                # 重新抛出MemoryServiceException
                raise
            except Exception as e:
                logger.error("Embed documents error occurred: %s", e, exc_info=True)
                raise MemoryServiceException(
                    error_code=ErrorCode.SERVER_INTERNAL_ERROR,
                    error_message=i18n_util.get_message(
                        I18nMessageCode.SYSTEM_ERROR_MESSAGE
                    ),
                    error_reason=i18n_util.get_message(
                        I18nMessageCode.SYSTEM_ERROR_REASON
                    ),
                    error_suggestion=i18n_util.get_message(
                        I18nMessageCode.SYSTEM_ERROR_SUGGESTION
                    ),
                )

        return all_embeddings

    async def embed_query(self, text: str, **kwargs: Any) -> List[float]:
        """嵌入单个查询文本"""
        if not text:
            return []

        url = f"{self.endpoint}/embeddings"
        data = {"model": self.model_name, "input": text, **kwargs}

        try:
            result = await self._make_request("POST", url, data)
            if "data" not in result or len(result["data"]) == 0:
                raise MemoryServiceException(
                    error_code=ErrorCode.EMBEDDING_MODEL_RETURN_ERROR_DATA,
                    error_message=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_MESSAGE
                    ),
                    error_reason=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_REASON
                    ),
                    error_suggestion=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_SUGGESTION
                    ),
                )

            embedding = result["data"][0].get("embedding")
            if embedding is None:
                raise MemoryServiceException(
                    error_code=ErrorCode.EMBEDDING_MODEL_RETURN_ERROR_DATA,
                    error_message=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_MESSAGE
                    ),
                    error_reason=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_REASON
                    ),
                    error_suggestion=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_MODEL_RETURN_ERROR_DATA_SUGGESTION
                    ),
                )

            if len(embedding) != self.embedding_dimension:
                raise MemoryServiceException(
                    error_code=ErrorCode.EMBEDDING_DIMENSION_MISMATCH,
                    error_message=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_DIMENSION_MISMATCH_MESSAGE
                    ),
                    error_reason=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_DIMENSION_MISMATCH_REASON
                    ),
                    error_suggestion=i18n_util.get_message(
                        I18nMessageCode.EMBEDDING_DIMENSION_MISMATCH_SUGGESTION
                    ),
                )

            return embedding
        except MemoryServiceException:
            # 重新抛出MemoryServiceException
            raise
        except Exception as e:
            logger.error("Embed query error occurred: %s", e, exc_info=True)
            raise MemoryServiceException(
                error_code=ErrorCode.SERVER_INTERNAL_ERROR,
                error_message=i18n_util.get_message(
                    I18nMessageCode.SYSTEM_ERROR_MESSAGE
                ),
                error_reason=i18n_util.get_message(I18nMessageCode.SYSTEM_ERROR_REASON),
                error_suggestion=i18n_util.get_message(
                    I18nMessageCode.SYSTEM_ERROR_SUGGESTION
                ),
            )

    async def __aenter__(self):
        """异步上下文管理器进入"""
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """异步上下文管理器退出"""
        await self._close_session()


# 全局实例
_embedding_instance: EmbeddingModelService


def get_embedding_instance() -> EmbeddingModelService:
    """获取向量化模型服务单例实例

    Returns:
        EmbeddingModelService: 向量化模型服务实例

    Raises:
        RuntimeError: 如果实例尚未初始化，请先调用 initialize_embedding() 方法
    """
    global _embedding_instance
    if _embedding_instance is None:
        raise RuntimeError(
            "Embedding model service not initialize，please call initialize_embedding()"
        )
    return _embedding_instance


def initialize_embedding(
    endpoint: str,
    model_name: str,
    dimension: int,
    api_key: Optional[str] = None,
    timeout: int = 30,
    custom_header: Optional[dict] = None,
):
    """初始化向量化模型服务实例

    Args:
        endpoint: 向量化模型服务地址
        model_name: 向量化模型名称
        dimension: 向量化维度
        api_key: 向量化模型API密钥（可选，默认为None）
        timeout: 连接超时时间（可选，默认30）
        custom_header: 自定义请求头（可选，默认为None）
    """
    global _embedding_instance
    _embedding_instance = EmbeddingModelService(
        endpoint=endpoint,
        model_name=model_name,
        dimension=dimension,
        api_key=api_key,
        timeout=timeout,
        custom_header=custom_header,
    )
