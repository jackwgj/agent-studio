#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Provides the dynamic orchestration capability."""

from enum import Enum
from typing import List, Union, Iterator, Dict, Generator

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.common.utils.utils import format_exception_reason
from jiuwen.controller.utils.utils import WorkspaceUtils
from jiuwen.insight.manager import TraceManager, get_child_manager
from jiuwen.insight.utils import get_instance_info
from jiuwen.memory import ConversationMemory
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.common.exception import OrchestrationException
from jiuwen.orchestration.common.exception import TaskPluginException
from jiuwen.orchestration.model_io import BaseModel
from jiuwen.orchestration.utils import Output
from jiuwen.planner import PlanStrategyType
from jiuwen.planner import RetCode
from jiuwen.planner.common.enum_class import ProviderType
from jiuwen.planner.common.exception import WorkflowParseException
from jiuwen.planner.planner.planner import Planner
from jiuwen.planner.planner.planner_config import PlanConfig
from jiuwen.plugin import PluginManager
from jiuwen.plugin.common.exception import ErrorCode as plugin_ErrCode
from jiuwen.plugin.models.function import Function
from jiuwen.plugin.models.param import DESCRIPTION, WORKFLOW_NAME
from jiuwen.plugin.models.plugin import BasePlugin
from jiuwen.plugin.models.tool import Tool, WORKFLOW_PROMPT
from jiuwen.prompt import Template

RET_CODE = "ret_code"


class WorkflowKey(Enum):
    """
    This Python file contains an enumeration class named WorkflowKey. This class is used to define the keys that are
    used in the workflow configuration.

    The keys defined in this class include:
    - PLAN_CONFIG: This key is used to specify the configuration for the workflow.
    - PLUGINS: This key is used to specify the plugins that are used in the workflow.
    - MODEL: This key is used to specify the model that is used in the workflow.
    - PLAN_TYPE: This key is used to specify the type of the workflow.
    - PLUGIN_RETRIEVE_ENABLE: This key is used to specify whether the plugins are retrieved from the server.
    - PLAN_TEMPLATE: This key is used to specify the template for the workflow.
    """

    PLAN_CONFIG = "planConfig"
    PLUGINS = "plugins"
    MODEL = "model"
    PLAN_TYPE = "planType"
    PLUGIN_RETRIEVE_ENABLE = "pluginRetrieveEnable"
    PLAN_TEMPLATE = "planTemplate"


class StandardPlanKey(Enum):
    """
    Enumeration class for standard plan keys.

    Attributes:
       PLAN_TYPE (str): The key for the plan type.
       PROMPT (str): The key for the prompt.
       CONFIG (str): The key for the configuration.
    """

    PLAN_TYPE = "planType"
    PROMPT = "prompt"
    CONFIG = "config"


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


