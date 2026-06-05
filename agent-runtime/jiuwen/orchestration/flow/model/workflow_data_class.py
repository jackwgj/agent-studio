#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
from dataclasses import dataclass, field

import jiuwen.orchestration.flow.state
from jiuwen.orchestration.flow.state import State
from jiuwen.orchestration.flow.stream.workflow_streaming import WorkflowStreamCallback


@dataclass
class WorkflowMetadata:
    """
    workflow 基础元数据类
    """

    node_id: str
    node_type: str
    node_name: str
    should_interrupt: bool = False
    node_state: State = field(default_factory=jiuwen.orchestration.flow.state.State)


@dataclass
class WorkflowInvokeOutput:
    """
    Defines the returning data structure of WorkflowInstance invoke().
    """

    data: dict
    message_list: list
    card_list: list
    execution_id: str


@dataclass
class WorkflowStreamingData:
    """
    workflow 流式相关数据
    """

    stream_callback: WorkflowStreamCallback
    execution_id: str
