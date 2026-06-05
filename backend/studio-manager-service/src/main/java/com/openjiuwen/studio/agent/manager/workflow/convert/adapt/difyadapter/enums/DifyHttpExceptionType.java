/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.enums;

public enum DifyHttpExceptionType {
    FAIL_BRANCH("fail-branch", false),

    DEFAULT_VALUE("default-value", true),

    NONE("none", false);

    private final String value;
    private final boolean agentBuilderExceptionEnable;

    DifyHttpExceptionType(String value, boolean enable) {
        this.value = value;
        this.agentBuilderExceptionEnable = enable;
    }

    public static DifyHttpExceptionType fromValue(String type) {
        for (DifyHttpExceptionType tempType : DifyHttpExceptionType.values()) {
            if (String.valueOf(tempType.value).equalsIgnoreCase(type)) {
                return tempType;
            }
        }
        return NONE;
    }

    public boolean isEnable() {
        return agentBuilderExceptionEnable;
    }
}
