#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""
agentBuilder chain StoreType
"""

import enum

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode


class StoreType(enum.Enum):
    """Template Store Type enum define here"""

    IN_MEMORY = "in_memory"

    @classmethod
    def match(cls, value):
        """match function"""
        for item in list(cls):
            if item.value == value:
                return item
        raise JiuWenBaseException(
            error_code=StatusCode.PROMPT_STORAGE_TYPE_NOT_MATCH_ERROR.code,
            message=StatusCode.PROMPT_STORAGE_TYPE_NOT_MATCH_ERROR.errmsg.format(
                error_msg=f"store type name: {value}"
            ),
        )
