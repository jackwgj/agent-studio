# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""This module contains an IRConverter."""

import json
import os
import re
import secrets
from collections import defaultdict
from typing import Any, Dict, Iterable, List, Optional

from agent_runtime.extension.workflow_node.flow_code import FlowCode
from agent_runtime.extension.workflow_node.questioner import (
    FieldInfo,
    Questioner,
    QuestionerConfig,
)
from jiuwen.common.configs.env_constants import (
    AGENT_CACHE_ENABLE_KEY,
    EXECUTION_SUPPORTED_IR_VERSIONS_KEY,
)
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger, get_x_execution_id
from jiuwen.common.utils.utils import safe_json_loads_raise_exception, timed_cache_op
from jiuwen.context.memory_engine.config.memory_ir_config import MemoryIrConfig
from jiuwen.controller.agent.agent import Agent
from jiuwen.controller.common.config import AgentConfig, AgentMetaData
from jiuwen.extension.patches.workflow_sub_stream_patch import (
    apply_workflow_sub_stream_patch,
)
from jiuwen.extension.workflow_node.end import End
from jiuwen.extension.workflow_node.exception_handler import (
    register_error_recovery_handler,
)
from jiuwen.extension.workflow_node.flow_agent import FlowAgent, FlowAgentConfig
from jiuwen.extension.workflow_node.flow_aggregate import Aggregate
from jiuwen.extension.workflow_node.flow_api import FlowApi
from jiuwen.extension.workflow_node.flow_card import FlowCard, FlowCardConfig
from jiuwen.extension.workflow_node.flow_exception import ExceptionInfo
from jiuwen.extension.workflow_node.flow_extractor import Extractor
from jiuwen.extension.workflow_node.flow_input import FlowInput
from jiuwen.extension.workflow_node.flow_mcp import FlowMcp
from jiuwen.extension.workflow_node.flow_message import Message
from jiuwen.extension.workflow_node.flow_qa import FlowQA
from jiuwen.extension.workflow_node.flow_stream_transform import FlowStreamTransform
from jiuwen.extension.workflow_node.intent_detection import IntentDetection
from jiuwen.extension.workflow_node.llm_chain import LLMChain
from jiuwen.extension.workflow_node.loop_set_variable import LoopSetVariable
from jiuwen.extension.workflow_node.start import Start
from jiuwen.extension.workflow_node.sub_workflow import SubWorkflow
from jiuwen.extension.wrapper.single_component_debug_wrapper import SingleComponentInfo
from jiuwen.multi_agent.agent_group.config import AgentGroupConfig, GroupSettings
from jiuwen.multi_agent.agent_group.hierarchical_group.agent_group import (
    HierarchicalAgentGroup,
)
from jiuwen.orchestration.flow.components.util import (
    format_pydantic_validation_error_message,
)
from jiuwen.orchestration.flow.enum import NodeType
from jiuwen.serve.controllers.execution.constants import DEFAULT_SUPPORTED_IR_VERSIONS
from jiuwen.serve.controllers.execution.enum import IRType
from jiuwen.serve.controllers.execution.manager import AsyncStateManager
from jiuwen.serve.controllers.execution.open_utils import (
    async_ir_load,
    deserialize_object,
    cache_agent_queue,
    cache_agent_group_queue,
)
from jiuwen.serve.controllers.execution.types import AgentIrValidator
from jiuwen.serve.controllers.execution.utils import AgentIrUtils, PluginIRConverter
from openjiuwen.core.common.exception.errors import (
    ValidationError as OpenjiuwenValidationError,
)
from openjiuwen.core.workflow import Workflow, WorkflowCard
from openjiuwen.core.workflow.components.base import ComponentAbility
from openjiuwen.core.workflow.components.component import WorkflowComponent
from openjiuwen.core.workflow.components.flow.branch_comp import BranchComponent
from openjiuwen.core.workflow.components.flow.branch_router import BranchRouter
from openjiuwen.core.workflow.components.flow.loop.loop_comp import (
    LoopComponent,
    LoopGroup,
    LoopBreakComponent,
)
from openjiuwen.core.workflow.workflow_config import ExceptionConfig
from pydantic import ValidationError

apply_workflow_sub_stream_patch()
from jiuwen.extension.workflow_node.utils import WorkflowMetadata

from jiuwen.serve.controllers.execution.ir_parallel_utils import (
    collect_parallel_join_nodes,
)
from agent_runtime.common.ir_exceptions import IRBuildException
from agent_runtime.common.ir_interfaces import StorageConfigError, StorageReadError
from agent_runtime.storage import get_storage_provider

_AGENT_VERSION = "agentVersion"
_WORKFLOW_VERSION = "workflowVersion"
_USE_AGENT_CORE_MODEL_ENV = "USE_AGENT_CORE_MODEL"
_BASE_AGENT_SWITCH = "BASE_AGENT_SWITCH"
_AGENT_CORE_REGISTERED_MODEL_IDS = set()
UNSUPPORTED_COMPONENT_DEBUG_LIST = [
    "jiuwen.start",
    "jiuwen.end",
    "jiuwen.message",
    "jiuwen.branch",
    "jiuwen.taskFlow",
]

_REFERENCE_PATTERN = re.compile(r"^\{([^{}]+)}$")
_REFERENCE_TOKEN_PATTERN = re.compile(r"(?<!\$)\{([^{}]+)}")
_OPEN_REFERENCE_PATTERN = re.compile(r"^\$\{([^{}]+)}$")
_OPEN_REFERENCE_TOKEN_PATTERN = re.compile(r"\$\{([^{}]+)}")
_NEGATION_PATTERN = re.compile(r"(?<![=!<>])!(?!=)")
_REFERENCE_ALIASES = {
    "system_fields": "systemFields",
    "user_fields": "userFields",
    "conversation_history": "conversationHistory",
    "dialogue_history": "dialogueHistory",
    "rawOutput": "raw_output",
}

_LOOP_TYPE_MAP = {
    "numLoop": "number",
    "arrayLoop": "array",
    "infiniteLoop": "always_true",
}

_SPECIAL_REF_ROOTS = frozenset(
    {
        "query",
        "sys",
        "memory",
        "conversationHistory",
        "dialogueHistory",
        "global",
    }
)


def _extract_source_component_ids(
    schema: dict, component_by_id: dict[str, Any]
) -> set[str]:
    """Extract source component IDs from reference paths in a schema.

    Parses the schema for reference patterns like ${node_id.field} and returns
    the set of source component IDs that exist in component_by_id.
    Skips special references (query, sys, global, etc.).
    """
    source_ids: set[str] = set()
    for ref_path in _iter_reference_paths(schema):
        source_id = ref_path.split(".", 1)[0]
        if not source_id or source_id in _SPECIAL_REF_ROOTS:
            continue
        if source_id in component_by_id:
            source_ids.add(source_id)
    return source_ids


class _LoopPassThroughComponent(WorkflowComponent):
    """空操作组件（对应官方文档的 EmptyNode），用作 break 条件路由流的循环体末端节点。
    无论 break 还是 continue 路径，迭代结束后都经过此节点。"""

    async def invoke(self, inputs, session, context):
        return {}


class _RoutedIntentDetection(IntentDetection):
    def __init__(self, configs: dict):
        super().__init__(configs)
        self._router = BranchRouter(True)

    def add_branch(
        self, condition: Any, target: str | list[str], branch_id: str | None = None
    ) -> None:
        self._router.add_branch(condition, target, branch_id=branch_id)

    async def invoke(
        self, inputs: Any, session: Any, context: Any, **kwargs: Any
    ) -> Any:
        self._router.set_session(session)
        return await super().invoke(inputs, session, context, **kwargs)

    def add_component(
        self, graph: Any, node_id: str, wait_for_all: bool = False
    ) -> None:
        graph.add_node(node_id, self.to_executable(), wait_for_all=wait_for_all)
        graph.add_conditional_edges(node_id, self._router)
        # Register branch targets for CNF barrier resolution at compile time
        if hasattr(graph, "register_branch_targets"):
            all_targets = self._router.all_targets
            if len(all_targets) > 1:
                graph.register_branch_targets(node_id, all_targets)


