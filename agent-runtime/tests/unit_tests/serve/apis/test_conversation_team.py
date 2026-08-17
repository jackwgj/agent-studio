import json
import asyncio
from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest
from pydantic import ValidationError

from agent_runtime.serve.apis import conversation_team as conversation_team_module
from agent_runtime.serve.apis.conversation_team import ConversationTeamReq, team_sse_stream
from agent_runtime.supervisor import runner as runner_module
from agent_runtime.supervisor.event.channel import get_channel
from agent_runtime.supervisor.skill_context import attach_agent_context, get_skill_context
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


@pytest.mark.parametrize("field_name", ["skillId", "versionId", "name", "description", "objectKey"])
def test_request_rejects_blank_skill_descriptor_fields(field_name):
    catalog = manager_skill_catalog()
    catalog[0][field_name] = " \t "

    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(skillCatalog=catalog))


def test_request_rejects_duplicate_skill_ids_and_invalid_object_key():
    duplicate = manager_skill_catalog() + [{
        "skillId": "s1",
        "versionId": "v2",
        "name": "另一个版本",
        "description": "冲突",
        "objectKey": "u/skills/s1/v2/a.zip",
    }]

    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(skillCatalog=duplicate))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(skillCatalog=[{
            **manager_skill_catalog()[0], "objectKey": "../outside.zip"
        }]))


def test_request_normalizes_duplicate_recommendations_and_explicitly_supports_aliases_and_field_names():
    req = ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=["s1", "s1"]))
    snake_case = ConversationTeamReq.model_validate({
        "conversation_id": "c1",
        "query": "整理会议",
        "sub_agent_ids": ["a1"],
        "model_deployment_id": "m1",
        "skill_catalog": [{
            "skill_id": "s1",
            "version_id": "v1",
            "name": "会议纪要",
            "description": "整理会议",
            "object_key": "u/skills/s1/v1/a.zip",
        }],
        "recommended_skill_ids": ["s1"],
    })

    assert req.recommended_skill_ids == ["s1"]
    assert snake_case.skill_catalog[0].skill_id == "s1"
    assert snake_case.recommended_skill_ids == ["s1"]


def test_request_rejects_null_extra_and_unknown_recommendation_with_alias_precedence():
    for field_name in ("skillCatalog", "recommendedSkillIds"):
        with pytest.raises(ValidationError):
            ConversationTeamReq.model_validate(request_payload(**{field_name: None}))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(untrustedBrowserMetadata={"x": 1}))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=["other"]))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=[" "]))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(
            skill_catalog=[{
                "skill_id": "s2",
                "version_id": "v2",
                "name": "field-name",
                "description": "conflicts with the alias",
                "object_key": "u/skills/s2/v2/a.zip",
            }]
        ))


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
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=["other"]))


@pytest.mark.asyncio
async def test_outer_sse_close_waits_for_runner_cleanup_in_the_consuming_context(monkeypatch):
    class BlockingAgent:
        card = object()

        async def stream(self, _inputs, _session):
            yield object()
            stream_started.set()
            try:
                await release.wait()
            finally:
                child_finished.set()

    stream_started = asyncio.Event()
    child_finished = asyncio.Event()
    release = asyncio.Event()
    agent = BlockingAgent()
    attach_agent_context(agent, [SkillDescriptor(
        skill_id="s1", version_id="v1", name="会议纪要", description="整理会议",
        object_key="u/skills/s1/v1/a.zip",
    )], [], SimpleNamespace())
    monkeypatch.setattr(conversation_team_module, "build_supervisor", AsyncMock(return_value=agent))
    monkeypatch.setattr(runner_module, "create_agent_session", lambda **_kwargs: object())
    monkeypatch.setattr(
        runner_module,
        "adapt_stream_chunk",
        lambda _chunk, _ctx: [{"event": "message", "data": {"delta": "x"}}],
    )
    stream = team_sse_stream(ConversationTeamReq.model_validate(request_payload()), "e1")

    await stream.__anext__()
    await stream.__anext__()
    event = await stream.__anext__()
    await stream_started.wait()
    await stream.aclose()

    assert json.loads(event.removeprefix("data: "))["event"] == "message"
    assert child_finished.is_set()
    assert get_channel() is None
    assert get_skill_context() is None
