/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 功能描述 codeAgent状态枚举
 *
 */
public enum CodeAgentStatus {
    /**
     * 开发态
     */
    DEVELOP("develop"),

    /**
     * 部署态
     */
    DEPLOY("deploy");

    private final String value;

    CodeAgentStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
