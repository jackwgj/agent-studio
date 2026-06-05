#!/bin/python3
#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Provides convenient syntax for developers to orchestrate DFDs between components, reducing code writing workload."""

__all__ = ("DslException", "parallel", "switch", "invokable")

import inspect
from functools import partial
from typing import (
    Any,
    Awaitable,
    Callable,
    Generic,
    TypeVar,
    Union,
    TYPE_CHECKING,
    no_type_check,
    overload,
)

from jiuwen.common.exception.status_code import StatusCode

# compatible with python version later than v2024.11
try:
    from typing import Tuple
except ImportError:
    Tuple = tuple

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.orchestration.base import (
    Invokable,
    InvokableLambda,
    InvokableParallel,
    coerce_to_invokable,
)
from jiuwen.orchestration.invokable_branch import InvokableBranch
from jiuwen.orchestration.utils import Input, Output

if TYPE_CHECKING:  # pragma: no cover
    from jiuwen.orchestration.base import InvokableLike


_Invokable = TypeVar("_Invokable", bound=Invokable)
_T = TypeVar("_T")
_ANY_DEFAULT = "_ANY_DEFAULT"

INVOKABLE_LIKE = "InvokableLike"


class DslException(JiuWenBaseException):
    """This exception is thrown when an error occurs during dsl orchestration."""

    def __init__(self, error_code: int, message: str):
        super().__init__(error_code=error_code, message=message)


class DslParallel(Invokable[Input, "list[Output]"], Generic[Input, Output]):
    """Dsl parallel orchestration."""

    __parallel: InvokableParallel
    __len: int

    def __init__(self, *invokable_like: INVOKABLE_LIKE):
        self.__parallel = InvokableParallel(
            {self.__gen_id(index): i for index, i in enumerate(invokable_like)}
        )
        self.__len = len(invokable_like)

    @classmethod
    def __gen_id(cls, index: int) -> str:
        return f"dsl_parallel_{index}"

    def invoke(self, inputs: Input, **kwargs) -> "list[Output]":
        sub_ret = self.__parallel.invoke(inputs, **kwargs)
        return list((sub_ret[self.__gen_id(index)] for index in range(self.__len)))


def parallel(
    a: INVOKABLE_LIKE, b: INVOKABLE_LIKE, *others: INVOKABLE_LIKE
) -> DslParallel:
    """
    Create a DSL parallel chain, whose output format of .invoke() is a list.
    The order of the elements in the list returned by invoke() is the same
    as the order of Invokable passed in when parallel() is called. For
    example, ``parallel(a, b).invoke(something)[0]`` is generated from ``a``.

    Args:
        a(InvokableLike) - The first chain.
        b(InvokableLike) - The second chain.
        *others(InvokableLike) - Some of the other chains if necessary.

    Returns:
        A parallel Invokable.
    """
    return DslParallel(a, b, *others)


if TYPE_CHECKING:  # pragma: no cover

    @overload
    def switch(
        situation: "InvokableLike[Input, _T]",
        action_map: "dict[_T, InvokableLike[Input, Output]]",
        *,
        default: "InvokableLike[Input, Output]",
    ) -> InvokableBranch[Input, Output]: ...

    @overload
    def switch(
        *branches: Union[
            Tuple[
                Union[
                    Invokable[Input, bool],
                    Callable[[Input], bool],
                    Callable[[Input], Awaitable[bool]],
                ],
                "InvokableLike",
            ],
            "InvokableLike",
        ],
    ) -> InvokableBranch[Input, Output]: ...


