/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

/**
 * 节点执行状态枚举类
 *
 */
public enum NodeExecutionStatus {
    /**
     * 运行中
     */
    RUNNING("running"),

    /**
     * 执行成功
     */
    SUCCEEDED("succeeded"),

    /**
     * 执行失败
     */
    FAILED("failed");

    public final String status;

    NodeExecutionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
