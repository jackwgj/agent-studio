/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.openjiuwen.studio.agent.common.enums.IEnum;

public enum SecretLevel implements IEnum<String> {
    NONE("None"),

    ENCRYPTION("Encryption");

    private final String value;

    SecretLevel(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SecretLevel fromValue(String value) {
        for (SecretLevel b : SecretLevel.values()) {
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