# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
# @override(jiuwen) added logging
"""OBS utils"""

import os
import threading
from abc import ABC

import requests
from jiuwen.common.alarm.hwclouds_alarm_utils import (
    record_alarm,
)  # @override(jiuwen) added alarm logging
from jiuwen.common.configs.env_constants import DATASOURCE_OBS_BUCKET_KEY
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.common.security.cryptor import Crypt

from obs import ObsClient


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


class OBSUtil:
    """OBS operation."""

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
    def check_bucket_existence(cls, bucket_name=None):
        """Check bucket existence."""
        try:
            if not bucket_name:
                bucket_name = os.environ.get(DATASOURCE_OBS_BUCKET_KEY)
            obs_client = OBSUtil.instance()
            resp = obs_client.headBucket(bucket_name)
            # 返回码为2xx时，接口调用成功，否则接口调用失败
            if resp.status < requests.codes.multiple_choices:
                return True
            return False
        except Exception as e:
            record_alarm(
                "OBS", "OBS", "ERROR", f"check_bucket_existence fail. {e}"
            )  # @override(jiuwen) added alarm logging
            if isinstance(e, OBSException):
                raise e
            logger.error("Head Bucket Failed")
            raise OBSException(
                error_code=StatusCode.OBS_CHECK_BUCKET_ERROR.code,
                message=StatusCode.OBS_CHECK_BUCKET_ERROR.errmsg,
            ) from e

    @classmethod
    def upload_content(cls, object_key, content, bucket_name=None):
        """Upload object."""
        try:
            if not bucket_name:
                bucket_name = os.environ.get(DATASOURCE_OBS_BUCKET_KEY)
            obs_client = OBSUtil.instance()
            resp = obs_client.putContent(bucket_name, object_key, content)
            # 返回码为2xx时，接口调用成功，否则接口调用失败
            if resp.status < requests.codes.multiple_choices:
                return resp
            if resp.status == requests.codes.not_found:
                raise OBSException(
                    error_code=StatusCode.OBS_PUT_CONTENT_ERROR.code,
                    message=StatusCode.OBS_PUT_CONTENT_ERROR.errmsg.format(resp.status),
                )
            raise OBSException(
                error_code=StatusCode.OBS_PUT_CONTENT_ERROR.code,
                message=StatusCode.OBS_PUT_CONTENT_ERROR.errmsg.format(resp.status),
            )
        except Exception as e:
            record_alarm(
                "OBS", "OBS", "ERROR", f"upload_content fail. {e}"
            )  # @override(jiuwen) added alarm logging
            if isinstance(e, OBSException):
                raise e
            message = StatusCode.OBS_CHECK_BUCKET_ERROR.errmsg
            logger.error(
                f"Failed to upload file, exception:{message}",
                simple_log="Failed to upload file",
            )
            raise OBSException(
                error_code=StatusCode.OBS_CHECK_BUCKET_ERROR.code, message=message
            ) from e

    @classmethod
    def get_content(cls, object_key, bucket_name=None):
        """Download object."""
        try:
            if not bucket_name:
                bucket_name = os.environ.get(DATASOURCE_OBS_BUCKET_KEY)
            obs_client = OBSUtil.instance()
            # loadStreamInMemory如果为True则会忽略 downloadpath 路径,将数据流下载到内存
            resp = obs_client.getObject(
                bucketName=bucket_name, objectKey=object_key, loadStreamInMemory=True
            )
            # 返回码为2xx时，接口调用成功，否则接口调用失败
            if resp.status < requests.codes.multiple_choices:
                return resp.body.buffer
            # @override(jiuwen) added logging
            logger.error(
                f"Fail read obs object, {bucket_name}/{object_key} {resp.body.buffer}"
            )
            # @override end
            raise OBSException(
                error_code=StatusCode.OBS_GET_OBJECT_ERROR.code,
                message=StatusCode.OBS_GET_OBJECT_ERROR.errmsg.format(
                    object_key, bucket_name
                ),
            )
        except Exception as e:
            record_alarm(
                "OBS", "OBS", "ERROR", f"get_content fail. {e}"
            )  # @override(jiuwen) added alarm logging
            if isinstance(e, OBSException):
                raise e
            message = StatusCode.OBS_CHECK_BUCKET_ERROR.errmsg
            # @override(jiuwen) added logging
            logger.error(f"fail read obs content. {e}")
            # @override end
            raise OBSException(
                error_code=StatusCode.OBS_CHECK_BUCKET_ERROR.code, message=message
            ) from e

    @classmethod
    def copy_object(
        cls, source_bucket_name, source_object_key, dest_bucket_name, dest_object_key
    ):
        """Copy Object."""
        try:
            obs_client = OBSUtil.instance()
            resp = obs_client.copyObject(
                source_bucket_name, source_object_key, dest_bucket_name, dest_object_key
            )
            # 返回码为2xx时，接口调用成功，否则接口调用失败
            if resp.status < requests.codes.multiple_choices:
                return resp
            raise OBSException(
                error_code=StatusCode.OBS_COPY_OBJECT_ERROR.code,
                message=StatusCode.OBS_COPY_OBJECT_ERROR.errmsg.format(
                    source_object_key, source_bucket_name
                ),
            )
        except Exception as e:
            record_alarm(
                "OBS", "OBS", "ERROR", f"copy_object fail. {e}"
            )  # @override(jiuwen) added alarm logging
            if isinstance(e, OBSException):
                raise e
            message = StatusCode.OBS_CHECK_BUCKET_ERROR.errmsg.format(
                source_object_key, source_bucket_name
            )
            logger.error(message)
            raise OBSException(
                error_code=StatusCode.OBS_CHECK_BUCKET_ERROR.code, message=message
            ) from e

    @classmethod
    def delete_object(cls, object_key, bucket_name=None):
        """
        Delete object.
        Return 204 if the file is successfully deleted or [does not exist].
        """
        try:
            if not bucket_name:
                bucket_name = os.environ.get(DATASOURCE_OBS_BUCKET_KEY)
            obs_client = OBSUtil.instance()
            resp = obs_client.deleteObject(bucket_name, object_key)
            # 返回码为2xx时，接口调用成功，否则接口调用失败
            if resp.status < requests.codes.multiple_choices:
                return resp
            raise OBSException(
                error_code=StatusCode.OBS_DELETE_OBJECT_ERROR.code,
                message=StatusCode.OBS_DELETE_OBJECT_ERROR.errmsg.format(
                    object_key, bucket_name
                ),
            )
        except Exception as e:
            record_alarm(
                "OBS", "OBS", "ERROR", f"delete_object fail. {e}"
            )  # @override(jiuwen) added alarm logging
            if isinstance(e, OBSException):
                raise e
            message = StatusCode.OBS_CHECK_BUCKET_ERROR.errmsg
            logger.error(message)
            raise OBSException(
                error_code=StatusCode.OBS_CHECK_BUCKET_ERROR.code, message=message
            ) from e


class OBSException(JiuWenBaseException):
    """This exception is thrown when an operation error occurs in the object storage service."""

    ...
