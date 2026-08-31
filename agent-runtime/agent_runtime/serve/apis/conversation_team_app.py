"""Conversation workspace adapter for a user-selected application."""

from __future__ import annotations

import json
from collections.abc import AsyncGenerator
from typing import Any

from agent_runtime.conversation.execution_context import (
    get_conversation_execution_context,
)
from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionParams, ExecutionRequest
from agent_runtime.serve.apis.orchestration import prepare_params
from agent_runtime.supervisor.event.conversation_adapter import adapt_runner_event
from jiuwen.serve.controllers.execution.open_utils import async_ir_load


_conversation_runner_factory = ConversationRunnerFactory()


def _event_payload(raw: Any) -> dict | None:
    """Parse a runner event dict or one SSE data frame."""
    if isinstance(raw, dict):
        return raw
    if isinstance(raw, (bytes, bytearray)):
        raw = bytes(raw).decode("utf-8", errors="replace")
    if isinstance(raw, str) and raw.startswith("data: "):
        try:
            payload = json.loads(raw[6:].strip())
        except json.JSONDecodeError:
            return None
        return payload if isinstance(payload, dict) else None
    return None


def _skill_context_variables(req: Any) -> dict:
    """Build the request-local Skill catalog without mutating published APP IR."""
    catalog = [
        {
            "skillId": item.skill_id,
            "versionId": item.version_id,
            "name": item.name,
            "description": item.description,
            "objectKey": item.object_key,
        }
        for item in getattr(req, "skill_catalog", None) or []
    ]
    return {
        "type": "APP",
        "skillCatalog": catalog,
        "recommendedSkillIds": list(
            getattr(req, "recommended_skill_ids", None) or []
        ),
    }


def _adapt_event(raw: Any, execution_id: str, conversation_id: str) -> dict | None:
    """Convert one ReAct/Controller frame to a canonical ConversationEvent."""
    return adapt_runner_event(
        raw,
        conversation_id=conversation_id,
        run_id=execution_id,
        index=None,
    )


async def stream_application(
    req: Any,
    execution_id: str,
    prepared_file_references: list[dict] | None = None,
) -> AsyncGenerator[dict, None]:
    """Execute the selected APP inside the active trusted conversation context."""
    execution_context = get_conversation_execution_context().for_child_call()
    identity = execution_context.identity
    execution_id = identity.execution_id
    ir_path = f"agent/ir/{req.app_id}/{req.app_id}.json"
    ir_data = await async_ir_load(ir_path)
    mode = (ir_data.get("configs") or {}).get("mode", "ReAct")
    if mode not in {"ReAct", "Controller", "PlanExecute"}:
        raise ValueError(f"unsupported conversation app mode: {mode}")

    history = req.conversation_history or []
    params = ExecutionParams(
        conversationHistory=history,
        globalVariables={
            "sys": {
                "conversationHistory": history,
                "conversationId": identity.conversation_id,
                "projectId": identity.project_id,
                "workspaceId": identity.workspace_id,
                "userId": identity.user_id,
                "executionId": identity.execution_id,
            },
            "conversationId": identity.conversation_id,
            "projectId": identity.project_id,
            "workspaceId": identity.workspace_id,
            "userId": identity.user_id,
            "executionId": identity.execution_id,
            "conversationInputFiles": list(prepared_file_references or []),
            "conversationTeam": _skill_context_variables(req),
        },
        pluginConfigs=[],
        toolSwitchDict={},
        isDebug=False,
    )
    execution_request = ExecutionRequest(
        conversationId=identity.conversation_id,
        userId=identity.user_id,
        irPath=ir_path,
        query=req.query,
        params=params,
        headers={},
    )
    execution_request.params = prepare_params(execution_request)
    runner = _conversation_runner_factory.get(mode)
    raw_stream = runner.run_streaming(execution_request, execution_id)
    try:
        async for raw in raw_stream:
            event = _adapt_event(raw, execution_id, identity.conversation_id)
            if event is not None:
                yield event
    finally:
        close = getattr(raw_stream, "aclose", None)
        if close is not None:
            await close()
