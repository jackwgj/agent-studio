import pytest

from jiuwen.deep_research.types import DRLocalSearchConfigValidator


def test_local_search_validator_accepts_knowledge_config_without_url():
    # 迁移后：search_url 可选，knowledgeConfig 承载检索配置
    cfg = DRLocalSearchConfigValidator(
        search_engine_name="openapi",
        search_api_key="",
        knowledgeConfig={
            "connectionId": "conn-1",
            "knowledgeBaseIds": ["kb-1"],
            "retrievalConfig": {"topK": 5},
        },
    )
    assert cfg.search_url == ""  # 默认空，不再必填
    assert cfg.knowledgeConfig["connectionId"] == "conn-1"
    assert cfg.knowledgeConfig["knowledgeBaseIds"] == ["kb-1"]


def test_local_search_validator_defaults():
    cfg = DRLocalSearchConfigValidator(
        search_engine_name="openapi", search_api_key=""
    )
    assert cfg.knowledgeConfig == {}
    assert cfg.search_datasets == []
    assert cfg.search_url == ""


def test_local_search_validator_still_requires_engine_name_and_key():
    with pytest.raises(Exception):
        DRLocalSearchConfigValidator(knowledgeConfig={})
