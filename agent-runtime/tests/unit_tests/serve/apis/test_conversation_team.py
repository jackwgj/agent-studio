import json
from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest

from agent_runtime.serve.apis.conversation_team import ConversationTeamReq, team_sse_stream
from agent_runtime.supervisor.skill_model import SkillDescriptor


def manager_skill_catalog():
    return [{
        "skillId": "s1",
        "versionId": "v1",
        "name": "会议纪要",
        "description": "整理会议",
        "objectKey": "u/skills/s1/v1/a.zip",
    }]


def request_payload(**overrides):
    payload = {
        "conversationId": "c1",
        "query": "整理会议",
        "subAgentIds": ["a1"],
        "modelDeploymentId": "m1",
        "skillCatalog": manager_skill_catalog(),
        "recommendedSkillIds": ["s1"],
    }
    payload.update(overrides)
    return payload


def test_request_accepts_manager_skill_contract():
    req = ConversationTeamReq.model_validate(request_payload())

    assert req.skill_catalog[0].skill_id == "s1"
    assert req.skill_catalog[0].version_id == "v1"
    assert req.skill_catalog[0].object_key == "u/skills/s1/v1/a.zip"
    assert req.recommended_skill_ids == ["s1"]


@pytest.mark.asyncio
async def test_team_stream_converts_manager_catalog_to_runtime_descriptors(monkeypatch):
    build_supervisor = AsyncMock(return_value=SimpleNamespace())

    async def run_supervisor(*_args):
        yield {"event": "message", "data": {"delta": "已整理"}, "executionId": "e1"}

    monkeypatch.setattr("agent_runtime.serve.apis.conversation_team.build_supervisor", build_supervisor)
    monkeypatch.setattr("agent_runtime.serve.apis.conversation_team.run_supervisor", run_supervisor)

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()), "e1"
    )]

    catalog = build_supervisor.await_args.kwargs["skill_catalog"]
    assert catalog == [SkillDescriptor(
        skill_id="s1",
        version_id="v1",
        name="会议纪要",
        description="整理会议",
        object_key="u/skills/s1/v1/a.zip",
    )]
    assert build_supervisor.await_args.kwargs["recommended_skill_ids"] == ["s1"]
    assert events[-1]["event"] == "message"


@pytest.mark.asyncio
async def test_team_stream_rejects_recommended_skill_outside_manager_catalog(monkeypatch):
    build_supervisor = AsyncMock()
    monkeypatch.setattr("agent_runtime.serve.apis.conversation_team.build_supervisor", build_supervisor)

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=["other"])), "e1"
    )]

    assert events[-1]["event"] == "error"
    assert events[-1]["data"]["code"] == "build_failed"
    build_supervisor.assert_not_awaited()
