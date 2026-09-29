"""Contract tests for graceful tool-budget exhaustion."""

import asyncio
from types import SimpleNamespace
from types import MethodType
from unittest.mock import MagicMock

import pytest

from agent_runtime.conversation.rail.graceful_tool_budget_rail import (
    GracefulToolBudgetRail,
)
from openjiuwen.core.foundation.llm import AssistantMessage, ToolMessage
from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall
from openjiuwen.core.foundation.tool import ToolCard
from openjiuwen.core.single_agent.agents.react_agent import ReActAgent, ReActAgentConfig
from openjiuwen.core.single_agent.schema.agent_card import AgentCard


class _PromptBuilder:
    def __init__(self):
        self.sections = {}

    def remove_section(self, name):
        self.sections.pop(name, None)


class _Agent:
    def __init__(self):
        self.prompt_builder = _PromptBuilder()

    def add_prompt_builder_section(self, name, content, priority):
        self.prompt_builder.sections[name] = (content, priority)


def _response(content="", tool_calls=None):
    return SimpleNamespace(content=content, tool_calls=list(tool_calls or []))


def _model_ctx(*, response=None, tools=None, extra=None):
    return SimpleNamespace(
        agent=_Agent(),
        inputs=SimpleNamespace(response=response, tools=tools),
        extra={} if extra is None else extra,
        request_force_finish=MagicMock(),
    )


@pytest.mark.asyncio
async def test_one_model_tool_response_counts_as_one_round():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=3)
    ctx = _model_ctx(response=_response(tool_calls=[object()]))

    await rail.after_model_call(ctx)

    assert ctx.extra[rail.COMPLETED_ROUNDS_KEY] == 1


@pytest.mark.asyncio
async def test_parallel_tool_calls_count_as_one_round():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=3)
    ctx = _model_ctx(response=_response(tool_calls=[object(), object(), object()]))

    await rail.after_model_call(ctx)

    assert ctx.extra[rail.COMPLETED_ROUNDS_KEY] == 1


@pytest.mark.asyncio
async def test_plain_answer_does_not_consume_tool_budget():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=3)
    ctx = _model_ctx(response=_response(content="done"))

    await rail.after_model_call(ctx)

    assert rail.COMPLETED_ROUNDS_KEY not in ctx.extra


@pytest.mark.asyncio
async def test_next_model_call_after_budget_is_tool_free_and_prompt_is_idempotent():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=2)
    ctx = _model_ctx(tools=["shell", "code"])
    ctx.extra[rail.COMPLETED_ROUNDS_KEY] = 2

    await rail.before_model_call(ctx)
    await rail.before_model_call(ctx)

    assert ctx.inputs.tools is None
    assert ctx.extra[rail.SUMMARY_MODE_KEY] is True
    assert list(ctx.agent.prompt_builder.sections) == [rail.PROMPT_SECTION_NAME]
    prompt, _priority = ctx.agent.prompt_builder.sections[rail.PROMPT_SECTION_NAME]
    assert "不再提供工具" in prompt
    assert "未完成" in prompt


@pytest.mark.asyncio
async def test_budget_state_is_isolated_by_callback_context():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=1)
    exhausted = _model_ctx(response=_response(tool_calls=[object()]))
    fresh = _model_ctx(tools=["shell"])

    await rail.after_model_call(exhausted)
    await rail.before_model_call(fresh)

    assert fresh.inputs.tools == ["shell"]
    assert fresh.extra == {}


@pytest.mark.asyncio
async def test_summary_response_keeps_text_and_strips_unexpected_tool_calls():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=1)
    response = _response("已完成读取；下一轮建议继续分析。", [object()])
    ctx = _model_ctx(response=response, extra={rail.SUMMARY_MODE_KEY: True})

    await rail.after_model_call(ctx)

    assert response.content == "已完成读取；下一轮建议继续分析。"
    assert response.tool_calls == []
    ctx.request_force_finish.assert_not_called()


@pytest.mark.asyncio
async def test_empty_summary_response_uses_deterministic_answer():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=1)
    response = _response("   ", [object()])
    ctx = _model_ctx(response=response, extra={rail.SUMMARY_MODE_KEY: True})

    await rail.after_model_call(ctx)

    assert response.tool_calls == []
    assert "工具调用预算已用完" in response.content
    assert "下一轮" in response.content
    ctx.request_force_finish.assert_not_called()


@pytest.mark.asyncio
@pytest.mark.parametrize(
    "tool_name",
    [
        "execute_cmd",
        "execute_code",
        "read_file",
        "activate_skill",
        "mcp_tool",
        "workflow_tool",
        "handoff_to_agent",
    ],
)
async def test_tool_execution_is_force_finished_in_summary_mode(tool_name):
    rail = GracefulToolBudgetRail(max_tool_call_rounds=1)
    ctx = _model_ctx(extra={rail.SUMMARY_MODE_KEY: True})
    ctx.inputs.tool_name = tool_name

    await rail.before_tool_call(ctx)

    ctx.request_force_finish.assert_called_once()
    result = ctx.request_force_finish.call_args.kwargs["result"]
    assert result["result_type"] == "answer"
    assert "工具调用预算已用完" in result["output"]


@pytest.mark.asyncio
async def test_after_invoke_removes_temporary_summary_prompt():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=1)
    ctx = _model_ctx(extra={rail.SUMMARY_MODE_KEY: True})
    ctx.agent.add_prompt_builder_section(rail.PROMPT_SECTION_NAME, "prompt", 100)

    await rail.after_invoke(ctx)

    assert ctx.agent.prompt_builder.sections == {}


