/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums;

/**
 * 第三方工作流类型枚举
 *
 */
public enum ThirdpartyWorkflowType {

    /**
     * Dify
     */
    DIFY("Dify");

    private final String type;

    ThirdpartyWorkflowType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * 类型转换，不存在则返回空值
     *
     * @param type 类型
     * @return
     */
    public static ThirdpartyWorkflowType fromValue(String type) {
        for (ThirdpartyWorkflowType thirdpartyWorkflow : ThirdpartyWorkflowType.values()) {
            if (thirdpartyWorkflow.type.equalsIgnoreCase(type)) {
                return thirdpartyWorkflow;
            }
        }
        return null;
    }
}
