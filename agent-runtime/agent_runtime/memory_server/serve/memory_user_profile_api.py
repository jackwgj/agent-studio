import asyncio
import logging
from enum import Enum
from typing import List, Optional

from fastapi import APIRouter, Path, Body, Query
from jiuwen.common.llm_service.messages import HumanMessage, SystemMessage, AIMessage
from jiuwen.context.memory_engine.config.config import (
    ProfileTopicConfig,
    ProfileTopicTag,
)
from memory_service.config.settings import EMBEDDING_TYPE, VECTOR_STORE_TYPE
from memory_service.utils.user_memory_event_service import (
    UserProfileMemoryEventService,
    TagValueEventData,
    NaturalLanguageMemoryEventData,
)
from pydantic import BaseModel, Field

event_service = UserProfileMemoryEventService()

logger = logging.getLogger(__name__)


class MessageRole(Enum):
    """消息角色枚举"""

    USER = "user"
    ASSISTANT = "assistant"
    SYSTEM = "system"
    TOOL = "tool"


class Message(BaseModel):
    """消息模型"""

    id: Optional[str] = Field(None, description="消息ID")
    role: MessageRole = Field(..., description="消息角色")
    timestamp: Optional[int] = Field(None, description="时间戳")
    content: str = Field(..., description="消息内容")
    conversation_id: str = Field(..., description="会话ID")


class TagConfig(BaseModel):
    name: str = Field(..., description="tag名称")
    description: str = Field(..., description="tag描述")


class TopicConfig(BaseModel):
    """用户画像提取请求体"""

    name: str = Field(..., description="topic名称")
    tags: Optional[List[TagConfig]] = Field(
        None, description="tag列表，如果为空列表，表示提取当前topic下所有的tag信息"
    )


class UserProfileExtractRequestBody(BaseModel):
    """用户画像提取请求体"""

    messages: List[Message] = Field(..., description="消息列表")
    topics: Optional[List[TopicConfig]] = Field(
        None, description="待提取的topic列表，如果为None，表示提取所有的用户画像信息"
    )


class TagValue(BaseModel):
    """标签模型"""

    topic: str = Field(..., description="主题")
    name: str = Field(..., description="名称")
    value: Optional[str] = Field(None, description="值")


class UserProfileExtractResponseBody(BaseModel):
    """用户画像提取响应体"""

    tags: List[TagValue] = Field(..., description="标签列表")


class UserProfileValueResponseBody(BaseModel):
    """用户画像查询的响应体"""

    tags: List[TagValue] = Field(..., description="标签列表")


class TagUpdateRequest(BaseModel):
    """Tag更新请求体"""

    value: Optional[str] = Field(None, description="tag待更新的值")


class TagUpdateResponse(BaseModel):
    """Tag更新响应体"""

    topic: str = Field(..., description="tag所属的主题")
    name: str = Field(..., description="tag名称")
    value: str = Field(..., description="tag更新的值")


class DeleteUserProfileValuesResponseBody(BaseModel):
    """Tag删除响应体"""

    success: bool = Field(..., description="删除是否成功")


router = APIRouter(prefix="/v1/{project_id}/memories/profile", tags=["user_profile"])


@router.post(
    "/users/{user_id}/apps/{app_id}", response_model=UserProfileExtractResponseBody
)
async def extract_user_profile(
    project_id: str = Path(..., description="项目ID"),
    user_id: str = Path(..., description="用户ID", example="user123"),
    app_id: str = Path(..., description="虚拟应用ID", example="fund_manager"),
    request_body: UserProfileExtractRequestBody = Body(
        ..., description="提取用户画像记忆的请求体"
    ),
):
    """
    根据用户的多轮对话内容生成用户画像

    Args:
        project_id: 项目ID
        user_id: 用户ID
        app_id: 虚拟应用ID
        request_body: 包含用户对话消息的请求体

    Returns:
        用户画像标签列表
    """
    logger.info(f"Extracting user profile for user: {user_id}, app: {app_id}")
    topic_list_to_extract = []
    if request_body.topics is not None:
        for topic in request_body.topics:
            topic_config = ProfileTopicConfig(topic=topic.name, tags=[])
            if topic.tags:
                for tag in topic.tags:
                    tag_config = ProfileTopicTag(
                        name=tag.name, description=tag.description
                    )
                    topic_config.tags.append(tag_config)
            topic_list_to_extract.append(topic_config)
    else:
        topic_list_to_extract = None
    messages = []

    # 把消息按照conversation分组，分别进行记忆抽取
    messages_conversation_dict: dict[str, List[Message]] = {}
    for msg in request_body.messages:
        message_in_conversation = (
            messages_conversation_dict[msg.conversation_id]
            if msg.conversation_id in messages_conversation_dict
            else []
        )
        message_in_conversation.append(msg)
        messages_conversation_dict[msg.conversation_id] = message_in_conversation

    extracted_tags = []

    role_mapping = {
        MessageRole.USER: HumanMessage,
        MessageRole.SYSTEM: SystemMessage,
        MessageRole.ASSISTANT: AIMessage,
    }

    for conversation_id, messages_in_conversation in messages_conversation_dict.items():
        for msg in messages_in_conversation:
            role_enum = MessageRole(msg.role) if isinstance(msg.role, str) else msg.role
            message_class = role_mapping.get(role_enum, HumanMessage)
            messages.append(message_class(content=msg.content))

        asyncio.create_task(
            _extract_user_profile_task(
                user_id, app_id, conversation_id, topic_list_to_extract, messages
            )
        )

    return UserProfileExtractResponseBody(tags=extracted_tags)


