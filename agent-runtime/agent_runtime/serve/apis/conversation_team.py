# -*- coding: UTF-8 -*-
"""Trusted conversation-team API with a canonical SSE event stream."""

from __future__ import annotations

import logging
import uuid

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

from agent_runtime.common.config import settings
from agent_runtime.conversation.execution_cleanup import (
    cleanup_execution_directories,
)
from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
    reset_conversation_execution_context,
    set_conversation_execution_context,
    validate_conversation_path_key,
)
from agent_runtime.conversation.execution_coordinator import (
    acquire_conversation_execution,
)
from agent_runtime.conversation.usage import TokenEstimateAccumulator
from agent_runtime.conversation.input_artifact_bridge import (
    ConversationInputArtifact,
    prepare_conversation_inputs,
)
from agent_runtime.conversation.output_artifact_publisher import (
    capture_conversation_output_baseline,
    publish_conversation_outputs,
)
from agent_runtime.conversation.supervisor_runner import run_conversation_supervisor
from agent_runtime.conversation.workspace_initializer import (
    ensure_conversation_workspace,
)
from agent_runtime.serve.apis.conversation_team_app import stream_application
from agent_runtime.supervisor.builder import normalize_skill_inputs
from agent_runtime.supervisor.common.constants import TeamEventField
from agent_runtime.supervisor.conversation_supervisor_builder import (
    build_conversation_supervisor_config,
)
from agent_runtime.supervisor.event.canonical import (
    CanonicalEventSequencer,
    build_artifact,
    build_canonical_event,
    build_error,
    build_run_end,
    build_run_start,
    sse_line,
)
from agent_runtime.supervisor.event.types import ConversationEventType
from agent_runtime.supervisor.skill_model import SkillDescriptor


team_router = APIRouter(tags=["conversation-team"])
logger = logging.getLogger(__name__)


