#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
Messages of Memory
"""

from typing import Union, Dict, List, Optional

from pydantic import BaseModel


class BaseMessage(BaseModel):
    """
    Base Message
    """

    type: str

    content: Union[str, List[Union[str, Dict]]]


class ConversationMessage(BaseMessage):
    """
    Conversation Message
    """

    type: str = "conversation"

    from_user: Optional[str] = None

    to_user: Optional[List[str]] = None


class AgentMessage(BaseMessage):
    """
    Agent Message
    """

    type: str = "agent"

    from_agent: Optional[str] = None

    to_agent: Optional[List[str]] = None
