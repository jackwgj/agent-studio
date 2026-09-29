"""Regression tests for conversation supervisor model configuration."""

import model_service  # noqa: F401  -- register the ``studio`` model provider

from agent_runtime.common.config import settings
from agent_runtime.supervisor.config import build_react_config
from openjiuwen.core.common.clients import get_client_registry


def _expose_studio_provider_to_model_config(monkeypatch):
    """Bridge the installed SDK registry variant used by this unit-test runtime."""
    registry = get_client_registry()
    registered = registry.list_clients()
    monkeypatch.setattr(
        registry,
        "list_clients",
        lambda: list(registered) + ["llm_studio"],
    )


def test_build_react_config_propagates_llm_ssl_verification(monkeypatch):
    """The supervisor must not fall back to the SDK's verify_ssl=True default."""
    _expose_studio_provider_to_model_config(monkeypatch)
    monkeypatch.setattr(settings.llm, "ssl_verify", False)

    config = build_react_config("You are a supervisor.", "deployment-1")

    assert config.model_client_config.verify_ssl is False


def test_build_react_config_uses_runtime_iteration_setting(monkeypatch):
    _expose_studio_provider_to_model_config(monkeypatch)
    monkeypatch.setattr(settings.agent, "max_iteration", 37)

    config = build_react_config("You are a supervisor.", "deployment-1")

    assert config.max_iterations == 37
