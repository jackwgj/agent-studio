# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

"""
Patch openjiuwen LoopGroup.on_invoke to clear loop-body node outputs before each
iteration.

Root cause: body components share the same io_state across loop rounds. When a
branch skips a node, stale outputs remain and downstream first-non-null aggregate
reads the previous round's value.

Applied at import time until openjiuwen merges an equivalent fix in LoopComponent.
"""

from __future__ import annotations

from typing import Iterable

from openjiuwen.core.graph.executable import Input, Output
from openjiuwen.core.session import BaseSession

_PATCH_APPLIED = False
_orig_loop_group_on_invoke = None


def _reset_loop_body_outputs(session: BaseSession, body_node_ids: Iterable[str]) -> None:
    """Drop committed io_state outputs for loop-body nodes under the loop scope."""
    parent_session = session.parent() if hasattr(session, "parent") else None
    scope_id = (
        parent_session.node_id()
        if parent_session is not None and hasattr(parent_session, "node_id")
        else session.node_id()
    )
    if not scope_id:
        return

    io_state = getattr(session.state(), "_io_state", None)
    if io_state is None:
        return
    pending = False
    for node_id in body_node_ids:
        if not node_id:
            continue
        nested_key = f"{scope_id}.{node_id}"
        io_state.update_by_id(nested_key, {nested_key: None})
        pending = True
    if pending:
        io_state.commit()


async def _patched_loop_group_on_invoke(
    self, inputs: Input, session: BaseSession, **kwargs
) -> Output:
    body_node_ids = getattr(self, "_node_ids", None)
    if body_node_ids:
        _reset_loop_body_outputs(session, body_node_ids)
    return await _orig_loop_group_on_invoke(self, inputs, session, **kwargs)


def apply_loop_body_output_reset_patch() -> bool:
    """Monkey-patch LoopGroup.on_invoke once per process."""
    global _PATCH_APPLIED, _orig_loop_group_on_invoke
    if _PATCH_APPLIED:
        return False

    from openjiuwen.core.workflow.components.flow.loop.loop_comp import LoopGroup

    _orig_loop_group_on_invoke = LoopGroup.on_invoke
    LoopGroup.on_invoke = _patched_loop_group_on_invoke
    _PATCH_APPLIED = True
    return True
