#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
import os
from enum import Enum
from typing import Optional

from jiuwen.common.log.base import logger
from jiuwen.common.utils.utils import safe_json_loads
from pydantic import BaseModel, Field, ValidationError


class CacheTypeEnum(str, Enum):
    MEMORY = "memory"
    REDIS = "redis"


class VectorTypeEnum(str, Enum):
    LAKE_SEARCH = "lakeSearch"
    MILVUS = "milvus"


class MilvusConfig(BaseModel):
    # 本地向量数据库路径
    local_db_path: str = Field(
        alias="localDbPath", default="./config/ei_vector.db", min_length=1
    )
    # 本地向量数据库名称
    collection_name: str = Field(
        alias="collectionName", default="LongTermMemoryVector", min_length=1
    )
    # 向量维数
    dim: int = Field(alias="dim", default=384, ge=1)
    # header中鉴权字段名称
    auth_header_name: Optional[str] = Field(alias="authHeaderName", default=None)
    # header中鉴权字段名称
    auth_header_value: Optional[str] = Field(alias="authHeaderValue", default=None)
    # embedding模型路径
    model_url: Optional[str] = Field(alias="modelUrl", default=None)
    # embedding模型输入文本填充字段,多层字段用"."连接。比如配置query, 生成的请求就是{"query":"xxxx"}
    input_field: Optional[str] = Field(alias="inputField", default=None)
    # embedding模型输出embedding返回字段,多层字段用"."连接。比如配置embedding[0], 就是取返回体中embedding字段的第一个值
    output_field: Optional[str] = Field(alias="outputField", default=None)
    # 单个用户单个工作流，保存最多几份总结，多余的通过总结再压缩保存
    max_summary_num: int = Field(alias="maxSummaryNum", default=20, ge=1)


class LongMemoryConfigInfo(BaseModel):
    enable: bool = Field(alias="enable", default=False)
    # 每过几轮对话总结一次
    summary_period: int = Field(alias="summaryPeriod", default=5, ge=1)
    # 对话缓存类型
    chat_cache_type: CacheTypeEnum = Field(
        alias="chatCacheType", default=CacheTypeEnum.MEMORY
    )
    # 工作流调用关系缓存类型
    call_workflow_cache_type: CacheTypeEnum = Field(
        alias="callWorkflowCacheType", default=CacheTypeEnum.MEMORY
    )
    # 每个用户每个工作流保存的历史对话的最大长度
    max_chat_length: int = Field(alias="maxChatLength", default=100, ge=1, le=200)
    # 对话摘要最大轮数
    max_summary_period: int = Field(alias="maxSummaryPeriod", default=10, ge=1)
    # 向量数据库类型
    vector_type: VectorTypeEnum = Field(
        alias="vectorType", default=VectorTypeEnum.MILVUS
    )
    # 本地向量数据库配置
    milvus_config: MilvusConfig = Field(
        alias="milvusConfig", default_factory=lambda: MilvusConfig()
    )


class LongMemoryConfig:
    __config: LongMemoryConfigInfo = None

    def __init__(self):
        if LongMemoryConfig.__config is not None:
            raise Exception("This class is a singleton!")
        if not os.environ.get("LONG_MEMORY_CONFIG"):
            LongMemoryConfig.__config = LongMemoryConfigInfo()
            return
        try:
            LongMemoryConfig.__config = LongMemoryConfigInfo.model_validate(
                safe_json_loads(os.environ.get("LONG_MEMORY_CONFIG"))
            )
        except ValidationError as e:
            logger.error(f"memory config error {e}")
            LongMemoryConfig.__config = LongMemoryConfigInfo()
            return

    @staticmethod
    def get_config() -> LongMemoryConfigInfo:
        if LongMemoryConfig.__config is None:
            LongMemoryConfig()
        return LongMemoryConfig.__config


long_memory_config = LongMemoryConfig.get_config()
