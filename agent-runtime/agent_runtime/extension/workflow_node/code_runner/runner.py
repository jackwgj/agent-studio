"""Runner pattern for code runner selection based on execution environment.

Strategy pattern: Different execution environments (local, sandbox, wasm, docker, etc.)
are abstracted as different "runners" implementing the same Runner interface.
The calling code only needs to use the interface without caring about underlying execution.

Factory pattern: Based on user configuration (exec_env), dynamically creates
the corresponding runner instance.
"""

from abc import ABC, abstractmethod
from typing import Any, Dict

from openjiuwen.core.sys_operation.code import BaseCodeOperation

from .local_code_runner import create_local_code_runner
from .sandbox_code_runner import SandboxCodeRunner


class Runner(ABC):
    """Abstract base class for code runners.

    Subclasses implement specific execution strategies (local, sandbox, wasm, etc.).
    """

    @abstractmethod
    def create(self, code_operation: BaseCodeOperation) -> Any:
        """Create a CodeRunner instance.

        Args:
            code_operation: Code operation for execution.

        Returns:
            A CodeRunner instance.
        """
        pass

    @abstractmethod
    def name(self) -> str:
        """Return the execution environment name.

        Returns:
            Environment name identifier (e.g., 'local', 'sandbox').
        """
        pass


class LocalRunner(Runner):
    """Runner for local code execution.

    根据 LOCAL_CODE_EXEC_MODE 环境变量选择执行方式：
    - inprocess（默认）: InprocessCodeRunner，进程内 exec() 执行
    - subprocess: LocalCodeRunner，子进程执行
    """

    def create(self, code_operation: BaseCodeOperation) -> Any:
        """Create a code runner instance based on LOCAL_CODE_EXEC_MODE.

        Args:
            code_operation: Code operation for subprocess execution (仅 subprocess 模式使用).

        Returns:
            InprocessCodeRunner 或 LocalCodeRunner 实例.
        """
        from agent_runtime.common.config import settings

        mode = settings.code_execution.local_exec_mode

        if mode == "inprocess":
            from .inprocess_code_runner import InprocessCodeRunner
            return InprocessCodeRunner()
        else:
            return create_local_code_runner(code_operation)

    def name(self) -> str:
        """Return runner name.

        Returns:
            'local'
        """
        return "local"


class SandboxRunner(Runner):
    """Runner for sandbox code execution."""

    SANDBOX_SYS_OP_ID = "flow_code_sandbox_sys_op"

    def create(self, code_operation: BaseCodeOperation) -> Any:
        """Create a SandboxCodeRunner instance.

        Args:
            code_operation: Not used for sandbox execution.

        Returns:
            SandboxCodeRunner instance.

        Raises:
            RuntimeError: If sandbox SysOperation is not registered.
        """
        from openjiuwen.core.runner import Runner as CoreRunner

        sys_op = CoreRunner.resource_mgr.get_sys_operation(self.SANDBOX_SYS_OP_ID)
        if sys_op is None:
            raise RuntimeError(
                f"Sandbox SysOperation '{self.SANDBOX_SYS_OP_ID}' not registered. "
                "Configure SECURITY_SANDBOX_SERVER to enable sandbox mode."
            )
        return SandboxCodeRunner(sys_op)

    def name(self) -> str:
        """Return runner name.

        Returns:
            'sandbox'
        """
        return "sandbox"


class CodeRunnerFactory:
    """Factory for creating code runners based on execution environment."""

    _runners: Dict[str, Runner] = {}

    @classmethod
    def register(cls, runner: Runner) -> None:
        """Register a runner with the factory.

        Args:
            runner: Runner instance to register.
        """
        cls._runners[runner.name()] = runner

    @classmethod
    def get_runner(cls, exec_env: str) -> Runner:
        """Get runner for the given execution environment.

        Args:
            exec_env: Execution environment name (e.g., 'local', 'sandbox').

        Returns:
            Appropriate runner instance. Falls back to 'local' for unknown environments.
        """
        runner = cls._runners.get(exec_env)
        if runner is None:
            return cls._runners.get("local")  # type: ignore[return-value]
        return runner

    @classmethod
    def init_default_runners(cls) -> None:
        """Register all built-in runners."""
        cls._runners = {
            "local": LocalRunner(),
            "sandbox": SandboxRunner(),
        }


CodeRunnerFactory.init_default_runners()
