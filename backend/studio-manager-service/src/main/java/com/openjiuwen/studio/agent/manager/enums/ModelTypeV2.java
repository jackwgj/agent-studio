/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ModelTypeV2 {
    LLM("LLM"),
    TEXT_EMBEDDING("Text-Embedding"),
    RERANK("RERANK"),
    TEXT_TO_IMAGE("TEXT-TO-IMAGE"),
    IMAGE_TO_TEXT("IMAGE-TO-TEXT"),
    AUDIO_TO_TEXT("AUDIO-TO-TEXT"),
    TEXT_TO_AUDIO("TEXT-TO-AUDIO"),
    TEXT_IMAGE_EMBEDDING("TEXT-IMAGE-EMBEDDING");

    private final String type;

    ModelTypeV2(String type) {
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
    public static ModelTypeV2 fromValue(String type) {
        for (ModelTypeV2 modelType : ModelTypeV2.values()) {
            if (modelType.type.equalsIgnoreCase(type)) {
                return modelType;
            }
        }
        return null;
    }
}
