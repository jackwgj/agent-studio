"""Canonical conversation event models and builders."""

from __future__ import annotations

import json
import time
from dataclasses import dataclass, field
from typing import Any

from .types import ConversationEventField as F
from .types import ConversationEventType as T


@dataclass
class CanonicalStreamContext:
    run_id: str
    parent_run_id: str | None = None
    execution_type: str = "agent"
    agent_id: str | None = None
    accumulated_text: str = field(default="", init=False)
    answer_text: str = field(default="", init=False)

    @property
    def final_text(self) -> str:
        return self.answer_text or self.accumulated_text


def build_canonical_event(
    event: T | str,
    *,
    conversation_id: str,
    run_id: str,
    data: dict[str, Any] | None = None,
    parent_run_id: str | None = None,
    execution_type: str = "agent",
    index: int | None = None,
) -> dict:
    event_name = event.value if isinstance(event, T) else str(event)
    payload = {F.RUN_ID: run_id, F.PARENT_RUN_ID: parent_run_id, F.EXECUTION_TYPE: execution_type}
    payload.update(data or {})
    result = {
        F.EVENT: event_name,
        F.CONVERSATION_ID: conversation_id,
        F.DATA: payload,
        F.RUN_ID: run_id,
        F.PARENT_RUN_ID: parent_run_id,
        F.EXECUTION_TYPE: execution_type,
        F.CREATED_TIME: int(time.time() * 1000),
    }
    if index is not None:
        result[F.INDEX] = index
    return result


def build_run_start(conversation_id: str, run_id: str, **kwargs) -> dict:
    return build_canonical_event(T.RUN_START, conversation_id=conversation_id, run_id=run_id, **kwargs)


def build_message(conversation_id: str, run_id: str, delta: str, *, agent_id: str | None = None, **kwargs) -> dict:
    data = dict(kwargs.pop("data", {}) or {})
    data["delta"] = delta
    if agent_id is not None:
        data[F.AGENT_ID] = agent_id
    return build_canonical_event(T.MESSAGE, conversation_id=conversation_id, run_id=run_id, data=data, **kwargs)


def build_reasoning(conversation_id: str, run_id: str, content: str, *, agent_id: str | None = None, **kwargs) -> dict:
    data = dict(kwargs.pop("data", {}) or {})
    data["content"] = content
    if agent_id is not None:
        data[F.AGENT_ID] = agent_id
    return build_canonical_event(T.REASONING, conversation_id=conversation_id, run_id=run_id, data=data, **kwargs)


def build_tool_call(conversation_id: str, run_id: str, tool_id: str, tool_name: str, *, arguments: Any = None, agent_id: str | None = None, **kwargs) -> dict:
    data = dict(kwargs.pop("data", {}) or {})
    data.update({F.TOOL_ID: tool_id, F.TOOL_NAME: tool_name})
    if arguments is not None:
        data["arguments"] = arguments
    if agent_id is not None:
        data[F.AGENT_ID] = agent_id
    return build_canonical_event(T.TOOL_CALL, conversation_id=conversation_id, run_id=run_id, data=data, **kwargs)


def build_tool_result(conversation_id: str, run_id: str, tool_id: str, tool_name: str, result: Any = "", *, agent_id: str | None = None, **kwargs) -> dict:
    data = dict(kwargs.pop("data", {}) or {})
    data.update({F.TOOL_ID: tool_id, F.TOOL_NAME: tool_name, "result": result})
    if agent_id is not None:
        data[F.AGENT_ID] = agent_id
    return build_canonical_event(T.TOOL_RESULT, conversation_id=conversation_id, run_id=run_id, data=data, **kwargs)


def build_workflow_node(conversation_id: str, run_id: str, data: dict[str, Any], **kwargs) -> dict:
    return build_canonical_event(T.WORKFLOW_NODE, conversation_id=conversation_id, run_id=run_id, data=data, **kwargs)


def build_run_end(
    conversation_id: str,
    run_id: str,
    status: str = "success",
    *,
    text: str | None = None,
    **kwargs,
) -> dict:
    data: dict[str, Any] = {"status": status}
    if text:
        data["text"] = text
    return build_canonical_event(
        T.RUN_END,
        conversation_id=conversation_id,
        run_id=run_id,
        data=data,
        **kwargs,
    )


def sse_line(event: dict) -> str:
    return f"data: {json.dumps(event, ensure_ascii=False)}\n\n"
