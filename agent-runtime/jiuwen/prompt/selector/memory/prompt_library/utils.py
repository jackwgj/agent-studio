#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

from jiuwen.prompt import TemplateManager
from jiuwen.prompt.selector.memory.prompt_library.assemble_prompt import (
    TemplateFormatter,
)


def get_template_formatter(template_name: str) -> TemplateFormatter:
    """Get a template formatter for given template name"""
    t = TemplateManager().get(template_name)
    return TemplateFormatter(name=t.name, content=t.content)
