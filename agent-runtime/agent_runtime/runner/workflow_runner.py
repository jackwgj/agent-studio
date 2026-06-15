"""WorkflowRunner — 编排存储读取 + ir_runner 执行工作流."""

from __future__ import annotations

import os
import time
from enum import Enum
from typing import AsyncGenerator

from agent_runtime.common.ir_exceptions import IRBuildException
from agent_runtime.common.ir_interfaces import ModelConfigProvider
from agent_runtime.common.model_providers import (
    EnvVarModelConfigProvider,
    IRModelConfigProvider,
)
from agent_runtime.common.redis_manager import ExecutionIdStore
from agent_runtime.runner.context import create_conversation_context
from agent_runtime.runner.workflow_stream_data_wrapper import WorkflowStreamDataWrapper
from agent_runtime.schemas.orchestration_mgr import (
    ComponentDebugRequest,
    ExecutionRequest,
)
from jiuwen.serve.controllers.execution.ir_converter import IRConverter
from jiuwen.serve.controllers.execution.open_utils import async_ir_load
from openjiuwen.core.common.exception.errors import ExecutionError, Termination
from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.session.checkpointer.checkpointer import CheckpointerFactory
from openjiuwen.core.session.interaction.interactive_input import InteractiveInput
from openjiuwen.core.session.stream import BaseStreamMode
from openjiuwen.core.workflow import create_workflow_session

GENERAL_ERROR = 101040


class ModelConfigStrategy(Enum):
    """模型配置来源策略"""

    ENV = "env"
    IR = "ir"


