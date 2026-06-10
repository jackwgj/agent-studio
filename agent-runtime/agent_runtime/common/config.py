# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.

from enum import Enum
from typing import Optional

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class ServerSettings(BaseSettings):
    host: str = Field(default="127.0.0.1", validation_alias="SERVER_HOST")
    port: int = Field(default=8000, validation_alias="SERVER_PORT")
    log_level: str = Field(default="INFO", validation_alias="LOG_LEVEL")
    # HTTPS 配置（参考商业版 agentBuilder-engine）
    https: bool = Field(default=False, validation_alias="HTTPS")
    tls_cert_path: str = Field(default="", validation_alias="TLS_CERT_PATH")
    tls_key_path: str = Field(default="", validation_alias="TLS_CERT_KEY_PATH")
    tls_key_password: str = Field(default="", validation_alias="TLS_CERT_KEY_PASSWD")
    tls_ciphers: str = Field(default="TLSv1.2 TLSv1.3", validation_alias="TLS_CIPHERS")


class RedisMode(Enum):
    """Redis 连接模式。"""

    SINGLE = "single"
    CLUSTER = "cluster"
    SENTINEL = "sentinel"


class RedisSettings(BaseSettings):
    """Redis 配置。环境变量前缀统一为 REDIS_。"""

    # 模式配置
    mode: RedisMode = Field(default=RedisMode.SINGLE, validation_alias="REDIS_MODE")

    # 单机/哨兵模式配置
    host: str = Field(default="127.0.0.1", validation_alias="REDIS_HOST")
    port: int = Field(default=6379, validation_alias="REDIS_PORT")
    db: int = Field(default=0, validation_alias="REDIS_DATABASE")
    password: Optional[str] = Field(default=None, validation_alias="REDIS_PASSWORD")

    # 集群模式配置
    cluster_nodes: str = Field(default="", validation_alias="REDIS_CLUSTER_NODES")

    # 哨兵模式配置
    sentinel_master: str = Field(
        default="mymaster", validation_alias="REDIS_SENTINEL_MASTER"
    )
    sentinel_nodes: str = Field(default="", validation_alias="REDIS_SENTINEL_NODES")

    # 连接池配置
    max_connections: int = Field(default=50, validation_alias="REDIS_MAX_CONNECTIONS")
    socket_timeout: int = Field(default=5, validation_alias="REDIS_SOCKET_TIMEOUT")
    socket_connect_timeout: int = Field(
        default=5, validation_alias="REDIS_SOCKET_CONNECT_TIMEOUT"
    )

    # SSL 配置（建议直接使用默认值就可以）
    ssl_enabled: bool = Field(default=False, validation_alias="REDIS_SSL_ENABLED")
    ssl_ca_cert: str = Field(default="", validation_alias="REDIS_SSL_CA_CERT")
    ssl_cert_file: str = Field(default="", validation_alias="REDIS_SSL_CERT_FILE")
    ssl_key_file: str = Field(default="", validation_alias="REDIS_SSL_KEY_FILE")

    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )


class LLMSettings(BaseSettings):
    api_key: str = Field(default="sk-placeholder", validation_alias="IR_LLM_API_KEY")
    api_base: str = Field(default="", validation_alias="MODEL_ROUTER_API")
    ssl_verify: bool = Field(default=False, validation_alias="IR_LLM_SSL_VERIFY")
    # LLM 调用读取超时（秒），与原版 agentBuilder-engine 的 STREAM_READ_TIMEOUT 保持一致
    timeout: float = Field(default=900.0, validation_alias="STREAM_READ_TIMEOUT")

    # Model configuration defaults
    model_name: str = Field(default="", validation_alias="IR_LLM_MODEL_NAME")
    temperature: float = Field(default=0.5, validation_alias="IR_LLM_TEMPERATURE")
    top_p: float = Field(default=0.5, validation_alias="IR_LLM_TOP_P")
    max_tokens: int = Field(default=4096, validation_alias="IR_LLM_MAX_TOKENS")

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        protected_namespaces=("settings_",),
    )


class ObjectStorageSettings(BaseSettings):
    server: str = Field(default="", validation_alias="DATASOURCE_OBS_SERVER")
    bucket: str = Field(default="", validation_alias="DATASOURCE_OBS_BUCKET")
    access_key: str = Field(default="", validation_alias="DATASOURCE_OBS_AK")
    secret_key: str = Field(default="", validation_alias="DATASOURCE_OBS_SK")
    enable_ssl: bool = Field(
        default=False, validation_alias="DATASOURCE_OBS_ENABLE_SSL"
    )
    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )


class HealthCheckSettings(BaseSettings):
    """健康检查配置。"""

    custom_rsp: str = Field(default="", validation_alias="CUSTOM_HEALTH_CHECK_RSP")

    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )


class SecuritySandboxSettings(BaseSettings):
    """Security sandbox configuration for code execution."""

    server: str = Field(default="", validation_alias="SECURITY_SANDBOX_SERVER")
    ssl_verify: bool = Field(
        default=False, validation_alias="SECURITY_SANDBOX_SSL_VERIFY"
    )
    idle_ttl_seconds: int = Field(
        default=600, validation_alias="SECURITY_SANDBOX_IDLE_TTL"
    )
    timeout_seconds: int = Field(
        default=300, validation_alias="SECURITY_SANDBOX_TIMEOUT"
    )
    scope: str = Field(default="system", validation_alias="SECURITY_SANDBOX_SCOPE")

    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )


class WorkflowLogSettings(BaseSettings):
    """Workflow logger configuration."""

    level: str = Field(default="INFO", validation_alias="WORKFLOW_LOG_LEVEL")

    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )


class Settings:
    server = ServerSettings()
    redis = RedisSettings()
    llm = LLMSettings()
    object_storage = ObjectStorageSettings()
    health_check = HealthCheckSettings()
    security_sandbox = SecuritySandboxSettings()
    workflow_log = WorkflowLogSettings()


settings = Settings()
