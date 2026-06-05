/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 知识库来源枚举类
 *
 */
public enum KnowledgeSource {
    /**
     * KooSearch知识库
     */
    KOOSEARCH("KooSearch"),

    /**
     * LakeSearch知识库
     */
    LAKESEARCH("LakeSearch"),

    /**
     * CUSTOM知识库
     */
    CUSTOM("CUSTOM");

    /**
     * 知识库类型
     */
    private final String source;

    KnowledgeSource(String source) {
        this.source = source;
    }

    @Override
    @JsonValue
    public String toString() {
        return source;
    }

    /**
     * 根据source枚举值，获取KnowledgeSource枚举对象
     *
     * @param source 知识库类型
     * @return KnowledgeSource
     */
    @JsonCreator
    public static KnowledgeSource fromValue(String source) {
        for (KnowledgeSource knowledgeSource : KnowledgeSource.values()) {
            if (knowledgeSource.source.equalsIgnoreCase(source)) {
                return knowledgeSource;
            }
        }
        return null;
    }
}
