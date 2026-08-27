# -*- coding: UTF-8 -*-
"""监督者共享工具函数 —— Skill 输入验证、子 Agent 描述加载、文件引用格式化。

这些函数由 conversation_supervisor_builder.py 和 conversation_react_runner.py 调用，
服务于当前默认团队主路径。
"""

from pathlib import PureWindowsPath
import re

from agent_runtime.supervisor.skill_model import SkillDescriptor
from jiuwen.serve.controllers.execution.open_utils import async_ir_load

# 监督者系统提示词固定引擎侧（F4/用户决策 2026-08-11）：请求不再含 systemPrompt，Java 不传。
# 核心约束（方案 B）：监督者是任务分派者，必须把完成任务所需信息写足在 handoff query 里；
# 子 Agent 纯无状态单任务执行、不感知对话历史。
SUPERVISOR_SYSTEM_PROMPT = (
    "你是团队监督者，负责把用户请求分派给最合适的子 Agent 处理。"
    "移交任务时，你必须把完成任务所需的全部上下文信息完整写入 query 参数，"
    "确保子 Agent 无需任何外部信息即可直接执行。"
    "若无法匹配任何子 Agent，直接告知用户并提供建议。"
)


def _ir_path(agent_id: str) -> str:
    return f"agent/ir/{agent_id}/{agent_id}.json"


def normalize_skill_inputs(
    skill_catalog: list[SkillDescriptor] | None,
    recommended_skill_ids: list[str] | None,
) -> tuple[list[SkillDescriptor], list[str]]:
    """Validate the Manager catalog and normalize recommendations for all call paths.

    This repeats task 3's object-key acceptance semantics at the request boundary without
    importing its private cache validator: object keys are relative POSIX paths and must
    contain the descriptor's ``skills/{skill_id}/{version_id}`` identity path.
    """
    catalog = list(skill_catalog or [])
    seen_ids: set[str] = set()
    for skill in catalog:
        if not isinstance(skill, SkillDescriptor):
            raise ValueError("skill catalog entries must be SkillDescriptor instances")
        values = (skill.skill_id, skill.version_id, skill.name, skill.description, skill.object_key)
        if any(not isinstance(value, str) or not value.strip() for value in values):
            raise ValueError("skill catalog descriptor fields must not be blank")
        if skill.skill_id in seen_ids:
            raise ValueError(f"duplicate skill ID in catalog: {skill.skill_id}")
        seen_ids.add(skill.skill_id)
        _validate_skill_object_key(skill)

    recommended: list[str] = []
    for skill_id in recommended_skill_ids or []:
        if not isinstance(skill_id, str) or not skill_id.strip():
            raise ValueError("recommended skill IDs must not be blank")
        if skill_id not in seen_ids:
            raise ValueError(f"recommended skill IDs are not present in the catalog: {skill_id}")
        if skill_id not in recommended:
            recommended.append(skill_id)
    return catalog, recommended


def _validate_skill_object_key(skill: SkillDescriptor) -> None:
    """Mirror task 3's object-key boundary checks before a Skill reaches a prompt."""
    key = skill.object_key
    if "\\" in key or key.startswith("/") or PureWindowsPath(key).is_absolute() or re.match(r"^[A-Za-z]:", key):
        raise ValueError("unsafe skill object key")
    parts = key.split("/")
    if any(not part or part in {".", ".."} for part in parts):
        raise ValueError("unsafe skill object key")
    if any(any(ord(character) < 32 or ord(character) == 127 for character in part) for part in parts):
        raise ValueError("unsafe skill object key")
    identity = (skill.skill_id, skill.version_id)
    if any("/" in value or "\\" in value for value in identity):
        raise ValueError("unsafe skill object key")
    expected = ("skills", *identity)
    if not any(tuple(parts[index : index + 3]) == expected for index in range(len(parts) - 2)):
        raise ValueError("unsafe skill object key")


async def _load_sub_agent_description(agent_id: str) -> str:
    """从子 Agent 已有 IR 读取描述，用于 handoff 工具的卡片描述。"""
    try:
        ir_data = await async_ir_load(_ir_path(agent_id))
        return (
            ir_data.get("description")
            or ir_data.get("agentName")
            or f"将任务移交给子 Agent {agent_id} 处理"
        )
    except Exception:
        return f"将任务移交给子 Agent {agent_id} 处理"


def format_file_references(file_references: list[dict] | None) -> str:
    """Format this turn's uploaded files for the supervisor without inlining content."""
    if not file_references:
        return ""
    lines = [
        "\n\n## 本轮上传文件",
        "以下文件由用户在本轮上传。文件名用于识别文件主题；只有在任务需要时才调用 read_file_from_url，且必须使用清单中的完整 URL。",
    ]
    for item in file_references:
        file_name = str(item.get("fileName") or item.get("file_name") or "未命名文件")
        url = str(item.get("url") or "")
        if url:
            lines.append(f"- **{file_name}**: {url}")
    return "\n".join(lines) if len(lines) > 2 else ""
