/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import lombok.Getter;

public enum VisibilityEnum {
    USER("user", 1),
    PROJECT("project", 2),
    WORKSPACE("workspace", 4),
    DOMAIN("domain", 8),
    GLOBAL("global", 16);

    @Getter
    private String value;

    @Getter
    private int scope;

    VisibilityEnum(String value, int scope) {
        this.value = value;
        this.scope = scope;
    }
}
