#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
import time
from typing import Any

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.orchestration.flow.chat_manager import ChatManager
from jiuwen.orchestration.flow.stream.base import BaseStreamWriter, StreamData


class StreamWriter(BaseStreamWriter):
    """
    实现通过异步队列方式传递流式数据。
    """

    def __init__(self, chat_manager: ChatManager):
        self.chat_manager = chat_manager

    async def write(self, content: Any, **kwargs):
        """
        向异步队列中写入流式数据
        """
        # 异步写入流式数据内容
        await self.chat_manager.put_out(content)
        # 异步等待流式数据被消费
        await self.chat_manager.get_control()


class WorkflowStreamCallback:
    """
    workflow处理流式数据的类
    """

    _stream_writer = None

    def __init__(self, writer: StreamWriter):
        self._stream_writer = writer

    @property
    def stream_writer(self):
        """
        传递流式数据的对象
        """
        return self._stream_writer

    @staticmethod
    async def on_close():
        """
        关闭
        """
        return NotImplemented

    async def on_recv(self, stream_data: StreamData):
        """
        传递流式数据
        """
        execution_id = stream_data.execution_id
        if self._stream_writer:
            start_time = time.time()

            try:
                await self._stream_writer.write(stream_data)

                end_time = time.time()
                write_duration = end_time - start_time
                # 根据耗时级别记录不同日志
                if write_duration > 2.0:
                    logger.error(
                        f"[{execution_id}] VERY SLOW write: {write_duration:.3f}s"
                    )
            except Exception as e:
                end_time = time.time()
                write_duration = end_time - start_time
                logger.error(
                    f"[{execution_id}] Stream write FAILED after {write_duration:.3f}s, "
                    f"error: operation failed"
                )
                # 将所有异常都封装成 JiuWenBaseException
                raise JiuWenBaseException(
                    error_code=StatusCode.WORKFLOW_STREAM_CALLBACK_INSTANCE_INVALID_ERROR.code,
                    message="流式数据写入失败",
                ) from e
        else:
            logger.error(
                f"[{execution_id}] Stream writer is None! Raising JiuWenBaseException"
            )
            raise JiuWenBaseException(
                error_code=StatusCode.WORKFLOW_STREAM_CALLBACK_INSTANCE_INVALID_ERROR.code,
                message=StatusCode.WORKFLOW_STREAM_CALLBACK_INSTANCE_INVALID_ERROR.errmsg,
            )
