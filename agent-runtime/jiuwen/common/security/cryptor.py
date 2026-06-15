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
    本地开发使用 PlainCrypt 模式，直接返回原始字符串。
    """

    @staticmethod
    def encrypt(origin: str):
        """pass-through encrypt for local dev"""
        return origin

    @staticmethod
    def decrypt(encrypt_str: str):
        """pass-through decrypt for local dev"""
        return encrypt_str
