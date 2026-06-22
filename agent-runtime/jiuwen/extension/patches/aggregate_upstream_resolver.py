# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

"""Resolve first-non-null aggregate upstream node ids from workflow IR / runtime spec."""

from __future__ import annotations

import re
from typing import Any, Iterable, Iterator

_AGGREGATE_IR_TYPES = frozenset(
    {
        "jiuwen.aggregation",
        "jiuwen.aggregate",
        "jiuwen.flowAggregate",
        "aggregate",
    }
)
_REF_NODE_ID_PATTERN = re.compile(r"\$\{([A-Za-z0-9_]+)\.")


def _iter_nested_values(value: Any) -> Iterator[Any]:
    if isinstance(value, dict):
        for item in value.values():
            yield from _iter_nested_values(item)
    elif isinstance(value, (list, tuple)):
        for item in value:
            yield from _iter_nested_values(item)
    else:
        yield value


def extract_node_ids_from_ref_text(text: str) -> set[str]:
    return set(_REF_NODE_ID_PATTERN.findall(text))


def extract_node_ids_from_ref_schema(schema: Any) -> set[str]:
    node_ids: set[str] = set()
    if schema is None:
        return node_ids
    for value in _iter_nested_values(schema):
        if isinstance(value, str):
            node_ids.update(extract_node_ids_from_ref_text(value))
    return node_ids


def extract_aggregate_upstream_node_ids_from_ir(components: Iterable[dict[str, Any]]) -> tuple[str, ...]:
    """Parse aggregate components from IR ``components`` list."""
    node_ids: set[str] = set()
    for component in components:
        if not isinstance(component, dict):
            continue
        comp_type = str(component.get("type") or "")
        if comp_type not in _AGGREGATE_IR_TYPES:
            continue
        inputs = component.get("inputs") or {}
        user_fields = inputs.get("userFields") if isinstance(inputs, dict) else None
        node_ids.update(extract_node_ids_from_ref_schema(user_fields))
    return tuple(sorted(node_ids))


def _workflow_graph_nodes(workflow_self: Any) -> dict[str, Any]:
    internal = getattr(workflow_self, "_internal", None)
    graph = getattr(internal, "_graph", None) if internal is not None else None
    get_nodes = getattr(graph, "get_nodes", None)
    if not callable(get_nodes):
        return {}
    nodes = get_nodes()
    return nodes if isinstance(nodes, dict) else {}


def _is_aggregate_executable(executable: Any) -> bool:
    component_type = getattr(executable, "component_type", None)
    if callable(component_type) and component_type() == "aggregate":
        return True
    conf = getattr(executable, "_conf", None)
    mode = getattr(conf, "mode", None)
    return mode == "first-non-null"


def resolve_aggregate_upstream_node_ids_from_workflow(workflow_self: Any) -> tuple[str, ...]:
    """Resolve upstream branch node ids for all aggregate components in a compiled workflow."""
    internal = getattr(workflow_self, "_internal", None)
    if internal is None:
        return ()

    spec = internal.config().spec
    comp_configs = getattr(spec, "comp_configs", None) or {}
    start_nodes = set(getattr(spec, "start_nodes", None) or [])
    graph_nodes = _workflow_graph_nodes(workflow_self)

    node_ids: set[str] = set()
    for comp_id, node_spec in comp_configs.items():
        vertex = graph_nodes.get(comp_id)
        executable = getattr(vertex, "_executable", None) if vertex is not None else None
        if not _is_aggregate_executable(executable):
            continue

        io_configs = getattr(node_spec, "io_configs", None)
        inputs_schema = getattr(io_configs, "inputs_schema", None) if io_configs else None
        if inputs_schema:
            for nid in extract_node_ids_from_ref_schema(inputs_schema):
                if nid in start_nodes:
                    continue
                node_ids.add(nid)

    return tuple(sorted(node_ids))


def get_aggregate_upstream_node_ids(workflow_self: Any) -> tuple[str, ...]:
    """Resolve aggregate upstream node ids for sub-workflow resume patch."""
    return resolve_aggregate_upstream_node_ids_from_workflow(workflow_self)
