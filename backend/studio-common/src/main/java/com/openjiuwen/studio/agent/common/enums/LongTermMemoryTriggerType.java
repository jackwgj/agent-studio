/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 长期记忆的触发类型
 *
 */
public enum LongTermMemoryTriggerType {

    // 表示在对话中检索使用
    SEARCH("search");

    private String value;

    LongTermMemoryTriggerType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}