#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from dataclasses import dataclass, field
from enum import Enum
from typing import List, Union, Iterator, Dict, Generator, Any

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.context.base import VARIABLE_NAME_KEY, VARIABLE_DEFAULT_KEY
from jiuwen.context.history import ConversationHistory
from jiuwen.context.memory_engine.config.memory_ir_config import MemoryIrConfig
from jiuwen.controller.common.constants import WorkflowConstants
from jiuwen.controller.common.enum import RetCode
from jiuwen.insight.manager import TraceManager, get_child_manager
from jiuwen.insight.utils import get_instance_info
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.enum import ExecutionStatus
from jiuwen.orchestration.utils import Output
from jiuwen.planner.common.enum_class import ProviderType
from jiuwen.plugin.models.function import Function
from jiuwen.plugin.models.plugin import BasePlugin
from jiuwen.serve.controllers.execution.manager import StateManager
from jiuwen.serve.controllers.execution.open_utils import serialize_object
from pydantic import BaseModel
from rag.controller.context_manager import ContextManager
from rag.controller.controller import Controller
from rag.controller.workspace import WorkSpace
from rag.models.tool import Tool
from rag.planner.planner_config import PlanConfig



class TaskInvokeStatus(Enum):
    """
    Enumeration class for task invoke status.

    Attributes:
       INIT (str): The initial status.
       SUCCESS (str): The success status.
       WAIT_USER_INPUT (str): The status indicating waiting for user input.
       FAILED (str): The failed status.
       UNKNOWN (str): The unknown status.
    """

    INIT = "init"
    SUCCESS = "success"
    WAIT_USER_INPUT = "wait_user_input"
    FAILED = "failed"
    UNKNOWN = "unknown"


@dataclass
class AgentState:
    workflow_states: list = field(
        default_factory=list
    )  # 未执行完的workflow的序列化实例, 序列化方法架设提供
    workflow_contexts: list = field(
        default_factory=list
    )  # 未执行完的workflow工具调用命令栈
    workflow_functions: list = field(
        default_factory=list
    )  # 上次调用时planning_state中的functions
    controller_global_variables: Dict[str, Any] = field(
        default_factory=dict
    )  # 控制器全局变量


