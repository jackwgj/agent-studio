/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mcp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
public class McpRemoteValidation {
    @JsonProperty("success")
    private boolean isSuccess = true;

    @JsonProperty("error_msg")
    private String errorMsg = null;

    public McpRemoteValidation(boolean isSuccess, String errorMsg) {
        this.isSuccess = isSuccess;
        this.errorMsg = errorMsg;
    }
}
