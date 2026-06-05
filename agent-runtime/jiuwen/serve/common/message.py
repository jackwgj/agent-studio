# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.

"""Message collector interfaces and implementations for streaming message collection.

This module provides abstract base classes for collecting and filtering messages
in a streaming context. It defines the MessageCollector interface and provides
a concrete StreamingCollector implementation.
"""

from abc import ABC, abstractmethod
from typing import Any, List


class MessageCollector(ABC):
    """Abstract base class for message collection.

    This class defines the interface for collecting, filtering, and managing
    messages in a streaming context. The collect() and done() methods are
    asynchronous, while filter(), messages(), and is_done() are synchronous.
    Subclasses must implement all abstract methods to provide concrete collection behavior.
    """

    @abstractmethod
    async def collect(self, message: Any) -> None:
        """Asynchronously collect a message if it passes the filter.

        Args:
            message: The message to collect. Can be of any type.
        """
        ...

    @abstractmethod
    def filter(self, message: Any) -> bool:
        """Check if a message should be collected.

        Args:
            message: The message to check. Can be of any type.

        Returns:
            True if the message should be collected, False otherwise.
        """
        ...

    @abstractmethod
    async def done(self) -> None:
        """Asynchronously mark the collection as complete.

        After calling this method, no more messages should be collected.
        """
        ...

    @abstractmethod
    def messages(self) -> List[Any]:
        """Get all collected messages.

        Returns:
            A list of all messages that have been collected.
        """
        ...

    @abstractmethod
    def is_done(self) -> bool:
        """Check if collection is complete.

        Returns:
            True if collection is done, False otherwise.
        """
        ...


class StreamingCollector(MessageCollector, ABC):
    """Base implementation of MessageCollector for streaming message collection.

    This class provides a concrete implementation of the MessageCollector interface
    with basic streaming collection functionality. It maintains an internal list
    of messages and tracks completion status. The collect() and done() methods are
    asynchronous, while filter(), messages(), and is_done() are synchronous.

    Subclasses should override the filter() method to implement custom filtering logic.
    """

    def __init__(self) -> None:
        """Initialize a new StreamingCollector instance."""
        self._messages: List[Any] = []
        self._done: bool = False

    async def collect(self, message: Any) -> None:
        """Asynchronously collect a message if collection is not done and message passes filter.

        Args:
            message: The message to collect. Can be of any type.
        """
        if self._done:
            return
        if not self.filter(message):
            return
        self._messages.append(message)

    async def done(self) -> None:
        """Asynchronously mark the collection as complete.

        After calling this method, collect() will no longer accept new messages.
        """
        self._done = True

    def messages(self) -> List[Any]:
        """Get all collected messages.

        Returns:
            A list of all messages that have been collected and passed the filter.
        """
        return self._messages.copy()

    def is_done(self) -> bool:
        """Check if collection is complete.

        Returns:
            True if done() has been called, False otherwise.
        """
        return self._done
