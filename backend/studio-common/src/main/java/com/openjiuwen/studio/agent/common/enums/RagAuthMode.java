/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 知识库鉴权方式枚举
 *
 */
public enum RagAuthMode {
    /**
     * 无需鉴权
     */
    NONE("none"),

    /**
     * basic认证
     */
    BASIC("basic"),

    /**
     * kerberos认证
     */
    KERBEROS("kerberos");

    /**
     * 鉴权类型
     */
    private final String authMode;

    RagAuthMode(String authMode) {
        this.authMode = authMode;
    }

    @Override
    @JsonValue
    public String toString() {
        return authMode;
    }
}
