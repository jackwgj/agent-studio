# Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
"""Facade that exposes a commercial Agent through the openJiuwen BaseAgent contract."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any, AsyncIterator, Optional

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from openjiuwen.core.single_agent.base import BaseAgent
from openjiuwen.core.single_agent.schema.agent_card import AgentCard

if TYPE_CHECKING:
    from jiuwen.controller.agent.agent import Agent


class OpenJiuwenAgentFacade(BaseAgent):
    """Expose a commercial Agent as an openJiuwen BaseAgent.

    The facade has two responsibilities:
    1. Satisfy the northbound openJiuwen ``BaseAgent`` interface so it can be
       executed by ``Runner.run_agent_streaming``.
    2. Preserve the existing commercial ``Agent`` surface used by the current
       execution pipeline, such as state save/load and control-mode inspection.
    """

    def __init__(self, delegate_agent: "Agent"):
        """Initialize the facade with a commercial Agent delegate.

        Args:
            delegate_agent: Commercial agent instance that keeps the existing
                business execution logic.
        """
        if delegate_agent is None:
            raise ValueError("delegate_agent is required")

        self._delegate_agent = delegate_agent
        self._config = None
        super().__init__(card=self._build_card(delegate_agent))

    @staticmethod
    def _build_card(delegate_agent: "Agent") -> AgentCard:
        """Build the minimum AgentCard required by openJiuwen Runner.

        Args:
            delegate_agent: Commercial agent instance.

        Returns:
            AgentCard: Minimal card carrying stable id/name metadata.
        """
        agent_id = (
            getattr(delegate_agent, "agent_id", None)
            or getattr(delegate_agent, "task_id", None)
            or "jiuwen_agent"
        )
        return AgentCard(
            id=agent_id,
            name=agent_id,
            description="Jiuwen commercial agent facade",
        )

    def configure(self, config) -> "OpenJiuwenAgentFacade":
        """Store openJiuwen config on the facade.

        Args:
            config: OpenJiuwen agent config object.

        Returns:
            OpenJiuwenAgentFacade: Current facade for chaining.
        """
        self._config = config
        return self

    @staticmethod
    def _ensure_dict_inputs(inputs: Any) -> dict:
        """Validate runner inputs are dict-like.

        Args:
            inputs: Runner inputs payload.

        Returns:
            dict: Normalized dict inputs.

        Raises:
            JiuWenBaseException: If inputs are not dict-like.
        """
        if not isinstance(inputs, dict):
            raise JiuWenBaseException(
                error_code=StatusCode.AGENT_INPUT_TYPE_ERROR.code,
                message=StatusCode.AGENT_INPUT_TYPE_ERROR.errmsg.format(type(inputs)),
            )
        return inputs

    @staticmethod
    def _extract_runtime_kwargs(inputs: dict) -> dict:
        """Extract commercial-agent runtime kwargs from dict-like inputs.

        Args:
            inputs: Runner inputs payload.

        Returns:
            dict: Runtime kwargs for ``Agent.astream``.

        Raises:
            JiuWenBaseException: If the bridge payload is not a dict.
        """
        runtime_kwargs = inputs.get("_jiuwen_runtime_kwargs", {})
        if runtime_kwargs is None:
            runtime_kwargs = {}
        if not isinstance(runtime_kwargs, dict):
            raise JiuWenBaseException(
                error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
                message="_jiuwen_runtime_kwargs must be dict",
            )
        return {
            "stream": runtime_kwargs.get("stream", True),
            "runtime_context": runtime_kwargs.get("runtime_context"),
            "tool_switch_dict": runtime_kwargs.get("tool_switch_dict"),
            "trace_handlers": runtime_kwargs.get("trace_handlers"),
        }

    async def invoke(self, inputs: Any, session: Optional[Any] = None, **kwargs) -> Any:
        """Blocking execution is intentionally unsupported in the current service.

        Args:
            inputs: Agent inputs.
            session: OpenJiuwen session object.
            **kwargs: Additional invoke kwargs.

        Raises:
            JiuWenBaseException: Always, because blocking agent execution is not
                supported by the current northbound service.
        """
        raise JiuWenBaseException(
            error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
            message="responseMode Blocking is not supported yet.",
        )

    async def stream(
        self,
        inputs: Any,
        session: Optional[Any] = None,
        stream_modes: Optional[list[Any]] = None,
        **kwargs,
    ) -> AsyncIterator[Any]:
        """Bridge openJiuwen stream invocation to the commercial Agent stream.

        Args:
            inputs: Dict-like inputs containing ``query``, ``conversation_id``
                and ``_jiuwen_runtime_kwargs``.
            session: OpenJiuwen session object created by Runner.
            stream_modes: Optional openJiuwen stream modes.
            **kwargs: Additional stream kwargs from openJiuwen runtime.

        Yields:
            Any: Unmodified stream chunks produced by the commercial Agent.
        """
        normalized_inputs = self._ensure_dict_inputs(inputs)
        runtime_kwargs = self._extract_runtime_kwargs(normalized_inputs)
        async for item in self._delegate_agent.astream(
            normalized_inputs,
            stream=runtime_kwargs["stream"],
            runtime_context=runtime_kwargs["runtime_context"],
            tool_switch_dict=runtime_kwargs["tool_switch_dict"],
            trace_handlers=runtime_kwargs["trace_handlers"],
        ):
            yield item

    @property
    def task_id(self) -> str:
        """Return the delegate task id."""
        return self._delegate_agent.task_id

    @property
    def agent_id(self) -> str:
        """Return the delegate agent id."""
        return self._delegate_agent.agent_id

    def get_control_mode(self):
        """Proxy control mode lookup to the delegate agent."""
        return self._delegate_agent.get_control_mode()

    def get_task_end(self):
        """Proxy task completion lookup to the delegate agent."""
        return self._delegate_agent.get_task_end()

    async def save_state(self, key):
        """Proxy state persistence to the delegate agent."""
        return await self._delegate_agent.save_state(key)

    async def load_state(self, state):
        """Proxy state restoration to the delegate agent."""
        return await self._delegate_agent.load_state(state)

    async def get_state(self):
        """Proxy state retrieval to the delegate agent."""
        return await self._delegate_agent.get_state()

    def clear_state(self):
        """Proxy in-memory cleanup to the delegate agent."""
        return self._delegate_agent.clear_state()

    def __getattr__(self, name: str):
        """Delegate unknown attribute access to the commercial agent."""
        return getattr(self._delegate_agent, name)
