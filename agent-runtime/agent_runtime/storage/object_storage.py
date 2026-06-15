#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
对象存储工具 — 用于从对象存储读取 IR 文件

提供可插拔的 ObjectStorageProvider 抽象类，内置实现：
- S3StorageProvider — 基于 boto3 的 S3 兼容存储（ OBS S3 端口、AWS S3、MinIO 等）
- LocalStorageProvider — 本地文件系统读取

配置项（通过 settings.object_storage 或环境变量）:
    DATASOURCE_OBS_SERVER — S3 endpoint（如 https://obs.example.com:30443）
    DATASOURCE_OBS_BUCKET — 桶名
    DATASOURCE_OBS_AK     — Access Key
    DATASOURCE_OBS_SK     — Secret Key
"""

import asyncio
import os
import threading

import boto3
from agent_runtime.common.config import settings
from agent_runtime.common.ir_interfaces import (
    ObjectStorageProvider,
    StorageConfigError,
    StorageReadError,
)
from botocore.config import Config as BotoConfig
from openjiuwen.core.common.logging import workflow_logger


class S3StorageProvider(ObjectStorageProvider):
    """基于 boto3 的 S3 兼容存储提供者

    支持OBS S3 端口、AWS S3、MinIO 等兼容 S3 协议的对象存储。
    使用 boto3 client.get_object() + 线程池异步读取对象内容。
    """

    _instance = None
    _lock = threading.Lock()

    @classmethod
    def _decrypt_sk(cls, sk: str) -> str:
        """SK 解密接口 — 当前返回明文

        预留后续替换为商用解密实现。
        当 agentBuilder-engine 部署到商用环境时，SK 可能为加密存储，
        届时替换此方法即可。
        """
        return sk

    @classmethod
    def _get_client(cls):
        """获取或创建 boto3 S3 client 单例"""
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    server = settings.object_storage.server
                    ak = settings.object_storage.access_key
                    sk = settings.object_storage.secret_key

                    if not server or not ak or not sk:
                        workflow_logger.error(
                            "S3 storage config incomplete: missing DATASOURCE_OBS_SERVER/AK/SK"
                        )
                        raise StorageConfigError(
                            "S3 storage config incomplete, missing DATASOURCE_OBS_SERVER/AK/SK"
                        )

                    sk = cls._decrypt_sk(sk)

                    cls._instance = boto3.client(
                        "s3",
                        endpoint_url=server,
                        aws_access_key_id=ak,
                        aws_secret_access_key=sk,
                        verify=settings.object_storage.enable_ssl,
                        config=BotoConfig(
                            signature_version="s3v4",
                            s3={"addressing_style": "path"},
                        ),
                    )
                    workflow_logger.info(
                        "S3 client initialized for endpoint: {}", server
                    )

        return cls._instance

    async def get_object_bytes(self, object_key: str) -> bytes:
        """异步读取 S3 对象内容，返回原始字节

        Args:
            object_key: S3 对象 key

        Returns:
            bytes: 对象原始字节内容

        Raises:
            StorageConfigError: 配置缺失
            StorageReadError: 读取失败
        """
        try:
            bucket_name = settings.object_storage.bucket
            if not bucket_name:
                raise StorageConfigError("DATASOURCE_OBS_BUCKET not configured")

            client = self._get_client()
            loop = asyncio.get_running_loop()
            response = await loop.run_in_executor(
                None,
                lambda: client.get_object(Bucket=bucket_name, Key=object_key),
            )
            return response["Body"].read()

        except Exception as e:
            if isinstance(e, (StorageConfigError, StorageReadError)):
                raise
            workflow_logger.error(
                f"S3 read failed: object_key={object_key}, {e}", exc_info=True
            )
            raise StorageReadError(
                f"S3 read failed: object_key={object_key}, error={e}"
            ) from e

    async def get_content(self, object_key: str) -> str:
        """异步读取 S3 对象内容，返回 UTF-8 字符串

        Args:
            object_key: S3 对象 key（如 workflow/ir/xxx/xxx.json）

        Returns:
            str: 对象内容的 UTF-8 字符串

        Raises:
            StorageConfigError: 配置缺失
            StorageReadError: 读取失败
        """
        try:
            bucket_name = settings.object_storage.bucket
            if not bucket_name:
                workflow_logger.error("DATASOURCE_OBS_BUCKET not configured")
                raise StorageConfigError("DATASOURCE_OBS_BUCKET not configured")

            client = self._get_client()

            # boto3 get_object 是同步调用，放到线程池中执行
            loop = asyncio.get_running_loop()
            response = await loop.run_in_executor(
                None,
                lambda: client.get_object(Bucket=bucket_name, Key=object_key),
            )

            body = response["Body"].read()
            content = body.decode("utf-8")
            workflow_logger.debug(
                f"Read object from OBS: bucket={bucket_name}, key={object_key}, size={len(content)} bytes"
            )
            workflow_logger.debug(f"Object content: {content}")
            return content

        except StorageConfigError:
            raise
        except StorageReadError:
            raise
        except Exception as e:
            workflow_logger.error(
                f"S3 read failed: object_key={object_key}, {e}", exc_info=True
            )
            raise StorageReadError(
                f"S3 read failed: object_key={object_key}, error={e}"
            ) from e

    @classmethod
    def reset(cls):
        """重置单例 — 仅用于测试"""
        with cls._lock:
            if cls._instance is not None:
                cls._instance = None


class LocalStorageProvider(ObjectStorageProvider):
    """本地文件系统存储提供者"""

    async def get_content(self, object_key: str) -> str:
        """读取本地文件内容，返回 UTF-8 字符串

        Args:
            object_key: 文件路径

        Raises:
            StorageReadError: 文件不存在或读取失败
        """
        if not os.path.exists(object_key):
            workflow_logger.error(f"File not found: {object_key}")
            raise StorageReadError(f"File not found: {object_key}")

        try:
            loop = asyncio.get_running_loop()
            content = await loop.run_in_executor(
                None,
                lambda: open(object_key, "r", encoding="utf-8").read(),
            )
            return content
        except Exception as e:
            workflow_logger.error(f"File read failed: {object_key}, {e}", exc_info=True)
            raise StorageReadError(f"File read failed: {object_key}, {e}") from e


def get_storage_provider() -> ObjectStorageProvider:
    """根据配置获取存储提供者

    当 object_storage.server 已配置时返回 S3StorageProvider，
    否则返回 LocalStorageProvider。
    """

    if settings.object_storage.server:
        return S3StorageProvider()
    return LocalStorageProvider()
