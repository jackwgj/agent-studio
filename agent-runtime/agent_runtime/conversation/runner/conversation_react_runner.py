"""Conversation-specific ReAct runner.

The runner reuses the official ReAct lifecycle and accepts an optional
request-local IR cache so the built-in Supervisor can use an in-memory IR view.
"""

from __future__ import annotations

import time
import uuid
from typing import AsyncGenerator, Dict, Optional

from agent_runtime.conversation.usage import (
    ConversationUsageRail,
    get_or_create_observer,
)
from agent_runtime.conversation.config.supervisor_config import SupervisorConfig
from agent_runtime.conversation.execution_context import get_conversation_execution_context
from agent_runtime.conversation.sandbox import ConversationSandboxToolBinder
from agent_runtime.runner.react_agent_runner import ReActAgentRunner
from agent_runtime.supervisor.skill_context import (
    attach as attach_skill_context,
    bind_agent_skill_context,
    reset_skill_context,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor
from agent_runtime.runner.react_stream_data_adapter import ReactStreamDataAdapter
from agent_runtime.schemas.orchestration_mgr import ExecutionRequest
from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.session.agent import Session, create_agent_session
from openjiuwen.core.session.stream import BaseStreamMode


class ConversationReActRunner(ReActAgentRunner):
    """Conversation wrapper around the official ReAct runner."""

    def _parse_prompt_template(
        self,
        ir_json: dict,
        conversation_history: list | None = None,
        skill_work_dir: str = "",
        global_variables: dict | None = None,
        has_file_links: bool = False,
    ) -> list[dict]:
        """Append the conversation-only workspace contract to the official prompt."""
        messages = super()._parse_prompt_template(
            ir_json,
            conversation_history,
            skill_work_dir,
            global_variables,
            has_file_links,
        )
        protocol = self._build_workspace_protocol_prompt()
        return [
            {
                **message,
                "content": f"{message.get('content', '')}{protocol}"
                if message.get("role") == "system"
                else message.get("content", ""),
            }
            for message in messages
        ]

    @staticmethod
    def _build_workspace_protocol_prompt() -> str:
        workspace = get_conversation_execution_context().workspace
        return (
            "\n\n## 当前会话沙箱目录协议\n"
            f"当前会话根目录：`{workspace.conversation_root}`。所有文件操作必须限制在该目录内，"
            "不得访问或写入当前会话根目录之外。\n"
            f"- `{workspace.input_dir}`：用户上传的原始输入文件。处理附件时优先从这里读取，"
            "不要覆盖用户原始文件。\n"
            f"- `{workspace.skills_dir}`：已激活 Skill 的完整制品和配套资源。执行 Skill 前读取其"
            " `SKILL.md`，并按需使用同目录资源，不要修改 Skill 制品。\n"
            f"- `{workspace.work_dir}`：默认工作目录。过程文件、中间结果和可继续编辑的工作文件"
            "放在这里。\n"
            f"- `{workspace.output_dir}`：正式成果目录。最终需要交付给用户下载的文档、表格、"
            "图片、压缩包等必须写入这里；只有写入 output 目录的文件才会作为正式成果被采集和发布。\n"
            f"- `{workspace.tmp_dir}`：临时目录。仅用于可丢弃的缓存和短期暂存，不要把最终成果"
            "留在这里。\n"
            "执行命令和代码时默认以 work 目录为 cwd；引用其他目录时使用以上明确路径。"
        )

    @staticmethod
    def _convert_supervisor_to_ir(config: SupervisorConfig) -> dict:
        """Return an in-memory official-shaped IR for the built-in Supervisor."""
        return config.to_ir()

    @staticmethod
    def _get_request_ir(req: ExecutionRequest) -> dict | None:
        """Return a request-local IR snapshot when one was prepared by conversation."""
        return getattr(req.params, "ir_cache", None)

    @staticmethod
    async def _attach_request_skill_context(agent, team_config: dict) -> bool:
        """Attach request-local workspace Skill context to the current Agent."""
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
        recommended = list(
            team_config.get("recommendedSkillIds")
            or team_config.get("recommended_skill_ids")
            or []
        )
        if not catalog:
            return False
        await attach_skill_context(agent, catalog, recommended)
        return True

    @staticmethod
    async def _attach_supervisor_skill_context(agent, team_config: dict) -> bool:
        """Attach the request-scoped workspace Skill catalog to Supervisor only."""
        return await ConversationReActRunner._attach_request_skill_context(
            agent, team_config
        )

    async def _register_supervisor_handoff_tools(
        self,
        agent,
        agent_ids: list[str],
        prepared_file_references: list[dict] | None = None,
        model_deployment_id: str | None = None,
    ) -> None:
        """Attach only built-in Supervisor handoff tools to this request's Agent."""
        from openjiuwen.core.runner import Runner
        from agent_runtime.supervisor.builder import _load_sub_agent_description
        from agent_runtime.supervisor.tool.conversation_handoff_tool import (
            ConversationHandoffTool,
        )

        for agent_id in agent_ids:
            description = await _load_sub_agent_description(agent_id)
            tool = ConversationHandoffTool(
                agent_id=agent_id,
                description=description,
                prepared_file_references=prepared_file_references,
                model_deployment_id=model_deployment_id,
            )
            result = agent.ability_manager.add(tool.card)
            if result.added and Runner.resource_mgr.get_tool(tool.card.id) is None:
                Runner.resource_mgr.add_tool(tool)

    async def _register_usage_rail(self, agent, exec_id: str, parent_run_id: str | None = None,
                                   execution_type: str = "agent"):
        """Bind one request-local usage observer to every ReAct Agent we create."""
        observer = get_or_create_observer(exec_id)
        await agent.register_rail(
            ConversationUsageRail(observer, exec_id, parent_run_id, execution_type)
        )
        return observer

    async def run_streaming(
        self, req: ExecutionRequest, execution_id: str | None = None
    ) -> AsyncGenerator[Dict, None]:
        """Run APP or built-in Supervisor through the official ReAct lifecycle."""
        exec_id = execution_id or req.conversation_id or str(uuid.uuid4())
        adapter = ReactStreamDataAdapter(execution_id=exec_id)

        sandbox_binder: ConversationSandboxToolBinder | None = None
        try:
            ir_json = self._get_request_ir(req) or await self._load_ir(req.ir_path)
        except Exception as error:
            workflow_logger.error("Failed to load conversation IR: %s", error, exc_info=True)
            yield adapter.adapt_error(f"Failed to load workflow configuration: {error}")
            return

        try:
            llm = await self._create_llm(ir_json)
        except Exception as error:
            workflow_logger.error("Failed to create conversation LLM: %s", error)
            yield adapter.adapt_error(f"Failed to create LLM: {error}")
            return

        skills_conf = ir_json.get("configs", {}).get("skills", {})
        skill_work_dir = ""
        skill_info_list = []
        if skills_conf:
            skill_dir = skills_conf.get("skill_dir", "")
            skill_info_list = skills_conf.get("skill_info", [])
            try:
                skill_work_dir = await self._download_skills(skill_dir, skill_info_list) or ""
            except Exception as error:
                workflow_logger.warning("Conversation skill download failed: %s", error)

        query = self._resolve_user_query(req)
        has_file_links = self._has_file_links(query)
        conversation_history = req.params.conversation_history
        global_variables = req.params.global_variables or {}
        prepared_inputs = list(global_variables.get("conversationInputFiles") or [])
        if prepared_inputs:
            input_lines = [
                "以下本轮上传文件已准备到当前会话沙箱 input 目录；只能使用这些沙箱路径："
            ]
            for item in prepared_inputs:
                if isinstance(item, dict) and item.get("path"):
                    input_lines.append(
                        f"- {item.get('fileName') or '未命名文件'}: {item['path']}"
                    )
            if len(input_lines) > 1:
                query = f"{query}\n\n" + "\n".join(input_lines)

        try:
            agent, agent_id = self._create_agent(
                ir_json,
                conversation_history,
                skill_work_dir,
                global_variables,
                has_file_links,
            )
            agent.set_llm(llm)
            observer = await self._register_usage_rail(
                agent,
                exec_id,
                parent_run_id=global_variables.get("parentRunId"),
                execution_type=global_variables.get("executionType", "agent"),
            )
        except Exception as error:
            workflow_logger.error("Failed to create conversation Agent: %s", error)
            yield adapter.adapt_error(f"Failed to create agent: {error}")
            return

        session: Optional[Session] = None
        skill_token = None
        is_first_llm_call = True
        try:
            try:
                await self._register_plugins(ir_json, agent, agent_id)
                await self._register_mcp_servers(ir_json, agent, agent_id)
                await self._register_workflows(ir_json, agent, agent_id)
                await self._register_skills(ir_json, agent, agent_id, skill_work_dir)
                if has_file_links:
                    self._register_file_reader_tool(agent, agent_id)

                sandbox_binder = ConversationSandboxToolBinder.from_runtime_settings()
                sandbox_binder.register(agent)
                team_config = (global_variables or {}).get("conversationTeam") or {}
                if team_config.get("type") == "SUPERVISOR":
                    skill_attached = await self._attach_supervisor_skill_context(
                        agent, team_config
                    )
                    await self._register_supervisor_handoff_tools(
                        agent,
                        list(team_config.get("subAgentIds") or []),
                        list(global_variables.get("conversationInputFiles") or []),
                        team_config.get("modelDeploymentId"),
                    )
                else:
                    skill_attached = await self._attach_request_skill_context(
                        agent, team_config
                    )
                if skill_attached:
                    skill_token = bind_agent_skill_context(agent)
            except Exception as error:
                workflow_logger.error("Failed to register conversation tools: %s", error)
                yield adapter.adapt_error(f"Failed to register tools: {error}")
                return

            try:
                from openjiuwen.extensions.tracer_otel.otel_rail import OtelRail

                await agent.register_rail(OtelRail())
            except Exception as error:
                workflow_logger.debug("Conversation OtelRail registration skipped: %s", error)

            inputs = {
                "query": query,
                "conversation_id": req.conversation_id,
                "user_id": req.user_id,
            }
            yield adapter.create_start_event()
            adapter.start_time = int(time.time() * 1000)
            chain_id = str(uuid.uuid4())
            adapter.set_chain_and_agent_ids(chain_id, chain_id)
            yield adapter.create_agent_node_start_event(
                invoke_id=chain_id,
                chain_id=chain_id,
                invoke_type="chain",
                name=ir_json.get("agentName", "Agent"),
            )

            session: Optional[Session] = None
            is_first_llm_call = True
            try:
                session_id = req.conversation_id or "default_session"
                session = create_agent_session(session_id=session_id, card=agent.card)
                await session.pre_run(inputs=inputs)
                inputs.setdefault("_jiuwen_runtime_kwargs", {})["session"] = session
                prompt_messages = self._parse_prompt_template(
                    ir_json, conversation_history, skill_work_dir, global_variables, has_file_links
                )
                llm_inputs = list(prompt_messages) + [{"role": "user", "content": query}]
                functions = self._get_agent_functions(agent)
                llm_meta_data = {
                    "class_name": "Openai",
                    "instance_attributes": {
                        "model": ir_json.get("configs", {}).get("modelConfig", {}).get("modelName", ""),
                        "temperature": ir_json.get("configs", {}).get("modelConfig", {}).get("hyperParameters", {}).get("temperature", 0.0),
                        "top_p": ir_json.get("configs", {}).get("modelConfig", {}).get("hyperParameters", {}).get("top_p", 1.0),
                        "functions": functions,
                    },
                }
                async for chunk in agent.stream(
                    inputs,
                    session,
                    [BaseStreamMode.OUTPUT, BaseStreamMode.TRACE, BaseStreamMode.CUSTOM],
                ):
                    chunk_type = getattr(chunk, "type", None)
                    if chunk_type == "llm_output" and is_first_llm_call:
                        is_first_llm_call = False
                        llm_invoke_id = str(uuid.uuid4())
                        adapter.set_llm_invoke_id(llm_invoke_id)
                        adapter.set_llm_inputs(llm_inputs)
                        adapter.set_llm_meta_data(llm_meta_data)
                        adapter.add_child_invoke_id(llm_invoke_id)
                        adapter._is_llm_call_started = True
                        yield adapter.create_agent_node_start_event(
                            invoke_id=llm_invoke_id,
                            chain_id=chain_id,
                            invoke_type="llm",
                            name="Openai",
                            inputs=llm_inputs,
                            meta_data=llm_meta_data,
                        )
                    for event in adapter.adapt(chunk):
                        if event:
                            yield event
                    for usage_event in observer.drain_events(req.conversation_id):
                        yield usage_event
                yield adapter.create_agent_node_end_event(
                    invoke_id=chain_id,
                    chain_id=chain_id,
                    invoke_type="chain",
                    name=ir_json.get("agentName", "Agent"),
                    inputs={"query": query},
                    outputs=adapter.final_output,
                    child_invokes=adapter.child_invoke_ids,
                )
            except Exception as error:
                workflow_logger.error("Conversation ReAct stream failed: %s", error, exc_info=True)
                yield adapter.adapt_error(f"Agent execution failed: {error}")
        finally:
            if sandbox_binder is not None:
                sandbox_binder.cleanup()
            if skill_token is not None:
                reset_skill_context(skill_token)
            if session is not None:
                await session.post_run()
