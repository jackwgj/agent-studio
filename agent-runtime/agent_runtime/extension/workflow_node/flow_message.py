# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2026. All rights reserved.

"""
FlowMessage - 消息节点组件
支持在工作流执行过程中输出中间消息。

功能:
1. 模板渲染：支持 {{variable}} 占位符替换
2. 变量引用：支持 ${node_id.field} 引用上游节点输出
3. 流式输出：通过 SSE 发送 partial_content 和 message_end 事件
4. 结构化输出：可选 struct_output_template
"""

from __future__ import annotations

import re
from copy import deepcopy
from dataclasses import dataclass, field
from enum import Enum
from typing import Optional, Union

from openjiuwen.core.common.constants.constant import USER_FIELDS
from openjiuwen.core.common.exception.codes import StatusCode
from openjiuwen.core.common.exception.errors import build_error
from openjiuwen.core.common.logging import LogEventType, workflow_logger
from openjiuwen.core.context_engine import ModelContext
from openjiuwen.core.graph.executable import Input, Output
from openjiuwen.core.session.node import Session
from openjiuwen.core.session.stream import CustomSchema
from openjiuwen.core.workflow.components.component import WorkflowComponent
from openjiuwen.core.workflow.components.flow.end_comp import TemplateUtils
from pydantic import BaseModel, ConfigDict, Field

PARTIAL_CONTENT = "partial_content"
MESSAGE_NODE_END = "message_end"
WORKFLOW_CHAT_HISTORY = "workflow_chat_history"
JIUWEN_MESSAGE_TYPE = "jiuwen.message"


class ExecutionStatus(Enum):
    START = "start"
    END = "end"


@dataclass
class MessageState:
    status: ExecutionStatus = ExecutionStatus.START
    inputs: dict = field(default_factory=dict)


