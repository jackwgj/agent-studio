#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
节点类型注册表

提供节点处理器的注册和查询功能。
"""

from typing import Optional

from .components import NODE_HANDLERS, BaseComponentHandler


def get_handler(ir_type: str) -> Optional[BaseComponentHandler]:
    """
    根据 IR 节点类型获取对应的处理器

    Args:
        ir_type: IR 节点类型（jiuwen.xxx）

    Returns:
        对应的处理器实例，如果不存在返回 None
    """
    return NODE_HANDLERS.get(ir_type)


def register_handler(ir_type: str, handler: BaseComponentHandler) -> None:
    """
    注册新的节点处理器

    Args:
        ir_type: IR 节点类型
        handler: 处理器实例
    """
    NODE_HANDLERS[ir_type] = handler


def unregister_handler(ir_type: str) -> None:
    """
    取消注册节点处理器

    Args:
        ir_type: IR 节点类型
    """
    NODE_HANDLERS.pop(ir_type, None)


def list_supported_types() -> list[str]:
    """
    列出所有已注册的节点类型

    Returns:
        已注册节点类型的列表
    """
    return list(NODE_HANDLERS.keys())
