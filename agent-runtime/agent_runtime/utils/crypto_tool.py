# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.

from openjiuwen.core.common.security.crypt_utils import BaseCrypt, CryptUtils


class PlainCrypt(BaseCrypt):
    PLAIN_CRYPT_NAME = "plain"

    def __init__(self):
        CryptUtils.register_crypt(PlainCrypt.PLAIN_CRYPT_NAME, self)

    def encrypt(self, key: bytes, origin: str) -> str:
        return origin

    def decrypt(self, key: bytes, encrypt_str: str) -> str:
        return encrypt_str


class CryptTool:
    _instance = None
    _crypt_name = PlainCrypt.PLAIN_CRYPT_NAME

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def encrypt(self, data: str, key: bytes = None) -> str:
        crypt = CryptUtils.get_crypt(self._crypt_name)
        return crypt.encrypt(key or b"", data)

    def decrypt(self, data: str, key: bytes = None) -> str:
        crypt = CryptUtils.get_crypt(self._crypt_name)
        return crypt.decrypt(key or b"", data)

    @classmethod
    def set_crypt(cls, name: str) -> None:
        cls._crypt_name = name


PlainCrypt()


encrypt = CryptTool().encrypt
decrypt = CryptTool().decrypt
