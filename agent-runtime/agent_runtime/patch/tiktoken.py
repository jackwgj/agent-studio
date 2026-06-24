# -*- coding: utf-8 -*-
"""
Tiktoken 禁用 Patch

完全禁用 tiktoken，使用 len(text)//4 估算 token 数量。
避免远程下载 BPE 词汇表文件，消耗大量时间。

使用：
    from agent_runtime.patch.tiktoken import apply
    apply()
"""
from openjiuwen.core.common.logging import logger


def apply() -> bool:
    """应用 TiktokenCounter 禁用 patch，直接使用 fallback"""
    try:
        from openjiuwen.core.context_engine.token.tiktoken_counter import TiktokenCounter

        if getattr(TiktokenCounter, "_patched", False):
            return True

        def patched_init(self, model: str = "gpt-4"):
            self._model = model
            self._enc = None
            self._fallback_warning_printed = False

        def patched_count(self, text: str, *, model: str = "", **kwargs):
            if not self._fallback_warning_printed:
                self._fallback_warning_printed = True
                logger.debug("Tiktoken disabled, using len//4 fallback for token counting")
            return len(text) // 4

        TiktokenCounter.__init__ = patched_init
        TiktokenCounter.count = patched_count
        TiktokenCounter._patched = True

        logger.info("TiktokenCounter disabled (using len//4 fallback)")
        return True

    except ImportError as e:
        logger.debug("Could not apply TiktokenCounter disable patch (module not imported yet): %s", e)
        return False
    except Exception as e:
        logger.warning("Failed to apply TiktokenCounter disable patch: %s", e)
        return False