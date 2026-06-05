package com.openjiuwen.studio.agent.space.app.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务操作类型
 * 1：删除；2：暂停；3：取消暂停；4：置顶；5：取消置顶；6：打分
 */
public enum AgentBuilderTaskOperationType {
    DELETE(1),
    PAUSE(2),
    CANCEL_PAUSE(3),
    PIN(4),
    CANCEL_PIN(5),
    RATE(6),
    STOP(7),
    ;

    private final int type;

    private static final Map<Integer, AgentBuilderTaskOperationType> LOOKUP = new HashMap<>();

    AgentBuilderTaskOperationType(int type) {
        this.type = type;
    }

    // 静态初始化块，在类加载时执行
    static {
        for (AgentBuilderTaskOperationType type : values()) {
            LOOKUP.put(type.getType(), type);
        }
    }

    public int getType() {
        return type;
    }

    public static AgentBuilderTaskOperationType getByType(int status) {
        return LOOKUP.get(status);
    }
}