@pytest.mark.asyncio
async def test_new_callback_context_receives_a_fresh_budget_concurrently():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=1)
    first = _model_ctx(response=_response(tool_calls=[object()]))
    second = _model_ctx(response=_response(tool_calls=[object()]))

    await asyncio.gather(
        rail.after_model_call(first), rail.after_model_call(second)
    )

    assert first.extra[rail.COMPLETED_ROUNDS_KEY] == 1
    assert second.extra[rail.COMPLETED_ROUNDS_KEY] == 1


@pytest.mark.asyncio
async def test_model_and_tool_exceptions_are_not_reclassified_by_the_rail():
    callbacks = GracefulToolBudgetRail(max_tool_call_rounds=1).get_callbacks()
    callback_names = {event.value for event in callbacks}

    assert "on_model_exception" not in callback_names
    assert "on_tool_exception" not in callback_names
    assert "before_task_iteration" not in callback_names
    assert "after_task_iteration" not in callback_names


@pytest.mark.asyncio
async def test_normal_tool_and_model_paths_are_untouched_before_exhaustion():
    rail = GracefulToolBudgetRail(max_tool_call_rounds=2)
    model_ctx = _model_ctx(tools=["shell"])
    tool_ctx = _model_ctx()

    await rail.before_model_call(model_ctx)
    await rail.before_tool_call(tool_ctx)

    assert model_ctx.inputs.tools == ["shell"]
    assert model_ctx.extra == {}
    tool_ctx.request_force_finish.assert_not_called()


class _ControlledModel:
    def __init__(self):
        self.calls = []

    async def invoke(self, *, model, messages, tools, **_kwargs):
        self.calls.append({"model": model, "messages": messages, "tools": tools})
        call_number = len(self.calls)
        if tools:
            return AssistantMessage(
                content="",
                tool_calls=[
                    ToolCall(
                        id=f"call-{call_number}",
                        type="function",
                        name="controlled_tool",
                        arguments="{}",
                    )
                ],
            )
        return AssistantMessage(content="已完成两轮检查；下一轮建议继续处理剩余事项。")


@pytest.mark.asyncio
async def test_official_react_loop_runs_two_tool_rounds_then_a_tool_free_summary():
    config = ReActAgentConfig()
    config.configure_max_iterations(max_iterations=3)
    config.configure_prompt_template(
        [{"role": "system", "content": "Use tools until the runtime asks for a summary."}]
    )
    agent = ReActAgent(
        AgentCard(id="budget-test-agent", name="Budget test", description="test")
    )
    agent.configure(config)
    model = _ControlledModel()
    agent.set_llm(model)
    agent.ability_manager.add(
        ToolCard(
            id="controlled-tool",
            name="controlled_tool",
            description="A controlled test tool",
            input_params={"type": "object", "properties": {}},
        )
    )
    await agent.register_rail(GracefulToolBudgetRail(max_tool_call_rounds=2))
    executed_calls = []

    async def _execute_tool_call(_self, ctx, tool_calls, session, context):
        executed_calls.extend(tool_calls)
        for tool_call in tool_calls:
            await context.add_messages(
                ToolMessage(
                    tool_call_id=tool_call.id,
                    content=f"confirmed-result-{len(executed_calls)}",
                )
            )
        return [({"ok": True}, None) for _tool_call in tool_calls]

    agent._execute_tool_call = MethodType(_execute_tool_call, agent)

    result = await agent.invoke(
        {"query": "perform the controlled task", "conversation_id": "budget-test"}
    )

    assert len(executed_calls) == 2
    assert len(model.calls) == 3
    assert model.calls[0]["tools"]
    assert model.calls[1]["tools"]
    assert model.calls[2]["tools"] is None
    assert any(
        "本轮工具预算已耗尽" in str(message.content)
        for message in model.calls[2]["messages"]
    )
    assert any(
        "confirmed-result-2" in str(message.content)
        for message in model.calls[2]["messages"]
    )
    assert result == {
        "output": "已完成两轮检查；下一轮建议继续处理剩余事项。",
        "result_type": "answer",
    }


@pytest.mark.asyncio
async def test_reused_agent_gets_a_fresh_budget_for_each_invoke():
    config = ReActAgentConfig()
    config.configure_max_iterations(max_iterations=2)
    config.configure_prompt_template([{"role": "system", "content": "test"}])
    agent = ReActAgent(AgentCard(id="reused-budget-agent", name="test", description="test"))
    agent.configure(config)
    model = _ControlledModel()
    agent.set_llm(model)
    agent.ability_manager.add(
        ToolCard(
            id="controlled-tool-reused",
            name="controlled_tool",
            description="test",
            input_params={"type": "object", "properties": {}},
        )
    )
    rail = GracefulToolBudgetRail(max_tool_call_rounds=1)
    await agent.register_rail(rail)
    executed_calls = []

    async def _execute_tool_call(_self, ctx, tool_calls, session, context):
        executed_calls.extend(tool_calls)
        for tool_call in tool_calls:
            await context.add_messages(
                ToolMessage(tool_call_id=tool_call.id, content="confirmed")
            )
        return [({"ok": True}, None) for _tool_call in tool_calls]

    agent._execute_tool_call = MethodType(_execute_tool_call, agent)

    first = await agent.invoke({"query": "first", "conversation_id": "fresh-budget-1"})
    second = await agent.invoke({"query": "second", "conversation_id": "fresh-budget-2"})

    assert first["result_type"] == second["result_type"] == "answer"
    assert len(executed_calls) == 2
    assert [bool(call["tools"]) for call in model.calls] == [True, False, True, False]
