#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from collections.abc import AsyncGenerator
from copy import deepcopy

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.log.base import logger
from jiuwen.context.history import ConversationMessage
from jiuwen.controller.agent.control_mode.base_mode import BaseMode
from jiuwen.controller.common.config import StreamRequest
from jiuwen.controller.common.constants import FUNCTION_ROLE
from jiuwen.controller.common.enum import RetCode
from jiuwen.controller.common.message import Message
from jiuwen.controller.common.task import Task
from jiuwen.controller.utils.utils import MessageConverter


class ReactMode(BaseMode):
    def __init__(self, **kwargs):
        """初始化控制器"""
        # 使用配置类初始化
        super(ReactMode, self).__init__(**kwargs)

        # 初始化核心组件
        self.terminate = None
        self._init_components(self.config)
        # 初始化 Agent 输入变量（仅设置默认值）
        self.init_agent_input_variables()

    def __call__(self, **kwargs):
        """
        调用控制器处理用户查询的兼容性方法，实际调用invoke方法

        注意：推荐直接使用invoke方法，此方法仅为保持兼容性
        """
        return self.invoke(**kwargs)

    @staticmethod
    def _format_assistant_msg_for_intermediate_msg(
        assistant_msg: ConversationMessage,
    ) -> dict:
        result = dict(role=assistant_msg.role, content=assistant_msg.content)
        tool_calls, function_call_list = [], []
        function_call = assistant_msg.function_call
        if function_call:
            if isinstance(function_call, list):
                function_call_list.extend(function_call)
            if isinstance(function_call, dict):
                function_call_list = [function_call]

        for _ in function_call_list:
            item = deepcopy(_)
            tool_call_id = item.pop("tool_call_id", None)
            tool_call_item = dict(type="function", id="", function=item)
            if tool_call_id:
                tool_call_item.update(id=tool_call_id)
            tool_calls.append(tool_call_item)
        if tool_calls:
            result.update(dict(tool_calls=tool_calls))

        return result

    @staticmethod
    def _format_tool_msg_for_intermediate_msg(
        function_msg: ConversationMessage,
    ) -> dict:
        return dict(
            role=FUNCTION_ROLE,
            name=function_msg.name,
            content=function_msg.content,
            tool_call_id=function_msg.tool_call_id,
        )

    async def stream(self, **kwargs) -> AsyncGenerator:
        """流式调用控制器处理用户查询"""
        try:
            # 创建流式请求对象
            request = StreamRequest(**kwargs)

            # 创建初始用户输入消息
            runtime_data = {
                "task_id": self.task_id,
                "prompt_info": request.prompt_info,
                "plugins": request.plugins,
                "workflows": request.workflows,
                "contexts": request.contexts,
                "runtime_context": request.runtime_context,
                "multimodal_image": request.multimodal_image,
                "mcps": request.mcps,
                "trace_handlers": request.trace_handlers,
            }
            self.task_planner.plugins = request.plugins
            self.task_planner.mcps = request.mcps

            message = MessageConverter.create_user_input_message(
                request.query, runtime_data
            )

            # 运行流式任务循环 - 使用更合适的方法名
            yield_result = self._stream_task_loop(message)
            async for item in yield_result:
                yield self._process_stream_message(item)

        except Exception as e:
            import traceback

            error_msg = f"控制器流式执行失败: {str(e)}\n{traceback.format_exc()}"
            logger.error(error_msg, simple_log=f"控制器流式执行失败: {type(e)}")
            if isinstance(e, JiuWenBaseException):
                code, msg = e.error_code, e.message
            else:
                code, msg = -1, error_msg

            yield {
                "ret_code": RetCode.FAILED,
                "error": "controller stream execution failed",
                "result": {"code": code, "message": msg},
            }

    def init_agent_input_variables(self):
        """
        初始化 Agent 输入变量到 ContextManager
        - 从 plan_config.input_variables 获取变量定义
        - 将默认值存入 ContextManager

        注意：此方法仅在初始化时调用，设置默认值。
        后续的变量更新和记忆加载由 Agent 类直接处理。
        """

        input_variables = getattr(self.plan_config, "input_variables", None)
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
            for field in var["fields"]:
                fname = field.get("name")
                if fname:
                    obj_val[fname] = self._build_default_from_tree(field)
            return obj_val
        return var.get("default")

    async def _stream_task_loop(self, initial_message: Message) -> AsyncGenerator:
        """执行任务驱动循环的流式版本，直到得到最终结果

        Args:
            initial_message: 初始消息

        Returns:
            AsyncGenerator: 生成流式结果
        """
        current_message = initial_message
        iterations = 0

        while iterations < self.max_task_iterations:
            iterations += 1
            logger.info(
                f"Stream task iteration {iterations}:"
                f"Processing message type {current_message.message_type if current_message else 'UNKNOWN'}"
            )

            # 1. 任务识别 - 使用流式任务规划
            task_planner_results = []
            final_task_or_message = None
            final_ret_code = RetCode.FAILED
            multiple_function_call_list = []

            # 处理规划器的流式输出
            async for result in self.task_planner.stream_plan_task(current_message):
                # 收集所有规划结果
                task_planner_results.append(result)

                # 如果是中间结果（非Task或Message），转换后输出
                if not isinstance(result, (Task, Message)):
                    yield result
                elif isinstance(result, Task):
                    multiple_function_call_list.append(result)
                else:
                    # 记录最终的任务或消息
                    final_task_or_message = result

                if isinstance(result, RetCode):
                    final_ret_code = result

            # 如果返回的是消息而非任务，说明这是最终输出
            if (
                not isinstance(final_task_or_message, Task)
                and final_ret_code == RetCode.SUCCESS
            ):
                logger.info(
                    f"Stream task loop completed with final message: {final_task_or_message}",
                    simple_log="Stream task loop completed with final message",
                )
                intermediate_msg = self._create_intermediate_message()
                if intermediate_msg is not None:
                    yield intermediate_msg
                yield RetCode.SUCCESS
                return

            # 2. 任务分发
            for final_task in multiple_function_call_list:
                task_execution = self.task_dispatcher.dispatch(final_task)

                # 3. 使用流式方法执行任务并处理结果
                async for result in self.process_task_execution_streaming_message(
                    task_execution
                ):
                    yield result

                # 使用最后一个消息作为下一轮循环的输入
                logger.info(
                    f"Stream task loop continue with current message: {task_execution.last_message}",
                    simple_log="Stream task loop continue with current message",
                )
                current_message = task_execution.last_message

            # 检查执行后的状态
            if self.terminate:
                logger.info("Stream task loop terminate")
                intermediate_msg = self._create_intermediate_message()
                if intermediate_msg is not None:
                    yield intermediate_msg
                yield RetCode.SUCCESS
                return

        # 如果达到最大迭代次数，返回超时错误
        yield {
            "ret_code": RetCode.FAILED,
            "result": "任务处理超出最大迭代次数，可能存在循环依赖",
        }

    def _create_intermediate_message(self):
        intermediate_message = []

        all_assistant_msg_and_tool_msg = (
            self.context_manager.get_all_assistant_msg_and_tool_msg()
        )
        for msg in all_assistant_msg_and_tool_msg:
            if msg.role == "assistant":
                intermediate_message.append(
                    self._format_assistant_msg_for_intermediate_msg(msg)
                )
            if msg.role == FUNCTION_ROLE:
                intermediate_message.append(
                    self._format_tool_msg_for_intermediate_msg(msg)
                )

        if intermediate_message:
            logger.info(
                f"After create_intermediate_message, intermediate message: {intermediate_message}",
                simple_log="intermediate_message create successfully",
            )
            return {"intermediate_message": intermediate_message}

        return None
