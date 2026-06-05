import json
import logging
import os
from typing import Dict

from memory_service.i18n.i18n_constant import Locale, normalize_locale
from memory_service.utils.request_context_util import language_context

logger = logging.getLogger(__name__)


class I18nUtil:
    def __init__(self):
        current_dir = os.path.dirname(os.path.abspath(__file__))
        self.locales_dir = os.path.join(current_dir, "locales")
        self._translations: Dict[Locale, Dict[str, str]] = {}
        self._load_translations()

    def _load_translations(self):
        """加载所有翻译文件"""
        if not os.path.exists(self.locales_dir):
            logger.error("There is no locale message for i18n.")
            raise FileNotFoundError(f"Locales directory not found: {self.locales_dir}")

        for filename in os.listdir(self.locales_dir):
            if filename.endswith(".json"):
                lang_code = filename.split(".")[0]
                filepath = os.path.join(self.locales_dir, filename)
                locale = normalize_locale(lang_code)
                with open(filepath, "r", encoding="utf-8") as f:
                    self._translations[locale] = json.load(f)

    def get_message(self, message_code: str, *args) -> str:
        """
        获取国际化消息

        Args:
            message_code: 消息编码
            *args: 位置参数，用于替换占位符 {0}, {1} 等

        Returns:
            str: 国际化后的消息
        """
        # 获取当前语言
        current_lang = language_context.get()

        translations = self._translations.get(current_lang)

        # 获取消息模板
        message_template = translations.get(message_code, message_code)

        # 替换占位符
        try:
            formatted_message = message_template.format(*args)
        except (IndexError, KeyError):
            # 如果参数不匹配，返回原始消息模板
            logger.error(
                f"Parameter mismatch for i18n message template: {message_template}, params: {args}"
            )
            formatted_message = message_template

        return formatted_message


# 创建全局实例
i18n_util = I18nUtil()
