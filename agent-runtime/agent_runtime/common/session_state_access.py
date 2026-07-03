# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.

"""Session 状态访问工具。

提供按多级路径（如 ``io_state.nodeA.field``）从 session 状态中读取内容，以及
整体导出 session 状态信息的便捷函数。仅支持以 ``.`` 分隔的点分路径，不支持中括号索引。
"""

from typing import Any, Optional, Tuple, Union

from openjiuwen.core.session.internal.workflow import NodeSession
from openjiuwen.core.session.state.base import InMemoryCommitState, InMemoryStateLike
from openjiuwen.core.session.state.workflow_state import StateCollection
from openjiuwen.core.workflow.components import Session

# 入参可为：
# - ``node.Session``：经 ``session._inner._state`` 定位（``_inner`` 为 ``NodeSession``）；
# - ``NodeSession``：直接取 ``session._state``（即 ``StateCollection``）。
StateLike = Union[Session, NodeSession]

# 第一层 path 到 (StateCollection 内部属性名, 成员取值意图) 的映射。
# 参考 StateCollection.get_state / get_updates（即 dump）的命名：
# - *_state 类 key 取成员底层 state dict（避开 get_state 的 deepcopy，直接返回引用）；
# - *_updates 类 key 取成员的 ``_updates``（get_updates 本身无 deepcopy）；
# - trace_state 取成员本身。
# 成员取值意图为 ``None`` 表示直接返回该成员本身。
_STATE_ACCESSORS: dict[str, Tuple[str, Optional[str]]] = {
    "io_state": ("_io_state", "_state"),
    "global_state": ("_global_state", "_state"),
    "comp_state": ("_comp_state", "_state"),
    "workflow_state": ("_workflow_state", "_state"),
    "io_state_updates": ("_io_state", "_updates"),
    "global_state_updates": ("_global_state", "_updates"),
    "comp_state_updates": ("_comp_state", "_updates"),
    "workflow_state_updates": ("_workflow_state", "_updates"),
    "trace_state": ("_trace_state", None),
}


def get_state_info(session: Optional[StateLike], key: Optional[str] = None) -> Any:
    """从 session 状态中按多级 key 读取内容。

    定位规则：
    - ``session`` 可为 ``node.Session``（经 ``_inner._state`` 定位）或 ``NodeSession``
      （直接取 ``_state``）；取出的 ``_state`` 必须是 ``StateCollection`` 类型，否则
      返回 ``None``；
    - 第一层 path 必须是 ``StateCollection`` dump 中的 key（``io_state`` /
      ``global_state`` / ``comp_state`` / ``workflow_state`` 及其 ``*_updates``、
      ``trace_state``），不在其中的 key 返回 ``None``；
    - 取值成员按 key 决定：``*_state`` 返回成员底层 state dict（引用，非 deepcopy），
      ``*_updates`` 返回成员的 ``_updates``，``trace_state`` 返回成员本身；
    - 后续 path 按字典键逐级访问。

    :param session: 会话对象（``openjiuwen.core.workflow.components.Session``）。
    :param key: 点分多级路径，例如 ``io_state.nodeA.field``；为 ``None`` 或空字符串时
                返回 ``session._inner._state``。
    :return: key 指向的状态内容；类型不符、key 非法、路径不存在时返回 ``None``。

    .. note:: ``*_state`` 类返回的是底层 dict 的直接引用（为规避 ``get_state`` 的
        deepcopy 开销），对其修改会影响 session 内部状态。

    例如 ``io_state.nodeA.field`` 会被拆分为 ``["io_state", "nodeA", "field"]``：
    首段 ``"io_state"`` 定位到 ``session._inner._state._io_state`` 并取出其底层 state
    dict，剩余 ``["nodeA", "field"]`` 按字典键逐级访问。
    """
    inner_state = _get_state_collection(session)
    if inner_state is None:
        return None

    if not key:
        return inner_state

    paths = key.split(".")

    # 第一层 path -> 按 dump key 取值成员（非法 key 返回 None）
    current = _resolve_state_member(inner_state, paths[0])

    # 后续 path 取值逻辑：dict 按键逐级访问
    for path in paths[1:]:
        current = _navigate(current, path)
        if current is None:
            return None
    return current


