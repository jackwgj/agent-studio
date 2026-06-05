#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import copy
import functools
import json
import re
import uuid
from dataclasses import dataclass, field
from enum import Enum
from json import JSONDecodeError
from typing import List, Union, AsyncGenerator, Any

from jiuwen.common.log.base import logger
from jiuwen.controller.common.enum import PlanAndExecuteCode
from jiuwen.controller.common.message import Message
from jiuwen.controller.common.message_type import MessageType
from jiuwen.controller.common.task import Task
from jiuwen.controller.common.task_type import TaskType
from jiuwen.controller.context_manager.context_manager import ContextManager
from jiuwen.controller.task_planner.planning_modules.ask_user_module import (
    AskUserModule,
)
from jiuwen.controller.task_planner.planning_modules.llm_module import LLMModule
from jiuwen.controller.task_planner.task_planner import TaskPlanner
from jiuwen.controller.task_planner.task_queue import TaskQueue
from jiuwen.planner.common.result_output_callback import ResultOutputStreamCallback
from jiuwen.plugin.models.mcpapi import McpAPI
from jiuwen.plugin.models.restfulapi import RestFulAPI

NAME = "name"


class ExecuteType(Enum):
    REACT = "react"
    EXECUTE = "execute"


class StepStatus(Enum):
    NOT_STARTED = "NOT_STARTED"
    IN_PROGRESS = "IN_PROGRESS"
    COMPLETED = "COMPLETED"
    ERROR = "ERROR"


@dataclass
class PlanStep:
    desc: str = None
    status: StepStatus = StepStatus.NOT_STARTED
    arguments: List = field(default_factory=list)
    response: List = field(default_factory=list)
    error_message: str = None


SCHEMA_EXECUTE_STEP = """
class ExecuteStep:
    desc: str = None  # 当前步骤的任务描述
    name: str = None  # 当前任务所需调用的插件名称
"""


@dataclass
class ExecuteStep(PlanStep):
    tool_name: str = None

    @staticmethod
    def get_schema():
        """获取当前类的schema"""
        return SCHEMA_EXECUTE_STEP


class StatusCode(Enum):
    PLANNING = "PLANNING"
    TASK_EXECUTING = "TASK_EXECUTING"
    FINISHED = "FINISHED"
    FAILED = "FAILED"
    WAIT_FOR_USER_INPUT = "WAIT_FOR_USER_INPUT"


@dataclass
class Plan:
    task: str = None
    status: StatusCode = StatusCode.PLANNING
    steps: List[Union[PlanStep, ExecuteStep]] = None
    current_step_idx: int = -1

    def __str__(self):
        """获取Plan执行状况"""
        if self.steps is None:
            return ""
        plan_string = [
            "任务目标：",
            "    {task}".format(task=self.task),
            "任务状态：",
            "    {status}".format(status=self.status),
            "执行状态：",
        ]
        for i, step in enumerate(self.steps):
            if step.status == StepStatus.COMPLETED:
                plan_string.append(str(i + 1) + ". " + step.desc)
                plan_string.append(
                    "    {step_result}".format(step_result=step.response)
                )
            elif step.status == StepStatus.ERROR:
                plan_string.append(str(i + 1) + ". " + step.desc)
                plan_string.append("**当前步骤执行失败，失败原因：**")
                plan_string.append(
                    "    {step_error}".format(step_error=step.error_message)
                )
                break
            elif step.status == StepStatus.IN_PROGRESS:
                plan_string.append("**当前正在执行** " + str(i + 1) + ". " + step.desc)
                break
            elif step.status == StepStatus.NOT_STARTED:
                plan_string.append("**当前你需要执行的步骤是：**")
                plan_string.append(str(i + 1) + ". " + step.desc)
                break
        return "\n".join(plan_string)

    def get_current_step(self):
        """获取当前任务"""
        return self.steps[self.current_step_idx]

    def step(self):
        """当前步骤执行完成，设置下一步"""
        if self.current_step_idx >= 0:
            self.steps[self.current_step_idx].status = StepStatus.COMPLETED
        if self.current_step_idx == len(self.steps) - 1:
            self.status = StatusCode.FINISHED
        else:
            self.current_step_idx += 1
            self.status = StatusCode.TASK_EXECUTING

    def append_current_step_result(self, result):
        """将当前执行结果填入"""
        if not self.steps[self.current_step_idx].response:
            self.steps[self.current_step_idx].response = [result]
        else:
            self.steps[self.current_step_idx].response.append(result)

    def set_current_step_error(self, error):
        """当前报错信息填入"""
        self.steps[self.current_step_idx].error_message = error
        self.steps[self.current_step_idx].status = StepStatus.ERROR
        self.status = StatusCode.FAILED

    def get_plan_status(self):
        """获取当前状态"""
        return self.status


