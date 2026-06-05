package com.openjiuwen.studio.agent.space.app.enums;

/**
 * 消息类型
 * 1：用户；2：系统
 */
public enum AgentBuilderMsgType {
    USER(1),
    SYSTEM(2);

    private final int type;

    AgentBuilderMsgType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
