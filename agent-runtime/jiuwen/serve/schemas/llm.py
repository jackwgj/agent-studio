#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
request/response definition of llm server.
"""

__all__ = ("FunctionCall", "Message", "RequestBody")

from typing import List

from jiuwen.orchestration.model_io.types import ModelConfig
from pydantic import BaseModel as pydantic_BaseModel, Field, model_validator


class FunctionCall(pydantic_BaseModel):
    """function call pydantic model"""

    name: str
    arguments: str
    id: str = None


class Message(pydantic_BaseModel):
    """message pydantic model"""

    # pydantic 1.10.5 under python 3.9 does not support typing.Literal
    role: str = Field(pattern=r"^((assistant)|(function)|(system)|(user))$")
    content: str = None
    name: str = None
    function_calls: List[FunctionCall] = Field(None, alias="functionCalls")
    id: str = None

    @model_validator(mode="after")
    def _check_content(self):
        role = self.role
        content = self.content
        function_calls = self.function_calls
        function_id = self.id
        if not content and not function_calls:
            raise ValueError(
                "Field 'content' and 'function_calls' cannot be empty at the same time."
            )
        if (role != "assistant") and function_calls:
            raise ValueError(
                "Only a message of role='assistant' can has function_calls."
            )
        if role != "function":
            if function_id:
                raise ValueError("Only a message of role='function' can has id.")
            self.id = None  # noqa
        return self


class RequestBody(pydantic_BaseModel):
    """request body pydantic model"""

    config: ModelConfig
    query: List[Message] = Field(min_items=1)
    user_id: str = Field(None, alias="userId")
    message_id: str = Field(None, alias="messageId")
