"""Configuration contract for conversation remote code execution."""

from __future__ import annotations

import os
from dataclasses import dataclass
from enum import Enum
from typing import Mapping

from agent_runtime.common.config import SecuritySandboxSettings


class ConversationSandboxConfigurationError(ValueError):
    """Raised when conversation sandbox configuration cannot be used safely."""


class ConversationSandboxMode(str, Enum):
    """The only execution modes allowed for a conversation."""

    AUTO = "auto"
    SANDBOX = "sandbox"
    DISABLED = "disabled"


@dataclass(frozen=True, slots=True)
class ConversationSandboxConfig:
    """Explicit security sandbox settings used by a conversation factory."""

    mode: ConversationSandboxMode
    server: str
    ssl_verify: bool
    sandbox_type: str
    idle_ttl_seconds: int
    timeout_seconds: int
    scope: str

    @classmethod
    def from_security_sandbox_settings(
        cls,
        security_sandbox: SecuritySandboxSettings,
        *,
        environment: Mapping[str, str] | None = None,
    ) -> ConversationSandboxConfig:
        """Copy existing security settings and read the conversation-only mode."""
        source = os.environ if environment is None else environment
        configured_mode = source.get("CONVERSATION_SANDBOX_MODE", "auto")
        try:
            mode = ConversationSandboxMode(configured_mode)
        except ValueError as error:
            raise ConversationSandboxConfigurationError(
                "CONVERSATION_SANDBOX_MODE must be one of: auto, sandbox, disabled"
            ) from error
        return cls(
            mode=mode,
            server=security_sandbox.server,
            ssl_verify=security_sandbox.ssl_verify,
            sandbox_type=security_sandbox.sandbox_type,
            idle_ttl_seconds=security_sandbox.idle_ttl_seconds,
            timeout_seconds=security_sandbox.timeout_seconds,
            scope=security_sandbox.scope,
        )
