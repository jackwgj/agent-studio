/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity.analytics;

/**
 * 发布通道
 *
 */
public enum AnalyticsChannel {
    API("api");

    private final String type;

    AnalyticsChannel(String type) {
        this.type = type;
    }

    public String getText() {
        return this.type;
    }
}
