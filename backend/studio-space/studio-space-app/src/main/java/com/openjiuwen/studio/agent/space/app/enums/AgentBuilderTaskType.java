package com.openjiuwen.studio.agent.space.app.enums;

/**
 * 任务类型
 */
public enum AgentBuilderTaskType {
    NORMAL_QA(0),
    USER_AGENT(1),
    A2A(2),
    ;

    private final int type;

    AgentBuilderTaskType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
