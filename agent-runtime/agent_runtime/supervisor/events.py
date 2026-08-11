# -*- coding: UTF-8 -*-
"""团队对话 SSE 事件 —— 枚举 + 统一常量类 + 构造 helper。

用户决策（2026-08-11）：事件类型构建成枚举、字段常量统一常量类管理，不在各应用类中硬编码
字符串。所有业务 ID（execution_id / sub_execution_id / tool_call_id）用全量 uuid4，不拼接。

事件分层（D0-5）：message/reasoning 是增量（前端实时、不落库）；user_message/sub_done/run_done
是边界完整文本（整句落库）。无 done 事件（SSE 流关闭即正常结束），error 用于异常终止。
"""

import asyncio
import json
import uuid
from contextvars import ContextVar
from enum import Enum


class TeamEventType(str, Enum):
    """团队对话 SSE 事件类型"""

    USER_MESSAGE = "user_message"      # 用户输入（落 t_conversation）
    RUN_START = "run_start"            # 本轮开始（边界）
    MESSAGE = "message"                # LLM 增量（监督者/子 Agent，实时）
    REASONING = "reasoning"            # 思考增量（监督者/子 Agent，实时）
    TOOL_CALL = "tool_call"            # 工具调用开始（统一，不分主子）
    TOOL_RESULT = "tool_result"        # 工具调用结束（统一，不分主子）
    SUB_START = "sub_start"            # 子 Agent 执行开始（边界）
    SUB_DONE = "sub_done"              # 子 Agent 执行完成（完整文本，落 t_conversation_sub_run）
    RUN_DONE = "run_done"              # 监督者整轮完成（完整文本，落 t_conversation_run）
    USAGE = "usage"                    # LLM token 消耗统计
    ERROR = "error"                    # 异常终止


class OutputSchemaType(str, Enum):
    """jiuwen ReActAgent stream 产出的 OutputSchema.type（消费 chunk 时判断用）"""

    LLM_OUTPUT = "llm_output"          # LLM 增量文本
    LLM_REASONING = "llm_reasoning"    # LLM 推理文本
    LLM_USAGE = "llm_usage"            # LLM 调用统计（token 消耗）
    ANSWER = "answer"                  # 一轮最终答案（完整文本）
    DONE = "done"                      # 一轮结束标记
    TRACER_AGENT = "tracer_agent"      # 工具调用追踪（Trace）


class TeamEventField:
    """SSE 事件字段名常量（统一管理，避免散落硬编码）"""

    EVENT = "event"
    DATA = "data"
    EXECUTION_ID = "executionId"
    SUB_EXECUTION_ID = "subExecutionId"
    TOOL_CALL_ID = "toolCallId"
    AGENT_ID = "agentId"
    DELTA = "delta"
    CONTENT = "content"
    TEXT = "text"
    QUERY = "query"
    CONVERSATION_ID = "conversationId"
    TOOL_NAME = "toolName"
    ARGUMENTS = "arguments"
    RESULT = "result"
    INPUT_TOKENS = "inputTokens"
    OUTPUT_TOKENS = "outputTokens"
    TOTAL_TOKENS = "totalTokens"
    LATENCY_MS = "latencyMs"
    CODE = "code"
    MESSAGE = "message"
    INDEX = "index"


# ---------------------------------------------------------------- 业务 ID 生成

def gen_execution_id() -> str:
    """监督者一轮的唯一标识（全量 uuid4，不拼接、不用 conversation_id）"""
    return str(uuid.uuid4())


def gen_sub_execution_id() -> str:
    """每次 handoff 的唯一标识（全量 uuid4，同轮多次指派不串）"""
    return str(uuid.uuid4())


def gen_tool_call_id() -> str:
    """每次工具调用的唯一标识（全量 uuid4）"""
    return str(uuid.uuid4())


# ---------------------------------------------------------------- 事件通道（ContextVar）

# 运行时注入：run_supervisor 建 asyncio.Queue 事件通道，经 ContextVar 注入；
# 工具（HandoffTool）内 emit() 冒泡子 Agent 事件到同一 queue（D0-4/D0-6 工具无状态保持）。
# 并发工具共享同一 context（监督者 stream 任务内 await），事件正确汇聚。
_team_event_queue: ContextVar[asyncio.Queue | None] = ContextVar("team_event_queue", default=None)
_team_execution_id: ContextVar[str] = ContextVar("team_execution_id", default="")


def set_event_queue(queue: asyncio.Queue) -> object:
    """设置当前执行上下文的事件通道，返回 token 供 reset。"""
    return _team_event_queue.set(queue)


def reset_event_queue(token: object) -> None:
    _team_event_queue.reset(token)


def set_execution_id(execution_id: str) -> object:
    """设置当前执行上下文的 execution_id（本轮唯一标识），返回 token 供 reset。"""
    return _team_execution_id.set(execution_id)


def reset_execution_id(token: object) -> None:
    _team_execution_id.reset(token)


def get_execution_id() -> str:
    """读取当前执行上下文的 execution_id（工具无状态，经 ContextVar 运行时注入）。"""
    return _team_execution_id.get()


