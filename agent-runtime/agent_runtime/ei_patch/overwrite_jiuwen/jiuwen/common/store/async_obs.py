# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
# @override(jiuwen) added logging
"""Async OBS utils"""

import os
import threading
from abc import ABC

import aiohttp
from jiuwen.common.alarm.hwclouds_alarm_utils import (
    record_alarm,
)  # @override(jiuwen) added alarm logging
from jiuwen.common.configs.env_constants import (
    DATASOURCE_OBS_BUCKET_KEY,
    DATASOURCE_OBS_ENABLE_SSL,
)
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.common.net import Connector
from jiuwen.common.security.cryptor import Crypt
from jiuwen.common.store.obs import OBSException
from yarl import URL

from obs import ObsClient

HTTP_SUCCESS_THRESHOLD = 300


class AbstractConfig(ABC):
    """Abstract config."""

    def __repr__(self):
        return str(self.__dict__)


class OBSConfig(AbstractConfig):
    """OBS config."""

    def __init__(self):
        env_prefix = "DATASOURCE_OBS_"
        self.server = os.environ.get(env_prefix + "SERVER")
        self.bucket = os.environ.get(env_prefix + "BUCKET")
        self.ak = os.environ.get(env_prefix + "AK")
        self.sk = os.environ.get(env_prefix + "SK")


