#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.

from enum import Enum


class ExecutionStatus(Enum):
    START = "start"
    USER_INTERACT = "user_interact"
    END = "end"
    LOOP_RECOVER = "loop_recover"
    LOOP_RUNNING = "loop_running"
    LOOP_FINISH = "loop_finish"


class NodeType(Enum):
    """
    节点类型的枚举类。

    定义了工作流中不同节点类型的常量，每个常量代表一种特定的节点类型。
    """

    ERROR_END = "jiuwen.exception"
    """表示异常结束节点类型。"""

    START = "jiuwen.start"
    """表示工作流开始节点类型。"""

    END = "jiuwen.end"
    """表示工作流结束节点类型。"""

    BRANCH = "jiuwen.branch"
    """表示分支节点类型。"""

    CODE = "jiuwen.code"
    """表示代码执行节点类型。"""

    API = "jiuwen.plugin"
    """表示API插件节点类型。"""

    LLM = "jiuwen.LLMComponent"
    """表示LLM组件节点类型。"""

    TASK = "jiuwen.taskFlow"
    """表示任务流节点类型。"""

    QUESTIONER = "jiuwen.questioner"
    """表示提问器节点类型。"""

    INTENT_DETECTION = "jiuwen.intentDetection"
    """表示意图识别节点类型。"""

    MESSAGE = "jiuwen.message"
    """表示消息节点类型。"""

    SUB_WORKFLOW = "jiuwen.workflowComposite"
    """表示子工作流节点类型。"""

    INPUT = "jiuwen.input"
    """表示输入节点类型。"""

    AGGREGATE = "jiuwen.aggregation"
    """表示聚合节点类型。"""

    LOOP = "jiuwen.loop"
    """表示循环节点类型。"""

    SET_VARIABLE = "jiuwen.setVariable"
    """表示设置变量节点类型。"""

    FLOW_AGENT = "jiuwen.agent"
    """表示Agent节点类型。"""

    AGENT = "jiuwen.LLMReAct"
    """表示LLM节点类型。"""

    MCP = "jiuwen.mcp"
    """表示MCP节点类型。"""

    QA = "EI.qa"
    """表示问答节点类型。"""

    EXTRACTOR = "jiuwen.extractor"
    """表示参数提取节点类型。"""

    CARD = "jiuwen.card"
    """表示卡片节点类型。"""

    STREAM_TRANSFORM = "jiuwen.streamTransform"
    """表示流式转换节点类型。"""


class LoopType(Enum):
    """
    循环节点类型枚举值
    """

    ARRAY = "arrayLoop"
    NUMBER = "numLoop"
    INFINITE = "infiniteLoop"


class StreamDataMsg(Enum):
    """
    流式数据msg枚举
    """

    SUCCESS = "success"
    FAIL = "fail"
    MESSAGE_END = "message_end"
    FINISH = "finish"


class ChatHistoryRole(Enum):
    """
    对话历史角色类型
    """

    USER = "user"
    ASSISTANT = "assistant"
    SYSTEM = "system"


class WorkflowLLMResponseType(Enum):
    """
    LLM组件响应类型枚举
    """

    JSON = "json"
    MARKDOWN = "markdown"
    TEXT = "text"
