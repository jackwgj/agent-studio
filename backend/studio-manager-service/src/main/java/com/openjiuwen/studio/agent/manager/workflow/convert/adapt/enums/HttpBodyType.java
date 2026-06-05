/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums;

public enum HttpBodyType {
    JSON("json"),

    NONE("none");

    final String value;

    HttpBodyType(String value) {
        this.value = value;
    }

    public String getValue(HttpBodyType httpBodyType) {
        return httpBodyType.value;
    }

    public static HttpBodyType fromValue(String type) {
        for (HttpBodyType tempType : HttpBodyType.values()) {
            if (String.valueOf(tempType.value).equalsIgnoreCase(type)) {
                return tempType;
            }
        }
        return NONE;
    }
}
