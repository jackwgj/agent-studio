#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import importlib.util
import inspect
import os
from typing import Dict, Type

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.orchestration.flow.components.flow_agent.strategies.base import (
    AgentStrategy,
)

BUILTIN_STRATEGIES_PATH = "jiuwen.orchestration.flow.components.flow_agent.strategies"
PRESET_PROVIDER = "JiuWen"


def get_custom_strategy_path() -> str:
    """get_custom_strategy_path"""
    # 从环境变量读取自定义策略路径
    strategy_file_path = os.getenv("JIUWEN_CUSTOM_STRATEGY_PATH")
    if not strategy_file_path:
        return ""
    # 策略路径不存在
    if not os.path.exists(strategy_file_path):
        raise JiuWenBaseException(
            error_code=StatusCode.STRATEGY_FILE_NOT_EXIST_ERROR.code,
            message=StatusCode.STRATEGY_FILE_NOT_EXIST_ERROR.errmsg,
        )
    # 策略路径不是文件路径
    if not os.path.isfile(strategy_file_path):
        raise JiuWenBaseException(
            error_code=StatusCode.STRATEGY_FILE_NOT_FILEPATH_ERROR.code,
            message=StatusCode.STRATEGY_FILE_NOT_FILEPATH_ERROR.errmsg,
        )

    return strategy_file_path


