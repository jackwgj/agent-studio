package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;

@Getter
public enum AgentWorkType {
    AGENT_LLM("agent_llm"),
    AGENT_SINGLE_WORKFLOW("agent_single_workflow"),
    AGENT_OPERATOR("agent_operator");

    private String agentWorkType;

    AgentWorkType(String agentWorkType) {
        this.agentWorkType = agentWorkType;
    }
}
