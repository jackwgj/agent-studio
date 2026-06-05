#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""the main insight"""

import weakref
from concurrent.futures import Future, ThreadPoolExecutor
from typing import Optional, Callable

from jiuwen.insight.client import Client
from jiuwen.insight.handlers.data import BaseDataHandler, InvokeData
from jiuwen.insight.utils import format_invoke_data

_CLIENT: Optional[Client] = None
_EXECUTOR: Optional[ThreadPoolExecutor] = None


def get_client() -> Client:
    """Get the client."""
    global _CLIENT
    if _CLIENT is None:
        _CLIENT = Client()
    return _CLIENT


def _get_executor() -> ThreadPoolExecutor:
    """Get the executor."""
    global _EXECUTOR
    if _EXECUTOR is None:
        _EXECUTOR = ThreadPoolExecutor()
    return _EXECUTOR


def _copy(invoke: InvokeData) -> InvokeData:
    """Copy a invoke"""
    try:
        return invoke.copy(deep=True)
    except TypeError:
        return invoke.copy()


class InsightHandler(BaseDataHandler):
    """Post to the insight endpoint"""

    def __init__(
        self,
        client: Optional[Client] = None,
        use_threading: bool = True,
    ):
        super().__init__()
        self.client = client or get_client()
        self._futures: weakref.WeakSet[Future] = weakref.WeakSet()
        self.executor = _get_executor() if use_threading else None

    def _send_invoke_data(self, invoke: InvokeData) -> None:
        invoke_dict = format_invoke_data(invoke)
        self.client.send_invoke(invoke_dict)

    def _submit(
        self, function: Callable[[InvokeData], None], invoke: InvokeData
    ) -> None:
        """Submit a function to the executor"""
        if self.executor is None:
            function(invoke)
        else:
            self._futures.add(self.executor.submit(function, invoke))

    def _on_prompt_start(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_prompt_end(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_prompt_error(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_llm_start(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_llm_end(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_llm_error(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_plugin_start(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_plugin_end(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_plugin_error(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_chain_start(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_chain_end(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_chain_error(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_retriever_start(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_retriever_end(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_retriever_error(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_evaluator_start(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_evaluator_end(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))

    def _on_evaluator_error(self, invoke: InvokeData) -> None:
        self._submit(self._send_invoke_data, _copy(invoke))
