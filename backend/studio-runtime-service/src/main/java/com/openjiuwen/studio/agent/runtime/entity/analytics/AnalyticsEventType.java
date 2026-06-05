/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity.analytics;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 分析事件类型
 *
 */
public enum AnalyticsEventType {
    /**
     * 运行成功
     */
    COMPLETE("complete"),
    /**
     * 运行失败
     */
    FAIL("fail"),
    /**
     * 点踩
     */
    DISLIKE("dislike"),
    /**
     * 点赞
     */
    LIKE("like");

    private final String type;

    AnalyticsEventType(String type) {
        this.type = type;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(type);
    }
}
