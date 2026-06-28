"""KB 适配器 search() 请求构建测试：用 fake aiohttp 捕获出站请求体/URL/headers。"""

import pytest

from agent_runtime.extension.workflow_node.kb_adapter import (
    lakesearch_adapter,
    koosearch_adapter,
    ragflow_adapter,
    general_kb_adapter,
)
from agent_runtime.extension.workflow_node.kb_adapter.lakesearch_adapter import (
    LakeSearchAdapter,
)
from agent_runtime.extension.workflow_node.kb_adapter.koosearch_adapter import (
    KooSearchAdapter,
)
from agent_runtime.extension.workflow_node.kb_adapter.ragflow_adapter import RagFlowAdapter
from agent_runtime.extension.workflow_node.kb_adapter.general_kb_adapter import (
    GeneralKBAdapter,
)

pytestmark = pytest.mark.asyncio


class _FakeResp:
    def __init__(self, payload):
        self._payload = payload
        self.ok = True
        self.status = 200

    async def __aenter__(self):
        return self

    async def __aexit__(self, *a):
        return False

    async def text(self):
        import json as _json

        return _json.dumps(self._payload)

    async def json(self):
        return self._payload


class _FakeSession:
    """记录最后一次 post 的 kwargs，返回预置响应。"""

    last = {}

    def __init__(self, payload):
        self._payload = payload

    async def __aenter__(self):
        return self

    async def __aexit__(self, *a):
        return False

    def post(self, url=None, json=None, headers=None, timeout=None):
        _FakeSession.last = {"url": url, "json": json, "headers": headers}
        return _FakeResp(self._payload)


def _patch_session(monkeypatch, module, payload):
    _FakeSession.last = {}
    monkeypatch.setattr(
        module.aiohttp, "ClientSession", lambda *a, **k: _FakeSession(payload)
    )


# --------------------------------------------------------------------------
# LakeSearch
# --------------------------------------------------------------------------


async def test_lakesearch_builds_request(monkeypatch):
    _patch_session(monkeypatch, lakesearch_adapter, {"doc_list": []})
    adapter = LakeSearchAdapter()
    await adapter.search(
        query="hello",
        connection_config={
            "endpoint": "http://host",
            "auth_mode": "BASIC",
            "authorization": "cred",
            "extra_params": {"project_id": "p1", "app_id": "a1"},
        },
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
        retrieval_params={"topK": 3, "searchMode": "faq", "tags": ["t1", "t2"]},
    )
    sent = _FakeSession.last
    assert sent["url"] == "http://host/v1/p1/applications/a1/uni-search/experience/searchtext"
    assert sent["json"]["repo_id"] == "ext-1"
    assert sent["json"]["content"] == "hello"
    assert sent["json"]["page_num"] == 1
    assert sent["json"]["page_size"] == 3  # min(topK, 50)
    assert sent["json"]["scope"] == "faq"
    assert sent["json"]["filter_string"] == "tags:(t1 OR t2)"
    assert sent["headers"]["Authorization"] == "Basic cred"


async def test_lakesearch_page_size_capped_at_50(monkeypatch):
    _patch_session(monkeypatch, lakesearch_adapter, {"doc_list": []})
    adapter = LakeSearchAdapter()
    await adapter.search(
        query="q",
        connection_config={
            "endpoint": "http://host",
            "auth_mode": "TOKEN",
            "authorization": "tok",
            "extra_params": {"project_id": "p1", "app_id": "a1"},
        },
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
        retrieval_params={"topK": 999},
    )
    assert _FakeSession.last["json"]["page_size"] == 50
    assert _FakeSession.last["headers"]["Authorization"] == "Bearer tok"


# --------------------------------------------------------------------------
# KooSearch
# --------------------------------------------------------------------------


