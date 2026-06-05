import datetime
import json
import logging
from dataclasses import dataclass, asdict
from typing import List, Optional

from memory_service.utils.async_redis_util import get_async_redis_client

logger = logging.getLogger(__name__)


@dataclass
class TagValueEventData:
    topic: str
    name: str
    value: Optional[str]
    operation: str = "UPDATE"


@dataclass
class NaturalLanguageMemoryEventData:
    memory_id: str
    content: str
    operation: str


@dataclass
class UserProfileRefreshEventData:
    time: int
    agent_id: str
    conversation_id: str
    tags: Optional[List[TagValueEventData]]
    memories: Optional[List[NaturalLanguageMemoryEventData]]


@dataclass
class UserProfileRefreshEvent:
    data: UserProfileRefreshEventData
    event: str = "MEMORY_UPDATED"

    def to_json(self) -> str:
        def default_encoder(obj):
            if hasattr(obj, "__dict__"):
                return obj.__dict__
            return str(obj)

        json_str = json.dumps(asdict(self), ensure_ascii=False, default=default_encoder)
        return json.dumps(json_str, ensure_ascii=False)


class UserProfileMemoryEventService:
    """用户记忆事件服务，用于处理用户记忆相关的Redis事件"""

    async def record_memory_refresh_event(
        self,
        user_id: str,
        app_id: str,
        conversation_id: str,
        tags: Optional[List[TagValueEventData]],
        memories: Optional[List[NaturalLanguageMemoryEventData]],
    ) -> bool:
        """
        记录用户记忆刷新事件到Redis

        Args:
            user_id: 用户ID
            app_id: 虚拟应用ID
            conversation_id: 会话ID
            tags: 标签列表
            memories: 自然语言类的

        Returns:
            bool: 记录成功返回True，失败返回False
        """
        try:
            # 过期时间（秒），默认1小时
            expires = 3600
            # 生成事件数据
            now = datetime.datetime.now(datetime.timezone.utc)
            total_milliseconds = int(now.timestamp() * 1000)
            event_data = UserProfileRefreshEventData(
                time=total_milliseconds,
                agent_id=app_id,
                conversation_id=conversation_id,
                tags=tags,
                memories=memories,
            )
            event = UserProfileRefreshEvent(data=event_data)

            # 获取事件键
            event_key = self._obtain_user_memory_refresh_event_key(
                user_id, app_id, conversation_id
            )

            # 先检查是否已存在
            existing_event = await self._get_existing_event(event_key)

            if existing_event:
                # 合并新旧数据
                merged_event_data = self._merge_event_data(existing_event, event_data)
                merged_event = UserProfileRefreshEvent(data=merged_event_data)
                if merged_event.data.tags or merged_event.data.memories:
                    await get_async_redis_client().set(
                        event_key, merged_event.to_json(), expire=expires
                    )
                    logger.info(
                        f"Updated existing memory refresh event for user: {user_id}, "
                        f"app: {app_id}, conversation: {conversation_id}"
                    )
            else:
                # 插入新事件
                if event.data.tags or event.data.memories:
                    await get_async_redis_client().set(
                        event_key, event.to_json(), expire=expires
                    )
                    logger.info(
                        f"Recorded new memory refresh event for user: {user_id}, "
                        f"app: {app_id}, conversation: {conversation_id}"
                    )

            return True

        except Exception as e:
            logger.error(f"Failed to record memory refresh event: {e}", exc_info=True)
            return False

    async def _get_existing_event(
        self, event_key: str
    ) -> Optional[UserProfileRefreshEventData]:
        """
        从Redis获取已存在的事件数据

        Args:
            event_key: Redis键

        Returns:
            Optional[UserProfileRefreshEventData]: 如果存在则返回事件数据，否则返回None
        """
        try:
            event_json = await get_async_redis_client().get(event_key)
            if event_json:
                # 解析JSON字符串并转换回对象
                event_dict = json.loads(event_json)
                # 如果是双重JSON字符串（因为UserProfileRefreshEvent.to_json()做了双重序列化)
                if isinstance(event_dict, str):
                    event_dict = json.loads(event_dict)

                return UserProfileRefreshEventData(
                    time=event_dict["data"].get("time"),
                    agent_id=event_dict["data"].get("agent_id"),
                    conversation_id=event_dict["data"].get("conversation_id"),
                    tags=[tag for tag in event_dict["data"].get("tags", [])],
                    memories=[mem for mem in event_dict["data"].get("memories", [])],
                )
            return None
        except Exception as e:
            logger.error(f"Failed to get existing event from redis: {e}", exc_info=True)
            return None

    def _merge_event_data(
        self,
        existing_event: UserProfileRefreshEventData,
        new_event: UserProfileRefreshEventData,
    ) -> UserProfileRefreshEventData:
        """
        合并新旧事件数据

        Args:
            existing_event: 已存在的事件数据
            new_event: 新的事件数据

        Returns:
            UserProfileRefreshEventData: 合并后的事件数据
        """
        # 合并标签：新标签优先，但保留不存在的旧标签
        existing_tags_dict = {
            (tag["topic"], tag["name"]): tag for tag in existing_event.tags
        }
        new_tags_dict = {(tag.topic, tag.name): tag for tag in new_event.tags}

        # 合并标签字典，新标签会覆盖旧标签
        merged_tags_dict = {**existing_tags_dict, **new_tags_dict}

        # 转换回标签列表
        merged_tags = list(merged_tags_dict.values())

        existing_memories = existing_event.memories if existing_event.memories else []
        new_memories = new_event.memories if new_event.memories else []

        merged_memories = existing_memories + new_memories

        return UserProfileRefreshEventData(
            time=new_event.time,
            agent_id=new_event.agent_id,
            conversation_id=new_event.conversation_id,
            tags=merged_tags,
            memories=merged_memories,
        )

    @staticmethod
    def _obtain_user_memory_refresh_event_key(
        user_id: str, app_id: str, conversation_id: str
    ) -> str:
        """
        生成用户记忆刷新事件键

        Args:
            user_id: 用户ID
            app_id: 虚拟应用ID
            conversation_id: 会话ID

        Returns:
            str: Redis键
        """
        return f"agent.service.user.memory.refresh.event.{user_id}.{app_id}.{conversation_id}"
