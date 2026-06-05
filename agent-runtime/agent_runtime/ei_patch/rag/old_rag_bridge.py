#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import os
import time
from asyncio import Queue
from typing import Iterable, Optional, Generator

from fastapi.responses import StreamingResponse
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.log.base import logger, get_x_execution_id
from jiuwen.context.base import ContextConfig
from jiuwen.context.history import ConversationHistory
from jiuwen.controller.common.config import OperatorConfig, ConstrainConfig
from jiuwen.controller.common.constants import WorkflowConstants
from jiuwen.insight.client import Client
from jiuwen.insight.handlers.remote import InsightHandler
from jiuwen.insight.manager import TraceManager
from jiuwen.orchestration.common.perf_log import wf_performance_buffer
from jiuwen.orchestration.flow.components.util import (
    format_pydantic_validation_error_message,
)
from jiuwen.orchestration.flow.enum import NodeType
from jiuwen.orchestration.task_model import TaskModel, TaskConfig
from jiuwen.planner.common.enum_class import ProviderType
from jiuwen.planner.common.plan_runtime_context import PlanRuntimeContext
from jiuwen.serve.controllers.execution.enum import (
    ResponseMode,
    IRType,
    ConversationEvent,
)
from jiuwen.serve.controllers.execution.ir_converter import IRConverter
from jiuwen.serve.controllers.execution.manager import AsyncStateManager
from jiuwen.serve.controllers.execution.open_utils import async_ir_load
from jiuwen.serve.controllers.execution.open_utils import deserialize_object
from jiuwen.serve.controllers.execution.types import (
    AgentIrValidator,
    StreamingChatResponse,
)
from jiuwen.serve.controllers.execution.types import ExecutionData
from jiuwen.serve.controllers.execution.utils import (
    AgentIrUtils,
    _execute_workflow,
    _execute_agent,
    _CONSTRAIN,
    _MAX_ITERATION,
    PLAN_TYPE,
    TaskOutputFormatter,
    _ANSWER,
    _TIME_CONSUMPTION,
    _OVERALL_LATENCY,
    _execute_agent_group,
)
from jiuwen.serve.schemas.orchestration_mgr import ExecutionRequest
from overwrite_jiuwen.jiuwen.serve.controllers.execution.utils import (
    get_current_time_ms,
    _UTF_8,
)
from pydantic import ValidationError
from rag.EI_ragchain import start_RAG_line
from rag.EI_tooluse import start_Tool_line
from rag.agent.agent import Agent
from rag.controller.workspace import WorkSpace
from rag.models.tool import Tool
from rag.planner.planner_config import PlanConfig, SOPPlanConfig

DEFAULT_MODE = "ReAct"


async def get_workflow_instance_from_state(ir_data: dict, agent_state):
    """get workflow instance"""
    workflow_ir_paths = ir_data.get("configs", {}).get("workflows", [])
    workflows = []
    unfinished_workflows = {
        state.workflow_state.bpmn_workflow_state.name: state
        for state in agent_state.workflow_states
    }
    for workflow_info in workflow_ir_paths:
        workflow_ir = await async_ir_load(workflow_info.get("ir_path"))
        component_list = workflow_ir.get("components", [])
        # 排包含有task组件的workflow 防止无穷嵌套
        for component in component_list:
            type_value = component.get("type")
            if not type_value or type_value == NodeType.TASK.value:
                raise JiuWenBaseException(
                    error_code=StatusCode.IR_DATA_VALIDATION_ERROR.code,
                    message=f"workflow ir contains task components or None, value is {type_value}",
                )
        workflow_params = WorkSpace.get_workflow_params(workflow_ir)
        workflow_name = workflow_ir.get("workflowName", "")
        if workflow_name in unfinished_workflows.keys():
            workflow_instance = await WorkSpace.create_workflow_instance(
                unfinished_workflows[workflow_name]
            )
        else:
            workflow_instance = await WorkSpace.create_workflow_instance(workflow_ir)
        workflow_tool = Tool(
            tool_id=workflow_ir.get("workflowName"),
            provider_type=ProviderType.WORKFLOW,
            invokable=workflow_instance,
            parameters=workflow_params,
        )
        workflows.append(workflow_tool)
    return workflows


