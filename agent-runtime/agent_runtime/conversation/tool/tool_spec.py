"""统一工具描述 ToolSpec —— 字段与 Java `t_tool` 列逐一对齐。

`source` 为进程内 transient 字段，仅用于区分工具来源
（supervisor_builtin / generic_builtin / child_ir / user_ir），**不写入 t_tool**。
"""

from __future__ import annotations

from dataclasses import asdict, dataclass
from typing import Optional


@dataclass
class ToolSpec:
    """内置工具注册描述（字段名对齐 t_tool 列，服务 describe/ensure 契约）。"""

    tool_id: str
    tool_display_name: str
    tool_desc: str
    type: str = "inner"  # 表注释仅允许 inner/custom；内置统一 inner
    visibility: str = "global"
    intf_type: str = "blocking"
    input_schema: Optional[str] = None
    output_schema: Optional[str] = None
    metadata: Optional[str] = None
    tool_chinese_name: Optional[str] = None
    published: int = 1
    auth_required: bool = False
    is_input_list: bool = False
    is_output_list: bool = False
    # Java ensureTools 对 type=inner 会强制 project_id/workspace_id/creator，这里仅作兜底说明
    project_id: Optional[str] = None
    workspace_id: Optional[str] = "default"
    creator: Optional[str] = "官方预置"
    creator_id: Optional[str] = "openjiuwen"
    category: Optional[str] = None
    # transient：不落库，仅描述工具来源
    source: Optional[str] = None

    def to_dict(self) -> dict:
        return asdict(self)