class IRConverter:
    """IR转换工具类。

    该类用于识别和转换中间表示(IR)数据，包括验证IR版本、创建Agent配置、
    从IR数据创建Agent和Agent Group配置、将IR数据转换为Agent或Agent Group实例等。
    """

    @staticmethod
    def extract_component_name_type_map(ir_data: dict) -> dict[str, dict]:
        """从 workflow IR 数据中提取 {component_id: {name, type}} 映射。

        用于在 TraceSchema → StreamData 转换时，用 IR 中定义的显示名称
        和类型覆盖 openjiuwen tracer 生成的 node_id 格式 componentName。

        Args:
            ir_data: Workflow IR 数据字典，包含 components 数组。

        Returns:
            dict: {component_id: {"name": str, "type": str}} 映射字典。
        """
        result: dict[str, dict] = {}
        components = ir_data.get("components") or []
        for comp in components:
            comp_id = comp.get("id")
            if not comp_id:
                continue
            result[comp_id] = {
                "name": comp.get("name", ""),
                "type": comp.get("type", ""),
            }
        return result

    @staticmethod
    def validate_ir_version(ir_data: dict, ir_type: IRType):
        """
        Validate whether the IR data if of the supported version.
        :param ir_data: Dictionary parsed from JSON IR file.
        :param ir_type: IRType 'Agent' or 'Workflow'.
        """
        if ir_type == IRType.Agent and _AGENT_VERSION not in ir_data:
            raise JiuWenBaseException(
                error_code=StatusCode.IR_DATA_VALIDATION_ERROR.code,
                message=f"IR missing field - {_AGENT_VERSION}",
            )
        if ir_type == IRType.Workflow and _WORKFLOW_VERSION not in ir_data:
            raise JiuWenBaseException(
                error_code=StatusCode.IR_DATA_VALIDATION_ERROR.code,
                message=f"IR missing field - {_WORKFLOW_VERSION}",
            )

        supported_ir_version = os.environ.get(
            EXECUTION_SUPPORTED_IR_VERSIONS_KEY, DEFAULT_SUPPORTED_IR_VERSIONS
        )
        try:
            supported_ir_version_lst = safe_json_loads_raise_exception(
                supported_ir_version
            )
        except ValueError as e:
            raise JiuWenBaseException(
                error_code=StatusCode.IR_VERSION_INCORRECTLY_CONFIGURED.code,
                message=StatusCode.IR_VERSION_INCORRECTLY_CONFIGURED.errmsg,
            ) from e
        if not isinstance(supported_ir_version_lst, list):
            raise JiuWenBaseException(
                error_code=StatusCode.IR_VERSION_INCORRECTLY_CONFIGURED.code,
                message=StatusCode.IR_VERSION_INCORRECTLY_CONFIGURED.errmsg,
            )
        ir_version = ir_data.get(_AGENT_VERSION) or ir_data.get(_WORKFLOW_VERSION) or ""
        if ir_version not in supported_ir_version_lst:
            raise JiuWenBaseException(
                error_code=StatusCode.IR_VERSION_NOT_SUPPORTED.code,
                message=StatusCode.IR_VERSION_NOT_SUPPORTED.errmsg,
            )

    @staticmethod
    async def create_agent_config(
        ir_data, llm, task_model, task_id, conversation_id=""
    ):
        """
        Create agent configuration from IR data.
        """
        workflows = await AgentIrUtils.get_controller_workflow_configs(ir_data)
        intent_identification = AgentIrUtils.get_intent_identification_configs(ir_data)

        # 从 IR 中提取 plugins 配置
        agent_id = ir_data.get("agentId", "Default_Agent_Id")
        agent_version = ir_data.get("agentVersion", "Default_Agent_Version")
        agent_id_in_config = f"{agent_id}_{agent_version}"
        plugin_irs = ir_data.get("configs", {}).get("plugins")
        plugins = None
        if plugin_irs:
            plugins = [
                PluginIRConverter.ir_to_plugin(plugin_ir, agent_id, conversation_id)
                for plugin_ir in plugin_irs
            ]

        # 从 IR 中提取 skills 配置
        skills_config = ir_data.get("configs", {}).get("skills", {})
        skill_dir = (
            skills_config.get("skill_dir", "")
            if isinstance(skills_config, dict)
            else ""
        )
        skill_info = (
            skills_config.get("skill_info", [])
            if isinstance(skills_config, dict)
            else []
        )

        return AgentConfig(
            model=llm,
            plan_config=task_model.configs.plan_config,
            plugins=plugins,
            workflows=workflows.normal,
            start_workflow=workflows.start,
            end_workflow=workflows.end,
            default_workflow=workflows.default,
            global_workflows=workflows.global_workflows,
            task_id=task_id,
            user_prompt=task_model.configs.sys_prompt_template,
            intent_workflows=workflows.intent,
            intent_identification=intent_identification,
            context=task_model.configs.context,
            memory_config=task_model.configs.memory_config,
            agent_id=agent_id_in_config,
            skill_dir=skill_dir,
            skill_info=skill_info,
        )

    @staticmethod
    async def create_all_agents_config_list(root_ir_data, conversation_id):
        """
        Create All agents config: List[AgentConfig]
        """
        all_configs: List[AgentConfig] = []
        agent_info_map: Dict[str, Dict[str, Any]] = {}

        async def _recursive_create(
            current_ir_data: Dict[str, Any],
            parent_metadata: Optional[AgentMetaData],
            parent_description: Optional[str] = None,
        ) -> AgentConfig:
            """for each agent, return its child AgentConfig"""
            try:
                AgentIrValidator(**current_ir_data)
            except ValidationError as e:
                raise JiuWenBaseException(
                    StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.code,
                    StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.errmsg.format(
                        error_msg=f"Agent_ir validate failed, root_case={format_pydantic_validation_error_message(e)}"
                    ),
                ) from e
            current_metadata = AgentMetaData(
                id=current_ir_data.get("agentId"),
                name=current_ir_data.get("agentName"),
                description=parent_description
                or current_ir_data.get("description", ""),
                intent_name=current_ir_data.get("intent_name", ""),
                intent_description=current_ir_data.get("intent_description", ""),
                ir_path=current_ir_data.get("ir_path"),
                mode=current_ir_data.get("configs", {}).get("mode", "Controller"),
            )
            child_metadata_list: List[AgentMetaData] = []
            # check if current ir_data has child agents
            if current_ir_data.get("configs", {}).get("agents"):
                for child in current_ir_data.get("configs", {}).get("agents"):
                    logger.info(
                        f"loading ir data of {child.get('id')}",
                        simple_log="loading ir data of child",
                    )
                    # 支持从父Agent修改子Agent描述
                    parent_description = child.get("description", "")
                    # 创建子agent的IR数据副本，合并intent字段
                    child_ir_data = await async_ir_load(child.get("ir_path"))
                    # 如果子agent配置中有intent字段，使用它，否则保持原有的intent
                    if "intent" in child:
                        child_intent = child.get("intent")
                        if isinstance(child_intent, dict):
                            if child_intent.get("name"):
                                child_ir_data["intent_name"] = child_intent.get("name")
                            if child_intent.get("description"):
                                child_ir_data["intent_description"] = child_intent.get(
                                    "description"
                                )

                    child_config = await _recursive_create(
                        child_ir_data, current_metadata, parent_description
                    )
                    child_config.metadata.ir_path = child.get("ir_path")
                    child_config.metadata.mode = child.get(
                        "mode", child_config.metadata.mode
                    )
                    child_metadata_list.append(child_config.metadata)

            task_id = str(secrets.token_hex(24))
            logger.info(
                f"conversation_id: {conversation_id} generate task_id: {task_id} for this request",
                simple_log="conversation generate task",
            )
            task_model, model_configs = AgentIrUtils().get_task_model(
                current_ir_data, task_id
            )
            # 创建LLM模型（合并变量）
            if model_configs:
                llm = IRConverter._create_llm_model(
                    model_configs,
                    cust_headers={},
                    project_id="",
                    task_id=task_id,
                    conversation_id=conversation_id,
                    agent_id=current_metadata.id or "",
                )
            else:
                llm = None
            logger.info(
                f"processing agent config of {current_metadata.id}",
                simple_log="processing agent config",
            )
            current_config = await IRConverter.create_agent_config(
                current_ir_data, llm, task_model, task_id
            )
            logger.info(
                f"get agent config of {current_metadata.id} success",
                simple_log="get agent config",
            )
            current_config.metadata = current_metadata
            current_config.parent_agent_metadata = parent_metadata
            current_config.child_agents_metadata = child_metadata_list
            current_config.ir_path = current_ir_data.get("ir_path")
            current_config.is_published = current_ir_data.get("is_published")

            agent_info_map[current_ir_data.get("agentId")] = {
                "task_id": task_id,
                "task_model": task_model,
            }
            all_configs.append(current_config)
            return current_config

        await _recursive_create(root_ir_data, parent_metadata=None)

        return all_configs, agent_info_map

    @staticmethod
    async def create_agent_group_config(root_ir_data, conversation_id):
        """
        Create agent group configuration from root IR data with cache
        """
        # 首先尝试从缓存获取
        cache_key = root_ir_data.get("ir_path", "")
        agent_group_config_result = None
        cache_enabled = str(
            os.environ.get("AGENT_GROUP_CACHE_ENABLE", "false")
        ).lower() == "true" and root_ir_data.get("is_published", False)
        if cache_enabled:
            agent_group_config_result = await timed_cache_op(
                "agent group config retrieval",
                cache_agent_group_queue.aget(cache_key),
                cache_key,
            )
            if agent_group_config_result:
                logger.info(
                    f"Cache hit for agent group config: {root_ir_data.get('agentId')}",
                    simple_log="Cache hit for agent group config",
                )
                return agent_group_config_result
            logger.info(
                f"Cache miss for agent group: {root_ir_data.get('agentId')}, need to create new config",
                simple_log="Cache miss for agent group, need to create new config",
            )

        agents_all, agent_info_map = await IRConverter.create_all_agents_config_list(
            root_ir_data, conversation_id
        )
        max_agent_calls = root_ir_data.get("max_agent_calls", 10)
        if (
            not isinstance(max_agent_calls, int)
            or isinstance(max_agent_calls, bool)
            or max_agent_calls < 1
        ):
            raise JiuWenBaseException(
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.code,
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg=f"Agent_ir validate failed, "
                    f"root_case= max_agent_calls is not a valid integer {max_agent_calls}"
                ),
            )

        agent_group_config_result = (
            AgentGroupConfig(
                group_id=root_ir_data.get("agentId"),
                group_name=root_ir_data.get("agentName"),
                description=root_ir_data.get("description", ""),
                main_agent=agents_all[-1],
                agents=agents_all,
                group_settings=GroupSettings(),
                max_agent_calls=max_agent_calls,
            ),
            agent_info_map,
        )
        if cache_enabled:
            await timed_cache_op(
                "Caching agent group config",
                cache_agent_group_queue.aput(cache_key, agent_group_config_result),
                cache_key,
            )
            logger.info(
                f"Cached agent group config for: {root_ir_data.get('agentId')}",
                simple_log="Caching agent group config",
            )

        return agent_group_config_result

    @staticmethod
    async def create_or_restore_agent(agent_config, conversation_id):
        """
        Create a new agent or restore from saved state.
        """
        cache_enabled = (
            str(os.environ.get(AGENT_CACHE_ENABLE_KEY, "false")).lower() == "true"
            and agent_config.is_published
        )
        agent = None
        if cache_enabled:
            agent = await timed_cache_op(
                "Agent instance retrieval",
                cache_agent_queue.aget(agent_config.ir_path),
                agent_config.ir_path,
            )

        if agent is None:
            agent = Agent(agent_config)
            if cache_enabled:
                await timed_cache_op(
                    "Caching agent instance",
                    cache_agent_queue.aput(agent_config.ir_path, agent),
                    agent_config.ir_path,
                )

        serialized_data = await AsyncStateManager().get_state(conversation_id)
        if not serialized_data:
            return agent
        agent_state = deserialize_object(serialized_data)
        await agent.load_state(agent_state)
        return agent

    @staticmethod
    def identify_ir(ir_data: dict) -> IRType:
        """
        识别IR数据是用于Agent还是Workflow。

        该方法通过检查IR数据中的特定字段（如'workflowId'和'agentId'）来判断IR数据的类型。
        如果IR数据中包含'workflowId'，则识别为Workflow类型；
        如果包含'agentId'，则进一步判断是否为多Agent(MultiAgents)类型；
        如果既不包含'workflowId'也不包含'agentId'，则抛出异常。

        在识别类型后，会调用validate_ir_version方法验证IR版本是否受支持。

        Args:
            ir_data (dict): 从JSON IR文件解析得到的字典数据。

        Returns:
            IRType: 返回IR类型，可能是'Agent'、'Workflow'或'MultiAgents'。

        Raises:
            JiuWenBaseException: 如果IR数据既不包含'workflowId'也不包含'agentId'时抛出异常。
        """
        if "workflowId" in ir_data:
            ir_type = IRType.Workflow
        elif "agentId" in ir_data:
            agents = ir_data.get("configs", {}).get("agents", [])
            if agents:
                # 有任何子 Agent 配置就走 MultiAgents（包括 PlanExecute 模式的子 Agent）
                ir_type = IRType.MultiAgents
            elif "deep_search_execute_mode" in ir_data.get("configs"):
                ir_type = IRType.DeepResearch
            else:
                ir_type = IRType.Agent
        else:
            raise JiuWenBaseException(
                error_code=StatusCode.IR_DATA_VALIDATION_ERROR.code,
                message=StatusCode.IR_DATA_VALIDATION_ERROR.errmsg,
            )
        IRConverter.validate_ir_version(ir_data, ir_type)
        return ir_type

    @staticmethod
    async def ir_to_agent(ir_data: dict, conversation_id="", **kwargs) -> Agent:
        """
        Convert IR data to an Agent instance.
        :param ir_data: Dictionary parsed from JSON IR file.
        :param conversation_id: Conversation ID for state management.
        :return: Agent instance.
        """
        IRConverter._validate_agent_ir(ir_data)

        task_id = get_x_execution_id()
        logger.info(
            f"conversation_id: {conversation_id} generate task_id: {task_id} for this request"
        )
        task_model, model_configs = AgentIrUtils().get_task_model(ir_data, task_id)
        cust_headers = kwargs.get("cust_headers", {})
        if model_configs:
            llm = IRConverter._create_llm_model(
                model_configs,
                cust_headers,
                kwargs.get("project_id", ""),
                task_id=task_id,
                conversation_id=conversation_id,
                agent_id=ir_data.get("agentId", ""),
            )
        else:
            llm = None
        agent_config = await IRConverter.create_agent_config(
            ir_data, llm, task_model, task_id, conversation_id
        )

        # 设置子Agent元数据（用于Controller模式判断是否有PE Agent）
        IRConverter._set_child_agents_metadata(agent_config, ir_data)

        # 创建或恢复agent
        agent = await IRConverter.create_or_restore_agent(agent_config, conversation_id)
        agent = IRConverter._finalize_agent(
            agent,
            agent_config,
            task_model,
            ir_data,
            cust_headers,
            conversation_id=conversation_id,
        )
        return IRConverter._wrap_agent_with_facade(agent)

    @staticmethod
    async def ir_to_agent_group(
        root_ir_data: dict, conversation_id="", **kwargs
    ) -> HierarchicalAgentGroup:
        """
        将IR数据转换为AgentGroup实例。

        该异步方法接收从JSON IR文件解析出的字典数据，创建或恢复Agent Group配置，并更新Agent Group的提示信息。
        它首先调用create_agent_group_config方法生成Agent Group配置和Agent信息映射，然后调用create_or_restore_agent_group方法创建或恢复Agent Group实例。
        最后，使用update_group_prompt方法更新Agent Group的提示信息。

        Args:
            root_ir_data (dict): 从JSON IR文件解析得到的字典数据。
            conversation_id (str): 用于状态管理的对话ID。
            **kwargs: 可变关键字参数，用于传递额外的信息。

        Returns:
            HierarchicalAgentGroup: 转换后的Agent Group实例。

        """
        (
            agent_group_config,
            agent_info_map,
        ) = await IRConverter.create_agent_group_config(root_ir_data, conversation_id)
        agent_group = await IRConverter.create_or_restore_agent_group(
            agent_group_config, conversation_id
        )
        agent_group.update_group_prompt(agent_info_map)
        return agent_group

    @staticmethod
    async def create_or_restore_agent_group(
        agent_group_config: AgentGroupConfig, conversation_id: str
    ) -> HierarchicalAgentGroup:
        """
        create_or_restore_agent_group
        Args:
            agent_group_config:
            conversation_id: String

        Returns:

        """
        agent_group = HierarchicalAgentGroup(agent_group_config)
        serialized_data = await AsyncStateManager().get_state(conversation_id)
        if not serialized_data:
            await agent_group.start()
            return agent_group
        group_state = deserialize_object(serialized_data)
        await agent_group.start(group_state)
        return agent_group

    @staticmethod
    async def ir_to_workflow(ir_data: dict, **kwargs) -> Workflow:
        """
        Convert IR data to an openjiuwen Workflow instance.
        :param ir_data: Dictionary parsed from JSON IR file.
        :param kwargs: Reserved for compatibility.
        :return: openjiuwen Workflow instance.
        """
        return await IRConverter._build_openjiuwen_workflow_from_ir(ir_data, **kwargs)

    @staticmethod
    async def async_ir_to_workflow(ir_data: dict, **kwargs) -> Workflow:
        """
        Convert IR data to an openjiuwen Workflow instance.

        Synchronous equivalent of ir_to_workflow — openjiuwen Workflow construction
        is pure in-memory and involves no IO. Kept as async for backward compatibility
        with callers that use await.

        Args:
            ir_data (dict): Dictionary parsed from JSON IR file.
            **kwargs: Reserved for compatibility.

        Returns:
            Workflow: openjiuwen Workflow instance.
        """
        return await IRConverter._build_openjiuwen_workflow_from_ir(ir_data, **kwargs)

    @staticmethod
    async def _build_openjiuwen_workflow_from_ir(ir_data: dict, **kwargs) -> Workflow:
        """Core IR -> openjiuwen Workflow conversion."""
        import time as _time

        t_total = _time.time()

        if not isinstance(ir_data, dict):
            raise ValueError("ir_data must be a dict")

        register_error_recovery_handler()

        t_card = _time.time()
        card = WorkflowCard(
            id=ir_data.get("workflowId"),
            name=ir_data.get("workflowName") or ir_data.get("workflowId"),
            description=ir_data.get("description") or "",
        )
        workflow = Workflow(card=card)
        logger.info(
            f"[PERF-IR] WorkflowCard creation: {(_time.time() - t_card) * 1000:.1f}ms"
        )

        global_model = (ir_data.get("configs") or {}).get("model")

        components = ir_data.get("components") or []
        if not components:
            raise ValueError("ir_data.components is empty")

        node_by_id = {node.get("id"): node for node in components if node.get("id")}
        connections = _normalize_list(ir_data.get("connections") or [])
        parallel_join_nodes = collect_parallel_join_nodes(connections, node_by_id)

        component_by_id: dict[str, Any] = {}

        # Pre-compute stream source IDs directly from IR data.
        # This avoids depending on _STREAM_SOURCE_IDS (populated later during
        # component creation) and allows schema splitting to happen upfront.
        ir_stream_source_ids: set[str] = set()
        _llm_types = {
            "jiuwen.llm",
            "jiuwen.llm_chain",
            "jiuwen.llmChain",
            "jiuwen.LLMComponent",
        }
        for node in components:
            nid = node.get("id")
            ntype = node.get("type", "")
            cfgs = node.get("configs") or {}
            if ntype in _llm_types and bool(cfgs.get("stream", False)):
                ir_stream_source_ids.add(nid)
            elif ntype in IRConverter._STREAMING_API_TYPES and bool(
                cfgs.get("streaming", False)
            ):
                ir_stream_source_ids.add(nid)

        # Pre-compute stream connection targets from explicit IR connections.
        t_stream_targets = _time.time()
        stream_input_target_ids: set[str] = set()
        for connection in connections:
            source = ((connection.get("source") or {}).get("componentId") or "").strip()
            target = ((connection.get("target") or {}).get("componentId") or "").strip()
            branch_id = ((connection.get("source") or {}).get("branchId") or "").strip()
            if not source or not target or branch_id:
                continue
            target_type = node_by_id.get(target, {}).get("type", "")
            is_stream_capable = (
                target_type in IRConverter._STREAM_INPUT_CAPABLE_TARGET_TYPES
            )
            if is_stream_capable and source in ir_stream_source_ids:
                # Aggregate only accepts stream input when ALL its $ref inputs
                # come from stream sources; otherwise use regular INVOKE path.
                if target_type in IRConverter._AGGREGATE_TYPES:
                    continue
                stream_input_target_ids.add(target)
        logger.info(
            f"[PERF-IR] Stream targets computed: {(_time.time() - t_stream_targets) * 1000:.1f}ms"
        )

        t_prep = _time.time()
        # Pre-collect all loop body component IDs so that root-level connection
        # processing can skip inter-body connections (they belong to LoopGroup, not main workflow).
        _loop_body_ids: set[str] = set()
        for _n in components:
            if _n.get("type") == "jiuwen.loop":
                for _bid in (_n.get("configs") or {}).get("loopBody") or []:
                    if isinstance(_bid, str) and _bid in node_by_id:
                        _loop_body_ids.add(_bid)
        logger.info(
            f"[PERF-IR] Prep (node_by_id, loop_body): {(_time.time() - t_prep) * 1000:.1f}ms"
        )

        pending_end_nodes: list[dict] = []
        pending_branch_nodes: list[dict] = []
        t_components_start = _time.time()
        for idx, node in enumerate(components):
            t_node = _time.time()
            node_id = node.get("id")
            is_stream_target = node_id in stream_input_target_ids
            component = await IRConverter._add_component(
                workflow,
                node,
                global_model,
                stream_inputs_schema=_convert_schema(node.get("inputs") or {})
                if is_stream_target
                else None,
                pending_end_nodes=pending_end_nodes,
                pending_branch_nodes=pending_branch_nodes,
                node_by_id=node_by_id,
                ir_connections=connections,
                stream_source_ids=ir_stream_source_ids,
                parallel_join_nodes=parallel_join_nodes,
            )
            logger.info(
                f"[PERF-IR] Component[{idx}] '{node_id}' ({node.get('type')}): {(_time.time() - t_node) * 1000:.1f}ms"
            )
            if component is not None:
                component_by_id[node.get("id")] = component
        logger.info(
            f"[PERF-IR] All components creation total: {(_time.time() - t_components_start) * 1000:.1f}ms"
        )

        t_connections_start = _time.time()
        end_node_ids = {p["node_id"] for p in pending_end_nodes}
        stream_connection_targets: set[str] = set()
        batch_connection_targets: set[str] = set()
        deferred_connections: list[tuple[str, str, bool]] = []
        default_branch_connections: list[tuple[str, str, str]] = []
        existing_connections: set[tuple[str, str]] = set()

        # Phase 1: wire branch routes before BranchComponent/IntentDetection join the
        # graph so add_workflow_comp → register_branch_targets sees populated routers.
        for connection in connections:
            source = ((connection.get("source") or {}).get("componentId") or "").strip()
            target = ((connection.get("target") or {}).get("componentId") or "").strip()
            branch_id = ((connection.get("source") or {}).get("branchId") or "").strip()
            if not source or not target:
                continue
            if source.endswith("_input") or target.endswith("_output"):
                continue
            if source in _loop_body_ids and target in _loop_body_ids:
                continue
            if not branch_id or "@@" in branch_id:
                continue
            # Branch connections (default/non-default) are batch paths
            if target in end_node_ids:
                batch_connection_targets.add(target)
            if branch_id.endswith("default"):
                default_branch_connections.append((source, target, branch_id))
            else:
                IRConverter._add_branch_connection(
                    component_by_id, node_by_id, source, target, branch_id
                )

        for source, target, branch_id in default_branch_connections:
            IRConverter._add_branch_connection(
                component_by_id, node_by_id, source, target, branch_id
            )

        IRConverter._register_pending_branch_nodes(workflow, pending_branch_nodes)

        # Phase 2: batch/stream edges and non-branch connections
        for connection in connections:
            source = ((connection.get("source") or {}).get("componentId") or "").strip()
            target = ((connection.get("target") or {}).get("componentId") or "").strip()
            branch_id = ((connection.get("source") or {}).get("branchId") or "").strip()
            if not source or not target:
                continue
            if source.endswith("_input") or target.endswith("_output"):
                continue
            # Skip connections between loop body components — they belong to LoopGroup
            if source in _loop_body_ids and target in _loop_body_ids:
                continue
            if branch_id:
                continue
            existing_connections.add((source, target))
            is_stream = IRConverter._is_stream_connection(components, source, target)
            if is_stream:
                stream_connection_targets.add(target)
            if target in end_node_ids:
                if not is_stream:
                    batch_connection_targets.add(target)
                deferred_connections.append((source, target, is_stream))
            elif is_stream:
                workflow.add_stream_connection(source, target)
            else:
                workflow.add_connection(source, target)
        logger.info(
            f"[PERF-IR] Connections processing (main loop): {(_time.time() - t_connections_start) * 1000:.1f}ms"
        )

        error_branch_groups: dict[str, list[tuple[str, str]]] = {}
        for connection in connections:
            _bid = ((connection.get("source") or {}).get("branchId") or "").strip()
            if _bid and "@@" in _bid:
                _src = (
                    (connection.get("source") or {}).get("componentId") or ""
                ).strip()
                _tgt = (
                    (connection.get("target") or {}).get("componentId") or ""
                ).strip()
                if _src and _tgt:
                    error_branch_groups.setdefault(_src, []).append((_tgt, _bid))

        for _src_id, _branches in error_branch_groups.items():
            _branch_comp = BranchComponent()
            _branch_node_id = f"{_src_id}_error_branch"
            for _target, _branch_id in _branches:
                _source_node = node_by_id.get(_src_id) or {}
                _source_type = _source_node.get("type", "")
                _cond = _resolve_branch_condition(
                    _source_node, _source_type, _branch_id
                )
                _branch_comp.add_branch(_cond, _target, branch_id=_branch_id)
            workflow.add_workflow_comp(
                _branch_node_id,
                _branch_comp,
                inputs_schema={"userFields": {"passthrough": "${" + _src_id + "}"}},
            )
            workflow.add_connection(_src_id, _branch_node_id)

        t_end_comp = _time.time()
        for pending in pending_end_nodes:
            node_id = pending["node_id"]
            end = pending["end"]
            inputs_schema = pending["inputs_schema"]
            is_stream_out = pending["is_stream_out"]
            batch_schema, stream_schema = _split_inputs_schema_by_source(
                inputs_schema, IRConverter._STREAM_SOURCE_IDS
            )
            # Determine incoming edge types for this End node
            has_stream = node_id in stream_connection_targets or stream_schema
            has_batch = node_id in batch_connection_targets or batch_schema

            set_end_kwargs: dict = {}
            if has_batch and has_stream:
                # Split schema by source type to avoid duplicate keys
                set_end_kwargs["inputs_schema"] = batch_schema if batch_schema else {}
                set_end_kwargs["stream_inputs_schema"] = (
                    stream_schema if stream_schema else {}
                )
            elif has_batch:
                set_end_kwargs["inputs_schema"] = inputs_schema
            elif has_stream:
                set_end_kwargs["stream_inputs_schema"] = inputs_schema
            else:
                set_end_kwargs["inputs_schema"] = inputs_schema
            if is_stream_out:
                set_end_kwargs["response_mode"] = "streaming"

            workflow.set_end_comp(node_id, end, **set_end_kwargs)
        logger.info(
            f"[PERF-IR] End comp setup: {(_time.time() - t_end_comp) * 1000:.1f}ms"
        )

        t_deferred = _time.time()
        for source, target, is_stream in deferred_connections:
            existing_connections.add((source, target))
            if is_stream:
                workflow.add_stream_connection(source, target)
            else:
                workflow.add_connection(source, target)
        logger.info(
            f"[PERF-IR] Deferred connections: {(_time.time() - t_deferred) * 1000:.1f}ms"
        )

        # Add missing connections based on schema references.
        # Strategy:
        # - Auto-complete stream edges only when target supports stream input.
        # - Auto-complete batch edges for mix-mode targets (Message/End/Aggregate/...)
        #   when schema references a stream source. Do NOT auto-add batch edges to
        #   pure-batch nodes like Code — that bypasses intermediate nodes and can
        #   schedule them before the stream producer finishes.
        all_target_ids: set[str] = set()
        t_missing = _time.time()
        for connection in connections:
            s = ((connection.get("source") or {}).get("componentId") or "").strip()
            t = ((connection.get("target") or {}).get("componentId") or "").strip()
            bid = ((connection.get("source") or {}).get("branchId") or "").strip()
            if s and t and not bid:
                all_target_ids.add(t)

        for target_id in all_target_ids:
            target_node = node_by_id.get(target_id)
            if not target_node:
                continue
            schema = _convert_schema(target_node.get("inputs") or {})
            schema_source_ids = _extract_source_component_ids(schema, component_by_id)
            # Target participates in mix mode if its schema references any stream source
            has_stream_ref = any(
                sid in IRConverter._STREAM_SOURCE_IDS for sid in schema_source_ids
            )
            target_type = target_node.get("type", "")
            for source_id in schema_source_ids:
                if source_id == target_id:
                    continue
                if (source_id, target_id) in existing_connections:
                    continue
                is_stream = source_id in IRConverter._STREAM_SOURCE_IDS
                if (
                    is_stream
                    and target_type in IRConverter._STREAM_INPUT_CAPABLE_TARGET_TYPES
                    and target_type not in IRConverter._AGGREGATE_TYPES
                ):
                    workflow.add_stream_connection(source_id, target_id)
                    existing_connections.add((source_id, target_id))
                elif (
                    has_stream_ref
                    and target_type in IRConverter._STREAM_INPUT_CAPABLE_TARGET_TYPES
                ):
                    workflow.add_connection(source_id, target_id)
                    existing_connections.add((source_id, target_id))
        logger.info(
            f"[PERF-IR] Missing connections: {(_time.time() - t_missing) * 1000:.1f}ms"
        )

        logger.info(
            f"[PERF-IR] === _build_openjiuwen_workflow_from_ir TOTAL: {(_time.time() - t_total) * 1000:.1f}ms ==="
        )
        return workflow

    @staticmethod
    async def _try_load_sub_workflow(configs: dict) -> Optional["Workflow"]:
        reference = configs.get("reference") or {}
        child_path = reference.get("path", "")
        if not child_path:
            return None
        provider = get_storage_provider()
        try:
            ir_json_str = await provider.get_content(child_path)
        except (StorageConfigError, StorageReadError) as e:
            raise IRBuildException(f"从存储读取 IR 文件失败: {child_path}, {e}") from e

        try:
            child_ir = json.loads(ir_json_str)
        except json.JSONDecodeError as e:
            raise IRBuildException(f"IR 文件 JSON 格式错误: {child_path}, {e}") from e
        return await IRConverter._build_openjiuwen_workflow_from_ir(child_ir)

    @staticmethod
    async def _create_component(
        node: dict,
        global_model: dict | None,
        *,
        node_by_id: dict[str, dict] | None = None,
        ir_connections: list[dict] | None = None,
    ) -> tuple[Any, str, dict]:
        """纯组件创建，不依赖 Workflow。

        返回 (component_instance, node_type, configs)。
        """
        import time as _time

        t_start = _time.time()

        node_id = node.get("id")
        node_name = node.get("name")
        node_type = node.get("type")
        configs = dict(node.get("configs") or {})
        if "name" not in configs and node_name:
            configs["name"] = node_name
        inputs_schema = _convert_schema(node.get("inputs") or {})
        outputs_schema = _convert_schema(node.get("outputs") or {})
        # Add output field references with #end_ prefix only for End component,
        # which uses them to know which fields to include in the response.
        if node_type == "jiuwen.end":
            output_user_fields = outputs_schema.get("userFields", {})
            if isinstance(output_user_fields, dict):
                if "userFields" not in inputs_schema:
                    inputs_schema["userFields"] = {}
                if isinstance(inputs_schema["userFields"], list):
                    for user_field, value in output_user_fields.items():
                        inputs_schema["userFields"].append("#end_" + user_field)
                elif isinstance(inputs_schema["userFields"], dict):
                    for user_field, value in output_user_fields.items():
                        inputs_schema["userFields"]["#end_" + user_field] = value
        if node_type == "jiuwen.start":
            result = Start(configs)
            logger.debug(
                f"[PERF-IR] _create_component '{node_id}' (start): {(_time.time() - t_start) * 1000:.1f}ms"
            )
            return result, node_type, configs

        if node_type == "jiuwen.end":
            result = End(_normalize_end_config(configs))
            logger.debug(
                f"[PERF-IR] _create_component '{node_id}' (end): {(_time.time() - t_start) * 1000:.1f}ms"
            )
            return result, node_type, configs

        if node_type == "jiuwen.message":
            result = Message(configs)
            logger.debug(
                f"[PERF-IR] _create_component '{node_id}' (message): {(_time.time() - t_start) * 1000:.1f}ms"
            )
            return result, node_type, configs

        if node_type in {
            "jiuwen.llm",
            "jiuwen.llm_chain",
            "jiuwen.llmChain",
            "jiuwen.LLMComponent",
        }:
            t_llm = _time.time()
            if node_id in IRConverter._STREAM_SOURCE_IDS:
                IRConverter._STREAM_SOURCE_IDS.remove(node_id)
            if global_model and "model" not in configs:
                configs["model"] = global_model
            elif "model" not in configs and "modelName" in configs:
                configs["model"] = {
                    "modelName": configs.pop("modelName"),
                    "modelType": configs.pop("modelType", "chat"),
                    "hyperParameters": configs.pop("hyperParameters", {}),
                    "extension": configs.pop("extension", {}),
                }
            result = LLMChain(configs)
            logger.debug(
                f"[PERF-IR] _create_component '{node_id}' (LLM): {(_time.time() - t_llm) * 1000:.1f}ms"
            )
            return result, node_type, configs

        if node_type == "jiuwen.questioner":
            return Questioner(_build_questioner_config(configs)), node_type, configs

        if node_type == "EI.qa":
            return FlowQA(configs), node_type, configs

        if node_type == "jiuwen.code":
            return FlowCode(configs), node_type, configs

        if node_type == "jiuwen.intentDetection":
            return (
                _RoutedIntentDetection(_normalize_intent_detection_config(configs)),
                node_type,
                configs,
            )

        if node_type == "jiuwen.branch":
            return BranchComponent(), node_type, configs

        if node_type in {"jiuwen.input", "jiuwen.flowInput"}:
            return FlowInput(configs), node_type, configs

        if node_type in {
            "jiuwen.aggregate",
            "jiuwen.flowAggregate",
            "jiuwen.aggregation",
        }:
            return Aggregate(configs), node_type, configs

        if node_type in {"jiuwen.card", "jiuwen.flowCard"}:
            return FlowCard(FlowCardConfig.model_validate(configs)), node_type, configs

        if node_type in {"jiuwen.extractor", "jiuwen.infoExtraction"}:
            return Extractor(), node_type, configs

        if node_type in {"jiuwen.plugin", "jiuwen.api", "jiuwen.flowApi"}:
            return FlowApi(configs), node_type, configs

        if node_type in {"jiuwen.mcp", "jiuwen.flowMcp"}:
            return FlowMcp(configs), node_type, configs

        if node_type in {"jiuwen.agent", "jiuwen.flowAgent"}:
            return (
                FlowAgent(FlowAgentConfig.model_validate(configs)),
                node_type,
                configs,
            )

        if node_type in {"jiuwen.subWorkflow", "jiuwen.workflowComposite"}:
            return SubWorkflow({**configs, "node_id": node_id}), node_type, configs

        if node_type == "jiuwen.loop":
            loop_group = LoopGroup()
            loop_body = configs.get("loopBody") or []

            # Resolve string IDs to full node objects
            if loop_body and isinstance(loop_body[0], str):
                loop_body = [
                    node_by_id[bid] for bid in loop_body if bid in (node_by_id or {})
                ]
            loop_body_by_id = {n.get("id"): n for n in loop_body if n.get("id")}

            # Add loop body components
            loop_component_by_id = {}
            for body_node in loop_body:
                body_comp = await IRConverter._add_component(
                    loop_group,
                    body_node,
                    global_model,
                )
                if body_comp is not None:
                    loop_component_by_id[body_node.get("id")] = body_comp

            # Process loop body connections from configs (if any)
            loop_body_connections = configs.get("connections") or []
            for conn in loop_body_connections:
                src = ((conn.get("source") or {}).get("componentId") or "").strip()
                tgt = ((conn.get("target") or {}).get("componentId") or "").strip()
                bid = ((conn.get("source") or {}).get("branchId") or "").strip()
                if not src or not tgt:
                    continue
                if bid:
                    IRConverter._add_branch_connection(
                        loop_component_by_id,
                        loop_body_by_id,
                        src,
                        tgt,
                        bid,
                    )
                    continue
                loop_group.add_connection(src, tgt)

            # Also extract connections between loop body components from top-level IR connections.
            # The IR stores loop body inter-component connections at the root level, NOT inside
            # the loop node's configs. Without this, body components are disconnected in LoopGroup.
            loop_body_ids = set(loop_body_by_id.keys())
            for conn in ir_connections or []:
                src = ((conn.get("source") or {}).get("componentId") or "").strip()
                tgt = ((conn.get("target") or {}).get("componentId") or "").strip()
                bid = ((conn.get("source") or {}).get("branchId") or "").strip()
                if not src or not tgt:
                    continue
                # Skip virtual _input/_output connections (handled below for start/end nodes)
                if src == f"{node_id}_input" or tgt == f"{node_id}_output":
                    continue
                # Only process connections where both endpoints are loop body components
                if src not in loop_body_ids or tgt not in loop_body_ids:
                    continue
                if bid:
                    IRConverter._add_branch_connection(
                        loop_component_by_id,
                        loop_body_by_id,
                        src,
                        tgt,
                        bid,
                    )
                    continue
                loop_group.add_connection(src, tgt)

            # Auto-determine start/end nodes
            start_nodes = [
                n.get("id") for n in loop_body if n.get("type") == "jiuwen.start"
            ]
            end_nodes = [
                n.get("id") for n in loop_body if n.get("type") == "jiuwen.end"
            ]

            # Determine from _input/_output virtual connections in top-level IR
            for conn in ir_connections or []:
                src = ((conn.get("source") or {}).get("componentId") or "").strip()
                tgt = ((conn.get("target") or {}).get("componentId") or "").strip()
                if src == f"{node_id}_input" and tgt in loop_body_by_id:
                    if tgt not in start_nodes:
                        start_nodes.append(tgt)
                if tgt == f"{node_id}_output" and src in loop_body_by_id:
                    if src not in end_nodes:
                        end_nodes.append(src)

            # Fallback: first body node is start, last is end
            if not start_nodes and loop_body:
                start_nodes = [loop_body[0].get("id")]
            if not end_nodes and loop_body:
                end_nodes = [loop_body[-1].get("id")]
            # Handle break condition: use BranchComponent + LoopBreakComponent (official pattern).
            #
            # Reference: test_workflow_with_loop_component_always_true test case.
            # Like test_workflow_with_loop_component_break, the branch and break components
            # are placed AFTER the original end_nodes (not replacing them). The end_nodes
            # remain unchanged — the break flag is set in the same PregelGraph superstep
            # and checked at the next iteration's condition node.
            #
            # Structure: original_end_node (end) → branch → break_comp
            #                                          └→ pass_through
            #
            # - branch evaluates break_expr via ExpressionCondition (BranchRouter built-in)
            # - True → LoopBreakComponent (sets broken flag, checked next iteration)
            # - False (catch-all via lambda: True) → pass_through (no-op)
            # - Body components run under SubWorkflowSession (parent_id=''), so external
            #   references like ${node_start.systemFields.query} can be resolved.
            break_expr = configs.get("breakCondition", "").strip()
            if break_expr:
                branch_id = f"_break_branch_{node_id}"
                break_comp_id = f"_break_{node_id}"
                pass_through_id = f"_loop_end_{node_id}"

                # Use standard BranchComponent with two branches:
                # 1. break_expr → LoopBreakComponent (break)
                # 2. lambda: True → pass_through (continue, always matches as fallback)
                branch_comp = BranchComponent()
                branch_comp.add_branch(break_expr, [break_comp_id])
                branch_comp.add_branch(lambda: True, [pass_through_id])

                loop_group.add_workflow_comp(branch_id, branch_comp)
                loop_group.add_workflow_comp(break_comp_id, LoopBreakComponent())
                loop_group.add_workflow_comp(
                    pass_through_id, _LoopPassThroughComponent()
                )

                # Connect original end nodes → branch (end_nodes remain unchanged)
                for end_id in end_nodes or []:
                    loop_group.add_connection(end_id, branch_id)

            if start_nodes:
                loop_group.start_nodes(start_nodes)
            if end_nodes:
                loop_group.end_nodes(end_nodes)

            # Build output_schema as dict for OutputCallback to properly aggregate results
            # OutputCallback.end_round calls get_inputs which resolves ref paths via IO state
            raw_output = node.get("outputs") or {}
            output_schema = _convert_schema(raw_output)

            try:
                loop_comp = LoopComponent(
                    loop_group,
                    output_schema=output_schema or None,
                )
            except OpenjiuwenValidationError as exc:
                raise ValueError(
                    f"unsupported workflow component type for openjiuwen workflow: {node_type}"
                ) from exc
            return loop_comp, node_type, configs

        if node_type == "jiuwen.setVariable":
            variable_mapping = {}
            operator_mapping = {}

            # 兼容新格式：settings
            settings = configs.get("settings") or []  # noqa: redefined-outer-name
            if settings:
                for item in settings:
                    left_val = item.get("left", {}).get("value", "")
                    right_val = item.get("right", {}).get("value", "")
                    operator = item.get("right", {}).get("operator", "")
                    if left_val:
                        variable_mapping[left_val] = right_val
                        if operator:
                            operator_mapping[left_val] = operator
            else:
                # 旧格式：variableMapping / variable_mapping
                for k, v in (
                    configs.get("variableMapping")
                    or configs.get("variable_mapping")
                    or {}
                ).items():
                    variable_mapping[_convert_reference_value(k)] = (
                        _convert_reference_value(v)
                    )
            return (
                LoopSetVariable(variable_mapping, operator_mapping),
                node_type,
                configs,
            )

        if node_type == "jiuwen.exception":
            return (
                ExceptionInfo(
                    node_id=node_id,
                    node_name=configs.get("name") or node_id,
                    node_type=node_type,
                ),
                node_type,
                configs,
            )

        if node_type == "jiuwen.streamTransform":
            metadata = WorkflowMetadata(
                node_id=node_id,
                node_type=node_type,
                node_name=configs.get("name") or node_id,
            )
            return FlowStreamTransform(configs, metadata), node_type, configs

        raise ValueError(
            f"unsupported workflow component type for openjiuwen workflow: {node_type}"
        )

    @staticmethod
    async def _add_component(
        workflow: Workflow,
        node: dict,
        global_model: dict | None,
        stream_inputs_schema: dict | None = None,
        *,
        pending_end_nodes: list[dict] | None = None,
        pending_branch_nodes: list[dict] | None = None,
        node_by_id: dict[str, dict] | None = None,
        ir_connections: list[dict] | None = None,
        stream_source_ids: set[str] | None = None,
        parallel_join_nodes: frozenset[str] | None = None,
    ) -> Any:
        """将组件注册到 Workflow。内部调用 _create_component 创建组件实例。"""
        import time as _time

        t_start = _time.time()

        node_id = node.get("id")
        node_type = node.get("type")

        t_schema = _time.time()
        inputs_schema = _convert_schema(node.get("inputs") or {})
        outputs_schema = _convert_schema(node.get("outputs") or {})
        logger.debug(
            f"[PERF-IR] _add_component '{node_id}' schema conversion: {(_time.time() - t_schema) * 1000:.1f}ms"
        )

        exception_config = _parse_exception_config(node)
        if exception_config and node_type not in _NO_EXCEPTION_TYPES:
            _timeout = exception_config.timeout
            _max_retries = exception_config.retry_times
            if _max_retries > 0 and node_type in _NO_RETRY_TYPES:
                logger.warning(f"{node_type}_{node_id} not support retry")
                _max_retries = 0
        else:
            _timeout = -1.0
            _max_retries = 0
            exception_config = None
        _comp_reg: dict[str, Any] = {
            "timeout": _timeout,
            "max_retries": _max_retries,
            "exception_config": exception_config,
        }
        if parallel_join_nodes and node_id in parallel_join_nodes:
            _comp_reg["wait_for_all"] = True
        # Add output field references with #end_ prefix only for End component,
        # which uses them to know which fields to include in the response.
        if node_type == "jiuwen.end":
            output_user_fields = outputs_schema.get("userFields", {})
            if isinstance(output_user_fields, dict):
                if "userFields" not in inputs_schema:
                    inputs_schema["userFields"] = {}
                if isinstance(inputs_schema["userFields"], list):
                    for user_field, value in output_user_fields.items():
                        inputs_schema["userFields"].append("#end_" + user_field)
                elif isinstance(inputs_schema["userFields"], dict):
                    for user_field, value in output_user_fields.items():
                        inputs_schema["userFields"]["#end_" + user_field] = value

        # subWorkflow 加载子工作流
        if node_type in {"jiuwen.subWorkflow", "jiuwen.workflowComposite"}:
            configs = dict(node.get("configs") or {})
            child_workflow = await IRConverter._try_load_sub_workflow(configs)
            component = SubWorkflow(
                {**configs, "node_id": node_id},
                sub_workflow=child_workflow,
            )
            _add_workflow_comp_with_exception(
                workflow,
                node_id,
                component,
                inputs_schema=inputs_schema,
                **_comp_reg,
            )
            logger.info(
                f"[PERF-IR] _add_component '{node_id}' (subWorkflow) total: {(_time.time() - t_start) * 1000:.1f}ms"
            )
            return component

        t_create = _time.time()
        component, resolved_type, configs = await IRConverter._create_component(
            node,
            global_model,
            node_by_id=node_by_id,
            ir_connections=ir_connections,
        )
        logger.debug(
            f"[PERF-IR] _add_component '{node_id}' _create_component: {(_time.time() - t_create) * 1000:.1f}ms"
        )

        if resolved_type == "jiuwen.start":
            workflow.set_start_comp(
                node_id, component, inputs_schema=_build_start_inputs_schema(node)
            )
            return component

        if resolved_type in {"jiuwen.branch", "jiuwen.intentDetection"}:
            if pending_branch_nodes is not None:
                pending_branch_nodes.append(
                    {
                        "node_id": node_id,
                        "component": component,
                        "inputs_schema": inputs_schema,
                        "timeout": _timeout,
                        "max_retries": _max_retries,
                        "exception_config": exception_config,
                    }
                )
                return component

        if resolved_type == "jiuwen.end":
            ref = inputs_schema.get("userFields", {})
            for _v in ref.values():
                if (
                    isinstance(_v, str)
                    and _v.startswith("${")
                    and ".__stream_metadata__" not in _v
                ):
                    _upstream_id = _v.split(".")[0][2:]
                    if _upstream_id:
                        _upstream_type = (
                            (node_by_id or {}).get(_upstream_id, {}).get("type", "")
                        )
                        if _upstream_type in {
                            "jiuwen.plugin",
                            "jiuwen.api",
                            "jiuwen.flowApi",
                        }:
                            inputs_schema["userFields"]["__stream_metadata__"] = (
                                "${" + _upstream_id + ".__stream_metadata__}"
                            )
                            break
            if pending_end_nodes is not None:
                pending_end_nodes.append(
                    {
                        "node_id": node_id,
                        "end": component,
                        "inputs_schema": inputs_schema,
                        "is_stream_out": bool(configs.get("isStreamOut")),
                    }
                )
            elif bool(configs.get("isStreamOut")):
                workflow.set_end_comp(
                    node_id,
                    component,
                    stream_inputs_schema=inputs_schema,
                    response_mode="streaming",
                )
            else:
                workflow.set_end_comp(node_id, component, inputs_schema=inputs_schema)
            return component

        if resolved_type == "jiuwen.message":
            if stream_inputs_schema is not None:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    stream_inputs_schema=stream_inputs_schema,
                    **_comp_reg,
                )
            else:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=inputs_schema,
                    **_comp_reg,
                )
            return component

        if resolved_type in {"jiuwen.card", "jiuwen.flowCard"}:
            if stream_inputs_schema is not None:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    stream_inputs_schema=stream_inputs_schema,
                    **_comp_reg,
                )
            else:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=inputs_schema,
                    **_comp_reg,
                )
            return component

        if resolved_type in {
            "jiuwen.aggregate",
            "jiuwen.flowAggregate",
            "jiuwen.aggregation",
        }:
            agg_inputs_schema = _aggregate_inputs_schema(inputs_schema)
            if stream_inputs_schema is not None:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    stream_inputs_schema=_aggregate_inputs_schema(stream_inputs_schema),
                    **_comp_reg,
                )
            else:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=agg_inputs_schema,
                    **_comp_reg,
                )
            return component

        if resolved_type == "jiuwen.streamTransform":
            if stream_inputs_schema is not None:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    stream_inputs_schema=stream_inputs_schema,
                    **_comp_reg,
                )
            else:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=inputs_schema,
                    **_comp_reg,
                )
            return component

        if resolved_type == "jiuwen.loop":
            loop_type_ir = configs.get("loopType", "")
            loop_type = _LOOP_TYPE_MAP.get(loop_type_ir, loop_type_ir)
            loop_inputs = {"loop_type": loop_type}
            node_inputs = node.get("inputs") or {}
            if "numLoopVar" in node_inputs:
                loop_inputs["loop_number"] = node_inputs["numLoopVar"]
            if "arrLoopVar" in node_inputs:
                loop_inputs["loop_array"] = node_inputs["arrLoopVar"]
            _loop_inputs_snapshot = loop_inputs

            def _loop_inputs_transformer(state):
                return _loop_inputs_snapshot

            _add_workflow_comp_with_exception(
                workflow,
                node_id,
                component,
                inputs_schema=_loop_inputs_transformer,
                **_comp_reg,
            )
            return component

        if resolved_type in {
            "jiuwen.llm",
            "jiuwen.llm_chain",
            "jiuwen.llmChain",
            "jiuwen.LLMComponent",
        }:
            if configs.get("responseFormat", {}).get("type", "text") == "json":
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=inputs_schema,
                    **_comp_reg,
                )
            elif bool(configs.get("stream", False)):
                IRConverter._STREAM_SOURCE_IDS.add(node_id)
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=inputs_schema,
                    comp_ability=[ComponentAbility.STREAM],
                    **_comp_reg,
                )
            else:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=inputs_schema,
                    **_comp_reg,
                )
            return component

        if resolved_type in {"jiuwen.plugin", "jiuwen.api", "jiuwen.flowApi"}:
            if bool(configs.get("streaming", False)):
                IRConverter._STREAM_SOURCE_IDS.add(node_id)
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=inputs_schema,
                    comp_ability=[ComponentAbility.STREAM],
                    **_comp_reg,
                )
            else:
                _add_workflow_comp_with_exception(
                    workflow,
                    node_id,
                    component,
                    inputs_schema=inputs_schema,
                    **_comp_reg,
                )
            return component

        _add_workflow_comp_with_exception(
            workflow,
            node_id,
            component,
            inputs_schema=inputs_schema,
            **_comp_reg,
        )
        return component

    @staticmethod
    def _add_branch_connection(
        component_by_id: dict[str, Any],
        node_by_id: dict[str, dict],
        source: str,
        target: str,
        branch_id: str,
    ) -> None:
        component = component_by_id.get(source)
        if component is None:
            return
        source_node = node_by_id.get(source) or {}
        source_type = source_node.get("type")
        condition = _resolve_branch_condition(source_node, source_type, branch_id)
        if hasattr(component, "add_branch"):
            component.add_branch(condition, target, branch_id=branch_id)

    @staticmethod
    def _register_pending_branch_nodes(
        workflow: Workflow, pending_branch_nodes: list[dict]
    ) -> None:
        """Register branch routers after IR branch connections are wired."""
        for pending in pending_branch_nodes:
            workflow.add_workflow_comp(
                pending["node_id"],
                pending["component"],
                inputs_schema=pending["inputs_schema"],
                timeout=pending["timeout"],
                max_retries=pending["max_retries"],
                exception_config=pending["exception_config"],
            )

    _STREAM_SOURCE_IDS = set()

    _AGGREGATE_TYPES = frozenset(
        {
            "jiuwen.aggregate",
            "jiuwen.flowAggregate",
            "jiuwen.aggregation",
        }
    )

    _STREAMING_API_TYPES = frozenset(
        {
            "jiuwen.plugin",
            "jiuwen.api",
            "jiuwen.flowApi",
            "jiuwen.agent",
            "jiuwen.flowAgent",
        }
    )

    _STREAM_INPUT_CAPABLE_TARGET_TYPES = frozenset(
        {
            "jiuwen.end",
            "jiuwen.flowAgent",
            "jiuwen.message",
            "jiuwen.card",
            "jiuwen.streamTransform",
            "jiuwen.aggregate",
            "jiuwen.flowAggregate",
            "jiuwen.aggregation",
        }
    )

    @staticmethod
    def _all_refs_from_stream_sources(
        schema: dict, stream_source_ids: set[str]
    ) -> bool:
        """Check if every $ref in *schema* points to a node in *stream_source_ids*.

        An empty schema or one with no ``$ref`` patterns returns ``True``
        (no non-stream source to reject).
        """
        if not isinstance(schema, dict):
            return True
        for value in schema.values():
            if isinstance(value, dict):
                if not IRConverter._all_refs_from_stream_sources(
                    value, stream_source_ids
                ):
                    return False
            elif isinstance(value, str) and "${" in value:
                ref = value.strip()
                if ref.startswith("${") and "}" in ref:
                    inner = ref[2 : ref.index("}")]
                    parts = inner.split(".", 1)
                    if parts and parts[0] not in stream_source_ids:
                        return False
        return True

    @staticmethod
    def _is_stream_connection(components: list[dict], source: str, target: str) -> bool:
        source_node = next(
            (item for item in components if item.get("id") == source), {}
        )
        target_node = next(
            (item for item in components if item.get("id") == target), {}
        )
        source_type = source_node.get("type")
        source_id = source_node.get("id")
        target_type = target_node.get("type")
        target_configs = target_node.get("configs") or {}
        if target_type in IRConverter._AGGREGATE_TYPES:
            # Legacy BPMN aggregate resolves ${node.field} refs from IO state after
            # upstream batch outputs are committed (including stream LLM via
            # get_stream_output).  Stream edges trigger COLLECT and skip that path.
            return False
        if target_type not in IRConverter._STREAM_INPUT_CAPABLE_TARGET_TYPES:
            return False
        if (
            target_type == "jiuwen.end"
            and target_configs.get("isStreamOut")
            and source_id in IRConverter._STREAM_SOURCE_IDS
        ):
            return True
        if (
            source_id in IRConverter._STREAM_SOURCE_IDS
            and target_type in IRConverter._STREAM_INPUT_CAPABLE_TARGET_TYPES
        ):
            return True
        return False

    @staticmethod
    def _add_normal_edge_spec_only(
        workflow: Workflow, source: str, target: str
    ) -> None:
        internal = getattr(workflow, "_internal", None)
        spec = getattr(internal, "_workflow_spec", None)
        if spec is None:
            return
        if source not in spec.edges:
            spec.edges[source] = [target]
        elif target not in spec.edges[source]:
            spec.edges[source].append(target)

    @staticmethod
    def create_single_component_ir(ir_data: dict, component_id: str) -> dict:
        """
        创建单个组件的中间表示(IR)数据。

        该方法用于生成单个组件的IR数据，适用于组件调试场景。
        它会验证IR数据的类型，并确保组件ID存在于IR数据中。
        如果组件类型不在支持的列表中，则会抛出异常。

        Args:
            ir_data (dict): 完整的工作流IR数据，从JSON IR文件解析得到的字典。
            component_id (str): 需要调试的组件ID。

        Returns:
            dict: 包含单个组件IR数据的字典。

        Raises:
            JiuWenBaseException: 如果IR类型不是工作流、找不到对应组件或组件类型不支持时抛出异常。
        """
        ir_type = IRConverter.identify_ir(ir_data)
        if ir_type != IRType.Workflow:
            raise JiuWenBaseException(
                error_code=StatusCode.COMPONENT_IR_ILLEGAL.code,
                message=StatusCode.COMPONENT_IR_ILLEGAL.errmsg.format(
                    reason="single component ir_type must be workflow"
                ),
            )
        # 获取待调测组件对应信息
        node_component = [
            node for node in ir_data["components"] if node.get("id") == component_id
        ]
        if not node_component:
            raise JiuWenBaseException(
                error_code=StatusCode.COMPONENT_IR_ILLEGAL.code,
                message=StatusCode.COMPONENT_IR_ILLEGAL.errmsg.format(
                    reason="cannot find the corresponding component,please check node_id is correct"
                ),
            )
        if node_component[0].get("type") in UNSUPPORTED_COMPONENT_DEBUG_LIST:
            raise JiuWenBaseException(
                error_code=StatusCode.COMPONENT_TYPE_ERROR.code,
                message=StatusCode.COMPONENT_TYPE_ERROR.errmsg,
            )
        # 单组件调测场景下 ir_data仅保留所需调测组件信息，删去其它无关组件及组件间连接关系
        ir_data["components"] = node_component
        ir_data["connections"] = []
        return ir_data

    @staticmethod
    async def create_single_component(
        ir_data: dict, component_id: str
    ) -> SingleComponentInfo:
        """从 IR 中创建单个组件实例，用于单组件调测。

        复用 create_single_component_ir 的验证逻辑 + _create_component 的创建逻辑。

        Args:
            ir_data: 完整的工作流 IR 数据。
            component_id: 需要调测的组件 ID。

        Returns:
            SingleComponentInfo: 组件信息，包含 component 实例、node_id、node_type、configs、inputs_schema。

        Raises:
            JiuWenBaseException: IR 类型不是工作流、找不到组件、组件类型不支持时抛出。
        """
        ir_type = IRConverter.identify_ir(ir_data)
        if ir_type != IRType.Workflow:
            raise JiuWenBaseException(
                error_code=StatusCode.COMPONENT_IR_ILLEGAL.code,
                message=StatusCode.COMPONENT_IR_ILLEGAL.errmsg.format(
                    reason="single component ir_type must be workflow"
                ),
            )
        node_component = [
            node
            for node in ir_data.get("components", [])
            if node.get("id") == component_id
        ]
        if not node_component:
            raise JiuWenBaseException(
                error_code=StatusCode.COMPONENT_IR_ILLEGAL.code,
                message=StatusCode.COMPONENT_IR_ILLEGAL.errmsg.format(
                    reason="cannot find the corresponding component, please check node_id is correct"
                ),
            )
        node = node_component[0]
        node_type = node.get("type")
        if node_type in UNSUPPORTED_COMPONENT_DEBUG_LIST:
            raise JiuWenBaseException(
                error_code=StatusCode.COMPONENT_TYPE_ERROR.code,
                message=StatusCode.COMPONENT_TYPE_ERROR.errmsg,
            )
        global_model = (ir_data.get("configs") or {}).get("model")
        component, _, configs = await IRConverter._create_component(node, global_model)

        # SubWorkflow 需要加载子工作流
        if node_type in {"jiuwen.subWorkflow", "jiuwen.workflowComposite"}:
            child_workflow = IRConverter._try_load_sub_workflow(configs)
            component = SubWorkflow(
                {**configs, "node_id": component_id}, sub_workflow=child_workflow
            )

        inputs_schema = _convert_schema(node.get("inputs") or {})
        return SingleComponentInfo(
            component=component,
            node_id=component_id,
            node_type=node_type,
            configs=configs,
            inputs_schema=inputs_schema,
            node_name=node.get("name", ""),
        )

    @staticmethod
    async def create_all_memory_config_list(root_ir_data):
        """
        Create All Memory configs: List[MemoryIrConfig]
        """
        all_configs: List[MemoryIrConfig] = []
        visited = set()

        async def _recursive_create(current_ir_data: dict[str, Any]):
            """for each agent|workflow, add its MemoryIrConfig into list"""
            if not current_ir_data or not isinstance(current_ir_data, dict):
                return
            node_id = current_ir_data.get("agentId", "") or current_ir_data.get(
                "workflowId", ""
            )
            if not node_id or node_id in visited:
                return
            visited.add(node_id)

            current_memory_ir_data = current_ir_data.get("configs", {}).get(
                "memory", {}
            )
            if current_memory_ir_data:
                all_configs.append(
                    MemoryIrConfig.from_config_dict(current_memory_ir_data)
                )

            current_ir_type = IRConverter.identify_ir(current_ir_data)
            if current_ir_type == IRType.Agent:
                for child_workflow_info in current_ir_data.get("configs", {}).get(
                    "workflows", []
                ):
                    child_workflow_ir_path = child_workflow_info.get("ir_path", "")
                    if child_workflow_ir_path:
                        child_workflow_ir_data = await async_ir_load(
                            child_workflow_ir_path
                        )
                        await _recursive_create(child_workflow_ir_data)
            elif current_ir_type == IRType.Workflow:
                for child_workflow_info in current_ir_data.get("components", {}):
                    if child_workflow_info.get("type") != NodeType.SUB_WORKFLOW.value:
                        continue
                    child_workflow_ir_path = (
                        child_workflow_info.get("configs", {})
                        .get("reference", {})
                        .get("path", "")
                    )
                    if child_workflow_ir_path:
                        child_workflow_ir_data = await async_ir_load(
                            child_workflow_ir_path
                        )
                        await _recursive_create(child_workflow_ir_data)
            elif current_ir_type == IRType.MultiAgents:
                for child_agent_info in current_ir_data.get("configs", {}).get(
                    "agents", []
                ):
                    child_agent_ir_path = child_agent_info.get("ir_path", "")
                    if child_agent_ir_path:
                        child_agent_ir_data = await async_ir_load(child_agent_ir_path)
                        await _recursive_create(child_agent_ir_data)
                for child_workflow_info in current_ir_data.get("configs", {}).get(
                    "workflows", []
                ):
                    child_workflow_ir_path = child_workflow_info.get("ir_path", "")
                    if child_workflow_ir_path:
                        child_workflow_ir_data = await async_ir_load(
                            child_workflow_ir_path
                        )
                        await _recursive_create(child_workflow_ir_data)

        await _recursive_create(root_ir_data)
        return all_configs

    @staticmethod
    async def get_memory_topics(root_ir_data) -> List[Dict[str, Any]]:
        """
        Recursively get and merge memory topics from the root IR data
        """
        if not root_ir_data or not isinstance(root_ir_data, dict):
            return []
        all_memory_configs = await IRConverter.create_all_memory_config_list(
            root_ir_data
        )

        # merge memory topics
        topic_lists: List[List[Dict[str, Any]]] = []
        for memory_config in all_memory_configs:
            if (
                memory_config.enable_memory
                and memory_config.enable_user_profile
                and memory_config.user_profile_topics
            ):
                topic_lists.append(memory_config.user_profile_topics)

        merged = defaultdict(dict)
        for topic_list in topic_lists:
            for item in topic_list:
                if not isinstance(item, dict):
                    continue
                topic_name = item.get("name")
                tags = item.get("tags")

                if topic_name is None or not isinstance(tags, list):
                    continue

                merged_tags = merged[topic_name]
                for tag in tags:
                    if not isinstance(tag, dict):
                        continue
                    tag_name = tag.get("name")
                    if tag_name is None:
                        continue
                    merged_tags.setdefault(tag_name, tag)

        return [
            {"name": topic_name, "tags": list(tag_map.values())}
            for topic_name, tag_map in merged.items()
        ]

    @staticmethod
    def _validate_agent_ir(ir_data: dict):
        """验证 Agent IR 数据"""
        try:
            AgentIrValidator(**ir_data)
        except ValidationError as e:
            error_message = format_pydantic_validation_error_message(e)
            raise JiuWenBaseException(
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.code,
                StatusCode.LLM_AGENT_IR_VALIDATE_ERROR.errmsg.format(
                    error_msg=f"Agent_ir validate failed, root_case={error_message}"
                ),
            ) from e

    @staticmethod
    def _create_llm_model(
        model_configs: dict,
        cust_headers: dict,
        project_id: str,
        task_id: str = "",
        agent_id: str = "",
        conversation_id: str = "",
    ):
        """创建 LLM 模型"""
        if IRConverter._use_agent_core_model(model_configs):
            return IRConverter._create_agent_core_llm_model(
                model_configs,
                task_id,
                agent_id,
                conversation_id=conversation_id,
            )

        runtime_context = {
            "runtime_context": {
                "workflow_execute_requests_headers_name": {
                    "cust_headers": cust_headers
                },
                "workflow_execute_project_id": {"project_id": project_id},
            }
        }
        return ModelFactory().get_model(
            model_type=model_configs.get("modelType"),
            model_name=model_configs.get("modelName"),
            **(model_configs.get("hyperParameters", {})),
            **(model_configs.get("extension", {})),
            **runtime_context,
        )

    @staticmethod
    def _use_agent_core_model(model_configs: dict) -> bool:
        """Return whether the IR model dependency should be backed by openjiuwen."""
        extension = model_configs.get("extension", {}) or {}
        enabled = extension.get(
            "useAgentCoreModel", extension.get("use_agent_core_model")
        )
        if enabled is not None:
            return str(enabled).lower() == "true"
        if str(os.environ.get(_USE_AGENT_CORE_MODEL_ENV, "true")).lower() != "true":
            return False
        provider = IRConverter._normalize_agent_core_client_provider(
            IRConverter._first_non_empty(
                extension.get("agentCoreClientProvider"),
                extension.get("agent_core_client_provider"),
                extension.get("client_provider"),
                model_configs.get("modelType"),
            )
        )
        return provider in {
            "OpenAI",
            "DashScope",
            "OpenRouter",
            "SiliconFlow",
            "ContractMock",
            "LLM",
        }

    @staticmethod
    def _create_agent_core_llm_model(
        model_configs: dict, task_id: str, agent_id: str, conversation_id: str = ""
    ):
        """Create jiuwen model contract layer backed by openjiuwen's ModelWrapper."""
        # Keep openjiuwen-related imports lazy so the legacy ModelFactory path remains unchanged.
        from jiuwen.extension.wrapper.model_wrapper import ModelWrapper
        from jiuwen.integration.agent_core_model_new import AgentCoreModelLayer

        extension = model_configs.get("extension", {}) or {}
        model_id = (
            extension.get("agentCoreModelId")
            or extension.get("agent_core_model_id")
            or extension.get("model_id")
            or model_configs.get("modelId")
            or model_configs.get("modelName")
            or ""
        )
        session_id = (
            extension.get("agentCoreSessionId")
            or extension.get("agent_core_session_id")
            or extension.get("session_id")
            or conversation_id
            or task_id
            or ""
        )
        resolved_agent_id = (
            extension.get("agentCoreAgentId")
            or extension.get("agent_core_agent_id")
            or extension.get("agent_id")
            or agent_id
            or ""
        )
        IRConverter._register_agent_core_llm_model(model_id, model_configs)
        return AgentCoreModelLayer(
            runtime=ModelWrapper(),
            default_model_id=model_id,
            default_session_id=session_id,
            default_agent_id=resolved_agent_id,
        )

    @staticmethod
    def _register_agent_core_llm_model(model_id: str, model_configs: dict) -> None:
        """Register an openjiuwen model resource from jiuwen IR model config.

        Uses IRModelConfigProvider for consistent model configuration across
        all components, including per-request auth headers.
        """
        import time as _time

        if not model_id:
            return
        if model_id in _AGENT_CORE_REGISTERED_MODEL_IDS:
            logger.info(
                f"Agent-core model resource already registered: model_id={model_id}",
                simple_log="agent-core model resource already registered",
            )
            return

        t_reg_start = _time.time()
        from agent_runtime.common.model_adapters import adapt_ir_converter_model_config
        from agent_runtime.common.model_providers import IRModelConfigProvider
        from openjiuwen.core.foundation.llm import Model
        from openjiuwen.core.runner import Runner

        t_adapt = _time.time()
        adapted_conf = adapt_ir_converter_model_config(model_configs, model_id)
        logger.info(
            f"[PERF-IR] Model config adaptation: {(_time.time() - t_adapt) * 1000:.1f}ms"
        )

        t_provider = _time.time()
        provider = IRModelConfigProvider()
        llm_comp_config = provider.get_llm_config(adapted_conf)
        logger.info(
            f"[PERF-IR] IRModelConfigProvider.get_llm_config: {(_time.time() - t_provider) * 1000:.1f}ms"
        )

        # Extract model name for logging
        model_name = model_configs.get("modelName") or model_id

        t_model_create = _time.time()
        try:
            model_instance = Model(
                model_client_config=llm_comp_config.model_client_config,
                model_config=llm_comp_config.model_config,
            )
            logger.info(
                f"[PERF-IR] Model instance creation: {(_time.time() - t_model_create) * 1000:.1f}ms"
            )
        except Exception as e:
            logger.error(
                f"Failed to create agent-core model resource: model_id={model_id}, "
                f"model_name={model_name}, error={e}",
                simple_log=(
                    f"failed to create agent-core model resource: "
                    f"model_id={model_id}, model_name={model_name}, error={e}"
                ),
            )
            raise

        def model_factory():
            return model_instance

        t_register = _time.time()
        add_result = Runner.resource_mgr.add_model(
            model_id=model_id, model=model_factory
        )
        logger.info(
            f"[PERF-IR] Runner.resource_mgr.add_model: {(_time.time() - t_register) * 1000:.1f}ms"
        )

        # Check registration result
        is_ok = getattr(add_result, "is_ok", None)
        succeeded = bool(is_ok()) if callable(is_ok) else True

        if not succeeded:
            if "resource already exist" in str(add_result):
                _AGENT_CORE_REGISTERED_MODEL_IDS.add(model_id)
                logger.info(
                    f"Agent-core model resource already exists in runner: model_id={model_id}",
                    simple_log="agent-core model resource already exists",
                )
                return
            raise RuntimeError(
                f"Failed to register agent-core model resource: {add_result}"
            )

        _AGENT_CORE_REGISTERED_MODEL_IDS.add(model_id)
        logger.info(
            f"[PERF-IR] === Model registration TOTAL: {(_time.time() - t_reg_start) * 1000:.1f}ms ==="
        )
        logger.info(
            f"Registered agent-core model resource: model_id={model_id}, model={model_name}",
            simple_log="registered agent-core model resource",
        )

    @staticmethod
    def _resolve_legacy_model_config(model_configs: dict) -> dict:
        """Resolve jiuwen model config so the openjiuwen model can reuse existing settings."""
        model_factory = ModelFactory()
        model_type = model_configs.get("modelType")
        model_name = model_configs.get("modelName")
        extension = model_configs.get("extension", {}) or {}
        hyper_parameters = model_configs.get("hyperParameters", {}) or {}
        resolver_kwargs = {**hyper_parameters, **extension}
        resolved_config = None
        if model_factory.model_resolver:
            resolved_config = model_factory.model_resolver(
                model_type, model_name, **resolver_kwargs
            )
        if not resolved_config:
            resolved_config = model_factory.default_model_resolver(
                model_type, model_name, **resolver_kwargs
            )
        return dict(resolved_config or {})

    @staticmethod
    def _filter_model_config_kwargs(config_cls, values: dict) -> dict:
        """Keep only fields supported by the installed openjiuwen config class."""
        fields = getattr(config_cls, "model_fields", None) or getattr(
            config_cls, "__fields__", None
        )
        clean_values = {
            key: value for key, value in values.items() if value is not None
        }
        if not fields:
            return clean_values
        return {key: value for key, value in clean_values.items() if key in fields}

    @staticmethod
    def _normalize_agent_core_client_provider(provider: str) -> str:
        """Map jiuwen model types to openjiuwen client provider names."""
        provider_aliases = {
            "dashscope": "DashScope",
            "openai": "OpenAI",
            "openrouter": "OpenRouter",
            "qwen": "OpenAI",
            "deepseek": "OpenAI",
            "siliconflow": "SiliconFlow",
        }
        if not provider:
            return provider
        return provider_aliases.get(str(provider).lower(), provider)

    @staticmethod
    def _use_legacy_direct_model(client_provider: str, api_base: str) -> bool:
        """Return whether a legacy full chat-completions endpoint should be called directly."""
        if str(client_provider).lower() != "openai" or not api_base:
            return False
        normalized_path = str(api_base).split("?", 1)[0].replace("\\", "/").lower()
        return "/chat/completions/" in normalized_path

    @staticmethod
    def _resolve_agent_core_ssl_values(
        api_base: str, extension: dict, resolved_config: dict
    ) -> dict:
        """Translate legacy jiuwen SSL config to openjiuwen's strict SSL fields."""
        explicit_verify = IRConverter._first_non_empty(
            extension.get("verify_ssl"),
            extension.get("verifySSL"),
            extension.get("ssl_verify"),
            extension.get("sslVerify"),
            extension.get("ssl_mode"),
            extension.get("sslMode"),
            resolved_config.get("verify_ssl"),
            resolved_config.get("verifySSL"),
            resolved_config.get("ssl_verify"),
            resolved_config.get("sslVerify"),
            resolved_config.get("ssl_mode"),
            resolved_config.get("sslMode"),
        )
        ssl_cert = IRConverter._first_non_empty(
            extension.get("ssl_cert"),
            extension.get("sslCert"),
            extension.get("ssl_certificate"),
            extension.get("sslCertificate"),
            extension.get("cert_path"),
            extension.get("certPath"),
            resolved_config.get("ssl_cert"),
            resolved_config.get("sslCert"),
            resolved_config.get("ssl_certificate"),
            resolved_config.get("sslCertificate"),
            resolved_config.get("cert_path"),
            resolved_config.get("certPath"),
        )

        if explicit_verify is not None:
            verify_ssl = IRConverter._to_bool(explicit_verify)
        else:
            legacy_verify = ModelUtil.parse_ssl_verify()
            if isinstance(legacy_verify, str):
                verify_ssl = True
                ssl_cert = ssl_cert or legacy_verify
            else:
                verify_ssl = bool(legacy_verify)

        if (
            str(api_base or "").lower().startswith("http://")
            and explicit_verify is None
        ):
            verify_ssl = False
        if verify_ssl and not ssl_cert:
            verify_ssl = False
        return {"verify_ssl": verify_ssl, "ssl_cert": ssl_cert}

    @staticmethod
    def _to_bool(value) -> bool:
        """Convert legacy bool-like config values to bool."""
        if isinstance(value, bool):
            return value
        if isinstance(value, str):
            return value.strip().lower() in {"1", "true", "yes", "y", "on"}
        return bool(value)

    @staticmethod
    def _agent_core_resource_add_succeeded(add_result) -> bool:
        """Return whether openjiuwen resource registration returned Ok."""
        is_ok = getattr(add_result, "is_ok", None)
        return bool(is_ok()) if callable(is_ok) else True

    @staticmethod
    def _agent_core_resource_already_exists(add_result) -> bool:
        """Return whether openjiuwen resource registration failed because it already exists."""
        return "resource already exist" in str(add_result)

    @staticmethod
    def _first_non_empty(*values):
        """Return the first value that is not None and not an empty string."""
        for value in values:
            if value is not None and value != "":
                return value
        return None

    @staticmethod
    def _set_child_agents_metadata(agent_config, ir_data: dict):
        """设置子 Agent 元数据"""
        child_agents = ir_data.get("configs", {}).get("agents", [])
        if not child_agents:
            return
        child_metadata_list = []
        for child in child_agents:
            child_metadata = AgentMetaData(
                id=child.get("id"),
                name=child.get("name"),
                description=child.get("description", ""),
                ir_path=child.get("ir_path"),
                mode=child.get("mode", "Controller"),
            )
            child_metadata_list.append(child_metadata)
            logger.info(
                f"Added child agent metadata: id={child.get('id')}, mode={child.get('mode')}",
                simple_log="added child agent metadata",
            )
        agent_config.child_agents_metadata = child_metadata_list

    @staticmethod
    def _finalize_agent(
        agent,
        agent_config,
        task_model,
        ir_data: dict,
        cust_headers: dict,
        conversation_id: str = "",
    ):
        """完成 Agent 初始化"""
        if ir_data.get("configs").get("mode") == "ToolUse":
            AgentIrUtils.register_system_prompt(
                agent_config.task_id + "-toolUseAgent", "tool_use_agent"
            )
            AgentIrUtils.register_system_prompt(
                agent_config.task_id,
                "tool_use_planner",
                ir_data.get("configs").get("sysPromptTemplate"),
            )
            AgentIrUtils.update_plugins_from_ir(
                agent, ir_data, conversation_id=conversation_id
            )
            AgentIrUtils.update_mcps_from_ir(agent, ir_data, cust_headers=cust_headers)
            return agent
        return AgentIrUtils.update_prompt_plugin(
            agent_config.task_id,
            agent,
            task_model,
            ir_data,
            cust_headers=cust_headers,
            conversation_id=conversation_id,
        )

    @staticmethod
    def _wrap_agent_with_facade(agent):
        """Wrap the commercial agent with OpenJiuwenAgentFacade when enabled."""
        if os.getenv(_BASE_AGENT_SWITCH, "false").strip().lower() not in {
            "1",
            "true",
            "yes",
            "on",
        }:
            return agent

        from jiuwen.integration.openjiuwen_agent_facade import OpenJiuwenAgentFacade

        if isinstance(agent, OpenJiuwenAgentFacade):
            return agent
        return OpenJiuwenAgentFacade(agent)


