"""
自定义 LLM 聊天模型实现
支持调用第三方模型的 API 获取对话返回信息
"""

import json
import logging
from typing import List, Any, Iterator, Optional

import aiohttp
from jiuwen.common.llm_service.language_model.base import (
    BaseChatModel,
    LanguageModelOutput,
)
from jiuwen.common.llm_service.messages import BaseMessage, AIMessage, Tool
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


class AgentBuilderMemoryLLM(BaseModel, BaseChatModel):
    """用于记忆提取的联调模型"""

    model_name: str
    api_key: Optional[str] = Field(default="", alias="api_key")
    api_base: Optional[str] = Field(default="", alias="api_base")
    temperature: float = Field(default=0.7)
    top_p: float = Field(default=0.8)
    streaming: bool = Field(default=False, alias="stream")
    max_tokens: int = Field(default=2048)
    timeout: int = Field(default=60)
    custom_header: Optional[dict] = Field(default=None, alias="custom_header")

    # HTTP 客户端（延迟初始化）
    _client: aiohttp.ClientSession = None

    def __init__(self, **data):
        super().__init__(**data)
        # 延迟初始化HTTP客户端，避免在同步上下文中创建
        self._client = None

    async def _initialize_client(self):
        """懒初始化HTTP客户端"""
        if self._client is None:
            # 创建 TCP 连接器，忽略SSL证书验证
            connector = aiohttp.TCPConnector(verify_ssl=False)

            self._client = aiohttp.ClientSession(
                timeout=aiohttp.ClientTimeout(total=self.timeout), connector=connector
            )

    async def _ensure_client(self):
        """确保HTTP客户端已初始化"""
        if self._client is None:
            await self._initialize_client()

    async def _close_client(self):
        """关闭 HTTP 客户端"""
        if self._client:
            await self._client.close()

    def _convert_messages(self, messages: List[BaseMessage]) -> list:
        """将消息格式转换为 API 所需的格式"""
        formatted_messages = []

        for message in messages:
            formatted_messages.append(
                {"role": message.type, "content": message.content}
            )

        return formatted_messages

    async def _chat(
        self,
        messages: List[BaseMessage],
        tools: List[Tool] = None,
        **kwargs: Any,
    ) -> LanguageModelOutput:
        """调用聊天 API 获取返回信息"""

        # 确保HTTP客户端已初始化
        await self._ensure_client()

        # 构建请求数据
        payload = {
            "model": self.model_name,
            "messages": self._convert_messages(messages),
            "temperature": self.temperature,
            "top_p": self.top_p,
            "max_tokens": self.max_tokens,
            "stream": False,
        }

        try:
            # 发送 POST 请求
            async with self._client.post(
                f"{self.api_base}/chat/completions",
                json=payload,
                headers=self._build_headers(),
            ) as response:
                response.raise_for_status()

                # 解析响应
                response_data = await response.json()
                content = response_data["choices"][0]["message"]["content"]

                return AIMessage(content=content)

        except aiohttp.ClientError as e:
            logger.error("HTTP error occurred: %s", e, exc_info=True)
            return AIMessage(
                content="Sorry, I encountered an error while processing your request."
            )
        except json.JSONDecodeError as e:
            logger.error("JSON decode error: %s", e, exc_info=True)
            return AIMessage(
                content="Sorry, I received an invalid response from the model."
            )
        except Exception as e:
            logger.error("Unexpected error: %s", e, exc_info=True)
            # 直接抛出异常，避免后续JSON解析错误
            raise e

    async def _achat(
        self,
        messages: List[BaseMessage],
        tools: List[Tool] = None,
        **kwargs: Any,
    ) -> LanguageModelOutput:
        """异步聊天方法，确保正确处理异步调用"""
        # 直接调用异步方法而不是使用 run_in_executor
        await self._ensure_client()

        # 构建请求数据
        payload = {
            "model": self.model_name,
            "messages": self._convert_messages(messages),
            "temperature": self.temperature,
            "top_p": self.top_p,
            "max_tokens": self.max_tokens,
            "stream": False,
        }

        try:
            # 发送 POST 请求
            async with self._client.post(
                f"{self.api_base}/chat/completions",
                json=payload,
                headers=self._build_headers(),
            ) as response:
                response.raise_for_status()

                # 解析响应
                response_data = await response.json()
                content = response_data["choices"][0]["message"]["content"]

                return AIMessage(content=content)

        except Exception as e:
            logger.error("Chat error occurred: %s", e, exc_info=True)
            # 直接抛出异常，避免后续JSON解析错误
            raise e

    async def _stream(
        self,
        messages: List[BaseMessage],
        tools: List[Tool] = None,
        **kwargs: Any,
    ) -> Iterator[LanguageModelOutput]:
        """支持流式响应的聊天 API"""

        # 确保HTTP客户端已初始化
        await self._ensure_client()

        # 构建请求数据，启用流式传输
        payload = {
            "model": self.model_name,
            "messages": self._convert_messages(messages),
            "temperature": self.temperature,
            "top_p": self.top_p,
            "max_tokens": self.max_tokens,
            "stream": True,
        }

        try:
            # 发送支持流式传输的 POST 请求
            async with self._client.post(
                f"{self.api_base}/chat/completions", json=payload
            ) as response:
                response.raise_for_status()

                async for line in response.content:
                    line = line.decode("utf-8").strip()
                    if line.startswith("data: "):
                        data = line[6:]  # 移除 "data: " 前缀
                        if data == "[DONE]":
                            break

                        try:
                            chunk = json.loads(data)
                            if "choices" in chunk and len(chunk["choices"]) > 0:
                                delta = chunk["choices"][0].get("delta", {})
                                if "content" in delta and delta["content"]:
                                    yield AIMessage(content=delta["content"])
                        except json.JSONDecodeError:
                            continue

        except Exception as e:
            logger.error("Stream error occurred: %s", e, exc_info=True)
            # 直接抛出异常，避免后续JSON解析错误
            raise e

        finally:
            await self._close_client()

    async def __aenter__(self):
        """异步上下文管理器进入"""
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """异步上下文管理器退出"""
        await self._close_client()

    def _build_headers(self) -> dict:
        model_request_header = {"Content-Type": "application/json"}
        if self.api_key:
            model_request_header.update({"Authorization": f"Bearer {self.api_key}"})
        if self.custom_header:
            model_request_header.update(self.custom_header)
        return model_request_header
