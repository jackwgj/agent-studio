#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
register index
"""

from jiuwen.prompt.index.template_store.template_store import TemplateStore

TEMPLATE_STORE_REGISTRY = {}


def register_template_store(name):
    """Register a new store into the global registry."""

    def register_template_store_cls(cls):
        """The inner register function"""
        if name in TEMPLATE_STORE_REGISTRY and TEMPLATE_STORE_REGISTRY[name] is not cls:
            raise ValueError(
                f"Cannot register template_store with duplicate class name {name}"
            )
        if not issubclass(cls, TemplateStore):
            raise ValueError(f"{name} must extend TemplateStore")
        TEMPLATE_STORE_REGISTRY[name] = cls
        return cls

    return register_template_store_cls
