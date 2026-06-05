/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import com.openjiuwen.studio.agent.common.dto.agent.Status;
import lombok.Getter;

/**
 * 节点运行状态
 *
 */
public enum NodeRunStatus {
    SUCCEED(1, "succeed"),
    FAILED(2, "failed"),
    WAIT(3, "wait");

    @Getter
    private Status status;

    NodeRunStatus(Integer code, String desc) {
        status = new Status();
        status.setCode(code);
        status.setDesc(desc);
    }
}
