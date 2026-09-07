# -*- coding: UTF-8 -*-
"""监督者组装器 —— 给定子 Agent IDs，为每个构建 handoff 工具，组装监督者 ReActAgent。

监督者 = openjiuwen ReActAgent（system_prompt 固定引擎侧，F4：Java 不传）+ N 个 HandoffTool。
子 Agent 由 HandoffTool.invoke 按需加载其已有 IR 执行（见 tool/handoff_tool.py）。

工具注册采用公开 API + 幂等（swarm 模板，D0-4）：ability_manager.add(card) 成功后才检查
resource_mgr 是否已有同 id 实例，不存在才注册。工具无状态、跨会话共享安全，数量有界。

运行（事件生成器 run_supervisor）在 runner.py —— build（组装）与 run（运行）职责分离。
"""

from openjiuwen.core.runner import Runner
from openjiuwen.core.single_agent.agents.react_agent import ReActAgent
from openjiuwen.core.single_agent.schema.agent_card import AgentCard

from agent_runtime.supervisor.config import build_react_config, format_conversation_history
from agent_runtime.supervisor.tool.handoff_tool import HandoffTool
from jiuwen.serve.controllers.execution.open_utils import async_ir_load

# 监督者系统提示词固定引擎侧（F4/用户决策 2026-08-11）：请求不再含 systemPrompt，Java 不传。
# 核心约束（方案 B）：监督者是任务分派者，必须把完成任务所需信息写足在 handoff query 里；
# 子 Agent 纯无状态单任务执行、不感知对话历史。
SUPERVISOR_SYSTEM_PROMPT = (
    "你是团队监督者，负责把用户请求分派给最合适的子 Agent 处理。"
    "移交任务时，你必须把完成任务所需的全部上下文信息完整写入 query 参数，"
    "确保子 Agent 无需任何外部信息即可直接执行。"
    "若无法匹配任何子 Agent，直接告知用户并提供建议。"
)


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
    model_deployment_id: str,
    conversation_history: list | None = None,
) -> ReActAgent:
    """按 sub_agent_ids 动态构建 N 个 handoff 工具，组装监督者 ReActAgent。

    Args:
        sub_agent_ids: 子 Agent IDs（已注册 Agent）
        model_deployment_id: 监督者模型部署 id（非模型名；路由解析成真实模型名，D0-8）
        conversation_history: 多轮历史 list[{role, content}]，仅注入监督者上下文（方案 B），子 Agent 不感知

    Returns:
        已配置并注册工具的监督者 ReActAgent
    """
    tools = []
    for agent_id in sub_agent_ids:
        description = await _load_sub_agent_description(agent_id)
        # 工具名 = transfer_to_{agentId[:8]}（HandoffTool 内生成，ASCII 短唯一；路由靠 description）
        tool = HandoffTool(
            agent_id=agent_id,
            description=description,
        )
        tools.append(tool)

    # 监督者提示词 = 引擎侧固定角色/指令 + 历史段（F4：历史只进监督者，方案 B）
    system_prompt = SUPERVISOR_SYSTEM_PROMPT + format_conversation_history(conversation_history)

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
