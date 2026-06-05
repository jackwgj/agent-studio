/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.openjiuwen.studio.agent.common.enums.IEnum;

public enum AuthType implements IEnum<String> {
    NONE("none"),

    API_KEY("api_key"),

    IAM("iam"),

    AGENCY("agency"),

    HIS("his");

    private final String value;

    AuthType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AuthType fromValue(String value) {
        for (AuthType b : AuthType.values()) {
            if (String.valueOf(b.value).equals(value)) {
                return b;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}