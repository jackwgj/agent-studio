#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import asyncio
from abc import abstractmethod
from typing import Callable, Optional

from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.log.base import logger
from jiuwen.context.base import ContextWindow, ContextConstant, ContextHandleType
from pydantic import BaseModel, Field


class BaseProcessorConfig(BaseModel):
    processor_type: str


class BaseAsyncProcessorConfig(BaseProcessorConfig):
    processor_type: str
    update_strategy: str = Field(default=ContextHandleType.UPDATE_NOTHING.value)


class BaseContextProcessor:
    def __init__(
        self, config: BaseProcessorConfig, model: Optional[BaseChatModel] = None
    ):
        self._config = config
        self._model = model

    @abstractmethod
    def run(self, context: ContextWindow) -> ContextWindow:
        """run context processing"""
        pass


class AsyncProcessCallbacks:
    def __init__(
        self,
        finished_callback: Optional[Callable] = None,
        data_update_callback: Optional[Callable] = None,
        exception_callback: Optional[Callable] = None,
    ):
        self.finished_callback: Callable[["AsyncContextProcessor"], None] = (
            finished_callback or AsyncProcessCallbacks.default_finished_callback
        )
        self.data_update_callback: Callable[[ContextWindow], None] = (
            data_update_callback or AsyncProcessCallbacks.default_data_update_callback
        )
        self.exception_callback: Callable[["AsyncContextProcessor"], None] = (
            exception_callback or AsyncProcessCallbacks.default_exception_callback
        )

    @staticmethod
    def default_finished_callback(processor: "AsyncContextProcessor"):
        """default finished callback"""
        pass

    @staticmethod
    def default_data_update_callback(context: ContextWindow):
        """default data update callback"""
        pass

    @staticmethod
    def default_exception_callback(processor: "AsyncContextProcessor"):
        """default exception callback"""
        pass


class AsyncContextProcessor(BaseContextProcessor):
    def __init__(
        self, config: BaseAsyncProcessorConfig, model: Optional[BaseChatModel] = None
    ):
        super(AsyncContextProcessor, self).__init__(config, model)
        self._callbacks: AsyncProcessCallbacks = AsyncProcessCallbacks()

    async def __call__(self, context: ContextWindow):
        """call async context processing"""
        try:
            output = await asyncio.wait_for(
                self.arun(context),
                timeout=ContextConstant.DEFAULT_ASYNC_PROCESS_TIMEOUT,
            )
        except TimeoutError as e:
            logger.error(
                f"async processor {self._config.processor_type} timeout: {str(e)}"
            )
            self._callbacks.exception_callback(self)
            return
        except Exception as e:
            logger.error(
                f"async processor {self._config.processor_type} error: {str(e)}"
            )
            self._callbacks.exception_callback(self)
            return
        self._callbacks.finished_callback(self)
        self._callbacks.data_update_callback(output)

    def set_callbacks(self, callbacks: AsyncProcessCallbacks):
        """set callbacks to processor"""
        if callbacks:
            self._callbacks = callbacks

    @abstractmethod
    async def arun(self, context: ContextWindow) -> ContextWindow:
        """async processing"""
        pass

    @abstractmethod
    async def condition(self, context: ContextWindow) -> bool:
        """trigger of doing async processing"""
        pass

    def get_update_strategy(self) -> ContextHandleType:
        """get update strategy"""
        return self._config.update_strategy

    def run(self, context: ContextWindow) -> ContextWindow:
        """async processor cannot implemented run"""
        raise NotImplementedError(
            "async context processor's process method not implemented"
        )
