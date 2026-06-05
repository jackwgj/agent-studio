/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

/**
 * @description mcp 安装方式
 * @since 2025/5/9
 **/
public enum EnumMcpStatus {
    /**
     * 创建中
     */
    CREATING_STACK("creatingStack"),

    /**
     * 资源栈创建失败
     */
    STACK_FAIL("stackFail"),

    /**
     * 创建成功
     */
    SUCCESS("success"),

    /**
     * 删除成功
     */
    DELETED("deleted"),

    /**
     * 删除失败
     */
    DELETION_FAIL("deletionFail"),

    /**
     * 删除中
     */
    DELETING("deleting"),

    /**
     * 未安装
     */
    NOT_INSTALLED("notInstalled"),

    /**
     * 待获取工具
     */
    PENDING_TOOLS("pendingTools");

    private String value;

    EnumMcpStatus(String value) {
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