async def _extract_user_profile_task(
    user_id: str,
    app_id: str,
    conversation_id: str,
    topic_list_to_extract: List[ProfileTopicConfig],
    messages: List,
):
    """
    提取用户画像的异步任务
    """
    from memory_service.memory.memory_engine_util import get_mem_engine

    mem_engine = get_mem_engine()
    extract_result = await mem_engine.process_messages(
        user_id=user_id,
        scope_id=app_id,
        messages=messages,
        session_id=conversation_id,
        topics=topic_list_to_extract,
    )
    extracted_tags = []
    for profile_topic in (
        extract_result.profile_topics if extract_result.profile_topics else []
    ):
        topic_name = profile_topic.topic
        tags = profile_topic.tags if profile_topic.tags else []
        for tag in tags:
            tag_value = TagValueEventData(
                topic=topic_name, name=tag.name, value=tag.value
            )
            extracted_tags.append(tag_value)
    logger.info(
        f"Extracting {len(extracted_tags)} user profile for user: {user_id}, app: {app_id}"
    )

    memories_update = []
    if EMBEDDING_TYPE.lower() != "none" and VECTOR_STORE_TYPE.lower() != "none":
        # 只有开启了向量相关服务时，才会记录自然语言类的记忆
        for mem in extract_result.user_memory if extract_result.user_memory else []:
            memories_update.append(
                NaturalLanguageMemoryEventData(
                    memory_id="",
                    content=mem.memory_content,
                    operation="DELETE" if mem.operation_type == "DELETE" else "UPDATE",
                )
            )

    # 用户记忆事件更新事件写入Redis
    success = await event_service.record_memory_refresh_event(
        user_id=user_id,
        app_id=app_id,
        conversation_id=conversation_id,
        tags=extracted_tags,
        memories=memories_update,
    )

    if success:
        logger.info(
            f"Record user profile refresh event into redis for user: {user_id}, app: {app_id}, conversation id: {conversation_id}"
        )
    else:
        logger.warning(
            f"Failed to record user profile refresh event into redis for user: {user_id}, app: {app_id}, conversation id: {conversation_id}"
        )


@router.get(
    "/users/{user_id}/apps/{app_id}/tags", response_model=UserProfileValueResponseBody
)
async def get_user_profile_by_topics(
    project_id: str = Path(..., description="项目ID"),
    user_id: str = Path(..., description="用户ID", example="user123"),
    app_id: str = Path(..., description="虚拟应用ID", example="fund_manager"),
    topics: Optional[str] = Query(
        None, description="主题列表，用逗号分隔", example="financial,geo"
    ),
):
    """
    查询用户指定topic的画像

    Args:
        project_id: 项目ID
        user_id: 用户ID
        app_id: 虚拟应用ID
        topics: 可选的主题列表，多个主题用逗号分隔

    Returns:
        用户画像标签列表
    """
    logger.info(f"Query tags for user: {user_id}, app: {app_id}, topics: {topics}")

    topic_list = topics.split(",") if topics else []

    tag_response = []

    from memory_service.memory.memory_engine_util import get_mem_engine

    # 如果用户想按照指定topic查询记忆内容，则不需要查询默认的主题记忆内容
    query_default_topics = False if topics else True
    tags_in_memory_engine = await get_mem_engine().get_topic_profile_by_topics(
        user_id, app_id, topic_list, enable_default_topics=query_default_topics
    )
    for topic in tags_in_memory_engine:
        topic_name = topic.topic
        tag_values = topic.tags if topic.tags else []
        for tag_value in tag_values:
            tag_response.append(
                TagValue(topic=topic_name, name=tag_value.name, value=tag_value.value)
            )

    logger.info(
        f"Find {len(tag_response)} user profile tag value for user: {user_id}, app: {app_id}"
    )
    return UserProfileValueResponseBody(tags=tag_response)


