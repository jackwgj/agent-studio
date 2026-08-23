"""Code-owned configuration for the built-in conversation Supervisor."""

from dataclasses import dataclass, field


@dataclass(frozen=True)
class SupervisorConfig:
    """Configuration normalized to the official ReAct IR shape at runtime."""

    agent_id: str
    agent_name: str
    description: str
    system_prompt: str
    model_deployment_id: str
    allowed_sub_agent_ids: tuple[str, ...] = field(default_factory=tuple)

    def to_ir(self) -> dict:
        """Build an in-memory ReAct IR view without persisting a Supervisor IR file."""
        return {
            "schemaVersion": "1.0",
            "agentId": self.agent_id,
            "agentName": self.agent_name,
            "description": self.description,
            "agentVersion": "conversation",
            "configs": {
                "mode": "ReAct",
                "sysPromptTemplate": self.system_prompt,
                "maxIteration": 5,
                "modelConfig": {
                    "modelName": self.model_deployment_id,
                    "extension": {"clientProvider": "studio"},
                },
                "plugins": [],
                "workflows": [],
                "skills": {},
                "conversationTeam": {
                    "subAgentIds": list(self.allowed_sub_agent_ids),
                },
            },
        }
