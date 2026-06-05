# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Tools to init orchestration IrManager/InvokableManager."""

from threading import Thread, Lock
from typing import Callable, Mapping, Any

from jiuwen.common.log.base import logger
from jiuwen.orchestration.common.sync import Once


class LazyInit:
    """Do once with thread-safe.

    Args:
        f: the function which want to be done once.
        after: Retry interval (seconds) after first failed.
        interval: Retry interval (seconds) after first failed.
        args: args to call `f`
        kwargs: kwargs to call `f`
    """

    def __init__(
        self,
        f: Callable,
        desc: str = None,
        after: float = 0,
        interval: float = 10,
        retry: int = 0,
        args: tuple = None,
        kwargs: Mapping[str, Any] = None,
    ):
        self.__once = Once()
        self.__f = f
        self.__args = args or tuple()
        self.__kwargs = kwargs or dict()
        desc = desc or f.__name__
        self.__stop_lock = Lock()
        self.__stop_lock.acquire()
        self.__stop_once = Once()
        self.__t = Thread(
            target=self.__main,
            name=f"Init of {desc}",
            args=(desc, after, interval, retry),
            daemon=True,
        )
        self.__t.start()

    def push(self):
        """Init immediately and do not ignore the exception. If already inited, nothing happens."""
        self.__once.do(self.__f, *self.__args, **self.__kwargs)
        self.cancel()

    def cancel(self):
        """Stop without trying init."""
        self.__stop_once.do(self.__cancel)

    def __main(self, desc: str, after: float, interval: float, retry: int):
        if (after > 0) and self.__stop_lock.acquire(
            timeout=after
        ):  # sleep but can be interrupted
            return
        cnt = 0
        while True:
            try:
                self.__once.do(self.__f, *self.__args, **self.__kwargs)
            except Exception as e:
                cnt += 1
                if retry and (cnt > retry):
                    logger.warning(
                        "Init %s failed with %s and won't retry (max retry count: %d): %s"
                        " (if this feature is not used, ignore all alarms of this thread)",
                        desc,
                        type(e).__name__,
                        retry,
                        e,
                        simple_log="Init failed and won't try",
                    )
                    break
                logger.warning(
                    "Init %s failed with %s and will retry after %s seconds: %s"
                    " (if this feature is not used, ignore all alarms of this thread)",
                    desc,
                    type(e).__name__,
                    interval,
                    e,
                    simple_log="Init failed and will retry",
                )
            if self.__once.done:
                logger.info("Init %s succeeded", desc)
                break
            if self.__stop_lock.acquire(
                timeout=interval
            ):  # sleep but can be interrupted
                break

    def __cancel(self):
        self.__stop_lock.release()
        self.__t.join()
        del self.__t
        self.__t = None
