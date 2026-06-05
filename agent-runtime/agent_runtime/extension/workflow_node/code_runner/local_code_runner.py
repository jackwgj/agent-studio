"""本地代码执行器模块 - 基于 subprocess 的代码执行实现"""

import json
from typing import Any

from openjiuwen.core.common.exception.codes import StatusCode
from openjiuwen.core.common.exception.errors import build_error
from openjiuwen.core.sys_operation.local.utils import OperationUtils

from .base_code_runner import CodeRunner


class LocalCodeRunner(CodeRunner):
    """本地代码执行器 - 使用 CodeOperation LOCAL mode（subprocess）

    将用户代码包装为独立的 Python 子进程执行，提供进程级别的安全隔离。
    """

    def __init__(self, code_operation):
        self._code_operation = code_operation

    async def run(self, user_code: str, inputs: dict, timeout: int = 300) -> dict:
        wrapped_code = self._wrap_user_code(user_code, inputs)
        result = await self._execute_wrapped_code(wrapped_code, timeout)
        return self._parse_result(result)

    def _wrap_user_code(self, user_code: str, inputs: dict) -> str:
        """将用户代码包装为 subprocess 可执行的脚本

        包装逻辑：
        1. import sys, json
        2. 注入用户代码
        3. 将输入数据序列化为 Python 字面量并赋值给 args 变量
        4. 调用 main(args)
        5. 将结果以 JSON 格式输出到 stdout

        注意：输入数据直接嵌入脚本中，避免 stdin 通信导致的死锁问题。
        """
        # 将输入数据序列化为 Python 字面量（支持 None/True/False 等 Python 原生语法）
        # 使用 repr() 而非 json.dumps()，因为 json.dumps 生成 JSON 语法（null/true/false），
        # 在 Python exec 上下文中无效，会导致 NameError
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
        """通过 CodeOperation 执行包装后的代码（subprocess 方式）"""
        env = OperationUtils.prepare_environment(None)

        return await self._code_operation.execute_code(
            code=wrapped_code,
            language="python",
            timeout=timeout,
            environment=env,
            options={"encoding": "utf-8"},
        )

    def _parse_result(self, raw_result: Any) -> dict:
        """解析 subprocess 执行结果，提取 JSON 输出"""

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
                reason=f"Code exited with status {raw_result.data.exit_code}: {raw_result.data.stderr}",
                workflow="n/a",
            )

        try:
            result_dict = json.loads(raw_result.data.stdout.strip())
        except json.JSONDecodeError as e:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_EXECUTION_ERROR,
                comp="flow_code",
                ability="execute",
                reason=f"Failed to parse code output as JSON: {e}",
                workflow="n/a",
            )

        if not isinstance(result_dict, dict):
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="flow_code",
                reason="Code must return a dict from main()",
                workflow="n/a",
            )

        return result_dict


def create_local_code_runner(code_operation) -> LocalCodeRunner:
    """工厂函数 - 创建 LocalCodeRunner 实例

    Args:
        code_operation: CodeOperation 实例

    Returns:
        LocalCodeRunner 实例
    """
    return LocalCodeRunner(code_operation)
