import asyncio
import json
import os
import time
from dataclasses import dataclass, asdict
from typing import Optional

import aiohttp
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.serve.common.context import request as current_request
from jiuwen.serve.controllers.execution.ir_converter import IRConverter


@dataclass
class UserProfileExtractMessageReq:
    role: str
    content: str
    conversation_id: str
    timestamp: Optional[int] = None


@dataclass
class UserProfileExtractTagConfigReq:
    name: str
    description: str


@dataclass
class UserProfileExtractTopicConfigReq:
    name: str
    tags: list[UserProfileExtractTagConfigReq]


@dataclass
class UserProfileExtractRequestBody:
    messages: list[UserProfileExtractMessageReq]
    topics: list[UserProfileExtractTopicConfigReq]

    def to_json(self) -> str:
        def default_encoder(obj):
            if hasattr(obj, "__dict__"):
                return obj.__dict__
            return str(obj)

        return json.dumps(asdict(self), ensure_ascii=False, default=default_encoder)


@dataclass
class TagValue:
    topic: str
    name: str
    value: str


@dataclass
class UserProfileExtractResponseBody:
    tags: list[TagValue]


@dataclass
class UserProfileMemoryExtractorConfig:
    extract_max_turns: Optional[int] = None
    extract_time_windows: Optional[int] = None
    user_profile_enable: bool = False


@dataclass
class UserProfileMemoryHistoryMessage:
    role: str
    content: str


@dataclass
class MessageChatTurn:
    messages: Optional[list[UserProfileMemoryHistoryMessage]] = None


@dataclass
class ChatHistoryConversationCache:
    user_id: str
    app_id: str
    conversation_id: str
    last_update_time: Optional[int] = None
    chat_turns: Optional[list[MessageChatTurn]] = None

    def add_chat_turn(self, chat_turn: MessageChatTurn):
        if not self.chat_turns:
            self.chat_turns = []
        self.chat_turns.append(chat_turn)
        self.last_update_time = int(time.time())

    @classmethod
    def from_json(cls, json_str: str):
        data = json.loads(json_str)

        # 转换历史对话列表
        chat_turns = []
        for chat_data in data.get("chat_turns", []):
            messages = []
            for msg_data in chat_data.get("messages", []):
                messages.append(UserProfileMemoryHistoryMessage(**msg_data))
            chat_turns.append(MessageChatTurn(messages=messages))

        return cls(
            user_id=data["user_id"],
            app_id=data["app_id"],
            conversation_id=data["conversation_id"],
            last_update_time=data["last_update_time"],
            chat_turns=chat_turns,
        )

    def to_json(self) -> str:
        def default_encoder(obj):
            if hasattr(obj, "__dict__"):
                return obj.__dict__
            return str(obj)

        return json.dumps(asdict(self), ensure_ascii=False, default=default_encoder)


