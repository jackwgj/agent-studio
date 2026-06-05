import logging
from typing import List, Optional

from fastapi import APIRouter, Path, Body, Query
from memory_service.utils.request_context_util import get_user_id
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


class UserProfileTagValue(BaseModel):
    """
    用户画像记忆tag的值
    """

    tag_name: str = Field(description="标签名称")
    tag_value: Optional[str] = Field(default=None, description="标签值")


class UserProfileTopicValue(BaseModel):
    """
    用户画像记忆topic的值
    """

    topic_name: str = Field(description="主题名称")
    tags: List[UserProfileTagValue] = Field(
        default_factory=list, description="标签列表"
    )


class GetUserProfileValuesResponseBody(BaseModel):
    """
    用户画像记忆值的响应消息体
    """

    topics: List[UserProfileTopicValue] = Field(description="主题列表")


class UserProfileTopicToDelete(BaseModel):
    """
    待删除的用户画像标签列表
    """

    topic_name: str = Field(description="主题名称")
    tags: List[str] = Field(default_factory=list, description="待删除的标签列表")


class DeleteUserProfileValuesRequestBody(BaseModel):
    """
    删除用户画像记忆值的请求消息体
    """

    topics: List[UserProfileTopicToDelete] = Field(description="主题列表")


class DeleteUserProfileValuesResponseBody(BaseModel):
    """
    删除用户画像记忆值的响应消息体
    """

    topics: List[UserProfileTopicToDelete] = Field(description="主题列表")


class UpdateUserProfileValuesRequestBody(BaseModel):
    """
    修改用户画像记忆值的请求消息体
    """

    topics: List[UserProfileTopicValue] = Field(description="主题列表")


class UpdateUserProfileValuesResponseBody(BaseModel):
    """
    修改用户画像记忆值的响应消息体
    """

    topics: List[UserProfileTopicValue] = Field(description="主题列表")


class ClearUserProfileValuesResponseBody(BaseModel):
    """
    清空用户画像记忆值的响应消息体
    """

    success: bool = Field(description="是否清除成功")


router = APIRouter(
    prefix="/private/v1/{project_id}/memories/profile", tags=["user_profile_private"]
)


@router.get("", response_model=GetUserProfileValuesResponseBody)
async def get_user_profile_values(
    project_id: str = Path(..., description="租户项目id", min_length=1, max_length=64),
    app_id: str = Query(
        ..., description="记忆的虚拟应用ID", min_length=1, max_length=64
    ),
):
    """
    查看某个应用中用户画像记忆的值

    Args:
        project_id: 租户项目id
        app_id: 记忆的虚拟应用ID

    Returns:
        用户画像记忆值的响应体
    """
    logger.info(
        f"Getting user profile values for project: {project_id}, application: {app_id}"
    )

    from memory_service.memory.memory_engine_util import get_mem_engine

    # 从内存引擎获取用户画像记忆值
    user_profile_values = await get_mem_engine().get_topic_profile_by_topics(
        user_id=get_user_id(), scope_id=app_id, topics=[], enable_default_topics=True
    )

    topics = []
    for topic in user_profile_values:
        topic_name = topic.topic
        tag_values = topic.tags if topic.tags else []
        tag_values_res = []
        for tag_value in tag_values:
            tag_values_res.append(
                UserProfileTagValue(tag_name=tag_value.name, tag_value=tag_value.value)
            )
        if tag_values_res:
            topics.append(
                UserProfileTopicValue(topic_name=topic_name, tags=tag_values_res)
            )

    return GetUserProfileValuesResponseBody(topics=topics)