def dump_state_info(session: Optional[StateLike]) -> dict:
    """导出 session 的完整状态信息。

    遍历 ``_STATE_ACCESSORS`` 中的所有 key，从 ``session`` 的 ``StateCollection``
    （``node.Session`` 经 ``_inner._state``、``NodeSession`` 直接 ``_state``）按各 key
    的取值规则导出为字典。

    :param session: 会话对象（``node.Session`` 或 ``NodeSession``）。
    :return: 以 dump 命名为 key、对应取值为 value 的字典；``session`` 为 ``None`` 或
             取不到 ``StateCollection`` 时返回空字典。

    返回字典的 key 为 ``io_state`` / ``global_state`` / ``comp_state`` /
    ``workflow_state`` 及其 ``*_updates``、``trace_state``，值分别为对应成员的
    底层 state dict（引用）/ ``_updates`` / 成员本身。
    """
    result: dict = {}
    inner_state = _get_state_collection(session)
    if inner_state is None:
        return result
    for key in _STATE_ACCESSORS:
        result[key] = _resolve_state_member(inner_state, key)
    return result


def _get_state_collection(session: Optional[StateLike]) -> Optional[StateCollection]:
    """取出 session 对应的 ``StateCollection``，类型不符返回 ``None``。

    入参兼容两种：
    - ``node.Session``：经 ``session._inner._state`` 定位（``_inner`` 为 ``NodeSession``）；
    - ``NodeSession``：直接取 ``session._state``（即 ``StateCollection``）。
    """
    if session is None:
        return None
    # NodeSession 直接持有 _state（StateCollection）
    if isinstance(session, NodeSession):
        inner_state = getattr(session, "_state", None)
    else:
        # node.Session 经 _inner._state 定位
        inner = getattr(session, "_inner", None)
        inner_state = getattr(inner, "_state", None)
    if not isinstance(inner_state, StateCollection):
        return None
    return inner_state


def _resolve_state_member(inner_state: StateCollection, key: str) -> Any:
    """按 dump key 取 StateCollection 上对应成员的取值。

    取值意图：
    - ``None``：返回成员本身（trace_state）；
    - ``"_state"``：返回成员底层 state dict（避开 ``get_state`` 的 deepcopy）；
    - ``"_updates"``：返回成员的 ``_updates``。

    key 不在 ``_STATE_ACCESSORS`` 中时返回 ``None``。
    """
    accessor = _STATE_ACCESSORS.get(key)
    if accessor is None:
        return None
    attr_name, member_attr = accessor
    member = getattr(inner_state, attr_name, None)
    if member_attr is None:
        return member
    if member_attr == "_state":
        return _unwrap_state_dict(member)
    return getattr(member, member_attr, None)


def _unwrap_state_dict(member: Any) -> Any:
    """避开 ``get_state`` 的 deepcopy，直接取成员底层 state dict 的引用。

    按成员具体类型处理：
    - ``InMemoryCommitState``：底层 dict 位于 ``member._state._state``
      （``member._state`` 为 ``InMemoryStateLike``）；
    - ``InMemoryStateLike``：底层 dict 位于 ``member._state``；
    - 其他类型：回退到 ``member.get_state()``（含 deepcopy）。

    返回的是底层 dict 的直接引用，调用方修改会影响 session 内部状态。
    """
    if isinstance(member, InMemoryCommitState):
        state_like = getattr(member, "_state", None)
        if isinstance(state_like, InMemoryStateLike):
            return getattr(state_like, "_state", None)
    elif isinstance(member, InMemoryStateLike):
        return getattr(member, "_state", None)
    if hasattr(member, "get_state"):
        return member.get_state()
    return None


def _navigate(current: Any, path: str) -> Any:
    """逐级访问：dict 按键，不存在或非 dict 返回 ``None``。"""
    if isinstance(current, dict):
        return current.get(path)
    return None
