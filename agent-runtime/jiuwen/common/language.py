#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
language
"""

import contextvars
import os

from jiuwen.common.configs.env_constants import JIUWEN_DEFAULT_LANGUAGE_TYPE_KEY

_coroutine_language_instance: contextvars.ContextVar[str | None] = (
    contextvars.ContextVar("language", default="")
)


class Language:
    """jiuwen language"""

    @staticmethod
    def get_default_language() -> str:
        """get default language"""
        return os.environ.get(JIUWEN_DEFAULT_LANGUAGE_TYPE_KEY, "")

    @staticmethod
    def set_context_language(language: str):
        """set thread local language"""
        _coroutine_language_instance.set(language)

    @staticmethod
    def get_context_language() -> str:
        """get thread local language"""
        return _coroutine_language_instance.get()
