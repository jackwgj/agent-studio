# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

import asyncio
import os
from typing import Optional, Dict, Any, List
from urllib.parse import quote_plus

from elasticsearch import AsyncElasticsearch
from elasticsearch.exceptions import (
    RequestError,
    NotFoundError,
    ConnectionError as ESConnectionError,
)
from jiuwen.common.configs.env_constants import (
    DATASOURCE_ES_HOST_KEY,
    DATASOURCE_ES_PORT_KEY,
    DATASOURCE_ES_USERNAME_KEY,
    DATASOURCE_ES_PASSWORD_KEY,
    DATASOURCE_ES_SSL_MODE_KEY,
    DATASOURCE_ES_TIMEOUT_KEY,
    DATASOURCE_ES_API_KEY,
)
from jiuwen.common.log.base import logger
from pydantic import BaseModel, Field

DEFAULT_ES_HOST = "localhost"
DEFAULT_ES_PORT = 9200
DEFAULT_ES_USERNAME = ""
DEFAULT_ES_PASSWORD = ""
DEFAULT_ES_SSL_MODE = "false"
DEFAULT_ES_TIMEOUT = 10

_async_es_client: Optional[AsyncElasticsearch] = None
_async_es_lock = asyncio.Lock()


class ESSearchConfig(BaseModel):
    # 连接参数
    host: str = Field(default=DEFAULT_ES_HOST)
    port: int = Field(default=DEFAULT_ES_PORT, gt=0, le=65535)
    username: Optional[str] = None
    password: Optional[str] = None
    use_ssl: bool = Field(default=False)
    timeout: int = Field(default=DEFAULT_ES_TIMEOUT, gt=0)
    api_key: Optional[str] = None


def get_elastic_search_config() -> ESSearchConfig:
    """
    读取ES配置

    Returns:
        ESSearchConfig
    """
    return ESSearchConfig(
        host=os.getenv(DATASOURCE_ES_HOST_KEY, DEFAULT_ES_HOST),
        port=int(os.getenv(DATASOURCE_ES_PORT_KEY, DEFAULT_ES_PORT)),
        username=os.getenv(DATASOURCE_ES_USERNAME_KEY, ""),
        password=os.getenv(DATASOURCE_ES_PASSWORD_KEY, ""),
        use_ssl=os.getenv(DATASOURCE_ES_SSL_MODE_KEY, DEFAULT_ES_SSL_MODE).lower()
        == "true",
        timeout=int(os.getenv(DATASOURCE_ES_TIMEOUT_KEY, DEFAULT_ES_TIMEOUT)),
        api_key=str(os.getenv(DATASOURCE_ES_API_KEY, None)),
    )


async def _build_async_es_client(config: ESSearchConfig) -> AsyncElasticsearch:
    """
    创建es链接

    Args:
        config: 配置项

    Returns:
        es实例
    """

    scheme = "https" if config.use_ssl else "http"

    if config.username and config.password:
        user = quote_plus(config.username)
        pwd = quote_plus(config.password)
        host_url = f"{scheme}://{user}:{pwd}@{config.host}:{config.port}"
    else:
        host_url = f"{scheme}://{config.host}:{config.port}"

    headers = {}
    if config.api_key:
        headers["Authorization"] = f"ApiKey {config.api_key}"

    client = AsyncElasticsearch(
        hosts=[host_url],
        headers=headers,
        timeout=config.timeout,
        max_retries=0,
        retry_on_timeout=False,
        verify_certs=config.use_ssl,
    )

    try:
        if not await client.ping():
            raise ESConnectionError("Elasticsearch ping failed")
        logger.info(f"Connected to {config.host}:{config.port}")
        return client
    except Exception:
        await client.close()
        raise


async def get_async_es_client(
    config: Optional[ESSearchConfig] = None,
) -> AsyncElasticsearch:
    """
    获取es client

    Args:
        config: 配置项

    Returns:
        AsyncElasticsearc: es client
    """
    global _async_es_client
    if _async_es_client is None:
        async with _async_es_lock:
            if _async_es_client is None:
                if config is None:
                    config = get_elastic_search_config()
                _async_es_client = await _build_async_es_client(config)
    return _async_es_client


async def close_async_es_client() -> None:
    """
    关闭es client

    Returns:

    """
    global _async_es_client
    if _async_es_client is not None:
        try:
            await _async_es_client.close()
            logger.info("Elasticsearch client closed.")
        except Exception as e:
            logger.error(f"Error closing ES client: {e}", exc_info=True)
        finally:
            _async_es_client = None


