import pytest

from agent_runtime.runner.controller_runner import ControllerRunner
from agent_runtime.runner.react_agent_runner import ReActAgentRunner

from agent_runtime.conversation.runner.conversation_controller_runner import (
    ConversationControllerRunner,
)
from agent_runtime.conversation.runner.conversation_react_runner import (
    ConversationReActRunner,
)
from agent_runtime.conversation.runner.conversation_runner_factory import (
    ConversationRunnerFactory,
)


def test_conversation_runners_extend_official_runtime_runners():
    assert issubclass(ConversationReActRunner, ReActAgentRunner)
    assert issubclass(ConversationControllerRunner, ControllerRunner)


def test_factory_maps_supported_modes_and_reuses_instances():
    factory = ConversationRunnerFactory(api_key="test-key", api_base="http://runner")

    react = factory.get("ReAct")
    assert isinstance(react, ConversationReActRunner)
    assert factory.get("ReAct") is react

    controller = factory.get("Controller")
    assert isinstance(controller, ConversationControllerRunner)
    assert factory.get("PlanExecute") is controller
    assert controller is not react


def test_factory_rejects_workflow_mode_for_this_increment():
    factory = ConversationRunnerFactory()

    with pytest.raises(ValueError, match="unsupported conversation runner mode"):
        factory.get("Workflow")


def test_factory_runner_instances_do_not_store_request_state():
    factory = ConversationRunnerFactory()

    react = factory.get("ReAct")
    assert set(vars(react)) <= {"_api_key", "_api_base"}

    controller = factory.get("Controller")
    assert set(vars(controller)) <= {"_api_key", "_api_base"}
