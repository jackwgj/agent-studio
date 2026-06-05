/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum VariableEnum {

    TEXT_INPUT("text-input", "string"),

    PARAGRAPH("paragraph", "string"),

    SELECT("select", "string"),

    NUMBER("number", "number"),

    CHECKBOX("checkbox", "boolean"),

    FILE("file", "file/default"),

    FILE_LIST("file-list", "array"),

    ARRAY("array", "array"),

    JSON_OBJECT("json_object", "object"),

    STRING("string", "string"),

    INTEGER("", "integer");


    private final String difyVariableType;

    private final String dslType;

    VariableEnum(String difyVariableType, String dslType) {
        this.difyVariableType = difyVariableType;
        this.dslType = dslType;
    }

    /**
     * 根据dify参数类型获取variableType 如果匹配不到默认string处理
     * @param difyType
     * @return
     */
    public static VariableEnum fromDifyValue(String difyType) {
        return Arrays.stream(VariableEnum.values()).filter(variableType -> variableType.difyVariableType != null &&
                variableType.difyVariableType.equalsIgnoreCase(difyType)).findFirst().orElse(TEXT_INPUT);
    }
}
