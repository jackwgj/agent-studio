from types import SimpleNamespace

import pytest

from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
    reset_conversation_execution_context,
    set_conversation_execution_context,
)
from agent_runtime.conversation.skill_artifact_bridge import ConversationSkillArtifactBridge
from agent_runtime.supervisor.skill_artifact_cache import CachedSkillArtifact
from agent_runtime.supervisor.skill_model import SkillDescriptor


class RecordingFs:
    def __init__(self):
        self.calls = []

    async def write_file(self, path, content, **kwargs):
        self.calls.append((path, content, kwargs))
        return SimpleNamespace(is_ok=lambda: True)


class RecordingOperation:
    def __init__(self, fs):
        self._fs = fs

    def fs(self):
        return self._fs


@pytest.mark.asyncio
async def test_skill_bridge_writes_complete_tree_to_server_derived_skills_path(tmp_path):
    artifact_dir = tmp_path / "validated"
    (artifact_dir / "scripts").mkdir(parents=True)
    (artifact_dir / "templates").mkdir()
    (artifact_dir / "SKILL.md").write_text("# complete", encoding="utf-8")
    (artifact_dir / "scripts" / "run.py").write_bytes(b"print('ok')")
    (artifact_dir / "templates" / "report.md").write_bytes(b"# report")
    skill = SkillDescriptor("s1", "v1", "complete", "description", "u/skills/s1/v1/a.zip")
    artifact = CachedSkillArtifact("# complete", artifact_dir)
    fs = RecordingFs()
    context = ConversationExecutionContext.create(
        ConversationIdentity("project", "workspace", "user", "conversation", "execution"),
        "/workspace",
    )
    token = set_conversation_execution_context(context)
    try:
        sandbox_path = await ConversationSkillArtifactBridge(
            lambda: RecordingOperation(fs)
        ).prepare(skill, artifact)
    finally:
        reset_conversation_execution_context(token)

    expected_root = str(context.workspace.skills_dir / skill.cache_key)
    assert sandbox_path == expected_root
    assert {call[0] for call in fs.calls} == {
        f"{expected_root}/SKILL.md",
        f"{expected_root}/scripts/run.py",
        f"{expected_root}/templates/report.md",
    }
    assert all(call[2] == {
        "mode": "bytes", "prepend_newline": False, "append_newline": False,
    } for call in fs.calls)
