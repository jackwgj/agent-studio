# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2026. All rights reserved.

"""
FlowQA - 问答节点组件
"""

from __future__ import annotations

import random
import re
from copy import deepcopy
from dataclasses import dataclass, field
from enum import Enum
from typing import Dict, List, Optional, Union

from openjiuwen.core.common.constants.constant import USER_FIELDS
from openjiuwen.core.common.exception.codes import StatusCode
from openjiuwen.core.common.exception.errors import build_error
from openjiuwen.core.common.logging import LogEventType, workflow_logger
from openjiuwen.core.context_engine import ModelContext
from openjiuwen.core.graph.executable import Input, Output
from openjiuwen.core.graph.pregel import GraphInterrupt
from openjiuwen.core.session.node import Session
from openjiuwen.core.session.stream import CustomSchema
from openjiuwen.core.workflow.components.component import WorkflowComponent
from openjiuwen.core.workflow.components.flow.end_comp import TemplateUtils
from pydantic import BaseModel, ConfigDict, Field

PARTIAL_CONTENT = "partial_content"
MESSAGE_NODE_END = "message_end"
WORKFLOW_CHAT_HISTORY = "workflow_chat_history"
JIUWEN_QA_TYPE = "EI.qa"


class QAStrategy(Enum):
    random = "random"
    index = "index"


class ExecutionStatus(Enum):
    START = "start"
    USER_INTERACT = "user_interact"
    END = "end"


@dataclass
class QAState:
    status: ExecutionStatus = ExecutionStatus.START
    inputs: dict = field(default_factory=dict)
    question: str = ""


