/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums.controller;

import lombok.Getter;

/**
 * 控制器节点类型
 *
 */
public enum AgentMode {
    /**
     * 多智能体类型
     */
    CONTROLLER("Controller"),

    /**
     * PlanExecute类型单智能体
     */
    PLANEXECUTE("PlanExecute");

    @Getter
    private final String mode;

    AgentMode(String mode) {
        this.mode = mode;
    }

}
