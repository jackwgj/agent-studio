#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import enum
import os
import sys


# 通过元类方式动态添加
def add_enum_member(enum_class, name, value):
    # 获取现有成员
    existing_members = {member.name: member.value for member in enum_class}
    # 添加新成员
    existing_members[name] = value
    # 创建新的枚举类
    new_enum = enum.Enum(enum_class.__name__, existing_members)
    # 替换模块中的类
    module = sys.modules[enum_class.__module__]
    setattr(module, enum_class.__name__, new_enum)
    return new_enum


def init():
    if os.environ.get("RAG_ENABLED") != "true":
        return

    from rag.common.enum_class import PlanStrategyType

    new_type = add_enum_member(PlanStrategyType, "RAG", "RAG")

    import importlib

    module_name = "jiuwen.planner"  # 替换为你要加载的模块路径
    module = importlib.import_module(module_name)
    setattr(module, PlanStrategyType.__name__, new_type)

    from jiuwen.serve.controllers.execution.utils import PLAN_TYPE

    PLAN_TYPE["RAG"] = new_type.RAG
