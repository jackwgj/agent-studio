/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToolDependency {

    @JsonProperty("tool_id")
    private String toolId;

    @JsonProperty("dependency_on_agent")
    private long dependencyOnAgent;

    @JsonProperty("dependency_on_workflow")
    private long dependencyOnWorkflow;

}
