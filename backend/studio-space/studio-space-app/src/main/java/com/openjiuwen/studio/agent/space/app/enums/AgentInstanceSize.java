package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;

@Getter
public enum AgentInstanceSize {
    SMALL("SMALL"),
    NORMAL("NORMAL");

    private final String size;

    AgentInstanceSize(String size) {
        this.size = size;
    }
}
