"""Gracefully converge a ReAct request after its tool budget is exhausted."""

from __future__ import annotations

from typing import Any

from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.single_agent.rail.base import AgentCallbackContext, AgentRail


class GracefulToolBudgetRail(AgentRail):
    """Turn the model call after the final tool round into a tool-free summary.

    The mutable state lives in ``AgentCallbackContext.extra``, whose lifetime is
    one invocation. A model response containing multiple parallel tool calls is
    deliberately counted as one tool round.
    """

    priority = 100

    COMPLETED_ROUNDS_KEY = "conversation_graceful_budget.completed_tool_rounds"
    SUMMARY_MODE_KEY = "conversation_graceful_budget.summary_mode"
    PROMPT_SECTION_NAME = "conversation_tool_budget_summary"
    PROMPT_SECTION_PRIORITY = 1000

    SUMMARY_PROMPT = (
        "## 本轮工具预算已耗尽\n"
        "本次模型请求不再提供工具。请只基于当前对话中已有的信息和工具结果，"
        "向用户输出自然语言阶段性总结，不得继续请求或假设执行任何工具。\n"
        "总结应清楚说明：\n"
        "1. 已完成的工作；\n"
        "2. 已确认的结果；\n"
        "3. 尚未完成的内容及原因；\n"
        "4. 用户下一轮可以如何继续。\n"
        "不得把未验证或未完成的内容描述为已完成。"
    )
    FALLBACK_OUTPUT = (
        "本轮工具调用预算已用完，任务可能尚未完全完成。"
        "请在下一轮继续，我会基于当前对话中已经取得的结果接着处理。"
    )

    def __init__(self, max_tool_call_rounds: int):
        if isinstance(max_tool_call_rounds, bool):
            raise ValueError("max_tool_call_rounds must be a positive integer")
        try:
            normalized_budget = int(max_tool_call_rounds)
        except (TypeError, ValueError) as error:
            raise ValueError(
                "max_tool_call_rounds must be a positive integer"
            ) from error
        if normalized_budget < 1:
            raise ValueError("max_tool_call_rounds must be a positive integer")
        self.max_tool_call_rounds = normalized_budget

    async def before_model_call(self, ctx: AgentCallbackContext) -> None:
        completed_rounds = self._completed_rounds(ctx)
        if completed_rounds < self.max_tool_call_rounds:
            return

        ctx.extra[self.SUMMARY_MODE_KEY] = True
        ctx.inputs.tools = None
        ctx.agent.add_prompt_builder_section(
            self.PROMPT_SECTION_NAME,
            self.SUMMARY_PROMPT,
            priority=self.PROMPT_SECTION_PRIORITY,
        )
        workflow_logger.info(
            "Conversation tool budget exhausted; entering tool-free summary mode "
            "(completed_rounds=%s, max_tool_call_rounds=%s)",
            completed_rounds,
            self.max_tool_call_rounds,
        )

    async def after_model_call(self, ctx: AgentCallbackContext) -> None:
        response = getattr(ctx.inputs, "response", None)
        if response is None:
            return

        if ctx.extra.get(self.SUMMARY_MODE_KEY):
            unexpected_tool_calls = getattr(response, "tool_calls", None)
            if unexpected_tool_calls:
                workflow_logger.warning(
                    "Ignoring tool calls returned during conversation tool-budget "
                    "summary mode"
                )
            response.tool_calls = []

            content = getattr(response, "content", None)
            if not isinstance(content, str) or not content.strip():
                response.content = self.FALLBACK_OUTPUT
                workflow_logger.warning(
                    "Conversation tool-budget summary was empty; using deterministic "
                    "fallback output"
                )
            return

        if getattr(response, "tool_calls", None):
            ctx.extra[self.COMPLETED_ROUNDS_KEY] = self._completed_rounds(ctx) + 1

    async def before_tool_call(self, ctx: AgentCallbackContext) -> None:
        if not ctx.extra.get(self.SUMMARY_MODE_KEY):
            return

        workflow_logger.error(
            "Blocked an unexpected tool execution in conversation tool-budget "
            "summary mode"
        )
        ctx.request_force_finish(result=self._fallback_result())

    async def after_invoke(self, ctx: AgentCallbackContext) -> None:
        prompt_builder = getattr(ctx.agent, "prompt_builder", None)
        remove_section = getattr(prompt_builder, "remove_section", None)
        if callable(remove_section):
            remove_section(self.PROMPT_SECTION_NAME)

    @classmethod
    def _completed_rounds(cls, ctx: AgentCallbackContext) -> int:
        value: Any = ctx.extra.get(cls.COMPLETED_ROUNDS_KEY, 0)
        try:
            return max(0, int(value))
        except (TypeError, ValueError):
            return 0

    @classmethod
    def _fallback_result(cls) -> dict[str, str]:
        return {"output": cls.FALLBACK_OUTPUT, "result_type": "answer"}
