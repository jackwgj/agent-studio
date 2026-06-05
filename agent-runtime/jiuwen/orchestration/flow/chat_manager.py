#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.

"""
标识一次对话过程的对象，包括创建方法、异步队列操作操作方法、标志位设置获取方法。
"""

import asyncio

from jiuwen.common.log.base import logger
from jiuwen.orchestration.common.utils import del_safely


class ChatManager:
    """
    workflow一次执行过程中，操作异步队列进行信息传递以及管理执行状态信息
    """

    def __init__(self, loop):
        # 放置用户输入
        self._input_queue = asyncio.Queue()
        # 放置返回给用户的内容
        self._output_queue = asyncio.Queue()
        # 放置控制信息
        self._control_queue = asyncio.Queue()
        # 表示此次对话是否为一次新对话
        self._new_chat = True
        # 表示对话是否已经结束，正常结束或者异常结束
        self._finished = False
        # 处理此次会话的事件循环
        self._loop = loop
        self.tasks = set()

    def __del__(self):
        try:
            if self.tasks:
                logger.info(f"chat_manager task del: {self.tasks}")
                for task in self.tasks:
                    task.cancel()
        except Exception as e:
            logger.error(f"cancel task failed: {e}")

    @staticmethod
    def create_chat_manager(loop):
        """
        创建一个ChatManager对象

        loop为事件循环

        返回一个ChatManager对象
        """
        return ChatManager(loop=loop)

    def recover_chat_manager(self, loop):
        """
        Recover the attributes of a chat_manager.
        """
        if asyncio.all_tasks(self._loop) and loop != self._loop:
            logger.info(
                f"chat manager recover_chat_manager：{id(self)} found unfinished tasks, try to cancel them"
            )
            for task in asyncio.all_tasks(self._loop):
                task.cancel()
            self._loop.run_until_complete(
                asyncio.wait(
                    list(asyncio.all_tasks(self._loop)),
                    return_when=asyncio.ALL_COMPLETED,
                )
            )
        # 放置用户输入
        self._input_queue = asyncio.Queue()
        # 放置返回给用户的内容
        self._output_queue = asyncio.Queue()
        # 放置控制信息
        self._control_queue = asyncio.Queue()
        # 表示此次对话是否为一次新对话
        self._new_chat = True
        # 表示对话是否已经结束，正常结束或者异常结束
        self._finished = False
        # 处理此次会话的事件循环
        self._loop = loop

    async def get_in(self):
        """
        异步获取ChatManager对象中_input_queue中的值
        """
        res = await self._input_queue.get()
        # 标识队列消费任务完成，便于后续资源清理判断
        self._input_queue.task_done()
        return res

    async def put_in(self, message):
        """
        异步向ChatManager对象的_input_queue中放入值
        """
        await self._input_queue.put(message)

    async def get_out(self):
        """
        异步获取ChatManager对象中_output_queue中的值
        """
        res = await self._output_queue.get()
        # 标识队列消费任务完成，便于后续资源清理判断
        self._output_queue.task_done()
        return res

    async def put_out(self, message):
        """
        异步向ChatManager对象的_output_queue中放入值
        """
        await self._output_queue.put(message)

    async def get_control(self):
        """
        异步获取ChatManager对象中_control_queue中的值
        """
        res = await self._control_queue.get()
        # 标识队列消费任务完成，便于后续资源清理判断
        self._control_queue.task_done()
        return res

    async def put_control(self, message):
        """
        异步向ChatManager对象的_control_queue中放入值
        """
        await self._control_queue.put(message)

    def set_finish(self, status: bool):
        """
        设置ChatManager对象中的finish标识位

        finish为 True 标识此次对话结束，无论是正常结束还是超时等异常结束
        """
        self._finished = status

    def get_finish(self):
        """
        获取ChatManager对象中的finish标识位
        """
        return self._finished

    def set_new_chat(self, status: bool):
        """
        设置ChatManager对象中的_new_chat标识位

        _new_chat为 True 标识此次对话为新对话，否则为已存在对话
        """
        self._new_chat = status

    def get_new_chat(self):
        """
        获取ChatManager对象中的_new_chat标识位
        """
        return self._new_chat

    def get_loop(self):
        """
        获取ChatManager对象绑定的事件循环
        """
        return self._loop

    def get_control_qsize(self):
        """
        Get the qsize of the _control_queue.
        """
        return self._control_queue.qsize()

    async def aclear(self):
        """资源清理"""
        try:
            await asyncio.wait_for(self._await_queue_join(), timeout=5.0)
        except asyncio.TimeoutError:
            logger.error("队列join超时，执行强制清理")
            self._force_clear_all_queues()

        del_safely(self, "_input_queue")
        del_safely(self, "_output_queue")
        del_safely(self, "_control_queue")
        logger.info(f"chat manager：{id(self)} cleared")

    def clear(self):
        """资源清理"""
        logger.debug(
            f"input_queue: qsize={self._input_queue.qsize()}, "
            f"unfinished={getattr(self._input_queue, '_unfinished_tasks', 0)}"
        )
        logger.debug(
            f"output_queue: qsize={self._output_queue.qsize()}, "
            f"unfinished={getattr(self._output_queue, '_unfinished_tasks', 0)}"
        )
        logger.debug(
            f"control_queue: qsize={self._control_queue.qsize()}, "
            f"unfinished={getattr(self._control_queue, '_unfinished_tasks', 0)}"
        )

        try:
            self._loop.run_until_complete(
                asyncio.wait_for(self._await_queue_join(), timeout=5.0)
            )
        except asyncio.TimeoutError:
            logger.error("队列join超时，执行强制清理")
            self._force_clear_all_queues()

        del self._input_queue
        del self._output_queue
        del self._control_queue
        self._loop.close()
        del self._loop
        logger.info(f"chat manager：{id(self)} cleared")

    async def _await_queue_join(self):
        """
        等待异步队列任务完成，完成后执行相关清理动作
        """
        if hasattr(self, "_input_queue") and self._input_queue:
            logger.debug(
                f"input_queue: qsize={self._input_queue.qsize()}, "
                f"unfinished={getattr(self._input_queue, '_unfinished_tasks', 0)}"
            )
            await self._input_queue.join()
        if hasattr(self, "_output_queue") and self._output_queue:
            logger.debug(
                f"output_queue: qsize={self._output_queue.qsize()}, "
                f"unfinished={getattr(self._output_queue, '_unfinished_tasks', 0)}"
            )
            await self._output_queue.join()
        if hasattr(self, "_control_queue") and self._control_queue:
            logger.debug(
                f"control_queue: qsize={self._control_queue.qsize()}, "
                f"unfinished={getattr(self._control_queue, '_unfinished_tasks', 0)}"
            )
            await self._control_queue.join()

    def _force_clear_all_queues(self):
        """强制清理所有队列"""
        queues = [
            ("input", self._input_queue),
            ("output", self._output_queue),
            ("control", self._control_queue),
        ]

        for name, queue in queues:
            cleared_items = 0

            # 清空队列并标记任务完成
            while not queue.empty():
                try:
                    queue.get_nowait()
                    queue.task_done()
                    cleared_items += 1
                except (asyncio.QueueEmpty, ValueError):
                    break

            # 标记剩余的未完成任务
            unfinished = getattr(queue, "_unfinished_tasks", 0)
            for _ in range(unfinished):
                try:
                    queue.task_done()
                except ValueError:
                    break

            logger.info(f"强制清理{name}队列: 清除{cleared_items}个项目")
