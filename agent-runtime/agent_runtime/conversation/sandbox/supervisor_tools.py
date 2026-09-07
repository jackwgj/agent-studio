"""Request-scoped remote sandbox tools for the conversation Supervisor."""

from __future__ import annotations

import base64
import json
import uuid
from typing import Any

from openjiuwen.core.foundation.tool import LocalFunction, ToolCard
from openjiuwen.core.runner import Runner
from openjiuwen.core.sys_operation import OperationMode
from openjiuwen.extensions.sys_operation.sandbox import providers as _sandbox_providers  # noqa: F401

from agent_runtime.common.config import settings
from agent_runtime.conversation.execution_context import get_conversation_execution_context
from agent_runtime.conversation.operation_result import (
    operation_error_detail,
    operation_succeeded,
)

from .config import ConversationSandboxConfig
from .factory import ConversationSysOperationFactory
from .path_policy import ConversationPathPolicy
from .registration import get_conversation_sandbox_operation


class ConversationSandboxToolBinder:
    """Bind remote-only sandbox tools to exactly one Supervisor invocation."""

    def __init__(self, factory: ConversationSysOperationFactory, resource_manager=None):
        self._factory = factory
        self._resource_manager = resource_manager or Runner.resource_mgr
        self._operation_id: str | None = None
        self._tag: str | None = None
        self._tool_ids: list[str] = []
        self._cleaned_up = False

    @classmethod
    def from_runtime_settings(cls) -> ConversationSandboxToolBinder:
        """Build a binder from the conversation's remote sandbox configuration."""
        config = ConversationSandboxConfig.from_security_sandbox_settings(
            settings.security_sandbox
        )
        return cls(ConversationSysOperationFactory(config))

    @property
    def operation_id(self) -> str | None:
        """Return this request's unique tool namespace (not the shared operation ID)."""
        return self._operation_id

    def register(self, agent) -> None:
        """Attach the three remote tools, or do nothing when sandbox is unavailable."""
        factory_card = self._factory.create()
        if factory_card is None:
            return
        if factory_card.mode is not OperationMode.SANDBOX:
            raise RuntimeError("conversation sandbox factory must return a SANDBOX SysOperationCard")

        context = get_conversation_execution_context()
        path_policy = ConversationPathPolicy(
            conversation_root=context.workspace.conversation_root,
            input_dir=context.workspace.input_dir,
            skills_dir=context.workspace.skills_dir,
            work_dir=context.workspace.work_dir,
            output_dir=context.workspace.output_dir,
            tmp_dir=context.workspace.tmp_dir,
        )
        operation_id = f"{factory_card.id}_{uuid.uuid4().hex}"
        self._operation_id = operation_id
        self._tag = operation_id

        try:
            operation = get_conversation_sandbox_operation(self._resource_manager, factory_card)

            for tool in self._build_tools(operation, path_policy):
                self._require_ok(
                    self._resource_manager.add_tool(tool, tag=self._tag),
                    f"Failed to register conversation sandbox tool {tool.card.name}",
                )
                self._tool_ids.append(tool.card.id)
                result = agent.ability_manager.add(tool.card)
                if getattr(result, "added", True) is False:
                    raise RuntimeError(
                        f"Failed to attach conversation sandbox tool {tool.card.name} to Supervisor"
                    )
        except Exception:
            self.cleanup()
            raise

    def cleanup(self) -> None:
        """Remove only resources owned by this binding; it is safe to call repeatedly."""
        if self._cleaned_up:
            return
        self._cleaned_up = True
        if self._tag is None:
            return

        for tool_id in reversed(self._tool_ids):
            try:
                self._resource_manager.remove_tool(tool_id, tag=self._tag)
            except Exception:
                pass
        # The shared operation outlives this request and may be in use by a
        # parent, child, artifact bridge or concurrent conversation.

    def _build_tools(
        self, operation, path_policy: ConversationPathPolicy
    ) -> list[LocalFunction]:
        if self._operation_id is None:
            raise RuntimeError("conversation sandbox operation id is not initialized")

        async def read_file(path: str, **kwargs: Any):
            return await operation.fs().read_file(
                path_policy.resolve(path),
                **self._without_none(kwargs),
            )

        async def execute_code(code: str, cwd: str | None = None, **kwargs: Any):
            remote_kwargs = self._without_none(kwargs)
            resolved_cwd = path_policy.resolve(cwd)
            remote_kwargs["environment"] = path_policy.environment(
                remote_kwargs.get("environment")
            )
            return await operation.code().execute_code(
                self._code_with_working_directory(
                    code, remote_kwargs.get("language", "python"), resolved_cwd
                ),
                cwd=resolved_cwd,
                **remote_kwargs,
            )

        async def execute_cmd(command: str, cwd: str | None = None, **kwargs: Any):
            resolved_cwd = path_policy.resolve(cwd)
            path_policy.validate_command(command, resolved_cwd)
            remote_kwargs = self._without_none(kwargs)
            remote_kwargs["environment"] = path_policy.environment(
                remote_kwargs.get("environment")
            )
            return await operation.shell().execute_cmd(
                command,
                cwd=resolved_cwd,
                **remote_kwargs,
            )

        return [
            LocalFunction(self._tool_card("read_file", self._read_file_schema()), read_file),
            LocalFunction(self._tool_card("execute_code", self._execute_code_schema()), execute_code),
            LocalFunction(self._tool_card("execute_cmd", self._execute_cmd_schema()), execute_cmd),
        ]

    def _tool_card(self, name: str, input_params: dict[str, Any]) -> ToolCard:
        return ToolCard(
            id=f"{self._operation_id}.{name}",
            name=name,
            description=f"Run {name} in the conversation remote sandbox.",
            input_params=input_params,
        )

    @staticmethod
    def _code_with_working_directory(code: str, language: str, cwd: str) -> str:
        """Embed a remote cwd because the installed AIO code provider ignores it."""
        encoded = base64.b64encode(code.encode("utf-8")).decode("ascii")
        if language == "python":
            return (
                "import base64 as __conversation_base64\n"
                "import os as __conversation_os\n"
                f"__conversation_os.chdir({cwd!r})\n"
                "exec(compile(__conversation_base64.b64decode("
                f"'{encoded}'), '<conversation>', 'exec'))"
            )
        if language == "javascript":
            return (
                f"process.chdir({json.dumps(cwd)});\n"
                "require('vm').runInThisContext(Buffer.from("
                f"'{encoded}', 'base64').toString('utf8'), "
                "{ filename: '<conversation>' });"
            )
        return code

    @staticmethod
    def _require_ok(result, message: str) -> None:
        if operation_succeeded(result):
            return
        detail = operation_error_detail(result)
        raise RuntimeError(f"{message}: {detail}")

    @staticmethod
    def _without_none(values: dict[str, Any]) -> dict[str, Any]:
        return {key: value for key, value in values.items() if value is not None}

    @staticmethod
    def _read_file_schema() -> dict[str, Any]:
        return {
            "type": "object",
            "properties": {"path": {"type": "string"}},
            "required": ["path"],
        }

    @staticmethod
    def _execute_code_schema() -> dict[str, Any]:
        return {
            "type": "object",
            "properties": {
                "code": {"type": "string"},
                "language": {"type": "string"},
                "timeout": {"type": "integer"},
                "environment": {"type": "object"},
                "cwd": {"type": "string"},
                "options": {"type": "object"},
            },
            "required": ["code"],
        }

    @staticmethod
    def _execute_cmd_schema() -> dict[str, Any]:
        return {
            "type": "object",
            "properties": {
                "command": {"type": "string"},
                "cwd": {"type": "string"},
                "timeout": {"type": "integer"},
                "environment": {"type": "object"},
                "options": {"type": "object"},
                "shell_type": {"type": "string"},
            },
            "required": ["command"],
        }
