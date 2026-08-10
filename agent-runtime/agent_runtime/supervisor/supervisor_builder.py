# -*- coding: UTF-8 -*-
"""监督者组装器 —— 给定子 Agent IDs，为每个构建 handoff 工具，组装监督者 ReActAgent。

监督者 = openjiuwen ReActAgent（system_prompt 固定，来自调用方）+ N 个 HandoffTool。
子 Agent 由 HandoffTool.invoke 按需加载其已有 IR 执行（见 handoff_tool.py）。

工具注册采用公开 API + 幂等（swarm 模板，D0-4）：ability_manager.add(card) 成功后才检查
resource_mgr 是否已有同 id 实例，不存在才注册。工具无状态、跨会话共享安全，数量有界。
"""

from openjiuwen.core.runner import Runner
from openjiuwen.core.session.agent import create_agent_session
from openjiuwen.core.single_agent.agents.react_agent import ReActAgent
from openjiuwen.core.single_agent.schema.agent_card import AgentCard

from agent_runtime.supervisor.config import build_react_config
from agent_runtime.supervisor.handoff_tool import HandoffTool
from jiuwen.serve.controllers.execution.open_utils import async_ir_load


def _ir_path(agent_id: str) -> str:
    return f"agent/ir/{agent_id}/{agent_id}.json"


async def _load_sub_agent_description(agent_id: str) -> str:
    """从子 Agent 已有 IR 读取描述，用于 handoff 工具的卡片描述。"""
    try:
        ir_data = await async_ir_load(_ir_path(agent_id))
        return (
            ir_data.get("description")
            or ir_data.get("agentName")
            or f"将任务移交给子 Agent {agent_id} 处理"
        )
    except Exception:
        return f"将任务移交给子 Agent {agent_id} 处理"


async def build_supervisor(
    sub_agent_ids: list,
    system_prompt: str,
    model_deployment_id: str,
) -> ReActAgent:
    """按 sub_agent_ids 动态构建 N 个 handoff 工具，组装监督者 ReActAgent。

    Args:
        sub_agent_ids: 子 Agent IDs（已注册 Agent）
        system_prompt: 监督者系统提示词
        model_deployment_id: 监督者模型部署 id（非模型名；路由解析成真实模型名，D0-8）

    Returns:
        已配置并注册工具的监督者 ReActAgent
    """
    tools = []
    for agent_id in sub_agent_ids:
        description = await _load_sub_agent_description(agent_id)
        tool = HandoffTool(
            agent_id=agent_id,
            description=description,
        )
        tools.append(tool)

    agent = ReActAgent(
        card=AgentCard(
            id="conversation_team_supervisor",
            name="Team Supervisor",
            description="团队监督者，负责把任务分派给最合适的子 Agent",
        )
    )
    agent.configure(build_react_config(system_prompt, model_deployment_id))

    # 注册工具：公开 API + 幂等（swarm 模板），不再直插私有 _tools 字段
    for tool in tools:
        result = agent.ability_manager.add(tool.card)
        if result.added:
            existing = Runner.resource_mgr.get_tool(tool.card.id)
            if existing is None:
                Runner.resource_mgr.add_tool(tool)

    return agent


async def run_supervisor(agent: ReActAgent, query: str, conversation_id: str):
    """建 session 并跑监督者，返回最终结果。conversation_id 为业务必填，不做兜底。"""
    session = create_agent_session(session_id=conversation_id)
    return await agent.invoke({"query": query}, session)
