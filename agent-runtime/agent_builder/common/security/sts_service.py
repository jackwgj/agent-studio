#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
Sts service
"""

import pystssdk
from agent_builder.common.exception.status_code import StatusCode
from jiuwen.common.exception import JiuWenBaseException
from pystssdk import sts_api


def sts_init(
    config_path=None,
    service=None,
    micro_service=None,
    sts_server_domain=None,
    with_local_cache=True,
):
    """sts init"""
    try:
        pystssdk.sts_api.init(
            config_path=config_path,
            service=service,
            micro_service=micro_service,
            sts_server_domain=sts_server_domain,
            with_local_cache=with_local_cache,
        )
    except Exception as e:
        raise JiuWenBaseException(
            StatusCode.STS_INIT_ERROR.code, StatusCode.STS_INIT_ERROR.errmsg
        ) from e


def decrypt(encrypt_text):
    """
    decrypt
    """
    try:
        kek_decrypt = pystssdk.AesCryptor.builder().with_kek().build()
        decrypt_str = kek_decrypt.decrypt().from_base64(encrypt_text).to_raw_str()
    except Exception as e:
        raise JiuWenBaseException(
            StatusCode.STS_DECRYPT_ERROR.code, StatusCode.STS_DECRYPT_ERROR.errmsg
        ) from e
    return decrypt_str


def decrypt_sensitive_config(encrypt_text: str):
    """decrype sensitive config"""
    try:
        decrypt_str = sts_api.decrypt_sensitive_config(encrypt_text)
    except Exception as e:
        raise JiuWenBaseException(
            StatusCode.STS_DECRYPT_ERROR.code, StatusCode.STS_DECRYPT_ERROR.errmsg
        ) from e
    return decrypt_str
