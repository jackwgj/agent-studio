#  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.


def resolve_variable_references(
    template: str, agent_inputs: dict, memory_vars: dict
) -> str:
    """
    解析模板中的变量引用
    - {{inputs.xxx}} 或 {{inputs.xxx.yyy}} → 从 agent_inputs 获取值（支持嵌套）
    - {{memory.xxx}} 或 {{memory.xxx.yyy}} → 从 memory_vars 获取值（支持嵌套）
    若路径不存在，则保留原始占位符
    """
    import re

    def get_nested_value(data: dict, path: str):
        """安全获取嵌套字典的值，路径如 'a.b.c'；若任一节点缺失，返回 None"""
        if not isinstance(data, dict):
            return None
        keys = path.split(".")
        value = data
        for key in keys:
            if isinstance(value, dict) and key in value:
                value = value[key]
            else:
                return None
        return value

    def replace_inputs(match):
        path = match.group(1)  # e.g., "school.age"
        value = get_nested_value(agent_inputs, path)
        if value is not None:
            return str(value)
        return "none"

    def replace_memory(match):
        path = match.group(1)  # e.g., "user.profile.name"
        value = get_nested_value(memory_vars, path)
        if value is not None:
            return str(value)
        return "none"

    # 替换 inputs.*（支持嵌套）
    template = re.sub(r"\{\{inputs\.([\w.]+)\}\}", replace_inputs, template)

    # 替换 memory.*（现在也支持嵌套）
    template = re.sub(r"\{\{memory\.([\w.]+)\}\}", replace_memory, template)

    return template
