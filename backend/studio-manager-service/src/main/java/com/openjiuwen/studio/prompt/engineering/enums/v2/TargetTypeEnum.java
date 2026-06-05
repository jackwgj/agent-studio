/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.enums.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 提示词优化任务目标
 */
public enum TargetTypeEnum {
    /**
     * 客观任务
     */
    OBJECTIVE("objective"),

    /**
     * 主观任务
     */
    SUBJECTIVE("subjective");

    private String value;

    TargetTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TargetTypeEnum fromValue(String text) {
        for (TargetTypeEnum b : TargetTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
