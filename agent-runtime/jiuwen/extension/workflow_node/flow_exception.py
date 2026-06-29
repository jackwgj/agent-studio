# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
ExceptionInfo - 异常结束组件

迁移自商用版本 ExceptionInfo，适配开源版本框架。

功能特性:
- 到达该节点时，直接抛出 WorkflowAbortException 终止工作流
- 携带用户自定义字段 (userFields) 作为异常数据
- 支持节点元信息 (node_id, node_name, node_type)

设计说明:
- JiuWenBaseException 继承 openjiuwen ExecutionError，复刻原 jiuwen JiuWenBaseException 接口
- WorkflowAbortException 继承 JiuWenBaseException，与原实现一致
- ExceptionInfo 继承 WorkflowComponent，实现 invoke 方法
"""

from typing import AsyncIterator

from jiuwen.extension.workflow_node.utils import WorkflowAbortException
from openjiuwen.core.context_engine import ModelContext
from openjiuwen.core.graph.executable import Input, Output
from openjiuwen.core.session.node import Session
from openjiuwen.core.session.stream.base import CustomSchema
from openjiuwen.core.workflow.components.component import WorkflowComponent

MAGIC_CODE = "\t"
USER_FIELDS = "userFields"
WORKFLOW_EXCEPTION = "workflow_exception"
WORKFLOW_FINAL = "workflow_final"


class ExceptionInfo(WorkflowComponent):
    """异常结束组件。

    到达该节点时，直接抛出 WorkflowAbortException 终止工作流。
    从输入中提取 userFields 作为异常数据传递给上层。

    Args:
        node_id: 节点 ID。
        node_name: 节点名称。
        node_type: 节点类型。
    """

    def __init__(self, node_id: str, node_name: str, node_type: str) -> None:
        super().__init__()
        self.node_id = node_id
        self.node_name = node_name
        self.node_type = node_type

    async def invoke(self, inputs: Input, session: Session, context: ModelContext) -> Output:
        """执行异常结束逻辑，通过 session.write_custom_stream() 输出 workflow_exception，
        随后抛出 WorkflowAbortException 终止工作流。

        通过 session global_state 中的 __abort__ 标志实现互斥：
        当多个异常结束节点并行执行时，仅第一个发送 WORKFLOW_EXCEPTION，
        其余跳过直接抛异常。与旧框架 _pass_abort_exception_info 中
        workflow.data["__abort__"] 的行为一致。

        Args:
            inputs: 输入数据，预期包含 userFields 字段。
            session: 工作流会话。
            context: 模型上下文。
        """
        if inputs is None:
            user_fields = {}
        else:
            user_fields = inputs.get(USER_FIELDS) or {}

        # 互斥守卫：检查是否已有其他异常结束节点触发。
        # get_global_state / update_global_state 均为同步方法，
        # 之间无 await，check-and-set 在 asyncio 协作式调度下是原子的。
        if session.get_global_state("__abort__") is not None:
            raise WorkflowAbortException(
                user_fields, self.node_id, self.node_name, self.node_type
            )
        session.update_global_state({"__abort__": True})
        # 将异常数据写入 tracer 的 onInvokeData，
        # 确保调试信息中包含异常节点的 error_code 和 jiuwen_exception_node_id，
        # 与旧版 _on_execution_error_debug_info 中 CallbackHandlerManager().trigger_event 行为一致。
        trace_data = {**user_fields, "jiuwen_exception_node_id": self.node_id}
        await session.trace(trace_data)
        await session.write_custom_stream(
            CustomSchema(
                type=WORKFLOW_EXCEPTION,
                index=0,
                data={
                    **user_fields,
                    "jiuwen_exception_node_id": self.node_id,
                },
            )
        )
        raise WorkflowAbortException(
            user_fields, self.node_id, self.node_name, self.node_type
        )

    async def stream(
        self, inputs: Input, session: Session, context: ModelContext
    ) -> AsyncIterator[Output]:
        """执行异常结束逻辑（流式），通过 session.write_custom_stream() 输出 workflow_exception。

        Args:
            inputs: 输入数据，预期包含 userFields 字段。
            session: 工作流会话。
            context: 模型上下文。
        """
        if inputs is None:
            user_fields = {}
        else:
            user_fields = inputs.get(USER_FIELDS) or {}
        # 将异常数据写入 tracer 的 onInvokeData（同 invoke 方法）
        trace_data = {**user_fields, "jiuwen_exception_node_id": self.node_id}
        await session.trace(trace_data)
        await session.write_custom_stream(
            CustomSchema(
                type=WORKFLOW_EXCEPTION,
                index=0,
                data={
                    **user_fields,
                    "jiuwen_exception_node_id": self.node_id,
                },
            )
        )
        yield {}
        raise WorkflowAbortException(
            user_fields, self.node_id, self.node_name, self.node_type
        )