def _normalize_end_config(configs: dict) -> dict | None:
    if not configs:
        return None
    if not configs.get("responseTemplate"):
        return None
    return configs


def _normalize_intent_detection_config(configs: dict) -> dict:
    normalized = dict(configs or {})
    llm_config = dict(normalized.get("llm") or {})
    model_config = dict(llm_config.get("model") or {})
    extension = dict(model_config.get("extension") or {})
    extension.setdefault("api_key", os.getenv("LLM_API_KEY", "mock-api-key"))
    extension.setdefault("api_base", os.getenv("LLM_API_BASE", "http://localhost/v1"))
    extension.setdefault(
        "verify_ssl", os.getenv("LLM_VERIFY_SSL", "false").lower() == "true"
    )
    model_config["extension"] = extension
    model_config["modelType"] = _normalize_model_provider(model_config.get("modelType"))
    llm_config["model"] = model_config
    normalized["llm"] = llm_config
    return normalized


def _normalize_model_provider(provider: Any) -> str:
    provider_text = str(provider or "").strip()
    if provider_text.startswith("llm_"):
        return provider_text
    return os.getenv("LLM_PROVIDER", "llm_OpenAI")


def _build_questioner_config(configs: dict) -> QuestionerConfig:
    model_config = configs.get("model") or {}
    return QuestionerConfig(
        model_name=model_config.get("modelName") or configs.get("modelName") or "",
        model_type=_normalize_model_provider(
            model_config.get("modelType") or configs.get("modelType")
        ),
        response_type=configs.get("responseType", "reply_directly"),
        extract_fields_from_response=bool(
            configs.get("extractFieldsFromResponse", True)
        ),
        with_chat_history=bool(configs.get("withChatHistory", False)),
        chat_history_max_rounds=int(
            configs.get("chatHistoryMaxRounds")
            or configs.get("chatHistoryMaxTurn")
            or 0
        ),
        extra_prompt_for_fields_extraction=configs.get("extraPromptForFieldsExtraction")
        or "",
        question_content=configs.get("questionContent") or "",
        question_construction_method=configs.get("questionConstructionMethod")
        or "rule_based",
        cn_fields_name=configs.get("cnFieldsName") or {},
        max_response=int(configs.get("maxResponse") or 3),
        option_content=configs.get("optionContent") or [],
        key_fields=_build_questioner_fields(configs),
        input_complement=bool(configs.get("inputComplement", False)),
        type_complement=configs.get("typeComplement") or "",
        hyper_parameters=model_config.get("hyperParameters")
        or configs.get("hyperParameters")
        or {},
        extension=model_config.get("extension") or configs.get("extension") or {},
        prompt_template=None,
        example_content=configs.get("exampleContent") or "",
        rails_config=configs.get("railsConfig") or {},
        accept_language=configs.get("acceptLanguage") or "zh",
        # 新增字段（补齐缺失的参数配置）
        allow_node_confirm=bool(configs.get("allowNodeConfirm", False)),
        need_user_confirm=bool(configs.get("needUserConfirm", True)),
        allow_node_break=bool(configs.get("allowNodeBreak", False)),
        enum_visible=bool(configs.get("enumVisible", False)),
        user_break=bool(configs.get("userBreak", False)),
        auto_ask_template=configs.get("autoAskTemplate") or "",
        mode=configs.get("mode") or "effect",
        assist_recognize=bool(configs.get("assistRecognize", False)),
        assist_example=configs.get("assistExample") or "",
        template_anthropomorphic=bool(configs.get("templateAnthropomorphic", False)),
    )


