#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from abc import ABC, abstractmethod
from typing import Dict

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode


class PromptStorage(ABC):
    """
    PromptStorage abstract class
    """

    def get_prompts(self) -> list[Dict]:
        """get prompts"""
        try:
            templates = self._load_prompts()
        except Exception as e:
            raise JiuWenBaseException(
                error_code=StatusCode.PROMPT_STORAGE_LOAD_ERROR.code,
                message=StatusCode.PROMPT_STORAGE_LOAD_ERROR.errmsg,
            ) from e
        return templates

    @abstractmethod
    def _load_prompts(self) -> list[Dict]:
        """load prompts"""
