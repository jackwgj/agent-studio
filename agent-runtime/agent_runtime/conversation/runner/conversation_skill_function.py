"""Jiuwen Function adapter for the shared conversation Skill activation contract."""

from jiuwen.plugin.models.function import Function
from jiuwen.plugin.models.param import Param

from agent_runtime.supervisor.tool.activate_skill_tool import ActivateSkillTool
from agent_runtime.supervisor.skill_context import (
    SkillExecutionContext,
    bind_skill_context,
    build_skill_catalog_prompt,
    build_skill_recommendation_prompt,
    reset_skill_context,
)


class ConversationActivateSkillFunction(Function):
    """Expose the existing ActivateSkillTool through Jiuwen's Invokable protocol."""

    def __init__(self, context: SkillExecutionContext):
        catalog = list(context.catalog_by_id.values())
        bound_ids = [skill.skill_id for skill in context.agent_bound_skills]
        prompt = build_skill_catalog_prompt(catalog, bound_ids)
        recommendation = build_skill_recommendation_prompt(
            catalog, context.recommended_skill_ids, bound_ids
        )
        if recommendation:
            prompt = f"{prompt}\n\n{recommendation}"
        super().__init__(
            name="activate_skill",
            description=(
                "按 Skill ID 加载当前工作空间 Skill 的完整 SKILL.md 指令。\n"
                + prompt
            ),
            params=[
                Param(
                    "skill_id",
                    "目录中的 Skill ID",
                    param_type="string",
                    required=True,
                )
            ],
        )
        self._context = context
        self._tool = ActivateSkillTool()

    async def ainvoke(self, inputs: dict, **kwargs):
        token = bind_skill_context(self._context)
        try:
            result = await self._tool.invoke(inputs, **kwargs)
        finally:
            reset_skill_context(token)
        if isinstance(result, dict) and "error" in result:
            error = result["error"]
            return {
                "errCode": -1,
                "errMessage": error.get("message", "Skill activation failed"),
            }
        return {"errCode": 0, "data": result}
