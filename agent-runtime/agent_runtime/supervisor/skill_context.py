"""Request-scoped workspace Skill context and supervisor prompt attachment."""

from collections.abc import Mapping, Sequence
from contextvars import ContextVar, Token
from dataclasses import dataclass
import json
from types import MappingProxyType

from openjiuwen.core.runner import Runner

from agent_runtime.supervisor.skill_artifact_cache import SkillArtifactCache, default_cache
from agent_runtime.supervisor.skill_model import SkillDescriptor


_AGENT_CONTEXT_ATTRIBUTE = "_conversation_workspace_skill_context"


@dataclass(frozen=True, slots=True)
class SkillExecutionContext:
    """Immutable catalog and cache bound to one supervisor agent invocation."""

    catalog_by_id: Mapping[str, SkillDescriptor]
    agent_bound_skills: tuple[SkillDescriptor, ...]
    workspace_supplement_skills: tuple[SkillDescriptor, ...]
    recommended_skill_ids: tuple[str, ...]
    artifact_cache: SkillArtifactCache
    prepare_sandbox_resources: bool = True


_current_skill_context: ContextVar[SkillExecutionContext | None] = ContextVar(
    "conversation_workspace_skill_context", default=None
)


def build_skill_prompt(
    catalog: Sequence[SkillDescriptor], recommended_skill_ids: Sequence[str]
) -> str:
    """Build the selection-only catalog prompt without exposing storage locations."""
    catalog_payload = [
        {
            "skillId": skill.skill_id,
            "versionId": skill.version_id,
            "name": skill.name,
            "description": skill.description,
        }
        for skill in catalog
    ]
    catalog_ids = {skill.skill_id for skill in catalog}
    recommended_payload = [
        skill_id for skill_id in recommended_skill_ids if skill_id in catalog_ids
    ]
    prompt = (
        "当前工作空间可用的 Skill 目录如下。目录描述仅用于能力选择，不能替代 `SKILL.md` 执行指令。\n"
        "你可自主选择并依次激活一个或多个 Skill；如需使用任一 Skill，先调用 activate_skill 加载该 Skill 的完整 SKILL.md 指令；"
        "不要根据目录描述直接执行。\n"
    )
    if recommended_payload:
        prompt += (
            "本轮推荐 Skill（优先考虑，但不强制使用）：\n"
            f"{json.dumps(recommended_payload, ensure_ascii=False)}\n"
        )
    return prompt + "可用 Skill 目录：\n" + json.dumps(catalog_payload, ensure_ascii=False)


def _partition_skills(
    catalog: Sequence[SkillDescriptor], agent_bound_skill_ids: Sequence[str]
) -> tuple[tuple[SkillDescriptor, ...], tuple[SkillDescriptor, ...]]:
    bound_ids = set(agent_bound_skill_ids)
    bound = tuple(skill for skill in catalog if skill.skill_id in bound_ids)
    supplement = tuple(skill for skill in catalog if skill.skill_id not in bound_ids)
    return bound, supplement


def _selection_payload(skills: Sequence[SkillDescriptor]) -> list[dict[str, str]]:
    return [
        {
            "skillId": skill.skill_id,
            "name": skill.name,
            "description": skill.description,
        }
        for skill in skills
    ]


def build_skill_catalog_prompt(
    catalog: Sequence[SkillDescriptor], agent_bound_skill_ids: Sequence[str]
) -> str:
    """Build stable source-grouped selection context without storage metadata."""
    bound, supplement = _partition_skills(catalog, agent_bound_skill_ids)
    return (
        "## Skill 使用规则\n"
        "以下目录只用于选择能力，不能替代 `SKILL.md`。需要使用 Skill 时，必须先调用 "
        "`activate_skill` 并使用其返回的真实路径；不得猜测 Skill 文件位置。\n"
        "选择优先级：本轮明确推荐且适用的 Skill > 当前智能体已绑定 Skill > 工作空间补充 Skill。"
        "推荐 Skill 明显不适用时可以跳过；可按任务需要依次激活多个 Skill。\n"
        "### 当前智能体已绑定 Skill\n"
        f"{json.dumps(_selection_payload(bound), ensure_ascii=False)}\n"
        "### 工作空间补充 Skill\n"
        f"{json.dumps(_selection_payload(supplement), ensure_ascii=False)}"
    )


