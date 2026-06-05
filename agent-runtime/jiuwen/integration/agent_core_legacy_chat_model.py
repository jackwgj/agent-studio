"""Legacy chat endpoint model used by the agent-core model layer."""

from __future__ import annotations

import json
from typing import Any, AsyncIterable

import httpx
from agent_runtime.common.config import settings
from jiuwen.common.security.cryptor import Crypt
from openjiuwen.core.foundation.llm.schema.message import (
    AssistantMessage,
    UsageMetadata,
)
from openjiuwen.core.foundation.llm.schema.message_chunk import AssistantMessageChunk


class AgentCoreLegacyChatModel:
    """Call jiuwen's legacy full chat endpoint without OpenAI base URL rewriting."""

    def __init__(
        self,
        *,
        api_base: str,
        api_key: str,
        model_name: str,
        temperature: float | None = None,
        top_p: float | None = None,
        timeout: float | None = None,
        verify_ssl: bool = False,
        ssl_cert: str | None = None,
    ) -> None:
        self.api_base = api_base
        self.api_key = api_key
        self.model_name = model_name
        self.temperature = temperature
        self.top_p = top_p
        self.timeout = timeout if timeout is not None else settings.llm.timeout
        self.verify_ssl = verify_ssl
        self.ssl_cert = ssl_cert

    async def invoke(
        self,
        messages: list[Any],
        *,
        tools: list[Any] | None = None,
        temperature: float | None = None,
        top_p: float | None = None,
        max_tokens: int | None = None,
        stop: Any = None,
        model: str | None = None,
        timeout: float | None = None,
        **kwargs: Any,
    ) -> AssistantMessage:
        """Invoke the legacy chat endpoint and return an openjiuwen message."""
        payload = self._build_payload(
            messages,
            tools=tools,
            temperature=temperature,
            top_p=top_p,
            max_tokens=max_tokens,
            stop=stop,
            model=model,
            stream=False,
            extra_params=kwargs,
        )
        async with httpx.AsyncClient(
            verify=self._httpx_verify(), timeout=timeout or self.timeout
        ) as client:
            response = await client.post(
                self.api_base, json=payload, headers=self._headers()
            )
        response.raise_for_status()
        return self._parse_assistant_message(response.json())

    async def stream(
        self,
        messages: list[Any],
        *,
        tools: list[Any] | None = None,
        temperature: float | None = None,
        top_p: float | None = None,
        max_tokens: int | None = None,
        stop: Any = None,
        model: str | None = None,
        timeout: float | None = None,
        **kwargs: Any,
    ) -> AsyncIterable[AssistantMessageChunk]:
        """Stream from the legacy chat endpoint using OpenAI-compatible SSE frames."""
        payload = self._build_payload(
            messages,
            tools=tools,
            temperature=temperature,
            top_p=top_p,
            max_tokens=max_tokens,
            stop=stop,
            model=model,
            stream=True,
            extra_params=kwargs,
        )
        async with httpx.AsyncClient(
            verify=self._httpx_verify(), timeout=timeout or self.timeout
        ) as client:
            async with client.stream(
                "POST", self.api_base, json=payload, headers=self._headers()
            ) as response:
                response.raise_for_status()
                async for line in response.aiter_lines():
                    chunk = self._parse_stream_line(line)
                    if chunk is not None:
                        yield chunk

    def _headers(self) -> dict[str, str]:
        app_code = self.api_key
        if app_code:
            try:
                app_code = Crypt().decrypt(app_code)
            except Exception:
                app_code = self.api_key
        return {
            "x-apig-appcode": app_code or "",
            "Content-Type": "application/json",
        }

    def _httpx_verify(self) -> bool | str:
        if self.verify_ssl and self.ssl_cert:
            return self.ssl_cert
        return self.verify_ssl

    def _build_payload(
        self,
        messages: list[Any],
        *,
        tools: list[Any] | None,
        temperature: float | None,
        top_p: float | None,
        max_tokens: int | None,
        stop: Any,
        model: str | None,
        stream: bool,
        extra_params: dict[str, Any],
    ) -> dict[str, Any]:
        payload = {
            "model": model or self.model_name,
            "messages": [self._message_to_dict(message) for message in messages],
            "stream": stream,
        }
        request_temperature = (
            temperature if temperature is not None else self.temperature
        )
        request_top_p = top_p if top_p is not None else self.top_p
        if request_temperature is not None:
            payload["temperature"] = request_temperature
        if request_top_p is not None:
            payload["top_p"] = request_top_p
        if max_tokens is not None:
            payload["max_tokens"] = max_tokens
        if stop is not None:
            payload["stop"] = stop
        if tools:
            payload["tools"] = [self._tool_to_dict(tool) for tool in tools]
        payload.update(
            {
                key: self._to_json_safe(value)
                for key, value in extra_params.items()
                if value is not None and not callable(value)
            }
        )
        return self._to_json_safe(payload)

    @staticmethod
    def _message_to_dict(message: Any) -> dict[str, Any]:
        if isinstance(message, dict):
            return AgentCoreLegacyChatModel._to_json_safe(dict(message))
        message_dict = {
            "role": getattr(message, "role", "user"),
            "content": getattr(message, "content", ""),
        }
        name = getattr(message, "name", None)
        if name is not None:
            message_dict["name"] = name
        tool_calls = getattr(message, "tool_calls", None)
        if tool_calls:
            message_dict["tool_calls"] = tool_calls
        return AgentCoreLegacyChatModel._to_json_safe(message_dict)

    @staticmethod
    def _tool_to_dict(tool: Any) -> dict[str, Any]:
        if isinstance(tool, dict):
            if tool.get("type") == "function" and "function" in tool:
                return AgentCoreLegacyChatModel._to_json_safe(dict(tool))
            return AgentCoreLegacyChatModel._to_json_safe(
                {"type": "function", "function": dict(tool)}
            )
        tool_dict = {
            "type": "function",
            "function": {
                "name": getattr(tool, "name", ""),
                "description": getattr(tool, "description", ""),
                "parameters": getattr(tool, "parameters", {}) or {},
            },
        }
        return AgentCoreLegacyChatModel._to_json_safe(tool_dict)

    @staticmethod
    def _to_json_safe(value: Any) -> Any:
        if callable(value):
            return None
        if isinstance(value, dict):
            result = {}
            for key, item in value.items():
                if not callable(item):
                    safe_value = AgentCoreLegacyChatModel._to_json_safe(item)
                    if safe_value is not None:
                        result[key] = safe_value
            return result
        if isinstance(value, (list, tuple, set)):
            result = []
            for item in value:
                if not callable(item):
                    safe_item = AgentCoreLegacyChatModel._to_json_safe(item)
                    if safe_item is not None:
                        result.append(safe_item)
            return result
        if hasattr(value, "model_dump"):
            return AgentCoreLegacyChatModel._to_json_safe(
                value.model_dump(exclude_none=True)
            )
        if hasattr(value, "dict"):
            return AgentCoreLegacyChatModel._to_json_safe(value.dict(exclude_none=True))
        try:
            json.dumps(value, ensure_ascii=False)
            return value
        except TypeError:
            return str(value)

    def _parse_assistant_message(self, data: dict[str, Any]) -> AssistantMessage:
        choice = (data.get("choices") or [{}])[0]
        message = choice.get("message") or {}
        content = message.get("content")
        if content is None:
            content = (
                data.get("content")
                or data.get("result")
                or json.dumps(data, ensure_ascii=False)
            )
        usage = data.get("usage") or {}
        message_values = {
            "content": content,
            "finish_reason": choice.get("finish_reason") or "stop",
            "usage_metadata": self._usage_metadata(usage),
        }
        if message.get("reasoning_content") is not None:
            message_values["reasoning_content"] = message.get("reasoning_content")
        if message.get("tool_calls"):
            message_values["tool_calls"] = message.get("tool_calls")
        return AssistantMessage(**message_values)

    def _parse_stream_line(self, line: str) -> AssistantMessageChunk | None:
        line = line.strip()
        if not line:
            return None
        if line.startswith("data:"):
            line = line[5:].strip()
        if line == "[DONE]":
            return None
        data = json.loads(line)
        choice = (data.get("choices") or [{}])[0]
        delta = choice.get("delta") or choice.get("message") or {}
        return AssistantMessageChunk(
            content=delta.get("content") or "",
            tool_calls=delta.get("tool_calls"),
            usage_metadata=self._usage_metadata(data.get("usage") or {}),
            finish_reason=choice.get("finish_reason") or "null",
        )

    def _usage_metadata(self, usage: dict[str, Any]) -> UsageMetadata:
        return UsageMetadata(
            model_name=self.model_name,
            input_tokens=usage.get("prompt_tokens") or usage.get("input_tokens") or 0,
            output_tokens=usage.get("completion_tokens")
            or usage.get("output_tokens")
            or 0,
            total_tokens=usage.get("total_tokens") or 0,
        )
