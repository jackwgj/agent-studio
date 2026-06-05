#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""Base class definition of the orchestration module."""

import asyncio
import inspect
import json
from abc import ABC, abstractmethod
from concurrent.futures import ThreadPoolExecutor
from copy import deepcopy
from dataclasses import field, dataclass
from typing import (
    Generic,
    Optional,
    Union,
    TypeVar,
    Any,
    List,
    cast,
    TYPE_CHECKING,
    Callable,
    Mapping,
    Awaitable,
    Iterator,
    AsyncIterator,
    Dict,
)

from jiuwen.common.exception.base import InterruptException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import set_thread_session, get_thread_session
from jiuwen.executor_sdk.operator.impl.common.runtime_context_impl import (
    RuntimeContextImpl,
)
from jiuwen.insight.manager import TraceManager, get_child_manager
from jiuwen.insight.utils import get_instance_info
from jiuwen.orchestration.callbacks.constants import TriggerEventName
from jiuwen.orchestration.callbacks.manager import CallbackHandlerManager
from jiuwen.orchestration.invokable.dependency import (
    executor as executor_only,
    InvokableBase,
    invokable_local,
)
from jiuwen.orchestration.utils import Input, Output

if TYPE_CHECKING:
    from jiuwen.memory.conversation import ConversationMemory

Other = TypeVar("Other")


def _get_function_param_num(func):
    sig = inspect.signature(func)
    params = sig.parameters
    return len(params)


class ExceptionWrapper(Exception):
    """
    Used to add additional exception message.
    """

    def __init__(self, original_exception, additional_info):
        super().__init__(str(original_exception))
        self.original_exception = original_exception
        self.additional_info = additional_info

    def __str__(self):
        return f"{super().__str__()}[{self.additional_info}]"


