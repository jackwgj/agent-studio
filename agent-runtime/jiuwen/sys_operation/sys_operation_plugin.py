# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
import inspect
from typing import Literal, Optional, Tuple, Dict, Any

from jiuwen.orchestration import Invokable
from jiuwen.plugin.decorator.function import func
from jiuwen.plugin.models.function import Function
from jiuwen.plugin.models.param import Param
from jiuwen.plugin.models.plugin import FunctionPlugin
from openjiuwen.core.sys_operation import SysOperation
from openjiuwen.core.sys_operation.fs import DEFAULT_READ_CHUNK_SIZE


class SysOperationPlugin(FunctionPlugin):
    def __init__(self, sys_operation: SysOperation):
        super().__init__("SysOperationPlugin", "本地文件/代码/命令行工具集", "")
        self._sys_op = sys_operation
        # 与 @function_plugin 装饰器相同的注册逻辑
        for _, method in inspect.getmembers(self, predicate=inspect.ismethod):
            if not hasattr(method, "__func_name__"):
                continue
            fn = Function()
            fn.name = method.__func_name__
            fn.description = method.__description__
            fn.params = method.__params__
            fn.callable_function = method
            self.function_list.append(fn)
            self.function_map[fn.name] = fn

    @staticmethod
    def _to_jsonable_data(data: Any) -> Any:
        if data is None:
            return None
        if hasattr(data, "model_dump"):
            return data.model_dump(mode="json")
        return data

    @func(
        name="read_file",
        description="Asynchronously read file with specified mode and parameters."
        " Mutually exclusive parameters: Only one of head, tail, or line_range can be specified",
        params=[
            Param(
                "path",
                "Full or relative path to the file to read (required)",
                param_type="string",
                required=True,
            ),
            Param(
                "mode",
                "Reading mode - 'text' (line-based, default) or 'bytes' (raw bytes)",
                param_type="string",
                default_value="text",
                required=False,
            ),
            Param(
                "head",
                "Number of lines to read from the start (text mode only).0 is equivalent to None",
                param_type="integer",
                default_value=0,
                required=False,
            ),
            Param(
                "tail",
                "Number of lines to read from the end (text mode only).0 is equivalent to None",
                param_type="integer",
                default_value=0,
                required=False,
            ),
            Param(
                "line_range",
                "Specific line range to read"
                " (start, end) - 1-indexed, inclusive (text mode only)."
                "If start <= 0 or end <= 0 or start > end, returns empty content.",
                param_type="array",
                required=False,
            ),
            Param(
                "encoding",
                "Character encoding for text mode (default: utf-8).",
                param_type="string",
                default_value="utf-8",
                required=False,
            ),
            Param(
                "chunk_size",
                "Maximum number of bytes to read at once (default: 0, unlimited).",
                param_type="integer",
                default_value=0,
                required=False,
            ),
            Param(
                "options",
                "Extended configuration options (dict, optional).",
                param_type="string",
                default_value=None,
                required=False,
            ),
        ],
    )
    async def read_file(
        self,
        path: str,
        *,
        mode: Literal["text", "bytes"] = "text",
        head: Optional[int] = None,
        tail: Optional[int] = None,
        line_range: Optional[Tuple[int, int]] = None,
        encoding: str = "utf-8",
        chunk_size: int = DEFAULT_READ_CHUNK_SIZE,
        options: Optional[Dict[str, Any]] = None,
    ) -> dict:
        """
        Asynchronously read file with specified mode and parameters.
        Mutually exclusive parameters: Only one of head, tail, or line_range can be specified.

        Args:
            path: Full or relative path to the file to read (required).
            mode: Reading mode - "text" (line-based, default) or "bytes" (raw bytes).
            head: Number of lines to read from the start (text mode only).0 is equivalent to None.
            tail: Number of lines to read from the end (text mode only).0 is equivalent to None.
            line_range: Specific line range to read (start, end) - 1-indexed, inclusive (text mode only).
                  If start <= 0 or end <= 0 or start > end, returns empty content.
            encoding: Character encoding for text mode (default: utf-8).
            chunk_size: Maximum number of bytes to read at once (default: 0, unlimited)
            options: Extended configuration options (dict, optional).

        Returns:
            ReadFileResult: Structured result.
        """
        fs_op = self._sys_op.fs()
        if fs_op is None:
            return {
                "errCode": -1,
                "errMessage": "文件系统操作未初始化：未加载内置 fs 实现，请确认已安装 jiuwen.sys_operation.local 并完成 OperationRegistry 扫描",
                "data": None,
            }
        result = await fs_op.read_file(
            path=path,
            mode=mode,
            head=head,
            tail=tail,
            line_range=line_range,
            encoding=encoding,
            chunk_size=chunk_size,
            options=options,
        )
        return {
            "errCode": result.code,
            "errMessage": result.message,
            "data": self._to_jsonable_data(result.data),
        }

    @func(
        name="execute_code",
        description="Execute arbitrary code asynchronously.",
        params=[
            Param(
                "code",
                "Non-empty string containing the source code to"
                " execute (required positional argument)",
                param_type="string",
                required=True,
            ),
            Param(
                "language",
                "Programming language of the code."
                " Strict type constraint to 'python' or 'javascript'",
                param_type="string",
                default_value="python",
                required=False,
            ),
            Param(
                "timeout",
                "Maximum execution time in seconds."
                " Defaults to the configured SECURITY_SANDBOX_TIMEOUT.",
                param_type="integer",
                default_value=None,
                required=False,
            ),
            Param(
                "environment",
                "Key-value dict of custom environment variables.",
                param_type="string",
                required=False,
            ),
            Param(
                "options",
                "Additional execution configuration options.",
                param_type="string",
                default_value=None,
                required=False,
            ),
        ],
    )
    async def execute_code(
        self,
        code: str,
        *,
        language: Literal["python", "javascript"] = "python",
        timeout: Optional[int] = None,
        environment: Optional[Dict[str, str]] = None,
        options: Optional[Dict[str, Any]] = None,
    ) -> dict:
        """
        Execute arbitrary code asynchronously.

        Args:
            code: Non-empty string containing the source code to execute (required positional argument).
            language: Programming language of the code. Strict type constraint to 'python' or 'javascript'.
            timeout: Maximum execution time in seconds. Defaults to the configured SECURITY_SANDBOX_TIMEOUT.
            environment: Key-value dict of custom environment variables.
            options: Additional execution configuration options.

        Returns:
            ExecuteCodeResult: Execution result.
        """
        if language not in ("python", "javascript"):
            language = "python"
        if timeout is None:
            from agent_runtime.common.config import settings

            timeout = settings.security_sandbox.timeout_seconds
        code_op = self._sys_op.code()
        if code_op is None:
            return {
                "errCode": -1,
                "errMessage": "代码执行操作未初始化：未加载内置 code 实现",
                "data": None,
            }
        result = await code_op.execute_code(
            code=code,
            language=language,
            timeout=timeout,
            environment=environment,
            options=options,
        )
        return {
            "errCode": result.code,
            "errMessage": result.message,
            "data": self._to_jsonable_data(result.data),
        }

    @func(
        name="execute_shell",
        description="Asynchronously execute a command(shell mode only).",
        params=[
            Param("command", "Command to execute", param_type="string", required=True),
            Param(
                "cwd",
                "Working directory for command execution (default: current directory).",
                param_type="string",
                required=False,
            ),
            Param(
                "timeout",
                "Command execution timeout in seconds (defaults to the configured SECURITY_SANDBOX_TIMEOUT).",
                param_type="integer",
                required=False,
            ),
            Param(
                "environment",
                "Key-value dict of custom environment variables.",
                param_type="string",
                required=False,
            ),
            Param(
                "options",
                "Additional execution configuration options.",
                param_type="string",
                default_value=None,
                required=False,
            ),
        ],
    )
    async def execute_shell(
        self,
        command: str,
        *,
        cwd: Optional[str] = None,
        timeout: Optional[int] = None,
        environment: Optional[Dict[str, str]] = None,
        options: Optional[Dict[str, Any]] = None,
    ) -> dict:
        """
        Asynchronously execute a command(shell mode only).

        Args:
            command: Command to execute.
            cwd: Working directory for command execution (default: current directory).
            timeout: Command execution timeout in seconds (defaults to the configured SECURITY_SANDBOX_TIMEOUT).
            environment: Key-value dict of custom environment variables.
            options: Additional execution configuration options.

        Returns:
            ExecuteCmdResult: Execution result.
        """
        if timeout is None:
            from agent_runtime.common.config import settings

            timeout = settings.security_sandbox.timeout_seconds
        shell_op = self._sys_op.shell()
        if shell_op is None:
            return {
                "errCode": -1,
                "errMessage": "Shell 操作未初始化：未加载内置 shell 实现",
                "data": None,
            }
        result = await shell_op.execute_cmd(
            command=command,
            cwd=cwd,
            timeout=timeout,
            environment=environment,
            options=options,
        )
        return {
            "errCode": result.code,
            "errMessage": result.message,
            "data": self._to_jsonable_data(result.data),
        }


def build_sysop_tools(sys_operation: SysOperation) -> list[Invokable]:
    """构造 SysOperationPlugin 并返回三个可用工具。"""
    plugin = SysOperationPlugin(sys_operation)
    return list(plugin.function_map.values())
