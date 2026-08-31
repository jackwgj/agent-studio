"""对话工具 describe API —— 方案 A2：Java 向 Python 索取本请求所需工具描述。

复用 `/v1/conversation/team` 的请求体（ConversationTeamReq），返回 `{tools: [ToolSpec...]}`。
"""

from fastapi import APIRouter

from agent_runtime.conversation.tool.describe import collect_tool_specs
from agent_runtime.serve.apis.conversation_team import ConversationTeamReq

tool_router = APIRouter(tags=["conversation-tool"])


@tool_router.post("/v1/conversation/tools/describe")
async def describe_tools(req: ConversationTeamReq) -> dict:
    """返回本请求需要注册的工具描述列表。"""
    specs = await collect_tool_specs(req.select_type, req.sub_agent_ids)
    return {"tools": specs}
