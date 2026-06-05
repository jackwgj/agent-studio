#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""对象存储基础设施层"""

from agent_runtime.common.ir_interfaces import (
    ObjectStorageProvider,
    StorageConfigError,
    StorageReadError,
)

from .object_storage import (
    S3StorageProvider,
    LocalStorageProvider,
    get_storage_provider,
)

__all__ = [
    "ObjectStorageProvider",
    "S3StorageProvider",
    "LocalStorageProvider",
    "StorageConfigError",
    "StorageReadError",
    "get_storage_provider",
]
