/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import lombok.Getter;

@Getter
public enum ConfigType {
    RESOURCE_LIMIT("resourceLimit");

    private final String value;

    ConfigType(String value) {
        this.value = value;
    }
}