async def test_koosearch_builds_request_with_extra_repo_ids(monkeypatch):
    _patch_session(monkeypatch, koosearch_adapter, {"doc_list": []})
    adapter = KooSearchAdapter()
    await adapter.search(
        query="q",
        connection_config={
            "endpoint": "http://koo",
            "authorization": "appcode-1",
            "extra_params": {"project_id": "p1", "application_id": "app1"},
        },
        knowledge_bases=[
            {"knowledge_base_id": "kb-1", "external_id": "ext-1"},
            {"knowledge_base_id": "kb-2", "external_id": "ext-2"},
        ],
        retrieval_params={"topK": 5, "searchMode": "mix"},
    )
    sent = _FakeSession.last
    assert sent["json"]["repo_id"] == "ext-1"
    assert sent["json"]["extra_repo_ids"] == ["ext-1", "ext-2"]
    assert sent["json"]["scope"] == "mix"
    assert sent["headers"]["X-Apig-AppCode"] == "appcode-1"


async def test_koosearch_skips_when_no_appcode(monkeypatch):
    _patch_session(monkeypatch, koosearch_adapter, {"doc_list": []})
    adapter = KooSearchAdapter()
    results = await adapter.search(
        query="q",
        connection_config={"endpoint": "http://koo", "extra_params": {}},
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
        retrieval_params={},
    )
    assert results == []


# --------------------------------------------------------------------------
# RAGFlow
# --------------------------------------------------------------------------


async def test_ragflow_builds_request(monkeypatch):
    _patch_session(monkeypatch, ragflow_adapter, {"code": 0, "data": {"chunks": []}})
    adapter = RagFlowAdapter()
    await adapter.search(
        query="q",
        connection_config={"endpoint": "http://rag", "authorization": "key-1"},
        knowledge_bases=[
            {"knowledge_base_id": "kb-1", "external_id": "ds-1"},
            {"knowledge_base_id": "kb-2", "external_id": "ds-2"},
        ],
        retrieval_params={"topK": 4, "scoreThreshold": 0.6},
    )
    sent = _FakeSession.last
    assert sent["url"] == "http://rag/api/v1/retrieval"
    assert sent["json"]["question"] == "q"
    assert sent["json"]["dataset_ids"] == ["ds-1", "ds-2"]
    assert sent["json"]["page_size"] == 4
    assert sent["json"]["similarity_threshold"] == 0.6
    assert sent["headers"]["Authorization"] == "Bearer key-1"


async def test_ragflow_apikey_fallback_from_extra_params(monkeypatch):
    _patch_session(monkeypatch, ragflow_adapter, {"code": 0, "data": {"chunks": []}})
    adapter = RagFlowAdapter()
    await adapter.search(
        query="q",
        connection_config={
            "endpoint": "http://rag",
            "authorization": "",
            "extra_params": {"APIKey": "fallback-key"},
        },
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ds-1"}],
        retrieval_params={},
    )
    assert _FakeSession.last["headers"]["Authorization"] == "Bearer fallback-key"


# --------------------------------------------------------------------------
# General
# --------------------------------------------------------------------------


async def test_general_builds_request(monkeypatch):
    _patch_session(monkeypatch, general_kb_adapter, {"search_result_list": []})
    adapter = GeneralKBAdapter()
    await adapter.search(
        query="q",
        connection_config={
            "endpoint": "http://gen",
            "authorization": "api-1",
            "extra_params": {},
        },
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
        retrieval_params={"topK": 7, "searchMode": "keyword", "scoreThreshold": 0.5},
    )
    sent = _FakeSession.last
    assert sent["url"] == "http://gen/knowledge-bases/retrieve"
    assert sent["json"]["knowledge_base_ids"] == ["ext-1"]
    assert sent["json"]["method"] == "keyword"
    assert sent["json"]["limit"] == 7
    assert sent["json"]["top_k"] == 7
    assert sent["json"]["search_threshold"] == 0.5
    assert sent["headers"]["Authorization"] == "Bearer api-1"
