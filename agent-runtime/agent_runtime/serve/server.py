"""
OLE FastAPI server — lightweight version of jiwen-server/serve/server.py
"""

import os
from contextlib import asynccontextmanager

from agent_runtime.common import settings
from agent_runtime.common.checkpointer_config import build_redis_checkpointer_config
from agent_runtime.common.exception.errors import AgentBuilderError
from agent_runtime.common.redis_manager import RedisClientManager
from agent_runtime.context.middleware import RequestContextMiddleware
from agent_runtime.serve.apis.orchestration import execution_app
from agent_runtime.serve.apis.user_variable_api import user_variable_router
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

# 初始化 prompt 模板
from jiuwen.common.init import init_prompt
from openjiuwen.core.common.logging import workflow_logger as logger
from openjiuwen.core.common.logging.log_config import configure_log_config
from openjiuwen.core.runner import Runner
from openjiuwen.core.session.checkpointer.checkpointer import CheckpointerFactory
from openjiuwen.core.sys_operation import SysOperationCard, OperationMode
from openjiuwen.core.sys_operation.config import (
    SandboxGatewayConfig,
    SandboxIsolationConfig,
    ContainerScope,
    PreDeployLauncherConfig,
)

from openjiuwen.extensions.sys_operation.sandbox import providers as _  # noqa: F401

# 导入 redis checkpointer 模块以触发 @CheckpointerFactory.register("redis") 装饰器
from openjiuwen.extensions.checkpointer.redis import checkpointer as _  # noqa: F401

prompt_dir = os.path.join(
    os.path.dirname(__file__), "..", "..", "jiuwen", "prompt", "template", "default"
)
if os.path.exists(prompt_dir):
    init_prompt(prompt_dir=prompt_dir)
    logger.info(f"Initialized prompt templates from {prompt_dir}")
else:
    logger.warning(f"Prompt template directory not found: {prompt_dir}")

# 注册工作流组件到组件池
from jiuwen.orchestration.flow.component_class_pool import component_class_pool
from agent_runtime.extension.workflow_node.flow_code import FlowCode, JIUWEN_CODE_TYPE

component_class_pool.register_component_class(JIUWEN_CODE_TYPE, FlowCode)
logger.info("Registered workflow component: jiuwen.code")

apps_map = [execution_app, user_variable_router]


@asynccontextmanager
async def lifespan(app: FastAPI):  # noqa: redefined-outer-name
    """define startup and shutdown logic here"""
    # 初始化 workflow_logger 日志级别（从环境变量 WORKFLOW_LOG_LEVEL 读取）
    workflow_log_level = settings.workflow_log.level.upper()
    configure_log_config(
        {
            "backend": "default",
            "level": "INFO",  # 全局默认级别保持 INFO
            "loggers": {
                "workflow": {"level": workflow_log_level},
                "sys_operation": {
                    "level": "WARNING"
                },  # 关闭 sys_operation 的 INFO 日志
            },
        }
    )
    logger.info(f"Workflow logger level set to: {workflow_log_level}")

    # 初始化 Redis 客户端
    redis_mgr = RedisClientManager.get_instance()
    redis_mgr.init()

    # 检查 Redis 连接是否正常
    if not redis_mgr.is_initialized:
        raise RuntimeError(
            "Redis client initialization failed, please check REDIS_* configuration"
        )

    redis_client = redis_mgr.get_client()
    try:
        await redis_client.ping()
        logger.info("Redis connection check passed")
    except Exception as e:
        raise RuntimeError(f"Redis connection check failed: {e}") from e

    # 创建并设置 Redis Checkpointer 为默认
    checkpointer_config = build_redis_checkpointer_config()
    redis_checkpointer = await CheckpointerFactory.create(checkpointer_config)
    CheckpointerFactory.set_default_checkpointer(redis_checkpointer)
    logger.info("Redis checkpointer initialized and set as default")

    # 注册 flow_code 专用的 SysOperation（local mode）
    sys_op_id = "flow_code_sys_op"
    if Runner.resource_mgr.get_sys_operation(sys_op_id) is None:
        card = SysOperationCard(id=sys_op_id, mode=OperationMode.LOCAL)
        add_res = Runner.resource_mgr.add_sys_operation(card)
        if add_res.is_ok():
            logger.info(
                f"Registered SysOperation: {sys_op_id} (mode={OperationMode.LOCAL})"
            )
        else:
            logger.error(f"Failed to register flow_code_sys_op: {add_res}")

    # 注册 SANDBOX SysOperation（根据 SECURITY_SANDBOX_SERVER 配置）
    sandbox_server = settings.security_sandbox.server
    if sandbox_server:
        sandbox_sys_op_id = "flow_code_sandbox_sys_op"
        if Runner.resource_mgr.get_sys_operation(sandbox_sys_op_id) is None:
            scope = ContainerScope.SYSTEM
            if settings.security_sandbox.scope == "session":
                scope = ContainerScope.SESSION
            sandbox_card = SysOperationCard(
                id=sandbox_sys_op_id,
                mode=OperationMode.SANDBOX,
                gateway_config=SandboxGatewayConfig(
                    isolation=SandboxIsolationConfig(container_scope=scope),
                    launcher_config=PreDeployLauncherConfig(
                        base_url=sandbox_server,
                        sandbox_type="aio",
                        idle_ttl_seconds=settings.security_sandbox.idle_ttl_seconds,
                    ),
                    timeout_seconds=settings.security_sandbox.timeout_seconds,
                ),
            )
            sandbox_res = Runner.resource_mgr.add_sys_operation(sandbox_card)
            if sandbox_res.is_ok():
                logger.info(
                    f"Registered SysOperation: {sandbox_sys_op_id} (mode={OperationMode.SANDBOX})"
                )
            else:
                logger.error(f"Failed to register sandbox SysOperation: {sandbox_res}")

    try:
        yield
    finally:
        # 关闭 Redis 客户端
        await redis_mgr.close()
        logger.info("Redis client closed")
        logger.info("OLE shutdown")


def instance_app(config: dict | None = None):
    """instance FastAPI server"""
    app = FastAPI(lifespan=lifespan, docs_url=None, redoc_url=None, openapi_url=None)  # noqa: redefined-outer-name
    app.add_middleware(RequestContextMiddleware)
    for i in apps_map:
        app.include_router(i)

    @app.exception_handler(AgentBuilderError)
    async def agent_builder_error_handler(request: Request, exc: AgentBuilderError):
        exec_id = getattr(request.state, "execution_id", "unknown")
        req_id = getattr(request.state, "request_id", "unknown")
        logger.error(
            f"AgentBuilderError: {exc}, execution_id={exec_id}, request_id={req_id}",
            exc_info=True,
        )
        return JSONResponse(
            status_code=500,
            content={
                "error": {
                    "code": getattr(exc, "code", "AgentBuilder_ERROR"),
                    "message": str(exc),
                }
            },
        )

    @app.exception_handler(Exception)
    async def generic_error_handler(request: Request, exc: Exception):
        exec_id = getattr(request.state, "execution_id", "unknown")
        req_id = getattr(request.state, "request_id", "unknown")
        logger.error(
            f"Unhandled exception: {type(exc).__name__}: {exc}, execution_id={exec_id}, request_id={req_id}",
            exc_info=True,
        )
        return JSONResponse(
            status_code=500,
            content={
                "error": {"code": "internal_error", "message": "Internal server error"}
            },
        )

    return app


# Create the app instance at module level
app = instance_app()