class AgentStrategyRegistry:
    """Agent策略注册中心 - 单例模式"""

    instance = None
    strategies: Dict[
        str, Type[AgentStrategy]
    ] = {}  # str: provider+strategy_name做拼接为唯一key
    _initialized = False

    def __new__(cls):
        if cls.instance is None:
            cls.instance = super().__new__(cls)
        return cls.instance

    @classmethod
    def get_strategy(cls, provider: str, strategy_name: str) -> Type[AgentStrategy]:
        """根据provider和strategy_name获取策略类"""
        if cls.instance is None:
            raise JiuWenBaseException(
                error_code=StatusCode.STRATEGY_REGISTRY_NOT_INIT_ERROR.code,
                message=StatusCode.STRATEGY_REGISTRY_NOT_INIT_ERROR.errmsg,
            )

        strategy_key = f"{provider}_{strategy_name}"
        strategy_class = cls.instance.strategies.get(strategy_key)
        if not strategy_class:
            raise JiuWenBaseException(
                error_code=StatusCode.STRATEGY_NOT_FOUND_ERROR.code,
                message=StatusCode.STRATEGY_NOT_FOUND_ERROR.errmsg.format(
                    strategy_name=strategy_name, provider=provider
                ),
            )
        return strategy_class

    @classmethod
    def get_all_strategies(cls) -> Dict[str, Type[AgentStrategy]]:
        """获取所有注册的策略"""
        if cls.instance is None:
            return {}
        return cls.instance.strategies.copy()

    @classmethod
    def list_strategies_by_provider(
        cls, provider: str
    ) -> Dict[str, Type[AgentStrategy]]:
        """根据provider获取策略列表"""
        if cls.instance is None:
            return {}

        return {
            key: strategy
            for key, strategy in cls.instance.strategies.items()
            if key.startswith(f"{provider}_")
        }

    def initialize(self, builtin_strategy_path: str = BUILTIN_STRATEGIES_PATH):
        """
        初始化策略注册中心
        :param builtin_strategy_path: 预置策略路径
        """
        if self._initialized:
            return

        self.strategies.clear()

        # 加载预置策略
        self._load_strategies_from_path(builtin_strategy_path, PRESET_PROVIDER)

        # 从环境变量读取自定义策略路径
        custom_path = get_custom_strategy_path()
        # 自定义策略默认使用 "Custom" 作为 provider
        self._load_custom_strategies_from_path(custom_path, "Custom")

        self._initialized = True

    def _load_custom_strategies_from_path(
        self, custom_path: str, strategy_provider: str
    ):
        """
        加载指定路径下的自定义策略（文件绝对路径）
        :param custom_path: 自定义策略文件绝对路径（环境变量配置）
        :param strategy_provider: 策略提供方
        """
        if not custom_path or not strategy_provider:
            return

        # 安全校验,必须是绝对路径
        if not os.path.isabs(custom_path):
            logger.error(
                f"Custom strategy path must be absolute: {custom_path}",
                simple_log="Custom strategy path must be absolute",
            )
            return

        filename = os.path.basename(custom_path)
        if (
            filename.endswith((".py", ".pyc"))
            and not filename.startswith("_")
            and filename != "base.py"
        ):
            self._load_strategies_from_file(custom_path, strategy_provider)

    def _load_strategies_from_path(
        self, builtin_strategy_path: str, strategy_provider: str
    ):
        """
        加载指定路径下的预置策略（模块路径）
        :param builtin_strategy_path: 预置策略文件模块路径
        :param strategy_provider: 预置策略提供方
        """
        try:
            spec = importlib.util.find_spec(builtin_strategy_path)
            if not spec or not spec.origin:
                # jiu wen base exception
                raise JiuWenBaseException(
                    error_code=StatusCode.BUILTIN_STRATEGY_PATH_NOT_FOUND_ERROR.code,
                    message=StatusCode.BUILTIN_STRATEGY_PATH_NOT_FOUND_ERROR.errmsg,
                )

            # __init__.py的实际路径
            module_path = spec.origin
            # 返回__init__.py所在的目录路径
            resolved_path = os.path.dirname(module_path)
            dir_abs = os.path.abspath(resolved_path)

            if not os.path.isdir(dir_abs):
                raise JiuWenBaseException(
                    error_code=StatusCode.BUILTIN_STRATEGY_PATH_NOT_FOUND_ERROR.code,
                    message=StatusCode.BUILTIN_STRATEGY_PATH_NOT_FOUND_ERROR.errmsg,
                )

            for filename in os.listdir(dir_abs):
                if (
                    filename.endswith((".py", ".pyc"))
                    and not filename.startswith("_")
                    and filename != "base.py"
                ):
                    file_path = os.path.join(dir_abs, filename)
                    self._load_strategies_from_file(file_path, strategy_provider)

        except Exception as e:
            logger.error(
                f"Failed to load strategies from path {builtin_strategy_path}: {e}",
                simple_log="Failed to load strategies from path",
            )
            raise

    def _load_strategies_from_file(self, file_path: str, provider: str):
        """从文件加载策略"""
        # 取模块名（去掉.py扩展名）
        module_name = os.path.basename(file_path)
        if module_name.endswith(".py"):
            module_name = module_name[:-3]
        elif module_name.endswith(".pyc"):
            module_name = module_name[:-4]

        try:
            spec = importlib.util.spec_from_file_location(module_name, file_path)
            if not spec or not spec.loader:
                logger.error(
                    f"Failed to create spec for {file_path}",
                    simple_log="Failed to create spec for module",
                )
                return

            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)
            # 筛选并注册有效策略类
            for attr_name in dir(module):
                attr = getattr(module, attr_name)

                # 检查是否为类且继承自AgentStrategy
                if (
                    inspect.isclass(attr)
                    and issubclass(attr, AgentStrategy)
                    and attr is not AgentStrategy
                ):  # 排除AgentStrategy基类本身
                    try:
                        # 创建策略实例
                        strategy_instance = attr()
                        strategy_name = strategy_instance.get_strategy_name()

                        # 注册策略（假设有一个注册方法）
                        self._register_strategy(strategy_name, attr)
                        logger.info(
                            f"Successfully loaded strategy: {strategy_name} from {file_path}",
                            simple_log=f"Successfully loaded strategy: {strategy_name}",
                        )

                    except Exception as e:
                        logger.error(
                            f"Failed to instantiate strategy class {attr_name}: {e}",
                            simple_log="Failed to instantiate strategy class",
                        )

        except Exception as e:
            logger.error(
                f"Failed to load strategies from file {file_path}: {e}",
                simple_log="Failed to load",
            )

    def _register_strategy(
        self, strategy_key: str, strategy_class: Type[AgentStrategy]
    ) -> None:
        """注册单个策略"""
        # 策略已存在
        if strategy_key in self.strategies:
            logger.warning(
                f"The strategy name '{strategy_key}' already exists, skipping duplicate registration."
            )
            return

        self.strategies[strategy_key] = strategy_class
        logger.info(f"Successfully registered strategy: {strategy_key}")