# 更新用户画像的记忆内容
@router.put("", response_model=UpdateUserProfileValuesResponseBody)
async def update_user_profile_values(
    project_id: str = Path(..., description="租户项目id", min_length=1, max_length=64),
    app_id: str = Query(
        ..., description="记忆的虚拟应用ID", min_length=1, max_length=64
    ),
    request_body: UpdateUserProfileValuesRequestBody = Body(...),
):
    """
    修改某个应用中用户画像记忆的值

    Args:
        project_id: 租户项目id
        app_id: 记忆的虚拟应用ID
        request_body: 修改用户画像记忆值的请求体

    Returns:
        修改用户画像记忆值的响应体
    """
    logger.info(
        f"Updating user profile values for project: {project_id}, application: {app_id}"
    )

    # 按照topic一个一个往记忆引擎中更新用户画像的值
    topic_value_dict: dict[str, dict[str, str]] = {}
    for topic_value_req in request_body.topics:
        if topic_value_req.topic_name not in topic_value_dict:
            topic_value_dict[topic_value_req.topic_name] = {}

        tag_value_dict = topic_value_dict[topic_value_req.topic_name]

        if not topic_value_req.tags:
            continue

        for tag_value_req in topic_value_req.tags:
            tag_value_dict[tag_value_req.tag_name] = tag_value_req.tag_value

    from memory_service.memory.memory_engine_util import get_mem_engine

    cur_user_id = get_user_id()
    for topic_name, tags in topic_value_dict.items():
        await get_mem_engine().update_topic_profile(
            user_id=cur_user_id, scope_id=app_id, topic=topic_name, tag_values=tags
        )

    return UpdateUserProfileValuesResponseBody(topics=request_body.topics)


@router.post("/delete", response_model=DeleteUserProfileValuesResponseBody)
async def delete_user_profile_values(
    project_id: str = Path(..., description="租户项目id", min_length=1, max_length=64),
    app_id: str = Query(
        ..., description="记忆的虚拟应用ID", min_length=1, max_length=64
    ),
    request_body: DeleteUserProfileValuesRequestBody = Body(...),
):
    """
    删除某个应用中用户画像记忆的值

    Args:
        project_id: 租户项目id
        app_id: 记忆的虚拟应用ID
        request_body: 删除用户画像记忆值的请求体

    Returns:
        删除用户画像记忆值的响应体
    """
    logger.info(
        f"Deleting user profile values for project: {project_id}, application: {app_id}"
    )
    # 按照topic一个一个从记忆引擎中删除用户画像的值
    topic_value_dict: dict[str, list[str]] = {}
    for topic_value_req in request_body.topics:
        if topic_value_req.topic_name not in topic_value_dict:
            topic_value_dict[topic_value_req.topic_name] = []

        tag_value_list = topic_value_dict[topic_value_req.topic_name]

        if not topic_value_req.tags:
            continue

        for tag_value_req in topic_value_req.tags:
            tag_value_list.append(tag_value_req)

    cur_user_id = get_user_id()
    from memory_service.memory.memory_engine_util import get_mem_engine

    for topic_name, tags in topic_value_dict.items():
        await get_mem_engine().delete_topic_profile(
            user_id=cur_user_id, scope_id=app_id, topic=topic_name, tags=tags
        )

    return DeleteUserProfileValuesResponseBody(topics=request_body.topics)


@router.delete("/clear", response_model=ClearUserProfileValuesResponseBody)
async def clear_user_profile_values(
    project_id: str = Path(..., description="租户项目id", min_length=1, max_length=64),
    app_id: str = Query(
        ..., description="记忆的虚拟应用ID", min_length=1, max_length=64
    ),
):
    """
    清空某个应用中用户画像记忆的值

    Args:
        project_id: 租户项目id
        app_id: 记忆的虚拟应用ID

    Returns:
        清空用户画像记忆值的响应体
    """
    logger.info(
        f"Clearing user profile values for project: {project_id}, application: {app_id}"
    )

    cur_user_id = get_user_id()
    from memory_service.memory.memory_engine_util import get_mem_engine

    # 先查出来当前用户在某个App下有哪些用户画像记忆内容
    user_profile_values = await get_mem_engine().get_topic_profile_by_topics(
        user_id=cur_user_id, scope_id=app_id, topics=[], enable_default_topics=True
    )
    if not user_profile_values:
        return ClearUserProfileValuesResponseBody(success=True)
    # 按照topic一个一个进行删除
    for topic_value in user_profile_values:
        await get_mem_engine().delete_topic_profile(
            user_id=cur_user_id, scope_id=app_id, topic=topic_value.topic, tags=[]
        )

    logger.info(
        f"Clearing user profile values for project: {project_id}, application: {app_id}, delete {len(user_profile_values)} tags value"
    )
    return ClearUserProfileValuesResponseBody(success=True)
