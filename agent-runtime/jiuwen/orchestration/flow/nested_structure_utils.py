#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
Contains a collection of nested structure utility functions
"""

from jiuwen.orchestration.flow.string_utils import split_string


def get_value_from_nested_structure_by_path_list(nested_structure: dict, path: str):
    """
    根据path路径从nested_structure中获取该路径对应的值
    @param nested_structure: 数据源
    @param path: 从节点ID开始的数据路径，例如node_start.systemFields.a[0]，表示node_start的key为systemFields下的key为a下的
                 第一个元素
    @return: 根据路径从数据源中获取到的值，未获取到的情况下返回 None
    """
    final_result = nested_structure
    path_list = split_string(path)
    for key in path_list:
        if isinstance(final_result, dict) and key in final_result:
            final_result = final_result.get(key)
        elif (
            isinstance(final_result, list)
            and isinstance(key, int)
            and key < len(final_result)
        ):
            final_result = final_result[key]
        else:
            return None
    return final_result


def set_value_to_nested_structure_by_path(nested_structure: dict, path: str, new_value):
    """
    根据path路径更新nested_structure该路径对应的值
    @param nested_structure: 数据源
    @param path: 从节点ID开始的数据路径，例如node_start.systemFields.a[0]，表示node_start的key为systemFields下的key为a下的
                 第一个元素
    @param new_value: 需要修改的新值
    @return: 根据路径将数据源中对应的值修改为新值
    """
    path_list = split_string(path)
    if not path_list:
        return nested_structure

    # 存储当前引用，遍历至倒数第二个 token
    current = nested_structure
    for token in path_list[:-1]:
        if (
            isinstance(current, list)
            and isinstance(token, int)
            and token < len(current)
        ):
            current = current[token]
        elif isinstance(current, dict):
            current = current.get(token, dict())
        else:
            return nested_structure

    # 最后一个 token 标识所在位置，进行赋值
    last_token = path_list[-1]
    current[last_token] = new_value

    return nested_structure
