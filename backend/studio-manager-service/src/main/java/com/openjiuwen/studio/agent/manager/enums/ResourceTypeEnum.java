/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资源类型枚举类
 *
 */
@AllArgsConstructor
public enum ResourceTypeEnum {

    /**
     * 模型供应商
     */
    PROVIDER("provider", 1),

    /**
     * 模型
     */
    MODEL("model", 2),

    /**
     * 路由策略
     */
    STRATEGY("strategy", 3),

    /**
     * mcp
     */
    MCP("mcp", 5),

    /**
     * 插件
     */
    TOOL("tool", 6),

    /**
     * sql
     */
    SQL("sql", 7),

    /**
     * repo
     */
    REPO("repo", 9),

    /**
     * Skill
     */
    SKILL("skill", 10),

    /**
     * 记忆库
     */
    MEMORY("memory", 11),

    /**
     * workflow
     */
    WORKFLOW("workflow", 18),

    /**
     * workflow
     */
    AGENT("agent", 19),

    /**
     * 多智能体
     */
    CONTROLLER("controller", 20),

    /**
     * FunctionGraph
     */
    FUNCTIONGRAPH("functiongraph", 12),

    /**
     * 不支持节点
     */
    UNSUPPORTED("unsupported", 404);

    private final String value;

    /**
     * 资源导入顺序,workflow/agent/多智能体的顺序不要变
     */
    @Getter
    private final int order;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    public static ResourceTypeEnum fromValue(String text) {
        for (ResourceTypeEnum b : ResourceTypeEnum.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return ResourceTypeEnum.UNSUPPORTED;
    }

}
