from types import SimpleNamespace

import pytest

from agent_runtime.conversation.runner.conversation_react_runner import (
    ConversationReActRunner,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionParams, ExecutionRequest


@pytest.mark.asyncio
async def test_each_request_keeps_its_model_deployment_ir(monkeypatch):
    seen = []
    runner = ConversationReActRunner()

    async def create_llm(_self, ir_json):
        seen.append(ir_json["configs"]["modelConfig"]["modelName"])
        return object()

    monkeypatch.setattr(ConversationReActRunner, "_create_llm", create_llm)

    request_a = ExecutionRequest(
        conversationId="conversation-a",
        irPath="unused-a",
        query="a",
        params=ExecutionParams(
            ir_cache={
                "agentId": "a",
                "agentName": "A",
                "configs": {
                    "mode": "ReAct",
                    "modelConfig": {"modelName": "deployment-a"},
                    "sysPromptTemplate": "A",
                },
            }
        ),
    )
    request_b = ExecutionRequest(
        conversationId="conversation-b",
        irPath="unused-b",
        query="b",
        params=ExecutionParams(
            ir_cache={
                "agentId": "b",
                "agentName": "B",
                "configs": {
                    "mode": "ReAct",
                    "modelConfig": {"modelName": "deployment-b"},
                    "sysPromptTemplate": "B",
                },
            }
        ),
    )

    await runner._create_llm(runner._get_request_ir(request_a))
    await runner._create_llm(runner._get_request_ir(request_b))

    assert seen == ["deployment-a", "deployment-b"]
    assert set(vars(runner)) <= {"_api_key", "_api_base"}
