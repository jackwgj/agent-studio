/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
public class AuthIamConfig {
    private String scope;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("domain_id")
    private String domainId;
}
