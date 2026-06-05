/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 第三方知识库连接的状态
 *
 * @since 2025-04-11
 */
public enum KnowledgeConnectionStatus {
    /**
     * 启用
     */
    OPEN("OPEN"),

    /**
     * 停用
     */
    CLOSE("CLOSE");

    private final String status;

    KnowledgeConnectionStatus(String status) {
        this.status = status;
    }

    @Override
    @JsonValue
    public String toString() {
        return status;
    }

    @JsonCreator
    public static KnowledgeConnectionStatus fromValue(String status) {
        for (KnowledgeConnectionStatus knowledgeConnectionStatus : KnowledgeConnectionStatus.values()) {
            if (knowledgeConnectionStatus.status.equalsIgnoreCase(status)) {
                return knowledgeConnectionStatus;
            }
        }
        return null;
    }
}
