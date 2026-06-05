/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.iam;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Project
 *
 */
@Data
public class Project {
    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;
}
