"""Conversation-specific Controller runner with request-local Skill Functions."""

from __future__ import annotations

import json
import os
import time
import traceback
import uuid
from typing import Any, AsyncGenerator

from agent_runtime.context.request_context import _request_ctx
from agent_runtime.runner.controller_runner import ControllerRunner
from agent_runtime.runner.controller_stream_data_adapter import (
    ControllerStreamDataAdapter,
)
from agent_runtime.runner.memory_extraction_context import MemoryExtractionContext
from agent_runtime.supervisor.skill_context import build_skill_execution_context
from agent_runtime.supervisor.skill_model import SkillDescriptor
from agent_runtime.conversation.runner.conversation_skill_function import (
    ConversationActivateSkillFunction,
)
from agent_runtime.schemas.orchestration_mgr import ExecutionRequest
from jiuwen.controller.common.constants import WorkflowConstants
from jiuwen.serve.controllers.execution.enum import IRType
from jiuwen.serve.controllers.execution.ir_converter import IRConverter
from jiuwen.serve.controllers.execution.manager import AsyncStateManager
from jiuwen.serve.controllers.execution.open_utils import async_ir_load
from jiuwen.serve.controllers.execution.types import ExecutionData
from jiuwen.serve.controllers.execution.utils import (
    build_agent_input,
    post_process_agent_group_streaming_output,
)
from openjiuwen.core.common.logging import performance_logger, workflow_logger
from openjiuwen.core.session.agent import Session, create_agent_session


