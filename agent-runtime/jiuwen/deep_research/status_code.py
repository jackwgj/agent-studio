#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Definition of DeepResearch response status codes"""

from enum import Enum

from jiuwen.common.language import Language

_OVERWRITE_STATUS_CODE: dict = {}


class DSStatusCode(Enum):
    """DeepResearch状态码枚举类"""

    LLM_DR_IR_VALIDATE_ERROR = (
        100030,
        "DeepResearch ir field validate error: {error_msg}",
    )

    @property
    def code(self):
        """获取状态码"""
        return self.value[0]

    @property
    def errmsg(self):
        """获取状态码信息"""
        return self._get_errmsg()

    def _get_errmsg(self):
        language = Language.get_context_language()
        if not language:
            return self.value[1]
        overwrite_errmsg = _OVERWRITE_STATUS_CODE.get(language, {}).get(
            self.value[0], ""
        )
        return overwrite_errmsg if overwrite_errmsg else self.value[1]