class QAConfig(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    name: Optional[str] = Field(default=None, description="Node name.")
    needReply: bool = Field(default=False, description="Whether need user reply.")
    qaStrategy: str = Field(
        default="random", description="QA strategy: random or index."
    )
    options: List[str] = Field(default_factory=list, description="QA options.")
    struct_output_template: str = Field(
        default="", description="Structured output template."
    )
    isStructMessage: bool = Field(
        default=False, description="Enable structured message."
    )
    enable_history: bool = Field(default=True, description="Enable history write.")
    index_key: str = Field(default="index", description="Key for index strategy.")


@dataclass
class FlowQAConfig:
    name: Optional[str] = None
    need_reply: bool = False
    qa_strategy: str = "random"
    options: List[str] = field(default_factory=list)
    struct_output_template: str = ""
    enable_struct_message: bool = False
    enable_history: bool = True
    index_key: str = "index"


class FlowQA(WorkflowComponent):
    """工作流问答节点

    功能:
    1. 根据配置的话术选项向用户展示问题
    2. 支持随机或索引两种问答策略
    3. 支持中断等待用户回复
    4. 支持会话历史管理
    5. 支持结构化信息输出
    """

    def __init__(self, conf: Union[FlowQAConfig, dict, None] = None):
        super().__init__()
        if not conf:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="flow_qa",
                reason="conf is required",
                workflow="n/a",
            )
        try:
            if isinstance(conf, dict):
                self._conf = FlowQAConfig(
                    name=conf.get("name"),
                    need_reply=conf.get("needReply", False),
                    qa_strategy=conf.get("qaStrategy", "random"),
                    options=conf.get("options", []),
                    struct_output_template=conf.get("struct_output_template", ""),
                    enable_struct_message=conf.get("isStructMessage", False),
                    enable_history=conf.get("enable_history", True),
                    index_key=conf.get("index_key", "index"),
                )
            else:
                self._conf = conf
        except Exception as e:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="flow_qa",
                reason=str(e),
                workflow="n/a",
                cause=e,
            )

        self._node_state = QAState()
        self._node_name: Optional[str] = self._conf.name
        self._stream_output: Optional[dict] = None

        self._qa_state_key = "flow_qa_state"

    def component_type(self) -> str:
        return JIUWEN_QA_TYPE

    @staticmethod
    def get_latest_k_rounds_chat(chat_history: list, k: int) -> list:
        if k is None:
            return chat_history
        return chat_history[-k * 2 - 1 :]

    def get_latest_chat_history(self, session: Session, k: int = None) -> List[Dict]:
        chat_history = None
        if hasattr(session, "get_global_state"):
            chat_history = session.get_global_state(WORKFLOW_CHAT_HISTORY)

        # 先使用工作流历史
        # if chat_history:
        #     if hasattr(chat_history, "get_conversation_history"):
        #         chat_history = chat_history.get_conversation_history()
        #     else:
        #         chat_history = []
        # else:
        #     chat_history = []
        if not chat_history:
            chat_history = []

        chat_history = self.get_latest_k_rounds_chat(chat_history, k)
        for history in chat_history:
            history.update({"content": history.get("content")})
        return chat_history

    def get_state(self) -> QAState:
        return self._node_state

    def load_state(self, state: QAState) -> None:
        self._node_state = deepcopy(state)

    def get_node_status(self) -> ExecutionStatus:
        return self._node_state.status

    def get_stream_output(self):
        return self._stream_output

    def error_to_output(self) -> None:
        self._node_state.status = ExecutionStatus.END

    @staticmethod
    def _load_state_from_session(session: Session) -> QAState:
        state_dict = None
        if hasattr(session, "get_state"):
            session_state = session.get_state()
            if isinstance(session_state, dict):
                state_dict = session_state.get("flow_qa_state")
        if state_dict:
            return QAState(
                status=ExecutionStatus(state_dict.get("status", "start")),
                inputs=state_dict.get("inputs", {}),
                question=state_dict.get("question", ""),
            )
        return QAState()

    @staticmethod
    def _store_state_to_session(state: QAState, session: Session):
        state_dict = {
            "status": state.status.value,
            "inputs": state.inputs,
            "question": state.question,
        }
        if hasattr(session, "update_state"):
            session.update_state({"flow_qa_state": state_dict})

    def should_interrupt(self) -> bool:
        return self._node_state.status == ExecutionStatus.USER_INTERACT

    def get_latest_user_query_intent(self, session: Session) -> Optional[str]:
        chat_history = None
        if hasattr(session, "get_global_state"):
            chat_history = session.get_global_state(WORKFLOW_CHAT_HISTORY)

        # 先使用工作流历史
        # if chat_history:
        #     if hasattr(chat_history, "get_conversation_history"):
        #         chat_history = chat_history.get_conversation_history()
        #     else:
        #         return None
        # else:
        #     return None

        for chat in reversed(chat_history):
            if chat.get("role") == "user":
                return chat.get("intent")

        return None

    def _process_values_of_dict(self, origin_template: str, inputs: dict) -> str:
        """
        处理占位符模板，进行参数替换。
        支持 {{param}} 格式的占位符。
        """
        pattern = re.compile(r"(\{\{.*?\}\})")
        response_list = pattern.split(origin_template)

        response = ""
        for res in response_list:
            if res.startswith("{{") and res.endswith("}}"):
                param_name = res[2:-2]
                if param_name not in inputs:
                    raise build_error(
                        StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                        comp="flow_qa",
                        reason=f"QA parameter key not found: {param_name}",
                        workflow="n/a",
                    )
                param_value = inputs.get(param_name)
                response += "" if param_value is None else str(param_value)
            else:
                response += res
        return response

    def _validate_options(self, options: List[str], inputs: dict) -> List[str]:
        """
        检查options中是否有占位符，同时进行替换。
        """
        return [self._process_values_of_dict(option, inputs) for option in options]

    def _get_qa_by_strategy(self, inputs: dict) -> str:
        strategy = self._conf.qa_strategy
        options = self._conf.options

        options = self._validate_options(options, inputs)

        if not options:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                comp="flow_qa",
                reason="QA options is empty",
                workflow="n/a",
            )

        if strategy not in [QAStrategy.random.value, QAStrategy.index.value]:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                comp="flow_qa",
                reason=f"Unsupported QA strategy: {strategy}",
                workflow="n/a",
            )

        if strategy == QAStrategy.random.value:
            return random.choice(options)
        else:
            index = int(inputs.get(self._conf.index_key, 0))
            if index >= len(options):
                raise build_error(
                    StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                    comp="flow_qa",
                    reason=f"QA index {index} out of range",
                    workflow="n/a",
                )
            return options[index]

    async def _write_interaction_stream(
        self,
        session: Session,
        answer: str,
        node_id: str,
        need_reply: bool = True,
    ) -> None:
        stream_related_info = {
            "answer": answer,
            "result": answer,
            "node_id": node_id,
            "node_name": self._node_name or node_id,
            "node_type": JIUWEN_QA_TYPE,
            "should_interrupt": True,
            "enable_history": self._conf.enable_history,
            "need_reply": need_reply,
        }

        if self._conf.enable_struct_message and self._conf.struct_output_template:
            struct_answer = TemplateUtils.render_template(
                self._conf.struct_output_template,
                {"_NODE_OUTPUT": answer, **self._node_state.inputs},
            )
            stream_related_info["answer"] = struct_answer  # 结构化输出作为 answer
            stream_related_info["origin_answer"] = answer  # 原始文本输出
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
                "FlowQA component invoke started",
                event_type=LogEventType.WORKFLOW_COMPONENT_START,
                component_id=session.get_component_id(),
                component_type_str=JIUWEN_QA_TYPE,
                session_id=session.get_session_id(),
            )

            state_from_session = self._load_state_from_session(session)
            has_saved_state = state_from_session.status != ExecutionStatus.START
            if has_saved_state:
                self._node_state = state_from_session
            else:
                self._node_state = QAState()

            node_id = session.get_component_id()
            node_name = self._node_name or node_id
            user_fields = inputs.get(USER_FIELDS, inputs) if inputs else {}

            if self._conf.need_reply:
                # 中断结束重新开始
                if self._node_state.status == ExecutionStatus.END:
                    self._node_state = QAState()

                if self._node_state.status == ExecutionStatus.START:
                    query = self._get_qa_by_strategy(user_fields)
                    # 先设置 inputs，再调用 _write_interaction_stream（因为模板渲染依赖 inputs）
                    self._node_state.inputs = user_fields
                    await self._write_interaction_stream(session, query, node_id, True)

                    self._node_state.question = query
                    self._node_state.status = ExecutionStatus.USER_INTERACT
                    self._store_state_to_session(self._node_state, session)

                    self._stream_output = {
                        "answer": query,
                        "node_id": node_id,
                        "node_name": node_name,
                        "node_type": JIUWEN_QA_TYPE,
                        "should_interrupt": True,
                    }

                    await session.interact(query)

                elif self._node_state.status == ExecutionStatus.USER_INTERACT:
                    # 恢复执行：通过 interact() 获取用户输入
                    user_response = await session.interact(
                        self._node_state.question or ""
                    )

                    self._node_state.status = ExecutionStatus.END
                    self._store_state_to_session(self._node_state, session)

                    return {USER_FIELDS: {"response": user_response}}
            else:
                self._node_state.status = ExecutionStatus.END
                query = self._get_qa_by_strategy(user_fields)
                # 先设置 inputs，再调用 _write_interaction_stream（因为模板渲染依赖 inputs）
                self._node_state.inputs = user_fields
                await self._write_interaction_stream(session, query, node_id, False)

                self._store_state_to_session(self._node_state, session)

                return {USER_FIELDS: {"response": query}}

            workflow_logger.debug(
                "FlowQA component invoke succeeded",
                event_type=LogEventType.WORKFLOW_COMPONENT_END,
                component_id=session.get_component_id(),
                component_type_str=JIUWEN_QA_TYPE,
                session_id=session.get_session_id(),
            )

        except GraphInterrupt:
            raise
        except Exception as e:
            workflow_logger.error(
                "FlowQA invoke failed",
                event_type=LogEventType.WORKFLOW_COMPONENT_ERROR,
                component_id=session.get_component_id(),
                component_type_str=JIUWEN_QA_TYPE,
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
        return JIUWEN_QA_TYPE

    @property
    def config(self) -> FlowQAConfig:
        return self._conf


__all__ = ["FlowQA", "FlowQAConfig", "QAState", "ExecutionStatus", "QAStrategy"]
