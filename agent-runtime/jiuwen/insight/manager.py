#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""handler send trace log to Insight backend"""

import uuid
from typing import Any, List, Type, Optional, Union

from jiuwen.common.log.base import logger
from jiuwen.insight.handlers.base import (
    PromptTraceHandler,
    LLMTraceHandler,
    PluginTraceHandler,
    ChainTraceHandler,
    RetrieverTraceHandler,
    EvaluatorTraceHandler,
)
from jiuwen.insight.handlers.data import BaseDataHandler

Trace = Optional[Union[List[BaseDataHandler], "TraceManager"]]


def _trace_event(
    trace_handlers: List[BaseDataHandler], event_name: str, *args: Any, **kwargs: Any
):
    """Invoke trace event"""
    logger.info(f"insight trace type:{event_name}, invoke_id:{kwargs.get('invoke_id')}")
    for trace_handler in trace_handlers:
        getattr(trace_handler, event_name)(*args, **kwargs)


class TraceManager(
    PromptTraceHandler,
    LLMTraceHandler,
    PluginTraceHandler,
    ChainTraceHandler,
    RetrieverTraceHandler,
    EvaluatorTraceHandler,
):
    """Tracer manager that manage trace handlers"""

    def __init__(
        self,
        trace_handlers: Optional[List[BaseDataHandler]] = None,
        parent_invoke_id: Optional[str] = None,
        instance_info: Optional[dict] = None,
        **kwargs,
    ) -> None:
        """Initialize"""
        self.trace_handlers = trace_handlers or []
        self.invoke_id = None
        self.parent_invoke_id = parent_invoke_id
        self.instance_info = instance_info
        self.extra = kwargs

    @classmethod
    def generate_manager(
        cls,
        trace_handlers: Optional["TraceManager"] = None,
        instance_info: Optional[dict] = None,
    ) -> "TraceManager":
        """Return a trace handlers manager"""
        return _generate_manager(cls, trace_handlers, instance_info)

    def add_trace_handler(self, trace_handler: BaseDataHandler) -> None:
        """Add a trace handler to the manager"""
        trace_handlers_type = [
            type(inheritable_trace_handler)
            for inheritable_trace_handler in self.trace_handlers
        ]
        if type(trace_handler) not in trace_handlers_type:
            self.trace_handlers.append(trace_handler)

    def remove_trace_handler(self, trace_handler: BaseDataHandler) -> None:
        """Remove a trace handler from the manager"""
        self.trace_handlers.remove(trace_handler)

    def on_prompt_start(self, inputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when prompt start"""
        self.get_run_id()
        _trace_event(
            self.trace_handlers,
            "on_prompt_start",
            self.instance_info,
            inputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **self.extra,
            **kwargs,
        )

    def on_prompt_end(self, outputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when prompt end"""
        _trace_event(
            self.trace_handlers,
            "on_prompt_end",
            outputs,
            invoke_id=self.invoke_id,
            partent_run_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_prompt_error(self, error: BaseException, *args: Any, **kwargs: Any) -> None:
        """Invoke when prompt error"""
        _trace_event(
            self.trace_handlers,
            "on_prompt_error",
            error,
            invoke_id=self.invoke_id,
            partent_run_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_llm_start(self, inputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when llm start"""
        self.get_run_id()
        _trace_event(
            self.trace_handlers,
            "on_llm_start",
            self.instance_info,
            inputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **self.extra,
            **kwargs,
        )

    def on_llm_end(self, outputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when llm end"""
        _trace_event(
            self.trace_handlers,
            "on_llm_end",
            outputs,
            invoke_id=self.invoke_id,
            partent_run_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_llm_error(self, error: BaseException, *args: Any, **kwargs: Any) -> None:
        """Invoke when llm error"""
        _trace_event(
            self.trace_handlers,
            "on_llm_error",
            error,
            invoke_id=self.invoke_id,
            partent_run_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_chain_start(self, inputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when chain start"""
        self.get_run_id()
        _trace_event(
            self.trace_handlers,
            "on_chain_start",
            self.instance_info,
            inputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **self.extra,
            **kwargs,
        )

    def on_chain_end(self, outputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when chain start"""
        _trace_event(
            self.trace_handlers,
            "on_chain_end",
            outputs,
            invoke_id=self.invoke_id,
            partent_run_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_chain_error(self, error: BaseException, *args: Any, **kwargs: Any) -> None:
        """Invoke when chain error"""
        _trace_event(
            self.trace_handlers,
            "on_chain_error",
            error,
            invoke_id=self.invoke_id,
            partent_run_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_plugin_start(self, inputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when tool start"""
        self.get_run_id()
        _trace_event(
            self.trace_handlers,
            "on_plugin_start",
            self.instance_info,
            inputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **self.extra,
            **kwargs,
        )

    def on_plugin_end(self, outputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when tool end"""
        _trace_event(
            self.trace_handlers,
            "on_plugin_end",
            outputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_plugin_error(self, error: BaseException, *args: Any, **kwargs: Any) -> None:
        """Invoke when tool error"""
        _trace_event(
            self.trace_handlers,
            "on_plugin_error",
            error,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_retriever_start(self, inputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when retriever start"""
        self.get_run_id()
        _trace_event(
            self.trace_handlers,
            "on_retriever_start",
            self.instance_info,
            inputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **self.extra,
            **kwargs,
        )

    def on_retriever_end(self, outputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when retriever end"""
        _trace_event(
            self.trace_handlers,
            "on_retriever_end",
            outputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_retriever_error(
        self, error: BaseException, *args: Any, **kwargs: Any
    ) -> None:
        """Invoke when retriever error"""
        _trace_event(
            self.trace_handlers,
            "on_retriever_error",
            error,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_evaluator_start(self, inputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when evaluator start"""
        self.get_run_id()
        _trace_event(
            self.trace_handlers,
            "on_evaluator_start",
            self.instance_info,
            inputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **self.extra,
            **kwargs,
        )

    def on_evaluator_end(self, outputs: Any, *args: Any, **kwargs: Any) -> None:
        """Invoke when evaluator end"""
        _trace_event(
            self.trace_handlers,
            "on_evaluator_end",
            outputs,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **kwargs,
        )

    def on_evaluator_error(
        self, error: BaseException, *args: Any, **kwargs: Any
    ) -> None:
        """Invoke when evaluator error"""
        _trace_event(
            self.trace_handlers,
            "on_evaluator_error",
            error,
            invoke_id=self.invoke_id,
            parent_invoke_id=self.parent_invoke_id,
            **kwargs,
        )

    def get_run_id(self) -> None:
        """
        获取运行ID的方法。
        如果当前的invoke_id为None，则生成一个新的UUID作为invoke_id。

        :return: None
        """
        if self.invoke_id is None:
            self.invoke_id = str(uuid.uuid4())


def remove_duplicate_trace_handlers(
    trace_handlers: Optional[List[Trace]],
) -> List[Trace]:
    """Remove duplicate trace handlers which have the same type."""
    if trace_handlers is None:
        return []

    processed_trace_handlers = []
    processed_trace_handlers_type = []
    for trace_handler in trace_handlers:
        if type(trace_handler) not in processed_trace_handlers_type:
            processed_trace_handlers_type.append(type(trace_handler))
            processed_trace_handlers.append(trace_handler)

    return processed_trace_handlers


def _generate_manager(
    trace_manager_cls: Type[TraceManager],
    trace_handlers: Trace = None,
    instance_info: Optional[dict] = None,
) -> TraceManager:
    """Return a trace manager"""
    trace_manager = trace_manager_cls(trace_handlers=[], instance_info=instance_info)
    if trace_handlers:
        if isinstance(trace_handlers, list) or trace_handlers is None:
            trace_handlers_ = remove_duplicate_trace_handlers(trace_handlers)
            trace_manager.trace_handlers = trace_handlers_.copy()
        else:
            trace_manager.trace_handlers = trace_handlers.trace_handlers.copy()
            trace_manager.parent_invoke_id = trace_handlers.parent_invoke_id
            trace_manager.extra = trace_handlers.extra
    return trace_manager


def get_child_manager(parent_manager: TraceManager, name: str = None) -> TraceManager:
    """Get a child trace handlers manager"""
    manager = TraceManager(
        trace_handlers=[], parent_invoke_id=parent_manager.invoke_id, name=name
    )
    for trace_handler in parent_manager.trace_handlers:
        manager.add_trace_handler(trace_handler)

    return manager