def _build_questioner_fields(configs: dict) -> list[FieldInfo]:
    raw_fields = configs.get("fieldNames") or configs.get("keyFields") or []
    if not raw_fields:
        raw_fields = (configs.get("userFields") or {}).get("inputs") or []
    fields = []
    for item in raw_fields:
        if not isinstance(item, dict):
            continue
        field_name = (
            item.get("fieldName")
            or item.get("field_name")
            or item.get("id")
            or item.get("name")
        )
        if not field_name:
            continue
        fields.append(
            FieldInfo(
                field_name=str(field_name),
                description=str(
                    item.get("description") or item.get("desc") or field_name
                ),
                type=str(item.get("type") or "string"),
                cn_field_name=str(
                    item.get("cnFieldName") or item.get("cn_field_name") or ""
                ),
                required=bool(item.get("required", False)),
                default_value=item.get("defaultValue")
                or item.get("default_value")
                or "",
                reflection=bool(item.get("reflection", False)),
            )
        )
    return fields


def _build_start_inputs_schema(node: dict) -> dict:
    inputs_schema = {
        "query": "${query}",
        "sys": "${sys}",
        "global_variables": "${global_variables}",
    }
    raw_inputs = _convert_schema(node.get("inputs") or {})
    for reference_path in _iter_reference_paths(raw_inputs):
        root = _resolve_start_input_name(reference_path)
        if not root or root in {"query", "sys", "global_variables"}:
            continue
        inputs_schema[root] = "${" + root + "}"
    configs = node.get("configs") or {}
    for field in (configs.get("userFields") or {}).get("inputs") or []:
        if not isinstance(field, dict):
            continue
        field_id = field.get("id")
        if not field_id:
            continue
        inputs_schema[str(field_id)] = "${" + str(field_id) + "}"
    return inputs_schema


