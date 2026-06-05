/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 凭证类型枚举
 *
 */
public enum CredentialType {
    /**
     * obs读取
     */
    OBS("obs"),

    /**
     * 动态委托
     */
    DELEGATION("delegation");

    private final String value;

    CredentialType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    public static CredentialType fromValue(String value) {
        for (CredentialType c : CredentialType.values()) {
            if (c.value.equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("not support " + value);
    }

}
