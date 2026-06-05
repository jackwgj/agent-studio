#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
Runnable
"""

import json
from typing import Dict, Any

from jiuwen.common.llm_service.messages import (
    BaseMessage,
    HumanMessage,
    AIMessage,
    SystemMessage,
    FunctionMessage,
)


def convert_message_to_dict(message: BaseMessage) -> Dict[str, Any]:
    """Convert a message to a dictionary.

    Args:
        message: The message.

    Returns:
        The dictionary.
    """
    role_str = "role"
    content_str = "content"
    function_str = "function"
    if isinstance(message, HumanMessage):
        message_dict = {role_str: "user", content_str: message.content}
    elif isinstance(message, AIMessage):
        message_dict = {role_str: "assistant", content_str: message.content}
        if message.tool_calls:
            message_dict.update(
                dict(
                    function_call=dict(
                        name=message.tool_calls.name,
                        arguments=json.dumps(
                            message.tool_calls.args, ensure_ascii=False
                        ),
                    )
                )
            )
    elif isinstance(message, SystemMessage):
        message_dict = {role_str: "system", content_str: message.content}
    elif isinstance(message, FunctionMessage):
        message_dict = {
            role_str: function_str,
            content_str: message.content,
            "name": message.name,
        }
    else:
        raise ValueError("Got unknown type Message.")
    return message_dict