def _add_workflow_comp_with_exception(
    workflow: Workflow | LoopGroup,
    comp_id: str,
    component,
    *,
    timeout: float,
    max_retries: int,
    exception_config: ExceptionConfig | None,
    inputs_schema=None,
    stream_inputs_schema=None,
    comp_ability=None,
    wait_for_all: bool | None = None,
) -> None:
    """Register a component on Workflow or LoopGroup.

    LoopGroup.add_workflow_comp does not accept exception-process kwargs; loop body
    components are registered via LoopGroup inside _create_component.
    """
    kwargs = {}
    if inputs_schema is not None:
        kwargs["inputs_schema"] = inputs_schema
    if stream_inputs_schema is not None:
        kwargs["stream_inputs_schema"] = stream_inputs_schema
    if comp_ability is not None:
        kwargs["comp_ability"] = comp_ability
    if wait_for_all:
        kwargs["wait_for_all"] = True
    if isinstance(workflow, LoopGroup):
        workflow.add_workflow_comp(comp_id, component, **kwargs)
    else:
        workflow.add_workflow_comp(
            comp_id,
            component,
            timeout=timeout,
            max_retries=max_retries,
            exception_config=exception_config,
            **kwargs,
        )


_NO_EXCEPTION_TYPES = frozenset({"jiuwen.loop", "jiuwen.exception"})
_NO_RETRY_TYPES = frozenset({"jiuwen.subWorkflow", "jiuwen.workflowComposite"})


