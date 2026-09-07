"""Factory for conversation-specific runner instances."""

import os
from typing import Any

from .conversation_controller_runner import ConversationControllerRunner
from .conversation_react_runner import ConversationReActRunner


class ConversationRunnerFactory:
    """Lazily cache stateless conversation runner instances by supported mode."""

    def __init__(self, api_key: str | None = None, api_base: str | None = None):
        self._api_key = api_key
        self._api_base = api_base
        self._runners: dict[str, Any] = {}

    def get(self, mode: str):
        if mode not in {"ReAct", "Controller", "PlanExecute"}:
            raise ValueError(f"unsupported conversation runner mode: {mode}")

        # Reuse the platform model-provider initialization without using its
        # Runner instances. The conversation subclasses remain the execution path.
        from agent_runtime.serve.apis.orchestration import _get_workflow_runner

        _get_workflow_runner()
        runner_key = "Controller" if mode in {"Controller", "PlanExecute"} else mode
        if runner_key not in self._runners:
            kwargs = {
                "api_key": self._api_key if self._api_key is not None else os.environ.get("API_KEY"),
                "api_base": self._api_base if self._api_base is not None else os.environ.get("API_BASE"),
            }
            runner_type = (
                ConversationReActRunner
                if runner_key == "ReAct"
                else ConversationControllerRunner
            )
            self._runners[runner_key] = runner_type(**kwargs)
        return self._runners[runner_key]
