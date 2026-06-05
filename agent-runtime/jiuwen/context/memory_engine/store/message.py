#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
from jiuwen.context.memory_engine.store.base_db_store import BaseDbStore
from sqlalchemy import Column, String
from sqlalchemy.orm import declarative_mixin, declarative_base

Base = declarative_base()


@declarative_mixin
class MessageMixin:
    message_id = Column(String(64), primary_key=True)
    user_id = Column(String(64), nullable=False)
    scope_id = Column(String(64), nullable=False)
    content = Column(String(4096), nullable=False)
    session_id = Column(String(64), nullable=True)
    role = Column(String(32), nullable=True)
    timestamp = Column(String(32), nullable=True)


class UserMessage(MessageMixin, Base):
    __tablename__ = "user_message"


def _create_user_message_table(sync_conn):
    Base.metadata.create_all(sync_conn, tables=[UserMessage.__table__], checkfirst=True)


async def create_tables(db_store: BaseDbStore):
    """
    create tables for db_store
    """
    # MySQL table
    async with db_store.get_async_engine().begin() as conn:
        await conn.run_sync(_create_user_message_table)
