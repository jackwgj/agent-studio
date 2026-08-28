"""Canonical conversation events and legacy TeamEvent compatibility helpers."""

from .conversation_adapter import adapt_runner_event
from .types import ConversationEventField, ConversationEventType

__all__ = ["ConversationEventField", "ConversationEventType", "adapt_runner_event"]
