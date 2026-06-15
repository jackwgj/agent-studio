#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
from abc import ABC, abstractmethod
from enum import Enum
from typing import Any, Union, Dict, Optional

from pydantic import BaseModel, Field


class BaseStreamWriter(ABC):
    """
    流式writer基类，进行流式数据传递
    """

    @abstractmethod
    async def write(self, content: Any, **kwargs):
        """
        传递流式数据
        """
        pass


class StreamCode(Enum):
    """
    流式输出状态码定义
    """

    START = 2000  # 流式开始
    WORKFLOW_START = (
        3000  # workflow start组件开始执行，当本次调用经过start组件的时候会存在
    )
    WORKFLOW_END = 4000  # workflow end组件执行完毕后输出
    MESSAGE_END = (
        5000  # 一个组件（消息、结束、提问器、规划）的流式结束标识，带有该组件的总结信息
    )
    PARTIAL_CONTENT = (
        1206  # 部分输出（流式输出），Output接口输出的数据都会带上该响应码。
    )
    ERROR = -1  # 流式过程中组件执行错误
    WORKFLOW_EXCEPTION = -2  # 流式过程中workflow执行错误
    CONTINUE = (
        1000  # 客户端应该继续请求，WaitForUserInput接口的feedback消息会带上该响应码。
    )
    FINISH = 0  # 表示最后一条消息。
    WORKFLOW_NODE_MESSAGE = 6000  # 表示中间调试信息
    CONTROLLER_INTERMEDIATE_MESSAGE = 7000  # 上下文信息
    CONTROLLER_WAIT_USER_INPUT_MESSAGE = 8000  # 控制器等待用户输入表示符
    CONTROLLER_START_MESSAGE = 9000  # 控制器任务开始标识符
    CONTROLLER_FINISH_MESSAGE = 10000  # 控制器任务结束标识符
    WORKFLOW_BLOCKED_MESSAGE = 11000  # 工作流跳出事件
    WORKFLOW_RESUME_MESSAGE = 12000  # 工作恢复事件
    TASK_TERMINATED_MESSAGE = 13000  # 工作流终止事件
    CONTROLLER_AGENT_HANDOFF_MESSAGE = 14000  # 控制器调用其他Agent事件
    CONTROLLER_AGENT_INTERRUPT_MESSAGE = 15000

    # PlanExecute 专属事件
    PE_SCENE_MATCH = 16000  # 场景匹配结果
    PE_PLAN_END = 17000  # 任务规划完成
    PE_STEP_START = 18000  # 步骤开始
    PE_STEP_END = 19000  # 步骤结束（复用此事件表示步骤完成）
    PE_TASK_COMPLETE = 20000  # 任务完成
    # PlanExecute 插件执行事件
    PE_FUNCTION_CALL = 21000  # 插件调用信息生成
    PE_API_EXEC_DATA = 22000  # 插件执行结果
    PE_FUNCTION_CALL_END = 23000  # 插件调用完成
    STATISTIC_DATA = 24000  # 统计数据
    SUMMARY_RESPONSE = 25000  # 总结响应

    INTENT_MSG = 1208  # 意图识别响应


class StreamData:
    """
    一条流式数据的表达结构
    """

    def __init__(
        self,
        code: StreamCode | Any,
        msg: str,
        data: Any,
        execution_id: str,
        index: int = 0,
        is_struct_message: bool = False,
    ):
        self._code = code
        self.msg = msg
        self.data = data
        self.execution_id = execution_id
        self.index = index
        self.is_struct_message = is_struct_message

    def __str__(self):
        return (
            f"StreamData(code={self.code}, msg={self.msg}, data={self.data}, index={self.index},"
            f"execution_id={self.execution_id}, is_struct_message={self.is_struct_message})"
        )

    @property
    def code(self):
        """
        获取流式数据的状态码值

        Returns:
            int: 如果 _code 是 StreamCode 枚举类型，返回其对应的整数值；否则直接返回 _code
        """
        if isinstance(self._code, StreamCode):
            return self._code.value
        return self._code


class SseStreamData(BaseModel):
    stream_id: str
    is_finish: bool
    content: Union[str, None] = Field(default=None)
    ext: Union[Dict, None] = Field(default=None)
    output_mode: int = Field(
        default=0, description="0：一次性完整输出消息；1：分多次输出消息"
    )
    content_type: int = Field(
        default=0, description="流式输出内容为，0：纯文本，1：序列化字符串"
    )
    return_type: Optional[int] = Field(
        default=0,
        description="最后一条 message，0: 是否直接输出给用户，1：大模型总结输出给用户，默认直接输出给用户",
    )
    context_mode: Optional[int] = Field(
        default=0, description="这条消息是否对话历史，0表示加入，默认0"
    )
