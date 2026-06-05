#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from typing import Optional

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.context.accessor.handler import ContextHandler
from jiuwen.context.executor.async_executor import AsyncContextExecutor
from jiuwen.context.processor.base import (
    BaseAsyncProcessorConfig,
    AsyncContextProcessor,
)
from jiuwen.context.processor.factory import ProcessorFactory


class ContextExecutor:
    def __init__(self, handler: ContextHandler, model: Optional[BaseChatModel] = None):
        self._handler: ContextHandler = handler
        self._model: Optional[BaseChatModel] = model
        self._async_executor: AsyncContextExecutor = AsyncContextExecutor(
            handler=handler, model=model
        )

    def start_async_task(self, config: Optional[BaseAsyncProcessorConfig] = None):
        """start async task loop"""
        try:
            async_processor = ProcessorFactory().create_processor(config, self._model)
            if not isinstance(async_processor, AsyncContextProcessor):
                raise JiuWenBaseException(
                    StatusCode.CONTEXT_ENGINE_CREATE_PROCESSOR_ERROR.code,
                    StatusCode.CONTEXT_ENGINE_CREATE_PROCESSOR_ERROR.errmsg.format(
                        error_msg=f"cannot run non-async processor {config.processor_type}"
                    ),
                )
        except JiuWenBaseException as e:
            raise e
        self._async_executor.add_processor(async_processor)
        self._async_executor.start_run()

    def end_async_task(self):
        """end async task"""
        self._async_executor.end_run()
