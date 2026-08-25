"""Request-scoped remote sandbox tools for the conversation Supervisor."""

from __future__ import annotations

import base64
import json
import posixpath
import uuid
from pathlib import PurePosixPath
from typing import Any

from openjiuwen.core.foundation.tool import LocalFunction, ToolCard
from openjiuwen.core.runner import Runner
from openjiuwen.core.sys_operation import OperationMode
from openjiuwen.extensions.sys_operation.sandbox import providers as _sandbox_providers  # noqa: F401

from agent_runtime.common.config import settings
from agent_runtime.conversation.execution_context import get_conversation_execution_context

from .config import ConversationSandboxConfig
from .factory import ConversationSysOperationFactory


class ConversationSandboxToolBinder:
    """Bind remote-only sandbox tools to exactly one Supervisor invocation."""

    def __init__(self, factory: ConversationSysOperationFactory, resource_manager=None):
        self._factory = factory
        self._resource_manager = resource_manager or Runner.resource_mgr
        self._operation_id: str | None = None
        self._tag: str | None = None
        self._tool_ids: list[str] = []
        self._registered_operation = False
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
        """Return this binding's unique official SysOperation identifier."""
        return self._operation_id

    def register(self, agent) -> None:
        """Attach the three remote tools, or do nothing when sandbox is unavailable."""
        factory_card = self._factory.create()
        if factory_card is None:
            return
        if factory_card.mode is not OperationMode.SANDBOX:
            raise RuntimeError("conversation sandbox factory must return a SANDBOX SysOperationCard")

        context = get_conversation_execution_context()
        work_dir = str(context.workspace.work_dir)
        conversation_root = str(context.workspace.conversation_root)
        operation_id = f"{factory_card.id}_{uuid.uuid4().hex}"
        request_card = factory_card.model_copy(update={"id": operation_id})
        self._operation_id = operation_id
        self._tag = operation_id

        try:
            self._require_ok(
                self._resource_manager.add_sys_operation(request_card, tag=self._tag),
                "Failed to register conversation SANDBOX SysOperation",
            )
            self._registered_operation = True
            operation = self._resource_manager.get_sys_operation(operation_id, tag=self._tag)
            if operation is None:
                raise RuntimeError("conversation SANDBOX SysOperation was not available after registration")

            for tool in self._build_tools(operation, work_dir, conversation_root):
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
        if self._registered_operation and self._operation_id is not None:
            try:
                self._resource_manager.remove_sys_operation(self._operation_id, tag=self._tag)
            except Exception:
                pass

    def _build_tools(
        self, operation, work_dir: str, conversation_root: str
    ) -> list[LocalFunction]:
        if self._operation_id is None:
            raise RuntimeError("conversation sandbox operation id is not initialized")

        async def read_file(path: str, **kwargs: Any):
            return await operation.fs().read_file(
                self._absolute_posix_path(work_dir, conversation_root, path),
                **self._without_none(kwargs),
            )

        async def execute_code(code: str, cwd: str | None = None, **kwargs: Any):
            remote_kwargs = self._without_none(kwargs)
            resolved_cwd = self._absolute_posix_path(work_dir, conversation_root, cwd)
            return await operation.code().execute_code(
                self._code_with_working_directory(
                    code, remote_kwargs.get("language", "python"), resolved_cwd
                ),
                cwd=resolved_cwd,
                **remote_kwargs,
            )

        async def execute_cmd(command: str, cwd: str | None = None, **kwargs: Any):
            return await operation.shell().execute_cmd(
                command,
                cwd=self._absolute_posix_path(work_dir, conversation_root, cwd),
                **self._without_none(kwargs),
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
    def _absolute_posix_path(
        work_dir: str, conversation_root: str, value: str | None
    ) -> str:
        candidate = PurePosixPath(value) if value is not None else PurePosixPath()
        path = candidate if candidate.is_absolute() else PurePosixPath(work_dir) / candidate
        normalized = PurePosixPath(posixpath.normpath(str(path)))
        trusted_root = PurePosixPath(conversation_root)
        if not normalized.is_relative_to(trusted_root):
            raise ValueError("path must remain within the active conversation workspace")
        return str(normalized)

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
        is_ok = getattr(result, "is_ok", None)
        if not callable(is_ok) or is_ok():
            return
        error = getattr(result, "error", None)
        detail = error() if callable(error) else error
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
