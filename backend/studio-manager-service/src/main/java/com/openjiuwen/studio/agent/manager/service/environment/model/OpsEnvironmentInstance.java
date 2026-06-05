/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.environment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
public class OpsEnvironmentInstance {
    @JsonProperty("deployId")
    private String deployId;

    @JsonProperty("deployName")
    private String deployName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("agentId")
    private String agentId;

    @JsonProperty("agentName")
    private String agentName;

    @JsonProperty("environmentId")
    private String environmentId;

    @JsonProperty("environmentName")
    private String environmentName;

    @JsonProperty("api")
    private String api;

    @JsonProperty("updateTime")
    private String updateTime;

    @JsonProperty("deployMode")
    private String deployMode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("elasticStrategyId")
    private String elasticStrategyId;

    @JsonProperty("vpcEp")
    private String vpcEp;

    @JsonProperty("agentType")
    private String agentType;

    @JsonProperty("total")
    private int total;

    @JsonProperty("agentVersion")
    private String agentVersion;

    @JsonProperty("workspaceId")
    private String workspaceId;
}
