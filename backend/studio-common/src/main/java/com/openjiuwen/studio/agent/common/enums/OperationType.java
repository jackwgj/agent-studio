/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

/**
 * 审计日志操作类型枚举
 */
public enum OperationType {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    READ("READ"),
    EXPORT("EXPORT"),
    IMPORT("IMPORT"),
    EXECUTE("EXECUTE"),
    ;

    private final String value;

    OperationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}