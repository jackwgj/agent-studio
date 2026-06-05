#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
ir_runner - 商用九问 IR 到 agent-core 工作流的构造器

将商用九问的 IR JSON 文件转换为 agent-core 的 InvokableWorkflow 实例。

使用示例：
    from agent_runtime.ir_runner import IRWorkflowBuilder, ModelConfigProvider

    # 需要一个 ModelConfigProvider 实现（如 runtime.ir_config.EnvVarModelConfigProvider）
    builder = IRWorkflowBuilder(model_provider=my_model_provider)
    workflow = builder.build(ir_json)

    # 执行工作流
    import asyncio
    async def run():
        session = builder.create_session()
        async for chunk in workflow.stream({"query": "你好"}, session):
            print(chunk)

    asyncio.run(run())
"""

from .builder import IRWorkflowBuilder, IRBuilderConfig, IRBuildException
from .interfaces import (
    ObjectStorageProvider,
    ModelConfigProvider,
    StorageConfigError,
    StorageReadError,
)
from .registry import (
    get_handler,
    register_handler,
    unregister_handler,
    list_supported_types,
)
from .types import (
    IR_TYPE_START,
    IR_TYPE_END,
    IR_TYPE_LLM,
    IR_TYPE_BRANCH,
    IR_TYPE_LOOP,
    IR_TYPE_MESSAGE,
    IR_TYPE_HTTP,
    IR_TYPE_CODE,
    IR_TYPE_QUESTION,
    IR_TYPE_INTENT,
    IR_TYPE_TO_HANDLER,
)

__version__ = "0.1.0"

__all__ = [
    # Core API
    "IRWorkflowBuilder",
    "IRBuilderConfig",
    "IRBuildException",
    # Interfaces (ABC + exceptions)
    "ObjectStorageProvider",
    "ModelConfigProvider",
    "StorageConfigError",
    "StorageReadError",
    # Registry
    "get_handler",
    "register_handler",
    "unregister_handler",
    "list_supported_types",
    # Constants
    "IR_TYPE_START",
    "IR_TYPE_END",
    "IR_TYPE_LLM",
    "IR_TYPE_BRANCH",
    "IR_TYPE_LOOP",
    "IR_TYPE_MESSAGE",
    "IR_TYPE_HTTP",
    "IR_TYPE_CODE",
    "IR_TYPE_QUESTION",
    "IR_TYPE_INTENT",
    "IR_TYPE_TO_HANDLER",
]
