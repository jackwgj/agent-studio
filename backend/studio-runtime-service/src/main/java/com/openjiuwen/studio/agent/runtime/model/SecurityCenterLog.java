/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class SecurityCenterLog {
    private String id;

    @JsonProperty("model_id")
    private String modelId;

    @JsonProperty("session_id")
    private String sessionId;

    private String region;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("user_id")
    private String userId;

    private String model;

    @JsonProperty("start_timestamp")
    private long startTimestamp;

    @JsonProperty("end_timestamp")
    private long endTimestamp;

    @JsonProperty("host_ip")
    private String hostIp;

    @JsonProperty("output_lang")
    private String outputLang;

    @JsonProperty("input")
    private String input;

    @JsonProperty("output")
    private String output;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reason")
    private String reason;
}