def _parse_exception_config(node: dict) -> ExceptionConfig | None:
    """从 IR 节点的 configs.exceptionProcess 中解析异常处理配置。"""
    ep = (node.get("configs") or {}).get("exceptionProcess")
    if not ep:
        return None

    from jiuwen.orchestration.flow.constant import EXCEPTION_HANDLE_INTERRUPT

    node_type = node.get("type", "")
    outputs_schema = _convert_schema(node.get("outputs") or {})

    return ExceptionConfig(
        handle_type=ep.get("handleType", EXCEPTION_HANDLE_INTERRUPT).lower(),
        timeout=ep.get("timeout", 7200.0),
        retry_times=ep.get("retryTimes", 0),
        default_outputs=ep.get("defaultOutputs", {}),
        outputs_schema=outputs_schema,
        _node_type=node_type,
        is_config=True,
    )


def _resolve_branch_condition(
    source_node: dict, source_type: str, branch_id: str
) -> str:
    if "@@" in branch_id:
        parts = branch_id.split("@@")
        if len(parts) == 2:
            source_id = parts[0]
            idx = parts[1].split("_")[1] if "_" in parts[1] else "0"
            if idx == "1":
                return f"${{{source_id}.result}} == '1'"
            return f"${{{source_id}.result}} != '1'"

    if source_type == "jiuwen.branch":
        branch_config = _find_branch_config(source_node, branch_id)
        if branch_config is None:
            return "True"
        return _normalize_branch_expression(
            branch_config.get("boolExpression") or "True"
        )
    if source_type == "jiuwen.intentDetection":
        branch_config = _find_branch_config(source_node, branch_id)
        branch_class = _intent_branch_class(branch_config, branch_id)
        return f'${{{source_node.get("id")}.result}} == "{branch_class}"'
    return "True"


