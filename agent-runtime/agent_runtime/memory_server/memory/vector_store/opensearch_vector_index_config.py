"""
OpenSearch 向量索引配置常量类
"""

from dataclasses import dataclass
from typing import Dict, Any, List

from memory_service.config.settings import (
    EMBEDDING_DIMENSION,
    INDEX_SHARD_COUNT,
    INDEX_REPLICA_COUNT,
)


@dataclass
class VectorFieldConfig:
    """向量字段配置"""

    name: str
    field_type: str
    description: str
    is_vector: bool = False
    dimension: int = 1024
    similarity: str = "cosinesimil"
    index_options: Dict[str, Any] = None


class OpenSearchVectorIndexConfig:
    """向量索引配置管理类"""

    # 从 settings 读取的配置
    VECTOR_DIMENSION = EMBEDDING_DIMENSION
    SHARD_COUNT = INDEX_SHARD_COUNT
    REPLICA_COUNT = INDEX_REPLICA_COUNT

    # 预定义字段配置
    PREDEFINED_FIELDS = [
        VectorFieldConfig(
            name="user_id",
            field_type="keyword",
            description="用户唯一标识符",
            is_vector=False,
        ),
        # 应用相关字段
        VectorFieldConfig(
            name="app_id", field_type="keyword", description="应用ID", is_vector=False
        ),
        VectorFieldConfig(
            name="memory_type",
            field_type="keyword",
            description="记忆类型",
            is_vector=False,
        ),
        # 向量字段
        VectorFieldConfig(
            name="content_vector",
            field_type="knn_vector",
            description="存储记忆文本向量化之后的向量",
            is_vector=True,
            dimension=VECTOR_DIMENSION,
            similarity="cosinesimil",
        ),
        # 内容字段
        VectorFieldConfig(
            name="content",
            field_type="text",
            description="记忆内容的原始文本，无需参与搜索和聚合",
            is_vector=False,
            index_options={"index": False, "doc_values": False},
        ),
        VectorFieldConfig(
            name="last_updated",
            field_type="long",
            description="最后更新时间，毫秒级时间戳",
            is_vector=False,
        ),
    ]

    # 索引配置
    INDEX_SETTINGS = {
        "index.knn": True,
        "number_of_shards": SHARD_COUNT,
        "number_of_replicas": REPLICA_COUNT,
    }

    @classmethod
    def build_full_mapping(cls) -> Dict[str, Any]:
        """构建完整的索引映射"""
        properties = {}
        fields_description = {}

        for field in cls.PREDEFINED_FIELDS:
            properties[field.name] = cls._build_field_mapping(field)
            fields_description[field.name] = field.description

        return {
            "mappings": {
                "_meta": {"fields_description": fields_description},
                "properties": properties,
            },
            "settings": cls.INDEX_SETTINGS,
        }

    @classmethod
    def get_field_mapping(cls, field_name: str) -> Dict[str, Any]:
        """获取单个字段的映射配置"""
        for field in cls.PREDEFINED_FIELDS:
            if field.name == field_name:
                return cls._build_field_mapping(field)

        raise ValueError(f"Field '{field_name}' not found in predefined fields")

    @classmethod
    def _build_field_mapping(cls, field: VectorFieldConfig) -> Dict[str, Any]:
        """构建单个字段的映射配置"""
        mapping = {"type": field.field_type}

        if field.is_vector:
            # 向量字段特殊处理 - knn_vector类型
            mapping.update(
                {
                    "dimension": field.dimension,
                    "method": {
                        "name": "hnsw",
                        "engine": "faiss",
                        "space_type": (
                            field.similarity if field.similarity else "cosinesimil"
                        ),
                    },
                }
            )
        elif field.index_options:
            # 其他字段的索引选项
            mapping.update(field.index_options)

        return mapping

    @classmethod
    def get_metadata_fields(cls) -> List[str]:
        """获取元数据字段名称列表"""
        return [
            field.name
            for field in cls.PREDEFINED_FIELDS
            if not field.is_vector and field.name not in ["content"]
        ]

    @classmethod
    def get_text_field_name(cls) -> str:
        """获取文本字段名称"""
        return "content"

    @classmethod
    def get_vector_field_name(cls) -> str:
        """获取向量字段名称"""
        return "content_vector"

    @classmethod
    def update_configuration(
        cls, dimension: int = None, shards: int = None, replicas: int = None
    ):
        """运行时更新配置（可选功能）"""
        if dimension is not None:
            cls.VECTOR_DIMENSION = dimension
            # 更新向量字段配置
            for field in cls.PREDEFINED_FIELDS:
                if field.is_vector:
                    field.dimension = dimension

        if shards is not None:
            cls.SHARD_COUNT = shards
            cls.INDEX_SETTINGS["number_of_shards"] = shards

        if replicas is not None:
            cls.REPLICA_COUNT = replicas
            cls.INDEX_SETTINGS["number_of_replicas"] = replicas


# 向后兼容的全局配置对象
vector_index_config = OpenSearchVectorIndexConfig()
