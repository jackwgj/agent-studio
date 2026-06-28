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


async def test_search_empty_endpoint_returns_empty():
    adapter = LakeSearchAdapter()
    results = await adapter.search(
        query="q",
        connection_config={"endpoint": "", "extra_params": {}},
        knowledge_bases=[{"knowledge_base_id": "kb-1", "external_id": "ext-1"}],
        retrieval_params={},
    )
    assert results == []
