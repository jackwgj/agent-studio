import logging
from typing import List, Optional

from fastapi import APIRouter, Path
from jiuwen.context.memory_engine.config.config import ProfileTopicTag
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


class Tag(BaseModel):
    """标签模型"""

    name: str = Field(..., description="标签名称")
    topic: str = Field(..., description="主题")
    description: Optional[str] = Field(None, description="描述")


class TagsRequest(BaseModel):
    """标签请求体"""

    tags: List[Tag] = Field(..., description="标签列表")


class TagsResponse(BaseModel):
    """标签响应体"""

    tags: List[Tag] = Field(..., description="标签列表")


router = APIRouter(
    prefix="/v1/{project_id}/memories/profile", tags=["user_profile_config"]
)


@router.post("/config", response_model=TagsResponse)
async def set_user_profile_config(
    tags_request: TagsRequest, project_id: str = Path(..., description="项目ID")
):
    """
    新增用户画像槽位信息
    """
    from memory_service.memory.memory_engine_util import (
        get_mem_engine,
        get_preset_user_profile_topic,
    )

    for tag in tags_request.tags:
        # 对于系统预置的topic，不允许修改，直接跳过
        if tag.topic in get_preset_user_profile_topic():
            logger.info(f"topic {tag.topic} is preset, can not be modify")
            continue
        tag_config = ProfileTopicTag(name=tag.name, description=tag.description)
        await get_mem_engine().set_user_profile_tag(tag.topic, tag_config)
    return TagsResponse(tags=tags_request.tags)


@router.post("/config/delete")
async def delete_user_profile_config(
    tags_request: TagsRequest, project_id: str = Path(..., description="项目ID")
):
    """
    删除用户画像槽位信息配置
    """
    from memory_service.memory.memory_engine_util import get_mem_engine

    for tag_req in tags_request.tags:
        await get_mem_engine().delete_user_profile_tag(tag_req.topic, tag_req.name)
    return TagsResponse(tags=tags_request.tags)


def join_topic_tag(topic: str, tag: str) -> str:
    return topic + ":" + tag
