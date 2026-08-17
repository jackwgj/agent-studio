from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest

from agent_runtime.supervisor.event.channel import EventChannel, reset_channel, set_channel
from agent_runtime.supervisor.skill_artifact_cache import SkillArtifactError
from agent_runtime.supervisor.skill_context import (
    attach_agent_context,
    bind_agent_skill_context,
    reset_skill_context,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor
from agent_runtime.supervisor.tool.activate_skill_tool import ActivateSkillTool


def descriptor(skill_id, version_id, name, description):
    return SkillDescriptor(
        skill_id=skill_id,
        version_id=version_id,
        name=name,
        description=description,
        object_key=f"user/skills/{skill_id}/{version_id}/{name}.zip",
    )


@pytest.mark.asyncio
async def test_activate_returns_instructions_and_emits_event():
    cache = AsyncMock()
    cache.load_instructions.return_value = "完整技能指令"
    agent = SimpleNamespace()
    attach_agent_context(agent, [descriptor("s1", "v1", "会议纪要", "整理会议")], [], cache)
    skill_token = bind_agent_skill_context(agent)
    channel = EventChannel("exec-1")
    event_token = set_channel(channel)
    try:
        result = await ActivateSkillTool().invoke({"skill_id": "s1"})
        event = await channel.get()
    finally:
        reset_channel(event_token)
        reset_skill_context(skill_token)

    assert result == {
        "skillId": "s1",
        "name": "会议纪要",
        "versionId": "v1",
        "instructions": "完整技能指令",
    }
    assert event["event"] == "skill_activated"
    assert event["data"] == {"skillId": "s1", "name": "会议纪要", "versionId": "v1"}
    assert "objectKey" not in event["data"]


@pytest.mark.asyncio
async def test_activate_rejects_id_outside_current_catalog():
    agent = SimpleNamespace()
    attach_agent_context(agent, [], [], AsyncMock())
    token = bind_agent_skill_context(agent)
    try:
        result = await ActivateSkillTool().invoke({"skill_id": "other"})
    finally:
        reset_skill_context(token)

    assert result["error"]["code"] == "skill_not_available"


@pytest.mark.asyncio
@pytest.mark.parametrize(
    ("error", "code"),
    [
        (RuntimeError("storage unavailable"), "skill_download_failed"),
        (SkillArtifactError("invalid skill archive"), "skill_artifact_invalid"),
        (SkillArtifactError("exactly one root SKILL.md is required"), "skill_instructions_missing"),
    ],
)
async def test_activate_returns_stable_cache_failure_codes_without_storage_details(error, code):
    cache = AsyncMock()
    cache.load_instructions.side_effect = error
    agent = SimpleNamespace()
    attach_agent_context(agent, [descriptor("s1", "v1", "会议纪要", "整理会议")], [], cache)
    token = bind_agent_skill_context(agent)
    try:
        result = await ActivateSkillTool().invoke({"skill_id": "s1"})
    finally:
        reset_skill_context(token)

    assert result["error"]["code"] == code
    assert "user/skills/" not in result["error"]["message"]


def test_activate_tool_card_accepts_only_skill_id():
    card = ActivateSkillTool().card

    assert card.id == "conversation_activate_skill"
    assert card.name == "activate_skill"
    assert card.input_params == {
        "type": "object",
        "properties": {"skill_id": {"type": "string", "description": "目录中的 Skill ID"}},
        "required": ["skill_id"],
    }
