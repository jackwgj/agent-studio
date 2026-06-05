/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 功能描述 agent状态枚举
 *
 */
public enum AgentStatus {
    /**
     * 草稿
     */
    DRAFT("draft"),

    /**
     * 发布
     */
    PUBLISHED("published");

    private final String value;

    AgentStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
