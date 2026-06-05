/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.openjiuwen.studio.agent.common.enums.IEnum;

public enum MemberRole implements IEnum<String> {
    OWNER("OWNER"),

    ADMIN("ADMIN"),

    DEVELOPER("DEVELOPER"),

    OPERATOR("OPERATOR");

    private final String value;

    MemberRole(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MemberRole fromValue(String value) {
        for (MemberRole b : MemberRole.values()) {
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