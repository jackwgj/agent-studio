import unittest

from memory_service.config.settings import (
    EMBEDDING_DIMENSION,
    INDEX_SHARD_COUNT,
    INDEX_REPLICA_COUNT,
)
from memory_service.memory.vector_store.opensearch_vector_index_config import (
    VectorFieldConfig,
    OpenSearchVectorIndexConfig,
)


class TestVectorFieldConfig(unittest.TestCase):
    """测试VectorFieldConfig类"""

    def test_vector_field_config_init_with_default_values(self):
        """测试VectorFieldConfig初始化默认值"""
        config = VectorFieldConfig(
            name="test_field", field_type="keyword", description="测试字段"
        )

        self.assertEqual(config.name, "test_field")
        self.assertEqual(config.field_type, "keyword")
        self.assertEqual(config.description, "测试字段")
        self.assertFalse(config.is_vector)
        self.assertEqual(config.dimension, 1024)
        self.assertEqual(config.similarity, "cosinesimil")
        self.assertEqual(config.index_options, None)

    def test_vector_field_config_init_with_all_values(self):
        """测试VectorFieldConfig初始化所有值"""
        index_options = {"index": False, "doc_values": False}
        config = VectorFieldConfig(
            name="content_vector",
            field_type="knn_vector",
            description="内容向量",
            is_vector=True,
            dimension=768,
            similarity="euclidean",
            index_options=index_options,
        )

        self.assertEqual(config.name, "content_vector")
        self.assertEqual(config.field_type, "knn_vector")
        self.assertEqual(config.description, "内容向量")
        self.assertTrue(config.is_vector)
        self.assertEqual(config.dimension, 768)
        self.assertEqual(config.similarity, "euclidean")
        self.assertEqual(config.index_options, index_options)


