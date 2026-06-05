/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 沙箱服务的请求体
 *
 */
@Data
public class PythonInterpreterReq {
    @JsonProperty("code")
    private String code;

    @JsonProperty("session_id")
    private String sessionId;
}
