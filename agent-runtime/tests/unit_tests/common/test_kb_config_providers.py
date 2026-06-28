# pylint: disable=protected-access  # 单元测试需直接验证内部方法行为
from agent_runtime.common import kb_config_providers
from agent_runtime.common.kb_config_providers import OBSKnowledgeBaseConfigProvider


def test_parse_connection_reads_knowledge_source():
    provider = OBSKnowledgeBaseConfigProvider()
    conn = provider._parse_connection(
        {
            "connectionId": "conn-1",
            "connectorId": "LakeSearchInside",
            "knowledgeSource": "CUSTOM",
            "params": [{"code": "endpoint", "value": "http://host"}],
        }
    )
    assert conn.knowledge_source == "CUSTOM"
    assert conn.connector_type == "LakeSearchInside"
    assert conn.endpoint == "http://host"


def test_parse_connection_defaults_knowledge_source_empty():
    provider = OBSKnowledgeBaseConfigProvider()
    conn = provider._parse_connection({"connectionId": "conn-1"})
    assert conn.knowledge_source == ""


def test_parse_connection_decrypts_secret_params(monkeypatch):
    monkeypatch.setattr(
        kb_config_providers, "decrypt_kb_secret", lambda v: f"decrypted::{v}"
    )
    provider = OBSKnowledgeBaseConfigProvider()
    conn = provider._parse_connection(
        {
            "connectionId": "conn-1",
            "connectorId": "Ragflow",
            "params": [
                {"code": "APIKey", "value": "cipher-text"},
                {"code": "endpoint", "value": "http://host"},
            ],
        }
    )
    # SECRET 字段被解密，普通字段保持原样
    assert conn.extra_params["APIKey"] == "decrypted::cipher-text"
    assert conn.extra_params["endpoint"] == "http://host"


def test_parse_connection_builds_basic_auth_from_user_password(monkeypatch):
    monkeypatch.setattr(kb_config_providers, "decrypt_kb_secret", lambda v: v)
    provider = OBSKnowledgeBaseConfigProvider()
    conn = provider._parse_connection(
        {
            "connectionId": "conn-1",
            "connectorId": "LakeSearchInside",
            "params": [
                {"code": "user_name", "value": "alice"},
                {"code": "password", "value": "secret"},
            ],
        }
    )
    # authorization 为空但有 user_name+password 时自动生成 Basic 凭证（base64(user:pass)）
    import base64

    assert conn.auth_mode == "BASIC"
    assert conn.authorization == base64.b64encode(b"alice:secret").decode()
