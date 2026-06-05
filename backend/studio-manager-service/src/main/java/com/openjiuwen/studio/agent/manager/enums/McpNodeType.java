/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.enums;

/**
 * Mcp节点类型
 */
public enum McpNodeType {
    Mcp("Mcp"),

    DataAcquisition("DataAcquisition"),

    DataProcess("DataProcess");

    private String value;

    McpNodeType(String value) {
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
