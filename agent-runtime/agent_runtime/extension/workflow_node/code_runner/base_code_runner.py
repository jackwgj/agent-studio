"""代码执行器统一接口"""

from abc import ABC, abstractmethod


class CodeRunner(ABC):
    """代码执行器统一接口

    所有代码执行器（本地、沙箱、Wasm等）都实现此接口。
    上层调用方只需要知道 run() 方法，不关心底层实现。
    """

    @abstractmethod
    async def run(self, user_code: str, inputs: dict, timeout: int = 300) -> dict:
        """
        执行用户代码并返回结果

        Args:
            user_code: 用户源代码（包含 main 函数定义）
            inputs: 输入参数字典，传递给 main 函数
            timeout: 超时时间（秒）

        Returns:
            dict: main 函数的返回值

        Raises:
            Exception: 执行失败或返回值非法
        """
        pass