class Invokable(Generic[Input, Output], InvokableBase):
    """
    所有可调用子类需要集成Invokable基类。

    Invokable提供了三个方法：with_memory()、with_local_params()、with_global_params()。
    """

    memory: "ConversationMemory" = None
    """会话内存对象，用于存储会话状态。"""

    memory_auto_save = True
    """是否自动保存内存状态标志，默认为True。"""

    local_params: dict = dict()
    """本地参数字典。"""

    global_params: dict = {"memory": None, "input": None}
    """全局参数字典，包含内存和输入对象。"""

    is_global: bool = False
    """是否为全局组件标志，默认为False。"""

    global_var_name: str = ""
    """全局变量名称。"""

    def __or__(
        self,
        other: Union["Invokable[Any, Other]"],
    ) -> "Invokable[Input, Other]":
        """Compose this Invokable with another object to create a InvokableSequence."""
        return InvokableSequence(head=self, tail=coerce_to_invokable(other))

    def __ror__(
        self,
        other: "Invokable[Any, Other]",
    ) -> "Invokable[Input, Other]":
        """Compose this Invokable with another object to create a InvokableSequence."""
        return InvokableSequence(head=coerce_to_invokable(other), tail=self)

    @executor_only
    def run(
        self, node_input: Dict[str, object], runtime_context: RuntimeContextImpl
    ) -> Dict[str, object]:
        """Forwards the TGF Node invoke call to orchestration Invokable `invoke` method."""
        if not hasattr(invokable_local, "executor_context"):
            invokable_local.executor_context = dict()
        invokable_local.executor_context[runtime_context.session_id] = runtime_context
        old_session = (
            get_thread_session()
        )  # Back up the original trace ID and replace with sessionId.
        set_thread_session(runtime_context.get_session_id())
        try:
            ret = self.invoke(node_input, runtime_context=runtime_context)
        except Exception as e:
            # send exception to callback handlers
            CallbackHandlerManager().trigger_event(
                event_name=TriggerEventName.ON_INVOKE.value,
                action=self,
                exception=e,
                data=None,
                runtime_context=runtime_context,
                supplement_info=self._construct_supplement_info(),
            )
            raise ExceptionWrapper(e, self.get_component_exception_message()) from e
        finally:
            invokable_local.executor_context.pop(runtime_context.session_id, None)
            set_thread_session(old_session)  # recover the original trace ID.
        return ret

    def get_global_config(self, global_key, runtime_context: RuntimeContextImpl):
        """get value from runtime context through global_key"""
        return runtime_context.get_context().get_session_ctx(global_key)

    @abstractmethod
    def invoke(self, inputs: Input, **kwargs) -> Output:
        """
        抽象方法，子类需要重写此方法。

        执行组件的主要逻辑。

        Args:
            inputs (Input): 输入数据。
            **kwargs: 关键字参数。

        Returns:
            Output: 返回输出数据。

        Raises:
            需要子类实现具体逻辑。
        """
        ...

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        """
        异步调用方法，子类应重写此方法以支持异步执行。

        默认实现调用invoke方法。

        Args:
            inputs (Input): 输入数据。
            **kwargs: 关键字参数。

        Returns:
            Output: 返回输出数据。

        Raises:
            需要子类实现具体逻辑。
        """
        return self.invoke(inputs, **kwargs)

    def interrupt(self, message: dict):
        """
        强制终止组件执行。

        通过抛出InterruptException来实现组件执行的强制终止。

        Args:
            message (dict): 终止消息字典。

        Raises:
            InterruptException: 抛出终止异常，包含错误码和消息。
        """
        raise InterruptException(
            error_code=StatusCode.CONTROLLER_INTERRUPT_ERROR.code,
            message=json.dumps(message, ensure_ascii=False),
        )

    def reset(self) -> bool:
        """
        组件状态重置。

        重置组件到初始状态。

        Returns:
            bool: 返回True表示重置成功。
        """
        return True

    def process(self, inputs: Iterator[Input], **kwargs) -> Iterator[Output]:
        """
        Default implementation, which buffers input until all is available and then calls stream.
        Subclass should override if they can start producing output while input is being generated.
        """
        next_inputs: Input = None
        got_first_val = False
        for item in inputs:
            if not got_first_val:
                next_inputs = item
                got_first_val = True
            else:
                next_inputs = next_inputs + item

        if got_first_val:
            yield from self.stream(next_inputs, **kwargs)

    async def aprocess(
        self, inputs: AsyncIterator[Input], **kwargs
    ) -> AsyncIterator[Output]:
        """
        Default implementation, which buffers input until all is available and then calls astream.
        Subclass should override if they can start producing output while input is being generated.
        """
        next_inputs: Input = None
        got_first_val = False
        async for item in inputs:
            if not got_first_val:
                next_inputs = item
                got_first_val = True
            else:
                next_inputs = next_inputs + item

        if got_first_val:
            async for output in self.astream(next_inputs, **kwargs):
                yield output

    def stream(self, inputs: Input, **kwargs) -> Iterator[Output]:
        """
        默认的流实现，子类应重写此方法以支持流式输出。

        调用invoke方法并生成输出。

        Args:
            inputs (Input): 输入数据。
            **kwargs: 关键字参数。

        Returns:
            Iterator[Output]: 返回输出数据的迭代器。
        """
        yield self.invoke(inputs, **kwargs)

    async def astream(self, inputs: Input, **kwargs) -> AsyncIterator[Output]:
        """
        默认的异步流实现，子类应重写此方法以支持异步流式输出。

        调用ainvoke方法并生成输出。

        Args:
            inputs (Input): 输入数据。
            **kwargs: 关键字参数。

        Returns:
            AsyncIterator[Output]: 返回输出数据的异步迭代器。
        """
        yield await self.ainvoke(inputs, **kwargs)

    def batch(self, input_list: List[Input], **kwargs):
        """Default implementation runs invoke in parallel using a thread pool executor."""
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None),
            get_instance_info(
                self, blacklist=["memory", "local_params", "global_params"]
            ),
        )
        trace_manager.on_chain_start(input_list)
        try:
            with ThreadPoolExecutor() as executor:
                futures = [
                    executor.submit(
                        self.invoke,
                        input_info,
                        trace_handlers=get_child_manager(trace_manager)
                        if trace_manager
                        else None,
                        **kwargs,
                    )
                    for input_info in input_list
                ]
                outputs = [future.result() for future in futures]
        except BaseException as e:
            trace_manager.on_chain_error(e)
            raise
        trace_manager.on_chain_end(outputs)
        return outputs

    def with_memory(self, memory: "ConversationMemory", auto_save=True) -> "Invokable":
        """
        Get a memory instance.

        Args:
            memory (ConversationMemory): A instance of ConversationMemory.
            auto_save(bool) : is auto save memory context，default True

        Returns:
            Invokable: An invokable with memory.

        Raises:
            TypeError: The input type is not ConversationMemory.

        """
        from jiuwen.memory.conversation import ConversationMemory

        if not isinstance(memory, ConversationMemory):
            raise TypeError("Input type need to be ConversationMemory type.")
        self.memory = memory
        self.memory_auto_save = auto_save
        return self

    def with_local_params(self, **kwargs) -> "Invokable":
        """
        Add extra local params.

        Args:
            kwargs (Any): Any local params you want to add.

        Returns:
            Invokable: An invokable with extra local params.

        """
        if isinstance(self, InvokableSequence):
            for action in self.actions:
                action.local_params = deepcopy(action.local_params)
                action.local_params.update(kwargs)
        else:
            self.local_params = deepcopy(self.local_params)
            self.local_params.update(kwargs)

        return self

    def with_global_params(self, var_name: str) -> "Invokable":
        """
        Save current invokable instance's output into the global params.
        The output's name is var_name.

        Args:
            var_name (str): The output's name.

        Returns:
            Invokable: A invokable with output.

        Raises:
            ValueError: The input value is equal to'input' and 'memory_content'.
        """
        if var_name in ["input", "memory_content"]:
            raise ValueError("The input value can not be 'input' or 'memory_content'.")
        if isinstance(self, InvokableSequence):
            action = self.actions[-1]
            action.is_global = True
            action.global_var_name = var_name
        else:
            self.is_global = True
            self.global_var_name = var_name

        return self


