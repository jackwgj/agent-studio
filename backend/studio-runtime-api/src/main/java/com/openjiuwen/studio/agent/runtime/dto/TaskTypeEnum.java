/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.openjiuwen.studio.agent.common.enums.IEnum;

public enum TaskTypeEnum implements IEnum<String> {
    CHAT("chat"),

    TASK("task"),

    AGENT("agent"),

    CONTROLLER("controller");

    TaskTypeEnum(String value) {
        this.value = value;
    }

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskTypeEnum fromValue(String value) {
        for (TaskTypeEnum b : TaskTypeEnum.values()) {
            if (String.valueOf(b.value).equals(value)) {
                return b;
            }
        }
        return null;
    }
}