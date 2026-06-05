#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""Tool Refiner class definition"""

from typing import Optional, List, Dict

from jiuwen.prompt.common.selector.tool_selector import ToolSelector
from pydantic import BaseModel


class ToolRefiner(BaseModel):
    """Tool refiner that selects relevant tools based on user query."""

    selector: Optional[ToolSelector]

    def __init__(self, selector: ToolSelector = None):
        """Initialize with optional tool selector."""
        if not selector:
            selector = ToolSelector()
        super().__init__(selector=selector)

    def run(self, query: str, tools: List[Dict]) -> List[Dict]:
        """refiner tools

        Args:
            query: user query
            tools: tool api list

        Returns:
            refined tools
        """
        refined_tools = self.selector.select(query=query, tools=tools)
        return refined_tools