def from_config_dict(cls, input_dict: dict):
    """Constructs a new PlanConfig instance by loading configuration from a specified dict."""
    configs = {}
    for config_type, config_value in input_dict.items():
        if config_type == "constrain":
            configs["constrain_config"] = ConstrainConfig(**config_value)
            continue
        if config_type == "sop_plan":
            configs["sop_plan_config"] = SOPPlanConfig(**config_value)
            continue
        if config_type == "workflow_switch_enable":
            configs["workflow_switch_enable"] = config_value
            continue
        if config_type == "choose_mode":
            configs["choose_mode"] = config_value
            continue
        if config_type == "input_variables":
            configs["input_variables"] = config_value
            continue
        configs[f"{config_type}_config"] = OperatorConfig(**config_value)
    return cls(**configs)


def get_task_model(ir_utils: AgentIrUtils, ir_data, task_id):
    """get task model input"""
    ir_configs = ir_data.get("configs", {})
    plan_config_dict = ir_utils.create_plan_config(task_id)
    # 携带上下文轮数
    model_configs = ir_configs.get("modelConfig")
    if model_configs.get("historySize"):
        if (
            "propose_next" in plan_config_dict
            and isinstance(plan_config_dict.get("propose_next"), dict)
            and "reserved_max_chat_rounds" in plan_config_dict.get("propose_next")
        ):
            plan_config_dict["propose_next"]["reserved_max_chat_rounds"] = (
                model_configs.get("historySize")
            )
    max_iteration = ir_configs.get("maxIteration", _MAX_ITERATION)
    if _CONSTRAIN in plan_config_dict and isinstance(
        plan_config_dict.get(_CONSTRAIN), dict
    ):
        plan_config_dict[_CONSTRAIN].update(max_iteration=max_iteration)
    plan_config_dict["workflow_switch_enable"] = ir_configs.get(
        "workflowSwitchEnabled", False
    )
    plan_config_dict["choose_mode"] = ir_configs.get("mode", "ReAct")
    plan_config_dict["input_variables"] = ir_utils.parse_input_variables_from_ir(
        ir_data.get("inputs", [])
    )
    memory_config = ir_configs.get(
        "memoryConfig", {}
    )  # 适配控制器和工作流接入记忆功能 需要根据实际IR进行修改

    context = ContextConfig.from_config_dict(ir_configs.get("context", {}))
    task_config = TaskConfig(
        sys_prompt_template=ir_configs.get("sysPromptTemplate"),
        mode=PLAN_TYPE.get(ir_configs.get("mode")),
        max_iteration=max_iteration,
        plan_config=plan_config_dict,
        context=context,
        memory_config=memory_config,
    )

    task_model = TaskModel(
        agent_id=ir_data.get("agentName"),
        agent_version=ir_data.get("agentVersion"),
        description=ir_data.get("description"),
        agent_name=ir_data.get("agentName"),
        configs=task_config,
    )
    model_configs = ir_configs.get("modelConfig")
    return task_model, model_configs


