#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""conversation and plan message definition"""

from abc import ABC
from abc import abstractmethod


class BaseMessage(ABC):
    """The base abstract Message class.

    Messages are the inputs and outputs of dialogue.
    """

    def __init__(self, content):
        self.content = content

    def __repr__(self):
        return f"Message(type={repr(self.type)}, content={repr(self.content)})"

    def __eq__(self, other):
        return self.type == other.type and self.content == other.content

    @property
    @abstractmethod
    def type(self) -> str:
        """Type of the Message, used for serialization."""


class SystemMessage(BaseMessage):
    """A Message from a user."""

    def __init__(self, content):
        super().__init__(content)
        self.content = content

    @property
    def type(self) -> str:
        """Type of the message, used for serialization."""
        return "system"


class UserMessage(BaseMessage):
    """A Message from a user."""

    def __init__(self, content):
        super().__init__(content)
        self.content = content

    @property
    def type(self) -> str:
        """Type of the message, used for serialization."""
        return "user"


class AssistantMessage(BaseMessage):
    """A Message from an Assistant."""

    def __init__(self, content):
        super().__init__(content)

    @property
    def type(self) -> str:
        """Type of the message, used for serialization."""
        return "assistant"