def _find_branch_config(source_node: dict, branch_id: str) -> dict | None:
    configs = source_node.get("configs") or {}
    branches = configs.get("branches") or []
    suffix = branch_id.replace(f"{source_node.get('id')}_", "", 1)
    for branch in branches:
        if not isinstance(branch, dict):
            continue
        current_id = str(branch.get("id") or "")
        if current_id == branch_id or current_id == suffix:
            return branch
    return None


def _intent_branch_class(branch_config: dict | None, branch_id: str) -> str:
    raw_id = str((branch_config or {}).get("id") or branch_id)
    match = re.search(r"branch_(\d+)", raw_id)
    if not match:
        return "\u5206\u7c7b0"
    return f"\u5206\u7c7b{match.group(1)}"


def _aggregate_inputs_schema(inputs_schema: dict) -> dict:
    """Aggregate reads group keys from userFields; flatten IR nesting for registration."""
    user_fields = inputs_schema.get("userFields")
    if isinstance(user_fields, dict):
        return dict(user_fields)
    return inputs_schema


def _normalize_list(value: Any) -> list:
    if not value:
        return []
    if isinstance(value, list):
        return value
    return [value]


def _extract_ref_source_id(value: Any) -> str | None:
    """Extract source component ID from a variable reference like ${node_llm.userFields.raw_output}."""
    if (
        not isinstance(value, str)
        or not value.startswith("${")
        or not value.endswith("}")
    ):
        return None
    inner = value[2:-1]
    if not inner or "." not in inner:
        return None
    return inner.split(".")[0]


