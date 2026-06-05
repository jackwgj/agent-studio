/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 查询节点类型枚举
 */
@Getter
public enum BoolQueryNodeType {
    /**
     * 组节点，用于逻辑组合
     */
    GROUP("GROUP"),

    /**
     * 规则节点，表示叶子节点，包含具体的标签值
     */
    RULE("RULE");

    @JsonValue
    private final String value;

    BoolQueryNodeType(String value) {
        this.value = value;
    }
}
