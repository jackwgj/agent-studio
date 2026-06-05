#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from typing import List, Dict

from jiuwen.prompt.selector.memory.prompt_library import (
    PROMPT_GENERATE_SUMMARY,
    PROMPT_GENERATE_SUMMARY_NO_TOPICS,
)
from jiuwen.prompt.selector.memory.prompt_library import (
    PROMPT_UPDATE_SUMMARY,
    PROMPT_UPDATE_SUMMARY_NO_TOPICS,
)


def construct_formatted_summary_prompt(
    conversation: str, topics: str, language: str = "zh_CN", summary: str = ""
) -> List[Dict]:
    """Construct summary prompt for querying LLM"""
    if summary:
        if topics:
            prompt_template = PROMPT_UPDATE_SUMMARY[language]
            prompt = prompt_template.format(
                dialog=conversation, topics=topics, existing_summary=summary
            )
        else:
            prompt_template = PROMPT_UPDATE_SUMMARY_NO_TOPICS[language]
            prompt = prompt_template.format(
                dialog=conversation, existing_summary=summary
            )
    else:
        if topics:
            prompt_template = PROMPT_GENERATE_SUMMARY[language]
            prompt = prompt_template.format(dialog=conversation, topics=topics)
        else:
            prompt_template = PROMPT_GENERATE_SUMMARY_NO_TOPICS[language]
            prompt = prompt_template.format(dialog=conversation)
    return prompt