def switch(
    *branches,
    situation: "InvokableLike[Input, _T]" = _ANY_DEFAULT,
    action_map: "dict[_T, InvokableLike[Input, Output]]" = _ANY_DEFAULT,
    default: "InvokableLike[Input, Output]" = _ANY_DEFAULT,
) -> InvokableBranch[Input, Output]:
    """
    Create an :py:class:`InvokableBranch`.

    Args:
        *branches (Union[Tuple[InvokableLike, InvokableLike], InvokableLike]): A
            list of `(condition, action)` pairs with a default branch at its
            tail. See :py:class:`orchestration.InvokableBranch` for details;
        situation (InvokableLike): Conditional decision function, should only
            have one required positional argument which will be filled with the
            input parameter of :py:meth:`~orchestration.InvokableBranch.invoke()`
            and the return value is regarded as the key of `action_map` ;
        action_map (dict[_T, InvokableLike[Input, Output]]): A map from the
            return value of `situation` to the value regarded as the `action`
            and the matched `action` will be called with the input parameter of
            :py:meth:`~orchestration.InvokableBranch.invoke()` and then the
            return value of `action` will be passed as the return value of
            :py:meth:`~orchestration.InvokableBranch.invoke()` .
        default (InvokableLike): A default action which is used with `situation`
            and `action_map` (not the default branch of `branches` ).

    Returns:
        An :py:class:`orchestration.InvokableBranch` instance.
    """
    # `default` is announced as keyword only in overload, use it to separate overload situations.
    if default is _ANY_DEFAULT:
        # base branch mode.
        # forbid extra value
        if situation is not _ANY_DEFAULT:
            raise TypeError("switch() got an unexpected keyword argument 'situation'")
        if action_map is not _ANY_DEFAULT:
            raise TypeError("switch() got an unexpected keyword argument 'action_map'")
        return InvokableBranch(*branches)
    # dsl branch mode.
    # Simulate exception raised from Python builtin library.
    args = list(branches)
    if situation is not _ANY_DEFAULT:
        if args:
            raise TypeError("switch() got multiple values for argument 'situation'")
    else:
        if not args:
            no_action_map = action_map is _ANY_DEFAULT
            raise TypeError(
                f"switch() missing {2 if no_action_map else 1} required"
                f" positional argument{'s' if no_action_map else ''}: 'situation'"
                f"""{" and 'action_map'" if no_action_map else ""}"""
            )
        situation = args.pop(0)
    if action_map is not _ANY_DEFAULT:
        if args:
            raise TypeError("switch() got multiple values for argument 'action_map'")
    else:
        if not args:
            raise TypeError(
                "switch() missing 1 required positional argument: 'action_map'"
            )
        action_map = args.pop(0)
    # Extra elements.
    if args:
        raise TypeError(
            f"switch() takes 2 positional argument but {len(branches)} were given"
        )
    # build
    return _switch_dsl_creator(situation, action_map, default)


def _switch_dsl_creator(
    situation: _T,
    action_map: "dict[_T, InvokableLike[Input, Output]]",
    default: "InvokableLike[Input, Output]",
) -> InvokableBranch[Input, Output]:
    if not isinstance(action_map, dict):
        raise DslException(
            error_code=StatusCode.DSL_PARAM_TYPE_ERROR.code,
            message=StatusCode.DSL_PARAM_TYPE_ERROR.errmsg.format(
                type(action_map).__name__
            ),
        )
    situation = coerce_to_invokable(situation)
    branches = []
    for case, action in action_map.items():
        branches.append((_switch_dsl_wrap_lambda(situation, case), action))
    branches.append(default)
    return InvokableBranch(*branches)


def _switch_dsl_wrap_lambda(situation: "InvokableLike", case):
    """Bind `case` into lambda."""
    return lambda x: situation.invoke(x) == case


@overload  # pragma: no cover
def invokable(f: Callable[[Input], Output]) -> InvokableLambda[Input, Output]: ...


@overload  # pragma: no cover
def invokable(
    *, wrap_cls: "type[_Invokable]"
) -> Callable[[Callable[[], Any]], _Invokable]: ...


@overload  # pragma: no cover
def invokable(
    *, async_fn: Callable[[Input], Awaitable[Output]]
) -> Callable[[Callable[[Input], Output]], InvokableLambda[Input, Output]]: ...


@overload  # pragma: no cover
def invokable(fn: Callable[[], Any], *, wrap_cls: "type[_Invokable]") -> _Invokable: ...


@overload  # pragma: no cover
def invokable(
    fn: Callable[[Input], Output], *, async_fn: Callable[[Input], Awaitable[Output]]
) -> InvokableLambda[Input, Output]: ...