class TestOpenSearchVectorIndexConfig(unittest.TestCase):
    """测试OpenSearchVectorIndexConfig类"""

    def setUp(self):
        """每个测试前的设置"""
        # 保存原始配置以便后续恢复
        self.original_dimension = OpenSearchVectorIndexConfig.VECTOR_DIMENSION
        self.original_shards = OpenSearchVectorIndexConfig.SHARD_COUNT
        self.original_replicas = OpenSearchVectorIndexConfig.REPLICA_COUNT
        self.original_index_settings = OpenSearchVectorIndexConfig.INDEX_SETTINGS.copy()

        # 为向量字段保存原始状态
        self.original_vector_fields = []
        for field in OpenSearchVectorIndexConfig.PREDEFINED_FIELDS:
            if field.is_vector:
                self.original_vector_fields.append(
                    {
                        "field": field,
                        "dimension": field.dimension,
                        "similarity": field.similarity,
                    }
                )

    def tearDown(self):
        """每个测试后的清理"""
        # 恢复原始配置
        OpenSearchVectorIndexConfig.VECTOR_DIMENSION = self.original_dimension
        OpenSearchVectorIndexConfig.SHARD_COUNT = self.original_shards
        OpenSearchVectorIndexConfig.REPLICA_COUNT = self.original_replicas

        # 恢复INDEX_SETTINGS
        OpenSearchVectorIndexConfig.INDEX_SETTINGS = self.original_index_settings.copy()

        # 恢复向量字段状态
        for field_data in self.original_vector_fields:
            field = field_data["field"]
            field.dimension = field_data["dimension"]
            field.similarity = field_data["similarity"]

    def test_class_attributes_initialization(self):
        """测试类属性初始化"""
        self.assertEqual(
            OpenSearchVectorIndexConfig.VECTOR_DIMENSION, EMBEDDING_DIMENSION
        )
        self.assertEqual(OpenSearchVectorIndexConfig.SHARD_COUNT, INDEX_SHARD_COUNT)
        self.assertEqual(OpenSearchVectorIndexConfig.REPLICA_COUNT, INDEX_REPLICA_COUNT)

    def test_get_field_mapping_existing_field(self):
        """测试获取已存在字段的映射配置"""
        mapping = OpenSearchVectorIndexConfig.get_field_mapping("app_id")

        self.assertIsInstance(mapping, dict)
        self.assertEqual(mapping["type"], "keyword")

    def test_get_field_mapping_vector_field(self):
        """测试获取向量字段的映射配置"""
        mapping = OpenSearchVectorIndexConfig.get_field_mapping("content_vector")

        self.assertIsInstance(mapping, dict)
        self.assertEqual(mapping["type"], "knn_vector")
        self.assertIn("dimension", mapping)
        self.assertIn("method", mapping)
        self.assertEqual(mapping["method"]["name"], "hnsw")

    def test_get_field_mapping_nonexistent_field(self):
        """测试获取不存在字段时抛出异常"""
        with self.assertRaises(ValueError) as context:
            OpenSearchVectorIndexConfig.get_field_mapping("nonexistent_field")

        self.assertIn("nonexistent_field", str(context.exception))

    def test_build_full_mapping_structure(self):
        """测试构建完整映射的结构"""
        full_mapping = OpenSearchVectorIndexConfig.build_full_mapping()

        self.assertIsInstance(full_mapping, dict)
        self.assertIn("mappings", full_mapping)
        self.assertIn("settings", full_mapping)

        mappings = full_mapping["mappings"]
        self.assertIn("_meta", mappings)
        self.assertIn("properties", mappings)
        self.assertIn("fields_description", mappings["_meta"])

        settings = full_mapping["settings"]
        self.assertIn("index.knn", settings)
        self.assertIn("number_of_shards", settings)
        self.assertIn("number_of_replicas", settings)

    def test_build_field_mapping_non_vector_field(self):
        """测试构建非向量字段的映射"""
        field_config = VectorFieldConfig(
            name="content",
            field_type="text",
            description="内容",
            is_vector=False,
            index_options={"index": False, "doc_values": False},
        )

        mapping = OpenSearchVectorIndexConfig._build_field_mapping(field_config)

        self.assertEqual(mapping["type"], "text")
        self.assertEqual(mapping["index"], False)
        self.assertEqual(mapping["doc_values"], False)

    def test_build_field_mapping_vector_field(self):
        """测试构建向量字段的映射"""
        field_config = VectorFieldConfig(
            name="content_vector",
            field_type="knn_vector",
            description="内容向量",
            is_vector=True,
            dimension=768,
            similarity="euclidean",
        )

        mapping = OpenSearchVectorIndexConfig._build_field_mapping(field_config)

        self.assertEqual(mapping["type"], "knn_vector")
        self.assertEqual(mapping["dimension"], 768)
        self.assertEqual(mapping["method"]["space_type"], "euclidean")

    def test_get_vector_field_name(self):
        """测试获取向量字段名称"""
        field_name = OpenSearchVectorIndexConfig.get_vector_field_name()
        self.assertEqual(field_name, "content_vector")

    def test_get_text_field_name(self):
        """测试获取文本字段名称"""
        field_name = OpenSearchVectorIndexConfig.get_text_field_name()
        self.assertEqual(field_name, "content")

    def test_get_metadata_fields(self):
        """测试获取元数据字段列表"""
        metadata_fields = OpenSearchVectorIndexConfig.get_metadata_fields()

        self.assertIsInstance(metadata_fields, list)
        self.assertIn("app_id", metadata_fields)
        self.assertIn("user_id", metadata_fields)
        self.assertIn("memory_type", metadata_fields)
        self.assertIn("last_updated", metadata_fields)
        self.assertNotIn("content_vector", metadata_fields)
        self.assertNotIn("content", metadata_fields)

    def test_update_configuration_dimension(self):
        """测试更新配置-维度"""
        new_dimension = 1536

        OpenSearchVectorIndexConfig.update_configuration(dimension=new_dimension)

        self.assertEqual(OpenSearchVectorIndexConfig.VECTOR_DIMENSION, new_dimension)

        # 检查向量字段是否已更新
        vector_fields = [
            field
            for field in OpenSearchVectorIndexConfig.PREDEFINED_FIELDS
            if field.is_vector
        ]
        for field in vector_fields:
            self.assertEqual(field.dimension, new_dimension)

    def test_update_configuration_shards(self):
        """测试更新配置-分片数"""
        new_shards = 5

        OpenSearchVectorIndexConfig.update_configuration(shards=new_shards)

        self.assertEqual(OpenSearchVectorIndexConfig.SHARD_COUNT, new_shards)
        self.assertEqual(
            OpenSearchVectorIndexConfig.INDEX_SETTINGS["number_of_shards"], new_shards
        )

    def test_update_configuration_replicas(self):
        """测试更新配置-副本数"""
        new_replicas = 3

        OpenSearchVectorIndexConfig.update_configuration(replicas=new_replicas)

        self.assertEqual(OpenSearchVectorIndexConfig.REPLICA_COUNT, new_replicas)
        self.assertEqual(
            OpenSearchVectorIndexConfig.INDEX_SETTINGS["number_of_replicas"],
            new_replicas,
        )

    def test_update_configuration_all_parameters(self):
        """测试更新配置-所有参数"""
        new_dimension = 2048
        new_shards = 6
        new_replicas = 2

        OpenSearchVectorIndexConfig.update_configuration(
            dimension=new_dimension, shards=new_shards, replicas=new_replicas
        )

        # 验证维度更新
        self.assertEqual(OpenSearchVectorIndexConfig.VECTOR_DIMENSION, new_dimension)

        # 验证分片和副本更新
        self.assertEqual(OpenSearchVectorIndexConfig.SHARD_COUNT, new_shards)
        self.assertEqual(OpenSearchVectorIndexConfig.REPLICA_COUNT, new_replicas)
        self.assertEqual(
            OpenSearchVectorIndexConfig.INDEX_SETTINGS["number_of_shards"], new_shards
        )
        self.assertEqual(
            OpenSearchVectorIndexConfig.INDEX_SETTINGS["number_of_replicas"],
            new_replicas,
        )

        # 检查向量字段是否已更新
        vector_fields = [
            field
            for field in OpenSearchVectorIndexConfig.PREDEFINED_FIELDS
            if field.is_vector
        ]
        for field in vector_fields:
            self.assertEqual(field.dimension, new_dimension)

    def test_predefined_fields_structure(self):
        """测试预定义字段的结构"""
        predefined_fields = OpenSearchVectorIndexConfig.PREDEFINED_FIELDS

        self.assertIsInstance(predefined_fields, list)
        self.assertTrue(len(predefined_fields) > 0)

        # 检查app_id字段
        app_id_field = next(
            (field for field in predefined_fields if field.name == "app_id"), None
        )
        self.assertIsNotNone(app_id_field)
        self.assertEqual(app_id_field.field_type, "keyword")
        self.assertFalse(app_id_field.is_vector)

        # 检查content_vector字段
        vector_field = next(
            (field for field in predefined_fields if field.name == "content_vector"),
            None,
        )
        self.assertIsNotNone(vector_field)
        self.assertEqual(vector_field.field_type, "knn_vector")
        self.assertTrue(vector_field.is_vector)
        self.assertEqual(
            vector_field.dimension, OpenSearchVectorIndexConfig.VECTOR_DIMENSION
        )

    def test_index_settings_initialization(self):
        """测试索引设置初始化"""
        settings = OpenSearchVectorIndexConfig.INDEX_SETTINGS

        self.assertIsInstance(settings, dict)
        self.assertTrue(settings["index.knn"])
        self.assertEqual(
            settings["number_of_shards"], OpenSearchVectorIndexConfig.SHARD_COUNT
        )
        self.assertEqual(
            settings["number_of_replicas"], OpenSearchVectorIndexConfig.REPLICA_COUNT
        )


if __name__ == "__main__":
    unittest.main()