async def emit(event: dict) -> None:
    """把事件放入当前上下文的事件通道（无通道时静默丢弃）。"""
    queue = _team_event_queue.get()
    if queue is not None:
        await queue.put(event)


# ---------------------------------------------------------------- 事件构造

def _build_event(event_type: TeamEventType, execution_id: str, data: dict, index: int | None = None) -> dict:
    """组装标准事件结构：{event, data, executionId, index?}"""
    event = {
        TeamEventField.EVENT: event_type.value,
        TeamEventField.DATA: data,
        TeamEventField.EXECUTION_ID: execution_id,
    }
    if index is not None:
        event[TeamEventField.INDEX] = index
    return event


def build_user_message(execution_id: str, conversation_id: str, query: str, index: int | None = None) -> dict:
    return _build_event(TeamEventType.USER_MESSAGE, execution_id,
                        {TeamEventField.CONVERSATION_ID: conversation_id, TeamEventField.QUERY: query}, index)


def build_run_start(execution_id: str, index: int | None = None) -> dict:
    return _build_event(TeamEventType.RUN_START, execution_id, {}, index)


def build_message(execution_id: str, delta: str, agent_id: str | None = None,
                  sub_execution_id: str | None = None, index: int | None = None) -> dict:
    data = {TeamEventField.DELTA: delta}
    if agent_id is not None:
        data[TeamEventField.AGENT_ID] = agent_id
    if sub_execution_id is not None:
        data[TeamEventField.SUB_EXECUTION_ID] = sub_execution_id
    return _build_event(TeamEventType.MESSAGE, execution_id, data, index)


def build_reasoning(execution_id: str, content: str, agent_id: str | None = None,
                    sub_execution_id: str | None = None, index: int | None = None) -> dict:
    data = {TeamEventField.CONTENT: content}
    if agent_id is not None:
        data[TeamEventField.AGENT_ID] = agent_id
    if sub_execution_id is not None:
        data[TeamEventField.SUB_EXECUTION_ID] = sub_execution_id
    return _build_event(TeamEventType.REASONING, execution_id, data, index)


def build_tool_call(execution_id: str, tool_call_id: str, tool_name: str,
                    arguments: dict | None = None, agent_id: str | None = None, index: int | None = None) -> dict:
    data = {
        TeamEventField.TOOL_CALL_ID: tool_call_id,
        TeamEventField.TOOL_NAME: tool_name,
    }
    if arguments is not None:
        data[TeamEventField.ARGUMENTS] = arguments
    if agent_id is not None:
        data[TeamEventField.AGENT_ID] = agent_id
    return _build_event(TeamEventType.TOOL_CALL, execution_id, data, index)


def build_tool_result(execution_id: str, tool_call_id: str, tool_name: str,
                      result: str | None = None, agent_id: str | None = None, index: int | None = None) -> dict:
    data = {
        TeamEventField.TOOL_CALL_ID: tool_call_id,
        TeamEventField.TOOL_NAME: tool_name,
    }
    if result is not None:
        data[TeamEventField.RESULT] = result
    if agent_id is not None:
        data[TeamEventField.AGENT_ID] = agent_id
    return _build_event(TeamEventType.TOOL_RESULT, execution_id, data, index)


def build_sub_start(execution_id: str, sub_execution_id: str, agent_id: str, index: int | None = None) -> dict:
    return _build_event(TeamEventType.SUB_START, execution_id,
                        {TeamEventField.SUB_EXECUTION_ID: sub_execution_id, TeamEventField.AGENT_ID: agent_id}, index)


def build_sub_done(execution_id: str, sub_execution_id: str, agent_id: str, text: str,
                   index: int | None = None) -> dict:
    return _build_event(TeamEventType.SUB_DONE, execution_id,
                        {TeamEventField.SUB_EXECUTION_ID: sub_execution_id,
                         TeamEventField.AGENT_ID: agent_id, TeamEventField.TEXT: text}, index)


def build_run_done(execution_id: str, text: str, index: int | None = None) -> dict:
    return _build_event(TeamEventType.RUN_DONE, execution_id, {TeamEventField.TEXT: text}, index)


def build_usage(execution_id: str, input_tokens: int, output_tokens: int, total_tokens: int,
                latency_ms: int | None = None, agent_id: str | None = None, index: int | None = None) -> dict:
    data = {
        TeamEventField.INPUT_TOKENS: input_tokens,
        TeamEventField.OUTPUT_TOKENS: output_tokens,
        TeamEventField.TOTAL_TOKENS: total_tokens,
    }
    if latency_ms is not None:
        data[TeamEventField.LATENCY_MS] = latency_ms
    if agent_id is not None:
        data[TeamEventField.AGENT_ID] = agent_id
    return _build_event(TeamEventType.USAGE, execution_id, data, index)


def build_error(execution_id: str, code: str | int, message: str, index: int | None = None) -> dict:
    return _build_event(TeamEventType.ERROR, execution_id,
                        {TeamEventField.CODE: code, TeamEventField.MESSAGE: message}, index)


def sse_line(event_dict: dict) -> str:
    """序列化为 SSE 一行：data: {json}\n\n（复用平台格式）"""
    return f"data: {json.dumps(event_dict, ensure_ascii=False)}\n\n"
