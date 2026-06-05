/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 模型类型
 *
 * @since 2025-04-14
 */
public enum ModelType {
    /**
     * 搜索向量化模型
     */
    EMBEDDING("embedding"),

    /**
     * 搜索精排模型
     */
    RERANK("rerank"),

    /**
     * OCR模型
     */
    OCR("ocr"),

    /**
     * 大语言模型
     */
    NLP("nlp");

    private final String type;

    ModelType(String type) {
        this.type = type;
    }

    @Override
    @JsonValue
    public String toString() {
        return this.type;
    }

    /**
     * 根据模型类型的值返回枚举值
     *
     * @param type 模型类型
     * @return ModelType
     */
    @JsonCreator
    public static ModelType fromValue(String type) {
        for (ModelType modelType : ModelType.values()) {
            if (modelType.type.equalsIgnoreCase(type)) {
                return modelType;
            }
        }
        return null;
    }
}
