#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import functools
from typing import Dict, Optional

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.log.base import logger
from jiuwen.common.utils.singleton import Singleton
from jiuwen.context.processor.base import BaseContextProcessor, BaseProcessorConfig


class ProcessorFactory(metaclass=Singleton):
    def __init__(self):
        self.registered_processors: Dict[str, type] = dict()
        self.registered_processor_configs: Dict[str, type] = dict()

    @classmethod
    def register(cls, processor_type: str, config: type, processor_class=None):
        """register processor type and config to factory"""

        @functools.wraps(processor_class)
        def register_processor_class(processor_class: type):
            cls().registered_processors[processor_type] = processor_class
            cls().registered_processor_configs[processor_type] = config
            return processor_class

        return register_processor_class

    def create_processor(
        self, config: BaseProcessorConfig, model: Optional[BaseChatModel] = None
    ) -> BaseContextProcessor:
        """create processor"""
        processor_type = config.processor_type
        if processor_type not in self.registered_processors:
            raise JiuWenBaseException(
                StatusCode.CONTEXT_ENGINE_CREATE_PROCESSOR_ERROR.code,
                StatusCode.CONTEXT_ENGINE_CREATE_PROCESSOR_ERROR.errmsg.format(
                    error_msg=f"{processor_type} doesn't exist"
                ),
            )

        try:
            processor = self.registered_processors[processor_type](config, model)
        except Exception as e:
            logger.error(
                f"Cannot create processor for {processor_type}, reason: {str(e)}",
                simple_log=f"Cannot create processor for {processor_type}, reason: {type(e).__name__}",
            )
            raise JiuWenBaseException(
                StatusCode.CONTEXT_ENGINE_CREATE_PROCESSOR_ERROR.code,
                StatusCode.CONTEXT_ENGINE_CREATE_PROCESSOR_ERROR.errmsg.format(
                    error_msg=f"create processor {processor_type} failed"
                ),
            ) from e
        return processor
