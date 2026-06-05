import logging
from typing import List

from fastapi import APIRouter, Path, Query
from memory_service.utils.request_context_util import get_user_id
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


class UserVariableValue(BaseModel):
    """
    用户变量的值
    """

    variable_key: str = Field(description="变量名称")
    variable_value: str = Field(description="变量的值")


class GetUserVariableValuesResponseBody(BaseModel):
    """
    用户变量列表
    """

    variables: List[UserVariableValue] = Field(default=None, description="变量值的列表")


router = APIRouter(
    prefix="/private/v1/{project_id}/memories/variables", tags=["user_variable_private"]
)


@router.get("", response_model=GetUserVariableValuesResponseBody)
async def get_user_variable_values(
    project_id: str = Path(..., description="租户项目id", min_length=1, max_length=64),
    app_id: str = Query(
        ..., description="记忆的虚拟应用ID", min_length=1, max_length=64
    ),
):
    """
    查看某个应用中用户变量记忆的值

    Args:
        project_id: 租户项目id
        app_id: 记忆的虚拟应用ID

    Returns:
        用户变量记忆值的响应体
    """
    logger.info(
        f"Getting user variable values for project: {project_id}, application: {app_id}"
    )

    from memory_service.memory.memory_engine_util import get_mem_engine

    # 从内存引擎获取用户画像记忆值
    user_variable_values = await get_mem_engine().get_user_variables(
        user_id=get_user_id(), scope_id=app_id
    )
    if not user_variable_values:
        return GetUserVariableValuesResponseBody(variables=[])

    variables = []
    for k, v in user_variable_values.items():
        variable = UserVariableValue(variable_key=k, variable_value=v)
        variables.append(variable)

    return GetUserVariableValuesResponseBody(variables=variables)