class Task(Invokable):
    """
    Task dynamically plan input instruction into subtasks, execute the subtasks, and output results.
    Multiple task planning policies and models can be set.

    """

    task_key_word = "query"
    planning_key_word = "result"
    plan_prompt_key_word = "prompt_info"
    _max_task_id_length = 32

    def __init__(
        self,
        plan_type: PlanStrategyType,
        model: BaseModel = None,
        prompt: Template = None,
        planning_config_file: str = None,
        plan_config: PlanConfig = None,
        plugins: Union[
            List[Union[Function, BasePlugin]],
            Function,
            BasePlugin,
            Dict[str, List[str]],
            str,
        ] = None,
        plugin_retrieve_enable: bool = False,
        top_k: int = 3,
        chains: Union[dict, Invokable] = None,
        task_id: str = None,
        workflow_irs: List[Dict] = None,
        workflows: List = None,
    ) -> None:
        """
        Initialize the neccessary moudles.

        Args:
            prompt (Template): Specify the prompt template that can be used, required.
            plan_type (PlanType): Plan type for planning assistance engine, required.
            planning_config_file (str): Config file for planning assistance engine, Default: ``None``, optional.
            plan_config (PlanConfig): Directly transfer parameters for configuration, Default: ``None``, optional.
            plugins (Union[List[Union[Function, BasePlugin]], Function, BasePlugin, dict, str]): Specify the plugin's
                functions by user, Default: ``None``, optional.
            plugin_retrieve_enable (bool): Specifies whether to enable the plugin retrive function. If ``True``,
                the system automatically searches for appropriate plugin's functions based on the user query, except
                for the plugin's functions specified by the user, finally task will use the union of retrieved plugin's
                functions and specified plugin's functions to complete the instruction, Default: ``False``, optional.
            top_k (int): Specifies the number of plugin's functions returned by plugin retrieval. This parameter is
                useful only when the ``plugin_retrieve_enable`` parameter is ``True``, Default: ``3``, optional.
            model (BaseModel): Large language model used to complete task, required.
            chains (Union[dict, Invokable]): When ``plan_type==PlanType.PlanAndSolve``,
                user-defined components can be set.
                Users can inherit the BaseSubTaskSummary, BaseRetriever, BaseAnswerAssemble, BaseSubQuestionKeyword,
                BaseReferenceDigest,
                BaseSearchAndSummary class and implement the run() method of the corresponding class to implement and
                transfer
                user-defined components. The components that support replacement can be obtained through the
                SupportedResearchChain class. optional.
            task_id (str): Unique ID of a task. If this parameter is transferred, the data can be used to restore the
                site, optional.
                If this parameter is not transferred, the onsite restoration capability cannot be provided, optional.
        Returns:
            None.

        """
        self._invoke_status = TaskInvokeStatus.INIT
        self.planners = list()
        self.results = list()
        self.workflow_dict = dict()
        # 适配平台化和框架侧agent_ir两种逻辑
        self.workflows = workflow_irs if workflow_irs else workflows
        self.plugin_manager = PluginManager()
        self.memory_inputs = None
        self.task_id = self._verify_task_id(task_id=task_id)
        # default memory, the `` with_memory`` function only support Conversation memory, if Task need other kind of
        # memory, rewrite the with_memory function
        self.memory = ConversationMemory()
        # init plugins
        if plugins:
            # plugin names in dict
            if isinstance(plugins, dict):
                functions = self._get_plugin_from_dict(plugins)
            # plugin name
            elif isinstance(plugins, str):
                functions = self._get_plugin_by_name(plugins)
            # plugin instance list
            elif isinstance(plugins, (BasePlugin, Invokable)):
                functions = [plugins]
            elif isinstance(plugins, list):
                functions = plugins
            else:
                raise ValueError("Unsupported type for plugin functions")
        else:
            functions = []
        functions = WorkspaceUtils.get_plugin_from_list(functions)
        self.init_plugins = functions
        self.init_mcps = []
        self.rec_plugin = plugin_retrieve_enable
        self.top_k_functions = top_k
        # create planners
        if not isinstance(plan_type, list):
            plan_type = [plan_type]
        self.is_in_react = [False for _ in range(len(plan_type))]
        self.chains = None
        if chains:
            logger.warning('The "chains" variable has been deprecated.')

        for plan in plan_type:
            self.planners.append(
                Planner(
                    plan_strategy_type=plan,
                    task_id=self.task_id,
                    model=model,
                    plan_config_file=planning_config_file,
                    plan_config=plan_config,
                )
            )

    @staticmethod
    def get_workflow_params(workflow_ir: dict) -> dict:
        """格式化workflow Panel接口"""
        components_list = workflow_ir.get("components", {})

        if len(components_list) == 0:
            raise WorkflowParseException(message="get workflow start node failed")
        for component in components_list:
            if component.get("type", "") == "jiuwen.start":
                start_node = component
                break
        else:
            raise WorkflowParseException(message="get workflow start node failed")

        properties = dict()
        input_param = start_node.get("inputs", {})
        if not input_param:
            raise WorkflowParseException(
                message="get workflow start node param is null"
            )
        configs = start_node.get("configs", dict())
        system_fields = configs.get("systemFields", dict()).get("inputs", list())
        sys_params_with_desc = {
            _.get("id"): _.get("description", "")
            for _ in system_fields
            if _.get("id") is not None
        }
        user_fields = configs.get("userFields", dict()).get("inputs", list())
        user_params_with_desc = {
            _.get("id"): _.get("description", "")
            for _ in user_fields
            if _.get("id") is not None
        }

        properties.update(sys_params_with_desc)
        properties.update(user_params_with_desc)
        workflow_info = dict(
            name=workflow_ir.get(WORKFLOW_NAME),
            description=workflow_ir.get(DESCRIPTION, "") + WORKFLOW_PROMPT,
            parameters=dict(type="object", properties=properties),
        )
        return workflow_info

    async def ainvoke(self, inputs: Union[dict, str], **kwargs) -> Output:
        """
        Invoke the planning assistance engine to execute the input instruction and output the result.

        Args:
            inputs (Union[dict, str]): User input instruction.
                throughout generation. See more details in `common.trace` module.

        Returns:
            Output: Result after planning and executing for the input instruction.
        """
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
        await trace_manager.on_chain_start(inputs)
        # call memory
        self.results = list()
        # call planning assistance engine
        if isinstance(inputs, dict):
            input_content = inputs.get(self.task_key_word)
            prompt_info = inputs.get(self.plan_prompt_key_word)
        elif isinstance(inputs, str):
            input_content = inputs
            prompt_info = None
        else:
            raise ValueError(f'The input type "{type(inputs)}" is not supported now.')
        # if task want support multi plan type, the memory_inputs may need to be a list.
        if not all(self.is_in_react):
            self.memory_inputs = input_content
        # get recommend plugins
        tools = self.gen_tools(input_content)
        for index, planner in enumerate(self.planners):
            plan_status_code, execute_res = planner(
                query=input_content,
                prompt_info=prompt_info,
                functions=tools,
                chains=self.chains,
                trace_handlers=get_child_manager(trace_manager)
                if trace_manager
                else None,
                contexts=kwargs.get("contexts"),
                runtime_context=kwargs.get("runtime_context"),
            )

            if plan_status_code == RetCode.FAILED:
                self._invoke_status = TaskInvokeStatus.FAILED
                err = ValueError("Planning assistance engine failed to plan or execute")
                await trace_manager.on_chain_error(err)
                raise err
            if plan_status_code == RetCode.SUCCESS:
                self.is_in_react[index] = False
                self._invoke_status = TaskInvokeStatus.SUCCESS

            elif plan_status_code == RetCode.WAIT_USER_INPUT:
                self._invoke_status = TaskInvokeStatus.WAIT_USER_INPUT

            else:
                self._invoke_status = TaskInvokeStatus.UNKNOWN
                err = ValueError(f"Unsupported plan_states_code: {plan_status_code}.")
                await trace_manager.on_chain_error(err)
                raise err
            self.results.append(
                execute_res.get(self.planning_key_word)
                if execute_res and isinstance(execute_res, dict)
                else ""
            )
        res = self._process_results()
        if self.memory and not all(self.is_in_react):
            self.memory.save_context(inputs=self.memory_inputs, outputs=res)
        await trace_manager.on_chain_end(res)
        return res

    async def astream(self, inputs: Union[dict, str], **kwargs) -> Iterator:
        """
        Execute the planning assistance engine to execute the input instruction and output the result as stream type.

        Args:
            inputs (Union[dict, str]): User input instruction.
                throughout generation. See more details in `common.trace` module.

        Returns:
            Output: Iterator after planning and executing for the input instruction.

        """
        # call planning assistance engine
        if isinstance(inputs, dict):
            input_content = inputs.get(self.task_key_word)
            prompt_info = inputs.get(self.plan_prompt_key_word)
        elif isinstance(inputs, str):
            input_content = inputs
            prompt_info = None
        else:
            raise ValueError(f'The input type "{type(inputs)}" is not supported now.')
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
        await trace_manager.on_chain_start(inputs)
        # call memory
        self.results = list()
        # if task want support multi plan type, the memory_inputs may need to be a list.
        if not all(self.is_in_react):
            self.memory_inputs = input_content
        # get recommend plugins
        tools = self.gen_tools(input_content)
        tool_switch_dict = kwargs.get("tool_switch_dict")
        if tool_switch_dict:
            if not isinstance(tool_switch_dict, dict):
                raise ValueError("tool switch dict type invalid")
            tools = self.update_tool_switch(tools, tool_switch_dict)
        for index, planner in enumerate(self.planners):
            try:
                yield_res = planner.stream(
                    query=input_content,
                    prompt_info=prompt_info,
                    functions=tools,
                    chains=self.chains,
                    task_id=self.task_id,
                    trace_handlers=get_child_manager(trace_manager)
                    if trace_manager
                    else None,
                    runtime_context=kwargs.get("runtime_context"),
                    contexts=kwargs.get("contexts"),
                    multimodal_image=kwargs.get("multimodal_image"),
                    round_id=kwargs.get("round_id"),
                    session_id=kwargs.get("session_id"),
                )
                async for chunk in self._iterate_stream_planner_res(
                    index=index, yield_res=yield_res, trace_manager=trace_manager
                ):
                    yield chunk
            except Exception as e:
                formatted_exception = JiuWenBaseException(
                    error_code=StatusCode.PLANNER_STREAM_EXCEPTION_ERROR.code,
                    message=StatusCode.PLANNER_STREAM_EXCEPTION_ERROR.errmsg.format(
                        reason=f"{format_exception_reason(e)}"
                    ),
                )
                yield formatted_exception
                raise formatted_exception from e
        res = self._process_results()
        if self.memory and not all(self.is_in_react):
            self.memory.save_context(inputs=self.memory_inputs, outputs=res)
        await trace_manager.on_chain_end(res)
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

    def update_plugins(self, plugins: list) -> None:
        """
        update plugins for task, the `plugins` list will replace the initialized plugins.
        Args:
            plugins: The list of plugins which replace the initialized plugins.

        Returns: None

        """
        if not plugins:
            self.init_plugins = []
        elif isinstance(plugins, list):
            check_index_list = [
                index
                for index, plugin in enumerate(plugins)
                if not isinstance(plugin, Invokable)
            ]
            if check_index_list:
                raise OrchestrationException(
                    StatusCode.TASK_UPDATE_PLUGIN_WITH_TYPE_ERROR.code,
                    "Invalid plugin type at position {}.".format(check_index_list[0]),
                )
            self.init_plugins = plugins
        else:
            raise OrchestrationException(
                StatusCode.TASK_UPDATE_PLUGIN_WITH_TYPE_ERROR.code,
                "Plugins must be provided as a list of Invokable instances.",
            )

    def update_mcps(self, mcps: list) -> None:
        """
        update mcps for task, the `mcps` list will replace the initialized plugins.
        Args:
            mcps: The list of plugins which replace the initialized plugins.

        Returns: None

        """
        if not mcps:
            self.init_mcps = []
        elif isinstance(mcps, list):
            check_index_list = [
                index
                for index, mcp in enumerate(mcps)
                if not isinstance(mcp, Invokable)
            ]
            if check_index_list:
                raise OrchestrationException(
                    StatusCode.TASK_UPDATE_PLUGIN_WITH_TYPE_ERROR.code,
                    StatusCode.TASK_UPDATE_PLUGIN_WITH_TYPE_ERROR.errmsg.format(
                        type(mcps[check_index_list[0]])
                    ),
                )
            self.init_mcps = mcps
        else:
            raise OrchestrationException(
                StatusCode.TASK_UPDATE_PLUGIN_WITH_TYPE_ERROR.code,
                StatusCode.TASK_UPDATE_PLUGIN_WITH_TYPE_ERROR.errmsg.format(type(mcps)),
            )

    def update_tool_switch(self, tools: List[Tool], tool_switch_dict: Dict):
        """更新tool switch"""
        update_tools = list()
        tool_ids = list(tool_switch_dict.keys())
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
        differ_ids = set(tool_ids).difference(set(exist_ids))
        if differ_ids:
            logger.error(f"tool switch include invalid ids {list(differ_ids)}")
        return update_tools

    def gen_tools(self, input_content: str) -> List[Tool]:
        """生成function_call tool候选list"""
        from jiuwen.orchestration.flow.workflow import sync_build_workflow

        tools = []
        output_plugins = self._get_all_plugins(input_content)
        for plugin in output_plugins:
            if not isinstance(plugin, Invokable):
                logger.error("plugin type error while wrap plugin into Tool")
                continue
            tools.append(
                Tool(
                    tool_id=plugin.name,
                    provider_type=ProviderType.PLUGIN,
                    invokable=plugin,
                )
            )
        current_ids = list()
        if self.workflows:
            for workflow in self.workflows:
                if isinstance(workflow, dict):
                    # 适配平台ir传入的逻辑
                    workflow_name = workflow.get("workflowName")
                    params = self.get_workflow_params(workflow)
                    workflow_instance = sync_build_workflow(
                        workflow, global_variables={}, is_workflow_mode=False
                    )
                    workflow = Tool(
                        tool_id=workflow_name,
                        provider_type=ProviderType.WORKFLOW,
                        invokable=workflow_instance,
                        parameters=params,
                    )
                current_ids.append(workflow.tool_id)
                if workflow.tool_id not in self.workflow_dict:
                    self.workflow_dict.update({workflow.tool_id: workflow})
        if isinstance(self.workflow_dict, dict):
            self.workflow_dict = {
                key: self.workflow_dict.get(key)
                for key in self.workflow_dict
                if key in current_ids
            }
            tools.extend(list(self.workflow_dict.values()))
        return tools

    def _process_results(self) -> str:
        """
        process results
        Returns:

        """
        if len(self.results) == 1:
            return self.results[0]
        raise NotImplementedError("Only single result supported now.")

    def _get_all_plugins(self, instruction) -> list:
        """Return a union of user input and recommend plugins for each instruction"""
        output_plugins = list()
        if self.rec_plugin:
            recommend_plugins = self.plugin_manager.retrieve(
                query=instruction, top_k=self.top_k_functions
            )
            redundant_plugins = recommend_plugins
            if self.init_plugins and isinstance(self.init_plugins, list):
                redundant_plugins += self.init_plugins
            for plugin in redundant_plugins:
                if plugin in output_plugins:
                    continue
                output_plugins.append(plugin)
        else:
            output_plugins = self.init_plugins
        return output_plugins

    def _verify_task_id(self, task_id):
        """
        verify task id
        Args:
            task_id:

        Returns:

        """
        if task_id and len(task_id) > self._max_task_id_length:
            raise ValueError(
                f'The length of "task_id" exceeds the maximum {self._max_task_id_length}.'
            )
        return task_id

    def _get_plugin_from_dict(
        self, plugin_dict: Dict[str, List[str]]
    ) -> List[Invokable]:
        """plugin_dict is a map from plugin name to a list of function names."""

        def _safe_log(v) -> str:
            s = str(v)
            return s.replace("\r", " ").replace("\n", " ")

        function_or_api_list = list()
        for plugin_name, functions_or_rest_apis in plugin_dict.items():
            if not isinstance(functions_or_rest_apis, (list, tuple, set)):
                logger.error(
                    f"The functions of plugin {_safe_log(plugin_name)} should be a list/tuple/set of str "
                    f"but got {functions_or_rest_apis}",
                    simple_log=(
                        f"The functions of plugin {_safe_log(plugin_name)} should be a list/tuple/set of str "
                        f"but got {type(functions_or_rest_apis).__name__}"
                    ),
                )

                raise TaskPluginException(
                    StatusCode.TASK_PLUGIN_TYPE_ERROR.code,
                    StatusCode.TASK_PLUGIN_TYPE_ERROR.errmsg.format(
                        plugin_name, type(functions_or_rest_apis).__name__
                    ),
                )
            for func_name in functions_or_rest_apis:
                # Plugin name and function name are checked by plugin_manager.
                intent = f"Get plugin by plugin_name={_safe_log(plugin_name)} and func_name={_safe_log(func_name)}"
                function_or_api_list.extend(
                    WorkspaceUtils.plugin_operate(
                        self.plugin_manager.get(
                            plugin_name=plugin_name, func_name=func_name
                        ),
                        intent,
                        plugin_ErrCode,
                    )
                )
        return function_or_api_list

    def _get_plugin_by_name(self, plugin_name: str) -> List[Invokable]:
        """
        get plugin by name
        Args:
            plugin_name:

        Returns:

        """
        return WorkspaceUtils.plugin_operate(
            self.plugin_manager.get(plugin_name=plugin_name),
            f"Get plugin by {plugin_name=}",
            plugin_ErrCode,
        )

    async def _iterate_stream_planner_res(
        self, index: int, yield_res: Generator, trace_manager: TraceManager
    ):
        """
        Loop yield_res generator to get the results from planner.
            index: the index of planner
            yield_res: the generator from Planner.stream()
            trace_manager: visualization tool class for Insight.
        """
        for item in yield_res:
            if isinstance(item, dict):
                if item.get(RET_CODE, "") == RetCode.SUCCESS:  # 正常执行
                    self.is_in_react[index] = False
                    execute_res = {"result": item.get("result", "")}
                    self.results.append(execute_res.get(self.planning_key_word))
                elif item.get(RET_CODE, "") in [
                    RetCode.FUNC_CALL_GEN,
                    RetCode.API_EXEC_RESULT,
                    RetCode.API_EXCEPTION,
                    RetCode.CONT_OPT_RESULT,
                    RetCode.UX_SIGNAL,
                    RetCode.STREAM_OUTPUT_LLM,
                    RetCode.STREAM_THINK_LLM,
                    RetCode.STREAM_REFLECTION,
                ]:
                    # 流式返回function call生成工具或执行结果、异常报错，还有内容优选的结果，还有对前端的控制信号，还有流式输出大模型的生成结果
                    yield item
                elif item.get(RET_CODE, "") == RetCode.FAILED:  # 执行报错
                    err = ValueError(
                        "Planning assistance engine failed to plan or execute"
                    )
                    await trace_manager.on_chain_error(err)
                    raise err
                else:
                    err = ValueError("Unsupported plan_states_code.")
                    await trace_manager.on_chain_error(err)
                    raise err