class SkillCatalogItemReq(BaseModel):
    """Manager-provided trusted Skill descriptor."""

    model_config = ConfigDict(populate_by_name=True, extra="forbid")

    skill_id: str = Field(alias="skillId")
    version_id: str = Field(alias="versionId")
    name: str
    description: str
    object_key: str = Field(alias="objectKey")

    @field_validator("skill_id", "version_id", "name", "description", "object_key")
    @classmethod
    def reject_blank_descriptor_fields(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("skill descriptor fields must not be blank")
        return value


class ConversationTeamStreamingResponse(StreamingResponse):
    """Close the async body in the same task that consumes its SSE chunks."""

    async def stream_response(self, send) -> None:
        try:
            await super().stream_response(send)
        finally:
            close = getattr(self.body_iterator, "aclose", None)
            if close is not None:
                await close()


class ConversationTeamReq(BaseModel):
    """Request body for ``/v1/conversation/team``."""

    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    conversation_id: str = Field(alias="conversationId")
    project_id: str = Field(alias="projectId")
    workspace_id: str = Field(alias="workspaceId")
    user_id: str = Field(alias="userId")
    query: str
    select_type: str = Field(default="SUPERVISOR", alias="selectType")
    app_id: str | None = Field(default=None, alias="appId")
    sub_agent_ids: list[str] = Field(default_factory=list, alias="subAgentIds")
    model_deployment_id: str | None = Field(default=None, alias="modelDeploymentId")
    conversation_history: list | None = Field(None, alias="conversationHistory")
    skill_catalog: list[SkillCatalogItemReq] = Field(
        default_factory=list, alias="skillCatalog"
    )
    recommended_skill_ids: list[str] = Field(
        default_factory=list, alias="recommendedSkillIds"
    )
    file_ids: list[ConversationInputArtifact] = Field(
        default_factory=list, alias="fileIds"
    )

    @model_validator(mode="before")
    @classmethod
    def reject_conflicting_aliases(cls, values):
        if not isinstance(values, dict):
            return values
        aliases = (
            ("conversationId", "conversation_id"),
            ("projectId", "project_id"),
            ("workspaceId", "workspace_id"),
            ("userId", "user_id"),
            ("subAgentIds", "sub_agent_ids"),
            ("modelDeploymentId", "model_deployment_id"),
            ("conversationHistory", "conversation_history"),
            ("skillCatalog", "skill_catalog"),
            ("recommendedSkillIds", "recommended_skill_ids"),
            ("fileIds", "file_ids"),
            ("selectType", "select_type"),
            ("appId", "app_id"),
        )
        if any(
            alias in values
            and field_name in values
            and values[alias] != values[field_name]
            for alias, field_name in aliases
        ):
            raise ValueError("conflicting request field aliases")
        return values

    @field_validator("conversation_id", "project_id", "workspace_id", "user_id")
    @classmethod
    def validate_execution_identity(cls, value: str) -> str:
        if not isinstance(value, str) or not value.strip():
            raise ValueError("execution identity fields must not be blank")
        return value.strip()

    @field_validator("conversation_id", "user_id")
    @classmethod
    def validate_visible_execution_identity(cls, value: str) -> str:
        return validate_conversation_path_key(value, "execution identity")

    @model_validator(mode="after")
    def validate_skill_catalog(self):
        catalog = [
            SkillDescriptor(
                skill_id=item.skill_id,
                version_id=item.version_id,
                name=item.name,
                description=item.description,
                object_key=item.object_key,
            )
            for item in self.skill_catalog
        ]
        _, self.recommended_skill_ids = normalize_skill_inputs(
            catalog, self.recommended_skill_ids
        )
        return self


def _canonical_runner_event(event: dict, req: ConversationTeamReq, run_id: str) -> dict | None:
    """Accept canonical events and normalize only current non-legacy test seams."""
    if not isinstance(event, dict):
        return None
    event_name = str(event.get("event") or "")
    if event_name == "run_done":
        # The removed protocol must never re-enter production via a runner seam.
        return None
    if event.get("runId"):
        return event
    if event_name not in {item.value for item in ConversationEventType}:
        return None
    return build_canonical_event(
        event_name,
        conversation_id=req.conversation_id,
        run_id=run_id,
        data=dict(event.get("data") or {}),
        parent_run_id=event.get("parentRunId"),
        execution_type=event.get("executionType") or "agent",
    )


async def team_sse_stream(req: ConversationTeamReq, execution_id: str | None = None):
    """Run one trusted execution and emit its canonical lifecycle in order."""
    execution_id = execution_id or str(uuid.uuid4())
    identity = ConversationIdentity(
        project_id=req.project_id,
        workspace_id=req.workspace_id,
        user_id=req.user_id,
        conversation_id=req.conversation_id,
        execution_id=execution_id,
    )
    context = ConversationExecutionContext.create(
        identity, settings.security_sandbox.workspace_root
    )
    context_token = set_conversation_execution_context(context)
    runner = None
    execution_lease = None
    index = 0
    sequencer = CanonicalEventSequencer(root_run_id=execution_id)
    root_terminal_seen = False
    root_failed = False
    accumulated_messages: dict[str, str] = {}
    token_estimator = TokenEstimateAccumulator(execution_id)
    token_estimator.add_input(req.query)
    token_estimator.add_input(req.conversation_history)
    try:
        execution_lease = await acquire_conversation_execution(context)
        await ensure_conversation_workspace()
        prepared_paths = await prepare_conversation_inputs(req.file_ids)
        prepared_file_references = [
            {"fileName": artifact.file_name, "path": path}
            for artifact, path in zip(req.file_ids, prepared_paths, strict=True)
        ]
        output_baseline = await capture_conversation_output_baseline()

        yield sse_line(
            build_canonical_event(
                ConversationEventType.MESSAGE,
                conversation_id=req.conversation_id,
                run_id=execution_id,
                data={"content": req.query, "role": "user"},
                index=index,
            )
        )
        index += 1
        yield sse_line(
            build_run_start(req.conversation_id, execution_id, index=index)
        )
        index += 1

        if req.select_type.upper() == "APP":
            runner = stream_application(req, execution_id, prepared_file_references)
        else:
            supervisor_config = await build_conversation_supervisor_config(
                sub_agent_ids=req.sub_agent_ids,
                model_deployment_id=req.model_deployment_id,
                conversation_history=req.conversation_history,
                file_references=prepared_file_references,
            )
            runner = run_conversation_supervisor(
                req,
                execution_id,
                supervisor_config,
                prepared_file_references,
            )

        async for raw_event in runner:
            event = _canonical_runner_event(raw_event, req, execution_id)
            if event is None:
                continue
            token_estimator.add(event)
            event_run_id = event.get("runId") or event.get("data", {}).get("runId")
            if event.get("event") == "run_start" and event_run_id == execution_id:
                # The API boundary already emitted the single authoritative root start.
                continue
            if event.get("event") == "message" and event_run_id:
                data = event.get("data") or {}
                delta = str(data.get("delta") or "")
                if (
                    data.get("controllerEvent") == "intermediate_message"
                    and delta
                    and accumulated_messages.get(event_run_id)
                ):
                    # Controller emits a final full-text snapshot after token deltas.
                    # Keep it only when it contributes content not already streamed.
                    continue
                accumulated_messages[event_run_id] = (
                    accumulated_messages.get(event_run_id, "") + delta
                )
            token_estimator.add(event)
            if event_run_id == execution_id and event.get("event") in {"run_end", "error"}:
                root_terminal_seen = True
                root_failed = root_failed or event.get("event") == "error"
            for accepted in sequencer.accept(event):
                accepted[TeamEventField.INDEX] = index
                index += 1
                yield sse_line(accepted)

        if root_failed:
            return
        if not root_terminal_seen:
            sequencer.accept(build_run_end(req.conversation_id, execution_id))

        for artifact in await publish_conversation_outputs(output_baseline):
            artifact_event = build_artifact(
                req.conversation_id,
                execution_id,
                execution_id=artifact.execution_id,
                object_key=artifact.object_key,
                file_name=artifact.file_name,
                size=artifact.size,
                media_type=artifact.media_type,
                checksum=artifact.checksum,
            )
            token_estimator.add(artifact_event)
            for accepted in sequencer.accept(artifact_event):
                accepted[TeamEventField.INDEX] = index
                index += 1
                yield sse_line(accepted)
        usage_event = token_estimator.finalize(req.conversation_id)
        for accepted in sequencer.accept(usage_event):
            accepted[TeamEventField.INDEX] = index
            index += 1
            yield sse_line(accepted)
        for accepted in sequencer.release_root_end():
            accepted[TeamEventField.INDEX] = index
            index += 1
            yield sse_line(accepted)
    except Exception as error:
        logger.error(
            "conversation/team build or app adapter failed: %s", error, exc_info=True
        )
        failure = build_error(
            req.conversation_id,
            execution_id,
            code="build_failed",
            message=str(error),
        )
        for accepted in sequencer.accept(failure):
            accepted[TeamEventField.INDEX] = index
            index += 1
            yield sse_line(accepted)
    finally:
        try:
            if runner is not None:
                try:
                    await runner.aclose()
                except Exception:
                    pass
        finally:
            try:
                if execution_lease is not None:
                    await cleanup_execution_directories(context, remove_output=False)
            except Exception:
                logger.warning(
                    "conversation tmp cleanup failed; retained until conversation cleanup",
                    exc_info=True,
                )
            finally:
                if execution_lease is not None:
                    await execution_lease.release()
                reset_conversation_execution_context(context_token)


@team_router.post("/v1/conversation/team")
async def conversation_team(req: ConversationTeamReq, request: Request):
    """Validate Agent selection and return one canonical SSE response."""
    if not req.query:
        return JSONResponse(status_code=400, content={"error": "query is required"})
    select_type = req.select_type.upper()
    if select_type not in {"SUPERVISOR", "APP"}:
        return JSONResponse(
            status_code=400,
            content={"error": "selectType must be SUPERVISOR or APP"},
        )
    if select_type == "SUPERVISOR":
        if not req.sub_agent_ids:
            return JSONResponse(
                status_code=400, content={"error": "subAgentIds is required"}
            )
        if not req.model_deployment_id:
            return JSONResponse(
                status_code=400,
                content={"error": "modelDeploymentId is required"},
            )
        if req.app_id:
            return JSONResponse(
                status_code=400,
                content={"error": "appId is not allowed for SUPERVISOR"},
            )
    else:
        if not req.app_id:
            return JSONResponse(
                status_code=400, content={"error": "appId is required"}
            )
        if req.sub_agent_ids or req.model_deployment_id:
            return JSONResponse(
                status_code=400,
                content={"error": "Supervisor fields are not allowed for APP"},
            )

    execution_id = request.headers.get("x-execution-id")
    return ConversationTeamStreamingResponse(
        content=team_sse_stream(req, execution_id),
        media_type="text/event-stream",
    )
