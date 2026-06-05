/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 功能描述
 *
 * @since 2025-04-11
 */
public enum KnowledgeRepoStatus {
    /**
     * 启用
     */
    OPEN("OPEN"),

    /**
     * 停用
     */
    CLOSE("CLOSE");

    private final String status;

    KnowledgeRepoStatus(String status) {
        this.status = status;
    }

    @Override
    @JsonValue
    public String toString() {
        return status;
    }

    /**
     * 根据status枚举值，获取KnowledgeRepoStatus枚举对象
     *
     * @param status 知识库状态
     * @return KnowledgeRepoStatus
     */
    @JsonCreator
    public static KnowledgeRepoStatus fromValue(String status) {
        for (KnowledgeRepoStatus knowledgeRepoStatus : KnowledgeRepoStatus.values()) {
            if (knowledgeRepoStatus.status.equalsIgnoreCase(status)) {
                return knowledgeRepoStatus;
            }
        }
        return null;
    }
}
