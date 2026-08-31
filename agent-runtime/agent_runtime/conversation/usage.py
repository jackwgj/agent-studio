"""Conversation LLM usage observation shared by ReAct and Controller runners."""

from __future__ import annotations

import contextvars
import json
import logging
import uuid
from dataclasses import dataclass, field
from math import ceil
from typing import Any

from openjiuwen.core.single_agent.rail.base import AgentRail


@dataclass(frozen=True)
class UsageRecord:
    invocation_id: str
    run_id: str
    parent_run_id: str | None
    execution_type: str
    input_tokens: int
    output_tokens: int
    total_tokens: int


@dataclass
class ConversationUsageObserver:
    """Request-local collector for usage from every LLM call in an execution tree."""

    execution_id: str
    records: dict[str, UsageRecord] = field(default_factory=dict)
    _pending: list[UsageRecord] = field(default_factory=list)

    def record(
        self,
        *,
        invocation_id: str | None,
        run_id: str | None,
        parent_run_id: str | None = None,
        execution_type: str = "agent",
        usage: Any,
    ) -> UsageRecord | None:
        """Read native usage metadata once; never derive tokens from text."""
        if not invocation_id or not usage or invocation_id in self.records:
            return self.records.get(invocation_id) if invocation_id else None
        values = usage.model_dump() if hasattr(usage, "model_dump") else usage
        if hasattr(values, "dict") and callable(values.dict):
            values = values.dict()
        if not isinstance(values, dict):
            return None
        record = UsageRecord(
            invocation_id=str(invocation_id),
            run_id=str(run_id or self.execution_id),
            parent_run_id=str(parent_run_id) if parent_run_id else None,
            execution_type=str(execution_type or "agent"),
            input_tokens=int(values.get("input_tokens") or values.get("prompt_tokens") or 0),
            output_tokens=int(values.get("output_tokens") or values.get("completion_tokens") or 0),
            total_tokens=int(values.get("total_tokens") or 0),
        )
        if record.total_tokens <= 0 and record.input_tokens <= 0 and record.output_tokens <= 0:
            return None
        self.records[record.invocation_id] = record
        self._pending.append(record)
        return record

    def drain_events(self, conversation_id: str) -> list[dict]:
        """Return newly observed usage records as canonical conversation events."""
        pending, self._pending = self._pending, []
        return [
            {
                "event": "usage",
                "conversationId": conversation_id,
                "runId": record.run_id,
                "parentRunId": record.parent_run_id,
                "executionType": record.execution_type,
                "data": {
                    "runId": record.run_id,
                    "parentRunId": record.parent_run_id,
                    "executionType": record.execution_type,
                    "invocationId": record.invocation_id,
                    "usage": {
                        "input_tokens": record.input_tokens,
                        "output_tokens": record.output_tokens,
                        "total_tokens": record.total_tokens,
                    },
                },
            }
            for record in pending
        ]


_current_observer: contextvars.ContextVar[ConversationUsageObserver | None] = contextvars.ContextVar(
    "conversation_usage_observer", default=None
)
def get_or_create_observer(execution_id: str) -> ConversationUsageObserver:
    observer = _current_observer.get()
    if observer is None:
        observer = ConversationUsageObserver(execution_id)
        _current_observer.set(observer)
    return observer

class TokenEstimateAccumulator:
    """Collect visible canonical event content for one execution-level estimate."""

    _IGNORED_EVENTS = {"run_start", "run_end", "error", "usage", "artifact"}

    def __init__(self, execution_id: str):
        self.execution_id = execution_id
        self._input_parts: list[str] = []
        self._output_parts: list[str] = []
        self._seen_snapshots: set[str] = set()
        self.native_usage_seen = False

    @staticmethod
    def _text(value: Any) -> str:
        if value is None:
            return ""
        if isinstance(value, str):
            return value
        if isinstance(value, (dict, list, tuple)):
            return json.dumps(value, ensure_ascii=False, sort_keys=True)
        return str(value)

    def add_input(self, value: Any) -> None:
        self._add_unique(self._input_parts, value)

    def add(self, event: dict | None) -> None:
        if not isinstance(event, dict):
            return
        event_name = str(event.get("event") or "")
        data = event.get("data") or {}
        if not isinstance(data, dict):
            data = {"content": data}
        if event_name == "usage":
            usage = data.get("usage") or data.get("usage_metadata") or data
            if isinstance(usage, dict) and any(
                int(usage.get(key) or 0) > 0
                for key in ("input_tokens", "output_tokens", "total_tokens")
            ):
                self.native_usage_seen = True
            return
        if event_name in self._IGNORED_EVENTS:
            return

        if event_name == "message" and data.get("role") == "user":
            return
        if event_name in {"message", "reasoning", "workflow_node"}:
            text = data.get("delta") or data.get("content") or data.get("text")
            self._add_unique(self._output_parts, text)
        elif event_name == "tool_call":
            self._add_unique(self._output_parts, data.get("toolName"))
            self._add_unique(self._output_parts, data.get("arguments"))
        elif event_name == "tool_result":
            self._add_unique(self._input_parts, data.get("result") or data.get("content"))
        else:
            self._add_unique(self._output_parts, data.get("content") or data.get("text"))

    def _add_unique(self, target: list[str], value: Any) -> None:
        text = self._text(value)
        if not text or text in self._seen_snapshots:
            return
        self._seen_snapshots.add(text)
        target.append(text)

    def finalize(self, conversation_id: str) -> dict | None:
        if self.native_usage_seen:
            return None
        input_tokens = ceil(len("\n".join(self._input_parts)) / 2)
        output_tokens = ceil(len("\n".join(self._output_parts)) / 2)
        return {
            "event": "usage",
            "conversationId": conversation_id,
            "runId": self.execution_id,
            "data": {
                "runId": self.execution_id,
                "invocationId": f"estimated_{self.execution_id}",
                "usage": {
                    "input_tokens": input_tokens,
                    "output_tokens": output_tokens,
                    "total_tokens": input_tokens + output_tokens,
                },
            },
        }


class ConversationUsageRail(AgentRail):
    """Adapter from official ReAct AgentRail callbacks to the shared observer."""

    def __init__(self, observer: ConversationUsageObserver, run_id: str, parent_run_id: str | None = None,
                 execution_type: str = "agent"):
        self.observer = observer
        self.run_id = run_id
        self.parent_run_id = parent_run_id
        self.execution_type = execution_type
        self._invocation_id: str | None = None

    async def before_model_call(self, ctx) -> None:
        self._invocation_id = str(uuid.uuid4())
        ctx.extra["conversation_usage_invocation_id"] = self._invocation_id

    async def after_model_call(self, ctx) -> None:
        response = getattr(ctx.inputs, "response", None)
        self.observer.record(
            invocation_id=ctx.extra.get("conversation_usage_invocation_id") or self._invocation_id,
            run_id=self.run_id,
            parent_run_id=self.parent_run_id,
            execution_type=self.execution_type,
            usage=getattr(response, "usage_metadata", None),
        )

    async def on_model_exception(self, ctx) -> None:
        return None
