# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
"""
ReActAgent Runner — 使用 openjiuwen ReActAgent 执行用户请求
"""

from __future__ import annotations

import os
import time
import uuid
import json
from typing import AsyncGenerator, Optional, Dict

from agent_runtime.runner.react_stream_data_adapter import ReactStreamDataAdapter
from agent_runtime.runner.react_workflow_adapter import ReactWorkflowAdapter
from agent_runtime.schemas.orchestration_mgr import ExecutionRequest
from jiuwen.serve.controllers.execution.open_utils import async_ir_load
from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.common.logging import performance_logger
from openjiuwen.core.foundation.llm import Model
from openjiuwen.core.session.agent import Session, create_agent_session
from openjiuwen.core.session.stream import BaseStreamMode
from openjiuwen.core.single_agent.agents.react_agent import ReActAgent, ReActAgentConfig
from openjiuwen.core.single_agent.schema.agent_card import AgentCard


def _adapt_react_agent_config(ir_json: dict) -> dict:
    """将 IR 配置适配为 IRModelConfigProvider 期望的格式"""
    configs = ir_json.get("configs", {})
    model_config = configs.get("modelConfig", {})

    return {
        "configs": {
            "model": {
                "modelName": model_config.get("modelName", ""),
                "modelType": model_config.get("modelType", ""),
                "modelBaseUrl": model_config.get("modelBaseUrl", ""),
                "hyperParameters": model_config.get("hyperParameters", {}),
                "extension": model_config.get("extension", {}),
            }
        }
    }


