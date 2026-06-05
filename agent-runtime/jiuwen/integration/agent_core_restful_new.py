"""Agent-core RESTful layer with jiuwen-compatible southbound interface."""

from typing import Any

from jiuwen.plugin.models.restfulapi import RestFulAPI


class AgentCoreRestfulLayer(RestFulAPI):
    """Adapt agent-core RESTful runtime to jiuwen's RestFulAPI contract.

    Inherits RestFulAPI for isinstance compatibility.
    Does NOT call super().__init__() — all attributes are proxied
    through __getattr__ to the underlying ToolWrapper runtime.
    """

    def __init__(
        self,
        runtime: Any = None,
        *,
        tool_id: str = "",
        agent_id: str = "",
        session_id: str = "",
    ) -> None:
        super().__init__(
            name="",
            description="",
            params=[],
            path="",
            headers={},
            method="GET",
            response=[],
            plugin_name="",
            plugin_desc="",
            auth={},
        )
        self._runtime = runtime or self._create_runtime(
            tool_id=tool_id,
            agent_id=agent_id,
            session_id=session_id,
        )

    @staticmethod
    def _create_runtime(*, tool_id: str, agent_id: str, session_id: str) -> Any:
        from jiuwen.extension.wrapper import ToolType
        from jiuwen.extension.wrapper.tool_wrapper import ToolWrapper

        return ToolWrapper(
            tool_id=tool_id,
            agent_id=agent_id,
            session_id=session_id,
            tool_type=ToolType.RESTFUL,
        )

    def __getattr__(self, name: str) -> Any:
        if name == "_runtime":
            raise AttributeError(name)
        return getattr(self._runtime, name)

    async def ainvoke(self, inputs, **kwargs):
        return await self._runtime.ainvoke(inputs, **kwargs)
