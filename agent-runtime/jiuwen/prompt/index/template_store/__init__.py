#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
template store
"""

__all__ = ["Template", "StoreType", "TemplateStore", "TEMPLATE_STORE_REGISTRY"]

from jiuwen.prompt.index.template_store.template_store import Template, TemplateStore
from jiuwen.prompt.index.template_store.store_type import StoreType
from jiuwen.prompt.index.template_store.register_index import (
    TEMPLATE_STORE_REGISTRY,
    register_template_store,
)
from jiuwen.prompt.index.template_store.in_memory_template_store import (
    InMemoryTemplateStore,
)
