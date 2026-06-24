# coding: utf-8
"""进程内代码执行器 - 使用 exec() 在当前进程内执行代码

对齐旧版 custom_code.py 的 _execute_code_safely() 实现。
无子进程开销，适用于 exec_env=local 且 LOCAL_CODE_EXEC_MODE=inprocess 的场景。
"""

import copy
import traceback
from unittest import mock

from agent_runtime.extension.workflow_node.code_runner.base_code_runner import CodeRunner
from openjiuwen.core.common.exception.codes import StatusCode
from openjiuwen.core.common.exception.errors import build_error


class InprocessCodeRunner(CodeRunner):
    """进程内代码执行器 - 使用 exec() 在当前进程内执行代码

    对齐旧版 custom_code.py 的 _execute_code_safely() 实现。
    无子进程开销，适用于 exec_env=local 且 LOCAL_CODE_EXEC_MODE=inprocess 的场景。
    """

    def __init__(self):
        self.function_log: str = ""

    async def run(self, user_code: str, inputs: dict, timeout: int = 300) -> dict:
        """在进程内执行用户代码

        Args:
            user_code: 用户源代码（包含 main 函数定义）
            inputs: 输入参数字典，传递给 main 函数
            timeout: 超时时间（秒），进程内模式不使用此参数

        Returns:
            dict: main 函数的返回值

        Raises:
            Exception: 执行失败时抛出原始异常
            BuildError: 返回值非 dict 时抛出
        """
        # 1. 深拷贝 inputs 防止污染
        inputs = copy.deepcopy(inputs)

        # 2. 构建执行代码：stdout 重定向 + 用户代码 + try/finally 恢复
        exec_code = (
            "import sys\n"
            "import io\n"
            "buffer = io.StringIO()\n"
            "original_stdout = sys.stdout\n"
            "sys.stdout = buffer\n"
            + user_code
            + "\ntry:\n"
            "    res = main(args)\n"
            "finally:\n"
            "    sys.stdout = original_stdout\n"
            "    console_log = buffer.getvalue()\n"
        )

        # 3. 执行代码（mock os.system）
        with mock.patch('os.system') as mock_system:
            mock_system.return_value = -1
            env = {
                "args": inputs,
                "__builtins__": __builtins__,
                "__name__": "__main__",
            }
            global_namespace = env
            local_namespace = env
            exec_exception = None
            error_log = None

            try:
                exec(exec_code, global_namespace, local_namespace)
            except Exception as e:
                error_log = traceback.format_exc()
                exec_exception = e

        # 4. 提取结果
        res = local_namespace.get("res", {})
        console_log = local_namespace.get("console_log", "")

        # 5. 错误 traceback 追加到 console_log（对齐旧版截取逻辑）
        # +17 = len('local_namespace')，截取变量名之后的 traceback 部分
        if error_log is not None:
            console_log += error_log[error_log.find('local_namespace') + 17:]

        # 6. 记录 function_log
        self.function_log = console_log

        # 7. 重新抛出异常
        if exec_exception is not None:
            raise exec_exception

        # 8. 返回值类型校验
        if not isinstance(res, dict):
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="flow_code",
                reason="main() must return a dict",
                workflow="n/a",
            )

        return res
