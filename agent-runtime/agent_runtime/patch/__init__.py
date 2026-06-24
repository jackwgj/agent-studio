# -*- coding: utf-8 -*-
"""
性能优化 Patch 模块

使用方法：
    from agent_runtime.patch import apply_patches
    apply_patches()
"""
from agent_runtime.patch.tiktoken import apply as _apply_tiktoken

_patch_applied = False


def apply_patches():
    """应用所有性能优化 patches"""
    global _patch_applied

    if _patch_applied:
        return

    _apply_tiktoken()
    _patch_applied = True


__all__ = ["apply_patches"]