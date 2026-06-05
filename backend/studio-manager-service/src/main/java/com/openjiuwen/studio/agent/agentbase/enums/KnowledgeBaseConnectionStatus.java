/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import lombok.Getter;

import org.apache.commons.lang3.Strings;

@Getter
public enum KnowledgeBaseConnectionStatus {

    /**
     * 开启
     */
    OPEN("OPEN"),

    /**
     * 通用
     */
    CLOSE("CLOSE");

    private final String value;

    KnowledgeBaseConnectionStatus(String value) {
        this.value = value;
    }

    public static KnowledgeBaseConnectionStatus fromValue(String value) {
        for (KnowledgeBaseConnectionStatus status : KnowledgeBaseConnectionStatus.values()) {
            if (Strings.CS.equals(status.getValue(), value)) {
                return status;
            }
        }
        return null;
    }

}
