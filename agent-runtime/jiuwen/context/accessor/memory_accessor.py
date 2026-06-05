#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Dict, List

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.context.base import VARIABLE_NAME_KEY, VARIABLE_DESC_KEY
from jiuwen.context.memory_engine.client.memory_proxy import MemoryProxy
from jiuwen.context.memory_engine.config.config import MemoryScopeConfig, Variable
from jiuwen.serve.common.context import request_json


class MemoryAccessor:
    """memory accessor"""

    def __init__(self, model: BaseChatModel = None):
        self.model: BaseChatModel = model
        self.agent_id = request_json.get().get("agentId", "")
        self.__init_memory_proxy()

    def set_variable_config(self, variable_config: List[Dict] = None):
        """set memory variable config
        Args:
            variable_config: Memory engine: request-level variable extraction supported, demo:
            [
                {
                    "name": "...",
                    "description": "...",
                },
                ...
            ]
        """
        config = MemoryScopeConfig(
            mem_variables=[
                Variable(
                    name=item.get(VARIABLE_NAME_KEY),
                    description=item.get(VARIABLE_DESC_KEY),
                )
                for item in variable_config
            ]
        )
        self._memory_proxy.set_scope_config_sync(scope_id=self.agent_id, config=config)

    def add_memory(self, user_id: str, agent_id: str, message: BaseMessage):
        """add_memory
        Args:
            user_id (str): user id
            agent_id (str): agent_id in agent mode, or main agent_id in multi-agent mode
            message (str): memory content
        """
        try:
            self._memory_proxy.process_messages_sync(
                user_id=user_id, scope_id=agent_id, messages=[message]
            )
        except Exception:
            logger.error("memory proxy add memory failed.")

    def __init_memory_proxy(self):
        """init memory client from MemoryClientManager"""
        try:
            self._memory_proxy: MemoryProxy = MemoryProxy()
            self._memory_proxy.set_scope_llm(scope_id=self.agent_id, llm=self.model)
            if not self._memory_proxy:
                logger.error(
                    "Got None memory proxy instance, please check memory MemoryClientManager configuration."
                )
                raise JiuWenBaseException(
                    error_code=StatusCode.CONTEXT_ENGINE_PROXY_INIT_ERROR.code,
                    message=StatusCode.CONTEXT_ENGINE_PROXY_INIT_ERROR.errmsg.format(
                        error_msg="Got None memory client instance, please check MemoryClientManager configuration."
                    ),
                )
        except Exception as e:
            logger.error(
                "memory client init failed, please check MemoryClientManager configuration."
            )
            raise JiuWenBaseException(
                error_code=StatusCode.CONTEXT_ENGINE_PROXY_INIT_ERROR.code,
                message=StatusCode.CONTEXT_ENGINE_PROXY_INIT_ERROR.errmsg.format(
                    error_msg="memory client init failed, please check MemoryClientManager configuration."
                ),
            ) from e
