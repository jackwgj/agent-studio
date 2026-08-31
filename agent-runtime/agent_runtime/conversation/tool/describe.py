"""按请求收集本请求需要注册的内置工具描述（describe 能力，方案 A2）。

Java 在发起对话前调用 `/v1/conversation/tools/describe` 索取本请求所需工具描述，
再由 Java ensure 幂等写入 `t_tool`。子 Agent 描述从 IR（OBS）读取，不依赖 agent 表注册。
"""

from __future__ import annotations

import json

from agent_runtime.conversation.tool.tool_spec import ToolSpec
from agent_runtime.supervisor.builder import _ir_path, _load_sub_agent_description
from jiuwen.serve.controllers.execution.open_utils import async_ir_load

# Handoff 工具入参/出参 schema（与 HandoffTool 卡片保持一致）
_HANDOFF_INPUT_SCHEMA = json.dumps(
    {
        "type": "object",
        "properties": {
            "query": {
                "type": "string",
                "description": "需要子 Agent 处理的用户问题",
            }
        },
        "required": ["query"],
    }
)
_HANDOFF_OUTPUT_SCHEMA = json.dumps({"type": "object", "properties": {}})

# 通用内置工具清单（source=generic_builtin）：与 IR 显式声明无关，未来在此集中登记
_GENERIC_BUILTIN_SPECS: list[dict] = []


async def _load_sub_agent_display_name(agent_id: str) -> str:
    """子 Agent 展示名：优先 IR agentName，回退描述/默认名。"""
    try:
        ir_data = await async_ir_load(_ir_path(agent_id))
        return (
            ir_data.get("agentName")
            or ir_data.get("description")
            or f"子 Agent {agent_id[:8]}"
        )
    except Exception:
        return f"子 Agent {agent_id[:8]}"


async def collect_tool_specs(
    select_type: str,
    sub_agent_ids: list[str] | None = None,
) -> list[dict]:
    """产出本请求需要注册的工具描述（ToolSpec dict 列表）。

    - SUPERVISOR：每个子 Agent 一个 handoff 工具（type=inner，tool_id=handoff_<agent_id>）。
    - 通用内置工具：source=generic_builtin，与 IR 显式声明无关。
    """
    specs: list[dict] = []
    select_type = (select_type or "SUPERVISOR").upper()
    if select_type == "SUPERVISOR":
        for agent_id in sub_agent_ids or []:
            display_name = await _load_sub_agent_display_name(agent_id)
            description = await _load_sub_agent_description(agent_id)
            specs.append(
                ToolSpec(
                    tool_id=f"handoff_{agent_id}",
                    tool_display_name=display_name,
                    tool_desc=description,
                    input_schema=_HANDOFF_INPUT_SCHEMA,
                    output_schema=_HANDOFF_OUTPUT_SCHEMA,
                    metadata=json.dumps(
                        {"tool_type": "handoff", "target_agent_id": agent_id}
                    ),
                    source="supervisor_builtin",
                ).to_dict()
            )
    specs.extend(_GENERIC_BUILTIN_SPECS)
    return specs