@router.get("/users/{user_id}/apps/{app_id}/tags/{tag_name}", response_model=TagValue)
async def get_user_profile_tag_latest_value(
    project_id: str = Path(..., description="项目ID"),
    user_id: str = Path(..., description="用户ID", example="user123"),
    app_id: str = Path(..., description="虚拟应用ID", example="fund_manager"),
    tag_name: str = Path(..., description="标签名称", example="理财产品"),
    topic: str = Query(..., description="主题名称", example="financial"),
):
    """
    查询指定 tag 当前值

    Args:
        project_id: 项目ID
        user_id: 用户ID
        app_id: 虚拟应用ID
        tag_name: 标签名称
        topic: 主题名称

    Returns:
        指定标签的详细信息
    """
    logger.info(
        f"Query tag '{tag_name}' for user: {user_id}, app: {app_id}, topic: {topic}"
    )
    topic_list = [topic]
    from memory_service.memory.memory_engine_util import get_mem_engine

    tags_in_memory_engine = await get_mem_engine().get_topic_profile_by_topics(
        user_id, app_id, topic_list
    )
    for topic_in_engine in tags_in_memory_engine:
        topic_name = topic_in_engine.topic
        if topic_name != topic:
            continue
        tag_values = topic_in_engine.tags if topic_in_engine.tags else []
        for tag_value in tag_values:
            if tag_value.name == tag_name:
                return TagValue(
                    topic=topic_name, name=tag_value.name, value=tag_value.value
                )

    return TagValue(topic=topic, name=tag_name, value=None)


@router.put(
    "/users/{user_id}/apps/{app_id}/tags/{tag_name}", response_model=TagUpdateResponse
)
async def update_user_profile_tag_value(
    project_id: str = Path(..., description="项目ID"),
    user_id: str = Path(..., description="用户ID", example="user123"),
    app_id: str = Path(..., description="虚拟应用ID", example="fund_manager"),
    tag_name: str = Path(..., description="标签名称", example="理财产品"),
    topic: str = Query(..., description="主题名称", example="financial"),
    request_body: TagUpdateRequest = Body(...),
):
    """
    更新tag的值

    Args:
        project_id: 项目ID
        user_id: 用户ID
        app_id: 虚拟应用ID
        tag_name: 标签名称
        topic: 主题名称
        request_body: 更新请求体

    Returns:
        更新后的标签信息
    """
    logger.info(
        f"Update tag '{tag_name}' for user: {user_id}, app: {app_id}, topic: {topic}"
    )
    tag_value: dict[str, str] = {tag_name: request_body.value}

    from memory_service.memory.memory_engine_util import get_mem_engine

    await get_mem_engine().update_topic_profile(
        topic=topic, tag_values=tag_value, user_id=user_id, scope_id=app_id
    )

    return TagUpdateResponse(topic=topic, name=tag_name, value=request_body.value)


@router.delete(
    "/users/{user_id}/apps/{app_id}/topics/{topic_name}",
    response_model=DeleteUserProfileValuesResponseBody,
)
async def delete_user_profile_values(
    project_id: str = Path(..., description="项目ID"),
    user_id: str = Path(..., description="用户ID", example="user123"),
    app_id: str = Path(..., description="虚拟应用ID", example="fund_manager"),
    topic_name: str = Path(..., description="主题名称", example="financial"),
    tags: str = Query(None, description="待删除的标签列表", example="理财产品"),
):
    """
    删除指定topic或tag的记忆内容

    Args:
        project_id: 项目ID
        user_id: 用户ID
        app_id: 虚拟应用ID
        topic_name: 主题名称
        tags: 待删除的标签列表

    Returns:
        删除操作是否成功
    """
    logger.info(
        f"Delete tags '{tags}' for user: {user_id}, app: {app_id}, topic: {topic_name}"
    )
    tags_to_delete = tags.split(",") if tags else []
    from memory_service.memory.memory_engine_util import get_mem_engine

    await get_mem_engine().delete_topic_profile(
        topic=topic_name, tags=tags_to_delete, user_id=user_id, scope_id=app_id
    )
    return DeleteUserProfileValuesResponseBody(success=True)


@router.delete(
    "/users/{user_id}/apps/{app_id}/topics",
    response_model=DeleteUserProfileValuesResponseBody,
)
async def clear_user_profile_values(
    project_id: str = Path(..., description="项目ID"),
    user_id: str = Path(..., description="用户ID", example="user123"),
    app_id: str = Path(..., description="虚拟应用ID", example="fund_manager"),
):
    """
    删除指定topic或tag的记忆内容

    Args:
        project_id: 项目ID
        user_id: 用户ID
        app_id: 虚拟应用ID

    Returns:
        删除操作是否成功
    """
    logger.info(f"clear user profile values for user: {user_id}, app: {app_id}")
    from memory_service.memory.memory_engine_util import get_mem_engine

    # 先查出来当前用户在某个App下有哪些用户画像记忆内容
    user_profile_values = await get_mem_engine().get_topic_profile_by_topics(
        user_id=user_id, scope_id=app_id, topics=[], enable_default_topics=True
    )
    if not user_profile_values:
        return DeleteUserProfileValuesResponseBody(success=True)
    # 按照topic一个一个进行删除
    for topic_value in user_profile_values:
        await get_mem_engine().delete_topic_profile(
            user_id=user_id, scope_id=app_id, topic=topic_value.topic, tags=[]
        )
    logger.info(
        f"Clearing user profile values for application: {app_id}, delete {len(user_profile_values)} tags value"
    )
    return DeleteUserProfileValuesResponseBody(success=True)
