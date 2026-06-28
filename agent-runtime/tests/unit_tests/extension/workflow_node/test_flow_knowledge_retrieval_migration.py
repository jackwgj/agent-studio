# pylint: disable=protected-access  # 单元测试需直接验证内部方法行为
from unittest.mock import AsyncMock

import pytest

from agent_runtime.extension.workflow_node.flow_knowledge_retrieval import (
    KNOWLEDGE_FILE_CACHE_PREFIX,
    KNOWLEDGE_IMAGE_CACHE_PREFIX,
    FlowKnowledgeRetrieval,
)
from agent_runtime.extension.tool.knowledge_retrieval_tool import (
    _build_retrieval_config,
)
from agent_runtime.extension.workflow_node.kb_adapter.base import KBSearchResult


pytestmark = pytest.mark.asyncio


async def test_normalize_results_maps_external_id_and_caches_file(monkeypatch):
    component = FlowKnowledgeRetrieval(
        {
            "connectionId": "conn-1",
            "knowledgeBaseIds": ["kb-1"],
            "retrievalConfig": {},
        }
    )
    cache_mock = AsyncMock()
    monkeypatch.setattr(component, "_cache_redis", cache_mock)

    results = [
        KBSearchResult(
            text="hello",
            score=0.9,
            source="external-1",
            knowledge_base_id="external-1",
            file_id="real-file-1",
            type="doc",
        )
    ]
    knowledge_bases = [
        {
            "knowledge_base_id": "kb-1",
            "external_id": "external-1",
            "type": "INTERNAL",
        }
    ]

    normalized = await component.normalize_results_like_java(
        results,
        knowledge_bases,
        {"retrieveImage": False},
    )

    assert normalized[0].knowledge_base_id == "kb-1"
    assert normalized[0].knowledge_base_type == "INTERNAL"
    assert normalized[0].file_id != "real-file-1"
    cache_key, cache_value, _ttl = cache_mock.await_args.args
    assert cache_key.startswith(KNOWLEDGE_FILE_CACHE_PREFIX)
    assert cache_value == "kb-1,doc,real-file-1"


async def test_normalize_results_replaces_image_placeholders(monkeypatch):
    component = FlowKnowledgeRetrieval(
        {
            "connectionId": "conn-1",
            "knowledgeBaseIds": ["kb-1"],
            "retrievalConfig": {},
        }
    )
    cache_mock = AsyncMock()
    monkeypatch.setattr(component, "_cache_redis", cache_mock)

    results = [
        KBSearchResult(
            text="before {img-abc123} after",
            score=0.9,
            source="external-1",
            knowledge_base_id="external-1",
            type="doc",
        )
    ]
    knowledge_bases = [
        {
            "knowledge_base_id": "kb-1",
            "external_id": "external-1",
            "type": "INTERNAL",
        }
    ]

    normalized = await component.normalize_results_like_java(
        results,
        knowledge_bases,
        {"retrieveImage": True},
    )

    assert "{img-abc123}" not in normalized[0].text
    assert "https://agent_arts_knowledge_img_url/" in normalized[0].text
    cache_key, cache_value, _ttl = cache_mock.await_args.args
    assert cache_key.startswith(KNOWLEDGE_IMAGE_CACHE_PREFIX)
    assert cache_value == "kb-1,img-abc123"


async def test_normalize_results_removes_image_placeholders_when_disabled(monkeypatch):
    component = FlowKnowledgeRetrieval(
        {
            "connectionId": "conn-1",
            "knowledgeBaseIds": ["kb-1"],
            "retrievalConfig": {},
        }
    )
    monkeypatch.setattr(component, "_cache_redis", AsyncMock())

    results = [
        KBSearchResult(
            text="before {img-abc123} after",
            score=0.9,
            source="external-1",
            knowledge_base_id="external-1",
            type="doc",
        )
    ]

    normalized = await component.normalize_results_like_java(
        results,
        [{"knowledge_base_id": "kb-1", "external_id": "external-1"}],
        {"retrieveImage": False},
    )

    assert normalized[0].text == "before  after"


def test_agent_retrieval_tool_prefers_new_knowledge_config():
    ir_data = {
        "name": "retrieval",
        "arguments": [
            {
                "name": "knowledge_bases",
                "default_value": [
                    {
                        "id": "kb-old",
                        "connection_id": "conn-old",
                    }
                ],
            },
            {"name": "top_k", "default_value": 3},
        ],
        "knowledgeConfig": {
            "connectionId": "conn-new",
            "knowledgeBaseIds": ["kb-new"],
            "retrievalConfig": {
                "topK": 8,
                "searchMode": "hybrid",
                "showSource": True,
            },
        },
    }

    retrieval_config = _build_retrieval_config(ir_data, ir_data["knowledgeConfig"])

    assert ir_data["knowledgeConfig"]["connectionId"] == "conn-new"
    assert ir_data["knowledgeConfig"]["knowledgeBaseIds"] == ["kb-new"]
    assert retrieval_config["topK"] == 3
    assert retrieval_config["searchMode"] == "hybrid"
    assert retrieval_config["showSource"] is True


class _FakeAdapter:
    """记录 search() 收到的参数，并返回预置结果。"""

    def __init__(self, results=None):
        self._results = results or []
        self.calls = []

    async def search(self, query, *, connection_config, knowledge_bases, retrieval_params):
        self.calls.append(
            {
                "query": query,
                "connection_config": connection_config,
                "knowledge_bases": knowledge_bases,
                "retrieval_params": dict(retrieval_params),
            }
        )
        return list(self._results)


def _make_component():
    return FlowKnowledgeRetrieval(
        {"connectionId": "conn-1", "knowledgeBaseIds": ["kb-1"], "retrievalConfig": {}}
    )


