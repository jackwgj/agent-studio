/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * 工具参数的位置
 *
 */
public enum ToolParamLocation {
    /**
     * 请求体
     */
    BODY("Body"),

    /**
     * 请求头
     */
    HEADER("Headers"),

    /**
     * Query
     */
    QUERY("Query"),

    /**
     * Path
     */
    PATH("Path");

    /**
     * 类型
     */
    public final String location;

    ToolParamLocation(String location) {
        this.location = location;
    }
}