class ReActAgentRunner:
    """使用 ReActAgent 运行用户请求的 Runner

    支持从 IR 配置加载并注册 Plugin/MCP/Workflow/Skill 等多种工具类型
    """

    def __init__(self, api_key: str | None = None, api_base: str | None = None):
        self._api_key = api_key or os.environ.get("API_KEY") or ""
        self._api_base = (api_base or os.environ.get("API_BASE", "https://api.deepseek.com")).rstrip("/")

    async def _load_ir(self, ir_path: str) -> dict:
        """从存储加载 IR 配置"""
        try:
            return await async_ir_load(ir_path)
        except Exception as e:
            workflow_logger.error(f"Failed to load IR from {ir_path}: {e}")
            raise

    def _parse_prompt_template(self, ir_json: dict, conversation_history: list = None) -> list[dict]:
        """解析 IR 中的提示词模板，添加工具使用说明"""
        configs = ir_json.get("configs", {})
        sys_prompt = configs.get("sysPromptTemplate", "")

        tool_instruction = "\n\n你可以通过调用工具来完成任务。可使用 available tools 中提供的工具，工具的参数优先结合上下文进行推断。"

        history_section = self._format_conversation_history(conversation_history)

        if sys_prompt:
            full_prompt = sys_prompt + tool_instruction + history_section
        else:
            full_prompt = "你是一个AI助手，能够帮助用户完成任务。" + tool_instruction + history_section

        return [{"role": "system", "content": full_prompt}]

    def _format_conversation_history(self, history: list) -> str:
        """格式化对话历史"""
        if not history:
            return ""

        history_lines = ["\n\n## 对话历史"]
        for msg in history:
            msg_dict = msg.model_dump() if hasattr(msg, "model_dump") else msg
            role = msg_dict.get("role", "")
            content = msg_dict.get("content", "")
            if content:
                history_lines.append(f"- **{role}**: {content}")

        return "\n".join(history_lines)

    def _parse_max_iterations(self, ir_json: dict) -> int:
        """解析最大迭代次数"""
        return ir_json.get("configs", {}).get("maxIteration", 5)

    def _create_llm(self, ir_json: dict) -> Model:
        """根据 IR 配置创建 LLM 模型实例"""
        from agent_runtime.common.model_providers import IRModelConfigProvider

        adapted_conf = _adapt_react_agent_config(ir_json)
        provider = IRModelConfigProvider()
        llm_comp_config = provider.get_llm_config(adapted_conf)

        return Model(
            model_client_config=llm_comp_config.model_client_config,
            model_config=llm_comp_config.model_config,
        )

    def _convert_arguments_to_schema(self, arguments: list) -> dict:
        """将 IR 的 arguments 转换为 JSON Schema 格式"""
        properties = {}
        required = []

        for arg in arguments:
            arg_name = arg.get("name", "")
            arg_type = arg.get("type", "string")
            arg_desc = arg.get("description", "")
            is_required = arg.get("required", False)
            arg_method = arg.get("method", "")

            if arg_method == "Headers":
                continue

            schema_type = self._map_argument_type(arg_type)

            properties[arg_name] = {
                "type": schema_type,
                "description": arg_desc,
            }

            if is_required:
                required.append(arg_name)

        return {
            "type": "object",
            "properties": properties,
            "required": required,
        }

    @staticmethod
    def _map_argument_type(arg_type: str) -> str:
        """映射参数类型到 JSON Schema 类型"""
        type_mapping = {
            "integer": "integer",
            "int": "integer",
            "number": "number",
            "float": "number",
            "double": "number",
            "boolean": "boolean",
            "bool": "boolean",
            "array": "array",
            "list": "array",
            "object": "object",
            "dict": "object",
        }
        return type_mapping.get(arg_type.lower(), "string")

    async def _register_plugins(self, ir_json: dict, agent: ReActAgent, agent_id: str) -> list[str]:
        """注册 Plugin 工具"""
        from jiuwen.extension.wrapper.restful_api_loader import load_restful_api_from_ir
        from openjiuwen.core.foundation.tool import ToolCard

        tool_ids = []
        plugins = ir_json.get("configs", {}).get("plugins", [])

        for plugin_conf in plugins:
            try:
                tool_id = load_restful_api_from_ir(plugin_conf, tag=agent_id)
                input_params = self._convert_arguments_to_schema(plugin_conf.get("arguments", []))

                card = ToolCard(
                    id=tool_id,
                    name=plugin_conf.get("name", tool_id),
                    description=plugin_conf.get("description", ""),
                    input_params=input_params,
                )
                agent.ability_manager.add(card)
                tool_ids.append(tool_id)
            except Exception as e:
                workflow_logger.error(f"Failed to register plugin {plugin_conf.get('name')}: {e}")

        workflow_logger.info(f"Registered {len(tool_ids)} plugins success")
        return tool_ids

    async def _register_mcp_servers(self, ir_json: dict, agent: ReActAgent, agent_id: str) -> list[str]:
        """注册 MCP Server"""
        from jiuwen.extension.wrapper.mcp_server_loader import convert_ir_to_server_config, load_mcp_server_from_ir

        tool_ids = []
        mcps = ir_json.get("configs", {}).get("mcps", [])

        for mcp_conf in mcps:
            try:
                mcp_tool_ids = await load_mcp_server_from_ir(mcp_conf, tag=agent_id)
                mcp_config = convert_ir_to_server_config(mcp_conf)
                agent.ability_manager.add(mcp_config)
                tool_ids.extend(mcp_tool_ids)
            except Exception as e:
                workflow_logger.error(f"Failed to register MCP server {mcp_conf.get('name')}: {e}")

        workflow_logger.info(f"Registered {len(tool_ids)} MCP tools success")
        return tool_ids

    async def _register_workflows(self, ir_json: dict, agent: ReActAgent, agent_id: str) -> list[str]:
        """注册 Workflow 工具"""
        from jiuwen.serve.controllers.execution.ir_converter import IRConverter
        from openjiuwen.core.runner import Runner
        from openjiuwen.core.workflow import WorkflowCard

        workflow_ids = []
        workflows = ir_json.get("configs", {}).get("workflows", [])

        for wf_conf in workflows:
            try:
                ir_path = wf_conf.get("ir_path", "")
                if not ir_path:
                    continue

                sub_ir = await async_ir_load(ir_path)
                workflow = await IRConverter.async_ir_to_workflow(sub_ir)

                workflow_id = wf_conf.get("name", "") or sub_ir.get("workflow", {}).get("id", "")
                if not workflow_id:
                    workflow_id = str(uuid.uuid4())

                wf_name = workflow.card.name
                wf_instance = workflow
                wf_desc = sub_ir.get("workflow", {}).get("description", "")
                user_fields_keys = [arg.get("name") for arg in wf_conf.get("arguments", [])]

                input_params = {
                    "type": "object",
                    "properties": {
                        arg.get("name", ""): {
                            "type": arg.get("type", "string"),
                            "description": arg.get("description", ""),
                        }
                        for arg in wf_conf.get("arguments", [])
                    },
                    "required": [
                        arg.get("name", "")
                        for arg in wf_conf.get("arguments", [])
                        if arg.get("required", False)
                    ],
                }

                # 清理旧注册
                for key in (wf_instance.card.id, wf_name):
                    try:
                        Runner.resource_mgr.remove_workflow(workflow_id=key, tag=agent_id)
                        Runner.resource_mgr.remove_tool(tool_id=key, tag=agent_id)
                    except Exception:
                        pass

                # 注册到 resource_mgr（使用 workflow 方式）
                new_card = WorkflowCard(
                    id=wf_name,
                    name=wf_name,
                    description=wf_desc,
                )
                Runner.resource_mgr.add_workflow(
                    card=new_card,
                    workflow=lambda card=new_card: wf_instance,
                    tag=agent_id,
                )

                # 注册 Tool 到 resource_mgr
                try:
                    wt_instance = ReactWorkflowAdapter(
                        workflow_instance=wf_instance,
                        workflow_name=wf_name,
                        workflow_desc=wf_desc,
                        input_params=input_params,
                        user_fields_keys=user_fields_keys,
                    )
                    Runner.resource_mgr.add_tool(tool=wt_instance, tag=agent_id)
                except Exception as e:
                    workflow_logger.error(f"Failed to register tool: {e}")

                # 注册到 ability_manager
                agent.ability_manager.add(wt_instance.card)
                workflow_ids.append(workflow_id)
            except Exception as e:
                workflow_logger.error(f"Failed to register workflow {wf_conf.get('name')}: {e}")

        workflow_logger.info(f"Registered {len(workflow_ids)} workflows success")
        return workflow_ids

    async def _register_skills(self, ir_json: dict, config: ReActAgentConfig) -> None:
        """注册 Skill 配置"""
        skills = ir_json.get("skills", {})
        if skills:
            skill_dir = skills.get("skillDir", "")
            if skill_dir:
                config.sys_operation_id = skill_dir

        workflow_logger.info(f"Registered skill config success")

    def _create_agent(self, ir_json: dict, conversation_history: list = None) -> tuple[ReActAgent, str]:
        """根据 IR 配置创建 ReActAgent 实例"""
        prompt_template = self._parse_prompt_template(ir_json, conversation_history)
        max_iterations = self._parse_max_iterations(ir_json)
        agent_id = ir_json.get("agentId", "react_agent")

        card = AgentCard(
            id=agent_id,
            name=ir_json.get("agentName", "ReAct Agent"),
            description=ir_json.get("description", "ReAct paradigm Agent"),
        )

        config = ReActAgentConfig()
        config.configure_max_iterations(max_iterations=max_iterations)
        config.configure_prompt_template(prompt_template)

        agent = ReActAgent(card)
        agent.configure(config)

        return agent, agent_id

    async def run_streaming(
        self, req: ExecutionRequest, execution_id: str | None = None
    ) -> AsyncGenerator[Dict, None]:
        """流式运行 ReActAgent 并 yield SSE 事件"""
        exec_id = execution_id or req.conversation_id or str(uuid.uuid4())
        adapter = ReactStreamDataAdapter(execution_id=exec_id)

        # 加载 IR 配置
        try:
            ir_json = await self._load_ir(req.ir_path)
        except Exception as e:
            workflow_logger.error(
                f"Failed to load IR from {req.ir_path}: {e}", exc_info=True
            )
            yield adapter.adapt_error(f"Failed to load workflow configuration: {e}")
            return

        # 创建 LLM 模型
        try:
            llm = self._create_llm(ir_json)
        except Exception as e:
            workflow_logger.error(f"Failed to create LLM: {e}")
            yield adapter.adapt_error(f"Failed to create LLM: {e}")
            return

        # 创建 Agent
        try:
            conversation_history = req.params.conversation_history
            agent, agent_id = self._create_agent(ir_json, conversation_history)
            agent.set_llm(llm)
        except Exception as e:
            workflow_logger.error(f"Failed to create agent: {e}")
            yield adapter.adapt_error(f"Failed to create agent: {e}")
            return

        # 注册工具
        try:
            await self._register_plugins(ir_json, agent, agent_id)
            await self._register_mcp_servers(ir_json, agent, agent_id)
            await self._register_workflows(ir_json, agent, agent_id)
            await self._register_skills(ir_json, agent._config)
        except Exception as e:
            workflow_logger.error(f"Failed to register tools: {e}")
            yield adapter.adapt_error(f"Failed to register tools: {e}")
            return

        # 构建输入
        query = req.query or "Hello"
        inputs = {
            "query": query,
            "conversation_id": req.conversation_id,
            "user_id": req.user_id,
        }

        # 发送初始事件
        yield adapter.create_start_event()
        adapter.start_time = int(time.time() * 1000)

        chain_id = str(uuid.uuid4())
        agent_node_id = str(uuid.uuid4())

        # 设置 adapter 的 chain 和 agent ID（必须在创建事件之前）
        adapter.set_chain_and_agent_ids(chain_id, agent_node_id)

        yield adapter.create_agent_node_start_event(
            invoke_id=agent_node_id,
            chain_id=chain_id,
            invoke_type="chain",
            name=ir_json.get("agentName", "Agent"),
        )

        # 执行并转换输出
        session: Optional[Session] = None
        is_first_llm_call = True

        try:
            session_id = req.conversation_id or "default_session"
            session = create_agent_session(session_id=session_id, card=agent.card)
            await session.pre_run(inputs=inputs)

            async for chunk in agent.stream(inputs, session, [BaseStreamMode.OUTPUT, BaseStreamMode.TRACE, BaseStreamMode.CUSTOM]):
                chunk_type = getattr(chunk, "type", None)

                
                # 第一次 llm_output 时发送 LLM 开始事件
                if chunk_type == "llm_output" and is_first_llm_call:
                    is_first_llm_call = False
                    llm_invoke_id = str(uuid.uuid4())
                    adapter.set_llm_invoke_id(llm_invoke_id)
                    adapter.add_child_invoke_id(llm_invoke_id)
                    adapter._is_llm_call_started = True

                    yield adapter.create_agent_node_start_event(
                        invoke_id=llm_invoke_id,
                        chain_id=chain_id,
                        invoke_type="llm",
                        name="Openai",
                    )

                # 处理 chunk，适配器会自动生成对应事件
                for event in adapter.adapt(chunk):
                    if event:
                        yield event

            # 发送 Agent 链结束事件
            yield adapter.create_agent_node_end_event(
                invoke_id=agent_node_id,
                chain_id=chain_id,
                invoke_type="chain",
                name=ir_json.get("agentName", "Agent"),
                inputs={"query": query},
                outputs=adapter.final_output,
                child_invokes=adapter.child_invoke_ids,
            )

        except Exception as e:
            workflow_logger.error(f"ReActAgent stream failed: {e}", exc_info=True)
            yield adapter.adapt_error(f"Agent execution failed: {e}")
        finally:
            if session:
                await session.post_run()

    async def run_blocking(self, req: ExecutionRequest) -> str:
        """运行 ReActAgent 并返回完整的 LLM 响应字符串"""
        result_parts = []
        async for chunk in self.run_streaming(req):
            if chunk is None:
                continue
            try:
                chunk_str = chunk.decode() if isinstance(chunk, bytes) else chunk
                if chunk_str.startswith("data: "):
                    data = json.loads(chunk_str[6:])
                    if data.get("event") == "message":
                        answer = data.get("data", {}).get("answer", "")
                        if answer:
                            result_parts.append(answer)
            except Exception as e:
                workflow_logger.error(f"Error processing agent run blocking chunk: {e}")
                pass

        return "".join(result_parts)