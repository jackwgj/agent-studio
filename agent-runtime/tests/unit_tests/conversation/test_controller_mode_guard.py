"""Conversation Controller/PlanExecute temporary disablement tests."""

from __future__ import annotations

from unittest.mock import MagicMock

import pytest

from agent_runtime.conversation.runner.conversation_controller_runner import (
    ConversationControllerRunner,
)
from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)
from agent_runtime.runner import controller_runner as generic_controller_runner
from agent_runtime.schemas.orchestration_mgr import ExecutionRequest
from openjiuwen.core.runner import Runner
from openjiuwen.core.sys_operation import OperationMode


UNAVAILABLE_MESSAGE = (
    "Controller and PlanExecute modes are unavailable in the conversation "
    "workspace until remote sandbox support is implemented."
)


def _request(conversation_id: str = "conversation-7") -> ExecutionRequest:
    return ExecutionRequest(
        conversationId=conversation_id,
        userId="user-7",
        irPath="agent/ir/app-7/app-7.json",
        query="hello",
    )


@pytest.mark.asyncio
@pytest.mark.parametrize(
    ("execution_id", "expected_execution_id"),
    [("execution-7", "execution-7"), (None, "conversation-7")],
)
async def test_conversation_controller_runner_emits_one_stable_error_and_completes(
    execution_id, expected_execution_id
):
    """Removing the early guard would enter the generic Controller stream."""
    events = [
        event
        async for event in ConversationControllerRunner().run_streaming(
            _request(), execution_id
        )
    ]

    assert len(events) == 1
    assert events[0]["event"] == "error"
    assert events[0]["executionId"] == expected_execution_id
    assert UNAVAILABLE_MESSAGE in events[0]["data"]["message"]


@pytest.mark.asyncio
async def test_conversation_controller_runner_never_enters_generic_or_local_execution(
    monkeypatch,
):
    """Calling the generic path would risk loading IR or registering LOCAL tools."""
    generic_seams = MagicMock()

    async def unexpected_base_stream(*_args, **_kwargs):
        generic_seams.base_stream()
        raise AssertionError("conversation Controller must not enter the base runner")
        yield None

    async def unexpected_ir_load(*_args, **_kwargs):
        generic_seams.ir_load()
        raise AssertionError("conversation Controller must not load IR")

    async def unexpected_build_input(*_args, **_kwargs):
        generic_seams.build_input()
        raise AssertionError("conversation Controller must not build agent input")

    async def unexpected_convert(*_args, **_kwargs):
        generic_seams.convert()
        raise AssertionError("conversation Controller must not convert IR")

    local_registration_modes = []

    def reject_local_registration(card, *, tag):
        local_registration_modes.append(card.mode)
        if card.mode is OperationMode.LOCAL:
            raise AssertionError("conversation Controller must not register LOCAL operations")

    monkeypatch.setattr(
        generic_controller_runner.ControllerRunner,
        "run_streaming",
        unexpected_base_stream,
    )
    monkeypatch.setattr(generic_controller_runner, "async_ir_load", unexpected_ir_load)
    monkeypatch.setattr(generic_controller_runner, "build_agent_input", unexpected_build_input)
    monkeypatch.setattr(
        generic_controller_runner.IRConverter,
        "ir_to_agent_group",
        unexpected_convert,
    )
    monkeypatch.setattr(
        Runner.resource_mgr,
        "add_sys_operation",
        reject_local_registration,
    )

    events = [
        event
        async for event in ConversationControllerRunner().run_streaming(
            _request(), "execution-7"
        )
    ]

    assert [event["event"] for event in events] == ["error"]
    assert UNAVAILABLE_MESSAGE in events[0]["data"]["message"]
    assert generic_seams.method_calls == []
    assert local_registration_modes == []


def test_factory_keeps_controller_and_planexecute_on_conversation_controller_runner(
    monkeypatch,
):
    """Changing either conversation mode's Runner would bypass the disablement."""
    monkeypatch.setattr(
        "agent_runtime.serve.apis.orchestration._get_workflow_runner", lambda: None
    )
    factory = ConversationRunnerFactory(api_key="test-key", api_base="http://runner")

    controller = factory.get("Controller")

    assert isinstance(controller, ConversationControllerRunner)
    assert factory.get("PlanExecute") is controller
