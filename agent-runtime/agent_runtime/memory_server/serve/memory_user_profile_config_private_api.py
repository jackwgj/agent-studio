import logging
from typing import List, Optional

from fastapi import APIRouter, Path
from jiuwen.context.memory_engine.config.config import (
    ProfileTopicConfig,
    ProfileTopicTag,
)
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


class UserProfileTag(BaseModel):
    name: str = Field(..., description="标签名称")
    description: Optional[str] = Field(None, description="描述")


class UserProfileTopic(BaseModel):
    topic: str = Field(..., description="主题名称")
    tags: Optional[List[UserProfileTag]] = Field(None, description="标签列表")


class UserProfileModifyRequest(BaseModel):
    topics: List[UserProfileTopic] = Field(..., description="待更新的主题列表")


class UserProfileDeleteRequest(BaseModel):
    topics: List[UserProfileTopic] = Field(..., description="待删除的主题列表")


class UserProfileTopicModifyResponse(BaseModel):
    success: bool = Field(..., description="修改是否成功")


class UserProfileTopicDeleteResponse(BaseModel):
    success: bool = Field(..., description="删除是否成功")


router = APIRouter(
    prefix="/private/v1/{project_id}/memories/profile",
    tags=["user_profile_config_private"],
)


@router.post("/config", response_model=UserProfileTopicModifyResponse)
async def modify_user_profile_config(
    request: UserProfileModifyRequest, project_id: str = Path(..., description="项目ID")
):
    """
    更新应用中的用户画像定义设置
    """

    from memory_service.memory.memory_engine_util import (
        get_mem_engine,
        get_preset_user_profile_topic,
    )

    profile_topics = []
    for topic in request.topics:
        # 对于系统预置的topic，不允许修改，直接跳过
        if topic.topic in get_preset_user_profile_topic():
            logger.info(f"topic {topic.topic} is preset, can not be modify")
            continue

        if not topic.tags:
            continue
        profile_tags = []
        for tag in topic.tags:
            profile_tags.append(
                ProfileTopicTag(name=tag.name, description=tag.description)
            )
        profile_topics.append(ProfileTopicConfig(topic=topic.topic, tags=profile_tags))

    for profile_topic in profile_topics:
        if not profile_topic.tags:
            continue
        for tag in profile_topic.tags:
            await get_mem_engine().set_user_profile_tag(profile_topic.topic, tag)

    return UserProfileTopicModifyResponse(success=True)


@router.post("/config/delete", response_model=UserProfileTopicDeleteResponse)
async def modify_user_profile_config(
    request: UserProfileDeleteRequest, project_id: str = Path(..., description="项目ID")
):
    """
    删除应用中的用户画像定义设置
    """

    from memory_service.memory.memory_engine_util import get_mem_engine

    profile_topics = []
    for topic in request.topics:
        if not topic.tags:
            continue
        profile_tags = []
        for tag in topic.tags:
            profile_tags.append(ProfileTopicTag(name=tag.name))
        profile_topics.append(ProfileTopicConfig(topic=topic.topic, tags=profile_tags))

    for profile_topic in profile_topics:
        if not profile_topic.tags:
            continue
        for tag in profile_topic.tags:
            await get_mem_engine().delete_user_profile_tag(
                profile_topic.topic, tag.name
            )

    return UserProfileTopicDeleteResponse(success=True)
