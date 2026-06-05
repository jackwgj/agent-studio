/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 知识库类型枚举类
 *
 */
public enum KnowledgeType {
    /**
     * 用户在Agent平台创建的知识库
     */
    INTERNAL("internal"),

    /**
     * 用户在知识库平台创建的知识库，在Agent平台中进行引用
     */
    EXTERNAL("external");

    /**
     * 知识库类型
     */
    private final String type;

    KnowledgeType(String type) {
        this.type = type;
    }

    @Override
    @JsonValue
    public String toString() {
        return type;
    }

    /**
     * 根据type枚举值，获取KnowledgeType枚举对象
     *
     * @param type 知识库类型
     * @return KnowledgeType
     */
    @JsonCreator
    public static KnowledgeType fromValue(String type) {
        for (KnowledgeType knowledgeType : KnowledgeType.values()) {
            if (knowledgeType.type.equals(type)) {
                return knowledgeType;
            }
        }
        return null;
    }
}
