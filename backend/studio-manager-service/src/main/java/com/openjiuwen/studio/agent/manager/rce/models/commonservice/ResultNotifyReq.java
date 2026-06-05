/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.commonservice;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResultNotifyReq {
    @JsonProperty("task_id")
    private String taskId;

    private String status;

    @JsonProperty("failed_reason")
    private String failedReason;

    @JsonProperty("module_name")
    private String moduleName;
}
