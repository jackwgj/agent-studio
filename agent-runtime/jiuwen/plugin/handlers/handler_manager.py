#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
import threading
from abc import ABC, abstractmethod
from typing import Dict, Type

import requests
from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.common.utils.utils import safe_json_loads, url_to_ip_with_protocol_and_port


class AbstractFileHandler(ABC):
    @abstractmethod
    def handle(self, file_path: str, **kwargs):
        """
        文件处理的抽象接口类
        """
        raise NotImplementedError()


class FileHandler(AbstractFileHandler):
    @staticmethod
    def _get_valid_api_cert(api_cert_env):
        """get valid api_cert"""
        if api_cert_env is None:
            return True

        api_cert = safe_json_loads(api_cert_env, None)
        if api_cert is None:
            return True

        if not isinstance(api_cert, str) and not isinstance(api_cert, bool):
            return True

        return api_cert

    @classmethod
    def handle(cls, file_path: str, **kwargs):
        # 远程方式
        # 默认校验证书
        try:
            api_cert_env = kwargs.get("api_cert")
            verify = cls._get_valid_api_cert(api_cert_env=api_cert_env)
            valid_url, _ = url_to_ip_with_protocol_and_port(file_path)
            with requests.get(
                valid_url, stream=True, verify=verify, allow_redirects=False, timeout=60
            ) as response:
                response.raise_for_status()

                content = bytearray()
                for chunk in response.iter_content(chunk_size=1024):
                    if chunk:
                        content.extend(chunk)
                return bytes(content)
        except ValueError as val_error:
            raise JiuWenBaseException(
                error_code=StatusCode.REQUEST_GET_METHOD_ERROR.code,
                message=StatusCode.REQUEST_GET_METHOD_ERROR.errmsg.format(
                    reason="url is invalid."
                ),
            ) from val_error
        except Exception as e:
            raise JiuWenBaseException(
                error_code=StatusCode.REQUEST_GET_METHOD_ERROR.code,
                message=StatusCode.REQUEST_GET_METHOD_ERROR.errmsg.format(
                    reason=f"{type(e).__name__}"
                ),
            ) from e


class HandlerManager:
    _instance_lock = threading.Lock()
    _instance = None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            with cls._instance_lock:
                if cls._instance is None:
                    cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        if hasattr(self, "_inited"):
            return
        self.file_handler_map: Dict[str, AbstractFileHandler] = dict()
        self.default_file_handler = FileHandler
        self._inited = True

    def register(
        self, handler_type_value: str, handler_class: Type[AbstractFileHandler]
    ):
        """对外接口，用于注册不同类型文件的处理handler逻辑"""
        if not isinstance(handler_type_value, str):
            logger.error("FileHandler register failed")
            return
        if not isinstance(handler_class, type) or not issubclass(
            handler_class, AbstractFileHandler
        ):
            logger.error("FileHandler register failed")
            return
        self.file_handler_map.update({handler_type_value: handler_class})
        logger.info("FileHandler register success")

    def get_handler(self, handler_type: str) -> AbstractFileHandler:
        """引用接口，用于引用已注册的handler逻辑"""
        return self.file_handler_map.get(handler_type, self.default_file_handler)()
