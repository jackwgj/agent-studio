#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.

import json
import re
from typing import Any, Union

from jiuwen.common.llm_service.messages import AIMessage
from jiuwen.common.log.base import logger


class LLMJsonOutputParser:
    """
    JsonOutputParser
    """

    async def parse(self, llm_output: Union[str, AIMessage]) -> Any:
        """
        parse
        """
        if isinstance(llm_output, AIMessage):
            text = llm_output.content
        elif isinstance(llm_output, str):
            text = llm_output
        else:
            logger.warning("Unsupported llm_output type for parse.")
            return None

        if not text:
            return None

        match = re.search(r"```json\n(.*?)```", text, re.DOTALL)
        if match:
            json_str = match.group(1).strip()
        else:
            json_str = text.strip()

        try:
            parsed_data = json.loads(json_str)
            return parsed_data
        except json.JSONDecodeError as e:
            logger.error(f"Failed to decode JSON from LLM output, error: {e.msg}")
            return None
        except Exception as e:
            logger.error(
                f"An unexpected error occurred during JSON parsing, error: {str(e)}"
            )
            return None
