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


def build_skill_activated(
    conversation_id: str,
    run_id: str,
    *,
    skill_id: str,
    name: str,
    version_id: str,
    agent_id: str | None = None,
    **kwargs,
) -> dict:
    data = {F.SKILL_ID: skill_id, F.NAME: name, F.VERSION_ID: version_id}
    if agent_id is not None:
        data[F.AGENT_ID] = agent_id
    return build_canonical_event(
        T.SKILL_ACTIVATED,
        conversation_id=conversation_id,
        run_id=run_id,
        data=data,
        **kwargs,
    )


def build_error(
    conversation_id: str,
    run_id: str,
    *,
    code: str | int,
    message: str,
    **kwargs,
) -> dict:
    return build_canonical_event(
        T.ERROR,
        conversation_id=conversation_id,
        run_id=run_id,
        data={"code": code, "message": message},
        **kwargs,
    )


def build_artifact(
    conversation_id: str,
    run_id: str,
    *,
    execution_id: str,
    object_key: str,
    file_name: str,
    size: int,
    media_type: str,
    checksum: str,
    **kwargs,
) -> dict:
    return build_canonical_event(
        T.ARTIFACT,
        conversation_id=conversation_id,
        run_id=run_id,
        data={
            F.EXECUTION_ID: execution_id,
            F.OBJECT_KEY: object_key,
            F.FILE_NAME: file_name,
            F.SIZE: size,
            F.MEDIA_TYPE: media_type,
            F.CHECKSUM: checksum,
        },
        **kwargs,
    )


def build_run_end(
    conversation_id: str,
    run_id: str,
    status: str = "success",
    *,
    text: str | None = None,
    **kwargs,
) -> dict:
    data: dict[str, Any] = dict(kwargs.pop("data", {}) or {})
    data["status"] = status
    if text:
        data["text"] = text
    return build_canonical_event(
        T.RUN_END,
        conversation_id=conversation_id,
        run_id=run_id,
        data=data,
        **kwargs,
    )


class CanonicalEventSequencer:
    """Order terminal events without delaying child runs or artifacts."""

    def __init__(self, root_run_id: str):
        self._root_run_id = root_run_id
        self._pending_root_end: dict | None = None
        self._terminated_run_ids: set[str] = set()

    def accept(self, event: dict) -> list[dict]:
        event_type = event.get(F.EVENT)
        run_id = event.get(F.RUN_ID) or event.get(F.DATA, {}).get(F.RUN_ID)
        if event_type not in {T.RUN_END.value, T.ERROR.value} or not run_id:
            return [event]
        if run_id in self._terminated_run_ids:
            return []

        if event_type == T.ERROR.value:
            if run_id == self._root_run_id:
                self._pending_root_end = None
            self._terminated_run_ids.add(run_id)
            return [event]

        status = event.get(F.DATA, {}).get("status", "success")
        if run_id == self._root_run_id and status == "success":
            if self._pending_root_end is None:
                self._pending_root_end = event
            return []

        if run_id == self._root_run_id:
            self._pending_root_end = None
        self._terminated_run_ids.add(run_id)
        return [event]

    def release_root_end(self) -> list[dict]:
        if self._pending_root_end is None or self._root_run_id in self._terminated_run_ids:
            return []
        event = self._pending_root_end
        self._pending_root_end = None
        self._terminated_run_ids.add(self._root_run_id)
        return [event]


def sse_line(event: dict) -> str:
    return f"data: {json.dumps(event, ensure_ascii=False)}\n\n"
