/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.enums;

import com.openjiuwen.studio.agent.common.dto.agent.Status;

import lombok.Getter;

/**
 * 节点运行状态
 *
 */
public enum WorkflowRunStatus {
    RUNNING(0, "running"),
    SUCCEEDED(1, "succeeded"),
    FAILED(2, "failed"),
    WAITING(3, "waiting"),
    ABORTED(4, "aborted");

    @Getter
    private Status status;

    WorkflowRunStatus(Integer code, String desc) {
        status = new Status();
        status.setCode(code);
        status.setDesc(desc);
    }
}
