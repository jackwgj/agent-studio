package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 快捷指令操作类型
 * 1：创建；2：修改；3：删除；4：开关；
 */
@Getter
public enum QuickCommandOperationType {
    ADD(1),
    UPDATE(2),
    DELETE(3),
    ENABLE(4);

    private final int type;

    private static final Map<Integer, QuickCommandOperationType> LOOKUP = new HashMap<>();

    QuickCommandOperationType(int type) {
        this.type = type;
    }

    // 静态初始化块，在类加载时执行
    static {
        for (QuickCommandOperationType type : values()) {
            LOOKUP.put(type.getType(), type);
        }
    }

    public static QuickCommandOperationType getByType(int status) {
        return LOOKUP.get(status);
    }
}
