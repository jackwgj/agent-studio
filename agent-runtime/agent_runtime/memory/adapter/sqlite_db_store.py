import asyncio
import logging

from openjiuwen.core.foundation.store.base_db_store import BaseDbStore
from sqlalchemy.ext.asyncio import AsyncEngine, create_async_engine

logger = logging.getLogger(__name__)


class SqliteDbStore(BaseDbStore):
    """Minimal BaseDbStore backed by in-memory SQLite via aiosqlite.

    The underlying engine is bound to the event loop that first calls
    ``get_async_engine()``.  If the loop changes (e.g. between pytest-asyncio
    test functions), a fresh engine is created to avoid the
    ``Session and connector has to use same event loop`` error.
    """

    def __init__(self):
        self._engine: AsyncEngine | None = None
        self._loop: asyncio.AbstractEventLoop | None = None

    def get_async_engine(self) -> AsyncEngine:
        current = asyncio.get_event_loop()
        if self._engine is None or self._loop is not current:
            self._engine = create_async_engine(
                "sqlite+aiosqlite:///:memory:",
                echo=False,
            )
            self._loop = current
        return self._engine