class FlowMessageConfig(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    name: Optional[str] = Field(default=None, description="Node name.")
    template: str = Field(
        default="", description="Message template with {{variable}} placeholders."
    )
    struct_output_template: str = Field(
        default="", description="Structured output template."
    )
    isStructMessage: bool = Field(
        default=False, description="Enable structured message."
    )
    enable_history: bool = Field(default=True, description="Enable history write.")


class FlowMessage(WorkflowComponent):
    """工作流消息节点

    功能:
    1. 根据配置的模板向用户展示消息
    2. 支持变量引用和模板渲染
    3. 支持会话历史管理
    4. 支持结构化信息输出
    """

    def __init__(self, conf: Union[FlowMessageConfig, dict, None] = None):
        super().__init__()
        if not conf:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="flow_message",
                reason="conf is required",
                workflow="n/a",
            )
        try:
            if isinstance(conf, dict):
                self._conf = FlowMessageConfig(
                    name=conf.get("name"),
                    template=conf.get("template", ""),
                    struct_output_template=conf.get("struct_output_template", ""),
                    isStructMessage=conf.get("isStructMessage", False),
                    enable_history=conf.get("enable_history", True),
                )
            else:
                self._conf = conf
        except Exception as e:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="flow_message",
                reason=str(e),
                workflow="n/a",
                cause=e,
            )

        self._node_state = MessageState()
        self._node_name: Optional[str] = self._conf.name
        self._stream_output: Optional[dict] = None

        self._message_state_key = "flow_message_state"

    def component_type(self) -> str:
        return JIUWEN_MESSAGE_TYPE

    @staticmethod
    def render_template(template: str, inputs: dict) -> str:
        """处理占位符模板，进行参数替换。

        支持 {{param}} 格式的占位符。
        """
        pattern = re.compile(r"(\{\{.*?\}\})")
        response_list = pattern.split(template)

        response = ""
        for res in response_list:
            if res.startswith("{{") and res.endswith("}}"):
                param_name = res[2:-2]
                if param_name not in inputs:
                    raise build_error(
                        StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                        comp="flow_message",
                        reason=f"Message parameter key not found: {param_name}",
                        workflow="n/a",
                    )
                param_value = inputs.get(param_name)
                response += str(param_value)
            else:
                response += res
        return response

    def get_state(self) -> MessageState:
        return self._node_state

    def load_state(self, state: MessageState) -> None:
        self._node_state = deepcopy(state)

    def get_node_status(self) -> ExecutionStatus:
        return self._node_state.status

    def get_stream_output(self):
        return self._stream_output

    def error_to_output(self) -> None:
        self._node_state.status = ExecutionStatus.END

    @staticmethod
    def _load_state_from_session(session: Session) -> MessageState:
        state_dict = None
        if hasattr(session, "get_global_state"):
            state_dict = session.get_global_state("flow_message_state")
        if state_dict:
            return MessageState(
                status=ExecutionStatus(state_dict.get("status", "start")),
                inputs=state_dict.get("inputs", {}),
            )
        return MessageState()

    @staticmethod
    def _store_state_to_session(state: MessageState, session: Session):
        state_dict = {"status": state.status.value, "inputs": state.inputs}
        if hasattr(session, "update_global_state"):
            session.update_global_state({"flow_message_state": state_dict})

    async def _write_interaction_stream(
        self,
        session: Session,
        answer: str,
        node_id: str,
    ) -> None:
        stream_related_info = {
            "answer": answer,
            "result": answer,
            "node_id": node_id,
            "node_name": self._node_name or node_id,
            "node_type": JIUWEN_MESSAGE_TYPE,
            "should_interrupt": False,
            "enable_history": self._conf.enable_history,
            "need_reply": False,
        }

        if self._conf.isStructMessage and self._conf.struct_output_template:
            struct_answer = TemplateUtils.render_template(
                self._conf.struct_output_template,
                {"_NODE_OUTPUT": answer, **self._node_state.inputs},
            )
            stream_related_info["struct_answer"] = struct_answer
            stream_related_info["is_struct_message"] = True

        await session.write_custom_stream(
            CustomSchema(
                type=PARTIAL_CONTENT,
                index=0,
                data=stream_related_info,
            ).model_dump(by_alias=True, exclude_none=False, exclude_unset=False)
        )
        await session.write_custom_stream(
            CustomSchema(
                type=MESSAGE_NODE_END,
                index=1,
                data=stream_related_info,
            ).model_dump(by_alias=True, exclude_none=False, exclude_unset=False)
        )

    async def invoke(
        self, inputs: Input, session: Session, context: ModelContext
    ) -> Output:
        try:
            workflow_logger.debug(
                "FlowMessage component invoke started",
                event_type=LogEventType.WORKFLOW_COMPONENT_START,
                component_id=session.get_component_id(),
                component_type_str=JIUWEN_MESSAGE_TYPE,
                session_id=session.get_session_id(),
            )

            state_from_session = self._load_state_from_session(session)
            has_saved_state = state_from_session.status != ExecutionStatus.START
            if has_saved_state:
                self._node_state = state_from_session
            else:
                self._node_state = MessageState()

            node_id = session.get_component_id()
            node_name = self._node_name or node_id
            user_fields = inputs.get(USER_FIELDS, inputs) if inputs else {}

            self._node_state.status = ExecutionStatus.END

            rendered_message = self.render_template(self._conf.template, user_fields)

            if hasattr(session, 'trace'):
                await session.trace(data={"assistant": rendered_message})

            await self._write_interaction_stream(session, rendered_message, node_id)

            self._store_state_to_session(self._node_state, session)

            workflow_logger.debug(
                "FlowMessage component invoke succeeded",
                event_type=LogEventType.WORKFLOW_COMPONENT_END,
                component_id=session.get_component_id(),
                component_type_str=JIUWEN_MESSAGE_TYPE,
                session_id=session.get_session_id(),
            )

            return {USER_FIELDS: {"response": rendered_message}}

        except Exception as e:
            workflow_logger.error(
                "FlowMessage invoke failed",
                event_type=LogEventType.WORKFLOW_COMPONENT_ERROR,
                component_id=session.get_component_id(),
                component_type_str=JIUWEN_MESSAGE_TYPE,
                session_id=session.get_session_id(),
                exception=e,
            )
            self.error_to_output()
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                comp=session.get_component_id(),
                ability="invoke",
                reason=str(e),
                workflow=session.get_workflow_id(),
                cause=e,
            )

    @property
    def node_type(self) -> str:
        return JIUWEN_MESSAGE_TYPE

    @property
    def config(self) -> FlowMessageConfig:
        return self._conf


__all__ = ["FlowMessage", "FlowMessageConfig", "MessageState", "ExecutionStatus"]
