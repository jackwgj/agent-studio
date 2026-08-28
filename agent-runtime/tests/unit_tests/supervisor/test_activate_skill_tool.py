from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest

from agent_runtime.supervisor.event.channel import EventChannel, reset_channel, set_channel
from agent_runtime.supervisor.skill_artifact_cache import SkillArtifactCache
from agent_runtime.supervisor.skill_context import (
    SkillExecutionContext,
    attach_agent_context,
    bind_agent_skill_context,
    reset_skill_context,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor
from agent_runtime.supervisor.tool.activate_skill_tool import ActivateSkillTool


class _Agent:
    pass


@pytest.mark.asyncio
async def test_activate_skill_loads_instructions_and_emits_event(tmp_path, monkeypatch):
    skill = SkillDescriptor(
        skill_id="meeting-minutes",
        version_id="v1",
        name="Meeting Minutes",
        description="Structure meeting notes",
        object_key="user/skills/meeting-minutes/v1/skill.zip",
    )
    async def download(_key):
        return _zip_bytes()

    cache = SkillArtifactCache(tmp_path, downloader=download)
    monkeypatch.setattr(
        "agent_runtime.supervisor.tool.activate_skill_tool.conversation_skill_sandbox_enabled",
        lambda: False,
    )
    agent = _Agent()
    attach_agent_context(agent, [skill], [], cache)
    skill_token = bind_agent_skill_context(agent)
    channel = EventChannel("execution-1", "conversation-1")
    channel_token = set_channel(channel)
    try:
        result = await ActivateSkillTool().invoke({"skill_id": "meeting-minutes"})
        assert result["instructions"] == "# Meeting Minutes\nUse the table format."
        assert result["resourceState"] == "instructions_only"
        assert "sandboxPath" not in result
        event = await channel.get()
        assert event["event"] == "skill_activated"
        assert event["data"]["skillId"] == "meeting-minutes"
    finally:
        reset_channel(channel_token)
        reset_skill_context(skill_token)


def _zip_bytes():
    import io
    import zipfile

    output = io.BytesIO()
    with zipfile.ZipFile(output, "w") as archive:
        archive.writestr(
            "meeting-minutes/SKILL.md",
            "# Meeting Minutes\nUse the table format.",
        )
    return output.getvalue()


@pytest.mark.asyncio
async def test_activate_skill_rejects_model_selected_sandbox_target():
    result = await ActivateSkillTool().invoke({
        "skill_id": "meeting-minutes",
        "target_path": "/workspace/forged",
    })

    assert result["error"]["code"] == "invalid_skill_activation_input"


@pytest.mark.asyncio
async def test_activate_skill_prepares_complete_artifact_when_remote_sandbox_is_enabled(monkeypatch):
    skill = SkillDescriptor(
        skill_id="meeting-minutes",
        version_id="v1",
        name="Meeting Minutes",
        description="Structure meeting notes",
        object_key="user/skills/meeting-minutes/v1/skill.zip",
    )
    artifact = SimpleNamespace(instructions="# complete", artifact_dir="validated")
    cache = SimpleNamespace(
        load_artifact=AsyncMock(return_value=artifact),
        load_instructions=AsyncMock(),
    )
    prepare = AsyncMock(return_value="/workspace/conversation/skills/version")
    monkeypatch.setattr(
        "agent_runtime.supervisor.tool.activate_skill_tool.conversation_skill_sandbox_enabled",
        lambda: True,
    )
    monkeypatch.setattr(
        "agent_runtime.supervisor.tool.activate_skill_tool.prepare_conversation_skill",
        prepare,
    )
    agent = _Agent()
    attach_agent_context(agent, [skill], [], cache)
    token = bind_agent_skill_context(agent)
    try:
        result = await ActivateSkillTool().invoke({"skill_id": skill.skill_id})
    finally:
        reset_skill_context(token)

    cache.load_artifact.assert_awaited_once_with(skill)
    cache.load_instructions.assert_not_awaited()
    prepare.assert_awaited_once_with(skill, artifact)
    assert result["instructions"] == "# complete"
    assert result["resourceState"] == "prepared"
    assert result["sandboxPath"] == "/workspace/conversation/skills/version"
