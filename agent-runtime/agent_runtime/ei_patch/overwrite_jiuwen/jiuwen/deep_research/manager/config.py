# -*- coding: UTF-8 -*-
from pathlib import Path
from typing import Any
from urllib.parse import quote_plus

from pydantic import Field, computed_field, AliasChoices, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # 数据库类型配置 (mysql/sqlite)
    db_type: str = "mysql"

    # mysql配置
    db_host: str = ""
    db_port: int = 3306
    db_user: str = ""
    db_password: str = ""
    deepsearch_db_name: str = ""

    # mysql数据库连接池配置， 避免断联时间过久导致前端需要发两次请求
    db_pool_pre_ping: bool = True
    db_pool_recycle: int = 3600

    # sqlite配置
    sqlite_db_path: str = "data/databases"
    deepsearch_sqlite_db: str = "agent.db"

    # Checkpointer 配置 (in_memory / persistence / redis)
    checkpointer_type: str = "redis"
    checkpointer_db_type: str = "sqlite"
    checkpointer_db_path: str = "data/databases/checkpointer.db"

    # --- Redis 基础字段 (自动从环境变量映射) ---
    # 使用 Field 的 validation_alias 可以精准匹配你现有的环境变量名
    redis_host: str = Field(
        default="127.0.0.1", validation_alias="DATASOURCE_REDIS_HOST"
    )
    redis_port: int = Field(default=6379, validation_alias="DATASOURCE_REDIS_PORT")
    redis_user: str = Field(default="", validation_alias="DATASOURCE_REDIS_USERNAME")
    redis_password: str = Field(
        default="", validation_alias="DATASOURCE_REDIS_PASSWORD"
    )
    redis_database: int = Field(default=0, validation_alias="DATASOURCE_REDIS_DATABASE")
    redis_ssl: bool = Field(default=False, validation_alias="DATASOURCE_REDIS_SSL_MODE")

    redis_cluster_mode: bool = Field(
        default=False, validation_alias=AliasChoices("DATASOURCE_REDIS_MODE")
    )
    redis_ttl: int = 7200
    redis_refresh_on_read: bool = True

    # --- 动态计算 Redis URL ---
    @computed_field
    @property
    def redis_url(self) -> str:
        """
        基于基础字段自动生成编码后的 Redis URL
        """
        protocol = "rediss" if self.redis_ssl else "redis"

        from jiuwen.common.security.cryptor import Crypt

        curr_work_api = Crypt()
        self.redis_password = curr_work_api.decrypt(self.redis_password)

        encoded_pwd = quote_plus(self.redis_password) if self.redis_password else ""

        # 兼容带用户名的格式: redis://user:password@host:port/db
        auth = ""
        if self.redis_user or encoded_pwd:
            auth = f"{self.redis_user}:{encoded_pwd}@"

        return f"{protocol}://{auth}{self.redis_host}:{self.redis_port}/{self.redis_database}"

    model_config = SettingsConfigDict(
        # 自动查找 .env 文件
        env_file=str(Path(__file__).parent.parent.parent / ".env"),
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    @field_validator("redis_cluster_mode", mode="before")
    @classmethod
    def transform_redis_mode(cls, v: Any) -> bool:
        # 只要环境变量包含 "cluster" (不区分大小写)，就返回 True
        if isinstance(v, str):
            return v.lower() == "cluster"
        return bool(v)


# 实例化
settings = Settings()
