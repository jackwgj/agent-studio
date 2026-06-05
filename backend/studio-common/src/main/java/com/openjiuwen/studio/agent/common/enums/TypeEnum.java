/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 功能描述 工作流类型枚举
 *
 */
public enum TypeEnum {
    /**
     * 整型
     */
    INTEGER("integer"),

    /**
     * 浮点型
     */
    NUMBER("number"),

    /**
     * 字符串型
     */
    STRING("string"),

    /**
     * 列表类型
     */
    ARRAY("array"),

    /**
     * bool型
     */
    BOOLEAN("boolean"),

    /**
     * 对象类型
     */
    OBJECT("object"),

    /**
     * 时间类型
     */
    TIME("time"),

    /**
     * 文件类型
     */
    FILE("file");

    private final String value;

    TypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    /**
     * 字符转type枚举
     *
     * @param value type值
     * @return 返回type枚举
     */
    public static TypeEnum fromValue(String value) {
        for (TypeEnum e : TypeEnum.values()) {
            if (e.value.equalsIgnoreCase(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("not support " + value);
    }

}
