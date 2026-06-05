/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.enums;

import lombok.Getter;

/**
 * 查询操作符类型枚举
 */
@Getter
public enum BoolQueryOperatorType {
    /**
     * 逻辑与操作符
     */
    AND("AND"),

    /**
     * 逻辑或操作符
     */
    OR("OR"),

    /**
     * 逻辑非操作符
     */
    NOT("NOT");

    private final String value;

    BoolQueryOperatorType(String value) {
        this.value = value;
    }

}
