#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from typing import Optional, List, Dict, Tuple

from jiuwen.prompt.selector.memory.prompt_library import ROLE_LOOKUP
from jiuwen.prompt.selector.memory.prompt_library import (
    TOOLCALL_TEMPLATE,
    TOOLCALL_SEP_FMT,
    TOOLCALL_FAILURE,
)


def create_msg(role: str, content: str, *args, **kwargs) -> Dict:
    """Shorthand for creating a message dictionary"""
    return dict(*args, role=role, content=str(content), **kwargs)


def sys_msg(content: str, *args, **kwargs) -> Dict:
    """Shorthand for creating a system message dictionary"""
    return create_msg("system", content, *args, **kwargs)


def usr_msg(content: str, *args, **kwargs) -> Dict:
    """Shorthand for creating a user message dictionary"""
    return create_msg("user", content, *args, **kwargs)


def llm_msg(content: str, *args, **kwargs) -> Dict:
    """Shorthand for creating an assistant message dictionary"""
    return create_msg("assistant", content, *args, **kwargs)


def fetch_all_tool_results(messages: List[Dict]) -> Dict[str, str]:
    """Collect all tool calling results and return a dictionary mapping tool_call_id to result"""
    return {
        msg["tool_call_id"]: msg["content"] for msg in messages if msg["role"] == "tool"
    }


def conv2str(
    messages: List[Dict],
    role_sep: str = "\n",
    tool_sep: Optional[Tuple[str, str]] = None,
    language: str = "zh_CN",
) -> str:
    """Format conversation in List[Dict] format to a single string"""
    conv_str_list = []  # list of conversation messages
    str_builder = ""  # buffer for string-building

    # Get corresponding string template by language
    if tool_sep is None:
        tool_sep = TOOLCALL_SEP_FMT[language]

    # Get tool call results
    tool_call_results = fetch_all_tool_results(messages)

    # Convert each message
    for msg in messages:
        if msg["role"] == "user":
            if str_builder:
                conv_str_list.append(str_builder)
                str_builder = ""
            conv_str_list.append(
                f"{ROLE_LOOKUP[language]['user']}:\n{msg['content']}\n"
            )
        elif msg["role"] == "assistant":
            if not str_builder:
                str_builder = f"{ROLE_LOOKUP[language]['assistant']}:\n"
                content = msg["content"]
                if content:
                    str_builder += content + "\n"
            else:
                str_builder += f"{msg['content']}\n"
            toolcalls = msg.get("tool_calls")
            if toolcalls:  # handle both None and []
                str_builder += tool_sep[0]
                for toolcall in toolcalls:
                    toolcall_id = toolcall["id"]
                    toolcall_ret = tool_call_results.get(
                        toolcall_id, TOOLCALL_FAILURE[language]
                    )
                    toolcall_func = toolcall["function"]
                    str_builder += TOOLCALL_TEMPLATE[language].format(
                        toolcall=toolcall_func["name"],
                        args=toolcall_func["arguments"],
                        result=toolcall_ret,
                    )
                str_builder += tool_sep[1]

    # Empty string-building buffer
    if str_builder:
        conv_str_list.append(str_builder)

    return role_sep.join(conv_str_list)
