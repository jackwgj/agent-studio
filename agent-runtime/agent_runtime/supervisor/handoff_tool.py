# -*- coding: UTF-8 -*-
"""Handoff 工具 —— 监督者 ReActAgent 调用的工具，把任务交给具体子 Agent。

子 Agent 是系统中已注册的 Agent，通过其已有 IR（OBS agent/ir/{agentId}/{agentId}.json）
加载，不重新上传、不生成新 IR。

HandoffTool 是无状态工具（D0-2/D0-8）：不绑定会话、不绑定模型；子 Agent 的模型来自其
自身 IR 的 modelConfig.modelName，会话为每次调用一次性创建。
"""

import uuid

from openjiuwen.core.foundation.tool import Tool, ToolCard
from openjiuwen.core.session.agent import create_agent_session
from openjiuwen.core.single_agent.agents.react_agent import ReActAgent
from openjiuwen.core.single_agent.schema.agent_card import AgentCard

from agent_runtime.supervisor.config import build_react_config
from jiuwen.serve.controllers.execution.open_utils import async_ir_load


class HandoffTool(Tool):
    """Handoff 工具：invoke 时加载子 Agent IR → 构建子 ReActAgent → 跑 query → 返回回答。"""

    def __init__(
        self,
        agent_id: str,
        description: str,
    ):
        self.agent_id = agent_id
        super().__init__(
            card=ToolCard(
                id=f"handoff_{agent_id}",
                name=f"transfer_to_{agent_id}",
                description=description or f"将任务移交给子 Agent {agent_id} 处理",
                input_params={
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "需要子 Agent 处理的用户问题",
                        }
                    },
                    "required": ["query"],
                },
            )
        )

    def _ir_path(self) -> str:
        return f"agent/ir/{self.agent_id}/{self.agent_id}.json"

    def _extract_query(self, inputs) -> str:
        if isinstance(inputs, dict):
            return str(inputs.get("query", ""))
        return str(getattr(inputs, "query", inputs))

    async def invoke(self, inputs, **kwargs):
        query = self._extract_query(inputs)
        return await self._run_sub_agent(query)

    async def stream(self, inputs, **kwargs):
        result = await self.invoke(inputs, **kwargs)
        yield result

    def _build_sub_agent(self, ir_data: dict) -> ReActAgent:
        """从子 Agent IR 构建子 ReActAgent（system prompt + model）。

        子 Agent 的模型只取自身 IR 的 modelConfig.modelName，缺失/无效显式报错，不拿监督者模型
        兜底（A6）。⚠️ IR 字段名 `modelName` 实际存的是**部署 id**（D0-8），由路由解析成真实
        模型名——故变量命名用 model_deployment_id，勿理解成模型名。
        """
        configs = ir_data.get("configs", {})
        model_config = configs.get("modelConfig", {})
        model_deployment_id = model_config.get("modelName")
        if not model_deployment_id or str(model_deployment_id).lower() == "null":
            raise RuntimeError(
                f"子 Agent {self.agent_id} 的 modelConfig.modelName 无效（{model_deployment_id}），"
                f"请检查该 Agent 是否已绑定有效模型部署"
            )
        system_prompt = configs.get("sysPromptTemplate") or ""

        agent = ReActAgent(
            card=AgentCard(
                id=f"sub_{self.agent_id}",
                name=self.agent_id,
                description="子 Agent",
            )
        )
        agent.configure(build_react_config(system_prompt, model_deployment_id))
        return agent

    async def _run_sub_agent(self, query: str):
        """加载子 Agent IR → 构建子 ReActAgent → 跑 query → 返回最终回答。

        子 Agent 会话为一次性 uuid4()，无 conversation 前缀（子 Agent 与会话无关，D0-2）。
        """
        try:
            ir_data = await async_ir_load(self._ir_path())
        except Exception as e:
            raise RuntimeError(
                f"子 Agent {self.agent_id} IR 加载失败（{self._ir_path()}）: {e}"
            ) from e

        sub_agent = self._build_sub_agent(ir_data)
        session = create_agent_session(session_id=str(uuid.uuid4()))
        try:
            result = await sub_agent.invoke({"query": query}, session)
        except Exception as e:
            raise RuntimeError(f"子 Agent {self.agent_id} 执行失败: {e}") from e

        output = result.get("output", result)
        if isinstance(output, dict):
            output = output.get("result", str(output))
        return {"result": f"[子Agent {self.agent_id}] {output}"}
