#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
from abc import abstractmethod
from typing import List, Union, Any

from jiuwen.common.exception import JiuWenException
from jiuwen.orchestration.utils import Input, Output


class BaseCallbackHandler:
    """
    回调处理器基类。

    定义了一系列回调方法，用于在组件生命周期的不同阶段执行自定义逻辑。
    """

    blacklist: List[str] = []
    """黑名单，存储不允许的处理器类型。"""

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

    @abstractmethod
    def on_pre_init(self, inputs: Input, *args: Any, **kwargs: Any) -> Any:
        """
        在组件初始化前调用。

        抽象方法，子类需实现此方法以定义在组件初始化前的行为。

        Args:
            inputs (Input): 输入数据。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """

    @abstractmethod
    async def on_pre_invoke(self, inputs: Input, *args: Any, **kwargs: Any) -> Any:
        """
        在组件调用前异步调用。

        抽象方法，子类需实现此方法以定义在组件调用前的异步行为。

        Args:
            inputs (Input): 输入数据。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """

    @abstractmethod
    def on_pre_destroy(self, inputs: Input, *args: Any, **kwargs: Any) -> Any:
        """
        在组件销毁前调用。

        抽象方法，子类需实现此方法以定义在组件销毁前的行为。

        Args:
            inputs (Input): 输入数据。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """

    @abstractmethod
    def on_post_init(self, outputs: Output, *args: Any, **kwargs: Any) -> Any:
        """
        在组件初始化后调用。

        抽象方法，子类需实现此方法以定义在组件初始化后的行为。

        Args:
            outputs (Output): 输出数据。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """

    @abstractmethod
    async def on_post_invoke(self, outputs: Output, *args: Any, **kwargs: Any) -> Any:
        """
        在组件调用后异步调用。

        抽象方法，子类需实现此方法以定义在组件调用后的异步行为。

        Args:
            outputs (Output): 输出数据。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """

    @abstractmethod
    def on_post_destroy(self, outputs: Output, *args: Any, **kwargs: Any) -> Any:
        """
        在组件销毁后调用。

        抽象方法，子类需实现此方法以定义在组件销毁后的行为。

        Args:
            outputs (Output): 输出数据。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """

    @abstractmethod
    def on_init(
        self, data: Union[JiuWenException, dict], *args: Any, **kwargs: Any
    ) -> Any:
        """
        在组件初始化时调用。

        抽象方法，子类需实现此方法以定义在组件初始化时的行为。

        Args:
            data (Union[JiuWenException, dict]): 初始化数据，可以是异常对象或字典。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """

    @abstractmethod
    async def on_invoke(
        self, data: Union[JiuWenException, dict], *args: Any, **kwargs: Any
    ) -> Any:
        """
        在组件调用时异步调用。

        抽象方法，子类需实现此方法以定义在组件调用时的异步行为。

        Args:
            data (Union[JiuWenException, dict]): 调用数据，可以是异常对象或字典。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """

    @abstractmethod
    def on_destroy(
        self, data: Union[JiuWenException, dict], *args: Any, **kwargs: Any
    ) -> Any:
        """
        在组件销毁时调用。

        抽象方法，子类需实现此方法以定义在组件销毁时的行为。

        Args:
            data (Union[JiuWenException, dict]): 销毁数据，可以是异常对象或字典。
            *args (Any): 可变参数。
            **kwargs (Any): 关键字参数。

        Returns:
            Any: 返回值类型，具体类型由子类实现决定。
        """
