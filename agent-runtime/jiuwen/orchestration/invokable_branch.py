#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Provide invokable with branching capability."""

from typing import (
    Any,
    Awaitable,
    Callable,
    Mapping,
    Sequence,
    Tuple,
    Union,
    cast,
)

from jiuwen.common.log.base import logger
from jiuwen.insight.manager import TraceManager, get_child_manager
from jiuwen.insight.utils import get_instance_info
from jiuwen.orchestration.base import Invokable, InvokableLike, coerce_to_invokable
from jiuwen.orchestration.utils import Input, Output


class InvokableBranch(Invokable[Input, Output]):
    """
    Invokable object that selects which branch to run based on a condition.

    Args:
        branches: a list of (condition, runnable) pairs and a default branch.

    """

    branches: Sequence[Tuple[Invokable[Input, bool], Invokable[Input, Output]]]
    default: Invokable[Input, Output]

    def __init__(
        self,
        *branches: Union[
            Tuple[
                Union[
                    Invokable[Input, bool],
                    Callable[[Input], bool],
                    Callable[[Input], Awaitable[bool]],
                ],
                InvokableLike,
            ],
            InvokableLike,  # To accommodate the default branch
        ],
    ) -> None:
        """Invokable object that runs one of two branches based on a condition."""
        if len(branches) < 2:
            raise ValueError("InvokableBranch requires at least two branches")

        default = branches[-1]

        if not isinstance(default, (Invokable, Callable, Mapping)):
            raise TypeError(
                "InvokableBranch default must be invokable, callable or mapping."
            )

        default_ = cast(
            Invokable[Input, Output], coerce_to_invokable(cast(InvokableLike, default))
        )

        _branches = []

        for branch in branches[:-1]:
            if not isinstance(branch, (tuple, list)):  # type: ignore[arg-type]
                raise TypeError(
                    f"InvokableBranch branches must be tuples or lists, not {type(branch)}"
                )

            if not len(branch) == 2:
                raise ValueError(
                    f"InvokableBranch branches must be tuples or lists of length 2, not {len(branch)}"
                )
            condition_function, invokable = branch
            condition_function = cast(
                Invokable[Input, bool], coerce_to_invokable(condition_function)
            )
            invokable = coerce_to_invokable(invokable)

            _branches.append((condition_function, invokable))

        self.branches = _branches
        self.default = default_

    def invoke(self, inputs: Input, **kwargs: Any) -> Output:
        """First evaluates the condition, then delegate to true or false branch."""
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None), get_instance_info(self)
        )
        trace_manager.on_chain_start(inputs)
        try:
            for i, branch in enumerate(self.branches):
                condition_function, invokable = branch
                expression_value = condition_function.invoke(
                    inputs,
                    trace_handlers=get_child_manager(
                        trace_manager, name=f"Branch{i}.condition"
                    )
                    if trace_manager
                    else None,
                    kwargs=kwargs,
                )

                if expression_value:
                    output = invokable.invoke(
                        inputs,
                        trace_handlers=get_child_manager(
                            trace_manager, name=f"Branch{i}.invoke"
                        )
                        if trace_manager
                        else None,
                        kwargs=kwargs,
                    )
                    break
            else:
                output = self.default.invoke(
                    inputs,
                    trace_handlers=get_child_manager(
                        trace_manager, name="DefaultBranch"
                    )
                    if trace_manager
                    else None,
                    kwargs=kwargs,
                )
        except Exception as e:
            trace_manager.on_chain_error(e)
            raise
        trace_manager.on_chain_end(output)
        return output

    async def ainvoke(self, inputs: Input, **kwargs: Any) -> Output:
        """Async version of invoke."""
        try:
            for _, branch in enumerate(self.branches):
                condition_function, invokable = branch
                expression_value = await condition_function.ainvoke(
                    inputs, kwargs=kwargs
                )

                if expression_value:
                    output = await invokable.ainvoke(inputs, kwargs=kwargs)
                    break
            else:
                output = await self.default.ainvoke(inputs, kwargs=kwargs)
        except Exception as e:
            logger.error(e)
            raise e
        return output

    def stream(self, inputs: Input, **kwargs) -> Output: ...

    async def astream(self, inputs: Input, **kwargs) -> Output: ...
