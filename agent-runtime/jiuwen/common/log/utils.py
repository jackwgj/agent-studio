#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""useful tools for logging data."""

__all__ = ("LazyStr", "blur", "blur_later")

from typing import Any, Callable

from pydantic import BaseModel


class LazyStr:
    """Call `f` while calling its `__str__`."""

    def __init__(self, f: Callable[[], str]):
        self.__f = f
        self.__val = None

    def __str__(self):
        if self.__val is None:
            self.__val = self.__f()
        return self.__val


def blur_later(obj):
    """Only blur `obj` while it is going to be printed."""
    return LazyStr(lambda: blur(obj))


def blur(obj) -> str:
    """Hides sensitive information that may be displayed and formats the information into string.

    If `obj` is a string, it will return its repr.
    If `blur_all` is True, blur every item. Otherwise, only blur items of the key in the dict in the fuzzy list.
    """
    if obj is None:
        return "None"
    for t, f in _blur_map:
        if isinstance(obj, t):
            return f(obj)
    return f"...({type(obj).__name__})"


def _blur_dict(d: dict) -> str:
    contents = ", ".join(f"{k!r}: {blur(v)}" for k, v in d.items())
    return "{" + contents + "}"


_blur_map: list[tuple[type, Callable[[Any], str]]] = [
    (dict, _blur_dict),
    (list, lambda li: "[" + ", ".join(blur(v) for v in li) + "]"),
    (BaseModel, lambda b: blur(b.model_dump(by_alias=True))),
]
