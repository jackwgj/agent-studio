"""Sandbox code runner module - executes code via SysOperation SANDBOX mode."""

import json
from typing import Any

from openjiuwen.core.common.exception.codes import StatusCode
from openjiuwen.core.common.exception.errors import build_error
from openjiuwen.core.common.logging import workflow_logger


class SandboxCodeRunner:
    """Sandbox code runner - executes code via SysOperation SANDBOX mode.

    Uses AIO sandbox container for isolated code execution.
    The SysOperation must be registered at startup with SANDBOX mode.
    """

    def __init__(self, sys_operation):
        self._sys_operation = sys_operation

    async def run(self, user_code: str, inputs: dict, timeout: int = 300) -> dict:
        wrapped_code = self.wrap_user_code(user_code, inputs)
        workflow_logger.warning("[SandboxCodeRunner] Executing code in sandbox mode")
        result = await self._execute_wrapped_code(wrapped_code, timeout)
        return self.parse_result(result)

    @staticmethod
    def wrap_user_code(user_code: str, inputs: dict) -> str:
        inputs_literal = repr(inputs)

        return f"""
import sys
import json

# ===== 用户代码开始 =====
{user_code}
# ===== 用户代码结束 =====

# 输入参数（直接嵌入脚本，避免 stdin 通信问题）
args = {inputs_literal}

# 调用 main 函数
result = main(args)

# 输出 JSON 结果到 stdout（default=str 处理 Decimal 等非标准 JSON 类型）
print(json.dumps(result, default=str))
"""

    async def _execute_wrapped_code(self, wrapped_code: str, timeout: int) -> Any:
        code_op = self._sys_operation.code()
        return await code_op.execute_code(
            code=wrapped_code, language="python", timeout=timeout
        )

    @staticmethod
    def parse_result(raw_result: Any) -> dict:
        if raw_result.code != StatusCode.SUCCESS.code:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                comp="flow_code",
                ability="execute",
                reason=raw_result.message,
                workflow="n/a",
            )

        if raw_result.data.exit_code != 0:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                comp="flow_code",
                ability="execute",
                reason=f"Sandbox code exited with status {raw_result.data.exit_code}: {raw_result.data.stderr}",
                workflow="n/a",
            )

        try:
            result_dict = json.loads(raw_result.data.stdout.strip())
        except json.JSONDecodeError as e:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                comp="flow_code",
                ability="execute",
                reason=f"Failed to parse sandbox code output as JSON: {e}",
                workflow="n/a",
            ) from e

        if not isinstance(result_dict, dict):
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="flow_code",
                reason="Sandbox code must return a dict from main()",
                workflow="n/a",
            )

        return result_dict
