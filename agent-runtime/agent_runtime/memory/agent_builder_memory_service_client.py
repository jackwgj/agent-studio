import json
import os
from datetime import datetime
from typing import Optional, Any

import aiohttp
from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.language_model.base import BaseChatModel
from jiuwen.common.llm_service.messages import BaseMessage
from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.client.local_memory_client import LocalMemoryClient
from jiuwen.context.memory_engine.client.memory_client import MemoryClient
from jiuwen.context.memory_engine.common.base import ProcessedMemResult
from jiuwen.context.memory_engine.config.config import (
    MemoryScopeConfig,
    ProcessMemoryMode,
)
from jiuwen.context.memory_engine.engine.memory_engine import MemoryEngine
from jiuwen.context.memory_engine.store import (
    BaseKVStore,
    BaseSemanticStore,
    BaseDbStore,
)
from jiuwen.serve.common.context import request as current_request
from memory.kv_store_redis_impl import KVStoreRedisImpl
from pydantic import BaseModel, Field


class VariableValue(BaseModel):
    """
    变量值数据结构
    """

    variable_key: str = Field(..., description="变量的Key")
    variable_value: str = Field(..., description="变量的Value")
    agent_id: str = Field(..., description="智能体ID")
    user_id: str = Field(..., description="用户ID")


class VariableList(BaseModel):
    """
    变量列表
    """

    variables: Optional[list[VariableValue]] = Field([], description="变量列表")


class VariableKeyList(BaseModel):
    """
    变量的key列表
    """

    variable_keys: Optional[list[str]] = Field([], description="变量的key列表")


