"""Conversation Controller/PlanExecute routing contract tests."""

from __future__ import annotations

from agent_runtime.conversation.runner.conversation_controller_runner import (
    ConversationControllerRunner,
)
from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)


def test_factory_keeps_controller_and_planexecute_on_conversation_controller_runner(
    monkeypatch,
):
    """Both modes remain selectable and enter the real Controller execution Runner."""
    monkeypatch.setattr(
        "agent_runtime.serve.apis.orchestration._get_workflow_runner", lambda: None
    )
    factory = ConversationRunnerFactory(api_key="test-key", api_base="http://runner")

    controller = factory.get("Controller")

    assert isinstance(controller, ConversationControllerRunner)
    assert factory.get("PlanExecute") is controller
