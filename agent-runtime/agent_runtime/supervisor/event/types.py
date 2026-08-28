"""Canonical conversation event types and field names."""

from enum import Enum


class ConversationEventType(str, Enum):
    RUN_START = "run_start"
    MESSAGE = "message"
    REASONING = "reasoning"
    TOOL_CALL = "tool_call"
    TOOL_RESULT = "tool_result"
    RUN_END = "run_end"
    ERROR = "error"
    WORKFLOW_NODE = "workflow_node"
    RUN_NODE = "workflow_node"  # backward-compatible name
    SKILL_ACTIVATED = "skill_activated"
    ARTIFACT = "artifact"
    USAGE = "usage"


class ConversationEventField:
    EVENT = "event"
    CONVERSATION_ID = "conversationId"
    DATA = "data"
    RUN_ID = "runId"
    PARENT_RUN_ID = "parentRunId"
    EXECUTION_TYPE = "executionType"
    AGENT_ID = "agentId"
    WORKFLOW_ID = "workflowId"
    NODE_ID = "nodeId"
    TOOL_ID = "toolId"
    TOOL_NAME = "toolName"
    SKILL_ID = "skillId"
    NAME = "name"
    VERSION_ID = "versionId"
    OBJECT_KEY = "objectKey"
    FILE_NAME = "fileName"
    SIZE = "size"
    MEDIA_TYPE = "mediaType"
    CHECKSUM = "checksum"
    EXECUTION_ID = "executionId"
    INDEX = "index"
    CREATED_TIME = "createdTime"
