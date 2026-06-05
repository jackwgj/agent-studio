#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

import re
from typing import Union, List, Dict

from pydantic import BaseModel, Field


class TemplateFormatter(BaseModel):
    name: str = Field(default="", alias="name")
    content: Union[str, list[dict]] = Field(required=True, alias="content")
    replace_pattern: re.Pattern = re.compile(r"{{\s*(\w+)\s*}}")

    def __repr__(self):
        return f"TemplateFormatter(name={self.name}, content={self.content}"

    def __str__(self):
        return self.content

    def format(self, /, **kwargs) -> List[Dict]:
        """Format a prompt template"""

        def replacer(match):
            var_name = match.group(1)
            return str(kwargs.get(var_name, match.group(0)))

        if isinstance(self.content, str):
            return self.replace_pattern.sub(replacer, self.content)

        formatted = (
            [msg.copy() for msg in self.content]
            if isinstance(self.content, list)
            else [self.content.copy()]
        )

        for msg in formatted:
            msg["content"] = self.replace_pattern.sub(replacer, msg["content"])
        return formatted
