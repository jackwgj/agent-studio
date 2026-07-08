# coding: utf-8
"""IRConverter node_defs 派生 componentName 映射的单元测试。

验证 node_defs_to_component_name_type_map() 能正确地将递归 extract_node_defs
结果展平为扁平 {component_id: {name, type}} 映射，包含子工作流内部节点。
"""

from jiuwen.serve.controllers.execution.ir_converter import IRConverter


def test_single_workflow_all_nodes_flattened():
    node_defs = {
        "wf-parent": {
            "node_start": {"node_name": "开始", "node_type": "jiuwen.start"},
            "node_end": {"node_name": "结束", "node_type": "jiuwen.end"},
            "node_msg": {"node_name": "消息", "node_type": "jiuwen.message"},
        }
    }
    result = IRConverter.node_defs_to_component_name_type_map(node_defs)
    assert result["node_start"] == {"name": "开始", "type": "jiuwen.start"}
    assert result["node_end"] == {"name": "结束", "type": "jiuwen.end"}
    assert result["node_msg"] == {"name": "消息", "type": "jiuwen.message"}


def test_nested_workflows_child_nodes_included():
    """核心断言：子工作流内部节点出现在扁平映射中。"""
    node_defs = {
        "wf-child": {
            "node_start": {"node_name": "开始", "node_type": "jiuwen.start"},
            "node_1782961091852": {"node_name": "消息", "node_type": "jiuwen.message"},
            "node_end": {"node_name": "结束", "node_type": "jiuwen.end"},
        },
        "wf-parent": {
            "node_start": {"node_name": "开始", "node_type": "jiuwen.start"},
            "node_1782961387661": {"node_name": "3级流", "node_type": "jiuwen.workflowComposite"},
            "node_end": {"node_name": "结束", "node_type": "jiuwen.end"},
        },
    }
    result = IRConverter.node_defs_to_component_name_type_map(node_defs)
    # 父流节点
    assert result["node_1782961387661"] == {"name": "3级流", "type": "jiuwen.workflowComposite"}
    # 子流内部节点 — 修复前 extract_component_name_type_map 无法覆盖
    assert result["node_1782961091852"] == {"name": "消息", "type": "jiuwen.message"}


def test_empty_node_defs_returns_empty():
    assert IRConverter.node_defs_to_component_name_type_map({}) == {}


def test_missing_fields_default_to_empty_string():
    node_defs = {
        "wf-1": {
            "node_x": {"node_type": "jiuwen.message"},
            "node_y": {"node_name": "测试"},
            "node_z": {},
        }
    }
    result = IRConverter.node_defs_to_component_name_type_map(node_defs)
    assert result["node_x"] == {"name": "", "type": "jiuwen.message"}
    assert result["node_y"] == {"name": "测试", "type": ""}
    assert result["node_z"] == {"name": "", "type": ""}


def test_collision_parent_wins():
    """相同 node_id 在父子工作流中同时存在时，父流胜出（父流节点最后加入 result，展平时后处理）。"""
    node_defs = {
        "wf-child": {
            "node_shared": {"node_name": "子流节点", "node_type": "jiuwen.message"},
        },
        "wf-parent": {
            "node_shared": {"node_name": "父流节点", "node_type": "jiuwen.message"},
        },
    }
    result = IRConverter.node_defs_to_component_name_type_map(node_defs)
    assert result["node_shared"] == {"name": "父流节点", "type": "jiuwen.message"}


def test_end_to_end_nested_child_node_lookup():
    """端到端：node_defs 派生的 map 能查找到子流内部节点，模拟 workflow_wrapper.py componentName 覆盖查找。"""
    node_defs = {
        "wf-child": {
            "node_start": {"node_name": "开始", "node_type": "jiuwen.start"},
            "node_1782961091852": {"node_name": "消息", "node_type": "jiuwen.message"},
            "node_end": {"node_name": "结束", "node_type": "jiuwen.end"},
        },
        "wf-parent": {
            "node_start": {"node_name": "开始", "node_type": "jiuwen.start"},
            "node_1782961387661": {"node_name": "3级流", "node_type": "jiuwen.workflowComposite"},
            "node_end": {"node_name": "结束", "node_type": "jiuwen.end"},
        },
    }
    node_map = IRConverter.node_defs_to_component_name_type_map(node_defs)

    # 子流 Message 节点 — tracer 发送原始 node_id 作为 componentName
    comp_id = "node_1782961091852"
    ir_metadata = node_map.get(comp_id)
    assert ir_metadata is not None
    effective_name = ir_metadata.get("name", "")
    assert effective_name == "消息"

    # 父流 workflowComposite 节点
    comp_id_parent = "node_1782961387661"
    ir_metadata_parent = node_map.get(comp_id_parent)
    assert ir_metadata_parent is not None
    assert ir_metadata_parent.get("name", "") == "3级流"

    # 不在 map 中的节点 → 回退为 payload 原始值（修复前的 bug 行为）
    assert node_map.get("node_unknown") is None