@dataclass
class InvokableSequence(Invokable[Input, Output], ABC):
    """
    A sequence of invokables, where the output of each is the input of the next.

    """

    head: Union[Invokable[Input, Any], Invokable[Input, Any]]
    tail: Union[Invokable[Input, Any], Invokable[Any, Output]]
    middle: List[Union[Invokable[Any, Any], Invokable[Any, Any]]] = field(
        default_factory=list
    )

    def __or__(self, other: Invokable[Any, Other]) -> Invokable[Input, Other]:
        if isinstance(other, InvokableSequence):
            return InvokableSequence(
                head=self.head,
                middle=self.middle + [self.tail] + [other.head] + other.middle,
                tail=other.tail,
            )

        return InvokableSequence(
            head=self.head,
            middle=self.middle + [self.tail],
            tail=coerce_to_invokable(other),
        )

    def __ror__(self, other: Invokable[Any, Other]) -> Invokable[Input, Other]:
        if isinstance(other, InvokableSequence):
            return InvokableSequence(
                head=other.head,
                middle=other.middle + [other.tail] + [self.head] + self.middle,
                tail=self.tail,
            )

        return InvokableSequence(
            head=coerce_to_invokable(other),
            middle=[self.head] + self.middle,
            tail=self.tail,
        )

    @property
    def actions(self) -> List[Invokable[Any, Any]]:
        """
        Get the list of actions.

        Returns:
            List[Invokable[Any, Any]]: The list of actions.
        """
        return [self.head] + self.middle + [self.tail]

    @staticmethod
    def check_dict_keys(dict1: dict, dict2: dict) -> bool:
        """Check whether two dict have the same key."""
        keys1 = set(dict1.keys())
        keys2 = set(dict2.keys())
        return bool(keys1.intersection(keys2))

    def invoke(self, inputs: Input, **kwargs) -> Output:
        """
        Run the sequence to transform an input into an output.

        Args:
            inputs (Input): Input of the first invokable.
                throughout generation. See more details in `common.trace` module.
            kwargs (Any): Extra params.

        Returns:
            Output: The final output of sequence.

        Raises:
            ValueError: The same variable name is not allowed in local params and global params.

        """
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None), get_instance_info(self)
        )
        trace_manager.on_chain_start(inputs)

        try:
            self.global_params["input"] = deepcopy(inputs)
            if self.memory is not None and self.memory.to_messages() is not None:
                self.global_params["memory_content"] = self.memory.to_messages()
            for action in self.actions:
                if self.check_dict_keys(self.global_params, action.local_params):
                    raise ValueError(
                        "The same variable name is not allowed in "
                        f"{action.__class__.__name__}.with_local_params and {self.__class__.__name__}"
                        ".with_global_params."
                    )
                inputs = action.invoke(
                    inputs,
                    trace_handlers=get_child_manager(trace_manager)
                    if trace_manager
                    else None,
                    **deepcopy(self.global_params),
                    **deepcopy(action.local_params),
                )
                if action.is_global:
                    if action.global_var_name in self.global_params:
                        raise ValueError(
                            "The same inputs exist in multiple with_global_params methods."
                        )
                    self.global_params[action.global_var_name] = deepcopy(inputs)
            if (
                self.memory is not None
                and isinstance(inputs, str)
                and self.memory_auto_save
            ):
                origin_input = self.global_params.get("input")
                self.memory.save_context(origin_input, inputs)
        except BaseException as e:
            trace_manager.on_chain_error(e)
            raise
        trace_manager.on_chain_end(inputs)
        return cast(Output, inputs)

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        self.global_params["input"] = deepcopy(inputs)
        for action in self.actions:
            if self.check_dict_keys(self.global_params, action.local_params):
                raise ValueError(
                    "The same variable name is not allowed in "
                    f"{action.__class__.__name__}.with_local_params and {self.__class__.__name__}.with_global_params."
                )
            inputs = await action.ainvoke(
                inputs, **deepcopy(self.global_params), **deepcopy(action.local_params)
            )
            if action.is_global:
                if action.global_var_name in self.global_params:
                    raise ValueError(
                        "The same inputs exist in multiple with_global_params methods."
                    )
                self.global_params[action.global_var_name] = deepcopy(inputs)
        if (
            self.memory is not None
            and isinstance(inputs, str)
            and self.memory_auto_save
        ):
            origin_input = self.global_params.get("input")
            self.memory.save_context(origin_input, inputs)

        return cast(Output, inputs)

    def process(self, inputs: Iterator[Input], **kwargs) -> Iterator[Output]:
        iterator = self._process(inputs, **kwargs)
        for item in iterator:
            yield item

    async def aprocess(
        self, inputs: AsyncIterator[Input], **kwargs
    ) -> AsyncIterator[Output]:
        async_iterator = self._aprocess(inputs, **kwargs)
        async for item in async_iterator:
            yield item

    def stream(self, inputs: Input, **kwargs) -> Iterator[Output]:
        """stream output"""
        yield from self.process(iter([inputs]), **kwargs)

    async def astream(self, inputs: Input, **kwargs) -> AsyncIterator[Output]:
        """asynchronous stream output"""

        async def convert_aiter() -> AsyncIterator[Input]:
            yield inputs

        async for item in self.aprocess(convert_aiter(), **kwargs):
            yield item

    def _process(self, inputs: Iterator[Input], **kwargs) -> Iterator[Output]:
        pipeline = inputs
        for action in self.actions:
            pipeline = action.process(pipeline, **kwargs)

        for output in pipeline:
            yield output

    async def _aprocess(
        self, inputs: AsyncIterator[Input], **kwargs
    ) -> AsyncIterator[Output]:
        async_pipeline = inputs
        for action in self.actions:
            async_pipeline = action.aprocess(async_pipeline, **kwargs)

        async for output in async_pipeline:
            yield output


