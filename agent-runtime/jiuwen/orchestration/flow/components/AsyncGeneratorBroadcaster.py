#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""AsyncGeneratorBroadcaster"""

import asyncio

from jiuwen.common.log.base import logger


class AsyncGeneratorBroadcaster:
    """异步生成器广播器：一个生成器，多个消费者，按output_id索引"""

    def __init__(self, source_generator):
        self.source_generator = source_generator
        self.queues = {}  # key: output_id, value: 该output_id的所有消费者队列
        self.history = []  # 历史数据缓存
        self.lock = asyncio.Lock()
        self._started = False
        self._finished = False
        self._error = None
        self._broadcast_task = None

    def get_generator_by_id(self, output_id):
        """生成器"""
        queue = asyncio.Queue()

        async def independent_generator():
            for item in self.history:
                yield item
            if self._finished:
                if self._error:
                    raise self._error
                return
            async with self.lock:
                if output_id not in self.queues:
                    self.queues[output_id] = []
                self.queues[output_id].append(queue)
                if not self._started:
                    self._started = True
                    self._broadcast_task = asyncio.create_task(self._broadcast())
            try:
                while True:
                    item = await queue.get()
                    if item is None:
                        break
                    if isinstance(item, Exception):
                        raise item
                    yield item
            finally:
                async with self.lock:
                    if output_id in self.queues and queue in self.queues[output_id]:
                        self.queues[output_id].remove(queue)

        return independent_generator()

    async def _broadcast(self):
        """广播原始数据到所有队列"""
        try:
            async for item in self.source_generator:
                self.history.append(item)
                async with self.lock:
                    current_queues = {}
                    for output_id, queues in self.queues.items():
                        current_queues[output_id] = list(queues)
                for _, queues in current_queues.items():
                    for queue in queues:
                        try:
                            await queue.put(item)
                        except Exception as e:
                            logger.error(f"广播失败: {e}")
            self._finished = True
            async with self.lock:
                for queues in self.queues.values():
                    for queue in list(queues):
                        try:
                            await queue.put(None)
                        except Exception as e:
                            logger.error(f"发送结束信号失败: {e}")

        except Exception as e:
            self._error = e
            self._finished = True
            async with self.lock:
                for queues in self.queues.values():
                    for queue in list(queues):
                        try:
                            await queue.put(e)
                        except Exception as send_error:
                            logger.error(f"发送错误信息到队列失败: {send_error}")
