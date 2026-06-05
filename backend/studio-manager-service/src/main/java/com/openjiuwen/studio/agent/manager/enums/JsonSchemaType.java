/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * JsonSchema类型枚举
 *
 */
public enum JsonSchemaType {
    STRING("string"),
    OBJECT("object"),
    NUMBER("number"),
    INTEGER("integer"),
    ARRAY("array"),
    BOOLEAN("boolean"),
    ARRAY_STRING("array<string>"),
    ARRAY_OBJECT("array<object>"),
    NULL("null");

    /**
     * JsonSchema类型
     */
    public final String type;

    JsonSchemaType(String type) {
        this.type = type;
    }
}
