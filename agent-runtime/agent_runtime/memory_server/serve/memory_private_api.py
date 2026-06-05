"""
记忆管理私有API接口

提供记忆的创建、查询、更新、删除等私有化接口，不对外开放
"""

import asyncio
import logging
from typing import List, Optional

from fastapi import APIRouter, Query, Path, Body
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.context.memory_engine.config.config import ProcessMemoryMode
from memory_service.config.settings import (
    EMBEDDING_TYPE,
    VECTOR_STORE_TYPE,
    USER_MAX_MEMORY_COUNT,
)
from memory_service.exception.error_code import ErrorCode
from memory_service.exception.memory_service_exception import MemoryServiceException
from memory_service.i18n.i18n_constant import I18nMessageCode
from memory_service.i18n.i18n_util import i18n_util
from memory_service.memory.memory_engine_util import get_mem_engine
from memory_service.utils.request_context_util import get_user_id
from memory_service.utils.user_memory_event_service import (
    UserProfileMemoryEventService,
    NaturalLanguageMemoryEventData,
)
from pydantic import BaseModel, Field

event_service = UserProfileMemoryEventService()

logger = logging.getLogger(__name__)

# API路由配置
router = APIRouter(
    prefix="/private/v1/{project_id}/long-term-memories",
    tags=["自然语言类的长期记忆管理接口"],
)


# =====请求模型定义=====


class MessageRequest(BaseModel):
    """消息请求模型"""

    role: str = Field(..., description="消息角色(如: user, assistant)")
    content: str = Field(..., description="消息内容")


class ProcessMessagesRequest(BaseModel):
    """处理消息请求模型"""

    conversation_id: str = Field(..., description="会话ID")
    messages: List[MessageRequest] = Field(..., description="消息列表")


class SearchMemoryRequest(BaseModel):
    """搜索记忆请求模型"""

    query: str = Field(..., description="搜索查询内容")
    num: int = Field(10, gt=0, le=100, description="返回结果数量(1-100)")


class ListMemoriesRequest(BaseModel):
    """列出记忆请求模型"""

    offset: int = Field(0, ge=0, description="分页偏移量")
    size: int = Field(10, ge=1, le=100, description="每页数量(1-100)")


class UpdateMemoryRequest(BaseModel):
    """更新记忆请求模型"""

    content: str = Field(..., description="新的记忆内容")


class MemoryUpdateItem(BaseModel):
    """记忆更新项"""

    memory_id: str = Field(..., description="记忆ID")
    content: str = Field(..., description="新的记忆内容")


class BatchUpdateMemoryRequest(BaseModel):
    """批量更新记忆请求模型"""

    memories: List[MemoryUpdateItem] = Field(
        ..., description="记忆更新列表", alias="natural_language_memories"
    )


# =====响应模型定义=====


class MessageResponse(BaseModel):
    """消息响应模型"""

    role: str
    content: str
    timestamp: Optional[str]


class ProcessedMemoryResponse(BaseModel):
    """处理后记忆响应模型"""

    success: bool = Field(..., description="是否开始提取记忆")


class SearchResultItem(BaseModel):
    """搜索结果项"""

    id: str = Field(..., description="记忆ID")
    content: str = Field(..., description="记忆内容")
    type: str = Field(..., description="记忆类型")
    last_update_time: int = Field(..., description="最后更新时间")
    score: Optional[float] = Field(None, description="搜索相关性分数")


class SearchResponse(BaseModel):
    """搜索记忆响应模型"""

    total: int = Field(..., description="总结果数")
    items: List[SearchResultItem] = Field(..., description="搜索结果列表")


class MemoryItem(BaseModel):
    """记忆项"""

    memory_id: str = Field(..., description="记忆ID")
    content: str = Field(..., description="记忆内容")
    type: str = Field(..., description="记忆类型")
    last_update_time: int = Field(..., description="最后更新时间")


class ListResponse(BaseModel):
    """列出记忆响应模型"""

    total: int = Field(..., description="总记忆数")
    natural_language_memories: List[MemoryItem] = Field(..., description="记忆列表")


class OperationResponse(BaseModel):
    """操作响应模型"""

    success: bool = Field(..., description="操作是否成功")