class Agent(Invokable):
    """
    new agent class
    """

    query_key_word = "query"
    result_key_word = "result"
    plan_prompt_key_word = "prompt_info"
    ret_code_key_word = "ret_code"
    _max_task_id_length = 64

    def __init__(
        self,
        model: BaseModel = None,
        plan_model: BaseModel = None,
        task_id: str = None,
        plan_config: PlanConfig = None,
        plugins: Union[
            List[Union[Function, BasePlugin]],
            Function,
            BasePlugin,
            Dict[str, List[str]],
            str,
        ] = None,
        workflow_irs: List[Dict] = None,
        workflows: List = None,
        agent_context=None,
        memory_config: MemoryIrConfig = None,
    ):

        self.context_manager = ContextManager(
            agent_context=agent_context, memory_config=memory_config
        )
        self.workspace = WorkSpace(workflows=workflows, plugins=plugins)
        self.controller = Controller(
            workspace=self.workspace,
            context_manager=self.context_manager,
            model=model,
            plan_model=plan_model,
            plan_config=plan_config,
        )

        self.result = None
        self.task_id = self._verify_task_id(task_id=task_id)
        self._invoke_status = TaskInvokeStatus.INIT
        self.init_agent_input_variables(plan_config)

    def init_agent_input_variables(self, plan_config):
        """
        初始化 Agent 输入变量到 ContextManager
        - 从 plan_config.input_variables 获取变量定义
        - 将默认值存入 ContextManager

        注意：此方法仅在初始化时调用，设置默认值。
        后续的变量更新和记忆加载由 Agent 类直接处理。
        """

        input_variables = getattr(plan_config, "input_variables", None)
        if not input_variables or not isinstance(input_variables, list):
            logger.info(f"task_id: {self.task_id}| No agent input variables defined")
            return

        agent_inputs = {}
        for variable_item in input_variables:
            name = variable_item.get("name")
            if name:
                agent_inputs[name] = self._build_default_from_tree(variable_item)

        self.context_manager.set_global_variables("agent_inputs", agent_inputs)
        logger.info(
            f"task_id: {self.task_id}| Initialized agent_inputs: {agent_inputs}"
        )

    def _build_default_from_tree(self, var):
        """
        从树形结构的变量定义中递归构建其默认值。
        - 如果是 object 且有 fields，返回字典；
        - 否则返回 default 字段。
        """
        if var.get("type", "").lower() == "object" and "fields" in var:
            obj_val = {}
            for fd in var["fields"]:
                fname = fd.get("name")
                if fname:
                    obj_val[fname] = self._build_default_from_tree(fd)
            return obj_val
        return var.get("default")

    @classmethod
    def from_state(
        cls,
        state: AgentState,
        model,
        plan_config,
        task_id,
        workflows,
        plan_model=None,
        task_model=None,
    ):
        """
        获取Agent状态， 如果获取到，根据保存的状态和 AgentConfig 初始化 Agent 实例，否则直接根据 AgentConfig 初始化 Agent 实例
        """
        agent = cls(
            model=model,
            plan_config=plan_config,
            task_id=task_id,
            workflows=workflows,
            plan_model=plan_model,
            agent_context=task_model.configs.context,
            memory_config=task_model.configs.memory_config,
        )
        agent.context_manager.workflow_contexts = state.workflow_contexts
        if state.workflow_contexts:
            agent.context_manager.msg_context.last_func = state.workflow_contexts.pop()
        if state.workflow_functions:
            agent.context_manager.msg_context.functions = state.workflow_functions.pop()
        if state.controller_global_variables:
            agent.context_manager.global_variables = state.controller_global_variables

        return agent

    def save_state(self, key):
        """
        保存 AgentState，Workflow的序列化方法由Workflow提供
        """
        if self.workspace.workflows:
            workflow_states = [
                workflow.invokable.get_state()
                for workflow in self.workspace.workflows
                if workflow.invokable.get_workflow_execute_status()
                == ExecutionStatus.USER_INTERACT
            ]
        else:
            workflow_states = []
        agent_state = AgentState(
            workflow_states=workflow_states,
            workflow_contexts=self.context_manager.workflow_contexts,
            workflow_functions=self.context_manager.workflow_functions,
            controller_global_variables=self.context_manager.global_variables,
        )
        serialized_agent_state = serialize_object(agent_state)
        StateManager().save_state(key, serialized_agent_state)

    def load_state(self, state: AgentState, ir_data=None):
        """
        根据 AgentState 覆盖 ContextMemory 中的消息队列和WorkSpace中的Workflow实例
        """
        from jiuwen.serve.controllers.execution.utils import AgentIrUtils

        if ir_data:
            workflows = AgentIrUtils.get_workflow_instance_from_state(ir_data, state)
            self.workspace.workflows = workflows
        self.context_manager.workflow_contexts = state.workflow_contexts
        if state.workflow_contexts:
            self.context_manager.msg_context.last_func = state.workflow_contexts.pop()
        if state.workflow_functions:
            self.context_manager.msg_context.functions = state.workflow_functions.pop()
        if state.controller_global_variables:
            self.context_manager.global_variables = state.controller_global_variables

    async def invoke(self, inputs, **kwargs) -> Output:
        """
        invoke agent
        """
        chat_history = (
            kwargs.get("runtime_context").agent_workflow_context.get(
                "workflow_chat_history", ConversationHistory()
            )
        ).get_all_messages()
        self.context_manager.msg_context.init_history(chat_history)
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None),
            get_instance_info(
                self,
                blacklist=[
                    "results",
                    "memory_inputs",
                    "memory",
                    "flows",
                    "workflow_dict",
                    "workflows",
                ],
            ),
        )
        trace_manager.on_chain_start(inputs)
        # call planning assistance engine
        if isinstance(inputs, dict):
            input_content = inputs.get(self.query_key_word)
            prompt_info = inputs.get(self.plan_prompt_key_word)
        elif isinstance(inputs, str):
            input_content = inputs
            prompt_info = None
        else:
            raise JiuWenBaseException(
                error_code=StatusCode.AGENT_INPUT_TYPE_ERROR.code,
                message=StatusCode.AGENT_INPUT_TYPE_ERROR.errmsg.format(type(inputs)),
            )
        # get recommend plugins
        tools = await self.workspace.gen_tools(input_content)
        plan_status_code, execute_res = self.controller(
            query=input_content,
            prompt_info=prompt_info,
            functions=tools,
            trace_handlers=get_child_manager(trace_manager) if trace_manager else None,
            contexts=kwargs.get("contexts"),
            runtime_context=kwargs.get("runtime_context"),
        )
        if plan_status_code == RetCode.FAILED:
            self._invoke_status = TaskInvokeStatus.FAILED
            err = ValueError("Planning assistance engine failed to plan or execute")
            trace_manager.on_chain_error(err)
            raise err
        if plan_status_code == RetCode.SUCCESS:
            self._invoke_status = TaskInvokeStatus.SUCCESS
        elif plan_status_code == RetCode.WAIT_USER_INPUT:
            self._invoke_status = TaskInvokeStatus.WAIT_USER_INPUT
        else:
            self._invoke_status = TaskInvokeStatus.UNKNOWN
            err = ValueError(f"Unsupported plan_states_code: {plan_status_code}.")
            trace_manager.on_chain_error(err)
            raise err
        self.result = (
            execute_res.get(self.result_key_word)
            if execute_res and isinstance(execute_res, dict)
            else ""
        )
        res = self.result
        trace_manager.on_chain_end(res)
        return res

    def _partial_update_dict(self, target: dict, updates: dict) -> bool:
        """
        递归地对 target 字典进行部分更新（仅更新已存在的键）
        :param target: 原始字典（会被原地修改）
        :param updates: 要更新的字段（可能嵌套）
        :return: 是否发生了更新
        """
        updated = False
        for key, value in updates.items():
            if key not in target:
                continue  # 不新增字段，跳过不存在的键

            if isinstance(value, dict) and isinstance(target[key], dict):
                # 递归进入嵌套字典
                if self._partial_update_dict(target[key], value):
                    updated = True
            else:
                # 叶子节点：直接替换值（允许类型变化）
                target[key] = value
                updated = True

        return updated

    def _update_agent_input_variables(self, global_variables: dict):
        """
        从请求的 globalVariables 部分更新 Agent 输入变量（支持嵌套）
        仅更新 agent_inputs 中已存在的键（包括嵌套键），不新增字段
        """
        if not global_variables:
            return

        agent_inputs = self.context_manager.get_global_variables("agent_inputs", {})

        if agent_inputs is None:
            return

        # 执行嵌套部分更新（仅对已存在的路径）
        updated = self._partial_update_dict(agent_inputs, global_variables)

        if updated:
            self.context_manager.set_global_variables("agent_inputs", agent_inputs)
            logger.info(f"task_id: {self.task_id}| Updated agent_inputs from request")

    async def _load_memory_variables(self, memory_app_id, memory_enable_user_profile):
        """
        加载记忆变量到 ContextManager
        复用 ContextEngine.search_memory_all_variables 方法
        """
        self.context_manager.set_memory_request_params(
            memory_app_id, memory_enable_user_profile
        )

        memory_vars = await self.context_manager.get_memory_variables_dict() or {}
        # 遍历配置中定义的变量，匹配并拼接结果
        for var in self.context_manager.engine["mem_variables"]:
            name = var.get(VARIABLE_NAME_KEY)
            if not name:
                continue

            # 从一次性获取的字典中取值，若无则用默认值
            var_value = memory_vars.get(name, "")
            default_value = var.get(VARIABLE_DEFAULT_KEY, "")
            if not var_value and default_value:
                memory_vars[name] = default_value

        self.context_manager.set_global_variables("memory_variables", memory_vars)

        logger.info(
            f"task_id: {self.task_id}| Loaded memory: {list(memory_vars.keys())}"
        )

    async def prepare_execution(self, inputs, kwargs):
        """
        准备执行环境，处理输入并设置上下文

        Args:
            inputs: 用户输入，可以是字符串或字典
            kwargs: 额外参数

        Returns:
            Tuple: (查询文本, 提示信息, 运行时上下文, 追踪管理器)
        """
        runtime_context = kwargs.get("runtime_context")

        # 传递记忆使用请求级参数
        params = runtime_context.agent_workflow_context.get(
            WorkflowConstants.WORKFLOW_REQ_PARAMS_KEY, {}
        )
        global_variables = params.get("global_variables", {})
        self._update_agent_input_variables(global_variables)

        memory_app_id = params.get("app_id", "")
        memory_enable_user_profile = params.get("enable_memory_retrieve", False)
        await self._load_memory_variables(memory_app_id, memory_enable_user_profile)

    async def stream(self, inputs: Union[dict, str], **kwargs) -> Iterator[Output]:
        """
        Execute the planning assistance engine to execute the input instruction and output the result as stream type.

        Args:
            inputs (Union[dict, str]): User input instruction.
                throughout generation. See more details in `common.trace` module.

        Returns:
            Output: Iterator after planning and executing for the input instruction.

        """
        # call planning assistance engine
        chat_history = (
            kwargs.get("runtime_context").agent_workflow_context.get(
                "workflow_chat_history", ConversationHistory()
            )
        ).get_all_messages()
        self.context_manager.msg_context.init_history(chat_history)
        if isinstance(inputs, dict):
            input_content = inputs.get(self.query_key_word)
            prompt_info = inputs.get(self.plan_prompt_key_word)
        elif isinstance(inputs, str):
            input_content = inputs
            prompt_info = None
        else:
            raise JiuWenBaseException(
                error_code=StatusCode.AGENT_INPUT_TYPE_ERROR.code,
                message=StatusCode.AGENT_INPUT_TYPE_ERROR.errmsg.format(
                    input_type=type(inputs)
                ),
            )

        await self.prepare_execution(inputs, kwargs)

        # set handler
        trace_manager = TraceManager.generate_manager(
            kwargs.pop("trace_handlers", None),
            get_instance_info(
                self,
                blacklist=[
                    "results",
                    "memory_inputs",
                    "memory",
                    "flows",
                    "workflow_dict",
                    "workflows",
                ],
            ),
        )
        trace_manager.on_chain_start(inputs)
        # get recommend plugins
        tools = await self.workspace.gen_tools(input_content)
        tool_switch_dict = kwargs.get("tool_switch_dict")
        if tool_switch_dict:
            if not isinstance(tool_switch_dict, dict):
                raise JiuWenBaseException(
                    error_code=StatusCode.AGENT_TOOL_SWITCH_ERROR.code,
                    message=StatusCode.AGENT_TOOL_SWITCH_ERROR.errmsg,
                )
            tools = self.update_tool_switch(tools, tool_switch_dict)
        try:
            yield_res = self.controller.stream(
                query=input_content,
                prompt_info=prompt_info,
                functions=tools,
                trace_handlers=get_child_manager(trace_manager)
                if trace_manager
                else None,
                runtime_context=kwargs.get("runtime_context"),
                contexts=kwargs.get("contexts"),
                multimodal_image=kwargs.get("multimodal_image"),
            )
            async for item in self._iterate_stream_planner_res(
                yield_res=yield_res, trace_manager=trace_manager
            ):
                yield item
        except Exception as e:
            yield e
            raise
        res = self.result
        trace_manager.on_chain_end(res)
        yield res

    def get_invoke_status(self):
        """
        get status of task when calling Task.invoke()
        Args:
            self: Task instance.

        Returns:
            status(str): status of task, the value of TaskInvokeStatus.
        """
        return self._invoke_status.value

    def update_tool_switch(self, tools: List[Tool], tool_switch_dict: Dict):
        """更新tool switch"""
        update_tools = list()
        tool_ids = set(tool_switch_dict.keys())
        exist_ids = list()
        for tool in tools:
            if tool.provider_type == ProviderType.PLUGIN:
                enable = tool_switch_dict.get(tool.invokable.id, True)
                cur_id = tool.invokable.id
            else:
                enable = tool_switch_dict.get(tool.tool_id, True)
                cur_id = tool.tool_id
            # 记录当前已有tool_id
            exist_ids.append(cur_id)
            if not enable:
                logger.info(f"react skip tools {cur_id}")
                continue
            update_tools.append(tool)
        differ_ids = tool_ids.difference(set(exist_ids))
        if differ_ids:
            logger.error(f"tool switch include invalid ids {list(differ_ids)}")
        return update_tools

    def update_plugins(self, plugins: list) -> None:
        """
        update plugins for task, the `plugins` list will replace the initialized plugins.
        Args:
            plugins: The list of plugins which replace the initialized plugins.

        Returns: None

        """
        if not plugins:
            self.workspace.plugins = []
        elif isinstance(plugins, list):
            check_index_list = [
                index
                for index, plugin in enumerate(plugins)
                if not isinstance(plugin, Invokable)
            ]
            if check_index_list:
                raise JiuWenBaseException(
                    StatusCode.AGENT_UPDATE_PLUGIN_WITH_TYPE_ERROR.code,
                    StatusCode.AGENT_UPDATE_PLUGIN_WITH_TYPE_ERROR.errmsg.format(
                        type(plugins[check_index_list[0]])
                    ),
                )
            self.workspace.plugins = plugins
        else:
            raise JiuWenBaseException(
                StatusCode.AGENT_UPDATE_PLUGIN_WITH_TYPE_ERROR.code,
                StatusCode.AGENT_UPDATE_PLUGIN_WITH_TYPE_ERROR.errmsg.format(
                    type(plugins)
                ),
            )

    def update_mcps(self, mcps: list) -> None:
        """
        update plugins for task, the `plugins` list will replace the initialized plugins.
        Args:
            mcps: The list of plugins which replace the initialized plugins.

        Returns: None

        """
        if not mcps:
            self.workspace.mcps = []
        elif isinstance(mcps, list):
            check_index_list = [
                index
                for index, mcp in enumerate(mcps)
                if not isinstance(mcp, Invokable)
            ]
            if check_index_list:
                raise JiuWenBaseException(
                    StatusCode.AGENT_UPDATE_MCP_WITH_TYPE_ERROR.code,
                    StatusCode.AGENT_UPDATE_MCP_WITH_TYPE_ERROR.errmsg.format(
                        type(mcps[check_index_list[0]])
                    ),
                )
            self.workspace.mcps = mcps
        else:
            raise JiuWenBaseException(
                StatusCode.AGENT_UPDATE_MCP_WITH_TYPE_ERROR.code,
                StatusCode.AGENT_UPDATE_MCP_WITH_TYPE_ERROR.errmsg.format(type(mcps)),
            )

    async def _iterate_stream_planner_res(
        self, yield_res: Generator, trace_manager: TraceManager
    ):
        """
        Loop yield_res generator to get the results from planner.
            index: the index of planner
            yield_res: the generator from Planner.stream()
            trace_manager: visualization tool class for Insight.
        """
        async for item in yield_res:
            if isinstance(item, dict):
                if item.get(self.ret_code_key_word, "") == RetCode.SUCCESS:  # 正常执行
                    execute_res = {"result": item.get("result", "")}
                    self.result = execute_res.get(self.result_key_word)
                elif item.get(self.ret_code_key_word, "") in [
                    RetCode.FUNC_CALL_GEN,
                    RetCode.API_EXEC_RESULT,
                    RetCode.API_EXCEPTION,
                    RetCode.CONT_OPT_RESULT,
                    RetCode.UX_SIGNAL,
                    RetCode.STREAM_OUTPUT_LLM,
                    RetCode.STREAM_REFLECTION,
                    RetCode.INTERMEDIATE_MESSAGE,
                    RetCode.STREAM_THINK_LLM,
                ]:
                    # 流式返回function call生成工具或执行结果、异常报错，还有内容优选的结果，还有对前端的控制信号，还有流式输出大模型的生成结果
                    yield item
                elif item.get(self.ret_code_key_word, "") == RetCode.FAILED:  # 执行报错
                    err = ValueError(
                        "Planning assistance engine failed to plan or execute"
                    )
                    trace_manager.on_chain_error(err)
                    raise err
                else:
                    err = ValueError("Unsupported plan_states_code.")
                    trace_manager.on_chain_error(err)
                    raise err

    def _verify_task_id(self, task_id):
        """
        verify task id
        Args:
            task_id:

        Returns:

        """
        if task_id and len(task_id) > self._max_task_id_length:
            raise JiuWenBaseException(
                error_code=StatusCode.AGENT_TASK_ID_INVALID_ERROR.code,
                message=StatusCode.AGENT_TASK_ID_INVALID_ERROR.errmsg.format(
                    self._max_task_id_length
                ),
            )
        return task_id
