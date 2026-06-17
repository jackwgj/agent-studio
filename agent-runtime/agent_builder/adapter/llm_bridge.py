# coding=utf-8
"""
LLM Bridge: delegates to agent-core (openjiuwen) Model for LLM invocation.

Provides OpenAICompatibleService that uses agent-core's Model class
(which supports OpenAI, DashScope, DeepSeek, SiliconFlow, etc.)
instead of raw HTTP requests.

agent-core capabilities used:
  - openjiuwen.core.foundation.llm.Model
  - openjiuwen.core.foundation.llm.ModelClientConfig
  - openjiuwen.core.foundation.llm.ModelRequestConfig

Sync/async bridge: agent-core's Model is async-only (invoke/stream).
This adapter bridges to sync callers via asyncio + ThreadPoolExecutor.
"""

import asyncio
import concurrent.futures
import traceback

from openjiuwen.core.foundation.llm import (
    Model,
    ModelClientConfig,
    ModelRequestConfig,
)

from agent_builder.adapter.exception_bridge import JiuWenBaseException
from agent_builder.adapter.logger_bridge import logger


# Standard HTTP headers that should NOT be forwarded to the LLM API
_EXCLUDED_HEADERS = {
    'host', 'connection', 'content-length', 'content-type',
    'accept', 'accept-encoding', 'user-agent', 'transfer-encoding',
}


def _run_async(coro):
    """Run an async coroutine from a sync context.

    Handles both cases: no running event loop (direct asyncio.run)
    and an already-running loop (offload to a thread pool).
    """
    try:
        loop = asyncio.get_running_loop()
    except RuntimeError:
        loop = None

    if loop and loop.is_running():
        with concurrent.futures.ThreadPoolExecutor(max_workers=1) as pool:
            return pool.submit(asyncio.run, coro).result()
    else:
        return asyncio.run(coro)


def _collect_async_gen(async_gen):
    """Collect all items from an async generator into a list (sync)."""
    async def _collect():
        return [item async for item in async_gen]
    return _run_async(_collect())


def _filter_headers(headers: dict) -> dict:
    """Filter out standard HTTP headers, keeping only custom ones."""
    if not headers:
        return {}
    return {
        k: v for k, v in headers.items()
        if k.lower() not in _EXCLUDED_HEADERS
    }


def _normalize_api_base(url: str) -> str:
    """Normalize the API base URL for agent-core's ModelClientConfig.

    agent-core expects the base URL without the /chat/completions path.
    Strips trailing /chat/completions if present, keeping /v1.
    e.g. https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
      →  https://dashscope.aliyuncs.com/compatible-mode/v1
    """
    url = url.rstrip("/")
    if url.endswith("/chat/completions"):
        url = url[: -len("/chat/completions")]
    return url


class OpenAICompatibleService:
    """
    LLM service using agent-core's Model class.

    Replaces the previous direct-HTTP implementation. Now delegates to
    agent-core's Model which internally uses the appropriate model client
    (OpenAI, DashScope, DeepSeek, SiliconFlow, etc.) based on the provider.

    Configuration comes from LLMModelInfo fields:
    - url: API base URL (e.g. "https://api.deepseek.com")
    - api_key: API key / bearer token
    - model: model name (e.g. "deepseek-chat")
    - temperature, top_p: sampling parameters
    - headers: extra request headers (filtered)
    - timeout: request timeout in seconds
    """

    def __init__(self, model_info=None):
        self.model_info = model_info
        self._model = None

        if model_info:
            api_base = getattr(model_info, "url", "") or ""
            api_key = getattr(model_info, "api_key", "") or ""
            model_name = getattr(model_info, "model", "") or ""
            temperature = getattr(model_info, "temperature", 0.1)
            top_p = getattr(model_info, "top_p", 0.1)
            extra_headers = getattr(model_info, "headers", {}) or {}
            timeout = getattr(model_info, "timeout", 120)

            if not api_base:
                raise JiuWenBaseException(
                    error_code=-1,
                    message="LLM API base URL is not configured in modelInfo.url",
                )

            filtered = _filter_headers(extra_headers)
            normalized_base = _normalize_api_base(api_base)

            client_config = ModelClientConfig(
                client_provider="OpenAI",
                api_key=api_key,
                api_base=normalized_base,
                timeout=float(timeout),
                verify_ssl=False,
                custom_headers=filtered if filtered else None,
            )
            request_config = ModelRequestConfig(
                model=model_name,
                temperature=temperature,
                top_p=top_p,
            )
            self._model = Model(
                model_client_config=client_config,
                model_config=request_config,
            )

    def full_chat(self, messages: list, extra_info: dict = None) -> dict:
        """
        Non-streaming chat completion via agent-core Model.invoke().

        Returns:
            dict with code, message, data keys
        """
        try:
            if self._model is None:
                return dict(code=-1, message="Model not initialized", data="")

            response = _run_async(self._model.invoke(messages))
            content = response.content if hasattr(response, "content") else str(response)
            return dict(code=0, message="success", data=content)

        except JiuWenBaseException as e:
            logger.warning(f"JiuWenBaseException in full_chat, propagating to caller: {e}")
            raise
        except Exception as e:
            traceback_msg = traceback.format_exc()
            logger.error(f"LLM API call failed: {e}, traceback: {traceback_msg}")
            return dict(
                code=-1,
                message=f"LLM API call failed: {str(e)}",
                data="",
            )

    def streaming_chat(self, messages: list, extra_info: dict = None):
        """
        Streaming chat completion (sync generator).

        Bridges agent-core's async stream() to a sync generator.
        Yields dicts with code, message, data keys for each chunk.
        """
        try:
            if self._model is None:
                yield dict(code=-1, message="Model not initialized", data="")
                return

            # Collect all chunks from the async generator, then yield as sync
            chunks = _collect_async_gen(self._model.stream(messages))
            for chunk in chunks:
                content = chunk.content if hasattr(chunk, "content") else str(chunk)
                if content:
                    yield dict(code=0, message="success", data=content)

        except JiuWenBaseException as e:
            logger.warning(f"JiuWenBaseException in streaming_chat, propagating to caller: {e}")
            raise
        except Exception as e:
            logger.error(f"LLM streaming call failed: {e}")
            yield dict(
                code=-1,
                message=f"LLM streaming failed: {str(e)}",
                data="",
            )

    async def astreaming_chat(self, messages: list, extra_info: dict = None):
        """
        Async streaming chat completion (async generator).

        Directly delegates to agent-core's Model.stream().
        """
        try:
            if self._model is None:
                yield dict(code=-1, message="Model not initialized", data="")
                return

            async for chunk in self._model.stream(messages):
                content = chunk.content if hasattr(chunk, "content") else str(chunk)
                if content:
                    yield dict(code=0, message="success", data=content)

        except JiuWenBaseException as e:
            logger.warning(f"JiuWenBaseException in astreaming_chat, propagating to caller: {e}")
            raise
        except Exception as e:
            logger.error(f"LLM async streaming call failed: {e}")
            yield dict(
                code=-1,
                message=f"LLM async streaming failed: {str(e)}",
                data="",
            )
