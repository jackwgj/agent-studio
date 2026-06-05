#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import json
import uuid
from collections.abc import AsyncGenerator
from typing import Union, Dict

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.controller.common.enum import OutputResultType, RetCode
from jiuwen.controller.common.message import Message
from jiuwen.controller.common.message_type import MessageType
from jiuwen.controller.common.task import Task
from jiuwen.controller.common.task_type import TaskType, TaskSubType
from jiuwen.controller.context_manager.workflow_context import WorkflowContext
from jiuwen.controller.task_planner.planning_modules.llm_module import LLMModule
from jiuwen.controller.task_planner.task_planner import TaskPlanner
from jiuwen.controller.task_planner.task_queue import TaskQueue
from jiuwen.planner.common.plan_constants import SPLITER, TimerInfo
from jiuwen.planner.common.result_output_callback import ResultOutputStreamCallback

NAME = "name"


class ReactPlanner(TaskPlanner):
    def __init__(self, plan_config, llm, plugins, workflows, context_manager, task_id):
        super().__init__(plan_config, llm, plugins, workflows, context_manager, task_id)
        self.task_queue = TaskQueue()
        self.llm_module = LLMModule(plan_config, llm, context_manager)
        self.result_output_callback = ResultOutputStreamCallback()

    @staticmethod
    def replace_func_args(function_call: Dict, args: Dict):
        """replace_func_call_args"""
        if not isinstance(function_call, dict):
            logger.error("function call is not type dict")
            return
        arguments = function_call.get("arguments", dict())

        # 如果是字符串，尝试解析为字典
        if isinstance(arguments, str):
            try:
                arguments = json.loads(arguments)
            except json.JSONDecodeError:
                logger.error("parse function call arguments failed")
                return

        # 如果是字典，进行替换
        if isinstance(arguments, dict):
            for key, value in args.items():
                arguments[key] = value

        function_call["arguments"] = json.dumps(arguments, ensure_ascii=False)

    @staticmethod
    def create_plugin_task(plugin, task_context):
        """创建插件任务"""
        """
        创建插件任务对象

        Args:
            plugin: 插件对象
            function_name: 函数名称
            function_call: 函数调用信息
            task_id: 任务ID

        Returns:
            Task: 创建的插件任务对象
        """
        plugin_name = plugin.name
        time_dict = task_context["time_dict"]
        message = task_context["message"]
        function_name = task_context["function_name"]
        function_call = task_context["function_call"]
        task_id = task_context["task_id"]
        logger.info(f"Creating plugin task for: {function_name}, plugin: {plugin_name}")

        # 直接创建Task对象
        task_unique_id = f"{task_id}_plugin_{plugin_name}_{uuid.uuid4().hex[:8]}"

        # 准备输入数据
        input_data = {
            "plugin_name": plugin_name,
            "function_name": function_name,
            "function_call": function_call,
            "arguments": function_call.get("arguments", {}),
            "plugin": plugin,
            "runtime_data": message.runtime_data or None,
            "time_dict": time_dict,
        }

        return Task(
            id=task_unique_id,
            task_type=TaskType.PLUGIN_START,
            input_data=input_data,
        )

    @staticmethod
    def create_mcp_task(mcp, task_context):
        """创建MCP任务"""
        """
        创建插件任务对象

        Args:
            mcp: mcp对象
            function_name: 函数名称
            function_call: 函数调用信息
            task_id: 任务ID

        Returns:
            Task: 创建的mcp任务对象
        """
        mcp_name = mcp.name
        time_dict = task_context["time_dict"]
        message = task_context["message"]
        function_name = task_context["function_name"]
        function_call = task_context["function_call"]
        task_id = task_context["task_id"]
        logger.info(f"Creating mcp task for: {function_name}, mcp: {mcp_name}")

        # 直接创建Task对象
        task_unique_id = f"{task_id}_mcp_{mcp_name}_{uuid.uuid4().hex[:8]}"

        # 准备输入数据
        input_data = {
            "mcp_name": mcp_name,
            "function_name": function_name,
            "function_call": function_call,
            "arguments": function_call.get("arguments", {}),
            "mcp": mcp,
            "runtime_data": message.runtime_data or None,
            "time_dict": time_dict,
        }

        return Task(
            id=task_unique_id,
            task_type=TaskType.MCP_START,
            input_data=input_data,
        )

    @staticmethod
    def create_workflow_task(workflow_context: WorkflowContext, task_context):
        """
        创建工作流任务对象

        Args:
            workflow: 工作流对象
            function_name: 函数名称
            function_call: 函数调用信息
            task_id: 任务ID

        Returns:
            Task: 创建的工作流任务对象
        """
        time_dict = task_context["time_dict"]
        message = task_context["message"]
        function_call = task_context["function_call"]
        task_id = task_context["task_id"]
        logger.info(
            f"Creating workflow task for workflow: {workflow_context.workflow_id}"
        )

        # 直接创建Task对象
        task_unique_id = (
            f"{task_id}_workflow_{workflow_context.workflow_id}_{uuid.uuid4().hex[:8]}"
        )

        # 准备输入数据
        input_data = {
            "function_call": function_call,
            "runtime_data": message.runtime_data,
            "time_dict": time_dict,
        }

        return Task(
            id=task_unique_id,
            task_type=TaskType.WORKFLOW_START,
            sub_task_type=TaskSubType.STARTED_FROM_REACT,
            input_data=input_data,
            workflow_context=workflow_context,
        )

    @staticmethod
    async def _format_yield_from_llm_output_with_timer(result, time_dict: dict):
        async for item in result:
            if isinstance(item, dict) and "time_consumption" in item:
                time_dict.update(item.get("time_consumption"))
            else:
                yield item

    def get_state(self):
        """获取当前Planner状态（用于向后兼容）

        Returns:
            ControllerState: 控制器状态对象
        """
        return

    def load_state(self, state):
        """加载当前ControllerPlanner的状态

        Args:
            state:

        Returns:

        """
        return

    def plan(self, message: Message) -> Union[Message, Task, None]:
        pass

    async def stream(self, message: Message) -> AsyncGenerator:
        """
        处理消息并生成响应流

        Args:
            message: 输入消息对象

        Yields:
            流式处理的中间结果，包括内容块和Task对象
        """
        if message is None:
            logger.error("stream input message is None")
            raise JiuWenBaseException(
                error_code=StatusCode.PLANNER_CONFIG_CHECK_FAILED.code,
                message=StatusCode.PLANNER_CONFIG_CHECK_FAILED.errmsg
                + "| stream input message is None",
            )

        # 获取初始参数
        files = None
        if message.message_type == MessageType.USER_INPUT:
            runtime_context = message.runtime_data.get("runtime_context")
            if runtime_context and hasattr(runtime_context, "agent_workflow_context"):
                workflow_req_params = runtime_context.agent_workflow_context.get(
                    "workflow_req_params", {}
                )
                files = workflow_req_params.get("files")
            self.context_manager.add_user_message(message.content, files=files)
            query = message.content
        else:
            query = self.context_manager.get_latest_user_content()
        task_id = self.task_id
        time_dict = {}
        if (
            message.message_type
            in [MessageType.PLUGIN_COMPLETION, MessageType.MCP_COMPLETION]
            or not self.context_manager.get_latest_intent()
        ):
            async for stream_message in self._execute_and_process_llm_stream(
                message, time_dict
            ):
                yield stream_message

        final_function_call = self.context_manager.get_latest_assistant_function_call()
        # 任务队列执行任务是先进后出的栈顺序，因此react模式的工具调用也应倒序生成Task
        final_function_call_list = list(
            reversed(
                self.context_manager.get_latest_assistant_function_call_list() or []
            )
        )
        final_content = self.context_manager.get_latest_assistant_content()

        # 处理最终结果
        if not final_function_call_list:
            if final_function_call:
                for task in self._create_exec_single_function_call_tasks(
                    final_function_call, message, query, task_id, time_dict
                ):
                    yield task
            else:
                # 处理纯内容响应
                for item_message in self._handle_content_response(
                    final_content, time_dict
                ):
                    yield item_message
        else:
            logger.info(
                f"In ReAct, before invoke LLM generated function call list = {final_function_call_list}",
                simple_log="In ReAct, before invoke LLM generated function call list",
            )
            for function_call in final_function_call_list:
                for task in self._create_exec_single_function_call_tasks(
                    function_call, message, query, task_id, time_dict
                ):
                    yield task

        if self.context_manager.get_result_message():
            yield RetCode.SUCCESS

    def _find_workflow_by_function_name(self, function_name):
        """
        根据函数名查找对应的工作流

        Args:
            function_name: 函数名称

        Returns:
            workflow/None: 找到的工作流信息，如果未找到则返回None
        """
        return self.context_manager.get_workflow_context_by_name_and_type(function_name)

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
        根据函数名查找对应的MCP

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

    async def _execute_and_process_llm_stream(self, message, time_dict):
        """
        处理LLM流式输出并收集最终结果到传入的字典中

        Args:
            message: 输入消息
            final_result: 用于存储最终结果的字典引用

        Yields:
            每个流式输出的数据块
        """
        for result in self.result_output_callback.on_llm_output_stream_started():
            yield result
        yield_res = self.llm_module.stream(message)
        async for result in self._format_yield_from_llm_output_with_timer(
            yield_res, time_dict
        ):
            yield result

    def _handle_content_response(self, content, time_dict):
        """处理纯内容响应"""
        yield from self.result_output_callback.on_llm_output_stream_end(
            OutputResultType.LLM_CONTENT
        )
        self.context_manager.set_result_message(
            f"{content}{SPLITER}{json.dumps(time_dict, ensure_ascii=False)}"
        )

    def _create_handle_non_exist_function_call_tasks(self, function_call, time_dict):
        """处理不存在的函数调用"""
        self.context_manager.add_function_message(
            name=function_call.get(NAME),
            intent=function_call.get(NAME),
            content="大模型生成不存在的工具，无法执行",
            tool_call_id=function_call.get("tool_call_id", ""),
        )
        yield from self.result_output_callback.on_function_call_generated(
            function_call, time_info=TimerInfo(**time_dict)
        )
        yield from self.result_output_callback.on_tool_executed(
            "无有效的插件名",
            function_call.get(NAME),
            "大模型生成不存在的工具，无法执行",
            False,
            TimerInfo(total_latency=0.0),
        )

    def _create_exec_single_function_call_tasks(
        self,
        function_call: Dict,
        message: Message,
        query: str,
        task_id: str,
        time_dict: Dict,
    ):
        # 处理函数调用响应
        function_call[NAME] = function_call[NAME].strip()
        function_name = function_call.get(NAME)
        # 判断函数类型并处理
        plugin = self._find_plugin_by_function_name(function_name)
        mcp = self._find_mcp_by_function_name(function_name)
        workflow_context = self._find_workflow_by_function_name(function_name)
        yield from self.result_output_callback.on_llm_output_stream_end(
            OutputResultType.LLM_FUNCTION_CALL
        )
        if plugin:
            # 创建任务上下文对象，减少方法参数数量
            task_context = {
                "time_dict": time_dict,
                "message": message,
                "function_name": function_name,
                "function_call": function_call,
                "task_id": task_id,
                "query": query,
            }
            yield self.create_plugin_task(plugin, task_context)
        elif workflow_context:
            # 创建任务上下文对象，减少方法参数数量
            task_context = {
                "time_dict": time_dict,
                "message": message,
                "function_name": function_name,
                "function_call": function_call,
                "task_id": task_id,
                "query": query,
            }
            yield self.create_workflow_task(workflow_context, task_context)
        elif mcp:
            # 创建任务上下文对象，减少方法参数数量
            task_context = {
                "time_dict": time_dict,
                "message": message,
                "function_name": function_name,
                "function_call": function_call,
                "task_id": task_id,
                "query": query,
            }
            yield self.create_mcp_task(mcp, task_context)
        else:
            yield from self._create_handle_non_exist_function_call_tasks(
                function_call, time_dict
            )