# --------------------------------------------------------------------------
# is_custom_source
# --------------------------------------------------------------------------


def test_is_custom_source_detects_custom():
    assert FlowKnowledgeRetrieval.is_custom_source({"knowledge_source": "CUSTOM"}) is True
    assert FlowKnowledgeRetrieval.is_custom_source({"knowledge_source": "custom"}) is True
    assert FlowKnowledgeRetrieval.is_custom_source({"knowledge_source": "LakeSearch"}) is False
    assert FlowKnowledgeRetrieval.is_custom_source({}) is False


# --------------------------------------------------------------------------
# _retrieve_custom: 剔除阈值、repo_id 用 kb_id、合并 tags、不归一化
# --------------------------------------------------------------------------


async def test_retrieve_custom_strips_thresholds_and_uses_kb_id_as_repo_id():
    component = _make_component()
    adapter = _FakeAdapter(results=[KBSearchResult(text="x", score=0.1)])

    results = await component._retrieve_custom(
        adapter=adapter,
        query="q",
        connection_config={"knowledge_source": "CUSTOM"},
        knowledge_bases=[
            {"knowledge_base_id": "kb-1", "external_id": "ext-1", "tags": ["t1"]},
            {"knowledge_base_id": "kb-2", "external_id": "ext-2", "tags": ["t2"]},
        ],
        retrieval_params={"scoreThreshold": 0.5, "recallThreshold": 0.5, "topK": 3},
    )

    call = adapter.calls[0]
    # 阈值被剔除，避免适配器内分数过滤多删
    assert "scoreThreshold" not in call["retrieval_params"]
    assert "recallThreshold" not in call["retrieval_params"]
    # repo_id（external_id）被知识库 id 覆盖
    assert [kb["external_id"] for kb in call["knowledge_bases"]] == ["kb-1", "kb-2"]
    # tags 扁平合并
    assert call["retrieval_params"]["tags"] == ["t1", "t2"]
    # 低分结果未被编排层过滤
    assert len(results) == 1


async def test_search_knowledge_repo_routes_custom_source(monkeypatch):
    component = _make_component()
    custom_mock = AsyncMock(return_value=[])
    monkeypatch.setattr(component, "_retrieve_custom", custom_mock)
    faq_mock = AsyncMock(return_value=[])
    monkeypatch.setattr(component, "_search_with_faq_fallback", faq_mock)

    await component.search_knowledge_repo(
        adapter=_FakeAdapter(),
        query="q",
        connection_config={"knowledge_source": "CUSTOM", "connector_type": "LakeSearch"},
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
        retrieval_params={},
    )

    custom_mock.assert_awaited_once()
    faq_mock.assert_not_awaited()


# --------------------------------------------------------------------------
# search_knowledge_repo: CLOSE 过滤 / 阈值过滤 / topK 截断
# --------------------------------------------------------------------------


async def test_search_knowledge_repo_filters_closed_kbs(monkeypatch):
    component = _make_component()
    faq_mock = AsyncMock(return_value=[])
    monkeypatch.setattr(component, "_search_with_faq_fallback", faq_mock)

    await component.search_knowledge_repo(
        adapter=_FakeAdapter(),
        query="q",
        connection_config={"connector_type": "LakeSearch"},
        knowledge_bases=[
            {"knowledge_base_id": "kb-1", "external_id": "ext-1", "status": "CLOSE"},
            {"knowledge_base_id": "kb-2", "external_id": "ext-2", "status": "ENABLE"},
        ],
        retrieval_params={},
    )

    passed_kbs = faq_mock.await_args.kwargs["knowledge_bases"]
    assert [kb["knowledge_base_id"] for kb in passed_kbs] == ["kb-2"]


async def test_search_knowledge_repo_returns_empty_when_all_closed(monkeypatch):
    component = _make_component()
    faq_mock = AsyncMock(return_value=[])
    monkeypatch.setattr(component, "_search_with_faq_fallback", faq_mock)

    results = await component.search_knowledge_repo(
        adapter=_FakeAdapter(),
        query="q",
        connection_config={"connector_type": "LakeSearch"},
        knowledge_bases=[
            {"knowledge_base_id": "kb-1", "external_id": "ext-1", "status": "CLOSE"},
        ],
        retrieval_params={},
    )

    assert results == []
    faq_mock.assert_not_awaited()


async def test_search_knowledge_repo_applies_recall_threshold_and_topk(monkeypatch):
    component = _make_component()
    raw = [
        KBSearchResult(text="a", score=0.9),
        KBSearchResult(text="b", score=0.7),
        KBSearchResult(text="c", score=0.3),  # 低于阈值，应被过滤
    ]
    monkeypatch.setattr(component, "_search_with_faq_fallback", AsyncMock(return_value=raw))

    results = await component.search_knowledge_repo(
        adapter=_FakeAdapter(),
        query="q",
        connection_config={"connector_type": "LakeSearch"},
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
        retrieval_params={"recallThreshold": 0.5, "topK": 1},
    )

    # 0.3 被阈值过滤，再按 topK=1 截断，保留最高分
    assert len(results) == 1
    assert results[0].score == 0.9


async def test_search_knowledge_repo_default_topk_is_10(monkeypatch):
    component = _make_component()
    raw = [KBSearchResult(text=str(i), score=1.0) for i in range(15)]
    monkeypatch.setattr(component, "_search_with_faq_fallback", AsyncMock(return_value=raw))

    results = await component.search_knowledge_repo(
        adapter=_FakeAdapter(),
        query="q",
        connection_config={"connector_type": "LakeSearch"},
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
        retrieval_params={},  # 未提供 topK，默认应为 10
    )

    assert len(results) == 10