async def ir_to_agent(ir_data: dict, conversation_id="", **kwargs):
    if os.environ.get("RAG_ENABLED") == "true" and (
        start_RAG_line(ir_data) or start_Tool_line(ir_data)
    ):
        logger.info("build old rag agent")
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
        task_id = get_x_execution_id()
        task_model, model_configs = get_task_model(AgentIrUtils(), ir_data, task_id)
        model_hyper_params = model_configs.get("hyperParameters", {})
        extensions = model_configs.get("extension", {})
        llm = ModelFactory().get_model(
            model_type=model_configs.get("modelType"),
            model_name=model_configs.get("modelName"),
            runtime_context={
                "workflow_execute_requests_headers_name": {
                    "cust_headers": kwargs.get("cust_headers")
                },
                "workflow_execute_project_id": {"project_id": kwargs.get("project_id")},
            },
            **model_hyper_params,
            **extensions,
        )

        plan_model_config = ir_data.get("configs", {}).get("planModelConfig")
        plan_model = None
        if plan_model_config:
            plan_model = ModelFactory().get_model(
                model_type=plan_model_config.get("modelType"),
                model_name=plan_model_config.get("modelName"),
                runtime_context={
                    "workflow_execute_requests_headers_name": {
                        "cust_headers": kwargs.get("cust_headers")
                    },
                    "workflow_execute_project_id": {
                        "project_id": kwargs.get("project_id")
                    },
                },
                **plan_model_config.get("hyperParameters", {}),
                **plan_model_config.get("extension", {}),
            )

        plan_config = from_config_dict(PlanConfig, task_model.configs.plan_config)
        setattr(
            plan_config, "choose_mode", ir_data.get("configs", {}).get("mode", "RAG")
        )
        # 新增workflow
        serialized_data = await AsyncStateManager().get_state(conversation_id)
        if not serialized_data:
            workflows = await WorkSpace.get_workflow_instance(ir_data)
            agent = Agent(
                model=llm,
                plan_model=plan_model,
                plan_config=plan_config,
                task_id=task_id,
                workflows=workflows,
                agent_context=task_model.configs.context,
                memory_config=task_model.configs.memory_config,
            )
        else:
            agent_state = deserialize_object(serialized_data)
            workflows = await get_workflow_instance_from_state(ir_data, agent_state)
            agent = Agent.from_state(
                agent_state,
                llm,
                plan_config,
                task_id,
                workflows,
                plan_model=plan_model,
                task_model=task_model,
            )
        if not hasattr(agent.context_manager, "states_info"):
            setattr(agent.context_manager, "states_info", [])
        # 更新 task 对应的插件和提示词模板
        agent = AgentIrUtils.update_prompt_plugin(
            task_id, agent, task_model, ir_data, cust_headers=kwargs.get("cust_headers")
        )
        return agent
    else:
        logger.info("build new agent")

        if ir_data["configs"]["mode"] in ["RAG", "ToolUse"]:
            ir_data["configs"]["mode"] = DEFAULT_MODE

        return await IRConverter.ir_to_agent(
            ir_data,
            conversation_id,
            project_id=kwargs.get("project_id"),
            cust_headers=kwargs.get("cust_headers"),
        )


async def distribute_execution_request_ei(
    req: ExecutionRequest, execution_data: ExecutionData
):
    """
    The general entry for IR execution. Distributing requests to each handler for processing.
    """
    if execution_data.instance_type == IRType.Agent:
        try:
            return await _execute_ei_agent(req, execution_data)
        except Exception:
            await AsyncStateManager().delete_state(req.conversation_id)
            raise
    if execution_data.instance_type == IRType.MultiAgents:
        try:
            return await _execute_agent_group(req, execution_data)
        except Exception:
            await AsyncStateManager().delete_state(req.conversation_id)
            raise
    if execution_data.instance_type == IRType.Workflow:
        try:
            return await _execute_workflow(req, execution_data)
        except Exception:
            wf_performance_buffer.record(req.conversation_id)
            await AsyncStateManager().delete_state(req.conversation_id)
            raise
    if execution_data.instance_type == IRType.DeepResearch:
        try:
            from jiuwen.deep_research.utils import execute_deepresearch

            return await execute_deepresearch(req, execution_data)
        except Exception:
            await AsyncStateManager().delete_state(req.conversation_id)
            raise
    raise JiuWenBaseException(
        error_code=StatusCode.IR_DATA_VALIDATION_ERROR.code,
        message=StatusCode.IR_DATA_VALIDATION_ERROR.errmsg,
    )


