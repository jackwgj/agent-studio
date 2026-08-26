"""Adapters from conversation runner frames to canonical ConversationEvents."""

from __future__ import annotations

import json
import time
from typing import Any

from .types import ConversationEventField as F
from .types import ConversationEventType as T


def _payload(raw: Any) -> dict | None:
    if isinstance(raw, dict):
        return raw
    if isinstance(raw, (bytes, bytearray)):
        text = bytes(raw).decode("utf-8", errors="replace")
        if text.startswith("data: "):
            try:
                value = json.loads(text[6:].strip())
            except json.JSONDecodeError:
                return None
            return value if isinstance(value, dict) else None
    return None


def _data(value: Any) -> dict:
    return value if isinstance(value, dict) else {"content": str(value)}


def _answer(data: dict) -> dict:
    value = data.get("answer")
    return value if isinstance(value, dict) else data


def _text(data: dict) -> str:
    for key in ("delta", "content", "text", "output", "answer"):
        value = data.get(key)
        if isinstance(value, str) and value:
            return value
    return ""


def adapt_runner_event(
    raw: Any,
    *,
    conversation_id: str,
    run_id: str,
    parent_run_id: str | None = None,
    execution_type: str = "agent",
    index: int | None = None,
) -> dict | None:
    """Convert one runner frame without changing runner execution semantics."""
    payload = _payload(raw)
    if not payload:
        return None
    raw_event = str(payload.get(F.EVENT, ""))
    data = _answer(_data(payload.get(F.DATA)))
    event: T | None = None
    canonical_run_id = payload.get(F.RUN_ID) or data.get(F.RUN_ID) or run_id
    canonical_parent_run_id = payload.get(F.PARENT_RUN_ID) or data.get(F.PARENT_RUN_ID) or parent_run_id
    canonical_execution_type = payload.get(F.EXECUTION_TYPE) or data.get(F.EXECUTION_TYPE) or execution_type
    event_data: dict[str, Any] = {
        F.RUN_ID: canonical_run_id,
        F.PARENT_RUN_ID: canonical_parent_run_id,
        F.EXECUTION_TYPE: canonical_execution_type,
    }
    for field in (F.AGENT_ID, F.WORKFLOW_ID, F.NODE_ID, F.CREATED_TIME):
        value = payload.get(field, data.get(field))
        if value is not None:
            event_data[field] = value

    if raw_event in {"message", "message_end"}:
        if data.get("isReasoning") or data.get("think"):
            event = T.REASONING
            event_data["content"] = data.get("think") or data.get("content") or ""
        else:
            event = T.MESSAGE
            event_data["delta"] = _text(data)
    elif raw_event in {"function_call", "pe_function_call"}:
        call = data.get("function_call") or data.get("functionCall") or data
        if not isinstance(call, dict):
            return None
        tool_id = call.get("tool_call_id") or call.get("toolCallId")
        if not tool_id:
            return None
        arguments = call.get("arguments")
        if isinstance(arguments, str):
            try:
                arguments = json.loads(arguments)
            except json.JSONDecodeError:
                arguments = {"raw": arguments}
        event = T.TOOL_CALL
        event_data.update({F.TOOL_ID: str(tool_id), F.TOOL_NAME: str(call.get("name") or "tool")})
        if isinstance(arguments, dict):
            event_data["arguments"] = arguments
    elif raw_event in {"api_exec_data", "pe_api_exec_data", T.TOOL_RESULT.value}:
        tool_id = data.get("tool_call_id") or data.get("toolCallId") or data.get(F.TOOL_ID)
        if not tool_id:
            return None
        event = T.TOOL_RESULT
        event_data.update({F.TOOL_ID: str(tool_id), F.TOOL_NAME: str(data.get("name") or data.get("tool_name") or data.get(F.TOOL_NAME) or "tool"),
                           "status": str(data.get("status") or "success"),
                           "result": data.get("result") or data.get("content") or ""})
    elif raw_event in {"skill_activated", T.SKILL_ACTIVATED.value}:
        event = T.SKILL_ACTIVATED
        event_data.update({"skillId": data.get("skillId") or data.get("skill_id"),
                           "name": data.get("name"), "content": _text(data)})
    elif raw_event in {"usage", "llm_usage", T.USAGE.value}:
        event = T.USAGE
        event_data["usage"] = data.get("usage") or data.get("usage_metadata") or data
    elif raw_event in {"done", "task_complete", "run_end"}:
        event = T.RUN_END
        event_data.update({"status": str(data.get("status") or "success")})
        text = _text(data)
        if text:
            event_data["text"] = text
    elif raw_event in {"error", "exception", "task_terminated"}:
        event = T.ERROR
        event_data.update({"code": data.get("code") or raw_event, "message": str(data.get("message") or data.get("error") or data)})
    elif raw_event in {"workflow_node_message", "workflow_node"}:
        event = T.WORKFLOW_NODE
        event_data.update({F.NODE_ID: data.get("nodeId") or data.get("node_id"), "content": _text(data)})
    else:
        return None

    if event in {T.MESSAGE, T.REASONING} and not event_data.get("delta") and not event_data.get("content"):
        return None
    result = {F.EVENT: event.value, F.CONVERSATION_ID: conversation_id, F.DATA: event_data,
              F.RUN_ID: canonical_run_id, F.PARENT_RUN_ID: canonical_parent_run_id,
              F.EXECUTION_TYPE: canonical_execution_type,
              F.INDEX: payload.get(F.INDEX, index),
              F.CREATED_TIME: payload.get(F.CREATED_TIME) or int(__import__("time").time() * 1000)}
    if index is not None:
        result[F.INDEX] = index
    return result
