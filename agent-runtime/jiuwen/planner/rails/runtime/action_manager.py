# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""action manager"""

import copy
import inspect
import threading
from dataclasses import dataclass, field
from typing import Dict, Callable, List, Optional

from jiuwen.common.log.base import logger
from jiuwen.planner.rails.config import RailsConfig
from jiuwen.planner.rails.schema.action import Action, ActionMetadata


class BaseActionManager:
    """base of action manager"""

    def __init__(self, rails_config: RailsConfig):
        self.action_map: Dict[str, Action] = dict()
        self._load_actions(rails_config)

    def get_action(self, action_name: str):
        """get action"""
        return self.action_map.get(action_name)

    def _load_actions(self, rails_config: RailsConfig):
        """load action from user defined directory and default directory"""
        pass


class RuntimeActionManager(BaseActionManager):
    def __init__(self, rails_config: RailsConfig):
        super().__init__(rails_config)

    def _load_actions(self, rails_config: RailsConfig):
        """load action from ActionManagerSingleton"""
        actions = copy.deepcopy(rails_config.actions)
        for action_name, action_config in actions.items():
            callable_action_schema = ActionManagerSingleton.get_callable_action_schema(
                action_name
            )
            action = Action(
                action_metadata=ActionMetadata(name=action_name, config=action_config),
                callable_action=callable_action_schema.action_callable,
                extra_input_keywords=callable_action_schema.extra_input_keys,
            )
            self.action_map.update({action_name: action})


@dataclass
class CallableActionSchema:
    action_callable: Callable
    extra_input_keys: List[str] = None
    is_async: bool = field(init=False)  # 自动计算的字段，不在初始化时设置

    def __post_init__(self):
        """在初始化后自动检测函数是否为异步"""
        self.is_async = inspect.iscoroutinefunction(self.action_callable)
        if self.extra_input_keys is None:
            self.extra_input_keys = []


class ActionManagerSingleton:
    """singleton of action manager - 支持异步"""

    _instance = None
    _lock = threading.Lock()
    _action_map: Dict[str, CallableActionSchema] = dict()

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            with cls._lock:
                if not cls._instance:
                    cls._instance = super(ActionManagerSingleton, cls).__new__(
                        cls, *args, **kwargs
                    )
        return cls._instance

    @classmethod
    def register_action(
        cls,
        action_name: str,
        action_callable: Callable,
        extra_input_keys: List[str] = None,
    ):
        """
        register action when initialization rails
        - action_name: str,
        - action_callable: Callable,
        - extra_input_keys: List[str]: extra input parameters' key, except context and config
        """
        with cls._lock:
            action_schema = CallableActionSchema(
                action_callable=action_callable, extra_input_keys=extra_input_keys
            )
            cls._action_map.update({action_name: action_schema})

            async_info = "async" if action_schema.is_async else "sync"
            logger.info(
                f"Success to register {async_info} action [{action_name}]",
                simple_log="Success to register",
            )

    @classmethod
    def get_action(cls, action_name: str) -> Optional[Callable]:
        """获取 action 函数"""
        action_schema = cls._action_map.get(action_name)
        return action_schema.action_callable if action_schema else None

    @classmethod
    def get_action_schema(cls, action_name: str) -> Optional[CallableActionSchema]:
        """获取 action schema"""
        return cls._action_map.get(action_name)

    @classmethod
    def is_action_async(cls, action_name: str) -> bool:
        """判断 action 是否为异步"""
        action_schema = cls._action_map.get(action_name)
        return action_schema.is_async if action_schema else False

    @classmethod
    def get_all_actions(cls) -> Dict[str, CallableActionSchema]:
        """获取所有 actions"""
        return cls._action_map.copy()

    @classmethod
    def get_callable_action_schema(cls, action_name: str) -> CallableActionSchema:
        """get_callable_action_schema"""
        with cls._lock:
            result = cls._action_map.get(action_name)
            if result is None:
                raise ValueError(f"Action [{action_name}] is not registered in advance")
            return result