class WorkflowRunner:
    """编排对象存储 + ir_runner 执行 IR 工作流的 Runner"""

    def __init__(
        self,
        api_key: str | None = None,
        api_base: str | None = None,
        model_strategy: ModelConfigStrategy = ModelConfigStrategy.IR,
    ):
        self._api_key = api_key or os.environ.get("API_KEY", "")
        if not self._api_key:
            workflow_logger.warning(
                "API_KEY is not configured; workflow execution will fail"
            )
        self._api_base = api_base or os.environ.get(
            "API_BASE", "https://api.deepseek.com"
        )
        self._model_strategy = model_strategy
        self._ir_converter = IRConverter()

    def _create_model_provider(self) -> ModelConfigProvider:
        match self._model_strategy:
            case ModelConfigStrategy.ENV:
                return EnvVarModelConfigProvider()
            case ModelConfigStrategy.IR:
                return IRModelConfigProvider()

    async def _is_session_interrupted(self, session_id: str) -> bool:
        """检查指定 session 是否存在已保存的 checkpoint（即处于中断状态）"""
        checkpointer = CheckpointerFactory.get_checkpointer()
        return await checkpointer.session_exists(session_id)

    async def run_streaming(
        self,
        req: ExecutionRequest,
        execution_id: str | None = None,
    ) -> AsyncGenerator[dict, None]:
        """执行 IR 工作流并流式返回 OutputSchema

        Args:
            req: 执行请求
            execution_id: 请求级执行 ID，用于 SSE 事件追踪；若未提供则使用 conversation_id
        """
        perf_start = time.time()

        # 1. 确定 session_id 和 execution_id
        session_id = req.conversation_id
        exec_id = execution_id or session_id

        # 2. 使用缓存的 IR（如果存在）或从存储读取
        ir_path = req.ir_path
        t_load_start = time.time()
        try:
            ir_json = await async_ir_load(ir_path)
            workflow_logger.info(
                f"[PERF] IR load (cache+storage): {(time.time() - t_load_start) * 1000:.1f}ms"
            )
        except Exception as e:
            workflow_logger.error(
                f"Failed to load IR from {ir_path}: {e}", exc_info=True
            )
            yield {
                "event": "error",
                "data": {"response": "Failed to load workflow configuration"},
                "executionId": exec_id,
                "index": 0,
                "createdTime": int(time.time()),
            }
            return

        # 3. 构建工作流（纯 dict → workflow，无存储）, 构建node_id与node_name映射
        t_node_map = time.time()
        node_defs = await IRConverter.extract_node_defs(ir_json)
        workflow_logger.info(
            f"[PERF] Node ID mapping build: {(time.time() - t_node_map) * 1000:.1f}ms"
        )

        t_convert_start = time.time()
        try:
            workflow = await self._ir_converter.async_ir_to_workflow(ir_json)
            workflow_logger.info(
                f"[PERF] IR->Workflow conversion total: {(time.time() - t_convert_start) * 1000:.1f}ms"
            )
        except IRBuildException as e:
            workflow_logger.error(f"Failed to build workflow: {e}", exc_info=True)
            yield {
                "event": "error",
                "data": {"response": "Failed to build workflow"},
                "executionId": exec_id,
                "index": 0,
                "createdTime": int(time.time()),
            }
            return

        # 3. 创建对话上下文
        t_context = time.time()
        context = create_conversation_context(
            context_id=req.ir_path,
            session_id=req.conversation_id,
            history=req.params.conversation_history,
        )
        workflow_logger.info(
            f"[PERF] ConversationContext creation: {(time.time() - t_context) * 1000:.1f}ms"
        )

        # 4. 创建 session，使用固定的 session_id 以支持中断恢复
        t_session = time.time()
        session = create_workflow_session(session_id=session_id)
        workflow_logger.info(
            f"[PERF] WorkflowSession creation: {(time.time() - t_session) * 1000:.1f}ms"
        )

        # 5. 检查会话是否处于中断状态，构建 inputs
        t_checkpoint = time.time()
        is_interrupted = await self._is_session_interrupted(session_id)
        workflow_logger.info(
            f"[PERF] Checkpoint check (session_exists): {(time.time() - t_checkpoint) * 1000:.1f}ms"
        )

        workflow_id = ir_json.get("workflowId", "")

        t_inputs_build = time.time()
        if is_interrupted:
            # 恢复执行：使用 InteractiveInput(raw_inputs) 恢复 checkpoint
            # 从 Redis 中恢复上一次保存的 exec_id
            t_redis_get = time.time()
            saved_exec_id = await ExecutionIdStore.get(workflow_id, session_id)
            workflow_logger.info(
                f"[PERF] Redis GET (restore exec_id): {(time.time() - t_redis_get) * 1000:.1f}ms"
            )
            if saved_exec_id:
                exec_id = saved_exec_id
            # resume_input 为本次恢复的用户输入
            resume_input = req.resume_input or req.query
            inputs = InteractiveInput(raw_inputs=resume_input)
            is_resuming = True
        else:
            # 首次执行：使用普通 query 输入
            inputs = {
                "query": req.query or "",
                **self._build_global_state_params(req.params.model_dump(), node_defs),
            }
            # 保存 exec_id 到 Redis，以便中断恢复时使用
            t_redis_save = time.time()
            await ExecutionIdStore.save(workflow_id, session_id, exec_id)
            workflow_logger.info(
                f"[PERF] Redis SAVE (store exec_id): {(time.time() - t_redis_save) * 1000:.1f}ms"
            )
            is_resuming = False
        workflow_logger.info(
            f"[PERF] Inputs build + Redis ops: {(time.time() - t_inputs_build) * 1000:.1f}ms"
        )

        workflow_logger.info(
            f"[PERF] === PRE-EXEC TOTAL: {(time.time() - perf_start) * 1000:.1f}ms ==="
        )

        # 6. 发送工作流开始帧（首次执行时）
        yield {
            "event": "start",
            "data": {},
            "index": 0,
            "executionId": exec_id,
            "createdTime": int(time.time()),
        }

        if not is_resuming:
            yield {
                "event": "workflow_start",
                "data": {},
                "index": 0,
                "executionId": exec_id,
                "createdTime": int(time.time()),
            }

        # 7. 执行工作流
        t_stream_start = time.time()
        workflow_wrapper = None
        try:
            is_debug = req.params.is_debug
            stream_modes = [BaseStreamMode.OUTPUT, BaseStreamMode.CUSTOM]
            if is_debug:
                stream_modes.append(BaseStreamMode.TRACE)

            workflow_wrapper = WorkflowStreamDataWrapper(
                execution_id=exec_id,
                is_debug=is_debug,
                conversation_id=req.conversation_id,
                node_id_to_name=node_defs,
                history=req.params.conversation_history,
                query=req.query or "",
                is_resuming=is_resuming,
            )

            t_compile_invoke_start = time.time()
            chunk_count = 0
            async for chunk in workflow.stream(
                inputs, session, context=context, stream_modes=stream_modes
            ):
                if chunk_count == 0:
                    workflow_logger.info(
                        f"[PERF] First chunk latency (compile+invoke start): {(time.time() - t_compile_invoke_start) * 1000:.1f}ms"
                    )
                chunk_count += 1
                for event in workflow_wrapper.wrap_stream_data(
                    chunk, is_resuming=is_resuming
                ):
                    yield event
            workflow_logger.info(
                f"[PERF] Workflow.stream() total: {(time.time() - t_stream_start) * 1000:.1f}ms"
            )
            workflow_logger.info(f"[PERF] Total chunks yielded: {chunk_count}")
        except Termination:
            raise
        except ExecutionError as e:
            last_node = workflow_wrapper.get_last_node()
            node_id = last_node.get("node_id", "")
            node_type = last_node.get("node_type", "")
            node_name = _resolve_node_name(node_defs, workflow_id, node_id)
            error_code = e.code
            error_msg = _format_error_message(error_code, e.message)
            yield {
                "event": "error",
                "data": {
                    "code": error_code,
                    "message": error_msg,
                    "node_id": node_id,
                    "node_name": node_name,
                    "node_type": node_type,
                    "workflow_id": workflow_id,
                    "workflow_name": ir_json.get("workflowName", ""),
                },
                "executionId": exec_id,
                "index": 0,
                "createdTime": int(time.time()),
            }
            yield {
                "event": "done",
                "data": {
                    "node_id": node_id,
                    "node_name": node_name,
                    "node_type": node_type,
                },
                "executionId": exec_id,
                "index": 0,
                "createdTime": int(time.time()),
            }
        except Exception as e:
            workflow_logger.error(f"Workflow execution failed: {e}, type={type(e).__name__}", exc_info=True)
            last_node = workflow_wrapper.get_last_node() if workflow_wrapper else {}
            node_id = last_node.get("node_id", "")
            node_type = last_node.get("node_type", "")
            node_name = _resolve_node_name(node_defs, workflow_id, node_id)
            error_code = GENERAL_ERROR
            error_msg = _format_error_message(error_code, "Workflow execution failed")
            yield {
                "event": "error",
                "data": {
                    "code": error_code,
                    "message": error_msg,
                    "node_id": node_id,
                    "node_name": node_name,
                    "node_type": node_type,
                    "workflow_id": workflow_id,
                    "workflow_name": ir_json.get("workflowName", ""),
                },
                "executionId": exec_id,
                "index": 0,
                "createdTime": int(time.time()),
            }

    async def run_blocking(self, req: ExecutionRequest) -> str:
        """执行 IR 工作流并返回完整结果"""
        result_parts = []
        async for chunk in self.run_streaming(req):
            event = chunk.get("event", "")
            data = chunk.get("data", {})
            if event == "message":
                text = data.get("answer", "") or data.get("output", "")
                if text:
                    result_parts.append(str(text))
            elif event == "done":
                text = data.get("answer", "") or data.get("output", "")
                if text:
                    result_parts.append(str(text))
        return "".join(result_parts)

    async def run_debug_streaming(
        self,
        req: ComponentDebugRequest,
        component_id: str,
        execution_id: str | None = None,
    ) -> AsyncGenerator[dict, None]:
        """单组件调试：直接执行 IR 中指定组件，绕过 Workflow 引擎。

        使用 IRConverter.create_single_component() + SingleComponentDebugWrapper
        直接调用组件的 on_invoke/on_stream，不走 workflow.stream()。
        """
        import uuid
        from jiuwen.extension.wrapper.single_component_debug_wrapper import (
            SingleComponentDebugWrapper,
        )
        from agent_runtime.runner.debug_formatter import DebugStreamFormatter

        exec_id = execution_id or str(uuid.uuid4())

        # 1. 从存储读取 IR
        ir_path = req.ir_path
        try:
            ir_json = await async_ir_load(ir_path)
        except Exception as e:
            workflow_logger.error(
                f"Failed to load IR from {ir_path}: {e}", exc_info=True
            )
            yield {
                "event": "error",
                "data": {"response": "Failed to load workflow configuration"},
                "executionId": exec_id,
                "index": 0,
                "createdTime": int(time.time() * 1000),
            }
            return

        # 2. 创建单组件实例
        try:
            component_info = await IRConverter.create_single_component(
                ir_json, component_id
            )
        except Exception as e:
            workflow_logger.error(f"Failed to create component: {e}", exc_info=True)
            yield {
                "event": "error",
                "data": {"response": f"Failed to create component: {e}"},
                "executionId": exec_id,
                "index": 0,
                "createdTime": int(time.time() * 1000),
            }
            return

        # 3. 创建 DebugStreamFormatter
        agent_id = ir_json.get("workflowId", "")
        formatter = DebugStreamFormatter(
            execution_id=exec_id,
            component_id=component_id,
            component_name=component_info.node_name,
            component_type=component_info.node_type,
            agent_id=agent_id,
            inputs=req.inputs,
        )

        # 4. 执行组件并格式化输出
        # session_id 用于中断恢复时保存/恢复状态（通过 conversationId 关联）
        wrapper = SingleComponentDebugWrapper(
            component_info=component_info,
            execution_id=exec_id,
            session_id=req.conversation_id,
        )

        try:
            async for stream_data in wrapper.astream(
                inputs=req.inputs, execution_id=exec_id
            ):
                for event in formatter.format(stream_data):
                    yield event
        except Exception as e:
            workflow_logger.error(
                f"Debug component execution failed: {e}", exc_info=True
            )
            yield {
                "event": "error",
                "data": {"response": f"Debug component execution failed: {e}"},
                "executionId": exec_id,
                "index": 0,
                "createdTime": int(time.time() * 1000),
            }
            return

        # 5. 补发遗漏事件（如流正常结束但未收到 FINISH）
        for event in formatter.finalize():
            yield event

    def _build_global_state_params(self, params: dict, node_defs: dict) -> dict:
        """构建需要通过 inputs → commit_user_inputs() 写入 global_state 的参数。

        这些参数同时存在于 envs（由 _build_envs 生成），但 envs 不被 checkpoint 保存。
        通过 commit_user_inputs() 写入 global_state 后，checkpoint 会保存这些值，
        中断恢复时自动恢复，组件通过 get_workflow_param() 读取时拿到的是最新值。
        """
        result = {}

        if "global_variables" in params:
            excluded_keys = {"conversationId", "sys", "conversationHistory"}
            result["_request"] = {
                k: v
                for k, v in params["global_variables"].items()
                if k not in excluded_keys
            }
            result["global_variables"] = params["global_variables"]

        runtime_keys = [
            "memory_variables",
            "CONTROLLER_MODE_JUMP",
            "CONTROLLER_MODE_SWITCH",
            "plugin_configs",
            "llm_extra_configs",
            "app_id",
            "global_intents",
            "environment_variables",
            "mem_map",
            "enable_memory_retrieve",
        ]
        for key in runtime_keys:
            if key in params:
                result[key] = params[key]

        # IR 中使用 ${_env.xxx} 引用环境变量，openjiuwen 的 get_by_schema 按 dot-path 从 io_state 取值，
        # 因此需要以 "_env" 为键写入
        if "environment_variables" in params:
            result["_env"] = params["environment_variables"]

        if node_defs:
            result["__node_defs__"] = node_defs

        return result


def _resolve_node_name(node_defs: dict, workflow_id: str, node_id: str) -> str:
    if not node_defs or not node_id:
        return node_id
    wf_defs = node_defs.get(workflow_id, {})
    node_info = wf_defs.get(node_id, {})
    return node_info.get("node_name", node_id) if isinstance(node_info, dict) else node_id


def _format_error_message(code: int, raw_message: str) -> str:
    from jiuwen.orchestration.flow.constant import WORKFLOW_UNIFIED_ERROR_INFORMATION_UNSAFE
    return WORKFLOW_UNIFIED_ERROR_INFORMATION_UNSAFE.format(code, raw_message)
