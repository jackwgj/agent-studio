/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * 功能描述 agent状态枚举
 *
 */
public enum EnumExecutionTiming {
    /**
     * 预处理
     */
    BEFORE_ENTRY("BEFORE_ENTRY"),

    /**
     * 退出前
     */
    AFTER_QUIT("AFTER_QUIT"),

    /**
     * 提参后
     */
    AFTER_EXTRACTION("AFTER_EXTRACTION");

    private final String value;

    EnumExecutionTiming(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
