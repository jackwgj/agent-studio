#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

from jiuwen.orchestration.common.runtime_env import (
    REQUEST_TIMEOUT_MCP,
    REQUEST_TIMEOUT_WORKFLOW,
)
from jiuwen.orchestration.flow.base_workflow import Node
from jiuwen.orchestration.flow.constant import ENVIRONMENT_VARIABLES
from jiuwen.orchestration.flow.enum import NodeType
from jiuwen.orchestration.flow.runtime_context import RuntimeContext

MS_P_S = 1000


def rewrite_timeout(runtime: RuntimeContext, invokable: Node, timeout: float) -> float:
    """
    Rewrite the timeout value based on the node type and environment variables.

    Args:
        runtime (RuntimeContext): The runtime context containing environment variables.
        invokable (Node): The node for which the timeout is being set.
        timeout (float): The default timeout value in seconds.

    Returns:
        float: The rewritten timeout value in seconds. If conditions are not met, the default timeout is returned.
    """

    # 如果节点类型不是 MCP 或 SUB_WORKFLOW，直接返回默认超时时间
    if invokable.type not in {NodeType.MCP.value, NodeType.SUB_WORKFLOW.value}:
        return timeout

    # 获取环境变量
    env = runtime.get(ENVIRONMENT_VARIABLES)
    if env is None:
        return timeout
    # 获取 REQUEST_TIMEOUT_MCP 的值
    if invokable.type == NodeType.MCP.value:
        return _get_timeout(env, REQUEST_TIMEOUT_MCP, timeout)
    if invokable.type == NodeType.SUB_WORKFLOW.value:
        return _get_timeout(env, REQUEST_TIMEOUT_WORKFLOW, timeout)
    return timeout


def _get_timeout(env: dict, key: str, timeout: float) -> float:
    val = env.get(key)
    if val is None or not isinstance(val, int):
        return timeout

    # 返回转换后的超时时间（毫秒转秒）
    new_timeout = val / MS_P_S
    return max(new_timeout, timeout)
