package com.openjiuwen.studio.agent.space.api.vo;

import lombok.Getter;

@Getter
public enum AgentType {
    BUILT_IN(1, "BUILT_IN"),
    CHAT_WORKFLOW(2, "chat"),
    SINGLE_AGENT(3, "agent"),
    MULTI_AGENT(4, "controller"),
    TASK_WORKFLOW(5, "task"),
    DEEP_RESEARCH(6, "deep_research"),
    ;

    private final Integer code;

    private final String type;

    AgentType(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    public static AgentType getTypeByCode(Integer code) {
        for (AgentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static Integer getAgentCode(String type) {
        switch (type) {
            case "agent" -> {
                return SINGLE_AGENT.getCode();
            }
            case "controller" -> {
                return MULTI_AGENT.getCode();
            }
            case "chat" -> {
                return CHAT_WORKFLOW.getCode();
            }
            case "task" -> {
                return TASK_WORKFLOW.getCode();
            }
            case "deep_research" -> {
                return DEEP_RESEARCH.getCode();
            }
            default -> {
                return -1;
            }
        }
    }
}
