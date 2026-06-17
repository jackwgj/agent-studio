"""ControllerRunner — HierarchicalAgentGroup execution with full SSE event flow."""

from __future__ import annotations

import os
import time
import uuid
from typing import AsyncGenerator, Any

from agent_runtime.runner.controller_stream_data_adapter import (
    ControllerStreamDataAdapter,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionRequest
from jiuwen.serve.controllers.execution.enum import IRType
from jiuwen.serve.controllers.execution.ir_converter import IRConverter
from jiuwen.serve.controllers.execution.open_utils import async_ir_load
from jiuwen.serve.controllers.execution.manager import AsyncStateManager
from jiuwen.serve.controllers.execution.types import ExecutionData
from jiuwen.serve.controllers.execution.utils import (
    build_agent_input,
    post_process_agent_group_streaming_output,
)
from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.common.logging import performance_logger


class ControllerRunner:
    """Execute IRType.Agent (Controller mode) via HierarchicalAgentGroup path.

    Produces full SSE event flow matching commercial JiuWen:
    start → task_start → intent → workflow_start → message(s) → workflow_end → done → task_end
    """

    def __init__(self, api_key: str | None = None, api_base: str | None = None):
        self._api_key = api_key or os.environ.get("API_KEY", "")
        self._api_base = api_base or os.environ.get(
            "API_BASE", "https://api.deepseek.com"
        )
        if self._api_key:
            os.environ["IR_LLM_API_KEY"] = self._api_key
        if self._api_base:
            os.environ["IR_LLM_API_BASE"] = self._api_base

    async def run_streaming(
        self,
        req: ExecutionRequest,
        execution_id: str | None = None,
    ) -> AsyncGenerator[Any]:
        """Execute Controller mode IR and yield SSE bytes.

        Args:
            req: Execution request with conversation_id, query, ir_path, params, headers
            execution_id: Request-level execution ID for SSE event tracking

        Yields:
            SSE event dictionaries in the format expected by the API
        """
        session_id = req.conversation_id
        exec_id = execution_id or session_id or str(uuid.uuid4())
        adapter = ControllerStreamDataAdapter(execution_id=exec_id)

        # 1. Load IR from cache or storage
        try:
            ir_json = await async_ir_load(req.ir_path)
        except Exception as e:
            workflow_logger.error(
                f"Failed to load IR from {req.ir_path}: {e}", exc_info=True
            )
            yield adapter.adapt_error(
                f"Failed to load workflow configuration: {e}", exec_id
            )
            return

        # 2. Build PlanRuntimeContext (reuse commercial logic)
        try:
            t_build_input = time.perf_counter()
            insight_client, runtime_context, tracer = await build_agent_input(req)
            performance_logger.info(f"build_agent_input|{round((time.perf_counter() - t_build_input) * 1000)}")
        except Exception as e:
            workflow_logger.error(f"Failed to build agent input: {e}", exc_info=True)
            yield adapter.adapt_error(f"Failed to build runtime context: {e}", exec_id)
            return

        # 3. Create Agent Group instance
        try:
            t_agent_group = time.perf_counter()
            agent_group = await IRConverter.ir_to_agent_group(ir_json, session_id)
            performance_logger.info(f"ir_to_agent_group|{round((time.perf_counter() - t_agent_group) * 1000)}")
        except Exception as e:
            workflow_logger.error(
                f"Failed to create agent group from IR: {e}", exc_info=True
            )
            yield adapter.adapt_error(f"Failed to create agent group: {e}", exec_id)
            return

        # 4. Setup insight queue for debug mode
        is_debug = (
            os.environ.get("INSIGHT_EI_DEBUG_INFO_ENABLE", "true").lower() == "true"
        )
        insight_queue = insight_client.data_queue if is_debug else None

        # 5. Create ExecutionData for post_process_agent_group_streaming_output
        execution_data = ExecutionData(
            instance=agent_group,
            instance_type=IRType.Agent,
            updated_time=int(time.time() * 1000),
            collector=None,
        )

        # 6. Execute agent group streaming using post_process_agent_group_streaming_output
        try:
            t_stream_start = time.perf_counter()
            workflow_logger.info(f"Starting agent_group.astream for query: {req.query}")
            streaming_output = agent_group.astream(
                req.query,
                stream=True,
                runtime_context=runtime_context,
                tool_switch_dict=getattr(req.params, "tool_switch_dict", None),
                trace_handlers=tracer,
            )

            async for chunk in post_process_agent_group_streaming_output(
                conversation_id=session_id,
                origin_output=streaming_output,
                execution_data=execution_data,
                insight_queue=insight_queue,
                is_debug=is_debug,
            ):
                yield adapter.adapt_execution_id(chunk)
            performance_logger.info(f"agent_group_stream|{round((time.perf_counter() - t_stream_start) * 1000)}")
        except Exception as e:
            workflow_logger.error(
                f"Agent group streaming failed with exception: {e}", exc_info=True
            )
            import traceback

            tb_str = traceback.format_exc()
            workflow_logger.error(f"Exception traceback: {tb_str}")
            yield adapter.adapt_error(f"Agent execution failed: {e}\n{tb_str}", exec_id)
            await AsyncStateManager().delete_state(session_id)
            return

    async def run_blocking(self, req: ExecutionRequest) -> str:
        """Execute Controller mode IR and return complete result.

        Collects all message events and concatenates them.
        """
        result_parts = []
        async for chunk in self.run_streaming(req):
            if chunk is None:
                continue
            # Parse SSE bytes to extract message text
            try:
                import json

                chunk_str = chunk.decode() if isinstance(chunk, bytes) else chunk
                if chunk_str.startswith("data: "):
                    data = json.loads(chunk_str[6:])
                    event = data.get("event", "")
                    answer = data.get("data", {}).get("answer", "") or data.get(
                        "data", {}
                    ).get("output", "")
                    if answer:
                        result_parts.append(str(answer))
            except Exception as e:
                workflow_logger.error(
                    f"Agent group blocking failed with exception: {e}", exc_info=True
                )
                continue
        return "".join(result_parts)
