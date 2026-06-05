/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums.controller;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.Getter;

public enum WorkflowType {
    /**
     * 开始工作流类型
     */
    START("Start"),

    /**
     * 子工作流类型
     */
    NORMAL("Normal"),

    /**
     * 意图工作流类型
     */
    INTENT("IntentIdentification"),

    /**
     * 默认工作流类型
     */
    DEFAULT("Default"),

    /**
     * 结束工作流类型
     */
    END("End");

    @Getter
    private final String type;

    WorkflowType(String type) {
        this.type = type;
    }

    /**
     * 根据type获取枚举对象
     *
     * @param type type字符
     * @return 返回枚举
     */
    public static WorkflowType typeOf(String type) {
        for (WorkflowType workflowType : WorkflowType.values()) {
            if (workflowType.getType().equalsIgnoreCase(type)) {
                return workflowType;
            }
        }
        throw new AgentStudioException(StudioError.MULTI_AGENT_UNSUPPORTED_WORKFLOW_TYPE);
    }
}
