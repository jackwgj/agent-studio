from agent_runtime.extension.tool.knowledge_retrieval_tool import (
    is_knowledge_retrieval_ir,
    _resolve_retrieval_params,
    _resolve_connection_id,
    _merge_tags,
)


# --------------------------------------------------------------------------
# is_knowledge_retrieval_ir
# --------------------------------------------------------------------------


def test_is_knowledge_retrieval_ir_true():
    ir = {"name": "retrieval", "arguments": [{"name": "knowledge_bases"}, {"name": "query"}]}
    assert is_knowledge_retrieval_ir(ir) is True


def test_is_knowledge_retrieval_ir_wrong_name():
    ir = {"name": "other", "arguments": [{"name": "knowledge_bases"}]}
    assert is_knowledge_retrieval_ir(ir) is False


def test_is_knowledge_retrieval_ir_missing_knowledge_bases_arg():
    ir = {"name": "retrieval", "arguments": [{"name": "query"}]}
    assert is_knowledge_retrieval_ir(ir) is False


def test_is_knowledge_retrieval_ir_not_dict():
    assert is_knowledge_retrieval_ir(None) is False
    assert is_knowledge_retrieval_ir("retrieval") is False


# --------------------------------------------------------------------------
# _resolve_connection_id
# --------------------------------------------------------------------------


def test_resolve_connection_id_from_first_kb():
    kbs = [{"id": "kb-1"}, {"id": "kb-2", "connection_id": "conn-2"}]
    assert _resolve_connection_id(kbs) == "conn-2"


def test_resolve_connection_id_none_returns_empty():
    assert _resolve_connection_id([{"id": "kb-1"}]) == ""


# --------------------------------------------------------------------------
# _merge_tags
# --------------------------------------------------------------------------


def test_merge_tags_applies_override_by_kb_id():
    loaded = [{"knowledge_base_id": "kb-1"}, {"knowledge_base_id": "kb-2"}]
    merged = _merge_tags(loaded, {"kb-1": ["t1", "t2"]})
    assert merged[0]["tags"] == ["t1", "t2"]
    assert "tags" not in merged[1]  # kb-2 无 override


def test_merge_tags_non_list_override_becomes_empty():
    loaded = [{"knowledge_base_id": "kb-1"}]
    merged = _merge_tags(loaded, {"kb-1": "not-a-list"})
    assert merged[0]["tags"] == []


# --------------------------------------------------------------------------
# _resolve_retrieval_params: 新形态 knowledgeConfig 优先于旧 knowledge_bases
# --------------------------------------------------------------------------


def test_resolve_retrieval_params_prefers_knowledge_config():
    ir = {
        "name": "retrieval",
        "knowledgeConfig": {
            "connectionId": "conn-new",
            "knowledgeBaseIds": ["kb-new"],
            "retrievalConfig": {},
        },
        "arguments": [
            {
                "name": "knowledge_bases",
                "default_value": [{"id": "kb-old", "connection_id": "conn-old", "tags": ["t"]}],
            }
        ],
    }
    resolved = _resolve_retrieval_params(ir)
    assert resolved["connection_id"] == "conn-new"
    assert resolved["knowledge_base_ids"] == ["kb-new"]
    # tag_overrides 仍来自旧 knowledge_bases（按 kb id 索引）
    assert resolved["tag_overrides"] == {"kb-old": ["t"]}


def test_resolve_retrieval_params_falls_back_to_knowledge_bases():
    ir = {
        "name": "retrieval",
        "arguments": [
            {
                "name": "knowledge_bases",
                "default_value": [{"id": "kb-1", "connection_id": "conn-1"}],
            }
        ],
    }
    resolved = _resolve_retrieval_params(ir)
    # 无 knowledgeConfig 时回退到 knowledge_bases
    assert resolved["connection_id"] == "conn-1"
    assert resolved["knowledge_base_ids"] == ["kb-1"]
