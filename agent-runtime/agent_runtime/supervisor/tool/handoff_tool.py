# -*- coding: UTF-8 -*-
"""Handoff 工具基类 —— 定义子 Agent 转交工具的公共结构。

HandoffTool 提供工具卡片构造、IR 路径和查询提取。
具体执行逻辑由子类 ConversationHandoffTool 实现。
"""

from openjiuwen.core.foundation.tool import Tool, ToolCard


class HandoffTool(Tool):
    """Handoff 工具基类：提供工具卡片、IR 路径和查询提取。"""

    def __init__(
        self,
        agent_id: str,
        description: str,
    ):
        self.agent_id = agent_id
        tool_name = f"transfer_to_{agent_id[:8]}"
        super().__init__(
            card=ToolCard(
                id=f"handoff_{agent_id}",
                name=tool_name,
                description=description or f"将任务移交给子 Agent {agent_id} 处理",
                input_params={
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "需要子 Agent 处理的用户问题",
                        }
                    },
                    "required": ["query"],
                },
            )
        )

    def _ir_path(self) -> str:
        return f"agent/ir/{self.agent_id}/{self.agent_id}.json"

    def _extract_query(self, inputs) -> str:
        if isinstance(inputs, dict):
            return str(inputs.get("query", ""))
        return str(getattr(inputs, "query", inputs))

    async def stream(self, inputs, **kwargs):
        result = await self.invoke(inputs, **kwargs)
        yield result
