"""Serialize executions that share one persistent conversation workspace."""

from __future__ import annotations

import asyncio
from dataclasses import dataclass
from threading import Lock

from agent_runtime.conversation.execution_context import ConversationExecutionContext


@dataclass
class _Entry:
    lock: asyncio.Lock
    users: int = 0


_entries: dict[str, _Entry] = {}
_entries_guard = Lock()


class ConversationExecutionLease:
    def __init__(self, key: str, entry: _Entry) -> None:
        self._key = key
        self._entry = entry
        self._released = False

    async def release(self) -> None:
        if self._released:
            return
        self._released = True
        self._entry.lock.release()
        with _entries_guard:
            self._entry.users -= 1
            if self._entry.users == 0:
                _entries.pop(self._key, None)


async def acquire_conversation_execution(
    context: ConversationExecutionContext,
) -> ConversationExecutionLease:
    """Wait for other executions using this process and conversation."""
    key = str(context.workspace.conversation_root)
    with _entries_guard:
        entry = _entries.get(key)
        if entry is None:
            entry = _Entry(asyncio.Lock())
            _entries[key] = entry
        entry.users += 1
    try:
        await entry.lock.acquire()
    except BaseException:
        with _entries_guard:
            entry.users -= 1
            if entry.users == 0:
                _entries.pop(key, None)
        raise
    return ConversationExecutionLease(key, entry)
