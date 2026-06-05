/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 提问器节点的模式
 *
 */
public enum QuestionerNodeMode {
    /**
     * 效果优先
     */
    EFFECT("effect"),

    /**
     * 速度优先
     */
    SPEED("speed");

    private final String mode;

    QuestionerNodeMode(String mode) {
        this.mode = mode;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(mode);
    }
}
