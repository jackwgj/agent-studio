#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from jiuwen.context.memory_engine.client import MemoryProxy
from jiuwen.context.memory_engine.config.memory_ir_config import MemoryIrConfig
from jiuwen.serve.common.context import request_json
from rag.controller.msg_context import MsgContext


class ContextManager:
    def __init__(self, agent_context=None, memory_config: MemoryIrConfig = None):
        self.msg_context: MsgContext = MsgContext()  # 上下文消息列表
        self.workflow_contexts: list = []  # 未完成的工具调用命令栈
        self.workflow_functions: list = []  # 上次执行的工具
        self.memory_config = memory_config
        self.global_variables = {}
        self.memory_app_id = ""
        self.engine = {
            "enable_memory": agent_context.enable_memory,
            "mem_variables": agent_context.mem_variables,
        }
        self.memory_enable: bool = (
            isinstance(memory_config, MemoryIrConfig) and memory_config.enable_memory
        )
        if self.memory_enable or (agent_context and agent_context.enable_memory):
            self._memory_proxy = MemoryProxy()

    def get_global_variables(self, key, default_value=None):
        return self.global_variables.get(key, default_value)

    def set_global_variables(self, key, value):
        self.global_variables[key] = value

    def set_memory_request_params(
        self, memory_app_id: str, memory_enable_user_profile: bool
    ):
        """set request params of memory"""
        self.memory_app_id = memory_app_id
        self.memory_enable = self.memory_enable and memory_enable_user_profile

    async def get_memory_variables_dict(self):
        """get memory variables dict"""
        if not self.engine["enable_memory"] or not self.engine["mem_variables"]:
            return {}
        user_id = request_json.get().get("userId", "")
        scope_id = request_json.get().get("agentId", "")
        if not user_id or not scope_id:
            return {}
        return await self._memory_proxy.list_user_variables(user_id, scope_id)
