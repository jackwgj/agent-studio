/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mcp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

/**
 * 功能描述
 *
 */
@Builder
public class McpRemoteTool {
    @JsonProperty("name")
    private String name;

    @JsonProperty("desc")
    private String desc;

    @JsonProperty("input")
    private String input;

    @JsonProperty("output")
    private String output;

    @JsonProperty("params_count")
    private int paramsCount;
}
