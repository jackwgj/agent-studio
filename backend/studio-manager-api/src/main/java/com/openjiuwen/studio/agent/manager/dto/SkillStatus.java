/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.openjiuwen.studio.agent.common.enums.IEnum;

public enum SkillStatus implements IEnum<String> {
    DEVELOPED("developed"),

    DEVELOPING("developing");

    private final String value;

    SkillStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SkillStatus fromValue(String value) {
        for (SkillStatus b : SkillStatus.values()) {
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