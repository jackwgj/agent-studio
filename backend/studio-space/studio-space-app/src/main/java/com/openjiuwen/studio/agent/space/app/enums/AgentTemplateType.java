package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;

@Getter
public enum AgentTemplateType {
    QA_AI("QA_AI"), // Question & Answer 问答基础版
    AN_AI("AN_AI"), // Ask Number 问数（问答专业版）
    DEFAULT("DEFAULT");

    private final String type;

    AgentTemplateType(String type) {
        this.type = type;
    }
}
