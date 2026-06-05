# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
"""单组件调测包装器，用于在新框架（openjiuwen）下实现旧框架的单组件调试能力。"""

import logging
from dataclasses import dataclass
from typing import Any, AsyncGenerator

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.orchestration.flow.enum import ExecutionStatus, StreamDataMsg
from jiuwen.orchestration.flow.stream.base import StreamCode, StreamData
from openjiuwen.core.common.constants.constant import INPUTS_KEY, CONFIG_KEY
from openjiuwen.core.graph.pregel import GraphInterrupt
from openjiuwen.core.session.checkpointer.checkpointer import CheckpointerFactory
from openjiuwen.core.session.internal.workflow import WorkflowSession, NodeSession

_USER_FIELDS = "userFields"
_SYSTEM_FIELDS = "systemFields"


@dataclass
class SingleComponentInfo:
    """单组件调测的组件信息。"""

    component: Any  # ComponentComposable 实例
    node_id: str
    node_type: str
    configs: dict  # 原始 IR configs
    inputs_schema: dict  # _convert_schema 生成
    node_name: str = ""  # 节点名称（来自 IR name 字段）


class SingleComponentDebugWrapper:
    """单组件调测包装器。

    职责：
    - 直接调用 executable.on_invoke/on_stream，绕过 Vertex
    - 输入预处理复刻旧框架 bpmn_workflow._pre_process_single_component_inputs
    - 输出转换为 StreamData，复刻旧框架 bpmn_workflow._arun 单组件分支
    - Duck-type 兼容 _format_component_output 尾部清理
    """

    _LLM_TYPES = frozenset(
        {
            "jiuwen.llm",
            "jiuwen.llm_chain",
            "jiuwen.llmChain",
            "jiuwen.LLMComponent",
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

    def __init__(
        self,
        component_info: SingleComponentInfo,
        execution_id: str,
        session_id: str = None,
    ):
        self._component = component_info.component
        self._node_id = component_info.node_id
        self._node_type = component_info.node_type
        self._component_name = component_info.node_name
        self._configs = component_info.configs
        self._inputs_schema = component_info.inputs_schema
        self._execution_id = execution_id
        self._session_id = session_id or execution_id

    # ------------------------------------------------------------------
    # 公开接口
    # ------------------------------------------------------------------

    async def astream(
        self, inputs: dict, execution_id: str
    ) -> AsyncGenerator[StreamData, None]:
        """执行单组件并异步生成 StreamData。

        输出序列与旧框架 bpmn_workflow._arun() 第 610-644 行一致：
        - LLM:  1..N PARTIAL_CONTENT -> 1 MESSAGE_END -> 1 FINISH
        - 流式 API (streaming=True): 1..N PARTIAL_CONTENT
        - 非流式: 1 FINISH
        - 等待用户输入（GraphInterrupt）: 返回已产生的问题内容和等待用户输入状态

        支持中断恢复：
        - 使用 _session_id 作为 key 通过 CheckpointerFactory 保存/恢复状态
        - 中断时保存状态到 checkpointer，恢复时从 checkpointer 读取
        """
        try:
            processed = self._preprocess_inputs(inputs)
            session = self._create_session()

            # 检查是否处于中断状态，若是则恢复保存的状态
            checkpointer = CheckpointerFactory.get_checkpointer()
            is_interrupted = (
                await checkpointer.session_exists(self._session_id)
                if checkpointer
                else False
            )
            if is_interrupted:
                # 从 checkpointer 恢复状态
                restored_state = await checkpointer.load_session(self._session_id)
                if restored_state and isinstance(restored_state, dict):
                    session.update_state(restored_state)

            # 将预处理后的 inputs 写入 session，使组件可通过引用解析访问
            # 类似旧框架 _pre_process_single_component_inputs 填充 runtime_context
            session.state().commit_user_inputs(processed)

            executable = self._component.to_executable()
            adapted = self._adapt_inputs_format(executable, processed)

            if self._should_stream():
                async for sd in self._execute_streaming(
                    executable, adapted, session, execution_id
                ):
                    yield sd
            else:
                async for sd in self._execute_invoke(
                    executable, adapted, session, execution_id
                ):
                    yield sd
        except GraphInterrupt as e:
            # GraphInterrupt 表示组件需要用户输入（如提问器等待用户回复）
            # 单节点调试模式下无法真正等待用户输入，但可以返回已产生的问题内容
            # 中断时保存状态到 checkpointer
            await self._save_state_to_checkpointer(session)
            async for sd in self._handle_graph_interrupt(e, session, execution_id):
                yield sd
        except Exception as e:
            yield self._build_error_stream_data(e, execution_id)

    # ------------------------------------------------------------------
    # Duck-type 兼容 _format_component_output 尾部清理
    # ------------------------------------------------------------------

    def get_workflow_execute_status(self):
        """返回 COMPLETED，使 async_update_state_of_workflow 走 delete_state 分支。"""
        return ExecutionStatus.END

    def get_state(self):
        return None

    async def async_clean_up(self):
        pass

    # ------------------------------------------------------------------
    # 输入预处理 — 复刻 bpmn_workflow._pre_process_single_component_inputs
    # ------------------------------------------------------------------

    def _preprocess_inputs(self, inputs: dict) -> dict:
        """精确复刻 bpmn_workflow:1201-1229 + 1119-1125 的逻辑。"""
        # 确定参数字段 (line 1120-1125)
        if self._node_type == "jiuwen.questioner":
            inputs["__single_debug_recovery__"] = True
            return inputs
        elif self._node_type == "jiuwen.intentDetection":
            return inputs
        elif self._node_type in ("jiuwen.subWorkflow", "jiuwen.workflowComposite"):
            param_field = _SYSTEM_FIELDS
        else:
            param_field = _USER_FIELDS

        invokable_inputs = self._configs.get(param_field, {})
        request_inputs = inputs
        processed_inputs = {}
        input_definitions = self._configs.get(param_field, {}).get("inputs", [])

        for item in input_definitions:
            key = item["id"]
            source_type = item.get("sourceType", "")
            if key in request_inputs:
                processed_inputs[key] = request_inputs[key]
            elif source_type == "input":
                processed_inputs[key] = invokable_inputs.get(key)
            else:
                raise JiuWenBaseException(
                    error_code=StatusCode.COMPONENT_STEP_DEBUG_ERROR.code,
                    message=StatusCode.COMPONENT_STEP_DEBUG_ERROR.errmsg.format(
                        reason=f"Input key '{key}' with sourceType 'ref' is missing in the request inputs."
                    ),
                )

        return {param_field: processed_inputs}

    # ------------------------------------------------------------------
    # 输入格式适配 — graph_invoker 组件需要 {INPUTS_KEY: ..., CONFIG_KEY: ...}
    # ------------------------------------------------------------------

    def _adapt_inputs_format(self, executable: Any, processed_inputs: dict) -> dict:
        """为 graph_invoker() 组件包装输入格式。

        正常流程中 Vertex 在 is_subgraph=True 时自动包装，我们绕过 Vertex 需自行处理。
        """
        if getattr(executable, "graph_invoker", bool)():
            return {INPUTS_KEY: processed_inputs, CONFIG_KEY: self._configs}
        return processed_inputs

    # ------------------------------------------------------------------
    # Session 构建 — WorkflowSession + NodeSession
    # ------------------------------------------------------------------

    def _create_session(self) -> NodeSession:
        """参照 openjiuwen _workflow.py:execute_single_component() 的模式。

        WorkflowSession(parent=None) 初始化:
        - _config = Config()         -> get_env() 返回 None（安全）
        - _tracer = None              -> trace() 变 no-op（安全）
        - _state = InMemoryState()    -> 所有 state 操作可用
        - _actor_manager = None       -> write_stream() 变 no-op（安全）
        - _stream_writer_manager = None -> write_custom_stream() 变 no-op（安全）

        ComponentExecutable.on_invoke() 会将 NodeSession 包装为 Session，
        Session 的 write_stream/write_custom_stream/trace 在 manager 为 None 时均安全跳过。
        """
        workflow_session = WorkflowSession(
            workflow_id=self._node_id,
            parent=None,
            session_id=self._session_id,  # 使用 conversationId，确保中断恢复时一致
        )
        return NodeSession(
            workflow_session,
            self._node_id,
            type(self._component).__name__,
        )

    # ------------------------------------------------------------------
    # 中断恢复 — 通过 CheckpointerFactory 保存/恢复状态
    # ------------------------------------------------------------------

    async def _save_state_to_checkpointer(self, session: NodeSession) -> None:
        """将 session 状态保存到 checkpointer（用于中断恢复）。

        Args:
            session: NodeSession 实例
        """
        try:
            checkpointer = CheckpointerFactory.get_checkpointer()
            if checkpointer is None:
                return
            # 使用 dump_state 获取完整状态（包括 comp_state_updates）
            if hasattr(session, "dump_state"):
                state = session.dump_state()
            else:
                state = session.get_state() if hasattr(session, "get_state") else None
            if state:
                await checkpointer.save_session(self._session_id, state)
        except Exception as e:
            logging.error(f"Failed to save state to checkpointer: {e}")

    # ------------------------------------------------------------------
    # 能力判定 — 复刻 bpmn_workflow._arun() line 613-614
    # ------------------------------------------------------------------

    def _should_stream(self) -> bool:
        """判断组件是否应该使用流式执行。"""
        if self._node_type in self._LLM_TYPES:
            return True
        if self._node_type in self._STREAMING_API_TYPES and self._configs.get(
            "streaming", False
        ):
            return True
        return False

    # ------------------------------------------------------------------
    # INVOKE 执行 — 复刻 bpmn_workflow._arun() else 分支 line 629-633
    # ------------------------------------------------------------------

    async def _execute_invoke(
        self,
        executable: Any,
        inputs: dict,
        session: NodeSession,
        execution_id: str,
    ) -> AsyncGenerator[StreamData, None]:
        """非流式执行，返回单个 FINISH StreamData。"""
        result = await executable.on_invoke(inputs, session=session, context=None)
        # SetVariable 组件 invoke 返回 None（新框架 LoopSetVariable 通过
        # node_session.state().set_outputs 写入变量），旧框架返回 copy.deepcopy(inputs)。
        # 此处从 session 提取实际写入的变量值，确认 SetVariable 真正生效。
        if result is None and self._node_type == "jiuwen.setVariable":
            result = self._extract_set_variable_result(session)
        # 对齐旧框架 BPMN _post_process：为 dict 类型结果补齐 systemFields
        if (
            isinstance(result, dict)
            and _USER_FIELDS in result
            and _SYSTEM_FIELDS not in result
        ):
            result[_SYSTEM_FIELDS] = {}
        yield StreamData(
            code=StreamCode.FINISH.value,
            msg=StreamDataMsg.FINISH.value,
            data=dict(answer=result, node_type=self._node_type),
            execution_id=execution_id,
        )

    # ------------------------------------------------------------------
    # STREAM 执行 — 复刻 bpmn_workflow._arun() line 615-644
    # ------------------------------------------------------------------

    async def _execute_streaming(
        self,
        executable: Any,
        inputs: dict,
        session: NodeSession,
        execution_id: str,
    ) -> AsyncGenerator[StreamData, None]:
        """流式执行，逐块转换为 StreamData。

        LLM 类型特殊处理：旧框架 BPMN 的 LLM generator 先 yield PARTIAL_CONTENT，
        最后 yield 一个 FINISH（携带完整 answer）。新框架组件 on_stream 产生的
        final_output chunk 对应 MESSAGE_END，此处将其转换为 FINISH 以对齐旧框架。
        参见 bpmn_workflow._process_streaming_data_generator (line 501-597)。

        流式 API 类型：旧框架 FlowApi._transform_async_stream_data (line 329-357)
        在迭代完所有响应块后总是 yield 一个 FINISH（由 get_data_of_streaming_with_metadata
        生成，包含 answer/userFields/node_id/node_name/node_type），此处对齐该行为。
        """
        is_llm = self._node_type in self._LLM_TYPES
        is_streaming_api = self._node_type in self._STREAMING_API_TYPES
        final_answer = None
        # 流式 API 累积输出，对齐旧框架 FlowApi._transform_async_stream_data 的 final_output_dict
        streaming_api_answer_parts: list[str] = []

        stream_iter = executable.on_stream(inputs, session=session, context=None)
        async for chunk in stream_iter:
            sd = self._convert_stream_chunk(chunk, execution_id)
            if sd is None:
                continue
            # LLM: MESSAGE_END 携带 final_output，旧框架将其作为 FINISH 输出
            if is_llm and sd.code == StreamCode.MESSAGE_END.value:
                final_answer = (
                    sd.data.get("answer") if isinstance(sd.data, dict) else sd.data
                )
                continue
            # 流式 API: 累积 PARTIAL_CONTENT 的 answer 文本
            if is_streaming_api and sd.code == StreamCode.PARTIAL_CONTENT.value:
                answer_val = (
                    sd.data.get("answer") if isinstance(sd.data, dict) else None
                )
                if answer_val is not None:
                    streaming_api_answer_parts.append(str(answer_val))
            yield sd

        # LLM 类型: 输出一个 FINISH，携带完整 answer + node 标识
        # 对齐旧框架 __format_result → _process_streaming_data_generator 最后一条 yield
        # 旧框架 FINISH data 结构由 get_data_of_streaming_with_metadata 生成，
        # 包含 answer, node_id, node_name, node_type
        if is_llm:
            finish_data = dict(
                node_id=self._node_id,
                node_name=self._component_name,
                node_type=self._node_type,
            )
            if final_answer is not None:
                # 对齐旧框架 format_response：将 raw answer 包装到 output 字段名中
                # 例：format_response("hello", "markdown", {"output1": {...}}, [...]) → {"output1": "hello"}
                wrapped_answer = self._wrap_llm_final_output(final_answer)
                finish_data["answer"] = {
                    _USER_FIELDS: wrapped_answer,
                    _SYSTEM_FIELDS: {},
                }
            yield StreamData(
                code=StreamCode.FINISH.value,
                msg=StreamDataMsg.FINISH.value,
                data=finish_data,
                execution_id=execution_id,
            )
        elif is_streaming_api:
            # 流式 API 类型：对齐旧框架 FlowApi._transform_async_stream_data 的 FINISH
            # 旧框架格式：get_data_of_streaming_with_metadata(
            #   answer={'USER_FIELDS': {output_id: final_output}}, metadata=...)
            # → {answer: {...}, node_id, node_name, node_type}
            final_output = "".join(streaming_api_answer_parts)
            formatted_answer = self._wrap_streaming_api_final_output(final_output)
            yield StreamData(
                code=StreamCode.FINISH.value,
                msg=StreamDataMsg.FINISH.value,
                data=dict(
                    answer={_USER_FIELDS: formatted_answer, _SYSTEM_FIELDS: {}},
                    node_id=self._node_id,
                    node_name=self._component_name,
                    node_type=self._node_type,
                ),
                execution_id=execution_id,
            )

    # ------------------------------------------------------------------
    # 流式 API FINISH 输出格式化 — 对齐旧框架 FlowApi._transform_async_stream_data
    # ------------------------------------------------------------------

    def _wrap_streaming_api_final_output(self, final_output: str) -> dict:
        """将流式 API 累积输出包装到 output 字段名中。

        对齐旧框架 FlowApi._transform_async_stream_data (line 350):
          formatted_res = {output.get("id"): final_output_dict["final_output"]
                          for output in self._outputs}
        """
        output_definitions = self._configs.get(_USER_FIELDS, {}).get("outputs") or []
        if output_definitions:
            return {item["id"]: final_output for item in output_definitions}
        return {"output": final_output}

    # ------------------------------------------------------------------
    # LLM FINISH 输出格式化 — 对齐旧框架 format_response 行为
    # ------------------------------------------------------------------

    def _wrap_llm_final_output(self, final_answer: Any) -> dict:
        """将 LLM final_output 包装到 output 字段名中，对齐旧框架 format_response。

        旧流程：LLMChain.__format_result → format_response(text, "markdown", outputs_config, ...)
        format_response 对 markdown 类型返回 {output_field_id: text}。
        例：outputs_config = {"output1": {...}} → {"output1": "hello"}

        新框架组件 on_stream 的 final_output 是原始文本，
        需要用 configs.userFields.outputs 中的 id 字段名包装。
        """
        output_definitions = self._configs.get(_USER_FIELDS, {}).get("outputs") or []
        if output_definitions:
            # 取第一个 output 字段名（markdown/text 类型只有一个 output）
            return {item["id"]: final_answer for item in output_definitions}
        return final_answer

    # ------------------------------------------------------------------
    # 流式块转换
    # ------------------------------------------------------------------

    def _convert_stream_chunk(self, chunk: Any, execution_id: str) -> StreamData | None:
        """将组件 on_stream() 输出转换为 StreamData。

        LLMChain.stream()（llm_chain.py:229-256）:
          - yield {_USER_FIELDS: {"raw_output": chunk_str}}   -> PARTIAL_CONTENT
          - yield {_USER_FIELDS: {"final_output": result_str}} -> MESSAGE_END

        FlowApi.stream()（flow_api.py:284-330）:
          - yield OutputSchema(type=..., payload=...)          -> PARTIAL_CONTENT
        """
        # dict 类型（LLMChain 等）
        if isinstance(chunk, dict):
            user_fields = chunk.get(_USER_FIELDS, chunk)
            if isinstance(user_fields, dict):
                if "raw_output" in user_fields:
                    return StreamData(
                        code=StreamCode.PARTIAL_CONTENT.value,
                        msg=StreamDataMsg.SUCCESS.value,
                        data={"answer": user_fields["raw_output"]},
                        execution_id=execution_id,
                    )
                if "final_output" in user_fields:
                    return StreamData(
                        code=StreamCode.MESSAGE_END.value,
                        msg=StreamDataMsg.SUCCESS.value,
                        data={"answer": user_fields["final_output"]},
                        execution_id=execution_id,
                    )
            # 兜底
            return StreamData(
                code=StreamCode.PARTIAL_CONTENT.value,
                msg=StreamDataMsg.SUCCESS.value,
                data=user_fields
                if isinstance(user_fields, dict)
                else {"answer": str(user_fields)},
                execution_id=execution_id,
            )

        # OutputSchema 类型（FlowApi 等）
        if hasattr(chunk, "payload"):
            payload = (
                chunk.payload
                if isinstance(chunk.payload, dict)
                else {"answer": str(chunk.payload)}
            )
            return StreamData(
                code=StreamCode.PARTIAL_CONTENT.value,
                msg=getattr(chunk, "type", "partial_content"),
                data=payload,
                execution_id=execution_id,
                index=getattr(chunk, "index", 0),
            )

        return None

    # ------------------------------------------------------------------
    # SetVariable session 验证 — 确认变量已正确写入 session
    # ------------------------------------------------------------------

    def _extract_set_variable_result(self, session: NodeSession) -> dict:
        """从 session state 提取 SetVariable 的输出，对齐旧框架行为。

        旧框架 SetVariable.invoke() 返回 copy.deepcopy(inputs)，
        新框架 LoopSetVariable.invoke() 返回 None，但通过 node_session.state().set_outputs()
        将变量写入 io_state。
        """
        session.state().commit()
        node_data = session.state().get_outputs()
        if node_data is None:
            raise JiuWenBaseException(
                error_code=StatusCode.COMPONENT_STEP_DEBUG_ERROR.code,
                message=StatusCode.COMPONENT_STEP_DEBUG_ERROR.errmsg.format(
                    reason="SetVariable 执行后 session 中无输出数据，变量写入可能失败。"
                ),
            )

        # 对齐旧框架：只保留 userFields 和 systemFields
        result = {}
        if _USER_FIELDS in node_data:
            result[_USER_FIELDS] = node_data[_USER_FIELDS]
        if _SYSTEM_FIELDS in node_data:
            result[_SYSTEM_FIELDS] = node_data[_SYSTEM_FIELDS]

        # 如果结果为空，说明没有 userFields/systemFields，这时候保持原样
        if not result:
            return node_data

        return result

    # ------------------------------------------------------------------
    # 错误处理 — 复刻 bpmn_workflow._pass_exception_info
    # ------------------------------------------------------------------

    def _build_error_stream_data(
        self, exception: Exception, execution_id: str
    ) -> StreamData:
        """构造错误 StreamData。"""
        if isinstance(exception, JiuWenBaseException):
            error_code = exception.error_code
            message = exception.message
        else:
            error_code = StatusCode.WORKFLOW_COMPONENT_EXECUTE_ERROR.code
            message = str(exception)
        return StreamData(
            code=StreamCode.ERROR.value,
            msg=StreamDataMsg.FAIL.value,
            data=dict(
                code=error_code,
                message=message,
                node_id=self._node_id,
                node_type=self._node_type,
            ),
            execution_id=execution_id,
        )

    # ------------------------------------------------------------------
    # GraphInterrupt 处理 — 单节点调试模式下处理需要用户输入的组件
    # ------------------------------------------------------------------

    async def _handle_graph_interrupt(
        self, interrupt: GraphInterrupt, session: NodeSession, execution_id: str
    ) -> AsyncGenerator[StreamData, None]:
        """处理 GraphInterrupt（组件需要用户输入时的中断信号）。

        单节点调试模式下无法真正等待用户输入，但可以：
        1. 从 GraphInterrupt 中提取已产生的输出（如提问器的问题）
        2. 返回已产生的部分结果和等待用户输入状态

        Args:
            interrupt: GraphInterrupt 异常对象
            session: NodeSession 实例
            execution_id: 执行ID

        Yields:
            StreamData: 包含已产生输出的流式数据
        """
        # 构建等待用户输入的响应数据
        wait_user_input_data = {
            "node_id": self._node_id,
            "node_name": self._component_name,
            "node_type": self._node_type,
            "status": "running",
            "should_interrupt": True,
        }

        # 从 interrupt.value 中提取信息
        # GraphInterrupt.value 是 (Interrupt,) 元组
        # Interrupt.value 是 OutputSchema(type=INTERACTION, payload=InteractionOutput(id=node_id, value=question_text))
        interrupt_value = interrupt.value
        if interrupt_value:
            if isinstance(interrupt_value, tuple):
                for item in interrupt_value:
                    if hasattr(item, "value"):
                        output_schema = item.value
                        # OutputSchema 有 payload 属性，包含 InteractionOutput
                        if hasattr(output_schema, "payload"):
                            payload = output_schema.payload
                            # InteractionOutput 有 id 和 value 属性，value 就是问题文本
                            if hasattr(payload, "value"):
                                wait_user_input_data["question"] = payload.value
                                wait_user_input_data["answer"] = payload.value
                            # 也保存 id
                            if hasattr(payload, "id"):
                                wait_user_input_data["node_id"] = payload.id

        # 先检查 session 中是否有写入的流数据（custom_stream）
        # questioner 组件在抛出 interrupt 前会调用 session.write_custom_stream
        try:
            custom_streams = getattr(session, "custom_streams", None)
            if custom_streams is not None and hasattr(custom_streams, "__iter__"):
                custom_streams = list(custom_streams)
            else:
                custom_streams = []
        except Exception:
            custom_streams = []

        # 从 interrupt 中提取的问题文本
        question_text = wait_user_input_data.get("question", "")

        # 如果有自定义流数据，先输出它们；否则使用从 interrupt 提取的问题文本
        if custom_streams:
            for custom_stream in custom_streams:
                yield StreamData(
                    code=StreamCode.PARTIAL_CONTENT.value,
                    msg=StreamDataMsg.SUCCESS.value,
                    data=custom_stream.get("data", {}),
                    execution_id=execution_id,
                )
        elif question_text:
            # 没有 custom_streams 时，使用从 interrupt 提取的问题内容输出 PARTIAL_CONTENT
            yield StreamData(
                code=StreamCode.PARTIAL_CONTENT.value,
                msg=StreamDataMsg.SUCCESS.value,
                data=wait_user_input_data,
                execution_id=execution_id,
            )

        # 输出 MESSAGE_END 事件
        yield StreamData(
            code=StreamCode.MESSAGE_END.value,
            msg=StreamDataMsg.SUCCESS.value,
            data={"answer": question_text},
            execution_id=execution_id,
        )

        # 最终恢复输出等待用户输入的状态
        # 使用 FINISH 事件表示组件执行完成（需要用户输入的状态在 answer 中体现）
        # 状态已在 GraphInterrupt 捕获前通过 _save_state_to_checkpointer 保存到 checkpointer
        yield StreamData(
            code=StreamCode.FINISH.value,
            msg=StreamDataMsg.FINISH.value,
            data=dict(answer=wait_user_input_data, node_type=self._node_type),
            execution_id=execution_id,
        )
