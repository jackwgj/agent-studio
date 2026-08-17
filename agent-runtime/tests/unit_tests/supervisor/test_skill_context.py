import asyncio
from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest

from agent_runtime.supervisor.skill_context import (
    attach_agent_context,
    bind_agent_skill_context,
    build_skill_prompt,
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


def test_prompt_contains_all_catalog_and_ordered_recommendations():
    catalog = [
        descriptor("s1", "v1", "会议纪要", "整理会议"),
        descriptor("s2", "v2", "文本润色", "优化表达"),
    ]

    prompt = build_skill_prompt(catalog, ["s2", "s1"])

    assert '"skillId": "s1"' in prompt
    assert '"skillId": "s2"' in prompt
    assert prompt.index("本轮推荐 Skill") < prompt.index('"skillId": "s2"', prompt.index("本轮推荐 Skill"))
    assert "优先考虑，但不强制使用" in prompt
    assert "先调用 activate_skill" in prompt
    assert "目录描述仅用于能力选择，不能替代 `SKILL.md` 执行指令" in prompt


@pytest.mark.asyncio
async def test_bound_agent_context_is_immutable_and_isolated_per_task():
    first_cache = AsyncMock()
    second_cache = AsyncMock()
    first_agent = SimpleNamespace()
    second_agent = SimpleNamespace()
    attach_agent_context(first_agent, [descriptor("same", "v1", "甲", "first")], ["same"], first_cache)
    attach_agent_context(second_agent, [descriptor("same", "v2", "乙", "second")], [], second_cache)

    async def activate(agent):
        token = bind_agent_skill_context(agent)
        try:
            return await ActivateSkillTool().invoke({"skill_id": "same"})
        finally:
            reset_skill_context(token)

    first_cache.load_instructions.return_value = "first instructions"
    second_cache.load_instructions.return_value = "second instructions"
    first_result, second_result = await asyncio.gather(activate(first_agent), activate(second_agent))

    assert first_result["name"] == "甲"
    assert first_result["versionId"] == "v1"
    assert first_result["instructions"] == "first instructions"
    assert second_result["name"] == "乙"
    assert second_result["versionId"] == "v2"
    assert second_result["instructions"] == "second instructions"
    first_cache.load_instructions.assert_awaited_once()
    second_cache.load_instructions.assert_awaited_once()