class AgentBuilderMemoryClient(MemoryClient):
    """
    memory-service客户端，用来查询用户的记忆信息

    Attributes:
        base_url: memory-server的基础URL
        project_id: 项目ID
    """

    def __init__(self):
        """
        初始化客户端

        """
        self.base_url = os.environ.get("MEMORY_SERVICE_ENDPOINT")
        memory_service_cert_path = os.environ.get("MEMORY_SERVICE_CERT_PATH", "")
        self.ssl_verify = (
            memory_service_cert_path if memory_service_cert_path else False
        )
        self._session = None
        # 对于记忆变量，还是使用本地的MemoryClient
        self.local_mem_engine = LocalMemoryClient()
        self.kv_store = KVStoreRedisImpl()
        engine_instance = MemoryEngine()
        engine_instance.register_store(self.kv_store)

    async def _ensure_client(self):
        """确保HTTP客户端已初始化"""
        if self._session is None:
            await self._initialize_client()

    async def _initialize_client(self):
        """懒初始化HTTP客户端"""
        if self._session is None:
            # 创建 TCP 连接器，忽略SSL证书验证
            self._session = aiohttp.ClientSession(
                connector=aiohttp.TCPConnector(ssl=self.ssl_verify)
            )

    async def list_user_variables(self, user_id: str, scope_id: str) -> dict[str, str]:
        return await self.local_mem_engine.list_user_variables(user_id, scope_id)

    async def set_scope_config(self, scope_id: str, config: MemoryScopeConfig):
        await self.local_mem_engine.set_scope_config(scope_id, config)

    async def delete_scope_config(self, scope_id: str) -> bool:
        return await self.local_mem_engine.delete_scope_config(scope_id)

    async def process_messages(
        self,
        messages: list[BaseMessage],
        user_id: str,
        scope_id: str,
        session_id: str,
        mode: ProcessMemoryMode = ProcessMemoryMode.BATCH_MODE,
        timestamp: datetime | None = None,
    ) -> ProcessedMemResult:
        return await self.local_mem_engine.process_messages(
            messages, user_id, scope_id, session_id, mode, timestamp
        )

    async def init_base_llm(self, llm: BaseChatModel) -> bool:
        return self.local_mem_engine.init_base_llm(llm)

    async def search_user_mem(
        self,
        user_id: str,
        scope_id: str,
        query: str,
        num: int,
        mem_type: Optional[str] = None,
        messages: Optional[list[BaseMessage]] = None,
    ) -> list[Any] | None:
        """
        调用 memory-service的/long-term-memories/search检索记忆

        Args:
            user_id: 用户id
            scope_id: 记忆库ID
            query: 用户输入
            num: 返回的检索结果数量
            mem_type: 记忆类型. 如果为None则检索记忆库的所有类型
            messages: 对话信息

        Returns:
        """
        try:
            await self._ensure_client()
            if not query:
                return []

            url = f"{self.base_url}/private/v1/{self._get_project_id()}/long-term-memories/search"

            params = {"memory_repo_id": scope_id, "query": query, "top_k": num}

            # 获取通用header
            headers = self.get_common_headers()

            # 创建aiohttp客户端获取响应
            async with self._session.get(
                url, params=params, headers=headers
            ) as response:
                if response.status == 200:
                    response_data = await response.json()
                    if not response_data["items"]:
                        return []
                    memories = []
                    for item in response_data["items"]:
                        memories.append(
                            {
                                "id": item["id"],
                                "mem": item["content"],
                                "mem_type": item["type"],
                            }
                        )
                    return memories
                else:
                    logger.error(
                        f"Call memory-service to search long term memories failed. Error message:{response}"
                    )
                    raise JiuWenBaseException(
                        error_code=StatusCode.WORKFLOW_CODE_RETURN_VALUE_TYPE_ERROR.code,
                        message="Call memory-service to search long term memories failed.",
                    )

        except Exception as e:
            logger.error(
                f"Call memory-service to search long term memories failed. Error message:{e}"
            )

    async def extract_memory_real_time(
        self, messages: list[BaseMessage], conversation_id: str, memory_repo_id: str
    ):
        """
        调用 memory-service的/long-term-memories/process接口提取记忆

        Args:
            messages: 消息列表
            conversation_id: 会话ID
            memory_repo_id: 记忆库ID

        Returns:
            用户画像主题结果列表
        """
        try:
            await self._ensure_client()
            if not messages:
                return

            message_request = []
            for msg in messages:
                message_request.append({"role": msg.type, "content": msg.content})

            request_body = {
                "conversation_id": conversation_id,
                "messages": message_request,
            }

            url = f"{self.base_url}/private/v1/{self._get_project_id()}/long-term-memories/extract"

            params = {"memory_repo_id": memory_repo_id}

            # 获取通用header
            headers = self.get_common_headers()

            # 创建aiohttp客户端获取响应
            async with self._session.post(
                url, params=params, headers=headers, json=request_body
            ) as response:
                if response.status == 200:
                    response_data = await response.json()
                    logger.info(
                        "Call memory-service to extract long term memories success"
                    )
                else:
                    logger.error(
                        f"Call memory-service to extract long term memories failed. Error message:{response}"
                    )
                    raise JiuWenBaseException(
                        error_code=StatusCode.WORKFLOW_CODE_RETURN_VALUE_TYPE_ERROR.code,
                        message="Call memory-service to extract long term memories failed.",
                    )

        except Exception as e:
            logger.error(
                f"Call memory-service to extract long term memories failed. Error message:{e}"
            )

    async def retrieve_user_variables(
        self, user_id: str, agent_id: str, variable_keys: Optional[list[str]]
    ) -> VariableList:
        """
        查询用户变量列表

        Args:
            user_id: 用户ID
            agent_id: 智能体ID
            variable_keys: 变量key列表，如果为空则返回所有变量

        Returns:
            变量列表
        """
        # 先查询出所有的变量值，然后过滤出接口需要的值，如果传入的variable_keys是空的，则返回所有的值
        all_variables = await self.list_user_variables(user_id, agent_id)
        if not all_variables:
            return VariableList(variables=[])

        # 如果variable_keys为空，返回所有变量
        if not variable_keys:
            variables = []
            for key, value in all_variables.items():
                variables.append(
                    VariableValue(
                        variable_key=key,
                        variable_value=value,
                        agent_id=agent_id,
                        user_id=user_id,
                    )
                )
            return VariableList(variables=variables)

        # 如果有指定的variable_keys，只返回这些key对应的变量
        variables = []
        for key in variable_keys:
            if key in all_variables:
                variables.append(
                    VariableValue(
                        variable_key=key,
                        variable_value=all_variables[key],
                        agent_id=agent_id,
                        user_id=user_id,
                    )
                )
        return VariableList(variables=variables)

    async def batch_delete_user_variables(
        self, user_id: str, agent_id: str, variable_keys: Optional[list[str]]
    ) -> list[str]:
        """
        删除用户的变量值

        Args:
            user_id: 用户ID
            agent_id: 智能体ID
            variable_keys: 待删除的变量key列表，如果为空则表示不删除，直接返回

        Returns:
            变量列表
        """
        # 先查询出所有的变量值，然后删除指定的key，再把数据重新写入到redis中，写入redis时，需要把dict作为value存到redis中
        if not variable_keys:
            return []

        all_variables = await self.list_user_variables(user_id, agent_id)
        if not all_variables:
            return []

        # 删除指定的变量key
        deleted_keys = []
        for key in variable_keys:
            if key in all_variables:
                del all_variables[key]
                deleted_keys.append(key)

        # 构建redis的key并更新到redis中
        redis_key = self._build_variable_memory_key(user_id, agent_id)
        if all_variables:
            await self.kv_store.set(
                redis_key, json.dumps(all_variables, ensure_ascii=False)
            )
        else:
            # 删除变量之后，如果已经没有变量了，直接删除对应的key
            await self.kv_store.delete(redis_key)

        return deleted_keys

    async def reset_user_variable_memory(self, user_id: str, agent_id: str) -> bool:
        """
        重置用户的变量值（删除用户的变量记忆）

        Args:
            user_id: 用户ID
            agent_id: 智能体ID

        Returns:
            操作结果
        """
        # 直接从Redis中删除对应的Key
        redis_key = self._build_variable_memory_key(user_id, agent_id)
        await self.kv_store.delete(redis_key)

        return True

    def set_scope_llm(self, scope_id: str, llm: BaseChatModel) -> bool:
        return self.local_mem_engine.init_base_llm(llm)

    def register_store(
        self,
        kv_store: BaseKVStore,
        semantic_store: BaseSemanticStore | None = None,
        db_store: BaseDbStore | None = None,
    ):
        return self.local_mem_engine.register_store(kv_store)

    def _build_variable_memory_key(self, user_id: str, agent_id: str) -> str:
        return f"user_var/{user_id}/{agent_id}"

    def _get_project_id(self) -> str:
        return current_request.get().headers.get("x-owner-project-id", "")

    def _get_auth_token(self) -> str:
        return current_request.get().headers.get("X-Auth-Token")

    def get_common_headers(self) -> dict:
        """
        获取通用请求头

        Returns:
            通用header字典
        """
        headers = {"X-Auth-Token": self._get_auth_token()}
        return headers