class BatchOperationResponse(BaseModel):
    """批量操作响应模型"""

    successful_ids: List[str] = Field(..., description="成功操作的记忆ID列表")
    failed_ids: List[str] = Field(..., description="失败操作的记忆ID列表")


@router.post("/process", response_model=ProcessedMemoryResponse)
async def process_messages(
    project_id: str = Path(..., description="项目ID"),
    app_id: str = Query(..., description="虚拟应用ID", example="fund_manager"),
    request: ProcessMessagesRequest = Body(..., description="提取记忆的请求体"),
):
    """
    处理用户消息，提取相关信息并创建记忆

    Args:
        project_id: 项目ID
        app_id: 应用ID(对应scope_id)
        request: 处理消息请求

    Returns:
        ProcessedMemoryResponse: 处理结果，包含生成的记忆和触发的主题
    """
    try:
        # 如果没有配置向量库相关配置，不做进一步提取
        if EMBEDDING_TYPE.lower() == "none" or VECTOR_STORE_TYPE.lower() == "none":
            return ProcessedMemoryResponse(success=True)

        mem_engine = get_mem_engine()

        # 转换请求格式
        messages = []
        for msg in request.messages:
            messages.append(BaseMessage(type=msg.role, content=msg.content))

        user_id = get_user_id()
        # 调用engine方法
        result = await mem_engine.process_messages(
            user_id=user_id,
            scope_id=app_id,
            messages=messages,
            session_id=request.conversation_id,
            topics=[],
            mode=ProcessMemoryMode.REALTIME_MODE,
        )
        asyncio.create_task(_send_memory_update_event(app_id, user_id, request, result))

        # 转换响应格式
        return ProcessedMemoryResponse(success=True)
    except MemoryServiceException as e:
        logger.error(f"Process messages failed: {e}", exc_info=True)
        raise e
    except Exception as e:
        logger.error(f"Process messages failed: {e}", exc_info=True)
        # 按照工程里的错误码机制抛出异常
        raise MemoryServiceException(
            error_code=ErrorCode.EXTRACT_MEMORY_FAILED,
            error_message=i18n_util.get_message(
                I18nMessageCode.EXTRACT_MEMORY_FAILED_MESSAGE
            ),
            error_reason=i18n_util.get_message(
                I18nMessageCode.EXTRACT_MEMORY_FAILED_REASON
            ),
            error_suggestion=i18n_util.get_message(
                I18nMessageCode.EXTRACT_MEMORY_FAILED_SUGGESTION
            ),
        )


async def _send_memory_update_event(app_id, user_id, request, result):
    if result.user_memory:
        # 记录记忆更新事件
        memories_update = []
        for mem in result.user_memory:
            memories_update.append(
                NaturalLanguageMemoryEventData(
                    memory_id="",
                    content=mem.memory_content,
                    operation="DELETE" if mem.operation_type == "DELETE" else "UPDATE",
                )
            )
        await event_service.record_memory_refresh_event(
            user_id=user_id,
            app_id=app_id,
            conversation_id=request.conversation_id,
            tags=[],
            memories=memories_update,
        )


@router.get("/search", response_model=SearchResponse)
async def search_user_mem(
    project_id: str = Path(..., description="项目ID"),
    app_id: str = Query(..., description="虚拟应用ID", example="fund_manager"),
    query: str = Query(..., description="搜索查询内容"),
    num: int = Query(10, ge=1, le=100, description="返回结果数量(1-100)"),
):
    """
    搜索用户记忆

    Args:
        app_id: 应用ID(对应scope_id)
        query: 搜索查询内容
        num: 返回结果数量，默认10，最大100

    Returns:
        SearchResponse: 搜索结果，包含总结果数和结果列表
    """
    try:
        mem_engine = get_mem_engine()

        # 调用engine方法
        results = await mem_engine.search_user_mem(
            user_id=get_user_id(), scope_id=app_id, query=query, num=num
        )

        # 转换响应格式
        search_items = []
        for item in results:
            search_items.append(
                SearchResultItem(
                    id=item.get("id", ""),
                    content=item.get("mem", ""),
                    type=item.get("mem_type", ""),
                    last_update_time=item.get("timestamp").timestamp() * 1000
                    if item.get("timestamp")
                    else 0,
                    score=item.get("score"),
                )
            )

        return SearchResponse(
            total=len(search_items),  # 当前返回的数量
            items=search_items,
        )

    except MemoryServiceException as e:
        logger.error(f"Search user memories failed: {e}", exc_info=True)
        raise e
    except Exception as e:
        logger.error(f"Search user memories failed: {e}", exc_info=True)
        # 按照工程里的错误码机制抛出异常
        raise MemoryServiceException(
            error_code=ErrorCode.LONG_TERM_MEMORY_SEARCH_FAILED,
            error_message=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_MESSAGE
            ),
            error_reason=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_REASON
            ),
            error_suggestion=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_SUGGESTION
            ),
        )


