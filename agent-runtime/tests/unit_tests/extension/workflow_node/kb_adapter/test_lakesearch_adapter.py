import json

import pytest

from agent_runtime.extension.workflow_node.kb_adapter.lakesearch_adapter import (
    LakeSearchAdapter,
)

pytestmark = pytest.mark.asyncio


async def test_search_raises_when_project_id_or_app_id_missing():
    adapter = LakeSearchAdapter()
    # endpoint 有、但 extra_params 缺 project_id / app_id → 直接报错，不兜底
    with pytest.raises(ValueError, match="project_id and app_id"):
        await adapter.search(
            query="q",
            connection_config={
                "endpoint": "http://host",
                "auth_mode": "BASIC",
                "authorization": "abc",
                "extra_params": {},
            },
            knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
            retrieval_params={},
        )


async def test_search_empty_endpoint_raises_error():
    adapter = LakeSearchAdapter()
    with pytest.raises(RuntimeError, match="endpoint"):
        await adapter.search(
            query="q",
            connection_config={"endpoint": "", "extra_params": {}},
            knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
            retrieval_params={},
        )


async def test_search_no_valid_kb_returns_empty():
    """所有知识库都没有 external_id 时，应返回空列表"""
    adapter = LakeSearchAdapter()
    result = await adapter.search(
        query="q",
        connection_config={
            "endpoint": "http://host",
            "auth_mode": "BASIC",
            "authorization": "abc",
            "extra_params": {"project_id": "p1", "app_id": "a1"},
        },
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": ""}],
        retrieval_params={},
    )
    assert result == []


def test_parse_response_snake_case_fields():
    """对齐 Java LakeSearchChatReferenceInfo @JsonNaming(SnakeCaseStrategy) 的 snake_case 字段"""
    adapter = LakeSearchAdapter()
    resp_data = {
        "doc_list": [
            {
                "content": "hello world",
                "score": 0.95,
                "file_id": "file-001",
                "chunk_id": "chunk-001",
                "title": "My Document",
                "subtitle": "Chapter 1",
                "doc_type": "pdf",
                "repo_id": "repo-001",
                "file_path": "/data/docs",
            }
        ]
    }
    results = adapter.parse_response(resp_data, "repo-default")
    assert len(results) == 1
    r = results[0]
    assert r.text == "hello world"
    assert r.score == 0.95
    assert r.file_id == "file-001"
    assert r.document_name == "My Document"
    assert r.subtitle == "Chapter 1"
    assert r.type == "pdf"
    assert r.source == "repo-001"  # 从文档自身 repo_id 取，非默认值


def test_parse_response_camel_case_fallback():
    """兼容 camelCase 响应（部分旧版 API 可能返回）"""
    adapter = LakeSearchAdapter()
    resp_data = {
        "docList": [
            {
                "content": "camel case",
                "score": 0.88,
                "fileId": "file-002",
                "chunkId": "chunk-002",
                "title": "CamelDoc",
                "docType": "docx",
                "repoId": "repo-002",
            }
        ]
    }
    results = adapter.parse_response(resp_data, "repo-default")
    assert len(results) == 1
    r = results[0]
    assert r.file_id == "file-002"
    assert r.document_name == "CamelDoc"
    assert r.type == "docx"
    assert r.source == "repo-002"


def test_parse_response_multi_kb_repo_id_per_doc():
    """多知识库检索时，每个结果的 repo_id 来自文档自身，而非请求参数"""
    adapter = LakeSearchAdapter()
    resp_data = {
        "doc_list": [
            {"content": "doc from repo A", "score": 0.9, "repo_id": "repo-A", "title": "A", "file_id": "f1"},
            {"content": "doc from repo B", "score": 0.8, "repo_id": "repo-B", "title": "B", "file_id": "f2"},
            {"content": "doc no repo_id", "score": 0.7, "title": "C", "file_id": "f3"},
        ]
    }
    results = adapter.parse_response(resp_data, "repo-default")
    assert len(results) == 3
    assert results[0].source == "repo-A"
    assert results[1].source == "repo-B"
    assert results[2].source == "repo-default"  # 无 repo_id 回退到 source 参数


def test_parse_response_empty_content_skipped():
    """content 为空的文档应被跳过"""
    adapter = LakeSearchAdapter()
    resp_data = {
        "doc_list": [
            {"content": "", "score": 0.9, "title": "empty"},
            {"content": "valid", "score": 0.8, "title": "ok"},
        ]
    }
    results = adapter.parse_response(resp_data, "repo-1")
    assert len(results) == 1
    assert results[0].text == "valid"