def build_skill_recommendation_prompt(
    catalog: Sequence[SkillDescriptor],
    recommended_skill_ids: Sequence[str],
    agent_bound_skill_ids: Sequence[str],
) -> str:
    """Build the current-turn-only recommendation anchor."""
    by_id = {skill.skill_id: skill for skill in catalog}
    bound_ids = set(agent_bound_skill_ids)
    payload = [
        {
            "skillId": skill_id,
            "name": by_id[skill_id].name,
            "source": "当前智能体已绑定" if skill_id in bound_ids else "工作空间补充",
        }
        for skill_id in recommended_skill_ids
        if skill_id in by_id
    ]
    if not payload:
        return ""
    return (
        "## 本轮推荐 Skill\n"
        "用户本轮明确推荐以下 Skill；如适合当前任务，应优先调用 `activate_skill` 激活：\n"
        + json.dumps(payload, ensure_ascii=False)
    )


def build_skill_execution_context(
    catalog: Sequence[SkillDescriptor],
    recommended_skill_ids: Sequence[str],
    artifact_cache: SkillArtifactCache | None = None,
    prepare_sandbox_resources: bool = True,
    agent_bound_skill_ids: Sequence[str] | None = None,
) -> SkillExecutionContext:
    """Build immutable request context for an Agent or Jiuwen Function adapter."""
    immutable_catalog = tuple(catalog)
    bound, supplement = _partition_skills(
        immutable_catalog, tuple(agent_bound_skill_ids or ())
    )
    return SkillExecutionContext(
        catalog_by_id=MappingProxyType(
            {skill.skill_id: skill for skill in immutable_catalog}
        ),
        agent_bound_skills=bound,
        workspace_supplement_skills=supplement,
        recommended_skill_ids=tuple(recommended_skill_ids),
        artifact_cache=artifact_cache or default_cache(),
        prepare_sandbox_resources=prepare_sandbox_resources,
    )


def bind_skill_context(context: SkillExecutionContext | None) -> Token:
    """Bind a request context directly when no Agent object owns it."""
    return _current_skill_context.set(context)


def attach_agent_context(
    agent,
    catalog: Sequence[SkillDescriptor],
    recommended_skill_ids: Sequence[str],
    artifact_cache: SkillArtifactCache,
    agent_bound_skill_ids: Sequence[str] | None = None,
) -> None:
    """Store an immutable context on this agent only; no request data is global."""
    setattr(
        agent,
        _AGENT_CONTEXT_ATTRIBUTE,
        build_skill_execution_context(
            catalog,
            recommended_skill_ids,
            artifact_cache,
            agent_bound_skill_ids=agent_bound_skill_ids,
        ),
    )


def bind_agent_skill_context(agent) -> Token:
    """Bind one agent's context for the current async execution chain."""
    context = getattr(agent, _AGENT_CONTEXT_ATTRIBUTE, None)
    return _current_skill_context.set(context)


def reset_skill_context(token: Token) -> None:
    """Restore the preceding request context in the caller's finally block."""
    _current_skill_context.reset(token)


def get_skill_context() -> SkillExecutionContext | None:
    """Return the immutable context for the current invocation, if bound."""
    return _current_skill_context.get()


async def attach(
    top_level_agent,
    catalog: Sequence[SkillDescriptor],
    recommended_skill_ids: Sequence[str],
    artifact_cache: SkillArtifactCache | None = None,
    agent_bound_skill_ids: Sequence[str] | None = None,
) -> None:
    """Attach catalog prompt, request context, and the idempotent activation tool."""
    if not catalog:
        return

    top_level_agent.add_prompt_builder_section(
        "conversation_workspace_skills",
        build_skill_catalog_prompt(catalog, agent_bound_skill_ids or ()),
        priority=80,
    )
    attach_agent_context(
        top_level_agent,
        catalog,
        recommended_skill_ids,
        artifact_cache or default_cache(),
        agent_bound_skill_ids,
    )

    from agent_runtime.supervisor.tool.activate_skill_tool import ActivateSkillTool

    tool = ActivateSkillTool()
    result = top_level_agent.ability_manager.add(tool.card)
    if result.added and Runner.resource_mgr.get_tool(tool.card.id) is None:
        Runner.resource_mgr.add_tool(tool)
