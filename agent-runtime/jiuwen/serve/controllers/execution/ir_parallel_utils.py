# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
"""Utilities for IR parallel-branch fork/join conversion."""

from __future__ import annotations

from collections import defaultdict, deque


def _component_id(endpoint: dict) -> str:
    return (endpoint.get("componentId") or "").strip()


def _build_reachability_graph(
    connections: list[dict],
    node_by_id: dict[str, dict] | None,
) -> dict[str, set[str]]:
    adjacency: dict[str, set[str]] = defaultdict(set)
    for connection in connections:
        source = _component_id(connection.get("source") or {})
        target = _component_id(connection.get("target") or {})
        if source and target:
            adjacency[source].add(target)

    for node_id, node in (node_by_id or {}).items():
        if (node.get("type") or "") != "jiuwen.loop":
            continue
        # Root IR represents loop internals with synthetic input/output nodes.
        # Treat the loop node as reaching its input so loop-body paths are
        # visible when filtering cyclic incoming edges.
        adjacency[node_id].add(f"{node_id}_input")

    return adjacency


def _is_reachable(adjacency: dict[str, set[str]], start: str, target: str) -> bool:
    if not start or not target:
        return False

    visited = {start}
    queue = deque(adjacency.get(start, ()))
    while queue:
        node_id = queue.popleft()
        if node_id == target:
            return True
        if node_id in visited:
            continue
        visited.add(node_id)
        queue.extend(adjacency.get(node_id, ()))
    return False


def collect_parallel_join_nodes(
    connections: list[dict],
    node_by_id: dict[str, dict] | None = None,
) -> frozenset[str]:
    """Return node IDs that join two or more parallel branches.

    IR marks branch convergence with ``parallelBranchId`` on the connection
    target.  The old BPMN converter inserts an inclusive gateway there; the
    openjiuwen workflow needs ``wait_for_all=True`` on the same nodes.
    """
    adjacency = _build_reachability_graph(connections, node_by_id)
    groups: dict[str, dict[str, set[str]]] = defaultdict(lambda: defaultdict(set))
    for connection in connections:
        source_info = connection.get("source") or {}
        target_info = connection.get("target") or {}
        source = _component_id(source_info)
        target = _component_id(target_info)
        parallel_id = (target_info.get("parallelBranchId") or "").strip()
        if not source or not target or not parallel_id:
            continue
        if source.endswith("_input") or target.endswith("_output"):
            continue
        if _is_reachable(adjacency, target, source):
            continue
        groups[target][parallel_id].add(source)

    join_nodes: set[str] = set()
    for target, parallel_groups in groups.items():
        for sources in parallel_groups.values():
            if len(sources) >= 2:
                join_nodes.add(target)
    return frozenset(join_nodes)
