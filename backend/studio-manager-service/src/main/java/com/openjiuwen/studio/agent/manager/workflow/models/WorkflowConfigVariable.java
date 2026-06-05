/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowConfigVariable {

    private String name;

    private String type;

    private String description;

    @JsonProperty("default_value")
    private Object defaultValue;

    private String scope;
}
