/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums.controller;

import lombok.Getter;

/**
 * 控制器节点类型
 *
 */
public enum AgentNodeType {
    /**
     * 控制器主节点
     */
    CONTROLLER("Controller"),

    /**
     * 控制器工作流节点
     */
    WORKFLOW("Workflow"),

    /**
     * 控制器子控制器节点
     */
    SUB_CONTROLLER("SubController"),

    /**
     * 单智能体节点
     */
    AGENT("Agent");

    @Getter
    private final String type;

    AgentNodeType(String type) {
        this.type = type;
    }

}
