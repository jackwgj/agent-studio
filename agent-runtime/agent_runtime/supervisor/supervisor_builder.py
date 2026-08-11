# -*- coding: UTF-8 -*-
"""监督者组装器 —— 给定子 Agent IDs，为每个构建 handoff 工具，组装监督者 ReActAgent。

监督者 = openjiuwen ReActAgent（system_prompt 固定，来自调用方）+ N 个 HandoffTool。
子 Agent 由 HandoffTool.invoke 按需加载其已有 IR 执行（见 handoff_tool.py）。

工具注册采用公开 API + 幂等（swarm 模板，D0-4）：ability_manager.add(card) 成功后才检查
resource_mgr 是否已有同 id 实例，不存在才注册。工具无状态、跨会话共享安全，数量有界。
"""

import asyncio

from openjiuwen.core.runner import Runner
from openjiuwen.core.session.agent import create_agent_session
from openjiuwen.core.single_agent.agents.react_agent import ReActAgent
from openjiuwen.core.single_agent.schema.agent_card import AgentCard

from agent_runtime.supervisor.config import build_react_config
from agent_runtime.supervisor.events import (
    OutputSchemaType,
    TeamEventField,
    build_error,
    build_message,
    build_reasoning,
    build_run_done,
    build_usage,
    reset_event_queue,
    reset_execution_id,
    set_event_queue,
    set_execution_id,
)
from agent_runtime.supervisor.handoff_tool import HandoffTool
from jiuwen.serve.controllers.execution.open_utils import async_ir_load

# 监督者 stream 消费任务结束哨兵
_STOP = object()


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
        # 工具名 = transfer_to_{agentId[:8]}（HandoffTool 内生成，ASCII 短唯一；路由靠 description）
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


def _adapt_supervisor_chunk(chunk, execution_id: str, supervisor_text: dict):
    """把监督者 stream 的 OutputSchema 提前转成事件 dict 列表（增量事件）。

    Args:
        chunk: OutputSchema 原始对象
        execution_id: 本轮唯一标识
        supervisor_text: {"text": str} 可变容器，answer 时记录权威完整文本

    Returns:
        events —— 增量事件 dict（message/reasoning/usage/error），index 由主生成器统一补
    """
    events = []
    chunk_type = getattr(chunk, "type", "")
    payload = getattr(chunk, "payload", {}) or {}

    if chunk_type == OutputSchemaType.LLM_OUTPUT.value:
        content = payload.get("content", "") or ""
        if content:
            events.append(build_message(execution_id, content))
    elif chunk_type == OutputSchemaType.LLM_REASONING.value:
        content = payload.get("content", "") or ""
        if content:
            events.append(build_reasoning(execution_id, content))
    elif chunk_type == OutputSchemaType.LLM_USAGE.value:
        usage = payload.get("usage_metadata", {}) or {}
        events.append(build_usage(
            execution_id,
            input_tokens=usage.get("input_tokens", 0),
            output_tokens=usage.get("output_tokens", 0),
            total_tokens=usage.get("total_tokens", 0),
            latency_ms=payload.get("total_latency_ms"),
        ))
    elif chunk_type == OutputSchemaType.ANSWER.value:
        if payload.get("result_type", "") == "error":
            events.append(build_error(execution_id, code=103004, message=str(payload.get("output", ""))))
        else:
            output_text = payload.get("output", "") or ""
            if output_text:
                supervisor_text["text"] = output_text
    return events


async def run_supervisor(agent: ReActAgent, query: str, conversation_id: str, execution_id: str):
    """事件生成器：跑监督者一轮，产出 SSE 事件 dict（message/reasoning/usage/run_done/error）。

    双任务模式：
    - 任务 A（supervisor_stream_task）：消费监督者 stream 的 OutputSchema → put 事件通道；
      工具（handoff）在该任务内被 await，子 Agent 增量经同一通道冒泡。
    - 主生成器（本协程）：从通道取事件 → yield（工具执行期间仍实时输出）。

    Args:
        agent: 监督者 ReActAgent
        query: 用户问题
        conversation_id: 业务会话 ID（作监督者执行 session_id，非事件 ID）
        execution_id: 本轮唯一标识（全量 uuid4）

    Yields:
        事件 dict（无 done，流结束即正常终止；异常发 error）
    """
    event_queue: asyncio.Queue = asyncio.Queue()
    token_queue = set_event_queue(event_queue)
    token_exec = set_execution_id(execution_id)
    # 必须传 card：agent.stream 内部 pre_run → checkpointer 读 session._card.id，缺 card 则 None.id 崩溃
    session = create_agent_session(session_id=conversation_id, card=agent.card)
    index = 0
    supervisor_text: dict = {"text": ""}
    error_sent = False
    try:
        async def supervisor_stream_task():
            # 监督者与子 Agent 同路径：stream 消费 → 提前转事件 dict → put queue（主生成器纯透传）
            try:
                async for chunk in agent.stream({"query": query}, session):
                    for ev in _adapt_supervisor_chunk(chunk, execution_id, supervisor_text):
                        await event_queue.put(ev)
            except asyncio.CancelledError:
                raise
            except Exception as e:
                await event_queue.put(e)
            finally:
                await event_queue.put(_STOP)

        task = asyncio.create_task(supervisor_stream_task())
        while True:
            item = await event_queue.get()
            if item is _STOP:
                break
            if isinstance(item, Exception):
                yield build_error(execution_id, code="supervisor_error", message=str(item), index=index)
                index += 1
                error_sent = True
                break
            # queue 中 item 必然是事件 dict（监督者 supervisor_stream_task 提前转 + 子 Agent emit 冒泡），直接透传
            item[TeamEventField.INDEX] = index
            index += 1
            yield item

        # 边界收尾：run_done 用 answer 的权威完整文本；异常已发 error 则不补 run_done
        if not error_sent:
            yield build_run_done(execution_id, supervisor_text["text"], index=index)
    finally:
        reset_event_queue(token_queue)
        reset_execution_id(token_exec)
