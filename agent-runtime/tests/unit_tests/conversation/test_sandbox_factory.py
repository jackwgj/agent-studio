"""Tests for the conversation-only remote sandbox SysOperation factory."""

from __future__ import annotations

import pytest

from agent_runtime.common.config import SecuritySandboxSettings
from agent_runtime.conversation.sandbox import (
    CONVERSATION_SYS_OPERATION_ID,
    ConversationSandboxConfig,
    ConversationSandboxConfigurationError,
    ConversationSandboxMode,
    ConversationSysOperationFactory,
)
from openjiuwen.core.sys_operation import OperationMode
from openjiuwen.core.sys_operation.config import ContainerScope


def _security_sandbox_settings(**overrides: object) -> SecuritySandboxSettings:
    values: dict[str, object] = {
        "SECURITY_SANDBOX_SERVER": "",
        "SECURITY_SANDBOX_SSL_VERIFY": True,
        "SECURITY_SANDBOX_TYPE": "aio",
        "SECURITY_SANDBOX_IDLE_TTL": 600,
        "SECURITY_SANDBOX_TIMEOUT": 300,
        "SECURITY_SANDBOX_SCOPE": "system",
    }
    aliases = {
        "server": "SECURITY_SANDBOX_SERVER",
        "ssl_verify": "SECURITY_SANDBOX_SSL_VERIFY",
        "sandbox_type": "SECURITY_SANDBOX_TYPE",
        "idle_ttl_seconds": "SECURITY_SANDBOX_IDLE_TTL",
        "timeout_seconds": "SECURITY_SANDBOX_TIMEOUT",
        "scope": "SECURITY_SANDBOX_SCOPE",
    }
    values.update({aliases.get(key, key): value for key, value in overrides.items()})
    return SecuritySandboxSettings(_env_file=None, **values)


def _config(mode: str | None = None, **sandbox_overrides: object) -> ConversationSandboxConfig:
    environment = {} if mode is None else {"CONVERSATION_SANDBOX_MODE": mode}
    return ConversationSandboxConfig.from_security_sandbox_settings(
        _security_sandbox_settings(**sandbox_overrides), environment=environment
    )


def _create(mode: str | None = None, **sandbox_overrides: object):
    return ConversationSysOperationFactory(_config(mode, **sandbox_overrides)).create()


def test_auto_mode_is_default_and_returns_no_card_without_a_remote_server():
    assert _config().mode is ConversationSandboxMode.AUTO
    assert _create() is None


@pytest.mark.parametrize("server", ["http://sandbox.example:8082", "https://sandbox.example"])
def test_auto_mode_builds_a_sandbox_card_for_valid_http_or_https_server(server: str):
    card = _create(
        "auto",
        server=server,
        sandbox_type="aio",
        idle_ttl_seconds=321,
        timeout_seconds=123,
    )

    assert card is not None
    assert card.id == CONVERSATION_SYS_OPERATION_ID
    assert card.id not in {"flow_code_sys_op", "flow_code_sandbox_sys_op"}
    assert card.mode is OperationMode.SANDBOX
    assert card.gateway_config.launcher_config.base_url == server
    assert card.gateway_config.launcher_config.sandbox_type == "aio"
    assert card.gateway_config.launcher_config.idle_ttl_seconds == 321
    assert card.gateway_config.timeout_seconds == 123


@pytest.mark.parametrize(
    ("scope", "expected_container_scope"),
    [("system", ContainerScope.SYSTEM), ("session", ContainerScope.SESSION)],
)
def test_factory_maps_existing_sandbox_scope_to_official_container_scope(
    scope: str, expected_container_scope: ContainerScope
):
    card = _create("auto", server="https://sandbox.example", scope=scope)

    assert card is not None
    assert card.gateway_config.isolation.container_scope is expected_container_scope


@pytest.mark.parametrize(
    ("server", "match"),
    [
        ("", "CONVERSATION_SANDBOX_MODE=sandbox requires SECURITY_SANDBOX_SERVER"),
        ("relative/path", "absolute http:// or https:// URL"),
        ("ftp://sandbox.example", "absolute http:// or https:// URL"),
    ],
)
def test_sandbox_mode_requires_a_valid_remote_server(server: str, match: str):
    with pytest.raises(ConversationSandboxConfigurationError, match=match):
        _create("sandbox", server=server)


@pytest.mark.parametrize("server", ["http://sandbox.example", "https://sandbox.example:9443"])
def test_sandbox_mode_builds_only_a_sandbox_card_for_valid_server(server: str):
    card = _create("sandbox", server=server)

    assert card is not None
    assert card.mode is OperationMode.SANDBOX
    assert card.mode is not OperationMode.LOCAL


@pytest.mark.parametrize("server", ["https://sandbox.example", "not a URL"])
def test_disabled_mode_returns_no_card_even_with_valid_or_invalid_server(server: str):
    assert _create("disabled", server=server) is None


@pytest.mark.parametrize("mode", ["local", "", "REMOTE"])
def test_factory_rejects_modes_outside_auto_sandbox_and_disabled(mode: str):
    with pytest.raises(ConversationSandboxConfigurationError, match="CONVERSATION_SANDBOX_MODE"):
        _create(mode, server="https://sandbox.example")
