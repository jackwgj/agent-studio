# -*- coding: UTF-8 -*-
"""团队对话事件通道 —— 请求级资源对象（EventChannel）+ ContextVar 注入。

请求级资源（event_queue / execution_id）封装为 EventChannel 对象，每轮 Supervisor 创建；
经 ContextVar 注入（等价 Java ThreadLocal：单上下文/单请求一对一，并发请求互不干扰，非全局共享）。

工具通过 get_channel() 取当前请求的 channel 写事件；
只暴露 execution_id / emit / get 最小接口，内部 queue 不直接暴露。
"""

import asyncio
from contextvars import ContextVar

from typing import Any


class EventChannel:
    """本轮请求的事件通道：封装 execution_id + 事件队列。

    请求级资源，每轮 Supervisor 创建一个；主生成器消费（get），
    工具/子 Agent 写事件（emit）。
    """

    def __init__(
        self,
        execution_id: str,
        conversation_id: str,
        *,
        parent_run_id: str | None = None,
        execution_type: str = "agent",
        agent_id: str | None = None,
        queue: asyncio.Queue | None = None,
    ):
        self._execution_id = execution_id
        self._conversation_id = conversation_id
        self._parent_run_id = parent_run_id
        self._execution_type = execution_type
        self._agent_id = agent_id
        self._queue: asyncio.Queue = queue or asyncio.Queue()

    @property
    def conversation_id(self) -> str:
        return self._conversation_id

    @property
    def execution_id(self) -> str:
        """本轮唯一标识（监督者一轮，全量 uuid4）"""
        return self._execution_id

    @property
    def parent_run_id(self) -> str | None:
        return self._parent_run_id

    @property
    def execution_type(self) -> str:
        return self._execution_type

    @property
    def agent_id(self) -> str | None:
        return self._agent_id

    def child(self, run_id: str, *, agent_id: str | None = None) -> "EventChannel":
        """Create a child-run view that forwards events to the root queue."""
        return EventChannel(
            run_id,
            self._conversation_id,
            parent_run_id=self._execution_id,
            execution_type="sub_agent",
            agent_id=agent_id,
            queue=self._queue,
        )

    async def emit(self, event: dict) -> None:
        """把事件写入队列（供主生成器消费）。"""
        await self._queue.put(event)

    async def get(self) -> Any:
        """从队列取下一个事件（主生成器消费）。"""
        return await self._queue.get()


# 当前执行上下文的事件通道。asyncio 为每个 Task 复制 Context，主 Agent 调用链
# 同属一个 Context，读写一致；并发请求各自独立。
_current_channel: ContextVar[EventChannel | None] = ContextVar("team_event_channel", default=None)


def set_channel(channel: EventChannel) -> object:
    """设置当前执行上下文的事件通道，返回 token 供 reset。"""
    return _current_channel.set(channel)


def reset_channel(token: object) -> None:
    _current_channel.reset(token)


def get_channel() -> EventChannel | None:
    """读取当前执行上下文的事件通道（工具无状态，经 ContextVar 运行时注入）。"""
    return _current_channel.get()
