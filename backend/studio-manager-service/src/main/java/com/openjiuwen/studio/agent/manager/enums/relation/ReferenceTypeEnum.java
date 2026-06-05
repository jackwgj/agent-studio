/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.enums.relation;

public enum ReferenceTypeEnum {

    SHARE("share"),
    DIRECT("direct");

    private final String value;

    ReferenceTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}


