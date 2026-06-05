"""Integration layers for replacing jiuwen southbound dependencies."""

__all__ = ["AgentCoreModelLayer", "AgentCoreMcpLayer", "AgentCoreRestfulLayer"]


def __getattr__(name: str):
    if name == "AgentCoreMcpLayer":
        from jiuwen.integration.agent_core_mcp_new import AgentCoreMcpLayer

        return AgentCoreMcpLayer
    if name == "AgentCoreModelLayer":
        from jiuwen.integration.agent_core_model_new import AgentCoreModelLayer

        return AgentCoreModelLayer
    if name == "AgentCoreRestfulLayer":
        from jiuwen.integration.agent_core_restful_new import AgentCoreRestfulLayer

        return AgentCoreRestfulLayer
    raise AttributeError(f"module 'jiuwen.integration' has no attribute {name!r}")