class ConversationControllerRunner(ControllerRunner):
    """Controller/PlanExecute wrapper with request-local Skill adaptation."""

    @staticmethod
    def _conversation_team(req) -> dict:
        return (
            (getattr(req.params, "global_variables", None) or {}).get(
                "conversationTeam"
            )
            or {}
        )

    @staticmethod
    def _build_skill_context(team_config: dict):
        catalog = [
            SkillDescriptor(
                skill_id=item.get("skillId") or item.get("skill_id") or "",
                version_id=item.get("versionId") or item.get("version_id") or "",
                name=item.get("name") or "",
                description=item.get("description") or "",
                object_key=item.get("objectKey") or item.get("object_key") or "",
            )
            for item in team_config.get("skillCatalog") or []
        ]
        if not catalog:
            return None
        recommended = list(
            team_config.get("recommendedSkillIds")
            or team_config.get("recommended_skill_ids")
            or []
        )
        return build_skill_execution_context(catalog, recommended)

    async def _build_request_agent_group(
        self, req: ExecutionRequest, mode: str, ir_json: dict | None = None
    ):
        """Build official in-memory group config and add request-local Skill Functions."""
        ir_json = ir_json or await async_ir_load(req.ir_path)
        request_context = _request_ctx.get()
        cust_headers = request_context.customer_headers if request_context else {}
        project_id = request_context.project_id if request_context else ""
        group_config, agent_info_map = await IRConverter.create_agent_group_config(
            ir_json,
            req.conversation_id,
            cust_headers=cust_headers,
            project_id=project_id,
        )
        skill_context = self._build_skill_context(self._conversation_team(req))
        if skill_context is not None:
            skill_function = ConversationActivateSkillFunction(skill_context)
            for agent_config in [group_config.main_agent, *group_config.agents]:
                plugins = list(agent_config.plugins or [])
                if not any(plugin.name == skill_function.name for plugin in plugins):
                    plugins.append(skill_function)
                agent_config.plugins = plugins
        return group_config, agent_info_map, skill_context

    async def _create_request_agent_group(
        self, req: ExecutionRequest, mode: str, ir_json: dict
    ):
        group_config, agent_info_map, skill_context = (
            await self._build_request_agent_group(req, mode, ir_json)
        )
        agent_group = await IRConverter.create_or_restore_agent_group(
            group_config, req.conversation_id
        )
        agent_group.update_group_prompt(agent_info_map)
        return agent_group, skill_context

    async def run_streaming(
        self, req: ExecutionRequest, execution_id: str | None = None
    ) -> AsyncGenerator[Any, None]:
        """Execute official Controller/PlanExecute flow with request Skill injection."""
        session_id = req.conversation_id
        exec_id = execution_id or session_id or str(uuid.uuid4())
        adapter = ControllerStreamDataAdapter(execution_id=exec_id)

        try:
            ir_json = await async_ir_load(req.ir_path)
        except Exception as error:
            workflow_logger.error(
                "Failed to load IR from %s: %s", req.ir_path, error, exc_info=True
            )
            yield adapter.adapt_error(
                f"Failed to load workflow configuration: {error}", exec_id
            )
            return

        try:
            start = time.perf_counter()
            insight_client, runtime_context, tracer = await build_agent_input(req)
            performance_logger.info(
                "build_agent_input|%s",
                round((time.perf_counter() - start) * 1000),
            )
            controller_memory_repo_id = (
                (ir_json.get("configs") or {}).get("memory") or {}
            ).get("memory_repo_id", "")
            if controller_memory_repo_id:
                req_params = runtime_context.agent_workflow_context.get(
                    WorkflowConstants.WORKFLOW_REQ_PARAMS_KEY, {}
                )
                req_params["memory_repo_id"] = controller_memory_repo_id
                if req.user_id:
                    global_variables = dict(req_params.get("global_variables") or {})
                    if not global_variables.get("userId"):
                        global_variables["userId"] = req.user_id
                    req_params["global_variables"] = global_variables
                runtime_context.agent_workflow_context[
                    WorkflowConstants.WORKFLOW_REQ_PARAMS_KEY
                ] = req_params
        except Exception as error:
            workflow_logger.error("Failed to build agent input: %s", error, exc_info=True)
            yield adapter.adapt_error(f"Failed to build runtime context: {error}", exec_id)
            return

        try:
            start = time.perf_counter()
            agent_group, _skill_context = await self._create_request_agent_group(
                req,
                (ir_json.get("configs") or {}).get("mode", "Controller"),
                ir_json,
            )
            performance_logger.info(
                "conversation_ir_to_agent_group|%s",
                round((time.perf_counter() - start) * 1000),
            )
        except Exception as error:
            workflow_logger.error(
                "Failed to create conversation AgentGroup: %s", error, exc_info=True
            )
            yield adapter.adapt_error(f"Failed to create agent group: {error}", exec_id)
            return

        is_debug = os.environ.get("INSIGHT_EI_DEBUG_INFO_ENABLE", "true").lower() == "true"
        insight_queue = insight_client.data_queue if is_debug else None
        execution_data = ExecutionData(
            instance=agent_group,
            instance_type=IRType.Agent,
            updated_time=int(time.time() * 1000),
            collector=None,
        )

        memory_response_parts: list[str] = []
        session: Session | None = None
        try:
            start = time.perf_counter()
            workflow_logger.info("Starting conversation agent_group.astream for query: %s", req.query)
            session_id = req.conversation_id or "default_session"
            session_inputs = {
                "query": req.query or "",
                "conversation_id": req.conversation_id,
                "user_id": req.user_id,
            }
            from agent_runtime.observability import setup_otel_tracer

            setup_otel_tracer()
            session = create_agent_session(session_id=session_id, card=agent_group.card)
            await session.pre_run(inputs=session_inputs)
            streaming_output = agent_group.astream(
                req.query,
                stream=True,
                runtime_context=runtime_context,
                tool_switch_dict=getattr(req.params, "tool_switch_dict", None),
                trace_handlers=tracer,
                session=session,
            )

            async for chunk in post_process_agent_group_streaming_output(
                conversation_id=session_id,
                origin_output=streaming_output,
                execution_data=execution_data,
                insight_queue=insight_queue,
                is_debug=is_debug,
            ):
                try:
                    chunk_text = chunk.decode("utf-8") if isinstance(chunk, bytes) else chunk
                    if isinstance(chunk_text, str) and chunk_text.startswith("data: "):
                        event_data = json.loads(chunk_text[6:])
                        event = event_data.get("event")
                        event_payload = event_data.get("data") or {}
                        answer = event_payload.get("answer", "") if isinstance(event_payload, dict) else ""
                        if isinstance(answer, dict):
                            answer = answer.get("content", "")
                        if not answer and isinstance(event_payload, dict):
                            answer = event_payload.get("content", "")
                        if event in {"message", "summary_response", "workflow_end", "done"} and isinstance(answer, str) and answer:
                            print(answer, flush=True)
                        if event in ("message", "done") and answer:
                            memory_response_parts.append(str(answer))
                except Exception:
                    workflow_logger.debug(
                        "Failed to parse Controller SSE chunk for memory extraction",
                        exc_info=True,
                    )
                adapted_chunk = adapter.adapt_execution_id(chunk)
                yield adapted_chunk

            performance_logger.info(
                "conversation_agent_group_stream|%s",
                round((time.perf_counter() - start) * 1000),
            )
            await self._trigger_memory_extraction(
                MemoryExtractionContext(
                    ir_json=ir_json,
                    user_id=req.user_id,
                    conversation_id=req.conversation_id,
                    user_query=req.query or "",
                    assistant_response="".join(memory_response_parts),
                    enable_memory_extract=bool(req.params.enable_memory_extract),
                )
            )
        except Exception as error:
            workflow_logger.error(
                "Conversation Controller stream failed: %s", error, exc_info=True
            )
            yield adapter.adapt_error(
                f"Agent execution failed: {error}\n{traceback.format_exc()}", exec_id
            )
            await AsyncStateManager().delete_state(session_id)
        finally:
            if session is not None:
                await session.post_run()
