#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
import asyncio
import os
from typing import List, Optional

from jiuwen.common.configs.env_constants import WORKFLOW_HANDLER_ENABLE_INSIGHT_KEY
from jiuwen.common.log.base import logger
from jiuwen.insight.handlers.insight_callback_handler import Insight4CallbackHandler
from jiuwen.orchestration.callbacks.base import BaseCallbackHandler
from starlette.datastructures import Headers


class CallbackHandlerManager:
    """
    回调处理器管理器类。

    该类用于管理回调处理器，提供单例模式以确保只有一个实例，并允许注册和管理回调处理器。
    """

    _instance = None
    blacklist: List[str] = []
    """黑名单，存储不允许的处理器类型。"""

    def __new__(cls, *args, **kwargs):
        """
        实现单例模式。

        如果实例尚未创建，则创建一个新的CallbackHandlerManager实例；否则返回已有的实例。

        Args:
            *args: 可变参数。
            **kwargs: 关键字参数。

        Returns:
            CallbackHandlerManager: 返回CallbackHandlerManager的实例。
        """
        if cls._instance is None:
            cls._instance = super(CallbackHandlerManager, cls).__new__(cls)
        return cls._instance

    def __init__(
        self,
        handlers: Optional[List[BaseCallbackHandler]] = None,
        blacklist: Optional[List[str]] = None,
    ) -> None:
        """
        初始化CallbackHandlerManager实例。

        如果实例已初始化，则直接返回；否则，初始化处理器列表和黑名单。

        Args:
            handlers (Optional[List[BaseCallbackHandler]]): 回调处理器列表，默认为空。
            blacklist (Optional[List[str]]): 黑名单，默认为空。

        Returns:
            None
        """
        if hasattr(self, "initialized") and self.initialized:
            return
        self.handlers = handlers if handlers else []
        self.blacklist = blacklist if blacklist else []
        self.initialized = True
        self._register_default_handler()

    @classmethod
    def set_blacklist(cls, blacklist: List[str]):
        """
        设置黑名单。

        类方法，用于设置不允许的处理器类型。

        Args:
            blacklist (List[str]): 黑名单列表。

        Returns:
            None
        """
        cls.blacklist = blacklist

    def add_handler(self, handler: BaseCallbackHandler) -> None:
        """
        添加回调处理器。

        将新的处理器添加到处理器列表中，确保列表中不会出现相同类型的处理器。

        Args:
            handler (BaseCallbackHandler): 要添加的回调处理器。

        Returns:
            None
        """
        for item in self.handlers:
            if isinstance(item, type(handler)):
                return
        self.handlers.append(handler)

    def get_handler(self, handler_type):
        """

        :param handler_type:
        :return:
        """
        for handler in self.handlers:
            if isinstance(handler, handler_type):
                return handler
        return None

    def remove_handler(self, handler: BaseCallbackHandler) -> None:
        """

        Args:
            handler:

        Returns:

        """
        self.handlers.remove(handler)

    def set_handlers(self, handlers: List[BaseCallbackHandler]) -> None:
        """

        Args:
            handlers:

        Returns:

        """
        self.handlers = handlers

    async def trigger_event(self, event_name, *args, **kwargs):
        """

        Args:
            event_name:
            *args:
            **kwargs:

        Returns:

        """

        def get_invoke_mode(kwargs):
            runtime_context = kwargs.get("runtime_context", {})
            if runtime_context is None:
                return ""
            headers_name = runtime_context.get(
                "workflow_execute_requests_headers_name", {}
            )
            if not isinstance(headers_name, dict):
                return ""
            header = headers_name.get("cust_headers", {})
            if not isinstance(header, (dict, Headers)):
                return ""
            x_invoke_mode = (
                header.get("X-Invoke-Mode") or header.get("x-invoke-mode") or ""
            )
            if not isinstance(x_invoke_mode, str):
                return ""
            return x_invoke_mode.lower()

        invoke_mode = get_invoke_mode(kwargs)
        component_type = kwargs.get("component_type")

        async def execute_handler(handler):
            if isinstance(handler, Insight4CallbackHandler) and invoke_mode != "debug":
                logger.debug(
                    "execute_handler, X-Invoke-Mode is not debug, no insight info return"
                )
                return
            if hasattr(handler, event_name):
                method = getattr(handler, event_name)
                if callable(method):
                    effective_blacklist = (
                        handler.blacklist
                        if handler.blacklist is not None
                        else self.blacklist
                    )
                    if component_type in effective_blacklist:
                        logger.info(
                            f"Component type '{component_type}' is in the effective blacklist. Skipping {event_name}."
                        )
                        return
                    if asyncio.iscoroutinefunction(method):
                        await method(*args, **kwargs)
                    else:
                        method(*args, **kwargs)

        if isinstance(self.handlers, list) and len(self.handlers) != 0:
            for handler in self.handlers:
                await execute_handler(handler)

    def _register_default_handler(self):

        # 注册 insight handler
        if str(os.environ.get(WORKFLOW_HANDLER_ENABLE_INSIGHT_KEY)).lower() == "true":
            self.add_handler(Insight4CallbackHandler())
