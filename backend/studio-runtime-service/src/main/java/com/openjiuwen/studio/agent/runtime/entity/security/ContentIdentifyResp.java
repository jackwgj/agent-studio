/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ContentIdentifyResp
 *
 */
@Data
@Accessors(chain = true)
public class ContentIdentifyResp {

    @JsonProperty("security_result")
    private String securityResult;

    @JsonProperty("task_id")
    private String taskId;
}
