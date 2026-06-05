#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

import json
import os
from typing import List

import httpx
from jiuwen.common.configs.env_constants import EMBEDDING_BASE_URL_KEY
from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger

TIMEOUT = 30.0
DEFAULT_EMBEDDING_DIMENSION = 1024


async def call_text_embedding(
    model_name: str = "text-embedding-v4",
    emb_dim: int = DEFAULT_EMBEDDING_DIMENSION,
    query: str = "",
    api_key: str = "",
) -> List[float]:
    """
    异步调用 embedding service
    """

    if model_name != "text-embedding-v4":
        raise ValueError(f"Unsupported model: {model_name}")

    base_url = os.getenv(EMBEDDING_BASE_URL_KEY, "").strip()
    if not base_url:
        raise ValueError("Embedding base URL is not configured")

    url = f"{base_url}/embeddings"
    headers = {
        "Authorization": f"Bearer {api_key.strip()}",
        "Content-Type": "application/json",
    }
    payload = {
        "model": model_name,
        "input": query,
        "dimensions": emb_dim,
        "encoding_format": "float",
    }

    try:
        async with httpx.AsyncClient(timeout=TIMEOUT) as client:
            resp = await client.post(url, json=payload, headers=headers)

        if resp.status_code != 200:
            err_summary = resp.text[:300]
            err_msg = f"API request failed [status={resp.status_code}]: {err_summary}"
            logger.error(err_msg, exc_info=True)
            raise JiuWenBaseException(
                error_code=StatusCode.EMBEDDING_SERVICE_REQUEST_FAILED.code,
                message=err_msg,
            )

        try:
            data = resp.json()
        except json.JSONDecodeError as e:
            err_msg = "Invalid JSON response"
            logger.error(err_msg, exc_info=True)
            raise ValueError(err_msg) from e

        # 提取 embedding
        try:
            embedding = data["data"][0]["embedding"]
        except (KeyError, IndexError, TypeError) as e:
            err_msg = "Malformed response structure"
            logger.error(err_msg, exc_info=True)
            raise ValueError(err_msg) from e

        if not isinstance(embedding, list) or len(embedding) != emb_dim:
            err_msg = (
                f"Invalid embedding: expected {emb_dim}-dim list, ",
                f"got {type(embedding)} len={len(embedding) if hasattr(embedding, '__len__') else 'N/A'}",
            )
            logger.error(err_msg, exc_info=True)
            raise ValueError(err_msg)
        if not all(isinstance(x, (int, float)) for x in embedding):
            err_msg = "Embedding contains non-numeric values"
            logger.error(err_msg, exc_info=True)
            raise ValueError(err_msg)

        return embedding

    except Exception as e:
        err_msg = "Failed to request embedding service."
        logger.error(err_msg, exc_info=True)
        raise JiuWenBaseException(
            error_code=StatusCode.EMBEDDING_SERVICE_REQUEST_FAILED.code,
            message=err_msg,
        ) from e
