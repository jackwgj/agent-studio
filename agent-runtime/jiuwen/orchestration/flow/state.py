# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2025. All rights reserved.
"""
Defines component state and workflow state.
"""

from dataclasses import dataclass
from typing import Union

from SpiffWorkflow.bpmn.specs.bpmn_process_spec import BpmnProcessSpec
from jiuwen.orchestration import Invokable


@dataclass
class State:
    pass


@dataclass
class TraceState:
    invoke_id: str
    insight_invoke_map: any
    insight_on_invoke_map: any
    insight_invoke_maps: []
    insight_on_invoke_maps: []
    loop_span_dict: dict


@dataclass
class BpmnInstanceState:
    name: str
    id: str
    description: str
    nodes_info: dict[str, any]  # <node_id, node_info>
    nodes_states: dict[str, State]  # <node_id, node_state>
    binary_workflow: bytes
    loop_comp: dict
    workflow_json: dict  # 原始 IR
    trace_state: TraceState


@dataclass
class ExecutedWorkflowState(State):
    workflow_id: str
    workflow_name: str
    parent_workflow_id: str
    sub_workflow_states: dict[str, "WorkflowState"]
    runtime_context: dict[
        str, any
    ]  # 包含 WORKFLOW_EXECUTE_DEBUG_INFO，记录insight相关信息
    bpmn_workflow_state: BpmnInstanceState


@dataclass
class LazyWorkflowState(State):
    parent_workflow_id: str
    workflow_id: str
    workflow_name: str
    comp_id: str
    ir_path: str
    runtime_context: dict[str, any]


LAZY_WORKFLOW_STATE_TYPE_NAME = "lazy_workflow_state"
EXECUTED_WORKFLOW_STATE_TYPE_NAME = "executed_workflow_state"


@dataclass
class WorkflowState(State):
    type: str
    workflow_state: Union[ExecutedWorkflowState, LazyWorkflowState]


@dataclass
class NodeSpec:
    """
    Jiuwen 节点状态无关的属性
    """

    node_config: dict
    invokable: Invokable = None
    node_type: str = None


@dataclass
class WorkflowSpec:
    """
    Workflow 中状态无关的关键属性
    """

    workflow_id: str
    ir: dict
    node_spec: dict[str, NodeSpec]  # Jiuwen 节点可复用内容
    bpmn_spec: dict[
        str, BpmnProcessSpec
    ]  # SpiffWorkflow.bpmn.workflow.BpmnWorkflow 的无状态 spec
