#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import asyncio
from typing import List, Optional

from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.log.base import logger
from jiuwen.context.accessor.handler import ContextHandler
from jiuwen.context.base import ContextConstant
from jiuwen.context.processor.base import AsyncContextProcessor, AsyncProcessCallbacks


class AsyncContextExecutor:
    def __init__(self, handler: ContextHandler, model: Optional[BaseChatModel] = None):
        self._model: Optional[BaseChatModel] = model
        self._handler: ContextHandler = handler
        self._pending_processors: List[AsyncContextProcessor] = []
        self._main_coroutine: Optional[asyncio.Future] = None
        self._is_running = False

    def start_run(self):
        """start async task loop"""
        if not self._pending_processors or self._main_coroutine:
            return
        self._is_running = True
        try:
            self._main_coroutine = asyncio.create_task(self._start())
        except RuntimeError as e:
            self._is_running = False
            logger.error(f"cannot start async context processing: {str(e)}")

    def end_run(self):
        """end async task loop"""
        if not self._main_coroutine:
            return
        self._is_running = False
        if not self._main_coroutine.cancel():
            logger.error("cannot end async context processing.")
            return
        self._main_coroutine = None

    def add_processor(self, processor: AsyncContextProcessor):
        """add a processor to pending processors list"""
        if not processor:
            return
        processor.set_callbacks(
            AsyncProcessCallbacks(
                finished_callback=self._process_finished,
                exception_callback=self._process_abnormal,
                data_update_callback=self._handler.get_handler(
                    processor.get_update_strategy()
                ),
            )
        )
        self._pending_processors.append(processor)

    async def _start(self):
        """entry task of async task loop"""
        while self._is_running:
            pending_processors = self._pending_processors
            self._pending_processors = []
            for async_processor in pending_processors:
                context_window = self._handler.create_context_window()
                if not async_processor.condition(context_window):
                    self._pending_processors.append(async_processor)
                    continue
                asyncio.create_task(async_processor(context_window))

            await asyncio.sleep(ContextConstant.DEFAULT_ASYNC_TASK_LOOP_PERIOD)

    def _process_finished(self, async_processor: AsyncContextProcessor):
        """process finished callback"""
        self._pending_processors.append(async_processor)

    def _process_abnormal(self, async_processor: AsyncContextProcessor):
        """process abnormal callback"""
        self._pending_processors.append(async_processor)