class AsyncElasticSearchDao:
    """
    ES服务封装
    """

    def __init__(self, config: Optional[ESSearchConfig] = None):
        """
        初始化
        Args:
            config:
        """
        self._config = config

    async def get_all_indices(self) -> List[str]:
        """
        获取ES服务所有索引

        Returns:
            List[str]: index list

        """
        try:
            client = await self._get_client()
            indices = await client.indices.get_alias(index="*")
            return list(indices.keys())
        except Exception as e:
            logger.error(f"Error getting indices: {e}", exc_info=True)
            return []

    async def create_index(self, index: str, index_body: Dict[str, Any]) -> bool:
        """
        创建索引

        Args:
            index: index name
            index_body: index body

        Returns:
            bool: success or not
        """
        try:
            client = await self._get_client()
            await client.indices.create(index=index, body=index_body)
            logger.info(f"Index '{index}' created")
            return True
        except RequestError as e:
            if e.error == "resource_already_exists_exception":
                logger.info(f"Index '{index}' already exists")
                return False
            logger.error(
                f"ES RequestError creating index '{index}': {e}", exc_info=True
            )
            return False
        except Exception as e:
            logger.error(f"Error creating index '{index}': {e}", exc_info=True)
            return False

    async def add_field_to_index(
        self, index_name: str, field_name: str, field_type: str
    ) -> bool:
        """
        索引添加字段

        Args:
            index_name: index name
            field_name: field name
            field_type: field type

        Returns:
            bool: success or not
        """
        try:
            client = await self._get_client()
            body = {"properties": {field_name: {"type": field_type}}}
            await client.indices.put_mapping(index=index_name, body=body)
            logger.info(f"Added field '{field_name}' to '{index_name}'")
            return True
        except Exception as e:
            logger.error(f"Error adding field: {e}", exc_info=True)
            return False

    async def exist_index(self, index_name: str) -> bool:
        """
        索引是否存在

        Args:
            index_name: index name

        Returns:
            bool: success or not
        """
        try:
            client = await self._get_client()
            return await client.indices.exists(index=index_name)
        except Exception as e:
            logger.error(f"Error checking index existence: {e}", exc_info=True)
            return False

    async def get_document(self, index: str, doc_id: str) -> Dict[str, Any]:
        """
        读取文档

        Args:
            index: index name
            doc_id: document id

        Returns:
            Dict[str, Any]: document
        """
        try:
            client = await self._get_client()
            response = await client.get(index=index, id=doc_id)
            return response.get("_source", {})
        except NotFoundError:
            return {}
        except Exception as e:
            logger.error(f"Error getting doc '{doc_id}': {e}", exc_info=True)
            return {}

    async def search(
        self, index: str, body: Dict[str, Any], from_: int = 0
    ) -> List[Dict[str, Any]]:
        """
        检索

        Args:
            index: index name
            body: body
            from_: start index

        Returns:
            List[Dict[str, Any]]: search result
        """
        try:
            client = await self._get_client()
            resp = await client.search(index=index, body=body, from_=from_)
            return resp["hits"]["hits"]
        except Exception as e:
            logger.error(f"Search error in '{index}': {e}", exc_info=True)
            return []

    async def insert_document(
        self, index: str, document: Dict[str, Any], doc_id: Optional[str] = None
    ) -> bool:
        """
        插入文档

        Args:
            index: index name
            document: document
            doc_id: document id

        Returns:
            bool: success or not
        """
        try:
            client = await self._get_client()
            kwargs = {"index": index, "body": document}
            if doc_id:
                kwargs["id"] = doc_id
            await client.index(**kwargs)
            logger.info(f"Inserted doc into '{index}'")
            return True
        except Exception as e:
            logger.error(f"Insert error in '{index}': {e}", exc_info=True)
            return False

    async def update_document(
        self, index: str, doc_id: str, document: Dict[str, Any]
    ) -> bool:
        """
        更新文档

        Args:
            index:
            doc_id:
            document:

        Returns:

        """
        try:
            client = await self._get_client()
            await client.update(index=index, id=doc_id, body={"doc": document})
            return True
        except NotFoundError:
            logger.warning(f"Update: doc '{doc_id}' not found")
            return False
        except Exception as e:
            logger.error(f"Update error: {e}", exc_info=True)
            return False

    async def delete_document(self, index: str, doc_id: str) -> bool:
        """
        删除文档

        Args:
            index:
            doc_id:

        Returns:

        """
        try:
            client = await self._get_client()
            await client.delete(index=index, id=doc_id)
            return True
        except NotFoundError:
            logger.warning(f"Delete: doc '{doc_id}' not found")
            return False
        except Exception as e:
            logger.error(f"Delete error: {e}", exc_info=True)
            return False

    async def _get_client(self) -> AsyncElasticsearch:
        """
        获取es client

        Returns:

        """
        return await get_async_es_client(self._config)


async def get_async_elastic_search_instance(
    config: Optional[ESSearchConfig] = None,
) -> AsyncElasticSearchDao:
    """
    获取es实例
    Args:
        config:

    Returns:

    """

    return AsyncElasticSearchDao(config=config)
