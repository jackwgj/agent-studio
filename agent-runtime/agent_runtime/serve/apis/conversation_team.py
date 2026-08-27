# -*- coding: UTF-8 -*-
"""团队对话 API —— `/v1/conversation/team`（SSE canonical ConversationEvent 流）

独立 API 接收团队对话请求并输出 canonical ConversationEvent。
"""

import logging
import uuid

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

from agent_runtime.supervisor.builder import normalize_skill_inputs
from agent_runtime.supervisor.conversation_supervisor_builder import (
    build_conversation_supervisor_config,
)
from agent_runtime.conversation.supervisor_runner import run_conversation_supervisor
from agent_runtime.supervisor.common.constants import TeamEventField
from agent_runtime.supervisor.event.canonical import build_canonical_event, build_run_end, build_run_start, sse_line
from agent_runtime.supervisor.event.types import ConversationEventType
from agent_runtime.supervisor.skill_model import SkillDescriptor
from agent_runtime.serve.apis.conversation_team_app import stream_application

team_router = APIRouter(tags=["conversation-team"])

logger = logging.getLogger(__name__)


class SkillCatalogItemReq(BaseModel):
    """Manager 在对话请求中下发的受信任 Skill 描述。"""

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
    """Close this endpoint's async body in the Task that consumes SSE chunks."""

    async def stream_response(self, send) -> None:
        try:
            await super().stream_response(send)
        finally:
            close = getattr(self.body_iterator, "aclose", None)
            if close is not None:
                await close()


class ConversationTeamReq(BaseModel):
    """/v1/conversation/team 请求体"""

    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    conversation_id: str = Field(alias="conversationId")
    user_id: str = Field(default="anonymous", alias="userId")
    query: str
    select_type: str = Field(default="SUPERVISOR", alias="selectType")
    app_id: str | None = Field(default=None, alias="appId")
    sub_agent_ids: list[str] = Field(default_factory=list, alias="subAgentIds")
    model_deployment_id: str | None = Field(default=None, alias="modelDeploymentId")
    # 多轮历史（list[{role, content}]）：仅注入监督者上下文（方案 B），子 Agent 不感知（D0-3/用户决策 2026-08-11）
    conversation_history: list | None = Field(None, alias="conversationHistory")
    skill_catalog: list[SkillCatalogItemReq] = Field(default_factory=list, alias="skillCatalog")
    recommended_skill_ids: list[str] = Field(default_factory=list, alias="recommendedSkillIds")
    file_ids: list[dict[str, str]] = Field(default_factory=list, alias="fileIds")

    @model_validator(mode="before")
    @classmethod
    def reject_conflicting_aliases(cls, values):
        if not isinstance(values, dict):
            return values
        aliases = (
            ("conversationId", "conversation_id"),
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
            alias in values and field_name in values and values[alias] != values[field_name]
            for alias, field_name in aliases
        ):
            raise ValueError("conflicting request field aliases")
        return values

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
        _, self.recommended_skill_ids = normalize_skill_inputs(catalog, self.recommended_skill_ids)
        return self


async def team_sse_stream(req: ConversationTeamReq, execution_id: str | None = None):
    """SSE 事件生成器：先发 user_message/run_start，再跑监督者事件流。index 统一在此递增。

    execution_id 优先级：X-Execution-Id 头（Java 侧生成并提前落了 user 行，需回显保证
    user 行与 run/sub_run 行 execution_id 一致）> 引擎 uuid4 兜底（全量 uuid4，不用 conversation_id）。
    """
    if not execution_id:
        execution_id = str(uuid.uuid4())
    index = 0
    yield sse_line(build_canonical_event(
        ConversationEventType.MESSAGE,
        conversation_id=req.conversation_id,
        run_id=execution_id,
        data={"content": req.query, "role": "user"},
        index=index,
    ))
    index += 1
    yield sse_line(build_run_start(req.conversation_id, execution_id, index=index))
    index += 1

    runner = None
    try:
        if req.select_type.upper() == "APP":
            runner = stream_application(req, execution_id)
        else:
            supervisor_config = await build_conversation_supervisor_config(
                sub_agent_ids=req.sub_agent_ids,
                model_deployment_id=req.model_deployment_id,
                conversation_history=req.conversation_history,
                file_references=req.file_ids,
            )
            runner = run_conversation_supervisor(req, execution_id, supervisor_config)

        async for event in runner:
            event[TeamEventField.INDEX] = index  # 统一占序，覆盖增量事件自带 index
            index += 1
            yield sse_line(event)
    except Exception as e:
        logger.error(f"conversation/team build or app adapter failed: {e}", exc_info=True)
        yield sse_line(build_canonical_event(
            ConversationEventType.ERROR,
            conversation_id=req.conversation_id,
            run_id=execution_id,
            data={"code": "build_failed", "message": str(e)},
            index=index,
        ))
    finally:
        if runner is not None:
            try:
                await runner.aclose()
            except Exception:
                pass


@team_router.post("/v1/conversation/team")
async def conversation_team(req: ConversationTeamReq, request: Request):
    """组装监督者 + N 个 handoff 工具，跑一轮团队对话，返回 SSE 事件流。"""
    if not req.query:
        return JSONResponse(status_code=400, content={"error": "query is required"})
    select_type = req.select_type.upper()
    if select_type not in {"SUPERVISOR", "APP"}:
        return JSONResponse(status_code=400, content={"error": "selectType must be SUPERVISOR or APP"})
    if select_type == "SUPERVISOR":
        if not req.sub_agent_ids:
            return JSONResponse(status_code=400, content={"error": "subAgentIds is required"})
        if not req.model_deployment_id:
            return JSONResponse(status_code=400, content={"error": "modelDeploymentId is required"})
        if req.app_id:
            return JSONResponse(status_code=400, content={"error": "appId is not allowed for SUPERVISOR"})
    else:
        if not req.app_id:
            return JSONResponse(status_code=400, content={"error": "appId is required"})
        if req.sub_agent_ids or req.model_deployment_id:
            return JSONResponse(status_code=400, content={"error": "Supervisor fields are not allowed for APP"})

    # Java 侧生成 execution_id 并经 X-Execution-Id 头下发（保证三表 execution_id 一致），缺省时引擎 uuid4 兜底
    execution_id = request.headers.get("x-execution-id")
    return ConversationTeamStreamingResponse(
        content=team_sse_stream(req, execution_id),
        media_type="text/event-stream",
    )
