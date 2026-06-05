# agent_runtime/extension/workflow_node/rails/registry.py
"""
Rails 校验器注册表
"""

from __future__ import annotations

from typing import Any, Dict, Optional, Type

from agent_runtime.extension.workflow_node.rails.base import ActionConfig, RailsAction
from openjiuwen.core.common.logging import workflow_logger


class RailsRegistry:
    """Rails Action 注册表"""

    _instance: Optional["RailsRegistry"] = None

    def __init__(self):
        self._actions: Dict[str, Type[RailsAction]] = {}

    @classmethod
    def get_instance(cls) -> "RailsRegistry":
        """获取单例实例"""
        if cls._instance is None:
            cls._instance = cls()
            # 单例创建后，自动初始化所有 actions
            cls._instance._register_all_actions()
        else:
            # 如果单例存在但没有 actions，重新初始化
            if not cls._instance._actions:
                cls._instance._register_all_actions()
        return cls._instance

    def _register_all_actions(self):
        """注册所有 actions"""
        # 注册枚举校验器
        from agent_runtime.extension.workflow_node.rails.validators.enum_validator import (
            EnumLegalityValidateAction,
        )

        self.register("enum_legality_validate", EnumLegalityValidateAction)

        # 注册长度校验器
        from agent_runtime.extension.workflow_node.rails.validators.length_validator import (
            LengthLimitValidateAction,
        )

        self.register("length_limit_validate", LengthLimitValidateAction)

        # 注册范围校验器
        from agent_runtime.extension.workflow_node.rails.validators.range_validator import (
            NumberRangeValidateAction,
        )

        self.register("number_range_validate", NumberRangeValidateAction)

        # 注册通用格式校验器
        from agent_runtime.extension.workflow_node.rails.validators.format_validator import (
            CommonDataFormatCheckAction,
        )

        self.register("common_data_format_check", CommonDataFormatCheckAction)

        # 注册日期时间格式校验器
        from agent_runtime.extension.workflow_node.rails.formatters.datetime_formatter import (
            DateTimeFormatValidateAction,
        )

        self.register("date_time_format", DateTimeFormatValidateAction)

        # 注册时间解析器
        from agent_runtime.extension.workflow_node.rails.validators.parse_time_validator import (
            TimeParseAction,
        )

        self.register("parse_time", TimeParseAction)

    def register(self, name: str, action_class: Type[RailsAction]) -> None:
        """注册 action"""
        self._actions[name] = action_class

    def get(self, name: str) -> Optional[Type[RailsAction]]:
        """获取 action 类"""
        return self._actions.get(name)

    def create_action(
        self, name: str, action_extra_args: Dict[str, Any] = None
    ) -> Optional[RailsAction]:
        """创建 action 实例"""
        action_class = self.get(name)
        if action_class is None:
            return None
        config = ActionConfig(action_extra_args)
        return action_class(config)

    def get_action_names(self) -> list:
        """获取所有已注册的 action 名称"""
        return list(self._actions.keys())


async def execute_rails(
    rails_config: Dict[str, Any], context: Dict[str, Any]
) -> Dict[str, Any]:
    """执行 rails 配置中的所有 action

    Args:
        rails_config: rails 配置，包含 actions_config 和 rails 定义
        context: 执行上下文

    Returns:
        处理后的参数
    """
    if not rails_config:
        return context.get("arguments", {})

    registry = RailsRegistry.get_instance()
    actions_config = rails_config.get("actions_config", [])
    execution_rails = rails_config.get("rails", {}).get("execution", [])

    # 构建 action_extra_args 映射
    action_extra_args_map = {}
    for action_config in actions_config:
        action_name = action_config.get("action")
        action_extra_args_map[action_name] = action_config.get("action_extra_args", {})

    # 按照 rails.execution 顺序执行
    args = context.get("arguments", {}).copy()
    workflow_logger.debug(f"[execute_rails] context keys: {list(context.keys())}")
    workflow_logger.debug(
        f"[execute_rails] user_input: {context.get('user_input', 'NOT_FOUND')}"
    )
    workflow_logger.debug(f"[execute_rails] execution_rails: {execution_rails}")
    workflow_logger.debug(
        f"[execute_rails] action_extra_args_map: {action_extra_args_map}"
    )
    for rail in execution_rails:
        action_name = rail.get("action")
        action_extra_args = action_extra_args_map.get(action_name, {})

        action_instance = registry.create_action(action_name, action_extra_args)
        if action_instance is None:
            continue

        # 构建当前 action 的执行上下文
        action_context = {
            "arguments": args,
            "inputs": context.get("inputs", {}),
            "outputs": context.get("outputs", []),
            "extracted_args": context.get("extracted_args", {}),
            "dialogue_history": context.get("dialogue_history", []),
            "component_config": context.get("component_config", {}),
            "runtime_context": context.get("runtime_context", {}),
            "user_input": context.get("user_input", ""),
        }

        result = await action_instance.async_execute(action_context)
        args = result.get("arguments", args)

    return args
