"""Jiuwen Function adapter for the shared conversation Skill activation contract."""

from jiuwen.plugin.models.function import Function
from jiuwen.plugin.models.param import Param

from agent_runtime.supervisor.tool.activate_skill_tool import ActivateSkillTool
from agent_runtime.supervisor.skill_context import (
    SkillExecutionContext,
    bind_skill_context,
    build_skill_prompt,
    reset_skill_context,
)


class ConversationActivateSkillFunction(Function):
    """Expose the existing ActivateSkillTool through Jiuwen's Invokable protocol."""

    def __init__(self, context: SkillExecutionContext):
        catalog = list(context.catalog_by_id.values())
        super().__init__(
            name="activate_skill",
            description=(
                "按 Skill ID 加载当前工作空间 Skill 的完整 SKILL.md 指令。\n"
                + build_skill_prompt(catalog, context.recommended_skill_ids)
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