@dataclass
class ToolUseState:
    """控制器状态类，用于存储ToolUsePlanner的状态"""

    current_workflow_index: int = 0
    start_workflow_executed: bool = False
    start_workflow_completed: bool = False
    end_workflow_executed: bool = False
    end_workflow_completed: bool = False
    global_iteration_count: int = 0
    processed_workflow_names: set = field(default_factory=set)
    task_end: bool = False
    task_id: str = field(default_factory=str)
    agent_contexts: dict = field(default_factory=dict)
    plan: Plan = field(default_factory=Plan)


class ToolUsePlanner(TaskPlanner):
    def __init__(
        self,
        plan_config,
        llm,
        plugins,
        workflows,
        context_manager: ContextManager,
        task_id,
    ):
        super().__init__(plan_config, llm, plugins, workflows, context_manager, task_id)
        execute_llm_config = copy.deepcopy(plan_config)
        execute_llm_config.propose_next_config.prompt_template_name += "-toolUseAgent"
        self.execute_llm_module = LLMModule(
            execute_llm_config, llm, copy.deepcopy(context_manager)
        )
        self.ask_user_module = AskUserModule(llm, copy.deepcopy(context_manager))
        self.result_output_callback = ResultOutputStreamCallback()
        self.task_queue = TaskQueue()
        self.plan_llm_module = LLMModule(plan_config, llm, context_manager)
        self._plan: Plan = Plan()
        ask_user_desc = self.format_tool_description(
            name="ask_user",
            description="和用户交互获取所需要的信息",
            params=[
                {
                    "name": "query",
                    "required": "True",
                    "description": "询问用户的问题",
                    "type": "string",
                }
            ],
        )
        self.tool_descriptions = {
            "ask_user": {"desc": ask_user_desc, "type": "ask_user"}
        }

    @staticmethod
    def process_llm_stream(func) -> callable:
        """
        装饰调用大模型方法，增加调用前后处理，装饰后的函数在调用时需增加time_dict参数

        Args:
            func: callable，调用大模型方法

        Returns:
            callable: 装饰后的函数
        """

        @functools.wraps(func)
        async def wrapper(self, *args, time_dict=None, response=None):
            if time_dict is None:
                time_dict = {}
            if response is None:
                response = []
            for item in self.result_output_callback.on_llm_output_stream_started():
                yield item
            yield_res = func(self, *args)
            async for item in yield_res:
                if isinstance(item, dict) and "time_consumption" in item:
                    time_dict.update(item.get("time_consumption", {}))
                else:
                    response.append(item.get("content", ""))
                    yield item

        return wrapper

    @staticmethod
    def format_tool_description(name, description, params):
        """拼接工具信息提示词"""
        return (
            f'{{"工具名称": "{name}", '
            f'"工具描述": "{description}", '
            f'"arguments": {json.dumps(params, ensure_ascii=False)}}}'
        )

    def create_plugin_task(self, plugin, argument, runtime_data):
        """返回插件任务"""
        task_unique_id = f"{self.task_id}_plugin_{plugin.name}_{uuid.uuid4().hex[:8]}"
        # 准备输入数据
        runtime_data = {
            **runtime_data,
            "plugins": [plugin],
        }
        input_data = {
            "plugin_name": plugin.name,
            "function_name": plugin.name,
            "function_call": {"name": plugin.name, "arguments": str(argument)},
            "arguments": str(argument),
            "plugin": plugin,
            "runtime_data": runtime_data,
            "time_dict": {},
        }
        logger.info(
            "successfully create plugin task: {}, argument: {}".format(
                plugin.name, str(argument)
            ),
            simple_log="successfully create plugin task and argument",
        )
        return Task(
            id=task_unique_id, task_type=TaskType.PLUGIN_START, input_data=input_data
        )

    def create_mcp_task(self, mcp, argument, runtime_data):
        """返回插件任务"""
        task_unique_id = f"{self.task_id}_plugin_{mcp.name}_{uuid.uuid4().hex[:8]}"
        # 准备输入数据
        runtime_data = {
            **runtime_data,
            "mcps": [mcp],
        }
        input_data = {
            "mcp_name": mcp.name,
            "function_name": mcp.name,
            "function_call": {"name": mcp.name, "arguments": str(argument)},
            "arguments": str(argument),
            "mcp": mcp,
            "runtime_data": runtime_data,
            "time_dict": {},
        }
        logger.info(
            "successfully create mcp task: {}, argument: {}".format(
                mcp.name, str(type(argument))
            )
        )
        return Task(
            id=task_unique_id, task_type=TaskType.MCP_START, input_data=input_data
        )

    def update_plugins(self, plugins: List[RestFulAPI]):
        """update plugin infos and create description for prompt"""
        self.plugins = plugins
        for plugin in plugins:
            params = [
                {
                    "name": p.name,
                    "required": p.required,
                    "description": p.description,
                    "type": p.type,
                }
                for p in plugin.params
            ]
            desc = self.format_tool_description(plugin.name, plugin.description, params)
            self.tool_descriptions.update(
                {plugin.name: {"desc": desc, "type": "plugin"}}
            )
        logger.info(
            "successfully update plugins: {}".format(
                ",".join((p.name for p in plugins))
            )
        )

    def update_mcps(self, mcps: List[McpAPI]):
        """update mcp infos and create description for prompt"""
        self.mcps = mcps
        for mcp in mcps:
            params = [
                {
                    "name": p,
                    "required": p in mcp.parameters.get("required", []),
                    "description": mcp.parameters.get("properties", {})[p].get(
                        "title", ""
                    ),
                    "type": mcp.parameters.get("properties", {})[p].get("type", ""),
                }
                for p in mcp.parameters.get("properties", {})
            ]
            desc = self.format_tool_description(mcp.name, mcp.description, params)
            self.tool_descriptions.update({mcp.name: {"desc": desc, "type": "mcp"}})
        logger.info(
            "successfully update mcps: {}".format(",".join((p.name for p in mcps)))
        )

    def get_state(self):
        """获取当前Planner状态（用于向后兼容）

        Returns:
            ToolUseState: 控制器状态对象
        """
        contexts = {
            "plan_context": self.plan_llm_module.context_manager.engine.get_messages(),
            "execute_context": self.execute_llm_module.context_manager.engine.get_messages(),
            "ask_user_context": self.ask_user_module.context_manager.engine.get_messages(),
        }
        return ToolUseState(
            task_id=self.task_id, agent_contexts=contexts, plan=self._plan
        )

    def load_state(self, state):
        """加载当前ControllerPlanner的状态

        Args:
            state:

        Returns:

        """
        self.__load_from_agent_state(state)

    def plan(self, message: Message) -> Union[Message, Task, None]:
        pass

    async def plan_stream(self, message: Message):
        """使用大模型规划"""
        response = []
        message.runtime_data = {
            **message.runtime_data,
            "prompt_info": {
                "tools": "\n".join(
                    (
                        self.tool_descriptions.get(k, {}).get("desc", "")
                        for k in self.tool_descriptions
                    )
                )
            },
        }
        if self._plan.steps:
            message.content = "\n当前规划任务执行情况：\n" + str(self._plan)
        async for result in self._process_plan_llm_stream(message, response=response):
            yield result
        if not self.load_plan_from_stream(response):
            yield PlanAndExecuteCode.SUCCESS

    async def execute_stream(self, message: Message):
        """ask use工具单独处理"""
        step = self._plan.get_current_step()
        if step.tool_name == "ask_user":
            async for result in self._process_ask_user_llm_stream(message):
                yield result
        else:
            async for result in self._process_execute_llm_stream(message):
                yield result

    def load_plan_from_stream(self, stream: List[str]):
        """
        从流式输出中加载plan

        Args:
            stream: 大模型流式输出
        """
        response = "".join(stream)
        if "```json" in response:
            pattern = r"```json(.*?)```"
            match = re.search(pattern, response, re.DOTALL)
            if match:
                json_string = match.group(1).strip()
            else:
                json_string = ""
            try:
                plan = json.loads(json_string)
                if self._plan.task is None:
                    steps = [
                        ExecuteStep(
                            tool_name=s.get("tool_name", ""), desc=s.get("desc", "")
                        )
                        for s in plan.get("steps", {})
                    ]
                    self._plan = Plan(
                        task=plan.get("task", ""), steps=steps, current_step_idx=0
                    )
                else:
                    steps = [
                        step
                        for step in self._plan.steps
                        if step.status == StepStatus.COMPLETED
                    ]
                    steps_to_extend = [ExecuteStep(**s) for s in plan.get("steps", {})]
                    steps.extend(steps_to_extend)
                    self._plan.steps = steps
                self._plan.status = StatusCode.TASK_EXECUTING
                logger.info("Successfully create plan.")
            except JSONDecodeError as e:
                logger.error(
                    f"JsonDecodeError for load_plan_from_stream: {e}",
                    simple_log="JsonDecodeError for load_plan_from_stream.",
                )
                self._plan.status = StatusCode.PLANNING
            return True
        self.plan_llm_module.context_manager.set_result_message(response)
        self._plan.status = StatusCode.WAIT_FOR_USER_INPUT
        return False

    async def stream(self, message: Message) -> AsyncGenerator:
        """根据规划状态选取执行的分支"""
        # 添加用户输入为上下文
        if message.message_type == MessageType.USER_INPUT:
            self.context_manager.add_user_message(message.content)
            if self._plan.status == StatusCode.WAIT_FOR_USER_INPUT:
                if self._plan.task:
                    self._plan.status = StatusCode.TASK_EXECUTING
                else:
                    self._plan.status = StatusCode.PLANNING

        # 根据Plan状态选择执行分支
        step_output = None
        while step_output not in (
            PlanAndExecuteCode.SUCCESS,
            PlanAndExecuteCode.WAIT_FOR_TOOL_COMPLETION,
        ):
            async for step in self.stream_step(message):
                step_output = step
                yield step

    async def stream_step(self, message: Message):
        """根据Plan状态选择执行分支"""
        if self._plan.status == StatusCode.WAIT_FOR_USER_INPUT:
            yield PlanAndExecuteCode.SUCCESS
        elif self._plan.status == StatusCode.FINISHED:
            async for result in self.plan_stream(message):
                yield result
        elif self._plan.status == StatusCode.PLANNING:
            yield PlanAndExecuteCode.PLANNING
            async for result in self.plan_stream(message):
                yield result
        elif self._plan.status == StatusCode.TASK_EXECUTING:
            async for result in self.execute_stream(message):
                yield result
        elif self._plan.status == StatusCode.FAILED:
            self._plan.status = StatusCode.PLANNING

    async def stream_plan_task(self, message: Message) -> AsyncGenerator[Any, None]:
        """流式规划任务并处理结果

        接收一条消息，返回一个任务或者消息

        Args:
            message: 输入消息，插件执行结果，或者用户输入

        Yields:
            流式输出内容，包括最终的任务或消息
        """
        logger.debug(f"Stream planning task for message type {message.message_type}")

        # 处理流式输出
        async for chunk in self.stream(message):
            yield chunk
            if chunk == PlanAndExecuteCode.SUCCESS:
                return

        # 获取队列中的下一个任务, 如果沒有新任务产生，则继续恢复上一个任务的执行
        if not self.task_queue.is_pending_task_empty():
            next_task = self.task_queue.get_next_task()
            logger.info(f"Yielding next task from queue: {next_task.task_type}")
            yield next_task

    @process_llm_stream
    async def _process_plan_llm_stream(self, message):
        """
        调用大模型进行任务规划

        Args:
            message: 输入信息

        Yields:
            大模型流式输出
        """
        async for result in self.plan_llm_module.stream(message):
            yield result

    async def _process_ask_user_llm_stream(self, message):
        step = self._plan.get_current_step()
        self.ask_user_module.set_task(step.desc)
        if step.status == StepStatus.NOT_STARTED:
            self.ask_user_module.context_manager.clear_all()
            step.status = StepStatus.IN_PROGRESS
            message = None
        async for res in self.ask_user_module.stream(message):
            if isinstance(res, dict) and "role" in res:
                yield res
        response = self.ask_user_module.context_manager.get_latest_assistant_message()
        if "```json" in response.content:
            self._plan.append_current_step_result(response.content)
            self._plan.step()
        else:
            self.context_manager.set_result_message(response.content)
            self._plan.status = StatusCode.WAIT_FOR_USER_INPUT

    async def _process_execute_llm_stream(self, message):
        """直接执行对应插件，返回Task"""
        step = self._plan.get_current_step()
        if step.status == StepStatus.IN_PROGRESS:
            if message.message_type in (
                MessageType.PLUGIN_COMPLETION,
                MessageType.MCP_COMPLETION,
            ):
                if message.content.get("exec_res", {}).get("errCode", 0) != 0:
                    self._plan.set_current_step_error(
                        message.content.get("errMessage", "")
                    )
                    return
                self._plan.append_current_step_result(message.content)
            if not self.task_queue.is_pending_task_empty():
                yield PlanAndExecuteCode.WAIT_FOR_TOOL_COMPLETION
            else:
                self._plan.step()
            if self._plan.status == StatusCode.FINISHED:
                result = ["当前任务已经完成。任务执行情况："]
                for step in self._plan.steps:
                    result.append(f"-{step.desc}: {str(step.response)}")
                self.context_manager.set_result_message("\n".join(result))
                yield PlanAndExecuteCode.SUCCESS
        elif step.status == StepStatus.NOT_STARTED:
            function_name = self._plan.get_current_step().tool_name
            plugin = self._find_plugin_by_function_name(function_name)
            mcp = self._find_mcp_by_function_name(function_name)
            if not plugin and not mcp:
                raise ValueError(
                    f"Plugin or MCP not found: {self._plan.get_current_step().tool_name}"
                )
            self.execute_llm_module.context_manager.clear_all()
            arguments = await self._extract_arguments(function_name)
            if not arguments:
                self._plan.set_current_step_error("参数提取错误")
                self._plan.status = StatusCode.PLANNING
            else:
                for argument in arguments:
                    if mcp:
                        self.task_queue.add_task(
                            self.create_mcp_task(mcp, argument, message.runtime_data)
                        )
                    if plugin:
                        self.task_queue.add_task(
                            self.create_plugin_task(
                                plugin, argument, message.runtime_data
                            )
                        )
                step.status = StepStatus.IN_PROGRESS
                yield PlanAndExecuteCode.WAIT_FOR_TOOL_COMPLETION

    def __load_from_agent_state(self, agent_state):
        """从redis中恢复状态"""
        controller_state = agent_state.controller_state
        self.task_id = controller_state.task_id
        self._plan = controller_state.plan
        self.plan_llm_module.context_manager.engine.history.msgs = (
            controller_state.agent_contexts.get("plan_context", [])
        )
        self.execute_llm_module.context_manager.engine.history.msgs = (
            controller_state.agent_contexts.get("execute_context", [])
        )
        self.ask_user_module.context_manager.engine.history.msgs = (
            controller_state.agent_contexts.get("ask_user_context", [])
        )

    async def _extract_arguments(self, function_name):
        """从当前plan中提取plugin的参数"""
        content = str(self._plan)
        message = Message(
            MessageType.USER_INPUT,
            content=content,
            runtime_data={
                "prompt_info": {
                    "tool": self.tool_descriptions.get(function_name, {}).get(
                        "desc", ""
                    ),
                    "task": self._plan.get_current_step().desc,
                }
            },
        )
        self.execute_llm_module.context_manager.add_user_message(message.content)
        response = []
        async for res in self.execute_llm_module.stream(message):
            response.append(res.get("content", ""))
        response = "".join((s for s in response if s is not None))
        pattern = r"```json(.*?)```"
        match = re.search(pattern, response, re.DOTALL)
        if match:
            json_string = match.group(1).strip()
            try:
                arguments = json.loads(json_string)
            except json.JSONDecodeError:
                logger.warning("Fail to extract arguments.")
                arguments = {}
            arguments = arguments.get("arguments", [])
            return arguments
        return []

    def _find_workflow_by_function_name(self, function_name):
        """
        根据函数名查找对应的工作流

        Args:
            function_name: 函数名称

        Returns:
            workflow/None: 找到的工作流信息，如果未找到则返回None
        """
        return self.context_manager.get_workflow_context_by_name(function_name)

    def _find_plugin_by_function_name(self, function_name):
        """
        根据函数名查找对应的插件

        Args:
            function_name: 函数名称

        Returns:
            BasePlugin/Function/None: 找到的插件或函数，如果未找到则返回None
        """
        if not self.plugins:
            return None

        for plugin in self.plugins:
            # 直接检查插件名称是否匹配
            if plugin.name == function_name:
                return plugin
        return None

    def _find_mcp_by_function_name(self, function_name):
        """
        根据函数名查找对应的mcp

        Args:
            function_name: 函数名称

        Returns:
            BasePlugin/Function/None: 找到的插件或函数，如果未找到则返回None
        """
        if not self.mcps:
            return None

        for mcp in self.mcps:
            # 直接检查插件名称是否匹配
            if mcp.name == function_name:
                return mcp
        return None
