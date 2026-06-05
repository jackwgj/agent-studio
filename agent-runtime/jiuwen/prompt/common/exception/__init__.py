#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""exception init"""

__all__ = ["NetworkError", "TemplateDuplicatedException", "TemplateNotFoundException"]

from jiuwen.prompt.common.exception.base import NetworkError
from jiuwen.prompt.common.exception.template import (
    TemplateDuplicatedException,
    TemplateNotFoundException,
)