class InvokableParallel(Invokable[Input, Dict[str, Any]]):
    """
    A Invokable that runs a mapping of Invokables in parallel,
    and returns a mapping of their outputs.
    """

    def __init__(
        self,
        __actions: Optional[
            Mapping[
                str,
                Union[
                    Invokable[Input, Any],
                    Callable[[Input], Any],
                    Mapping[str, Union[Invokable[Input, Any], Callable[[Input], Any]]],
                ],
            ]
        ] = None,
        **kwargs: Union[
            Invokable[Input, Any],
            Callable[[Input], Any],
            Mapping[str, Union[Invokable[Input, Any], Callable[[Input], Any]]],
        ],
    ) -> None:
        super().__init__()
        merged_actions = {**__actions} if __actions else {}
        merged_actions.update(kwargs)
        self.actions = {
            key: coerce_to_invokable(value) for key, value in merged_actions.items()
        }

    def invoke(self, inputs: Input, **kwargs) -> Output:
        """
        Run in parallel.
        """
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None), get_instance_info(self)
        )
        trace_manager.on_chain_start(inputs)

        try:
            with ThreadPoolExecutor() as executor:
                futures = [
                    executor.submit(
                        action.invoke,
                        inputs,
                        trace_handlers=get_child_manager(trace_manager)
                        if trace_manager
                        else None,
                        **kwargs,
                    )
                    for key, action in self.actions.items()
                ]
                outputs = {
                    key: future.result() for key, future in zip(self.actions, futures)
                }
        except BaseException as e:
            trace_manager.on_chain_error(e)
            raise
        trace_manager.on_chain_end(outputs)
        return outputs

    async def ainvoke(self, inputs: Input, **kwargs) -> Output:
        """Run in parallel asynchronously."""
        results = await asyncio.gather(
            *(action.ainvoke(inputs, **kwargs) for key, action in self.actions.items())
        )
        outputs = dict(zip(self.actions, results))

        return outputs

    def stream(self, inputs: Input, **kwargs) -> Output: ...

    async def astream(self, inputs: Input, **kwargs) -> Output: ...