def _split_inputs_schema_by_source(
    schema: dict, stream_source_ids: set[str]
) -> tuple[dict | None, dict | None]:
    """Split input schema into batch and stream parts based on referenced source component type.

    Leaf fields referencing stream source components go to stream_schema.
    Others go to batch_schema.  This avoids duplicate keys between
    inputs_schema and stream_inputs_schema validation.

    Returns (batch_schema_or_None, stream_schema_or_None).
    """
    batch_schema: dict = {}
    stream_schema: dict = {}
    for category, fields in schema.items():
        if not isinstance(fields, dict):
            batch_schema[category] = fields
            continue
        batch_fields: dict = {}
        stream_fields: dict = {}
        for field_name, value in fields.items():
            source_id = _extract_ref_source_id(value)
            if source_id is not None and source_id in stream_source_ids:
                stream_fields[field_name] = value
            else:
                batch_fields[field_name] = value
        if batch_fields:
            batch_schema[category] = batch_fields
        if stream_fields:
            stream_schema[category] = stream_fields
    return (batch_schema or None), (stream_schema or None)


def _convert_schema(value: Any) -> Any:
    if isinstance(value, dict):
        converted = {key: _convert_schema(item) for key, item in value.items()}
        if str(converted.get("sourceType") or "").lower() == "reference" and isinstance(
            converted.get("value"), str
        ):
            converted["value"] = _convert_reference_value(converted["value"])
        return converted
    if isinstance(value, list):
        converted_items = [_convert_schema(item) for item in value]
        compacted = _compact_named_input_items(converted_items)
        if compacted is not None:
            return compacted
        return converted_items
    if isinstance(value, str):
        return _convert_string_schema(value)
    return value


def _convert_string_schema(value: str) -> str:
    # If value already looks like a ref path (starts with ${), preserve it
    # _convert_reference_value would strip the ${} wrapper and break is_ref_path checks
    if value.startswith("${") and value.endswith("}"):
        return value
    match = _REFERENCE_PATTERN.match(value)
    if match:
        return _convert_reference_value(match.group(1))
    if "{" not in value or "}" not in value:
        return value
    return _REFERENCE_TOKEN_PATTERN.sub(
        lambda item: _convert_reference_value(item.group(1)),
        value,
    )


def _convert_reference_value(reference: str) -> str:
    normalized_path = _normalize_reference_path(reference)
    if normalized_path.startswith("${") and normalized_path.endswith("}"):
        return normalized_path
    return "${" + normalized_path + "}"


def _normalize_reference_path(reference: str) -> str:
    parts = [segment.strip() for segment in str(reference).split(".")]
    normalized_parts = [
        _REFERENCE_ALIASES.get(segment, segment) for segment in parts if segment
    ]
    return ".".join(normalized_parts)


def _iter_reference_paths(value: Any) -> Iterable[str]:
    if isinstance(value, dict):
        for item in value.values():
            yield from _iter_reference_paths(item)
        return
    if isinstance(value, list):
        for item in value:
            yield from _iter_reference_paths(item)
        return
    if not isinstance(value, str):
        return
    open_match = _OPEN_REFERENCE_PATTERN.match(value)
    if open_match:
        yield _normalize_reference_path(open_match.group(1))
        return
    exact_match = _REFERENCE_PATTERN.match(value)
    if exact_match:
        yield _normalize_reference_path(exact_match.group(1))
        return
    for match in _OPEN_REFERENCE_TOKEN_PATTERN.finditer(value):
        yield _normalize_reference_path(match.group(1))
    for match in _REFERENCE_TOKEN_PATTERN.finditer(value):
        yield _normalize_reference_path(match.group(1))


def _resolve_start_input_name(reference_path: str) -> str | None:
    normalized = _normalize_reference_path(reference_path)
    if normalized in {"query", "sys", "memory", "conversationHistory"}:
        return normalized
    if normalized.startswith("start.userFields."):
        return normalized.split(".", 2)[-1] or None
    if normalized.startswith("start.systemFields."):
        field_name = normalized.split(".", 2)[-1]
        if field_name == "query":
            return "query"
        if field_name == "sys":
            return "sys"
        if field_name == "conversationHistory":
            return "conversationHistory"
        return field_name or None
    return normalized.split(".", 1)[0] or None


def _normalize_branch_expression(expression: str) -> str:
    converted = _convert_string_schema(expression)
    converted = converted.replace("&&", " and ").replace("||", " or ")
    converted = _NEGATION_PATTERN.sub(" not ", converted)
    return " ".join(converted.split())


def _compact_named_input_items(items: list[Any]) -> dict[str, Any] | None:
    if not items or not all(isinstance(item, dict) for item in items):
        return None
    compacted: dict[str, Any] = {}
    for item in items:
        field_name = item.get("name") or item.get("id") or item.get("fieldName")
        if not field_name:
            return None
        if "value" in item:
            compacted[str(field_name)] = item["value"]
            continue
        if "default" in item:
            compacted[str(field_name)] = item["default"]
            continue
        return None
    return compacted
