#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

from datetime import datetime, timezone
from typing import Dict, Any, Tuple

from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.context.memory_engine.manage.data_id_manager import DataIdManager
from jiuwen.context.memory_engine.store.sql_db_store import SqlDbStore


## DB-Based Message Management
class MessageManager:
    def __init__(self, sql_db_store: SqlDbStore, data_id_manager: DataIdManager):
        self.sql_db = sql_db_store
        self.message_table = "user_message"
        self.data_id = data_id_manager

    async def add(
        self,
        user_id: str,
        scope_id: str,
        content: str,
        role: str = None,
        session_id: str = None,
        timestamp: datetime = None,
    ) -> str:
        """add memory content to store"""
        message_id = str(await self.data_id.generate_next_id(user_id=user_id))
        if user_id is None:
            raise ValueError("Must provide user_id")
        if scope_id is None:
            raise ValueError("Must provide scope_id")
        if content is None:
            raise ValueError("Must provide content")
        time = datetime.now(timezone.utc) if not timestamp else timestamp
        data = {
            "message_id": message_id,
            "user_id": user_id or "",
            "session_id": session_id or "",
            "scope_id": scope_id or "",
            "role": role or "",
            "content": content,
            "timestamp": time,
        }
        await self.sql_db.write(self.message_table, data)
        return message_id

    async def get(
        self,
        user_id: str = None,
        scope_id: str = None,
        session_id: str = None,
        message_len: int = 10,
    ) -> list[Tuple[BaseMessage, datetime]]:
        """get the most recent messages with a specified id,
        where the number of messages if controlled by message_len
        """
        filters: Dict[str, Any] = {}
        if user_id is not None:
            filters["user_id"] = user_id
        if scope_id is not None:
            filters["scope_id"] = scope_id
        if session_id is not None:
            filters["session_id"] = session_id
        if message_len <= 0:
            raise ValueError("message_len Must bigger than zero")
        messages = await self.sql_db.get_with_sort(
            table=self.message_table, filters=filters, order="DESC", limit=message_len
        )
        return [
            (BaseMessage(**message), message["timestamp"])
            for message in reversed(messages)
        ]

    async def get_by_id(self, msg_id: str) -> Tuple[BaseMessage, datetime] | None:
        """get the message by its id"""
        filters: Dict[str, Any] = {"message_id": [msg_id]}
        messages = await self.sql_db.condition_get(
            table=self.message_table, conditions=filters
        )
        if not messages:
            return None
        return BaseMessage(**messages[0]), messages[0]["timestamp"]
