/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * 工具类型枚举
 *
 */
public enum ToolType {
    /**
     * 自定义工具
     */
    CUSTOM("custom"),
    /**
     * 预置工具
     */
    INNER("inner"),
    /**
     * 工作流工具
     */
    WORKFLOW("workflow"),
    /**
     * 自定义工具+预定义工具
     */
    ALL("all");

    /**
     * 工具类型
     */
    public final String type;

    ToolType(String type) {
        this.type = type;
    }
}
