#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
IR 相关接口定义 — 供 agentBuilder-engine workflow runner 使用的抽象契约。

实现类在 agentBuilder-engine 的 infrastructure 层提供：
- ObjectStorageProvider → agent_runtime.storage.object_storage
- ModelConfigProvider   → agent_runtime.common.model_providers
"""

from abc import ABC, abstractmethod
from typing import Optional

from agent_runtime.common.exception.errors import AgentBuilderError, ExtensionStatusCode
from openjiuwen.core.workflow.components.llm.llm_comp import LLMCompConfig


class StorageConfigError(AgentBuilderError):
    """存储配置异常 — 环境变量缺失或无效"""

    def __init__(self, msg: str = "", **kwargs):
        super().__init__(ExtensionStatusCode.STORAGE_CONFIG_ERROR, msg=msg, **kwargs)


class StorageReadError(AgentBuilderError):
    """存储读取异常 — 下载对象失败"""

    def __init__(self, msg: str = "", **kwargs):
        super().__init__(ExtensionStatusCode.STORAGE_READ_ERROR, msg=msg, **kwargs)


class ObjectStorageProvider(ABC):
    """对象存储提供者抽象类

    扩展时继承此类并实现 get_content()，然后通过
    WorkflowRunner 注入。
    """

    @abstractmethod
    async def get_content(self, object_key: str) -> str:
        """读取对象内容，返回 UTF-8 字符串

        Args:
            object_key: 对象 key（如 workflow/ir/xxx/xxx.json）

        Returns:
            str: 对象内容的 UTF-8 字符串

        Raises:
            StorageReadError: 读取失败
        """
        pass


class ModelConfigProvider(ABC):
    """模型配置提供者接口"""

    @abstractmethod
    def get_llm_config(
        self,
        ir_node: dict,
        global_config: Optional[dict] = None,
    ) -> LLMCompConfig:
        """从 IR 节点获取 LLM 组件配置

        Args:
            ir_node: IR 节点配置
            global_config: 全局模型配置

        Returns:
            LLMCompConfig: LLM 组件配置
        """
        pass
