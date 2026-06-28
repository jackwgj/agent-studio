import pytest

from agent_runtime.extension.tool import knowledge_retrieval_tool as krt
from agent_runtime.extension.tool.knowledge_retrieval_tool import (
    _to_agent_output,
    _format_output,
    _run_retrieval,
)
from agent_runtime.extension.workflow_node.kb_adapter.base import KBSearchResult
from jiuwen.plugin.common import constant


def test_to_agent_output_maps_all_fields():
    r = KBSearchResult(
        text="body",
        score=0.7,
        source="src",
        knowledge_base_id="kb-1",
        knowledge_base_type="INTERNAL",
        file_id="f-1",
        document_name="doc.pdf",
        subtitle="sub",
        serial_number=2,
        retrieval_id="rid",
        type="faq",
        metadata={"k": "v"},
    )
    out = _to_agent_output(r)
    assert out["content"] == "body"
    assert out["text"] == "body"
    assert out["score"] == 0.7
    assert out["knowledge_base_id"] == "kb-1"
    assert out["knowledge_base_type"] == "INTERNAL"
    assert out["file_id"] == "f-1"
    assert out["document_name"] == "doc.pdf"
    assert out["serial_number"] == 2
    assert out["retrieval_id"] == "rid"
    assert out["type"] == "faq"
    assert out["metadata"] == {"k": "v"}


def test_format_output_structure():
    out = _format_output([{"x": 1}])
    assert out[constant.ERR_CODE] == 0
    assert out[constant.ERR_MESSAGE] == "success"
    assert out[constant.RESTFUL_DATA] == {"output_list": [{"x": 1}]}


@pytest.mark.asyncio
async def test_run_retrieval_empty_query_short_circuits():
    # 空 query 直接返回空 output_list，不触发任何检索
    out = await _run_retrieval(
        query="   ",
        connection_id="conn-1",
        knowledge_base_ids=["kb-1"],
        retrieval_config={},
        tag_overrides={},
    )
    assert out[constant.RESTFUL_DATA] == {"output_list": []}


@pytest.mark.asyncio
async def test_run_retrieval_custom_source_skips_normalization(monkeypatch):
    """CUSTOM 知识源：_run_retrieval 不调用 normalize_results_like_java。"""
    from agent_runtime.extension.workflow_node import flow_knowledge_retrieval as fkr

    sample = [KBSearchResult(text="t", score=0.9, knowledge_base_id="kb-1")]

    async def fake_ensure(self):
        return {
            "connection": {"connector_type": "LakeSearch", "knowledge_source": "CUSTOM"},
            "knowledge_bases": [{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
            "retrieval_params": {},
        }

    async def fake_search(self, **kwargs):
        return sample

    normalize_called = {"v": False}

    async def fake_normalize(self, results, kbs, params):
        normalize_called["v"] = True
        return results

    monkeypatch.setattr(fkr.FlowKnowledgeRetrieval, "ensure_kb_config", fake_ensure)
    monkeypatch.setattr(fkr.FlowKnowledgeRetrieval, "search_knowledge_repo", fake_search)
    monkeypatch.setattr(
        fkr.FlowKnowledgeRetrieval, "normalize_results_like_java", fake_normalize
    )
    # 避免真实构造适配器
    monkeypatch.setattr(krt.KBAdapterFactory, "create", lambda *a, **k: object())

    out = await _run_retrieval(
        query="hello",
        connection_id="conn-1",
        knowledge_base_ids=["kb-1"],
        retrieval_config={},
        tag_overrides={},
    )

    assert normalize_called["v"] is False  # CUSTOM 跳过归一化
    assert len(out[constant.RESTFUL_DATA]["output_list"]) == 1
