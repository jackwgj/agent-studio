# pylint: disable=protected-access  # 单元测试需直接验证内部方法行为
from agent_runtime.common import kb_config_providers
from agent_runtime.common.kb_config_providers import (
    KBConnectionConfig,
    OBSKnowledgeBaseConfigProvider,
)
from agent_runtime.context.request_context import RequestContext, _request_ctx


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
    """SECRET 类型参数在 _parse_connection 中通过 CryptTool().decrypt() 解密。"""
    def _fake_decrypt(v):
        return f"decrypted::{v}"

    class _FakeCryptTool:
        decrypt = staticmethod(_fake_decrypt)

    monkeypatch.setattr(kb_config_providers, "CryptTool", _FakeCryptTool)
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
    """authorization 为空但有 user_name+password 时自动生成 Basic 凭证。"""
    class _FakeCryptTool:
        @staticmethod
        def decrypt(v):
            return v

    monkeypatch.setattr(kb_config_providers, "CryptTool", _FakeCryptTool)
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


# ---------------------------------------------------------------------------
# _merge_auth_headers 测试
# ---------------------------------------------------------------------------


def test_merge_auth_headers_lakesearch_fills_user_token():
    """LakeSearch 连接：authorization 为空时，用用户 token 兜底。"""
    provider = OBSKnowledgeBaseConfigProvider()
    conn = KBConnectionConfig(
        connection_id="conn-1",
        connector_type="LakeSearch",
        authorization="",
    )
    token = _request_ctx.set(
        RequestContext(headers={"X-Auth-Token": "user-login-token"})
    )
    try:
        provider._merge_auth_headers(conn)
        assert conn.authorization == "user-login-token"
        assert conn.auth_mode == "TOKEN"
    finally:
        _request_ctx.reset(token)


def test_merge_auth_headers_ragflow_skips_user_token():
    """RAGFlow 连接：不应该用用户 token 填充 authorization。"""
    provider = OBSKnowledgeBaseConfigProvider()
    conn = KBConnectionConfig(
        connection_id="conn-1",
        connector_type="RagFlow",
        authorization="",
    )
    token = _request_ctx.set(
        RequestContext(headers={"X-Auth-Token": "user-login-token"})
    )
    try:
        provider._merge_auth_headers(conn)
        assert conn.authorization == ""  # 不应被填充
    finally:
        _request_ctx.reset(token)


def test_merge_auth_headers_koosearch_skips_user_token():
    """KooSearch 连接：不应该用用户 token 填充 authorization。"""
    provider = OBSKnowledgeBaseConfigProvider()
    conn = KBConnectionConfig(
        connection_id="conn-1",
        connector_type="KooSearch",
        authorization="",
    )
    token = _request_ctx.set(
        RequestContext(headers={"X-Auth-Token": "user-login-token"})
    )
    try:
        provider._merge_auth_headers(conn)
        assert conn.authorization == ""  # 不应被填充
    finally:
        _request_ctx.reset(token)


def test_merge_auth_headers_no_override_existing():
    """已有 authorization 时不覆盖。"""
    provider = OBSKnowledgeBaseConfigProvider()
    conn = KBConnectionConfig(
        connection_id="conn-1",
        connector_type="LakeSearch",
        authorization="existing-cred",
    )
    token = _request_ctx.set(
        RequestContext(headers={"X-Auth-Token": "user-login-token"})
    )
    try:
        provider._merge_auth_headers(conn)
        assert conn.authorization == "existing-cred"  # 保持原值
    finally:
        _request_ctx.reset(token)


def test_merge_auth_headers_empty_connector_type_fills_user_token():
    """connector_type 为空（未知/兼容旧数据）时仍用用户 token 兜底。"""
    provider = OBSKnowledgeBaseConfigProvider()
    conn = KBConnectionConfig(
        connection_id="conn-1",
        connector_type="",
        authorization="",
    )
    token = _request_ctx.set(
        RequestContext(headers={"X-Auth-Token": "user-login-token"})
    )
    try:
        provider._merge_auth_headers(conn)
        assert conn.authorization == "user-login-token"
    finally:
        _request_ctx.reset(token)


def test_merge_auth_headers_lakesearch_inside_fills_user_token():
    """LakeSearchInside 连接：底层是 LakeSearchAdapter，应用户 token 兜底。"""
    provider = OBSKnowledgeBaseConfigProvider()
    conn = KBConnectionConfig(
        connection_id="conn-1",
        connector_type="LakeSearchInside",
        authorization="",
    )
    token = _request_ctx.set(
        RequestContext(headers={"X-Auth-Token": "user-login-token"})
    )
    try:
        provider._merge_auth_headers(conn)
        assert conn.authorization == "user-login-token"
    finally:
        _request_ctx.reset(token)


def test_merge_auth_headers_custom_fills_user_token():
    """Custom 连接：底层是 LakeSearchAdapter，应用户 token 兜底。"""
    provider = OBSKnowledgeBaseConfigProvider()
    conn = KBConnectionConfig(
        connection_id="conn-1",
        connector_type="Custom",
        authorization="",
    )
    token = _request_ctx.set(
        RequestContext(headers={"X-Auth-Token": "user-login-token"})
    )
    try:
        provider._merge_auth_headers(conn)
        assert conn.authorization == "user-login-token"
    finally:
        _request_ctx.reset(token)
