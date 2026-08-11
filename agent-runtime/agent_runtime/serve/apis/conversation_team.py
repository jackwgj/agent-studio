# -*- coding: UTF-8 -*-
"""团队对话 API —— `/v1/conversation/team`（SSE 事件流）

独立 API（不耦合 /v1/orchestration/ir/execute）：
接收子 Agent IDs + 模型部署 ID（+ 多轮历史），引擎侧内置组装「监督者 + handoff 工具」并执行。
监督者系统提示词固定引擎侧（F4/用户决策 2026-08-11）：Java 不再传 systemPrompt。
子 Agent 通过其已有 IR 加载（OBS agent/ir/{agentId}/{agentId}.json）。

SSE 事件化（Phase 3）：产出结构化事件流（user_message/run_start/message/reasoning/tool_call/
tool_result/sub_start/sub_done/run_done/usage/error），暴露执行边界给前端与 Java 三表落库。
无 done 事件（SSE 流关闭即正常结束），error 用于异常。
"""

import logging
import uuid

from fastapi import APIRouter
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import BaseModel, Field

from agent_runtime.supervisor.builder import build_supervisor
from agent_runtime.supervisor.common.constants import TeamEventField
from agent_runtime.supervisor.event.adapt import build_error, build_run_start, build_user_message, sse_line
from agent_runtime.supervisor.runner import run_supervisor

team_router = APIRouter(tags=["conversation-team"])

logger = logging.getLogger(__name__)


class ConversationTeamReq(BaseModel):
    """/v1/conversation/team 请求体"""

    conversation_id: str = Field(alias="conversationId")
    user_id: str = Field(default="anonymous", alias="userId")
    query: str
    sub_agent_ids: list[str] = Field(alias="subAgentIds")
    model_deployment_id: str = Field(alias="modelDeploymentId")
    # 多轮历史（list[{role, content}]）：仅注入监督者上下文（方案 B），子 Agent 不感知（D0-3/用户决策 2026-08-11）
    conversation_history: list | None = Field(None, alias="conversationHistory")


async def team_sse_stream(req: ConversationTeamReq):
    """SSE 事件生成器：先发 user_message/run_start，再跑监督者事件流。index 统一在此递增。"""
    execution_id = str(uuid.uuid4())  # 监督者一轮唯一标识（全量 uuid4，不用 conversation_id）
    index = 0
    yield sse_line(build_user_message(execution_id, req.conversation_id, req.query, index=index))
    index += 1
    yield sse_line(build_run_start(execution_id, index=index))
    index += 1

    try:
        agent = await build_supervisor(
            sub_agent_ids=req.sub_agent_ids,
            model_deployment_id=req.model_deployment_id,
            conversation_history=req.conversation_history,
        )
    except Exception as e:
        logger.error(f"conversation/team build_supervisor failed: {e}", exc_info=True)
        yield sse_line(build_error(execution_id, code="build_failed", message=str(e), index=index))
        return

    async for event in run_supervisor(agent, req.query, req.conversation_id, execution_id):
        event[TeamEventField.INDEX] = index  # 统一占序，覆盖增量事件自带 index
        index += 1
        yield sse_line(event)


@team_router.post("/v1/conversation/team")
async def conversation_team(req: ConversationTeamReq):
    """组装监督者 + N 个 handoff 工具，跑一轮团队对话，返回 SSE 事件流。"""
    if not req.query:
        return JSONResponse(status_code=400, content={"error": "query is required"})
    if not req.sub_agent_ids:
        return JSONResponse(status_code=400, content={"error": "subAgentIds is required"})

    return StreamingResponse(
        content=team_sse_stream(req),
        media_type="text/event-stream",
    )
