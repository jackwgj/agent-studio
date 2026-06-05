/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * @description mcp 安装方式
 * @since 2025/7/19
 **/
public enum EnumElementType {
    /**
     * mcp 元素
     */
    MCP("mcp"),

    /**
     * 工具（连接器）
     */
    TOOL("tool"),

    /**
     * 工作流
     */
    FLOW("flow"),

    /**
     * 知识检索流
     */
    RAG_FLOW("knowledge-retrieval-flow"),

    ;

    private String value;

    EnumElementType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public String getValue() {
        return value;
    }
}

