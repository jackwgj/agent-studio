/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.openjiuwen.studio.agent.common.enums.IEnum;

public enum SkillSource implements IEnum<String> {
    CUSTOM("custom"),

    IMPORT("import");

    private final String value;

    SkillSource(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SkillSource fromValue(String value) {
        for (SkillSource b : SkillSource.values()) {
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