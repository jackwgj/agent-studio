/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.constants;

public enum AbilityTypeEnum {
    RETRIEVE,
    KNOWLEDGE_LIST,
    THIRD_PARTY_MODEL,
    OCR;

    public static boolean isValid(AbilityTypeEnum ability) {
        if (ability == null) {
            return false;
        }
        for (AbilityTypeEnum type : values()) {
            if (type == ability) {
                return true;
            }
        }
        return false;
    }

}
