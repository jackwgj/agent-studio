"""Request-local Jiuwen Function adapters for the conversation AIO sandbox."""

from __future__ import annotations

import uuid
from typing import Any

from jiuwen.plugin.models.function import Function
from jiuwen.plugin.models.param import Param
from openjiuwen.core.runner import Runner
from openjiuwen.core.sys_operation import OperationMode

from agent_runtime.common.config import settings
from agent_runtime.conversation.execution_context import (
    get_conversation_execution_context,
)
from agent_runtime.conversation.operation_result import (
    operation_error_detail,
    operation_succeeded,
)
from agent_runtime.conversation.sandbox.config import ConversationSandboxConfig
from agent_runtime.conversation.sandbox.factory import ConversationSysOperationFactory
from agent_runtime.conversation.sandbox.path_policy import ConversationPathPolicy
from agent_runtime.conversation.sandbox.registration import (
    get_conversation_sandbox_operation,
)
from agent_runtime.conversation.sandbox.supervisor_tools import (
    ConversationSandboxToolBinder,
)


class ConversationSandboxFunctionBinder:
    """Build PlanExecute-compatible functions without registering LOCAL tools."""

    def __init__(self, factory: ConversationSysOperationFactory, resource_manager=None):
        self._factory = factory
        self._resource_manager = resource_manager or Runner.resource_mgr
        self._scope_id = f"conversation_planexecute_{uuid.uuid4().hex}"
        self._functions: list[Function] = []

    @classmethod
    def from_runtime_settings(cls) -> "ConversationSandboxFunctionBinder":
        config = ConversationSandboxConfig.from_security_sandbox_settings(
            settings.security_sandbox
        )
        return cls(ConversationSysOperationFactory(config))

    @property
    def scope_id(self) -> str:
        return self._scope_id

    def build(self) -> list[Function]:
        """Return three request-owned remote functions, or none when disabled."""
        card = self._factory.create()
        if card is None:
            return []
        if card.mode is not OperationMode.SANDBOX:
            raise RuntimeError(
                "conversation PlanExecute requires a SANDBOX SysOperationCard"
            )

        context = get_conversation_execution_context()
        workspace = context.workspace
        policy = ConversationPathPolicy(
            conversation_root=workspace.conversation_root,
            input_dir=workspace.input_dir,
            skills_dir=workspace.skills_dir,
            work_dir=workspace.work_dir,
            output_dir=workspace.output_dir,
            tmp_dir=workspace.tmp_dir,
        )
        operation = get_conversation_sandbox_operation(self._resource_manager, card)

        async def read_file(path: str):
            result = await operation.fs().read_file(policy.resolve(path))
            return self._to_plugin_result(result)

        async def execute_code(
            code: str,
            language: str = "python",
            cwd: str | None = None,
            timeout: int | None = None,
        ):
            resolved_cwd = policy.resolve(cwd)
            kwargs = self._without_none(
                {
                    "language": language,
                    "timeout": timeout,
                    "cwd": resolved_cwd,
                    "environment": policy.environment(),
                }
            )
            wrapped = ConversationSandboxToolBinder._code_with_working_directory(
                code, language, resolved_cwd
            )
            result = await operation.code().execute_code(wrapped, **kwargs)
            return self._to_plugin_result(result)

        async def execute_cmd(
            command: str,
            cwd: str | None = None,
            timeout: int | None = None,
            shell_type: str | None = None,
        ):
            resolved_cwd = policy.resolve(cwd)
            policy.validate_command(command, resolved_cwd)
            kwargs = self._without_none(
                {
                    "cwd": resolved_cwd,
                    "timeout": timeout,
                    "shell_type": shell_type,
                    "environment": policy.environment(),
                }
            )
            result = await operation.shell().execute_cmd(command, **kwargs)
            return self._to_plugin_result(result)

        self._functions = [
            Function(
                name="read_file",
                description=(
                    "读取当前对话会话工作空间中的文件；相对路径默认从 work 目录解析，"
                    "不得访问其他会话或工作空间之外的路径。"
                ),
                params=[Param("path", "当前会话内的文件路径", param_type="string")],
                func=read_file,
            ),
            Function(
                name="execute_code",
                description=(
                    "在当前对话的远程 AIO 沙箱中执行代码；默认工作目录为当前会话 work，"
                    "正式成果必须写入当前会话 output。"
                ),
                params=[
                    Param("code", "要执行的代码", param_type="string"),
                    Param(
                        "language",
                        "代码语言，默认 python",
                        param_type="string",
                        required=False,
                    ),
                    Param(
                        "cwd",
                        "当前会话内的工作目录",
                        param_type="string",
                        required=False,
                    ),
                    Param(
                        "timeout",
                        "执行超时秒数",
                        param_type="integer",
                        required=False,
                    ),
                ],
                func=execute_code,
            ),
            Function(
                name="execute_cmd",
                description=(
                    "在当前对话的远程 AIO 沙箱中执行 Shell 命令；默认工作目录为当前会话 work，"
                    "不得访问其他会话或工作空间之外的路径。"
                ),
                params=[
                    Param("command", "要执行的 Shell 命令", param_type="string"),
                    Param(
                        "cwd",
                        "当前会话内的工作目录",
                        param_type="string",
                        required=False,
                    ),
                    Param(
                        "timeout",
                        "执行超时秒数",
                        param_type="integer",
                        required=False,
                    ),
                    Param(
                        "shell_type",
                        "Shell 类型",
                        param_type="string",
                        required=False,
                    ),
                ],
                func=execute_cmd,
            ),
        ]
        return list(self._functions)

    def cleanup(self) -> None:
        """Release request-owned references; the shared AIO operation remains alive."""
        self._functions.clear()

    @staticmethod
    def _without_none(values: dict[str, Any]) -> dict[str, Any]:
        return {key: value for key, value in values.items() if value is not None}

    @staticmethod
    def _to_plugin_result(result: Any) -> dict[str, Any]:
        """Translate a SysOperation result to Jiuwen's plugin result contract."""
        if isinstance(result, dict):
            if "errCode" in result:
                return result
            if "code" not in result:
                return {"errCode": 0, "data": result}
            code = result.get("code")
            data = result.get("data")
            message = result.get("message")
        else:
            code = getattr(result, "code", None)
            data = getattr(result, "data", None)
            message = operation_error_detail(result)

        serialized_data = ConversationSandboxFunctionBinder._serialize_result_data(data)
        if isinstance(result, dict):
            exit_code = data.get("exit_code") if isinstance(data, dict) else None
            try:
                succeeded = int(code) == 0 and (
                    exit_code is None or int(exit_code) == 0
                )
            except (TypeError, ValueError):
                succeeded = False
        else:
            succeeded = operation_succeeded(result)
        if succeeded:
            return {"errCode": 0, "data": serialized_data}

        exit_code = getattr(data, "exit_code", None)
        if exit_code is None and isinstance(data, dict):
            exit_code = data.get("exit_code")
        stderr = getattr(data, "stderr", None)
        if stderr is None and isinstance(data, dict):
            stderr = data.get("stderr")
        error_code = exit_code if exit_code not in (None, 0, "0") else code
        try:
            error_code = int(error_code)
        except (TypeError, ValueError):
            error_code = -1
        return {
            "errCode": error_code if error_code != 0 else -1,
            "errMessage": str(stderr or message or "AIO sandbox operation failed"),
            "data": serialized_data,
        }

    @staticmethod
    def _serialize_result_data(data: Any) -> Any:
        if hasattr(data, "model_dump"):
            return data.model_dump(mode="json")
        if hasattr(data, "dict"):
            return data.dict()
        if hasattr(data, "__dict__"):
            return dict(vars(data))
        return data
