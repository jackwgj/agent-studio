/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * NameSplitInfix
 *
 * @Date: 2024/8/22 9:35
 * @Description:
 */

public enum NameSplitInfix {
    /**
     * 租户自创建连接器中缀
     */
    PRIVATE_CONNECTOR("connect_split"),

    /**
     * 资产中心连接器中缀
     */
    ASSET_CONNECTOR("asset_split"),

    /**
     * 从资产中心订阅的连接器中缀
     */
    SUBSCRIBED_CONNECTOR("subscribed_split"),

    /**
     * 调试检验临时连接器中缀
     */
    TEMP_CONNECTOR("temp_split"),

    /**
     * 流中缀
     */
    PRIVATE_FLOW("flow_split"),

    /**
     * 函数中缀
     */
    FUNCTION("function_split"),

    /**
     * 连接中缀
     */
    CONNECTION("connection_split"),

    /**
     * 依赖包中缀
     */
    DEPENDENCY("depend_split");

    private final String value;

    NameSplitInfix(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
