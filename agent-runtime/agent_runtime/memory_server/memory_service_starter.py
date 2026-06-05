import logging.config
import os

import uvicorn
from fastapi import FastAPI
from memory_service.serve.memory_private_api import router as memory_private_router
from memory_service.serve.memory_user_profile_api import (
    router as memory_user_profile_router,
)
from memory_service.serve.memory_user_profile_config_api import (
    router as memory_user_profile_config_router,
)
from memory_service.serve.memory_user_profile_config_private_api import (
    router as memory_user_profile_config_private_router,
)
from memory_service.serve.memory_user_profile_private_api import (
    router as memory_user_profile_private_router,
)

import config.settings
from filter.auth_token_filter import IamAuthFilterMiddleware
from filter.global_exception_handler_filter import GlobalExceptionMiddleware
from filter.language_filter import LanguageFilterMiddleware
from log.logging_config import logging_config


def init_logging():
    os.makedirs(config.settings.LOG_PATH, exist_ok=True)
    # 在应用启动前配置日志，确保早期初始化的日志能正确输出
    logging.config.dictConfig(logging_config)


def init_memory_engine():
    # 注意，memory_engine_util类中涉及到使用Crypto加解密，为了防止Crypto初始化，只能在使用时再去import get_mem_engine，不能在最外层倒入，否则在环境上会启动失败
    from memory_service.memory.memory_engine_util import get_mem_engine

    get_mem_engine()


def init_redis_util():
    # 注意，async_redis_util类中涉及到使用Crypto加解密，为了防止Crypto初始化，只能在使用时再去import get_async_redis_client，不能在最外层倒入，否则在环境上会启动失败
    from utils.async_redis_util import get_async_redis_client

    get_async_redis_client()


def init_ssl_config() -> dict:
    # 注意，ssl_config类中涉及到使用Crypto加解密，为了防止Crypto初始化，只能在使用时再去import get_ssl_cert_config，不能在最外层倒入，否则在环境上会启动失败
    from memory_service.config.ssl_config import get_ssl_cert_config

    return get_ssl_cert_config()


app = FastAPI(title="Memory Service")
iam_auth_white_list = (
    config.settings.IAM_AUTH_WHITE_LIST.split(",")
    if config.settings.IAM_AUTH_WHITE_LIST
    else []
)
app.add_middleware(IamAuthFilterMiddleware, whitelist_routes=iam_auth_white_list)
app.add_middleware(GlobalExceptionMiddleware)
app.add_middleware(LanguageFilterMiddleware)
app.include_router(memory_user_profile_router)
app.include_router(memory_user_profile_config_router)
app.include_router(memory_user_profile_config_private_router)
app.include_router(memory_user_profile_private_router)
app.include_router(memory_private_router)

if __name__ == "__main__":
    init_logging()
    init_redis_util()
    init_memory_engine()
    ssl_config = init_ssl_config()
    uvicorn.run(
        app,
        host=config.settings.SERVER_HOST,
        port=config.settings.SERVER_PORT,
        timeout_keep_alive=config.settings.REQ_TIME_OUT,
        log_config=logging_config,
        **ssl_config,
    )
