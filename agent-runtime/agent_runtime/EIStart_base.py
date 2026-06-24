import os

import uvicorn

from agent_runtime.common import settings
from agent_runtime.common.uvicron_log_cfg import UvicornLogConfig


def _get_workers() -> int:
    """计算 uvicorn worker 数量。

    优先使用环境变量 GUNICORN_WORK_NUM；未设置时回退到 CPU 核心数 + 1。
    """
    configured = settings.server.workers
    if configured is not None:
        return configured
    return (os.cpu_count() or 1) + 1


def get_ssl_cert_config() -> dict:
    """读取 HTTPS 相关配置（参考旧版 agentBuilder-engine）"""
    if not settings.server.https:
        return {}

    password = (
        settings.server.tls_key_password.encode("utf-8")
        if settings.server.tls_key_password
        else None
    )

    return {
        "ssl_certfile": settings.server.tls_cert_path,
        "ssl_keyfile": settings.server.tls_key_path,
        "ssl_keyfile_password": password,
        "ssl_ciphers": settings.server.tls_ciphers,
    }


def main():
    host = settings.server.host
    port = settings.server.port
    log_level = settings.server.log_level.lower()

    app_path = "agent_runtime.serve.server:app"

    log_config_instance = UvicornLogConfig(log_level=log_level)

    # 获取 SSL 配置
    ssl_config = get_ssl_cert_config()

    uvicorn.run(
        app_path,
        host=host,
        port=port,
        log_level=log_level,
        log_config=log_config_instance.to_dict(),
        workers=_get_workers(),
        **ssl_config,
    )


if __name__ == "__main__":
    main()
