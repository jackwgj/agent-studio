/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model.memory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.openjiuwen.studio.agent.runtime.enums.MessageRole;

import lombok.Setter;

import java.util.Locale;

public enum MemoryExtractMessageRole {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    @Setter
    private String value;

    MemoryExtractMessageRole(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static MemoryExtractMessageRole fromValue(String text) {
        for (MemoryExtractMessageRole b : MemoryExtractMessageRole.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static MemoryExtractMessageRole fromMessageRole(MessageRole messageRole) {
        return fromValue(messageRole.name().toLowerCase(Locale.ROOT));
    }
}
