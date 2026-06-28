#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.



import base64
import os
import threading
import time
from typing import Dict, Optional, Tuple

from openjiuwen.core.common.logging import workflow_logger

# Lazy import sentinel — gssapi 只在首次调用时导入
_gssapi = None
_gssapi_import_error: Optional[Exception] = None
_import_lock = threading.Lock()


def _ensure_gssapi():
    """延迟导入 gssapi，避免启动时就因缺少 gssapi 而报错"""
    global _gssapi, _gssapi_import_error
    if _gssapi is not None:
        return _gssapi
    if _gssapi_import_error is not None:
        raise _gssapi_import_error

    with _import_lock:
        if _gssapi is not None:
            return _gssapi
        if _gssapi_import_error is not None:
            raise _gssapi_import_error

        try:
            import gssapi
            from gssapi import creds as gssapi_creds

            _gssapi = {"gssapi": gssapi, "creds": gssapi_creds}
            workflow_logger.info("gssapi module loaded for Kerberos authentication")
            return _gssapi
        except ImportError as e:
            msg = (
                "Kerberos authentication requires the 'gssapi' library. "
                "Install it with: pip install gssapi. "
                "On Linux, you may also need: apt-get install libkrb5-dev (Debian/Ubuntu) "
                "or yum install krb5-devel (RHEL/CentOS)"
            )
            _gssapi_import_error = ImportError(msg)
            raise _gssapi_import_error from e


# 凭证缓存：key = (keytab_path, principal), value = (creds, expire_time)
_creds_cache: Dict[Tuple[str, str], Tuple[any, float]] = {}
_cache_lock = threading.Lock()

# 凭证有效期：1 小时（与 Java 的 loginDelay 对齐）
DEFAULT_CREDS_LIFETIME = 3600


def get_spnego_token(
    service_principal: str,
    keytab_path: str,
    krb5_conf_path: Optional[str] = None,
    client_principal: Optional[str] = None,
    creds_lifetime: int = DEFAULT_CREDS_LIFETIME,
) -> str:

    mods = _ensure_gssapi()
    gssapi = mods["gssapi"]
    gssapi_creds = mods["creds"]

    # 校验文件存在性
    if not os.path.isfile(keytab_path):
        raise FileNotFoundError(f"Keytab file not found: {keytab_path}")
    if krb5_conf_path and not os.path.isfile(krb5_conf_path):
        raise FileNotFoundError(f"krb5.conf file not found: {krb5_conf_path}")

    # 设置 KRB5_CONFIG 环境变量（GSSAPI 会读取）
    if krb5_conf_path:
        old_krb5_config = os.environ.get("KRB5_CONFIG")
        os.environ["KRB5_CONFIG"] = krb5_conf_path
        workflow_logger.debug("Set KRB5_CONFIG=%s", krb5_conf_path)
    else:
        old_krb5_config = None

    try:
        # 1. 获取或创建客户端凭证（带缓存）
        creds = _get_or_create_creds(keytab_path, client_principal, creds_lifetime)

        # 2. 构造服务主体名（Name 对象）
        service_name = gssapi.Name(
            service_principal, name_type=gssapi.NameType.hostbased_service
        )

        # 3. 初始化 SPNEGO 上下文，生成 token
        ctx = gssapi.SecurityContext(
            name=service_name, creds=creds, usage="initiate", mech=gssapi.MechType.spnego
        )

        # 初始化握手（第一次调用 step 返回 token）
        token = ctx.step()

        # Base64 编码（Java 端也是 Base64.getEncoder().encodeToString(token)）
        return base64.b64encode(token).decode("ascii")

    finally:
        # 恢复 KRB5_CONFIG
        if old_krb5_config is not None:
            os.environ["KRB5_CONFIG"] = old_krb5_config
        elif krb5_conf_path:
            os.environ.pop("KRB5_CONFIG", None)


def _get_or_create_creds(keytab_path: str, principal: Optional[str], lifetime: int):
    """获取或创建客户端凭证（带缓存）"""
    mods = _ensure_gssapi()
    gssapi = mods["gssapi"]
    gssapi_creds = mods["creds"]
    cache_key = (keytab_path, principal or "")
    now = time.time()

    with _cache_lock:
        if cache_key in _creds_cache:
            cached_creds, expire_time = _creds_cache[cache_key]
            if now < expire_time:
                workflow_logger.debug(
                    "Reusing cached Kerberos credentials (expires in %.1fs)",
                    expire_time - now,
                )
                return cached_creds

        # 缓存过期或不存在，重新认证
        workflow_logger.info(
            "Acquiring Kerberos credentials from keytab: %s (principal=%s)",
            keytab_path,
            principal or "auto",
        )

        # 从 keytab 获取凭证
        # store={'keytab': path} 指定 keytab 文件，usage='initiate' 表示客户端
        store = {"keytab": keytab_path}
        if principal:
            name = gssapi.Name(principal, name_type=gssapi.NameType.kerberos_principal)
            creds = gssapi_creds.Credentials(name=name, usage="initiate", store=store)
        else:
            # 不指定 name，让 GSSAPI 从 keytab 自动推断
            creds = gssapi_creds.Credentials(usage="initiate", store=store)

        # 写入缓存
        expire_time = now + lifetime
        _creds_cache[cache_key] = (creds, expire_time)
        workflow_logger.debug("Cached Kerberos credentials, expires at %s", expire_time)

        return creds


def clear_creds_cache():
    """清除凭证缓存（主要用于测试或手动刷新）"""
    with _cache_lock:
        _creds_cache.clear()
    workflow_logger.info("Cleared Kerberos credentials cache")
