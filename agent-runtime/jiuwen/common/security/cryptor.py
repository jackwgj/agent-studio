#!/usr/bin/env python
# -*- coding:utf-8 -*-

#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
An encryption and decryption module,
which uses the RootKey and WorkKey keys for encryption and decryption operations.
"""

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode


class Crypt:
    """
    保护敏感信息在传输过程中不被窃取或篡改。
    定义了两个静态方法encrypt和decrypt，用于加密和解密。
    """

    @staticmethod
    def encrypt(origin: str):
        """
        用于实现加密功能。
        Args:
            origin (str): 需要加密的原始字符串。
        Returns:
            无返回值。
        Raises:
            JiuWenBaseException: 如果加密初始化失败，抛出此异常。
        """
        raise JiuWenBaseException(
            error_code=StatusCode.CRYPT_INIT_ERROR.code,
            message=StatusCode.CRYPT_INIT_ERROR.errmsg,
        )

    @staticmethod
    def decrypt(encrypt_str: str):
        """
        用于实现解密功能。
        Args:
            encrypt_str (str): 需要解密的加密字符串。
        Returns:
            无返回值。
        Raises:
            JiuWenBaseException: 如果解密初始化失败，抛出此异常。
        """
        raise JiuWenBaseException(
            error_code=StatusCode.CRYPT_INIT_ERROR.code,
            message=StatusCode.CRYPT_INIT_ERROR.errmsg,
        )