class AsyncOBSUtil:
    """异步obs实现"""

    _instance = None
    _lock = threading.Lock()

    @classmethod
    def instance(cls, ak=None, sk=None, server=None):
        """Get an obs instance."""
        if cls._instance is None:
            with cls._lock:
                # 单例标准写法：判断两次，多个线程可以避免不必要的锁竞争
                if cls._instance is None:
                    obs_env_config = OBSConfig()
                    # 优先使用函数参数值，参数值为空时则取环境变量默认值
                    ak = ak or obs_env_config.ak
                    sk = sk or obs_env_config.sk
                    curr_work_api = Crypt()
                    sk = curr_work_api.decrypt(sk)
                    server = server or obs_env_config.server
                    if ak is None or sk is None or server is None:
                        record_alarm(
                            "OBS", "OBSClient", "ERROR", "missing obs config."
                        )  # @override(jiuwen) added alarm logging
                        raise OBSException(
                            error_code=StatusCode.OBS_CHECK_CONFIG_ERROR.code,
                            message=StatusCode.OBS_CHECK_CONFIG_ERROR.errmsg,
                        )
                    cls._instance = ObsClient(
                        access_key_id=ak,
                        secret_access_key=sk,
                        server=server,
                        path_style=True,
                    )

        return cls._instance

    @classmethod
    async def get_content(cls, object_key: str, bucket_name=None):
        """Download object."""
        try:
            bucket_name = bucket_name or os.environ.get(DATASOURCE_OBS_BUCKET_KEY)
            obs_client = AsyncOBSUtil.instance()
            signed_url = obs_client.createSignedUrl(
                method="GET", bucketName=bucket_name, objectKey=object_key
            )
            url = signed_url["signedUrl"]
            if "true" == os.environ.get("DEEP_RESEARCH_ENABLED"):
                # DeepResearch 场景下，如果模板文件名带英文括号，实际链接中的编码会被aiohttp decode，此处obs sdk已完成编码，无需aiohttp再处理，其他场景若遇到此处403-SignatureDoesNotMatch的问题，可参考解决
                url = URL(url, encoded=True)
            headers = signed_url["actualSignedRequestHeaders"]
            connector = Connector().get_tcp_connector()
            async with aiohttp.ClientSession(
                connector=connector, connector_owner=connector is None
            ) as session:
                async with session.get(
                    url=url,
                    headers=headers,
                    ssl=os.getenv(DATASOURCE_OBS_ENABLE_SSL, "True").lower() == "true",
                    allow_redirects=False,
                ) as res:
                    if res.status < HTTP_SUCCESS_THRESHOLD:
                        return await res.content.read()
                    # @override(jiuwen) added logging
                    content = await res.content.read()
                    logger.error(f"Fail read obs object. url {url} content:{content}")
                    # @override end
                    raise OBSException(
                        error_code=StatusCode.OBS_GET_OBJECT_ERROR.code,
                        message=StatusCode.OBS_GET_OBJECT_ERROR.errmsg.format(
                            object_key, bucket_name
                        ),
                    )
        except Exception as e:
            if isinstance(e, OBSException):
                # @override(jiuwen) added logging
                record_alarm(
                    "OBS", "getObjectContent", "ERROR", f"Fail read obs. {e}"
                )  # @override(jiuwen) added alarm logging
                logger.error(f"Fail read obs. {e}")
                # @override end
                raise e
            message = StatusCode.OBS_CHECK_BUCKET_ERROR.errmsg.format(
                object_key, bucket_name
            )
            # @override(jiuwen) added logging
            record_alarm(
                "OBS", "getObjectContent", "ERROR", f"Fail read obs. {message}"
            )  # @override(jiuwen) added alarm logging
            logger.error(
                f"fail read obs. bucket:{bucket_name} object:{object_key} {e}",
                simple_log="Head Bucket Failed.",
            )
            # @override end
            raise OBSException(
                error_code=StatusCode.OBS_CHECK_BUCKET_ERROR.code, message=message
            ) from e

    @classmethod
    async def download_to_file(cls, object_key: str, local_path: str, bucket_name=None):
        """Download OBS object to local file using HTTP request."""
        try:
            bucket_name = bucket_name or os.environ.get(DATASOURCE_OBS_BUCKET_KEY)
            obs_client = AsyncOBSUtil.instance()
            signed_url = obs_client.createSignedUrl(
                method="GET", bucketName=bucket_name, objectKey=object_key
            )
            url = signed_url["signedUrl"]
            url = URL(url, encoded=True)
            headers = signed_url["actualSignedRequestHeaders"]
            connector = Connector().get_tcp_connector()

            # 确保目标目录存在
            os.makedirs(os.path.dirname(local_path), exist_ok=True)

            async with aiohttp.ClientSession(
                connector=connector, connector_owner=connector is None
            ) as session:
                async with session.get(
                    url=url,
                    headers=headers,
                    ssl=os.getenv(DATASOURCE_OBS_ENABLE_SSL, "True").lower() == "true",
                    allow_redirects=False,
                ) as res:
                    if res.status < HTTP_SUCCESS_THRESHOLD:
                        # 流式写入本地文件
                        with open(local_path, "wb") as f:
                            async for chunk in res.content.iter_chunked(
                                8192
                            ):  # 8KB chunks
                                f.write(chunk)
                        return local_path

                    # @override(jiuwen) added logging
                    content = await res.content.read()
                    logger.error(f"Fail read obs object. url {url} content:{content}")
                    # @override end
                    raise OBSException(
                        error_code=StatusCode.OBS_GET_OBJECT_ERROR.code,
                        message=StatusCode.OBS_GET_OBJECT_ERROR.errmsg.format(
                            object_key, bucket_name
                        ),
                    )
        except Exception as e:
            if isinstance(e, OBSException):
                # @override(jiuwen) added logging
                record_alarm("OBS", "downloadToFile", "ERROR", f"Fail read obs. {e}")
                logger.error(f"Fail read obs. {e}")
                # @override end
                raise e
            message = StatusCode.OBS_CHECK_BUCKET_ERROR.errmsg.format(
                object_key, bucket_name
            )
            # @override(jiuwen) added logging
            record_alarm("OBS", "downloadToFile", "ERROR", f"Fail read obs. {message}")
            logger.error(
                f"fail read obs. bucket:{bucket_name} object:{object_key} {e}",
                simple_log="Head Bucket Failed.",
            )
            # @override end
            raise OBSException(
                error_code=StatusCode.OBS_CHECK_BUCKET_ERROR.code, message=message
            ) from e
