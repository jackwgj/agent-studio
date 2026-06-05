#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""base trace handler"""

from abc import abstractmethod
from typing import Any


class PromptTraceHandler:
    """Prompt trace handler"""

    @abstractmethod
    def on_prompt_start(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a prompt starts."""
        ...

    @abstractmethod
    def on_prompt_end(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a prompt ends."""
        ...

    @abstractmethod
    def on_prompt_error(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a prompt error occurs."""
        ...


class LLMTraceHandler:
    """LLM trace handler"""

    @abstractmethod
    def on_llm_start(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a llm starts."""
        ...

    @abstractmethod
    def on_llm_end(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a llm ends."""
        ...

    @abstractmethod
    def on_llm_error(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a llm error occurs."""
        ...


class PluginTraceHandler:
    """Plugin trace handler"""

    @abstractmethod
    def on_plugin_start(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a plugin starts."""
        ...

    @abstractmethod
    def on_plugin_end(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a plugin ends."""
        ...

    @abstractmethod
    def on_plugin_error(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a plugin error occurs."""
        ...


class ChainTraceHandler:
    """Chain trace handler"""

    @abstractmethod
    def on_chain_start(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a chain starts."""
        ...

    @abstractmethod
    def on_chain_end(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a chain ends."""
        ...

    @abstractmethod
    def on_chain_error(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a chain error occurs."""
        ...


class RetrieverTraceHandler:
    """Retriever trace handler"""

    @abstractmethod
    def on_retriever_start(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a retriever starts."""
        ...

    @abstractmethod
    def on_retriever_end(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a retriever ends."""
        ...

    @abstractmethod
    def on_retriever_error(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when a retriever error occurs."""
        ...


class EvaluatorTraceHandler:
    """Evaluator trace handler"""

    @abstractmethod
    def on_evaluator_start(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when an evaluator starts."""
        ...

    @abstractmethod
    def on_evaluator_end(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when an evaluator ends."""
        ...

    @abstractmethod
    def on_evaluator_error(self, *args: Any, **kwargs: Any) -> None:
        """Abstract method for operations to be executed when an evaluator error occurs."""
        ...
