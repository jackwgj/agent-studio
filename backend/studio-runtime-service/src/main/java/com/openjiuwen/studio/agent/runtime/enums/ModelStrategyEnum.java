/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.enums;

import lombok.Getter;

public enum ModelStrategyEnum {

    MODEL("model"),
    ROUTER("router");

    @Getter
    private final String type;

    ModelStrategyEnum(String type) {
        this.type = type;
    }
}