class UserProfileMemoryExtractor:
    _instance = None
    _initialized = False
    _user_profile_enable = (
        os.getenv("MEMORY_USER_PROFILE_ENABLE", "false").lower() == "true"
    )
    _extract_max_turns = int(os.getenv("MEMORY_USER_PROFILE_EXTRACT_MAX_TURN", 5))
    _extract_time_windows = int(
        os.getenv("MEMORY_USER_PROFILE_EXTRACT_TIME_WINDOWS", 10)
    )
    _memory_service_url = os.environ.get("MEMORY_SERVICE_ENDPOINT")
    _memory_service_cert_path = os.environ.get("MEMORY_SERVICE_CERT_PATH", "")
    _memory_service_ssl_verify = (
        _memory_service_cert_path if _memory_service_cert_path else False
    )
    _memory_service_session = None
    _memory_extract_delay_task = set()

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        # 防止多次初始化
        if not self._initialized:
            from jiuwen.common.store.async_redis import get_async_redis_instance

            self._redis_client = get_async_redis_instance()
            self._initialized = True
            logger.info(
                f"Init user profile memory extractor with enable:{self._user_profile_enable}, "
                f"default extract max turns: {self._extract_max_turns}, "
                f"default extract time windows: {self._extract_time_windows}"
            )

            self._memory_service_session = aiohttp.ClientSession(
                connector=aiohttp.TCPConnector(ssl=self._memory_service_ssl_verify)
            )

    async def async_add_chat_turn(
        self,
        user_id: str,
        app_id: str,
        conversation_id: str,
        ir_data: dict,
        messages: list[BaseMessage],
    ):
        """
        为用户画像记忆的提取来存储每一轮会话
        Args:
            user_id: 用户ID
            app_id: 应用ID，可以是智能体ID或者工作流ID
            conversation_id: 会话ID
            ir_data: 智能体或工作流的IR数据
            messages: 本轮对话的消息

        """
        if not self._user_profile_enable:
            # 未开启用户画像功能时，不做任何处理
            return

        try:
            # 解析IR配置中用户画像提取相关的配置
            extractor_config = self._convert_memory_extract_config(ir_data)
            if not extractor_config.user_profile_enable:
                return

            real_extract_config = UserProfileMemoryExtractorConfig(
                extract_max_turns=self._extract_max_turns,
                extract_time_windows=self._extract_time_windows,
            )
            if extractor_config and extractor_config.extract_max_turns:
                real_extract_config.extract_max_turns = (
                    extractor_config.extract_max_turns
                )
            if extractor_config and extractor_config.extract_time_windows:
                real_extract_config.extract_time_windows = (
                    extractor_config.extract_time_windows
                )

            task = asyncio.create_task(
                self._async_add_chat_turn_task(
                    user_id,
                    app_id,
                    conversation_id,
                    real_extract_config,
                    messages,
                    ir_data,
                )
            )
            await task
        except Exception as e:
            logger.error(
                f"Add chat history to user profile memory cache error: {e}",
                exc_info=True,
            )

    async def _async_add_chat_turn_task(
        self,
        user_id: str,
        app_id: str,
        conversation_id: str,
        extractor_config: UserProfileMemoryExtractorConfig,
        messages: list[BaseMessage],
        ir_data: dict,
    ):
        """
        为用户画像记忆的提取来存储每一轮会话的异步任务
        Args:
            user_id: 用户ID
            app_id: 应用ID，可以是智能体ID或者工作流ID
            conversation_id: 会话ID
            extractor_config: 用户画像提取配置
            messages: 本轮对话的消息
            ir_data: 智能体或工作流的IR数据

        """
        try:
            await self._cache_chat_message(
                user_id, app_id, conversation_id, extractor_config, messages, ir_data
            )
        except Exception as e:
            logger.error(
                f"Add chat history to user profile memory cache error: {e}",
                exc_info=True,
            )

    async def _cache_chat_message(
        self,
        user_id: str,
        app_id: str,
        conversation_id: str,
        extractor_config: UserProfileMemoryExtractorConfig,
        messages: list[BaseMessage],
        ir_data: dict,
    ):
        """
        缓存用于记忆提取的历史消息
        Args:
            user_id: 用户ID
            app_id: 应用ID，可以是智能体ID或者工作流ID
            conversation_id: 会话ID
            extractor_config: 用户画像提取配置
            messages: 历史消息
            ir_data: 智能体或工作流的IR数据

        Returns:

        """
        if not messages:
            return

        message_in_turn = []
        for message in messages:
            message_in_turn.append(
                UserProfileMemoryHistoryMessage(
                    role=message.type, content=message.content
                )
            )
        chat_turn = MessageChatTurn(messages=message_in_turn)

        history_message = await self._redis_client.get(
            self._obtain_user_memory_key(user_id, app_id, conversation_id)
        )

        if not history_message:
            logger.info(
                f"Cached user memory is empty. Create userMemory for user: {user_id}."
            )
            cache_conversation = ChatHistoryConversationCache(
                user_id=user_id, app_id=app_id, conversation_id=conversation_id
            )
            cache_conversation.add_chat_turn(chat_turn)
        else:
            cache_conversation = ChatHistoryConversationCache.from_json(
                history_message.decode("utf-8")
            )
            cache_conversation.add_chat_turn(chat_turn)

        # 如果达到了设置的对话轮数，则开始提取记忆；如果没有达到，则继续缓存消息，并启动一个延时任务，等待一段时间后再提取记忆
        if len(cache_conversation.chat_turns) >= extractor_config.extract_max_turns:
            logger.info(
                f"Cached chatTurns exceeds maxCachedTurn count. Extract user memory for user: {user_id}, conversation: {conversation_id}."
            )
            await self._extract_memory(
                user_id, app_id, conversation_id, ir_data, cache_conversation
            )
        else:
            message_json_str = ChatHistoryConversationCache.to_json(cache_conversation)
            await self._redis_client.set(
                self._obtain_user_memory_key(user_id, app_id, conversation_id),
                message_json_str.encode("utf-8"),
            )
            logger.info(
                f"Cached chatTurns is within maxCachedTurn count. Schedule delay extract user memory  for user: {user_id}, conversation: {conversation_id} later."
            )
            asyncio.create_task(
                self._extract_memory_delay(
                    user_id,
                    app_id,
                    conversation_id,
                    extractor_config.extract_time_windows,
                    ir_data,
                )
            )

    async def _extract_memory_delay(
        self,
        user_id: str,
        app_id: str,
        conversation_id: str,
        delay_minutes: int,
        ir_data: dict,
    ):
        """
        延时提取用户画像记忆
        Args:
            user_id: 用户ID
            app_id: 应用ID，可以是智能体ID或者工作流ID
            conversation_id: 会话ID
            delay_minutes: 延时执行的时间，单位：分钟
            ir_data: 智能体或工作流的IR数据

        Returns:

        """
        # 如果针对某个对话已经存在延时任务了，就不再继续新增一个延时任务
        if conversation_id in self._memory_extract_delay_task:
            return

        await asyncio.sleep(delay_minutes * 60)
        try:
            await self._extract_memory(user_id, app_id, conversation_id, ir_data)
        except Exception as e:
            logger.error(
                f"Add chat history to user profile memory cache error: {e}",
                exc_info=True,
            )

    async def _extract_memory(
        self,
        user_id: str,
        app_id: str,
        conversation_id: str,
        ir_data: dict,
        cached_conversation: Optional[ChatHistoryConversationCache] = None,
    ):
        """
        提取用户画像记忆
        Args:
            user_id: 用户ID
            app_id: 应用ID，可以是智能体ID或者工作流ID
            conversation_id: 会话ID
            cached_conversation: 缓存的历史对话内容
            ir_data: 智能体或工作流的IR数据

        Returns:

        """
        cached_conversation_to_extract = cached_conversation
        if not cached_conversation:
            conversation_str = await self._redis_client.get(
                self._obtain_user_memory_key(user_id, app_id, conversation_id)
            )
            if not conversation_str:
                logger.info(
                    f"There is no message cache. Skip extract user memory for user: {user_id}, conversation: {conversation_id}."
                )
                return
            cached_conversation_to_extract = ChatHistoryConversationCache.from_json(
                conversation_str.decode("utf-8")
            )
        if not cached_conversation_to_extract.chat_turns:
            logger.warning(
                f"Cached conversation is empty. Skip extract user memory for user: {user_id}, conversation: {conversation_id}."
            )
            return
        # 已经准备触发提取了，需要从redis中删除缓存的消息，避免用户一直对话，还在持续的往redis中写入消息缓存
        await self._redis_client.delete(
            self._obtain_user_memory_key(user_id, app_id, conversation_id)
        )

        # 从IR中递归解析所有的topic和tag信息
        all_tags = await IRConverter.get_memory_topics(ir_data)
        topics_to_extract = []
        if all_tags:
            for topic in all_tags:
                topic_to_extract = UserProfileExtractTopicConfigReq(
                    name=topic.get("name"), tags=[]
                )
                if topic.get("tags"):
                    for tag in topic.get("tags"):
                        tag_to_extract = UserProfileExtractTagConfigReq(
                            name=tag.get("name"), description=tag.get("description")
                        )
                        topic_to_extract.tags.append(tag_to_extract)
                topics_to_extract.append(topic_to_extract)

        # 调用memory-service的接口提取用户画像
        url = f"{self._memory_service_url}/v1/{self._get_project_id()}/memories/profile/users/{user_id}/apps/{app_id}"
        reqeust_body = self._convert_message_for_memory_extract(
            cached_conversation_to_extract
        )
        reqeust_body.topics = topics_to_extract
        headers = self._get_headers()
        try:
            async with self._memory_service_session.post(
                url, data=reqeust_body.to_json(), headers=headers
            ) as response:
                if response.status == 200:
                    logger.info(
                        f"call memor-service extract user profile success for user: {user_id}, conversation: {conversation_id}"
                    )
                else:
                    logger.error(
                        f"extract user profile failed for user: {user_id}, conversation: {conversation_id}. Error message:{response}"
                    )

        except Exception as e:
            logger.error(
                f"call memor-service error when extract user profile. Error message:{e}",
                exc_info=True,
            )
        finally:
            # 不管有没有提取成功，都要删除已经存在的延时任务
            self._memory_extract_delay_task.discard(conversation_id)

    def _convert_message_for_memory_extract(
        self, history_messages: ChatHistoryConversationCache
    ) -> UserProfileExtractRequestBody:
        if not history_messages.chat_turns:
            return UserProfileExtractRequestBody(messages=[], topics=[])

        request_body = UserProfileExtractRequestBody(messages=[], topics=[])
        for chat_turn in history_messages.chat_turns:
            for message in chat_turn.messages:
                msg = UserProfileExtractMessageReq(
                    role=message.role,
                    content=message.content,
                    conversation_id=history_messages.conversation_id,
                )
                request_body.messages.append(msg)

        return request_body

    def _convert_memory_extract_config(
        self, ir_data: dict
    ) -> UserProfileMemoryExtractorConfig:
        configs = ir_data.get("configs") if ir_data.get("configs") else {}
        memory_config = configs.get("memory") if configs.get("memory") else {}
        user_profile_config = (
            memory_config.get("userProfile") if memory_config.get("userProfile") else {}
        )
        user_profile_enable = (
            user_profile_config.get("enable")
            if user_profile_config.get("enable")
            else False
        )
        user_profile_extract_config = (
            user_profile_config.get("extractConfig")
            if user_profile_config.get("extractConfig")
            else {}
        )
        return UserProfileMemoryExtractorConfig(
            extract_max_turns=user_profile_extract_config.get("maxChatTurn")
            if user_profile_extract_config.get("maxChatTurn")
            else self._extract_max_turns,
            extract_time_windows=user_profile_extract_config.get("timeWindow")
            if user_profile_extract_config.get("timeWindow")
            else self._extract_time_windows,
            user_profile_enable=user_profile_enable,
        )

    def _get_headers(self) -> dict[str, str]:
        request_headers = current_request.get().headers

        headers = {
            "Content-Type": "application/json",
            "X-Auth-Token": request_headers.get("X-Auth-Token", ""),
        }
        return headers

    def _get_project_id(self) -> str:
        return current_request.get().headers.get("x-owner-project-id", "")

    def _obtain_user_memory_key(self, user_id: str, app_id: str, conversation_id: str):
        return f"memory_chat_cache:{user_id}:{app_id}:{conversation_id}"


def get_instance():
    global _extractor_instance
    if _extractor_instance is None:
        _extractor_instance = UserProfileMemoryExtractor()
    return _extractor_instance


_extractor_instance = None