async def _format_rag_agent_streaming_output(
    origin_output: Iterable,
    insight_queue: Queue,
    task_id: str,
    conversation_id: str,
    execution_data: Optional[ExecutionData],
) -> Generator:
    """
    Used for agent streaming output formatting.
    Call the formatting tool class of Task - TaskOutputFormatter.
    Convert the item in the origin_output Iterable to StreamingChatResponse, and dump it to a json string.
    """
    start_time = time.time()
    async for item in origin_output:
        # For each item yielded by the agent (or more accurately, Task),
        # call the corresponding method of TaskOutputFormatter for structured processing.
        if isinstance(item, dict):
            event, data = TaskOutputFormatter.handle_dict_item(start_time, item)
        elif isinstance(item, str):
            event, data = TaskOutputFormatter.handle_str_item(item)
        elif isinstance(item, JiuWenBaseException):
            error_response = {
                "event": "error",
                "data": {"message": item.message, "code": item.error_code},
                "createdTime": get_current_time_ms(),
            }
            error_response_str = StreamingChatResponse(
                **error_response
            ).model_dump_json(by_alias=True, exclude_none=True)
            yield f"data: {error_response_str}\n\n".encode(_UTF_8)
            raise item
        else:
            event, data = TaskOutputFormatter.handle_unknown_item()

        # summary_response event will be formatted into [two] StreamingChatResponse structure:
        # STATISTIC_DATA and SUMMARY_RESPONSE
        if event == ConversationEvent.SUMMARY_RESPONSE:
            time_consumption = data.get(_ANSWER).pop(_TIME_CONSUMPTION)
            time_consumption[_OVERALL_LATENCY] = round(time.time() - start_time, 2)
            static_data_structure = StreamingChatResponse(
                event=ConversationEvent.STATISTIC_DATA,
                data=dict(answer=time_consumption),
                createdTime=get_current_time_ms(),
            )
            yield f"data: {static_data_structure.model_dump_json(by_alias=True, exclude_none=True)}\n\n".encode(
                _UTF_8
            )

        structure = StreamingChatResponse(
            event=event,
            data=data,
            createdTime=get_current_time_ms(),
            executionId=task_id,
        )
        yield f"data: {structure.model_dump_json(by_alias=True, exclude_none=True)}\n\n".encode(
            _UTF_8
        )

        while not insight_queue.empty():
            data_pop = insight_queue.get()
            structure = StreamingChatResponse(
                event=ConversationEvent.AGENT_NODE_MSG,
                data=data_pop.get("data"),
                createdTime=get_current_time_ms(),
                executionId=task_id,
            )
            yield f"data: {structure.model_dump_json(by_alias=True, exclude_none=True)}\n\n".encode(
                _UTF_8
            )
    execution_data.instance.save_state(conversation_id)


async def _execute_ei_agent(req: ExecutionRequest, execution_data: ExecutionData):
    """
    Agent execution request handler.
    """
    agent = execution_data.instance
    if os.environ.get("RAG_ENABLED") == "true" and isinstance(agent, Agent):
        logger.info("execute old rag agent")
        # 插件所需headers透传
        runtime_context = PlanRuntimeContext()
        runtime_context.api_config = {"headers": req.headers}
        workflow_chat_history = ConversationHistory(
            [
                chat.model_dump(exclude_none=True)
                for chat in req.params.conversation_history
            ]
        )
        runtime_context.agent_workflow_context["workflow_chat_history"] = (
            workflow_chat_history
        )
        runtime_context.agent_workflow_context[
            WorkflowConstants.WORKFLOW_REQ_PARAMS_KEY
        ] = req.params.model_dump()
        runtime_context.conversation_id = req.conversation_id
        insight_client = Client(insight_project_id="agent_debug_info")
        if req.headers.get("x-invoke-mode", "").lower() == "debug":
            tracer = TraceManager(
                trace_handlers=[InsightHandler(client=insight_client)]
            )
        else:
            logger.debug(
                "_execute_agent, X-Invoke-Mode is not debug, no insight info return"
            )
            tracer = TraceManager(trace_handlers=[])
        # 流式调用
        if req.response_mode == ResponseMode.STREAMING:
            streaming_output = agent.stream(
                req.query,
                stream=True,
                runtime_context=runtime_context,
                tool_switch_dict=req.params.tool_switch_dict,
                trace_handlers=tracer,
            )
            resp = _format_rag_agent_streaming_output(
                origin_output=streaming_output,
                insight_queue=insight_client.data_queue,
                task_id=agent.task_id,
                conversation_id=req.conversation_id,
                execution_data=execution_data,
            )
            return StreamingResponse(resp, media_type="text/event-stream")

        # 非流式调用
        raise JiuWenBaseException(
            error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
            message="responseMode Blocking is not supported yet.",
        )
    else:
        logger.info("execute new agent")
        return await _execute_agent(req, execution_data)
