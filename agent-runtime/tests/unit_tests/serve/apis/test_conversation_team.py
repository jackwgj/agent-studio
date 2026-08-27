import json
import asyncio
from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest
from pydantic import ValidationError

from agent_runtime.serve.apis.conversation_team import ConversationTeamReq, team_sse_stream
from agent_runtime.supervisor.event.channel import (
    EventChannel,
    get_channel,
    reset_channel,
    set_channel,
)
from agent_runtime.supervisor.skill_context import (
    attach_agent_context,
    bind_agent_skill_context,
    get_skill_context,
    reset_skill_context,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor
from agent_runtime.supervisor.tool.activate_skill_tool import ActivateSkillTool


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


def test_request_accepts_file_references_with_names_and_urls():
    req = ConversationTeamReq.model_validate(request_payload(fileIds=[{
        "url": "https://files.test/report.pdf",
        "fileName": "report.pdf",
    }]))

    assert req.file_ids == [{"url": "https://files.test/report.pdf", "fileName": "report.pdf"}]


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


def test_request_rejects_null_and_unknown_recommendation_and_ignores_legacy_top_level_extra():
    for field_name in ("skillCatalog", "recommendedSkillIds"):
        with pytest.raises(ValidationError):
            ConversationTeamReq.model_validate(request_payload(**{field_name: None}))
    legacy = ConversationTeamReq.model_validate(request_payload(
        systemPrompt="旧客户端字段", untrustedBrowserMetadata={"x": 1}
    ))
    assert legacy.query == "整理会议"
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


def test_request_rejects_conflicting_alias_and_field_name_but_keeps_descriptor_extra_forbidden():
    with pytest.raises(ValidationError, match="conflicting request field aliases"):
        ConversationTeamReq.model_validate(request_payload(
            skill_catalog=[{
                "skill_id": "s2",
                "version_id": "v2",
                "name": "field-name",
                "description": "conflicts with the alias",
                "object_key": "u/skills/s2/v2/a.zip",
            }]
        ))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(skillCatalog=[{
            **manager_skill_catalog()[0], "unexpected": "still forbidden inside descriptors"
        }]))


@pytest.mark.asyncio
async def test_team_stream_rejects_recommended_skill_outside_manager_catalog(monkeypatch):
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=["other"]))


@pytest.mark.asyncio
async def test_concurrent_skill_context_and_event_channel_are_isolated():
    def agent_with_catalog(skill_id):
        agent = SimpleNamespace()
        cache = AsyncMock()
        cache.load_instructions.return_value = f"instructions-{skill_id}"
        attach_agent_context(
            agent,
            [SkillDescriptor(
                skill_id=skill_id,
                version_id="v1",
                name=f"name-{skill_id}",
                description=f"description-{skill_id}",
                object_key=f"user/skills/{skill_id}/v1/{skill_id}.zip",
            )],
            [],
            cache,
        )
        return agent

    async def invoke_in_context(agent, skill_id, execution_id):
        skill_token = bind_agent_skill_context(agent)
        channel = EventChannel(execution_id, f"conversation-{skill_id}")
        event_token = set_channel(channel)
        try:
            result = await ActivateSkillTool().invoke({"skill_id": skill_id})
            event = await channel.get()
            return result, event
        finally:
            reset_channel(event_token)
            reset_skill_context(skill_token)

    (result_a, event_a), (result_b, event_b) = await asyncio.gather(
        invoke_in_context(agent_with_catalog("s1"), "s1", "exec-1"),
        invoke_in_context(agent_with_catalog("s2"), "s2", "exec-2"),
    )

    assert result_a["skillId"] == event_a["data"]["skillId"] == "s1"
    assert result_b["skillId"] == event_b["data"]["skillId"] == "s2"
    assert event_a["runId"] == "exec-1"
    assert event_b["runId"] == "exec-2"
    assert get_channel() is None
    assert get_skill_context() is None


# TODO: SSE 生命周期测试（SSE close、client disconnect）需要改写为新路径版本。
# 原有测试依赖 legacy build_supervisor 和 adapt_stream_chunk，已在 F7 清理中移除。
