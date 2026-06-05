/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public enum IdentifyType {
    TEXT("text"),
    IMAGE("image"),
    AUDIO("audio");

    IdentifyType(String type) {
        this.msgType = type;
    }

    private final String msgType;

    /**
     * 根据type枚举值，获取KnowledgeType枚举对象
     *
     * @param type 知识库类型
     * @return KnowledgeType
     */
    @JsonCreator
    public static IdentifyType fromValue(String type) {
        for (IdentifyType value : IdentifyType.values()) {
            if (value.msgType.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