@router.get("", response_model=ListResponse)
async def list_user_mem(
    project_id: str = Path(..., description="项目ID"),
    app_id: str = Query(..., description="虚拟应用ID", example="fund_manager"),
    offset: int = Query(0, ge=0, le=10000, description="分页偏移量"),
    limit: int = Query(10, ge=1, le=200, description="每页数量"),
):
    """
    列出用户记忆

    Args:
        app_id: 应用ID(对应scope_id)
        offset: 分页偏移量，默认0
        limit: 每页数量，默认10，最大100

    Returns:
        ListResponse: 记忆列表，包含总数和分页信息
    """
    try:
        # 当前此接口用于页面展示记忆列表，记忆数量有限制，暂时不支持分页，以配置项中的最大记忆数量为主
        limit = USER_MAX_MEMORY_COUNT
        mem_engine = get_mem_engine()

        # 调用engine方法
        memories = await mem_engine.list_user_mem(
            user_id=get_user_id(), scope_id=app_id, offset=offset, size=limit
        )

        # 转换响应格式
        memory_items = []
        for mem in memories:
            memory_items.append(
                MemoryItem(
                    memory_id=mem.get("id", ""),
                    content=mem.get("mem", ""),
                    type=mem.get("mem_type", ""),
                    last_update_time=mem.get("timestamp").timestamp() * 1000
                    if mem.get("timestamp")
                    else 0,
                )
            )

        return ListResponse(
            total=len(memory_items),  # 当前返回的数量
            natural_language_memories=memory_items,
        )
    except MemoryServiceException as e:
        raise e
    except Exception as e:
        logger.error(f"List user memories failed: {e}", exc_info=True)
        # 按照工程里的错误码机制抛出异常
        raise MemoryServiceException(
            error_code=ErrorCode.LONG_TERM_MEMORY_SEARCH_FAILED,
            error_message=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_MESSAGE
            ),
            error_reason=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_REASON
            ),
            error_suggestion=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SEARCH_FAILED_SUGGESTION
            ),
        )


@router.put("", response_model=BatchOperationResponse)
async def batch_update_memory(
    project_id: str = Path(..., description="项目ID"),
    app_id: str = Query(..., description="虚拟应用ID", example="fund_manager"),
    request: BatchUpdateMemoryRequest = Body(..., description="批量更新记忆的请求体"),
):
    """
    批量更新记忆内容

    Args:
        project_id: 项目ID
        app_id: 应用ID(对应scope_id)
        request: 批量更新请求，包含记忆更新列表

    Returns:
        BatchOperationResponse: 批量操作结果，包含成功和失败的记忆ID列表
    """
    try:
        mem_engine = get_mem_engine()
        successful_ids = []
        failed_ids = []

        # 批量调用engine方法
        for update_item in request.memories:
            try:
                success = await mem_engine.update_mem_by_id(
                    mem_id=update_item.memory_id,
                    memory=update_item.content,
                    user_id=get_user_id(),
                    scope_id=app_id,
                )
                if success:
                    successful_ids.append(update_item.memory_id)
                else:
                    failed_ids.append(update_item.memory_id)
            except Exception as e:
                logger.error(
                    f"Update memory failed for mem_id {update_item.memory_id}: {e}",
                    exc_info=True,
                )
                failed_ids.append(update_item.memory_id)

        return BatchOperationResponse(
            successful_ids=successful_ids, failed_ids=failed_ids
        )

    except MemoryServiceException as e:
        raise e
    except Exception as e:
        logger.error(f"Batch update memory failed: {e}", exc_info=True)
        # 按照工程里的错误码机制抛出异常
        raise MemoryServiceException(
            error_code=ErrorCode.LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED,
            error_message=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_MESSAGE
            ),
            error_reason=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_REASON
            ),
            error_suggestion=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_SAVE_MEMORIES_FAILED_SUGGESTION
            ),
        )


