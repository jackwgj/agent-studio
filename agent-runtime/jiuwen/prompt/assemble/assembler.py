#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""for assembling prompt template"""

from __future__ import annotations

from typing import List

from jiuwen.prompt.agent.assemble.assembler import PromptAssembler

DEFAULT_TRUNCATE_LEN = 150


class PromptAssemblerForEI(PromptAssembler):
    @staticmethod
    def format_history(history: List[dict]) -> str:
        """format_history_as_string"""
        history_str_list = []
        for message in history:
            if message["role"] == "assistant":
                history_str_list.append(f"{message['role']}: {message.get('content')}")
            else:
                history_str_list.append(f"{message['role']}: {message.get('content')}")

        return "\n".join(history_str_list)
