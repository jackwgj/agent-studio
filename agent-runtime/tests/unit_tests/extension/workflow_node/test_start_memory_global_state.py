"""验证 Start 节点将 memory 变量写入 global_state 的测试"""

from unittest.mock import MagicMock

import pytest

from jiuwen.extension.workflow_node.start import Start


pytestmark = pytest.mark.asyncio

_SYS_FIELDS = {
    "conversationHistory": [],
    "conversationId": "conv-1",
    "userId": "u1",
    "dialogueCount": 1,
    "currentTime": "2026-06-12",
}


def _make_session_mock():
    """构造一个 mock session，模拟 openjiuwen 的 Session 接口。

    返回 (session_mock, global_updates, outputs_updates)，
    global_updates 和 outputs_updates 是列表，记录所有 update_global / set_outputs 调用。
    """
    global_updates = []
    outputs_updates = []

    class FakeNodeState:
        """模拟 NodeSession 使用的 state 对象，支持 update_global / set_outputs / commit。"""
        def __init__(self, node_id="node_start"):
            self._node_id = node_id

        @staticmethod
        def set_outputs(data):
            outputs_updates.append(data)

        @staticmethod
        def update_global(data):
            global_updates.append(data)

        @staticmethod
        def commit():
            pass

        @staticmethod
        def get_state():
            return {"io_state": {}}

        @staticmethod
        def get_by_prefix(key, prefix):
            return None

        @staticmethod
        def get(key):
            return None

        def create_node_state(self, executable_id, parent_id):
            return self

    fake_node_state = FakeNodeState()

    inner_mock = MagicMock()
    inner_mock.state.return_value = fake_node_state
    inner_mock.parent.return_value = MagicMock()

    # inner_session.parent().state().create_node_state(...) 需要返回一个支持
    # update_global / set_outputs / commit 的对象
    parent_session_mock = MagicMock()
    parent_session_mock.state.return_value.create_node_state.return_value = fake_node_state
    inner_mock.parent.return_value = parent_session_mock

    session_mock = MagicMock()
    session_mock._inner = inner_mock  # pylint: disable=protected-access

    return session_mock, global_updates, outputs_updates


async def test_start_writes_memory_to_global_state():
    """Start 节点应将 memory 变量写入 global_state，key 格式为 MEMORY_VARIABLE.xxx"""
    conf = {
        "preDefinedFields": {
            "inputs": [
                {
                    "schema": [
                        {
                            "id": "parent_global_var",
                            "type": "string",
                            "storage_method": "assignment",
                            "aging_level": "session",
                            "default_value": "",
                        }
                    ],
                    "id": "memory",
                    "type": "object",
                }
            ]
        },
        "userFields": {"inputs": [], "outputs": []},
        "systemFields": {"inputs": [], "outputs": []},
    }

    start = Start(conf)
    inputs = {
        "query": "test",
        "sys": _SYS_FIELDS,
        "conversationId": "conv-1",
        "userId": "u1",
        "global_variables": {"parent_global_var": "2580"},
    }

    session_mock, global_updates, outputs_updates = _make_session_mock()
    context_mock = MagicMock()

    await start.invoke(inputs, session_mock, context_mock)

    # 验证 update_global 被调用，且包含 MEMORY_VARIABLE.parent_global_var
    assert len(global_updates) > 0, "update_global should be called"
    merged_global = {}
    for update in global_updates:
        merged_global.update(update)
    assert "MEMORY_VARIABLE.parent_global_var" in merged_global, \
        f"Expected MEMORY_VARIABLE.parent_global_var in updates, got keys: {list(merged_global.keys())}"
    assert merged_global.get("MEMORY_VARIABLE.parent_global_var") == "2580"


async def test_start_no_memory_no_global_update():
    """当 memory 为空时，不应写入 MEMORY_VARIABLE 到 global_state"""
    conf = {
        "userFields": {"inputs": [], "outputs": []},
        "systemFields": {"inputs": [], "outputs": []},
    }
    start = Start(conf)
    inputs = {
        "query": "test",
        "sys": _SYS_FIELDS,
        "conversationId": "conv-1",
        "userId": "u1",
    }

    session_mock, global_updates, outputs_updates = _make_session_mock()
    context_mock = MagicMock()

    await start.invoke(inputs, session_mock, context_mock)

    # 验证没有 MEMORY_VARIABLE 写入（可能有 _session_var_defs 写入，但没有 MEMORY_VARIABLE）
    merged_global = {}
    for update in global_updates:
        merged_global.update(update)
    memory_variable_keys = [k for k in merged_global if k.startswith("MEMORY_VARIABLE.")]
    assert len(memory_variable_keys) == 0, \
        f"No MEMORY_VARIABLE keys expected when memory is empty, got: {memory_variable_keys}"


