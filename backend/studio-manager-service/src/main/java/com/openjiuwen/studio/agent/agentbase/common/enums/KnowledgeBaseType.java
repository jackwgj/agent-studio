/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.common.enums;

import lombok.Getter;

import org.apache.commons.lang3.Strings;

/**
 * 知识库类型
 *
 * @since 2025-08-15
 */
@Getter
public enum KnowledgeBaseType {

    INTERNAL("internal"),
    // 第三方
    EXTERNAL("external");

    private final String value;

    KnowledgeBaseType(String value) {
        this.value = value;
    }

    public static KnowledgeBaseType fromValue(String value) {
        for (KnowledgeBaseType knowledgeBaseType : KnowledgeBaseType.values()) {
            if (Strings.CS.equals(knowledgeBaseType.getValue(), value)) {
                return knowledgeBaseType;
            }
        }
        return null;
    }

}
