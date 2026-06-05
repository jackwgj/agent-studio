/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ShareScopeEnum {

    ALL,
    GLOBAL,
    PARTIAL,
    SELF;

    @JsonCreator
    public static ShareScopeEnum fromValue(String text) {
        for (ShareScopeEnum scopeEnum : ShareScopeEnum.values()) {
            if (String.valueOf(scopeEnum).equals(text)) {
                return scopeEnum;
            }
        }
        return null;
    }
}