@no_type_check
def invokable(
    fn: Callable = None,
    *,
    wrap_cls: "type[_Invokable]" = _ANY_DEFAULT,
    async_fn: Callable[[Input], Awaitable[Output]] = _ANY_DEFAULT,
):
    """
    Using ``@invokable`` to convert one function into something invokable,
    which depends on its usage:

    - Using ``@invokable`` only to convert the decorated function into an
      instance of :py:class:`orchestration.InvokableLambda` using the
      decorated function as the first param. The decorated function should
      have only one positional arguments;
    - Using ``@invokable(wrap_cls=SomeInvokable)`` to replace the decorated
      function with an instance of `SomeInvokable` which is initialized by
      the return value of the decorated function;
    - Using ``@invokable(async_fn=some_async_fn)`` to convert the decorated
      function into an instance of :py:class:`orchestration.InvokableLambda`
      but will apply `some_async_fn` as the param `async_func` of the init
      param.

    Args:
        fn (Callable): The decorated function. The parameter requirements
            vary according to the usage.
        wrap_cls (type): A subclass of :py:class:`orchestration.Invokable`
            and will raise an :py:class:`~orchestration.dsl.JiuWenBaseException`
            if not. The decorated function `fn` should have no required
            parameter, and the return value will be regarded as the first
            init positional parameter (while `None` is regarded as no
            parameter and a list or a tuple is regarded as a list of
            positional parameters).
        async_fn (Callable[[Input], Awaitable[Output]]): An async function
            used to create an :py:class:`orchestration.InvokableLambda`
            with the decorated function.

    Returns:
        An :py:class:`orchestration.Invokable` which type differs from the
        usage of this decorator.

    Raises:
        - :py:class:`orchestration.dsl.JiuWenBaseException` - If in these situations:

          - Using `wrap_cls` and `async_fn` at the same time;
          - Using `wrap_cls` but it is not a subclass of
            :py:class:`orchestration.Invokable` or the annotation of its init
            function has any required keyword-only argument (no default value);
          - Using `async_fn` but not a coroutine function;
    """
    # ban both wrap_cls and async_fn
    if all((wrap_cls is not _ANY_DEFAULT, async_fn is not _ANY_DEFAULT)):
        raise DslException(
            error_code=StatusCode.DSL_INVALID_WRAP_CLS_USAGE_MODE.code,
            message=StatusCode.DSL_INVALID_WRAP_CLS_USAGE_MODE.errmsg,
        )

    if async_fn is not _ANY_DEFAULT:
        # usage of @invokable(async_fn=some_async_func) for creating InvokableLambda with async function
        if not inspect.iscoroutinefunction(async_fn):
            raise DslException(
                error_code=StatusCode.DSL_ASYNC_FUNCTION_ERROR.code,
                message=StatusCode.DSL_ASYNC_FUNCTION_ERROR.errmsg,
            )
        if fn is not None:
            return InvokableLambda(fn, async_func=async_fn)
        return partial(InvokableLambda, async_func=async_fn)

    if wrap_cls is not _ANY_DEFAULT:
        # usage of @invokable(wrap_cls=xxx), check no required keyword-only argument of the __init__ of wrap_cls.
        is_type = isinstance(wrap_cls, type)
        if not (is_type and issubclass(wrap_cls, Invokable)):
            raise DslException(
                error_code=StatusCode.DSL_ILLEGAL_WRAP_CLS_TYPE.code,
                message=StatusCode.DSL_ILLEGAL_WRAP_CLS_TYPE.errmsg.format(
                    wrap_cls.__name__
                    if is_type
                    else ("instance of " + type(wrap_cls).__name__)
                ),
            )
        _no_required_keyword(
            wrap_cls.__init__ if hasattr(wrap_cls, "__init__") else None
        )
        if fn is not None:
            return _create_invokable(wrap_cls, fn)
        return partial(_create_invokable, wrap_cls)

    # usage of @invokable def xxx(): ...
    return InvokableLambda(fn)


def _create_invokable(
    cls: "type[_Invokable]", func: Callable[..., Union[tuple, None]]
) -> Invokable[Input, Output]:
    args = func()
    if isinstance(args, (tuple, list)):
        return cls(*args)
    if args is None:
        return cls()
    return cls(args)


def _no_required_keyword(f: Callable) -> None:
    if f is None:
        return
    params = inspect.signature(f).parameters
    for param in params.values():
        if (param.kind == param.KEYWORD_ONLY) and (
            param.default == inspect.Parameter.empty
        ):
            raise DslException(
                error_code=StatusCode.DSL_UNSUPPORTED_KEYWORD_ERROR.code,
                message=StatusCode.DSL_UNSUPPORTED_KEYWORD_ERROR.errmsg,
            )
