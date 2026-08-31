# -*- coding: UTF-8 -*-
"""团队对话 SSE 事件 —— 枚举与常量声明（纯声明，零逻辑）。

用户决策（2026-08-11）：枚举类和常量单独一个文件，只做声明，不加任何逻辑。
事件构造（build_*）/ 序列化（sse_line）在 event/canonical.py；
请求级事件通道对象（EventChannel）在 event/channel.py。
"""

from enum import Enum


class TeamEventType(str, Enum):
    """团队对话事件类型。"""

    MESSAGE = "message"
    RUN_START = "run_start"
    REASONING = "reasoning"
    TOOL_CALL = "tool_call"
    TOOL_RESULT = "tool_result"
    USAGE = "usage"
    SKILL_ACTIVATED = "skill_activated"
    ARTIFACT = "artifact"
    RUN_END = "run_end"
    ERROR = "error"


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
    SKILL_ID = "skillId"
    NAME = "name"
    VERSION_ID = "versionId"
    OBJECT_KEY = "objectKey"
    FILE_NAME = "fileName"
    SIZE = "size"
    MEDIA_TYPE = "mediaType"
    CHECKSUM = "checksum"