@router.delete("/batch-delete", response_model=BatchOperationResponse)
async def batch_delete_memory(
    project_id: str = Path(..., description="项目ID"),
    app_id: str = Query(..., description="虚拟应用ID", example="fund_manager"),
    mem_ids: str = Query(
        ..., description="要删除的记忆ID列表，用逗号分隔，例如: id1,id2,id3"
    ),
):
    """
    批量删除记忆

    Args:
        project_id: 项目ID
        app_id: 应用ID(对应scope_id)
        mem_ids: 要删除的记忆ID列表，用逗号分隔

    Returns:
        BatchOperationResponse: 批量操作结果，包含成功和失败的记忆ID列表
    """
    try:
        mem_engine = get_mem_engine()
        successful_ids = []
        failed_ids = []

        # 解析记忆ID列表
        memory_ids = mem_ids.split(",") if mem_ids else []

        # 批量调用engine方法
        for mem_id in memory_ids:
            try:
                success = await mem_engine.delete_mem_by_id(
                    mem_id=mem_id.strip(), user_id=get_user_id(), scope_id=app_id
                )
                if success:
                    successful_ids.append(mem_id.strip())
                else:
                    failed_ids.append(mem_id.strip())
            except Exception as e:
                logger.error(
                    f"Delete memory failed for mem_id {mem_id}: {e}", exc_info=True
                )
                failed_ids.append(mem_id.strip())

        return BatchOperationResponse(
            successful_ids=successful_ids, failed_ids=failed_ids
        )

    except MemoryServiceException as e:
        raise e
    except Exception as e:
        logger.error(f"Batch delete memory failed: {e}", exc_info=True)
        # 按照工程里的错误码机制抛出异常
        raise MemoryServiceException(
            error_code=ErrorCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED,
            error_message=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_MESSAGE
            ),
            error_reason=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_REASON
            ),
            error_suggestion=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_SUGGESTION
            ),
        )


@router.delete("", response_model=OperationResponse)
async def delete_memories(
    project_id: str = Path(..., description="项目ID"),
    app_id: str = Query(..., description="虚拟应用ID", example="fund_manager"),
    user_id: str = Query(None, description="用户ID", example="user123"),
):
    """
    删除指定用户的所有记忆(此处会删除存储到向量库中的记忆以及Redis中的记忆)

    Args:
        user_id: 用户ID
        app_id: 应用ID(对应scope_id)

    Returns:
        OperationResponse: 操作结果
    """
    try:
        mem_engine = get_mem_engine()

        # 调用engine方法
        if user_id is None:
            # 如果用户ID是空的，则按照应用删除记忆
            success = await mem_engine.delete_memory_by_scope_id(scope_id=app_id)
            return OperationResponse(
                success=success,
            )
        else:
            # 如果用户ID不是空的，则按照应用删除某个用户的记忆
            success = await mem_engine.delete_mem_by_user_id(
                user_id=user_id, scope_id=app_id
            )
            return OperationResponse(
                success=success,
            )

    except MemoryServiceException as e:
        raise e
    except Exception as e:
        logger.error(
            f"Delete memories failed for user {user_id} and app {app_id}: {e}",
            exc_info=True,
        )
        # 按照工程里的错误码机制抛出异常
        raise MemoryServiceException(
            error_code=ErrorCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED,
            error_message=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_MESSAGE
            ),
            error_reason=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_REASON
            ),
            error_suggestion=i18n_util.get_message(
                I18nMessageCode.LONG_TERM_MEMORY_DELETE_MEMORIES_FAILED_SUGGESTION
            ),
        )
