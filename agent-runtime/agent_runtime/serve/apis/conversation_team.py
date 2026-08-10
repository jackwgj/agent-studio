# -*- coding: UTF-8 -*-
"""团队对话 API —— `/v1/conversation/team`

独立 API（不耦合 /v1/orchestration/ir/execute）：
接收子 Agent IDs + 系统提示词 + 模型部署 ID，引擎侧内置组装「监督者 + handoff 工具」并执行。
子 Agent 通过其已有 IR 加载（OBS agent/ir/{agentId}/{agentId}.json）。
"""

import logging

from fastapi import APIRouter
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from agent_runtime.supervisor.supervisor_builder import build_supervisor, run_supervisor

team_router = APIRouter(tags=["conversation-team"])

logger = logging.getLogger(__name__)


class ConversationTeamReq(BaseModel):
    """/v1/conversation/team 请求体"""

    conversation_id: str = Field(alias="conversationId")
    user_id: str = Field(default="anonymous", alias="userId")
    query: str
    sub_agent_ids: list[str] = Field(alias="subAgentIds")
    system_prompt: str = Field(default="", alias="systemPrompt")
    model_deployment_id: str = Field(alias="modelDeploymentId")


@team_router.post("/v1/conversation/team")
async def conversation_team(req: ConversationTeamReq):
    """组装监督者 + N 个 handoff 工具，跑一轮团队对话，返回监督者最终回答。"""
    if not req.query:
        return JSONResponse(status_code=400, content={"error": "query is required"})
    if not req.sub_agent_ids:
        return JSONResponse(status_code=400, content={"error": "subAgentIds is required"})

    try:
        # 请求字段 modelDeploymentId 携带部署 id（非模型名，D0-8）：路由(31113)解析成真实模型名后调 LLM
        agent = await build_supervisor(
            sub_agent_ids=req.sub_agent_ids,
            system_prompt=req.system_prompt,
            model_deployment_id=req.model_deployment_id,
        )
        result = await run_supervisor(
            agent, query=req.query, conversation_id=req.conversation_id
        )
        output = result.get("output", result)
        if isinstance(output, dict):
            output = output.get("result", "")
        return {
            "event": "done",
            "executionId": req.conversation_id,
            "data": {"text": str(output)},
        }
    except Exception as e:
        logger.error(f"conversation/team failed: {e}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"error": f"conversation/team failed: {e}"},
        )
