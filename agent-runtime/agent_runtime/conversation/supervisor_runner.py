"""Supervisor main-run bridge for the conversation runner path.

The legacy supervisor runner remains available. This bridge keeps its
request-local EventChannel and cleanup semantics while consuming the standard
ConversationReActRunner stream for the main agent.
"""

from __future__ import annotations

import asyncio
import sys

from agent_runtime.conversation.execution_context import (
    get_conversation_execution_context,
)
from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionParams, ExecutionRequest
from agent_runtime.serve.apis.conversation_team_app import _adapt_event
from agent_runtime.supervisor.common.constants import TeamEventField
from agent_runtime.supervisor.event.adapt import build_error, build_run_done
from agent_runtime.supervisor.event.channel import EventChannel, reset_channel, set_channel

_STOP = object()


async def run_conversation_supervisor(
    req,
    execution_id: str,
    supervisor_config,
    prepared_file_references: list[dict] | None = None,
):
    """Consume the standard ReAct runner for the built-in Supervisor."""
    execution_context = get_conversation_execution_context().for_child_call()
    identity = execution_context.identity
    execution_id = identity.execution_id
    channel = EventChannel(execution_id)
    channel_token = set_channel(channel)
    task = None
    try:
        ir_json = supervisor_config.to_ir()
        params = ExecutionParams(
            conversationHistory=[],
            globalVariables={
                "conversationId": identity.conversation_id,
                "projectId": identity.project_id,
                "workspaceId": identity.workspace_id,
                "userId": identity.user_id,
                "executionId": identity.execution_id,
                "conversationInputFiles": list(prepared_file_references or []),
                "conversationTeam": {
                    "type": "SUPERVISOR",
                    "subAgentIds": list(req.sub_agent_ids),
                    "skillCatalog": [
                        {
                            "skillId": item.skill_id,
                            "versionId": item.version_id,
                            "name": item.name,
                            "description": item.description,
                            "objectKey": item.object_key,
                        }
                        for item in (getattr(req, "skill_catalog", None) or [])
                    ],
                    "recommendedSkillIds": list(
                        getattr(req, "recommended_skill_ids", None) or []
                    ),
                },
            },
            pluginConfigs=[],
            toolSwitchDict={},
            isDebug=False,
            ir_cache=ir_json,
        )
        execution_request = ExecutionRequest(
            conversationId=identity.conversation_id,
            userId=identity.user_id,
            irPath="__conversation_supervisor__",
            query=req.query,
            params=params,
            headers={},
        )
        runner = ConversationRunnerFactory().get("ReAct")
        index = 0
        final_text = ""
        error_sent = False

        async def consume():
            nonlocal final_text, error_sent
            try:
                async for raw in runner.run_streaming(execution_request, execution_id):
                    event = _adapt_event(raw, execution_id)
                    if event is None:
                        continue
                    if event.get("event") == "run_done":
                        data = event.get("data") or {}
                        final_text = str(data.get("text") or final_text)
                        continue
                    if event.get("event") == "error":
                        error_sent = True
                    await channel.emit(event)
                    if event.get("event") == "message":
                        data = event.get("data") or {}
                        final_text += str(data.get("delta") or "")
            except asyncio.CancelledError:
                raise
            except Exception as error:
                await channel.emit(error)
            finally:
                await channel.emit(_STOP)

        task = asyncio.create_task(consume())
        while True:
            item = await channel.get()
            if item is _STOP:
                break
            if isinstance(item, BaseException):
                error_sent = True
                yield build_error(execution_id, "supervisor_error", str(item), index=index)
                index += 1
                continue
            item[TeamEventField.INDEX] = index
            index += 1
            yield item

        if task is not None:
            task.result()
        if not error_sent:
            yield build_run_done(execution_id, final_text, index=index)
    finally:
        primary_error = sys.exception()
        if task is not None and not task.done():
            task.cancel()
            try:
                await task
            except asyncio.CancelledError:
                pass
        reset_channel(channel_token)
        if primary_error is not None:
            raise primary_error
