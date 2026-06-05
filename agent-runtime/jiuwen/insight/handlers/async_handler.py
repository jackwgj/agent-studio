#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""the main insight"""

from typing import Optional

from jiuwen.insight.async_client import Client
from jiuwen.insight.handlers.data import BaseDataHandler, InvokeData
from jiuwen.insight.utils import format_invoke_data

_CLIENT: Optional[Client] = None


def get_client() -> Client:
    """Get the client."""
    global _CLIENT
    if _CLIENT is None:
        _CLIENT = Client()
    return _CLIENT


class InsightHandler(BaseDataHandler):
    """Post to the insight endpoint"""

    def __init__(self, client: Optional[Client] = None):
        super().__init__()
        self.client = client or get_client()

    def _send_invoke_data(self, invoke: InvokeData) -> None:
        """Send a snapshot of invoke (deep copy) so later mutation does not affect sent data; format and post."""
        invoke_dict = format_invoke_data(invoke)
        self.client.send_invoke(invoke_dict)

    def _on_prompt_start(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_prompt_end(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_prompt_error(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_llm_start(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_llm_end(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_llm_error(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_plugin_start(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_plugin_end(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_plugin_error(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_chain_start(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_chain_end(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_chain_error(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_retriever_start(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_retriever_end(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_retriever_error(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_evaluator_start(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_evaluator_end(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)

    def _on_evaluator_error(self, invoke: InvokeData) -> None:
        self._send_invoke_data(invoke)