async def test_start_filters_input_params_from_global_state():
    """输入参数不应被写入 MEMORY_VARIABLE 到 global_state，只有 preDefinedFields 定义的变量才写入"""
    conf = {
        "preDefinedFields": {
            "inputs": [
                {
                    "schema": [
                        {
                            "id": "parent_global_var",
                            "type": "string",
                            "storage_method": "assignment",
                            "aging_level": "session",
                            "default_value": "",
                        }
                    ],
                    "id": "memory",
                    "type": "object",
                }
            ]
        },
        "userFields": {
            "inputs": [
                {"id": "input_param", "type": "string", "required": False, "default_value": ""},
            ],
            "outputs": [
                {"id": "input_param", "type": "string", "required": False, "default_value": ""},
            ],
        },
        "systemFields": {"inputs": [], "outputs": []},
    }

    start = Start(conf)
    inputs = {
        "query": "test",
        "sys": _SYS_FIELDS,
        "conversationId": "conv-1",
        "userId": "u1",
        "global_variables": {
            "input_param": "123",
            "parent_global_var": "456",
        },
    }

    session_mock, global_updates, outputs_updates = _make_session_mock()
    context_mock = MagicMock()

    await start.invoke(inputs, session_mock, context_mock)

    merged_global = {}
    for update in global_updates:
        merged_global.update(update)

    # 记忆变量应写入
    assert "MEMORY_VARIABLE.parent_global_var" in merged_global, \
        f"Expected MEMORY_VARIABLE.parent_global_var, got keys: {list(merged_global.keys())}"
    assert merged_global.get("MEMORY_VARIABLE.parent_global_var") == "456"

    # 输入参数不应写入
    assert "MEMORY_VARIABLE.input_param" not in merged_global, \
        "input_param should NOT be in MEMORY_VARIABLE"


async def test_start_memory_includes_permanent_vars():
    """permanent 级别的记忆变量也应写入 MEMORY_VARIABLE"""
    conf = {
        "preDefinedFields": {
            "inputs": [
                {
                    "schema": [
                        {
                            "id": "perm_var",
                            "type": "string",
                            "storage_method": "assignment",
                            "aging_level": "permanent",
                            "default_value": "",
                        },
                        {
                            "id": "session_var",
                            "type": "string",
                            "storage_method": "assignment",
                            "aging_level": "session",
                            "default_value": "",
                        },
                    ],
                    "id": "memory",
                    "type": "object",
                }
            ]
        },
        "userFields": {
            "inputs": [
                {"id": "input_param", "type": "string", "required": False, "default_value": ""},
            ],
            "outputs": [
                {"id": "input_param", "type": "string", "required": False, "default_value": ""},
            ],
        },
        "systemFields": {"inputs": [], "outputs": []},
    }

    start = Start(conf)
    inputs = {
        "query": "test",
        "sys": _SYS_FIELDS,
        "conversationId": "conv-1",
        "userId": "u1",
        "global_variables": {
            "input_param": "input_val",
            "perm_var": "perm_val",
            "session_var": "session_val",
        },
    }

    session_mock, global_updates, outputs_updates = _make_session_mock()
    context_mock = MagicMock()

    await start.invoke(inputs, session_mock, context_mock)

    merged_global = {}
    for update in global_updates:
        merged_global.update(update)

    # session 级和 permanent 级记忆变量都应写入
    assert "MEMORY_VARIABLE.session_var" in merged_global
    assert merged_global.get("MEMORY_VARIABLE.session_var") == "session_val"
    assert "MEMORY_VARIABLE.perm_var" in merged_global
    assert merged_global.get("MEMORY_VARIABLE.perm_var") == "perm_val"

    # 输入参数不应写入
    assert "MEMORY_VARIABLE.input_param" not in merged_global
