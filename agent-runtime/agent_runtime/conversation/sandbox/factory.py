"""Build a remote-only SysOperation card for conversation execution."""

from __future__ import annotations

import inspect
from urllib.parse import urlsplit

from openjiuwen.core.sys_operation import OperationMode, SysOperationCard
from openjiuwen.core.sys_operation.config import (
    ContainerScope,
    PreDeployLauncherConfig,
    SandboxGatewayConfig,
    SandboxIsolationConfig,
)

from .config import (
    ConversationSandboxConfig,
    ConversationSandboxConfigurationError,
    ConversationSandboxMode,
)


CONVERSATION_SYS_OPERATION_ID = "conversation_sandbox_sys_op"


class ConversationSysOperationFactory:
    """Create an unregistered SANDBOX card for one conversation execution."""

    def __init__(self, config: ConversationSandboxConfig):
        self._config = config

    def create(self) -> SysOperationCard | None:
        """Return a remote sandbox card, or no capability when it is disabled."""
        if self._config.mode is ConversationSandboxMode.DISABLED:
            return None

        server = self._validated_server()
        if not server:
            return None

        return SysOperationCard(
            id=CONVERSATION_SYS_OPERATION_ID,
            mode=OperationMode.SANDBOX,
            gateway_config=self._gateway_config(server),
        )

    def _validated_server(self) -> str:
        server = self._config.server.strip()
        if not server:
            if self._config.mode is ConversationSandboxMode.SANDBOX:
                raise ConversationSandboxConfigurationError(
                    "CONVERSATION_SANDBOX_MODE=sandbox requires SECURITY_SANDBOX_SERVER"
                )
            return ""

        try:
            parsed = urlsplit(server)
            hostname = parsed.hostname
            _ = parsed.port
        except ValueError as error:
            raise ConversationSandboxConfigurationError(
                "SECURITY_SANDBOX_SERVER must be an absolute http:// or https:// URL"
            ) from error

        if (
            parsed.scheme not in {"http", "https"}
            or not parsed.netloc
            or not hostname
            or any(character.isspace() for character in server)
        ):
            raise ConversationSandboxConfigurationError(
                "SECURITY_SANDBOX_SERVER must be an absolute http:// or https:// URL"
            )
        return server

    def _gateway_config(self, server: str) -> SandboxGatewayConfig:
        scope = self._container_scope()
        gateway_options: dict[str, object] = {
            "isolation": SandboxIsolationConfig(container_scope=scope),
            "launcher_config": PreDeployLauncherConfig(
                base_url=server,
                sandbox_type=self._config.sandbox_type,
                idle_ttl_seconds=self._config.idle_ttl_seconds,
            ),
            "timeout_seconds": self._config.timeout_seconds,
        }
        gateway_parameters = inspect.signature(SandboxGatewayConfig).parameters
        if "ssl_verify" in gateway_parameters:
            gateway_options["ssl_verify"] = self._config.ssl_verify
        elif "verify_ssl" in gateway_parameters:
            gateway_options["verify_ssl"] = self._config.ssl_verify
        return SandboxGatewayConfig(**gateway_options)

    def _container_scope(self) -> ContainerScope:
        if self._config.scope == "system":
            return ContainerScope.SYSTEM
        if self._config.scope == "session":
            return ContainerScope.SESSION
        raise ConversationSandboxConfigurationError(
            "SECURITY_SANDBOX_SCOPE must be either system or session for conversations"
        )
