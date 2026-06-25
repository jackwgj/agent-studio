#!/usr/bin/env python
# coding=utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""Register and cache openjiuwen workflows — conversion delegated to IRConverter."""

from __future__ import annotations

from typing import Any

from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.runner import Runner

_JIUWEN_IR_PATH_ATTR = "_jiuwen_ir_path"


async def ensure_openjiuwen_workflow_registered(
    workflow_ir: dict,
    *,
    workflow_id: str,
    workflow_name: str = "",
    description: str = "",
    agent_id: str = "",
):
    """Ensure an openjiuwen workflow resource exists for the current workflow/tag.

    IMPORTANT: Always builds a fresh Workflow instance from the IR. Reusing a
    Workflow object across runs is unsafe because agent-core mutates internal
    state (abilities, vertex sessions, compiled Pregel graph) during execution,
    and the reset between runs is only partial — leading to NoneType errors on
    the second invocation.
    """
    workflow_id = workflow_id or workflow_ir.get("workflowId")
    tag = agent_id or None
    current_ir_path = _resolve_workflow_ir_path(workflow_ir)
    if not workflow_id:
        raise ValueError("workflow_id is required to register openjiuwen workflow")

    existing = await _get_workflow_if_exists(workflow_id, tag)
    if existing is not None:
        if _workflow_matches_ir_path(existing, current_ir_path):
            _reset_workflow_components(existing)
            return existing
        _remove_workflow_if_exists(workflow_id, tag)

    from jiuwen.serve.controllers.execution.ir_converter import IRConverter

    workflow = await IRConverter.ir_to_workflow(workflow_ir)
    Runner.resource_mgr.remove_workflow(
        workflow_id=workflow.card.id, tag=workflow.card.id
    )
    _attach_workflow_ir_path(workflow, current_ir_path)

    result = Runner.resource_mgr.add_workflow(
        card=workflow.card,
        workflow=lambda *args, workflow=workflow, **kwargs: workflow,
        tag=tag,
    )
    if hasattr(result, "is_ok") and not result.is_ok():
        err = result.err() if hasattr(result, "err") else result
        if "resource already exist" not in str(err):
            raise RuntimeError(f"failed to register openjiuwen workflow: {err}")
    return workflow


def _resolve_workflow_ir_path(workflow_ir: dict) -> str:
    if not isinstance(workflow_ir, dict):
        return ""
    return str(workflow_ir.get("ir_path") or workflow_ir.get("irPath") or "")


def _reset_workflow_components(workflow: Any) -> None:
    """Reset mutable per-request state on cached workflow components.

    The workflow instance is cached by ``ensure_openjiuwen_workflow_registered``
    and reused across requests. Components like the End node use an
    ``_executed`` flag for intra-request idempotency; without resetting it
    before reuse, the second request's End ``transform`` is skipped, yielding
    zero chunks and an empty answer.
    """
    try:
        from jiuwen.extension.workflow_node.end import End

        for comp in _iter_workflow_components(workflow):
            if isinstance(comp, End):
                comp.reset_execution_state()
    except Exception as e:
        workflow_logger.warning("Failed to reset workflow components: %s", e, exc_info=True)


def _iter_workflow_components(workflow: Any):
    """Best-effort iterator over component instances inside a workflow."""
    bpmn = getattr(workflow, "_bpmn_workflow_instance", None)
    if bpmn is None:
        return
    graph = getattr(bpmn, "graph", None)
    if graph is None:
        return
    nodes = getattr(graph, "nodes", None)
    if not nodes:
        return
    for vertex in nodes.values():
        executable = getattr(vertex, "_executable", None)
        if executable is not None:
            yield executable


def _attach_workflow_ir_path(workflow: Any, ir_path: str) -> None:
    if ir_path:
        setattr(workflow, _JIUWEN_IR_PATH_ATTR, ir_path)


def _workflow_matches_ir_path(workflow: Any, ir_path: str) -> bool:
    if not ir_path:
        return True
    return getattr(workflow, _JIUWEN_IR_PATH_ATTR, None) == ir_path


def _remove_workflow_if_exists(workflow_id: str, tag: str | None) -> None:
    try:
        result = Runner.resource_mgr.remove_workflow(workflow_id=workflow_id, tag=tag)
    except TypeError:
        result = Runner.resource_mgr.remove_workflow(workflow_id)
    except Exception as exc:
        if _is_not_found_error(exc):
            return
        raise

    if hasattr(result, "is_ok") and not result.is_ok():
        err = result.err() if hasattr(result, "err") else result
        if not _is_not_found_error(err):
            raise RuntimeError(f"failed to remove stale openjiuwen workflow: {err}")


async def _get_workflow_if_exists(workflow_id: str, tag: str | None):
    try:
        return await Runner.resource_mgr.get_workflow(workflow_id=workflow_id, tag=tag)
    except Exception as exc:
        if "not found" in str(exc).lower() or "not exist" in str(exc).lower():
            return None
        return None


def _is_not_found_error(error: Any) -> bool:
    error_text = str(error).lower()
    return "not found" in error_text or "not exist" in error_text
