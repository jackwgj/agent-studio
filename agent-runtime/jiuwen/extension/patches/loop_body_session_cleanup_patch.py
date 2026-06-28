# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

"""
Loop body session cleanup (jiuwen-side patch, no openjiuwen core edits).

Problem (PR !1163): loop-body components share io_state across rounds. Nodes skipped on
the current branch/path can still leave outputs in session; downstream aggregate reads
stale values via get_inputs().

Approach: at each LoopGroup body run, clear session data for registered body components
(not the whole scope root, so virtual keys like state_store survive), and reset body
node ids in executed_nodes / finished_stream_nodes.

Sub-workflow interrupt/resume cleanup stays in workflow_sub_stream_patch.py.

Applied once at import from ir_converter / sub_workflow.
"""

from __future__ import annotations

from typing import Iterable

from openjiuwen.core.graph.executable import Input, Output
from openjiuwen.core.session import BaseSession

_PATCH_APPLIED = False
_orig_loop_group_on_invoke = None


def _loop_comp_workflow_session(session: BaseSession) -> BaseSession:
    """Session that owns workflow_state (executed_nodes) for the loop component vertex."""
    if hasattr(session, "node_id") and session.node_id() == "body" and session.parent() is not None:
        return session.parent()
    return session


def clear_loop_body_round_marks(session: BaseSession, body_node_ids: Iterable[str]) -> None:
    """Drop loop-body component ids from executed_nodes / finished_stream_nodes."""
    body_set = {node_id for node_id in body_node_ids if node_id}
    if not body_set:
        return
    workflow_state = _loop_comp_workflow_session(session).state()
    updates: dict[str, list[str]] = {}
    executed_nodes = workflow_state.get_workflow_state("executed_nodes") or []
    if executed_nodes:
        updates["executed_nodes"] = [nid for nid in executed_nodes if nid not in body_set]
    finished_stream_nodes = workflow_state.get_workflow_state("finished_stream_nodes") or []
    if finished_stream_nodes:
        updates["finished_stream_nodes"] = [nid for nid in finished_stream_nodes if nid not in body_set]
    if updates:
        workflow_state.update_and_commit_workflow_state(updates)


def clear_loop_body_component_io_state(
    session: BaseSession,
    scope_id: str,
    body_node_ids: Iterable[str],
) -> None:
    """Remove io_state outputs for registered loop-body graph components only.

    Keys are nested paths like ``loop_node.branch_a`` under the shared io_state root.
    Does not wipe the whole ``loop_node`` scope so virtual keys (e.g. state_store) persist.
    """
    if not scope_id:
        return
    io_state = getattr(_loop_comp_workflow_session(session).state(), "_io_state", None)
    if io_state is None:
        return
    cleared = False
    for node_id in body_node_ids:
        if not node_id:
            continue
        nested_key = f"{scope_id}.{node_id}"
        io_state.update_by_id(nested_key, {nested_key: None})
        cleared = True
    if cleared:
        io_state.commit()


def clear_loop_body_session_for_round(
    session: BaseSession,
    scope_id: str,
    body_node_ids: Iterable[str],
) -> None:
    """Boundary cleanup before each loop body iteration."""
    clear_loop_body_round_marks(session, body_node_ids)
    clear_loop_body_component_io_state(session, scope_id, body_node_ids)


def _loop_body_io_scope_id(session: BaseSession) -> str:
    """parent_id used by loop-body NodeSession state."""
    loop_compile_session = session.parent() if hasattr(session, "parent") else None
    if loop_compile_session is None:
        return ""
    return loop_compile_session.node_id()


async def _patched_loop_group_on_invoke(
    self, inputs: Input, session: BaseSession, **kwargs
) -> Output:
    body_node_ids = getattr(self, "_node_ids", None)
    if body_node_ids:
        scope_id = _loop_body_io_scope_id(session)
        clear_loop_body_session_for_round(session, scope_id, body_node_ids)
    return await _orig_loop_group_on_invoke(self, inputs, session, **kwargs)


def apply_loop_body_session_cleanup_patch() -> bool:
    """Patch LoopGroup to clear body session data at each round boundary."""
    global _PATCH_APPLIED, _orig_loop_group_on_invoke
    if _PATCH_APPLIED:
        return False

    from openjiuwen.core.workflow.components.flow.loop.loop_comp import LoopGroup

    _orig_loop_group_on_invoke = LoopGroup.on_invoke
    LoopGroup.on_invoke = _patched_loop_group_on_invoke

    _PATCH_APPLIED = True
    return True
