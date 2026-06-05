#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
placeholder type
"""

from typing import Optional

from pydantic import BaseModel, Field

PLACE_HOLDER_VALUE_MIN_LIMIT = 1
PLACE_HOLDER_VALUE_MAX_LIMIT = 200
PLACE_HOLDER_PREFIX_MIN_LIMIT = 1
PLACE_HOLDER_PREFIX_MAX_LIMIT = 200
PLACE_HOLDER_DESCRIPTION_MIN_LIMIT = 0
PLACE_HOLDER_DESCRIPTION_MAX_LIMIT = 200
PLACE_HOLDER_TYPE_MIN_LIMIT = 0
PLACE_HOLDER_TYPE_MAX_LIMIT = 64


class PlaceholderData(BaseModel):
    """Placeholder data for template variables."""

    value: str = Field(
        min_length=PLACE_HOLDER_VALUE_MIN_LIMIT, max_length=PLACE_HOLDER_VALUE_MAX_LIMIT
    )
    prefix: str = Field(
        min_length=PLACE_HOLDER_PREFIX_MIN_LIMIT,
        max_length=PLACE_HOLDER_PREFIX_MAX_LIMIT,
    )
    description: Optional[str] = Field(
        default="",
        min_length=PLACE_HOLDER_DESCRIPTION_MIN_LIMIT,
        max_length=PLACE_HOLDER_DESCRIPTION_MAX_LIMIT,
    )
    type: Optional[str] = Field(
        default="中文",
        min_length=PLACE_HOLDER_TYPE_MIN_LIMIT,
        max_length=PLACE_HOLDER_TYPE_MAX_LIMIT,
    )
    inserted: Optional[bool] = Field(default=False)

    def format(self):
        """Generate formatted placeholder string for templates."""
        if self.inserted:
            return f"{self.value}"

        return f"\n\n## {self.prefix} \n" + "{{" + f"{self.value}" + "}}"
