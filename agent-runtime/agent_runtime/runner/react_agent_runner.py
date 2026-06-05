"""ReActAgent Runner — 使用ReActAgent执行用户请求"""

from __future__ import annotations

import os
import time
from typing import AsyncGenerator, Optional

from agent_runtime.schemas.orchestration_mgr import ExecutionRequest
from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.session.agent import Session, create_agent_session
from openjiuwen.core.session.stream.base import OutputSchema
from openjiuwen.core.single_agent.agents.react_agent import ReActAgent, ReActAgentConfig
from openjiuwen.core.single_agent.schema.agent_card import AgentCard


class ReActAgentRunner:
    """使用ReActAgent运行用户请求的Runner"""

    def __init__(self, api_key: str | None = None, api_base: str | None = None):
        self._api_key = api_key or os.environ.get("API_KEY") or ""
        self._api_base = (
            api_base or os.environ.get("API_BASE", "https://api.deepseek.com")
        ).rstrip("/")

    def _create_agent(self) -> ReActAgent:
        """创建ReActAgent实例"""
        if not self._api_key:
            workflow_logger.error("API_KEY is required but not configured")
            raise ValueError(
                "API_KEY is required but not configured. Set the API_KEY environment variable."
            )
        card = AgentCard(
            id="react_agent",
            name="ReAct Agent",
            description="ReAct paradigm Agent for reasoning and acting",
        )

        config = ReActAgentConfig()
        config.configure_model_client(
            provider="OpenAI",
            api_key=self._api_key,
            api_base=self._api_base,
            model_name=os.getenv("MODEL_NAME", "deepseek-chat"),
            verify_ssl=os.getenv("LLM_SSL_VERIFY", "false").lower() == "true",
        )
        config.configure_max_iterations(max_iterations=5)
        config.configure_prompt_template(
            [
                {"role": "system", "content": "你是一个AI助手，能够帮助用户完成任务。"},
            ]
        )

        agent = ReActAgent(card)
        agent.configure(config)
        return agent

    async def run_streaming(
        self, req: ExecutionRequest, execution_id: str | None = None
    ) -> AsyncGenerator[str, None]:
        """流式运行ReActAgent并yield每个token chunk"""
        agent = self._create_agent()
        query = req.query or "Hello"

        inputs = {
            "query": query,
            "conversation_id": req.conversation_id,
            "user_id": req.user_id,
        }

        session: Optional[Session] = None
        try:
            session_id = req.conversation_id or "default_session"
            session = create_agent_session(session_id=session_id, card=agent.card)
            await session.pre_run(inputs=inputs)

            async for chunk in agent.stream(inputs, session):
                if isinstance(chunk, OutputSchema):
                    if chunk.type == "answer" and chunk.payload:
                        text = chunk.payload.get("output", "") or chunk.payload.get(
                            "response", ""
                        )
                        if text:
                            yield text
                    elif chunk.type == "error":
                        error_msg = chunk.payload.get("error", "Unknown error")
                        workflow_logger.error(
                            f"ReActAgent component error: {error_msg}"
                        )
                        yield {
                            "event": "error",
                            "data": {"response": "Agent execution failed"},
                            "executionId": execution_id or req.conversation_id,
                            "index": 0,
                            "createTime": int(time.time()),
                        }
                elif isinstance(chunk, str):
                    yield chunk
        except Exception as e:
            workflow_logger.error(f"ReActAgent stream failed: {e}", exc_info=True)
            yield {
                "event": "error",
                "data": {"response": "Agent execution failed"},
                "executionId": execution_id or req.conversation_id,
                "index": 0,
                "createTime": int(time.time()),
            }
        finally:
            if session:
                await session.post_run()

    async def run_blocking(self, req: ExecutionRequest) -> str:
        """运行ReActAgent并返回完整的LLM响应字符串"""
        agent = self._create_agent()
        query = req.query or "Hello"

        inputs = {
            "query": query,
            "conversation_id": req.conversation_id,
            "user_id": req.user_id,
        }

        result = await agent.invoke(inputs)
        return result.get("output", "")
