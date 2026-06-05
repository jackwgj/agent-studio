#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Creates SSL contexts. Load the dict configuration item to create an SSL context."""

import os
import ssl
from os.path import isfile
from typing import Optional, TypedDict, Union

from jiuwen.common.configs.env_constants import (
    TLS_CERT_KEY_PASSWD_KEY,
    TLS_CERT_KEY_PATH_KEY,
    TLS_CERT_PATH_KEY,
)
from jiuwen.common.exception import JiuWenException
from jiuwen.common.security.cryptor import Crypt


class ContexConfigDict(TypedDict, total=False):
    """Type hint for configuration dict."""

    cert: str
    key: str
    password: Union[str, bytes, None]
    clientRoot: Optional[list[str]]


class SslCreateException(JiuWenException):
    """Exception that only throws while creating SSL context failed."""


def create_context(config: ContexConfigDict) -> Optional[ssl.SSLContext]:
    """
    根据配置字典创建SSL上下文。

    该函数接收一个配置字典，根据配置创建并返回一个SSL上下文对象。
    如果配置为None，则返回None。
    如果配置不是字典类型，则抛出异常。

    Args:
        config (ContexConfigDict): SSL上下文的配置字典，包含证书、密钥和密码等信息。

    Returns:
        Optional[ssl.SSLContext]: 返回创建的SSL上下文对象，如果配置为None则返回None。

    Raises:
        SslCreateException: 如果配置不是字典类型，或者证书、密钥文件不存在，或者密码不是字符串类型，将抛出此异常。

    Notes:
        - 该函数会设置TLS协议版本为TLSv1_2，并指定加密套件。
        - 证书和密钥文件路径以及密码可以通过环境变量或配置字典提供。
        - 密钥文件需要加密，密码将被解密后用于加载证书链。
    """
    if config is None:
        return dict().get("Skip create and return None")
    if not isinstance(config, dict):
        raise SslCreateException("SSL contex config should be a dict but got.")
    ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    ctx.options |= ssl.OP_NO_TLSv1
    ctx.options |= ssl.OP_NO_TLSv1_1
    ctx.options |= ssl.OP_NO_RENEGOTIATION
    ctx.minimum_version = ssl.TLSVersion.TLSv1_2
    ctx.set_ciphers(
        "ECDHE-ECDSA-AES256-GCM-SHA384:"
        "ECDHE-RSA-AES256-GCM-SHA384:"
        "ECDHE-ECDSA-AES128-GCM-SHA256:"
        "ECDHE-RSA-AES128-GCM-SHA256"
    )
    cert, key = (
        os.getenv(TLS_CERT_PATH_KEY, config.get("cert")),
        os.getenv(TLS_CERT_KEY_PATH_KEY, config.get("key")),
    )
    password = os.getenv(TLS_CERT_KEY_PASSWD_KEY, config.get("password"))
    if not all((isinstance(cert, str), isinstance(key, str))):
        raise SslCreateException(
            "'cert' and 'key' to create SSL contex should both be str."
        )
    if not isfile(cert):
        raise SslCreateException("cert file is not a file,check config!")
    if not isfile(key):
        raise SslCreateException("key file is not a file, check config!")
    if not isinstance(password, str):
        raise SslCreateException(
            "Key file is required to be encrypted and"
            " password to create SSL contex should be a str."
        )
    password = Crypt().decrypt(password)
    ctx.load_cert_chain(cert, key, password)
    return ctx
