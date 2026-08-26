package com.openjiuwen.studio.conversation.domain.model;

public enum ConversationEventType {
    RUN_START("run_start"),
    MESSAGE("message"),
    REASONING("reasoning"),
    TOOL_CALL("tool_call"),
    TOOL_RESULT("tool_result"),
    RUN_END("run_end"),
    ERROR("error"),
    RUN_NODE("run_node"),
    SKILL_ACTIVATED("skill_activated"),
    USAGE("usage");

    private final String value;

    ConversationEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
