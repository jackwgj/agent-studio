"""会话工作台用户单/多智能体适配层。"""

from typing import Any, AsyncGenerator

import json

from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionParams, ExecutionRequest
from agent_runtime.serve.apis.orchestration import prepare_params
from agent_runtime.supervisor.event.conversation_adapter import adapt_runner_event
from jiuwen.serve.controllers.execution.open_utils import async_ir_load

_conversation_runner_factory = ConversationRunnerFactory()


def _event_payload(raw: Any) -> dict | None:
    """Parse a runner event dict or an SSE data frame."""
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
    """Build request-local Skill variables shared by conversation runners."""
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
        "recommendedSkillIds": list(getattr(req, "recommended_skill_ids", None) or []),
    }


def _adapt_event(raw: Any, execution_id: str, conversation_id: str) -> dict | None:
    """Convert one ReAct/Controller frame to a ConversationEvent."""
    return adapt_runner_event(
        raw,
        conversation_id=conversation_id,
        run_id=execution_id,
        index=None,
    )


async def stream_application(
    req: Any,
    execution_id: str,
) -> AsyncGenerator[dict, None]:
    """Execute an APP and output canonical ConversationEvents."""
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
                "conversationId": req.conversation_id,
                "userId": req.user_id,
            },
            "conversationId": req.conversation_id,
            "userId": req.user_id,
            "conversationTeam": _skill_context_variables(req),
        },
        pluginConfigs=[],
        toolSwitchDict={},
        isDebug=False,
    )
    execution_request = ExecutionRequest(
        conversationId=req.conversation_id,
        userId=req.user_id,
        irPath=ir_path,
        query=req.query,
        params=params,
        headers={},
    )
    execution_request.params = prepare_params(execution_request)
    runner = _conversation_runner_factory.get(mode)
    async for raw in runner.run_streaming(execution_request, execution_id):
        event = _adapt_event(raw, execution_id, req.conversation_id)
        if event is not None:
            yield event
