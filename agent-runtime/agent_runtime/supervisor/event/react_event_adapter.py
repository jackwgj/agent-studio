"""ReAct runner adapter for canonical conversation events."""
from __future__ import annotations
from typing import Any
from .canonical import CanonicalStreamContext, build_message, build_reasoning, build_tool_call, build_tool_result, build_run_end
from .types import ConversationEventType as T


def adapt_react_chunk(chunk: Any, ctx: CanonicalStreamContext, conversation_id: str) -> list[dict]:
    payload = getattr(chunk, "payload", {}) or {}
    kind = getattr(chunk, "type", "")
    if kind == "llm_output" and payload.get("content"):
        text = str(payload["content"]); ctx.accumulated_text += text
        return [build_message(conversation_id, ctx.run_id, text, parent_run_id=ctx.parent_run_id, execution_type=ctx.execution_type, data={"agentId": ctx.agent_id} if ctx.agent_id else None)]
    if kind == "llm_reasoning" and payload.get("content"):
        return [build_reasoning(conversation_id, ctx.run_id, str(payload["content"]), parent_run_id=ctx.parent_run_id, execution_type=ctx.execution_type, data={"agentId": ctx.agent_id} if ctx.agent_id else None)]
    if kind == "llm_usage":
        return []
    if kind == "answer":
        if payload.get("result_type") == "error":
            return []
        ctx.answer_text = str(payload.get("output") or "")
    return []


def adapt_react_end(ctx: CanonicalStreamContext, conversation_id: str) -> dict:
    return build_run_end(conversation_id, ctx.run_id, parent_run_id=ctx.parent_run_id, execution_type=ctx.execution_type)