InvokableLike = Union[
    Invokable[Input, Output],
    Callable[[Input], Output],
    Callable[[Input], Awaitable[Output]],
    Mapping[str, Any],
]


class InvokableLambda(Invokable[Input, Output]):
    """InvokableLambda converts a python callable into a Invokable.

    Wrapping a callable in a InvokableLambda makes the callable usable
    within either a sync or async context.

    Args:
        func: Either sync or async callable
        async_func: An async callable that takes an input and returns an output.

    """

    def __init__(
        self,
        func: Union[
            Callable[[Input, Optional[Any]], Output],
            Callable[[Input, Optional[Any]], Awaitable[Output]],
        ],
        async_func: Optional[
            Callable[[Input, Optional[Any]], Awaitable[Output]]
        ] = None,
    ) -> None:
        """Create a InvokableLambda from a callable, and async callable or both.

        Accepts both sync and async variants to allow providing efficient
        implementations for sync and async execution.

        Args:
            func: sync callable
            async_func: async callable
        """
        super().__init__()
        self.async_func = async_func
        if callable(func):
            self.func = cast(Callable[[Input, Optional[Any]], Output], func)
        else:
            raise TypeError(
                "Expected a callable type for `func`.Instead got an unsupported type: {type(func)}"
            )

    def invoke(
        self,
        inputs: Input,
        **kwargs: Optional[Any],
    ) -> Output:
        """Invoke this runnable synchronously."""
        blacklist = []
        if self.async_func:
            blacklist.append("async_func")
        if self.func:
            blacklist.append("func")
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None),
            get_instance_info(self, blacklist=blacklist),
        )
        trace_manager.on_chain_start(inputs)
        if hasattr(self, "func"):
            try:
                param_num = _get_function_param_num(self.func)
                if param_num == 1:
                    outputs = self.func(inputs)
                else:
                    outputs = self.func(inputs, **kwargs)
                trace_manager.on_chain_end(outputs)
                return outputs
            except BaseException as e:
                trace_manager.on_chain_error(e)
                raise e
        else:
            raise TypeError(
                "Cannot invoke a coroutine function synchronously."
                "Use `ainvoke` instead."
            )

    async def ainvoke(self, inputs: Input, **kwargs: Optional[Any]) -> Output:
        """Invoke this runnable asynchronously."""
        if hasattr(self, "async_func"):
            param_num = _get_function_param_num(self.func)
            if param_num == 1:
                return await self.async_func(inputs)
            return await self.async_func(inputs, **kwargs)

        # Delegating to super implementation of ainvoke.
        # Uses asyncio executor to run the sync version (invoke)
        return await super().ainvoke(inputs, **kwargs)


def coerce_to_invokable(thing: InvokableLike) -> Invokable[Input, Output]:
    """
    Coerce a given object to an Invokable object.

    Args:
       thing (InvokableLike): The object to be coerced.

    Returns:
       Invokable[Input, Output]: The coerced Invokable object.

    Raises:
       TypeError: If the given object is not an Invokable, callable or dict.
    """
    if isinstance(thing, Invokable):
        return thing
    if callable(thing):
        return InvokableLambda(cast(Callable[[Input], Output], thing))
    if isinstance(thing, dict):
        return cast(Invokable[Input, Output], InvokableParallel(thing))
    raise TypeError(
        f"Expected a Invokable, callable or dict. Instead got an unsupported type: {type(thing)}"
    )
