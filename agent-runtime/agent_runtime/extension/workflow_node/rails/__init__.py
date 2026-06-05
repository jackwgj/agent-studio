# agent_runtime/extension/workflow_node/rails/__init__.py
"""
Rails 校验框架 - 提问器参数校验扩展

目录结构:
  rails/
    __init__.py         # 导出注册器和所有Action类
    base.py             # RailsAction基类和@action装饰器
    registry.py         # 校验器注册表
    validators/
      enum_validator.py     # enum_legality_validate - 枚举值校验
      length_validator.py   # length_limit_validate - 长度限制校验
      range_validator.py    # number_range_validate - 数值范围校验
      format_validator.py   # common_data_format_check - 通用格式校验
    formatters/
      datetime_formatter.py # date_time_format - 日期时间格式化
"""

from agent_runtime.extension.workflow_node.rails.base import RailsAction, action
from agent_runtime.extension.workflow_node.rails.registry import RailsRegistry

# 导出所有注册的action
__all__ = [
    "RailsAction",
    "action",
    "RailsRegistry",
    # validators
    "enum_legality_validate",
    "length_limit_validate",
    "number_range_validate",
    "common_data_format_check",
    "parse_time",
    # formatters
    "date_time_format",
]

# 初始化注册器并注册所有action
registry = RailsRegistry()

# 注册枚举校验器
from agent_runtime.extension.workflow_node.rails.validators.enum_validator import (
    EnumLegalityValidateAction,
)

registry.register("enum_legality_validate", EnumLegalityValidateAction)

# 注册长度校验器
from agent_runtime.extension.workflow_node.rails.validators.length_validator import (
    LengthLimitValidateAction,
)

registry.register("length_limit_validate", LengthLimitValidateAction)

# 注册范围校验器
from agent_runtime.extension.workflow_node.rails.validators.range_validator import (
    NumberRangeValidateAction,
)

registry.register("number_range_validate", NumberRangeValidateAction)

# 注册通用格式校验器
from agent_runtime.extension.workflow_node.rails.validators.format_validator import (
    CommonDataFormatCheckAction,
)

registry.register("common_data_format_check", CommonDataFormatCheckAction)

# 注册日期时间格式校验器
from agent_runtime.extension.workflow_node.rails.formatters.datetime_formatter import (
    DateTimeFormatValidateAction,
)

registry.register("date_time_format", DateTimeFormatValidateAction)

# 注册时间解析器
from agent_runtime.extension.workflow_node.rails.validators.parse_time_validator import (
    TimeParseAction,
)

registry.register("parse_time", TimeParseAction)
