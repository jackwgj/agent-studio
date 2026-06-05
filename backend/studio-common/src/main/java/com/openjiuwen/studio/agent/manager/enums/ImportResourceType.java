/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ImportResourceType {

    AGENT("AGENT"),

    WORKFLOW("WORKFLOW"),

    PLUGIN("PLUGIN");


    private final String value;

    ImportResourceType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
